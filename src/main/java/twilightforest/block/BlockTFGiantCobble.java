package twilightforest.block;

import net.minecraft.Block;

public class BlockTFGiantCobble extends BlockTFGiantBlock {

	public BlockTFGiantCobble(int id) {
		super(id, Block.cobblestone);
		// balance (MITE_BALANCE B7): hardness 9.6, harvest level 2, resistance 15
		this.setHardness(9.6F);
		this.setResistance(15.0F);
		this.setMinHarvestLevel(2);
	}

}
