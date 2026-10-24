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
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.compat.ITFReplaceableByLeaves;

/** Thorn leaves (0) and beanstalk leaves (1). Same metadata bits as BlockTFLeaves. */
public class BlockTFLeaves3 extends BlockLeavesBase implements ITFReplaceableByLeaves {

	public static final String[] names = new String[] {"thorn", "beanstalk"};

	public BlockTFLeaves3(int id) {
		super(id, Material.tree_leaves, true);
		this.setTickRandomly(true);
		this.setHardness(0.2F);
		this.setLightOpacity(1);
		this.setStepSound(Block.soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setCushioning(0.8F);
	}

	public String[] func_150125_e() {
		return names;
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		return (meta & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : ((meta & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : ColorizerFoliage.getFoliageColorBasic());
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return (meta & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : ((meta & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : BlockTFLeaves.averageFoliageColor(world, x, z));
	}

	@Override
	public Icon getIcon(int i, int j) {
		return Block.leaves.getIcon(i, 0);
	}

	/** The icons come from vanilla leaves. */
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
	}

	@Override
	public boolean hidesAdjacentSide(IBlockAccess block_access, int x, int y, int z, Block neighbor, int side) {
		return !this.graphicsLevel;
	}

	/** Drops nothing (the original quantityDropped is 0); a silk harvest still gives the block itself. */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return 0;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		BlockTFLeaves.flagNeighbourLeaves(world, x, y, z);
	}

	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (super.updateTick(world, x, y, z, random)) {
			return true;
		}
		return BlockTFLeaves.checkLeafDecay(this, world, x, y, z);
	}

	@Override
	public String getMetadataNotes() {
		return "Bits 1 and 2 used for subtype (0 thorn, 1 beanstalk), bit 4 set if placed by player, bit 8 set when decay should be checked";
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16 && (metadata & 3) < names.length;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata & 3;
	}
}
