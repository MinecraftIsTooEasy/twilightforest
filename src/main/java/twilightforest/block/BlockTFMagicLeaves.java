package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockHardness;
import net.minecraft.BlockLeavesBase;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFParticles;

/**
 * Leaves of the magic trees. Metadata: bits 1+2 = tree type, bit 4 = placed by player (never decays), bit 8 = decay check pending.
 * Rendered by RenderBlockTFMagicLeaves (render type from the proxy), decay is implemented here because MITE's BlockLeaves is final.
 */
public class BlockTFMagicLeaves extends BlockLeavesBase {

	int oakColor = 0x48B518;
	int canopyColor = 0x609860;
	int mangroveColor = 0x80A755;

	public static final int META_TIME = 0;
	public static final int META_TRANS = 1;
	public static final int META_MINE = 2;
	public static final int META_SORT = 3;

	public static Icon SPR_TIMELEAVES;
	public static Icon SPR_TIMEFX;
	public static Icon SPR_TRANSLEAVES;
	public static Icon SPR_TRANSFX;
	public static Icon SPR_SORTLEAVES;
	public static Icon SPR_SORTFX;

	protected BlockTFMagicLeaves(int id) {
		super(id, Material.tree_leaves, true);
		this.setTickRandomly(true);
		this.setHardness(BlockHardness.leaves);
		this.setLightOpacity(2);
		this.setStepSound(Block.soundGrassFootstep);
		this.setCreativeTab(net.minecraft.CreativeTabs.tabDecorations);
		this.setCushioning(0.8F);
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata & 3;
	}

	@Override
	public String getMetadataNotes() {
		return "Bits 1 and 2 used for subtype, bit 4 set if placed by player, and bit 8 set when a neighboring log is destroyed";
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		switch (meta & 3) {
		case META_TIME:
			return 106 << 16 | 156 << 8 | 23;
		case META_TRANS:
			return 108 << 16 | 204 << 8 | 234;
		case META_MINE:
			return 252 << 16 | 241 << 8 | 68;
		case META_SORT:
			return 54 << 16 | 76 << 8 | 3;
		}

		return 16777215;
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int leafType = world.getBlockMetadata(x, y, z) & 0x03;

		int red = 0;
		int green = 0;
		int blue = 0;

		if (leafType == META_TIME) {
			int fade = x * 16 + y * 16 + z * 16;
			if ((fade & 256) != 0) {
				fade = 255 - (fade & 255);
			}
			fade &= 255;

			float spring = (255 - fade) / 255F;
			float fall = fade / 255F;

			red = (int) (spring * 106 + fall * 251);
			green = (int) (spring * 156 + fall * 108);
			blue = (int) (spring * 23 + fall * 27);
		} else if (leafType == META_MINE) {
			int fade = x * 31 + y * 33 + z * 32;
			if ((fade & 256) != 0) {
				fade = 255 - (fade & 255);
			}
			fade &= 255;

			float spring = (255 - fade) / 255F;
			float fall = fade / 255F;

			red = (int) (spring * 252 + fall * 237);
			green = (int) (spring * 241 + fall * 172);
			blue = (int) (spring * 68 + fall * 9);
		} else if (leafType == META_TRANS) {
			int fade = x * 27 + y * 63 + z * 39;
			if ((fade & 256) != 0) {
				fade = 255 - (fade & 255);
			}
			fade &= 255;

			float spring = (255 - fade) / 255F;
			float fall = fade / 255F;

			red = (int) (spring * 108 + fall * 96);
			green = (int) (spring * 204 + fall * 107);
			blue = (int) (spring * 234 + fall * 121);
		} else if (leafType == META_SORT) {
			int fade = x * 63 + y * 63 + z * 63;
			if ((fade & 256) != 0) {
				fade = 255 - (fade & 255);
			}
			fade &= 255;

			float spring = (255 - fade) / 255F;
			float fall = fade / 255F;

			red = (int) (spring * 54 + fall * 168);
			green = (int) (spring * 76 + fall * 199);
			blue = (int) (spring * 3 + fall * 43);
		}

		return red << 16 | green << 8 | blue;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getMagicLeavesBlockRenderID();
	}

	/**
	 * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
	 */
	@Override
	public int getRenderBlockPass() {
		return 0;
	}

	// isOpaqueCube(): BlockLeavesBase is never opaque in MITE, no override needed

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		switch (meta & 0x03) {
		default:
			return SPR_TIMELEAVES;
		case 1:
			return SPR_TRANSLEAVES;
		case 3:
			return SPR_SORTLEAVES;
		}
	}

	@Override
	public void registerIcons(IconRegister reg) {
		BlockTFMagicLeaves.SPR_TIMELEAVES = reg.registerIcon(TwilightForestMod.ID + ":time_leaves");
		BlockTFMagicLeaves.SPR_TRANSLEAVES = reg.registerIcon(TwilightForestMod.ID + ":trans_leaves");
		BlockTFMagicLeaves.SPR_SORTLEAVES = reg.registerIcon(TwilightForestMod.ID + ":sort_leaves");
	}

	/**
	 * Ticks the block: when flagged for decay (bit 8) and not player placed (bit 4), look for a log within 4 leaves-steps.
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		if ((meta & 8) == 0 || (meta & 4) != 0) {
			return false;
		}

		if (hasSupport(world, x, y, z)) {
			world.setBlockMetadataWithNotify(x, y, z, meta & ~8, 4);
			return true;
		}

		this.dropBlockAsEntityItem(new BlockBreakInfo(world, x, y, z).setDroppedSelf());
		world.setBlockToAir(x, y, z);
		return true;
	}

	/**
	 * Breadth first search through connected leaves (any kind) for a block that can sustain leaves, at most 4 steps away.
	 */
	private static boolean hasSupport(World world, int x, int y, int z) {
		final int maxDist = 4;
		if (!world.checkChunksExist(x - maxDist - 1, y - maxDist - 1, z - maxDist - 1, x + maxDist + 1, y + maxDist + 1, z + maxDist + 1)) {
			return true; // do not decay leaves next to unloaded chunks
		}

		final int size = maxDist * 2 + 1;
		boolean[] visited = new boolean[size * size * size];
		int[] queue = new int[size * size * size * 4];
		int head = 0;
		int tail = 0;

		visited[(maxDist * size + maxDist) * size + maxDist] = true;
		queue[tail++] = 0;
		queue[tail++] = 0;
		queue[tail++] = 0;
		queue[tail++] = 0;

		final int[][] dirs = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };

		while (head < tail) {
			int cx = queue[head++];
			int cy = queue[head++];
			int cz = queue[head++];
			int dist = queue[head++];

			for (int[] d : dirs) {
				int nx = cx + d[0];
				int ny = cy + d[1];
				int nz = cz + d[2];
				int wx = x + nx;
				int wy = y + ny;
				int wz = z + nz;

				if (TFBlockUtil.canSustainLeaves(world, wx, wy, wz)) {
					return true; // dist + 1 <= maxDist by construction
				}

				if (dist + 1 < maxDist && TFBlockUtil.isLeaves(world, wx, wy, wz)) {
					int idx = ((ny + maxDist) * size + (nx + maxDist)) * size + (nz + maxDist);
					if (!visited[idx]) {
						visited[idx] = true;
						queue[tail++] = nx;
						queue[tail++] = ny;
						queue[tail++] = nz;
						queue[tail++] = dist + 1;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Magic leaves drop saplings and sticks like normal MITE leaves, never when placed by a player.
	 * Rates follow MITE's BlockLeaves (1.7.10: vanilla 1/20 sapling chance).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if ((info.getMetadata() & 4) != 0) {
			return 0;
		}
		int dropped = this.dropBlockAsEntityItem(info, Block.sapling.blockID, info.getMetadata() & 3, 1, 0.01F);
		if (dropped > 0) {
			return dropped;
		}
		return this.dropBlockAsEntityItem(info, Item.stick.itemID, 0, 1, 0.05F);
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);

		if ((meta & 3) == META_TRANS) {
			this.sparkleRunes(world, x, y, z, rand);
		}
	}

	/**
	 * The leaf sparkles.
	 */
	private void sparkleRunes(World world, int x, int y, int z, Random rand) {
		double offset = 0.0625D;

		int side = rand.nextInt(5) + 1;
		double rx = x + rand.nextFloat();
		double ry = y + rand.nextFloat();
		double rz = z + rand.nextFloat();

		if (side == 0 && world.isAirBlock(x, y + 1, z)) {
			ry = y + 1 + offset;
		}

		if (side == 1 && world.isAirBlock(x, y - 1, z)) {
			ry = y + 0 - offset;
		}

		if (side == 2 && world.isAirBlock(x, y, z + 1)) {
			rz = z + 1 + offset;
		}

		if (side == 3 && world.isAirBlock(x, y, z - 1)) {
			rz = z + 0 - offset;
		}

		if (side == 4 && world.isAirBlock(x + 1, y, z)) {
			rx = x + 1 + offset;
		}

		if (side == 5 && world.isAirBlock(x - 1, y, z)) {
			rx = x + 0 - offset;
		}

		if (rx < x || rx > x + 1 || ry < y || ry > y + 1 || rz < z || rz > z + 1) {
			TFParticles.spawn(world, "leafrune", rx, ry, rz, 0.0D, 0.0D, 0.0D);
		}
	}
}
