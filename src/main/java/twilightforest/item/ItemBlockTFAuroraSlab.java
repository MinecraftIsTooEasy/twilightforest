package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.BlockDoubleSlab;
import net.minecraft.BlockSlab;
import net.minecraft.ItemSlab;

/**
 * MITE's ItemSlab takes (half slab, double slab, isFullBlock) and derives the ItemBlock block itself.
 * The leading Block parameter is kept so TFRegistry.registerBlock's reflection constructor (Block, extra...) still works.
 */
public class ItemBlockTFAuroraSlab extends ItemSlab {

	public ItemBlockTFAuroraSlab(Block block, BlockSlab singleSlab, BlockDoubleSlab doubleSlab, Boolean isDouble) {
		super(singleSlab, doubleSlab, isDouble);
	}

	public ItemBlockTFAuroraSlab(BlockSlab singleSlab, BlockDoubleSlab doubleSlab, boolean isDouble) {
		super(singleSlab, doubleSlab, isDouble);
	}

}
