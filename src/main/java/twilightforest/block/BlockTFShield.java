package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.BlockPistonBase;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EnumFace;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class BlockTFShield extends Block {

	public static Icon sprSide;
	private Icon sprInside;
	private Icon sprOutside;

	/**
	 * MITE_BALANCE B8: stronghold shield is unbreakable (hardness -1, level 100, blast proof).
	 * Metadata 0-5 is the facing of the "inside" side.
	 */
	public BlockTFShield(int id) {
		super(id, Material.stone, new BlockConstants());
		this.setBlockUnbreakable();
		this.setStepSound(Block.soundMetalFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta < 16;
	}

	/** Only one item subtype (1.7.10 getSubBlocks / damageDropped == 0). */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (side == meta) {
			return this.sprInside;
		} else {
			return this.sprOutside;
		}
	}

	/**
	 * Properly register icon source
	 */
	@Override
	public void registerIcons(IconRegister register) {
		this.sprInside = register.registerIcon(TwilightForestMod.ID + ":shield_inside");
		this.sprOutside = register.registerIcon(TwilightForestMod.ID + ":shield_outside");
	}

	/**
	 * Drops nothing (1.7.10 quantityDropped == 0).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return 0;
	}

	@Override
	public boolean canSilkHarvest(int metadata) {
		return false;
	}

	/**
	 * Called when the block is placed: the inside faces the placer (1.7.10 onBlockPlacedBy).
	 */
	@Override
	public int getMetadataForPlacement(World world, int x, int y, int z, ItemStack itemStack, Entity entity, EnumFace face, float offsetX, float offsetY, float offsetZ) {
		if (entity instanceof EntityLivingBase) {
			return BlockPistonBase.determineOrientation(world, x, y, z, (EntityLivingBase) entity);
		}
		return 0;
	}

	// TODO(mite-port): 1.7.10 getPlayerRelativeBlockHardness let the player mine the shield from its inside face
	// (breakSpeed(stone) / 1.5 / 100). MITE has no per-face hardness hook and MITE_BALANCE B8 makes the shield
	// unbreakable, so the shield is always unbreakable here.

}
