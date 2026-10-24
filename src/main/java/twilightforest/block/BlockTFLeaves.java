package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockLeavesBase;
import net.minecraft.ColorizerFoliage;
import net.minecraft.CreativeTabs;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.compat.TFBlockUtil;

/**
 * Twilight oak / canopy / mangrove / rainbow oak leaves. Metadata: bits 1-2 = type, bit 4 = placed by player (never
 * decays, drops nothing), bit 8 = check decay. BlockLeaves is final in MITE, so this extends BlockLeavesBase and
 * implements the decay itself ({@link #checkLeafDecay}).
 */
public class BlockTFLeaves extends BlockLeavesBase {

	int oakColor = 0x48B518;
	int canopyColor = 0x609860;
	int mangroveColor = 0x80A755;

	public static final String[] unlocalizedNameArray = new String[] {"twilightoak", "canopy", "mangrove", "rainboak"};

	public BlockTFLeaves(int id) {
		super(id, Material.tree_leaves, true);
		this.setTickRandomly(true);
		this.setHardness(0.2F);
		this.setLightOpacity(2);
		this.setStepSound(Block.soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setCushioning(0.8F);
	}

	@Override
	public int getBlockColor() {
		double var1 = 0.5D;
		double var3 = 1.0D;
		return ColorizerFoliage.getFoliageColor(var1, var3);
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int par1) {
		return (par1 & 3) == 1 ? canopyColor : ((par1 & 3) == 2 ? mangroveColor : oakColor);
	}

	/** Average foliage colour of the 3x3 biome area around x,z (MITE's getBiomeFoliageColor has no coordinates). */
	public static int averageFoliageColor(IBlockAccess world, int x, int z) {
		int red = 0;
		int green = 0;
		int blue = 0;

		for (int dz = -1; dz <= 1; ++dz) {
			for (int dx = -1; dx <= 1; ++dx) {
				int c = world.getBiomeGenForCoords(x + dx, z + dz).getBiomeFoliageColor();
				red += (c & 16711680) >> 16;
				green += (c & 65280) >> 8;
				blue += c & 255;
			}
		}

		return (red / 9 & 0xFF) << 16 | (green / 9 & 0xFF) << 8 | blue / 9 & 0xFF;
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z) {
		int meta = par1IBlockAccess.getBlockMetadata(x, y, z);
		int normalColor = averageFoliageColor(par1IBlockAccess, x, z);

		if ((meta & 3) == 1) {
			// canopy colorizer
			return ((normalColor & 0xFEFEFE) + 0x469A66) / 2;
		} else if ((meta & 3) == 2) {
			// mangrove colors
			return ((normalColor & 0xFEFEFE) + 0xC0E694) / 2;
		} else if ((meta & 3) == 3) {
			// RAINBOW!
			int red = x * 32 + y * 16;
			if ((red & 256) != 0) {
				red = 255 - (red & 255);
			}
			red &= 255;

			int blue = y * 32 + z * 16;
			if ((blue & 256) != 0) {
				blue = 255 - (blue & 255);
			}
			blue ^= 255;

			int green = x * 16 + z * 32;
			if ((green & 256) != 0) {
				green = 255 - (green & 255);
			}
			green &= 255;

			return red << 16 | blue << 8 | green;
		} else {
			return normalColor;
		}
	}

	@Override
	public Icon getIcon(int i, int j) {
		return Block.leaves.getIcon(i, (j & 3) == 3 ? 0 : j & 3);
	}

	/** The icons come from vanilla leaves. */
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
	}

	@Override
	public boolean hidesAdjacentSide(IBlockAccess block_access, int x, int y, int z, Block neighbor, int side) {
		return !this.graphicsLevel;
	}

	/**
	 * Drops like MITE leaves (nothing if placed by a player): the matching TF sapling 1% (mangrove 2%), else a stick 5%.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		int meta = info.getMetadata();
		if ((meta & 4) != 0) {
			return 0;
		}

		int num;
		float chance = (meta & 3) == 2 ? 0.02F : 0.01F;
		if (TFBlocks.sapling != null && (num = this.dropBlockAsEntityItem(info, TFBlocks.sapling.blockID, this.getSaplingMeta(meta), 1, chance)) > 0) {
			return num;
		}
		return this.dropBlockAsEntityItem(info, Item.stick.itemID, 0, 1, 0.05F);
	}

	public int getSaplingMeta(int leafMeta) {
		int leafType = leafMeta & 3;

		return leafType == 3 ? 9 : leafType;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		flagNeighbourLeaves(world, x, y, z);
	}

	/** Breaking a leaf block makes the leaves around it check for decay. */
	public static void flagNeighbourLeaves(World world, int x, int y, int z) {
		if (!world.decorating) {
			byte range = 1;
			int check = range + 1;
			if (world.checkChunksExist(x - check, y - check, z - check, x + check, y + check, z + check)) {
				for (int dx = -range; dx <= range; ++dx) {
					for (int dy = -range; dy <= range; ++dy) {
						for (int dz = -range; dz <= range; ++dz) {
							TFBlockUtil.beginLeavesDecay(world, x + dx, y + dy, z + dz);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (super.updateTick(world, x, y, z, random)) {
			return true;
		}
		return checkLeafDecay(this, world, x, y, z);
	}

	/**
	 * Vanilla leaf decay: a flagged (bit 8), non-placed (bit 4 clear) leaf survives only when a log / leaf supporter is
	 * within 4 blocks through connected leaves. Shared by every TF leaf block that decays.
	 */
	public static boolean checkLeafDecay(Block self, World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if ((meta & 8) == 0 || (meta & 4) != 0) {
			return false;
		}

		final int range = 4;
		final int size = range * 2 + 1;
		int[] dist = new int[size * size * size];
		int check = range + 1;
		if (world.checkChunksExist(x - check, y - check, z - check, x + check, y + check, z + check)) {
			for (int dx = -range; dx <= range; ++dx) {
				for (int dy = -range; dy <= range; ++dy) {
					for (int dz = -range; dz <= range; ++dz) {
						Block b = world.getBlock(x + dx, y + dy, z + dz);
						int idx = ((dx + range) * size + dy + range) * size + dz + range;
						if (b != null && TFBlockUtil.canSustainLeaves(world, x + dx, y + dy, z + dz)) {
							dist[idx] = 0;
						} else if (b != null && TFBlockUtil.isLeaves(b)) {
							dist[idx] = -2;
						} else {
							dist[idx] = -1;
						}
					}
				}
			}

			for (int step = 1; step <= range; ++step) {
				for (int dx = -range; dx <= range; ++dx) {
					for (int dy = -range; dy <= range; ++dy) {
						for (int dz = -range; dz <= range; ++dz) {
							int idx = ((dx + range) * size + dy + range) * size + dz + range;
							if (dist[idx] == step - 1) {
								spread(dist, size, range, dx - 1, dy, dz, step);
								spread(dist, size, range, dx + 1, dy, dz, step);
								spread(dist, size, range, dx, dy - 1, dz, step);
								spread(dist, size, range, dx, dy + 1, dz, step);
								spread(dist, size, range, dx, dy, dz - 1, step);
								spread(dist, size, range, dx, dy, dz + 1, step);
							}
						}
					}
				}
			}
		}

		int centre = dist[((range) * size + range) * size + range];
		if (centre >= 0) {
			world.setBlockMetadataWithNotify(x, y, z, meta & -9, 4);
		} else {
			self.dropBlockAsEntityItem(new BlockBreakInfo(world, x, y, z).setDroppedSelf());
			world.setBlockToAir(x, y, z);
		}
		return true;
	}

	private static void spread(int[] dist, int size, int range, int dx, int dy, int dz, int step) {
		if (dx < -range || dx > range || dy < -range || dy > range || dz < -range || dz > range) {
			return;
		}
		int idx = ((dx + range) * size + dy + range) * size + dz + range;
		if (dist[idx] == -2) {
			dist[idx] = step;
		}
	}

	@Override
	public String getMetadataNotes() {
		return "Bits 1 and 2 used for subtype, bit 4 set if placed by player, bit 8 set when decay should be checked";
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata & 3;
	}

	public String[] func_150125_e() {
		return unlocalizedNameArray;
	}
}
