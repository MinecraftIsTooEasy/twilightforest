package twilightforest.client.renderer.blocks;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;
import twilightforest.client.TFBlockRenderer;

/**
 * Draws nothing (the critters are rendered by their TESRs; BlockTFCritter.getRenderType() is -1 in MITE, see
 * TFRenderTypes.CRITTER). Kept only for name parity with 1.7.10 and is not registered.
 */
public class RenderBlockTFCritters implements TFBlockRenderer {

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
