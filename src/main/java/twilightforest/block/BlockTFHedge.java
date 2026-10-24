package twilightforest.block;

import java.util.List;
import java.util.Random;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySpider;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.RaycastCollision;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;

/**
 * Hedge (metadata 0, prickly) and dark hedge (metadata 1, darkwood leaves look, harmless; metadata 2 is the old
 * "temporary workaround" value and behaves like 0). Balance (MITE_BALANCE B2): wood, hardness 2.0, harvest level 1
 * (flint axe), contact damage 3.
 * <p>
 * The 1.7.10 class extended BlockLeavesBase, but that is not a leaf block there ({@code isLeaves} is false). MITE's
 * BlockLeavesBase would make it a tree leaf (leaf decay bits would corrupt the metadata), so this is a plain opaque
 * Block; an opaque standard form cube hides shared faces just like the original did.
 */
public class BlockTFHedge extends Block {

	public int damageDone;

	public static Icon sprHedge;
	public static Icon sprDarkwoodLeaves;

	public BlockTFHedge(int id) {
		super(id, Material.wood, new BlockConstants());
		this.damageDone = 3;
		this.setHardness(2F);
		this.setMinHarvestLevel(1);
		this.setStepSound(Block.soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta <= 2;
	}

	/** Item subtype: 0 hedge, 1 dark hedge (1.7.10 pick block used the raw metadata, 2 was the workaround for 0). */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta == 2 ? 0 : meta;
	}

	/**
	 * Returns a bounding box (world coordinates) for collisions
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == 0) {
			float f = 0.0625F;
			return AxisAlignedBB.getBoundingBoxFromPool(x, y, z, 0, 0, 0, 1, 1 - f, 1);
		}
		return AxisAlignedBB.getBoundingBoxFromPool(x, y, z, 0, 0, 0, 1, 1, 1);
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		Icon icon = meta == 1 ? BlockTFHedge.sprDarkwoodLeaves : BlockTFHedge.sprHedge;
		// never hand the renderer a null icon (the atlas is rebuilt on resource reload; a missing sprite must not hide the wall)
		return icon != null ? icon : this.blockIcon != null ? this.blockIcon : Block.leaves.getIcon(side, 0);
	}

	// A hedge is a full, opaque cube drawn in the solid pass. These are stated explicitly (and not inherited from BlockLeavesBase,
	// which MITE forces to never-opaque, always flags as tree leaves and only keeps "fancy" for Block.leaves) so the maze walls
	// always render and cull their shared faces.
	@Override
	public int getRenderType() {
		return 0;
	}

	@Override
	public int getRenderBlockPass() {
		return 0;
	}

	/** Untinted: the hedge textures carry their own green / brown, a 0 (black) multiplier must never come out of here. */
	@Override
	public int getRenderColor(int meta) {
		return 0xFFFFFF;
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return 0xFFFFFF;
	}

	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return true;
	}

	@Override
	public boolean isSolid(boolean[] is_solid, int metadata) {
		return true;
	}

	private boolean isPrickly(int meta) {
		// meta 2 is a temporary workaround value that behaves like 0
		return meta == 0 || meta == 2;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (!world.isRemote && isPrickly(world.getBlockMetadata(x, y, z)) && shouldDamage(entity)) {
			TFDamage.hurt(entity, DamageSource.cactus, damageDone);
		}
	}

	@Override
	public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
		if (!world.isRemote && isPrickly(world.getBlockMetadata(x, y, z)) && shouldDamage(entity)) {
			TFDamage.hurt(entity, DamageSource.cactus, damageDone);
		}
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityplayer) {
		if (isPrickly(world.getBlockMetadata(x, y, z)) && !world.isRemote) {
			world.scheduleBlockUpdate(x, y, z, this.blockID, 10);
		}
	}

	/**
	 * Called on the server just before a player harvests the block (1.7.10 harvestBlock): the prickly hedge stings the harvester.
	 */
	@Override
	public void onBlockAboutToBeBroken(BlockBreakInfo info) {
		EntityPlayer player = info.getResponsiblePlayer();
		if (player != null && !info.isResponsiblePlayerInCreativeMode() && isPrickly(info.getMetadata())) {
			TFDamage.hurt(player, DamageSource.cactus, damageDone);
		}
	}

	/**
	 * This should be called 10 ticks after we've received a click event from a player.
	 * If we see player nearby swinging at a hedge block, prick them
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (super.updateTick(world, x, y, z, random)) {
			return true;
		}

		double range = 4.0; // do we need to get this with a better method than hardcoding it?

		// find players within harvest range
		List<?> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(range, range, range));

		// are they swinging?
		for (Object o : nearbyPlayers) {
			EntityPlayer player = (EntityPlayer) o;
			if (player.isSwingInProgress) {
				// are they pointing at this block?
				RaycastCollision hit = getPlayerPointVec(world, player, range);

				if (hit != null && hit.isBlock() && hit.getBlockHit() == this) {
					// prick them!  prick them hard!
					TFDamage.hurt(player, DamageSource.cactus, damageDone);

					// trigger this again!
					world.scheduleBlockUpdate(x, y, z, this.blockID, 10);
				}
			}
		}
		return false;
	}

	/**
	 * What block is the player pointing at?
	 *
	 * This very similar to player.rayTrace, but that method is not available on the server.
	 */
	private RaycastCollision getPlayerPointVec(World worldObj, EntityPlayer player, double range) {
		Vec3 position = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 look = player.getLook(1.0F);
		Vec3 dest = position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
		return worldObj.getBlockCollisionForSelection(position, dest, false);
	}

	private boolean shouldDamage(Entity entity) {
		return !(entity instanceof EntitySpider) && !(entity instanceof EntityItem) && !entity.doesEntityNotTriggerPressurePlate();
	}

	// TODO(mite-port): 1.7.10 getFlammability (1 for metadata 1 only) / getFireSpreadSpeed (0): MITE's fire table is per block id,
	// TFBlocks has to call Block.fire.setBurnRate(hedge.blockID, 0, 1) (TF_FORGE_HOOKS §getFlammability).

	/**
	 * Dark hedge (metadata 1) drops a darkwood sapling with a 1 in 40 chance, the prickly hedge drops nothing
	 * (silk touch / shears go through dropBlockAsItself instead).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.getMetadata() == 1 && !info.wasExploded()) {
			// sapling metadata 3 = darkwood sapling
			return this.dropBlockAsEntityItem(info, TFBlocks.sapling.blockID, 3, 1, 1.0F / 40.0F);
		}
		return 0;
	}

	/**
	 * Properly register icon source
	 */
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFHedge.sprHedge = par1IconRegister.registerIcon(TwilightForestMod.ID + ":hedge");
		BlockTFHedge.sprDarkwoodLeaves = par1IconRegister.registerIcon(TwilightForestMod.ID + ":darkwood_leaves");
		this.blockIcon = BlockTFHedge.sprHedge;
	}
}
