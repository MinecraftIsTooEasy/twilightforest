package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.ItemMultiTextureTile;
import twilightforest.block.BlockTFThorns;

public class ItemBlockTFThorns extends ItemMultiTextureTile {

	public ItemBlockTFThorns(Block block, BlockTFThorns thorns, String[] names) {
		super(block, names);
	}

	public ItemBlockTFThorns(Block block, String[] names) {
		super(block, names);
	}

}
