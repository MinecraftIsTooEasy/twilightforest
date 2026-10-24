package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockBreakable;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFWispyCloud extends BlockBreakable {

	public BlockTFWispyCloud(int id) {
		super(id, TwilightForestMod.ID + ":wispy_cloud", Material.craftedSnow, false, new BlockConstants().setUsesAlphaBlending());
		this.setStepSound(soundClothFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);

		this.setHardness(0.3F);
	}

	/**
	 * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
	 */
	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	/**
	 * Drops nothing when broken normally; a silk harvest gives the block itself (Block.dropBlockAsEntityItem(Block, info)
	 * routes silk harvests to dropBlockAsItself because canSilkHarvest defaults to canBeCarried()).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return 0;
	}
}
