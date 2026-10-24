package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockFluid;
import net.minecraft.BlockPlant;
import net.minecraft.Entity;
import net.minecraft.EntityBoat;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/**
 * Giant 2x2 lily pad. Each of the four blocks holds a quarter of the pad; metadata = (orientation << 2) | piece.
 * Rendered by a custom render type (TwilightForestMod.proxy.getHugeLilyPadBlockRenderID()).
 */
public class BlockTFHugeLilyPad extends BlockPlant {

	private Icon pad1;
	private Icon pad2;
	private Icon pad3;
	private Icon blank;
	private boolean isSelfDestructing = false;

	public BlockTFHugeLilyPad(int id) {
		super(id, Material.plants);

		float f = 0.5F;
		float f1 = 0.015625F;
		this.setBlockBoundsForAllThreads(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
		this.setStepSound(soundGrassFootstep);
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getHugeLilyPadBlockRenderID();
	}

	/**
	 * No light requirement (BlockPlant would demand light 8..15).
	 */
	@Override
	public int getMinAllowedLightValue() {
		return 0;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		// sides blank
		if (side > 1) {
			return this.blank;
		}

		int orient = meta >> 2;
		int piece = meta & 3;

		// why can't this just be simple?
		if (orient == 1) {
			orient = 3;
		} else if (orient == 3) {
			orient = 1;
		}

		int display = (piece + orient) % 4;

		switch (display) {
		case 0:
		default:
			return this.blockIcon;
		case 1:
			return this.pad1;
		case 2:
			return this.pad2;
		case 3:
			return this.pad3;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":huge_lilypad_0");
		this.pad1 = par1IconRegister.registerIcon(TwilightForestMod.ID + ":huge_lilypad_1");
		this.pad2 = par1IconRegister.registerIcon(TwilightForestMod.ID + ":huge_lilypad_2");
		this.pad3 = par1IconRegister.registerIcon(TwilightForestMod.ID + ":huge_lilypad_3");
		this.blank = par1IconRegister.registerIcon(TwilightForestMod.ID + ":blank");
	}

	/** metadata = (orientation << 2) | piece, both 2 bits. */
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	/**
	 * Single block may be placed on a full water block (the item places all four pieces).
	 */
	@Override
	public boolean isLegalOn(int metadata, Block blockBelow, int blockBelowMetadata) {
		return BlockFluid.isFullWaterBlock(blockBelow, blockBelowMetadata, true);
	}

	@Override
	public boolean canBePlacedOnBlock(int metadata, Block blockBelow, int blockBelowMetadata, double blockBelowBoundsMaxY) {
		return this.isLegalOn(metadata, blockBelow, blockBelowMetadata);
	}

	/**
	 * Placement of a single piece: replaceable target and water below (the 2x2 completeness is only enforced for
	 * already placed pads, see isLegalAt).
	 */
	@Override
	public boolean canBePlacedAt(World world, int x, int y, int z, int metadata) {
		Block existing = world.getBlock(x, y, z);
		if (existing != null && !this.canReplaceBlock(metadata, existing, world.getBlockMetadata(x, y, z))) {
			return false;
		}

		return this.isLegalOn(metadata, world.getBlock(x, y - 1, z), world.getBlockMetadata(x, y - 1, z));
	}

	@Override
	public boolean canReplaceBlock(int metadata, Block existingBlock, int existingBlockMetadata) {
		return false;
	}

	/**
	 * Called when the player harvests or an explosion destroys one piece: the whole giant pad goes.
	 */
	@Override
	public void onBlockAboutToBeBroken(BlockBreakInfo info) {
		super.onBlockAboutToBeBroken(info);
		this.setGiantBlockToAir(info.world, info.x, info.y, info.z);
	}

	/**
	 * Called on server worlds only when the block is about to be replaced by a different block or the same block with a
	 * different metadata value. Args: world, x, y, z, old metadata
	 */
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if (!this.isSelfDestructing && !this.canBlockStay(world, x, y, z)) {
			this.setGiantBlockToAir(world, x, y, z);
		}
	}

	/**
	 * Set the whole giant block area to air
	 */
	private void setGiantBlockToAir(World world, int x, int y, int z) {
		// this flag is maybe not totally perfect
		this.isSelfDestructing = true;

		int bx = (x >> 1) << 1;
		int bz = (z >> 1) << 1;

		// this is the best loop over 3 items that I've ever programmed!
		for (int dx = 0; dx < 2; dx++) {
			for (int dz = 0; dz < 2; dz++) {
				if (!(x == bx + dx && z == bz + dz)) {
					if (world.getBlock(bx + dx, y, bz + dz) == this) {
						world.setBlock(bx + dx, y, bz + dz, 0, 0, 2);
					}
				}
			}
		}

		this.isSelfDestructing = false;
	}

	/**
	 * Can this block stay at this position: all four pieces present and all four blocks below are source water.
	 */
	public boolean canBlockStay(World world, int x, int y, int z) {
		boolean allThisBlock = true;
		boolean allWater = true;

		int bx = (x >> 1) << 1;
		int bz = (z >> 1) << 1;

		for (int dx = 0; dx < 2; dx++) {
			for (int dz = 0; dz < 2; dz++) {
				allThisBlock &= world.getBlock(bx + dx, y, bz + dz) == this;
				allWater &= BlockFluid.isFullWaterBlock(world.getBlock(bx + dx, y - 1, bz + dz), world.getBlockMetadata(bx + dx, y - 1, bz + dz), true);
			}
		}

		return allThisBlock && allWater;
	}

	@Override
	public boolean isLegalAt(World world, int x, int y, int z, int metadata) {
		return this.canBlockStay(world, x, y, z);
	}

	/**
	 * Not legal: remove silently (1.7.10 checkAndDropBlock had the drop commented out); onBlockPreDestroy removes the rest.
	 */
	@Override
	public boolean onNotLegal(World world, int x, int y, int z, int metadata) {
		world.setBlock(x, y, z, 0, 0, 2);
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

	// like vanilla lily pad (BlockLilyPad)
	@Override
	public boolean canCollideWithEntity(Entity entity) {
		return !(entity instanceof EntityBoat);
	}

	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		return this.canCollideWithEntity(entity) ? super.getCollisionBounds(world, x, y, z, entity) : null;
	}

	@Override
	public boolean isSolid(boolean[] isSolid, int metadata) {
		return true;
	}

	@Override
	public boolean blocksPrecipitation(boolean[] blocksPrecipitation, int metadata) {
		return true;
	}

	@Override
	public boolean blocksFluids(boolean[] blocksFluids, int metadata) {
		return true;
	}

	@Override
	public int getBlockColor() {
		return 2129968;
	}

	@Override
	public int getRenderColor(int par1) {
		return 2129968;
	}

	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		return 2129968;
	}
}
