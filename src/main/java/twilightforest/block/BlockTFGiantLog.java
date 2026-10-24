package twilightforest.block;

import net.minecraft.Block;

public class BlockTFGiantLog extends BlockTFGiantBlock {

	public BlockTFGiantLog(int id) {
		super(id, Block.wood);
		// balance (MITE_BALANCE B7): hardness 4.8, harvest level 1 (axe, never bare-handed), resistance 5
		this.setHardness(4.8F);
		this.setResistance(5.0F);
		this.setMinHarvestLevel(1);
	}

}
