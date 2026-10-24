package twilightforest.block;

import net.minecraft.Block;

public class BlockTFGiantObsidian extends BlockTFGiantBlock {

	public BlockTFGiantObsidian(int id) {
		super(id, Block.obsidian);
		// balance (MITE_BALANCE B7): hardness 24, harvest level 3 (iron pickaxe), resistance 1000
		this.setHardness(24.0F);
		this.setResistance(1000.0F);
		this.setMinHarvestLevel(3);
	}

}
