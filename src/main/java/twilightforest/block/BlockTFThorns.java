package twilightforest.block;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.ForgeDirection;
import twilightforest.compat.ITFBlockHooks;
import twilightforest.compat.ITFLeafSupport;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFDamage;

/**
 * Thorns. Metadata: bits 0-1 subtype (0 brown, 1 green), bits 2-3 axis (0 up/down, 4 east/west, 8 north/south).
 * Balance (MITE_BALANCE B2): wood, hardness 6.0, needs an iron axe (harvest level 3), contact damage 6.
 */
public class BlockTFThorns extends BlockRotatedPillar implements ITFLeafSupport, ITFBlockHooks {

	private static final float THORN_DAMAGE = 6.0F;

	private String[] names;
	private Icon[] sideIcons;
	private Icon[] topIcons;

	public BlockTFThorns(int id) {
		super(id, Material.wood);

		this.setNames(new String[] {"brown", "green"});

		this.setHardness(6.0F);
		this.setMinHarvestLevel(3);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	// ---- subtypes / shape (pure functions of metadata, called from Block.<init>) ----

	/** Bits 0-1 are the subtype, bits 2-3 the axis (12 is unused). */
	@Override
	public boolean isValidMetadata(int meta) {
		return (meta & 12) != 12 && (meta & 3) <= 1;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta & 3;
	}

	@Override
	public boolean isStandardFormCube(boolean[] isStandardFormCube, int metadata) {
		return false;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getThornsBlockRenderID();
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		float pixel = 0.0625F;

		switch (world.getBlockMetadata(x, y, z) & 12) {
		case 0:
		default:
			this.setBlockBoundsForCurrentThread(pixel * 3F, 0, pixel * 3F, 1F - pixel * 3F, 1F, 1F - pixel * 3F);
			break;
		case 4:
			this.setBlockBoundsForCurrentThread(0, pixel * 3F, pixel * 3F, 1F, 1F - pixel * 3F, 1F - pixel * 3F);
			break;
		case 8:
			this.setBlockBoundsForCurrentThread(pixel * 3F, pixel * 3F, 0, 1F - pixel * 3F, 1F - pixel * 3F, 1F);
			break;
		}
	}

	/**
	 * Returns a bounding box (world coordinates) for collisions
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		int rotation = world.getBlockMetadata(x, y, z) & 12;
		float pixel = 0.0625F;

		switch (rotation) {
		case 0:
		default:
			return AxisAlignedBB.getBoundingBoxFromPool(x + pixel * 3F, y, z + pixel * 3F, x + 1F - pixel * 3F, y + 1F, z + 1F - pixel * 3F);
		case 4:
			return AxisAlignedBB.getBoundingBoxFromPool(x, y + pixel * 3F, z + pixel * 3F, x + 1F, y + 1F - pixel * 3F, z + 1F - pixel * 3F);
		case 8:
			return AxisAlignedBB.getBoundingBoxFromPool(x + pixel * 3F, y + pixel * 3F, z, x + 1F - pixel * 3F, y + 1F - pixel * 3F, z + 1F);
		}
	}

	/**
	 * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
	 */
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (!world.isRemote) {
			TFDamage.hurt(entity, DamageSource.cactus, THORN_DAMAGE);
		}
	}

	/**
	 * When we are chopped, things get bad (Forge removedByPlayer, called for survival players only).
	 * Returns true if the block took care of its own removal.
	 */
	@Override
	public boolean tf$removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!player.capabilities.isCreativeMode) {
			if (!world.isRemote) {
				// grow back
				world.setBlock(x, y, z, this.blockID, (meta & 12) | 1, 2);
				// grow more
				this.doThornBurst(world, x, y, z, meta);
			}
		} else {
			world.setBlockToAir(x, y, z);
		}

		return true;
	}

	/**
	 * Returns the mobility information of the block, 0 = free, 1 = can't push but can move over, 2 = total immobility
	 * and stop pistons
	 */
	@Override
	public int getMobilityFlag() {
		return 2;
	}

	/**
	 * Grow thorns out of both the ends, then maybe in another direction too
	 */
	private void doThornBurst(World world, int x, int y, int z, int meta) {
		int rotation = meta & 12;

		switch (rotation) {
		case 0:
			growThorns(world, x, y, z, ForgeDirection.UP);
			growThorns(world, x, y, z, ForgeDirection.DOWN);
			break;
		case 4:
			growThorns(world, x, y, z, ForgeDirection.EAST);
			growThorns(world, x, y, z, ForgeDirection.WEST);
			break;
		case 8:
			growThorns(world, x, y, z, ForgeDirection.NORTH);
			growThorns(world, x, y, z, ForgeDirection.SOUTH);
			break;
		}

		// also try three random directions
		growThorns(world, x, y, z, ForgeDirection.VALID_DIRECTIONS[world.rand.nextInt(ForgeDirection.VALID_DIRECTIONS.length)]);
		growThorns(world, x, y, z, ForgeDirection.VALID_DIRECTIONS[world.rand.nextInt(ForgeDirection.VALID_DIRECTIONS.length)]);
		growThorns(world, x, y, z, ForgeDirection.VALID_DIRECTIONS[world.rand.nextInt(ForgeDirection.VALID_DIRECTIONS.length)]);
	}

	/**
	 * grow several green thorns in the specified direction
	 */
	private void growThorns(World world, int x, int y, int z, ForgeDirection dir) {
		int length = 1 + world.rand.nextInt(3);

		for (int i = 1; i < length; i++) {
			int dx = x + (dir.offsetX * i);
			int dy = y + (dir.offsetY * i);
			int dz = z + (dir.offsetZ * i);

			if (world.isAirBlock(dx, dy, dz)) {
				world.setBlock(dx, dy, dz, this.blockID, getMetaFor(dir) | 1, 2);
			} else {
				break;
			}
		}
	}

	/**
	 * Get the meta we need to place a new block in the specified direction
	 */
	public static int getMetaFor(ForgeDirection dir) {
		switch (dir) {
		case UNKNOWN:
		default:
		case UP:
		case DOWN:
			return 0;
		case EAST:
		case WEST:
			return 4;
		case NORTH:
		case SOUTH:
			return 8;
		}
	}

	/**
	 * Like logs, we start leaf decay when broken
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int metadata) {
		byte range = 4;
		int exRange = range + 1;

		if (world.checkChunksExist(x - exRange, y - exRange, z - exRange, x + exRange, y + exRange, z + exRange)) {
			for (int dx = -range; dx <= range; ++dx) {
				for (int dy = -range; dy <= range; ++dy) {
					for (int dz = -range; dz <= range; ++dz) {
						if (TFBlockUtil.isLeaves(world, x + dx, y + dy, z + dz)) {
							TFBlockUtil.beginLeavesDecay(world, x + dx, y + dy, z + dz);
						}
					}
				}
			}
		}
	}

	/**
	 * Thorns never drop anything (1.7.10 quantityDropped == 0).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return 0;
	}

	/** 1.7.10: renderAsNormalBlock() == false, so Forge refused silk harvesting. */
	@Override
	public boolean canSilkHarvest(int metadata) {
		return false;
	}

	@Override
	protected Icon getSideIcon(int subtype) {
		return this.sideIcons[subtype & 3];
	}

	@Override
	protected Icon getEndIcon(int subtype) {
		return this.topIcons[subtype & 3];
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.sideIcons = new Icon[4];
		this.topIcons = new Icon[4];

		for (int i = 0; i < 4; i++) {
			// subtypes beyond the named ones (never valid) reuse the last texture so that lookups cannot fail
			String name = getNames()[Math.min(i, getNames().length - 1)];
			this.sideIcons[i] = iconRegister.registerIcon(TwilightForestMod.ID + ":" + name + "_thorns_side");
			this.topIcons[i] = iconRegister.registerIcon(TwilightForestMod.ID + ":" + name + "_thorns_top");
		}
	}

	@Override
	public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}
}
