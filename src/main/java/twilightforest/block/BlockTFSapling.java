package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockSapling;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.World;
import net.minecraft.WorldGenerator;
import twilightforest.TwilightForestMod;
import twilightforest.compat.EnumPlantType;
import twilightforest.compat.TFBlockUtil;
import twilightforest.world.TFGenCanopyTree;
import twilightforest.world.TFGenDarkCanopyTree;
import twilightforest.world.TFGenHollowTree;
import twilightforest.world.TFGenLargeRainboak;
import twilightforest.world.TFGenMangroveTree;
import twilightforest.world.TFGenMinersTree;
import twilightforest.world.TFGenSmallRainboak;
import twilightforest.world.TFGenSmallTwilightOak;
import twilightforest.world.TFGenSortingTree;
import twilightforest.world.TFGenTreeOfTime;
import twilightforest.world.TFGenTreeOfTransformation;

/**
 * Twilight saplings, metadata 0-9: oak, canopy, mangrove, darkwood, hollow oak, time, transformation, mining, sorting,
 * rainbow oak. MITE's two-stage growth (metadata bit 8) is not used because TF's metadata 8 and 9 need that bit; the
 * growth chance is instead folded into one roll.
 */
public class BlockTFSapling extends BlockSapling {

	private Icon[] icons;
	private String[] iconNames = new String[] {"sapling_oak", "sapling_canopy", "sapling_mangrove", "sapling_darkwood", "sapling_hollow_oak", "sapling_time", "sapling_transformation", "sapling_mining", "sapling_sorting", "sapling_rainboak"};

	public BlockTFSapling(int id) {
		super(id);
		float var3 = 0.4F;
		this.setBlockBoundsForAllThreads(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 2.0F, 0.5F + var3);
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	/** Plains-type soil: grass, dirt, farmland and any TF soil that sustains Plains (replaces Forge canSustainPlant). */
	@Override
	public boolean isLegalOn(int metadata, Block block_below, int block_below_metadata) {
		return TFBlockUtil.canSustainPlant(block_below, block_below_metadata, EnumPlantType.Plains);
	}

	/**
	 * BlockSapling.updateTick already does the legality check and rolls light >= 9 and 1/28 before calling this. TF grew
	 * after one roll of 1/7 at light 9; MITE's two stages of 1/28 average 56 ticks, so the extra 1/2 here (28 * 2 = 56)
	 * keeps that pace.
	 */
	@Override
	public boolean markOrGrowMarked(World world, int x, int y, int z, Random rand) {
		if (world.isRemote || world.getBlock(x, y, z) != this) {
			return false;
		}
		if (rand.nextInt(2) != 0) {
			return false;
		}
		this.func_149878_d(world, x, y, z, rand);
		return world.getBlock(x, y, z) != this;
	}

	/**
	 * Attempts to grow a sapling into a tree (was func_149878_d / BlockSapling.growTree). Also usable by other code that
	 * grows the sapling directly (e.g. the tower wing planter).
	 */
	public void func_149878_d(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);
		WorldGenerator treeGenerator = null;
		int var8 = 0;
		int var9 = 0;
		boolean largeTree = false;

		if (meta == 1) {
			treeGenerator = new TFGenCanopyTree(true);
		} else if (meta == 2) {
			treeGenerator = new TFGenMangroveTree(true);
		} else if (meta == 3) {
			treeGenerator = new TFGenDarkCanopyTree(true);
		} else if (meta == 4) {
			treeGenerator = new TFGenHollowTree(true);
		} else if (meta == 5) {
			treeGenerator = new TFGenTreeOfTime(true);
		} else if (meta == 6) {
			treeGenerator = new TFGenTreeOfTransformation(true);
		} else if (meta == 7) {
			treeGenerator = new TFGenMinersTree(true);
		} else if (meta == 8) {
			treeGenerator = new TFGenSortingTree(true);
		} else if (meta == 9) {
			treeGenerator = rand.nextInt(7) == 0 ? new TFGenLargeRainboak(true) : new TFGenSmallRainboak(true);
		} else {
			treeGenerator = new TFGenSmallTwilightOak(true);
		}

		if (largeTree) {
			world.setBlock(x + var8, y, z + var9, 0, 0, 4);
			world.setBlock(x + var8 + 1, y, z + var9, 0, 0, 4);
			world.setBlock(x + var8, y, z + var9 + 1, 0, 0, 4);
			world.setBlock(x + var8 + 1, y, z + var9 + 1, 0, 0, 4);
		} else {
			world.setBlock(x, y, z, 0, 0, 4);
		}

		if (!treeGenerator.generate(world, rand, x + var8, y, z + var9)) {
			if (largeTree) {
				world.setBlock(x + var8, y, z + var9, this.blockID, meta, 4);
				world.setBlock(x + var8 + 1, y, z + var9, this.blockID, meta, 4);
				world.setBlock(x + var8, y, z + var9 + 1, this.blockID, meta, 4);
				world.setBlock(x + var8 + 1, y, z + var9 + 1, this.blockID, meta, 4);
			} else {
				world.setBlock(x, y, z, this.blockID, meta, 4);
			}
		}
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int metadata) {
		if (metadata >= 0 && metadata < this.icons.length) {
			return this.icons[metadata];
		} else {
			return null;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.icons = new Icon[iconNames.length];

		for (int i = 0; i < this.icons.length; ++i) {
			this.icons[i] = par1IconRegister.registerIcon(TwilightForestMod.ID + ":" + iconNames[i]);
		}
		this.blockIcon = this.icons[0];
	}

	@Override
	public String getMetadataNotes() {
		return "Metadata 0-9 = tree type (oak, canopy, mangrove, darkwood, hollow oak, time, transformation, mining, sorting, rainbow oak)";
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 10;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}
}
