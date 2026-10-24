package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.ItemMultiTextureTile;
import twilightforest.block.BlockTFDeadrock;

public class ItemBlockTFDeadrock extends ItemMultiTextureTile {

	public ItemBlockTFDeadrock(Block block, BlockTFDeadrock deadrock, String[] names) {
		super(block, names);
	}

	public ItemBlockTFDeadrock(Block block, String[] names) {
		super(block, names);
	}

}
