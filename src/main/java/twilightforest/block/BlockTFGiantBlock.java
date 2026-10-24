package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.Explosion;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.World;

/**
 * A 4x4x4 cube of blocks that is placed, broken and exploded as one piece.
 */
public abstract class BlockTFGiantBlock extends Block {

	private Icon[][][] giantIcon;
	private Block baseBlock;
	private boolean isSelfDestructing;

	/**
	 * @param constants block constants; note that the block stays "always legal" (the 1.7.10 canBlockStay was only
	 *                  ever consulted from onBlockPreDestroy, and MITE's legality check would otherwise tear the whole
	 *                  cube apart while it is being removed cell by cell).
	 */
	protected BlockTFGiantBlock(int id, Block baseBlock, BlockConstants constants) {
		super(id, baseBlock.blockMaterial, constants);
		this.setStepSound(baseBlock.stepSound);
		this.setCreativeTab(CreativeTabs.tabBlock);

		this.baseBlock = baseBlock;
	}

	protected BlockTFGiantBlock(int id, Block baseBlock) {
		this(id, baseBlock, new BlockConstants());
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.giantIcon = new GiantBlockIcon[4][4][6];

		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int side = 0; side < 6; side++) {
					this.giantIcon[x][y][side] = new GiantBlockIcon(this.baseBlock.getBlockTextureFromSide(side), x, y);
				}
			}
		}
	}

	/**
	 * Returns an icon from the icon matrix, depending on the position inside the giant cube.
	 */
	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
		switch (side) {
		case 0:
		default:
			return this.giantIcon[x & 3][z & 3][side];
		case 1:
			return this.giantIcon[x & 3][z & 3][side];
		case 2:
			return this.giantIcon[3 - (x & 3)][3 - (y & 3)][side];
		case 3:
			return this.giantIcon[x & 3][3 - (y & 3)][side];
		case 4:
			return this.giantIcon[z & 3][3 - (y & 3)][side];
		case 5:
			return this.giantIcon[3 - (z & 3)][3 - (y & 3)][side];
		}
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return this.giantIcon[0][0][side];
	}

	/**
	 * Checks that the whole 4x4x4 area is free (replaces 1.7.10 canPlaceBlockAt).
	 */
	@Override
	public boolean canBePlacedAt(World world, int x, int y, int z, int metadata) {
		int bx = (x >> 2) << 2;
		int by = (y >> 2) << 2;
		int bz = (z >> 2) << 2;

		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 4; dy++) {
				for (int dz = 0; dz < 4; dz++) {
					Block existing = world.getBlock(bx + dx, by + dy, bz + dz);
					if (existing != null && !this.canReplaceBlock(metadata, existing, world.getBlockMetadata(bx + dx, by + dy, bz + dz))) {
						return false;
					}
				}
			}
		}

		return super.canBePlacedAt(world, x, y, z, metadata);
	}

	// The 1.7.10 getCollisionBoundingBoxFromPool returned the whole 4x4x4 box for every cell. Every cell is a full
	// standard-form cube here, so the default per-cell collision box is identical in effect and is not overridden.

	/**
	 * Called when the block is placed in the world: fills the whole 4x4x4 area.
	 */
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, Entity placer, boolean test_only) {
		if (!test_only && !world.isRemote) {
			int bx = (x >> 2) << 2;
			int by = (y >> 2) << 2;
			int bz = (z >> 2) << 2;

			for (int dx = 0; dx < 4; dx++) {
				for (int dy = 0; dy < 4; dy++) {
					for (int dz = 0; dz < 4; dz++) {
						world.setBlock(bx + dx, by + dy, bz + dz, this.blockID, 0, 2);
					}
				}
			}
		}

		return super.onBlockPlacedMITE(world, x, y, z, metadata, placer, test_only);
	}

	// TODO(mite-port): addDestroyEffects (4x4x4 grid of EntityDiggingFX break particles) is client-only and MITE has no
	// per-block hook for it (World.blockFX(EnumBlockFX.destroy)); needs a client mixin/render unit. Vanilla break particles
	// are used instead.

	/**
	 * Called when the block is attempted to be harvested by a player (replaces 1.7.10 onBlockHarvested).
	 */
	@Override
	public void onBlockAboutToBeBroken(BlockBreakInfo info) {
		super.onBlockAboutToBeBroken(info);
		this.setGiantBlockToAir(info.world, info.x, info.y, info.z);
	}

	/**
	 * Called when the block is destroyed by an explosion (replaces 1.7.10 onBlockExploded).
	 * The explosion has already replaced this cell with air.
	 */
	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
		this.setGiantBlockToAir(world, x, y, z);
	}

	/**
	 * Called on server worlds only when the block is about to be replaced by a different block or the same block with a
	 * different metadata value. Args: world, x, y, z, old metadata
	 */
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		super.onBlockPreDestroy(world, x, y, z, meta);
		if (!this.isSelfDestructing && !canBlockStay(world, x, y, z)) {
			this.setGiantBlockToAir(world, x, y, z);
		}
	}

	/**
	 * Set the whole giant block area to air
	 */
	private void setGiantBlockToAir(World world, int x, int y, int z) {
		// this flag is maybe not totally perfect
		this.isSelfDestructing = true;

		int bx = (x >> 2) << 2;
		int by = (y >> 2) << 2;
		int bz = (z >> 2) << 2;

		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 4; dy++) {
				for (int dz = 0; dz < 4; dz++) {
					if (!(x == bx + dx && y == by + dy && z == bz + dz)) {
						if (world.getBlock(bx + dx, by + dy, bz + dz) == this) {
							world.setBlockToAir(bx + dx, by + dy, bz + dz);
						}
					}
				}
			}
		}

		this.isSelfDestructing = false;
	}

	/**
	 * Is the whole 4x4x4 area still made of this block?
	 */
	public boolean canBlockStay(World world, int x, int y, int z) {
		boolean allThisBlock = true;

		int bx = (x >> 2) << 2;
		int by = (y >> 2) << 2;
		int bz = (z >> 2) << 2;

		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 4; dy++) {
				for (int dz = 0; dz < 4; dz++) {
					allThisBlock &= world.getBlock(bx + dx, by + dy, bz + dz) == this;
				}
			}
		}

		return allThisBlock;
	}

	/**
	 * Returns the mobility information of the block, 0 = free, 1 = can't push but can move over, 2 = total immobility
	 * and stop pistons
	 */
	@Override
	public int getMobilityFlag() {
		return 2;
	}
}
