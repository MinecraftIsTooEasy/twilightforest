package twilightforest.client.renderer.blocks;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;
import twilightforest.client.TFBlockRenderer;

/** Dead code in 1.7.10 (never referenced); kept for name parity, not registered. */
public class RenderBlockTFGiantBlocks implements TFBlockRenderer {

	@Override
	public void renderInventoryBlock(Block block, int metadata, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

}
