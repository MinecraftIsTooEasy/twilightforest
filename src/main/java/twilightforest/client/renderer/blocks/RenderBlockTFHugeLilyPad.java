package twilightforest.client.renderer.blocks;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;
import twilightforest.client.TFBlockRenderer;

public class RenderBlockTFHugeLilyPad implements TFBlockRenderer {

	@Override
	public void renderInventoryBlock(Block block, int metadata, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
		// rotate the sides
		int meta = world.getBlockMetadata(x, y, z);

		setRenderRotate(renderer, meta);
		boolean didRender = renderer.renderStandardBlock(block, x, y, z);
		restoreRendererRotate(renderer);

		return didRender;
	}

	private void restoreRendererRotate(RenderBlocks renderer) {
		renderer.uvRotateSouth = 0;
		renderer.uvRotateEast = 0;
		renderer.uvRotateWest = 0;
		renderer.uvRotateNorth = 0;
		renderer.uvRotateTop = 0;
		renderer.uvRotateBottom = 0;
	}

	private void setRenderRotate(RenderBlocks renderer, int meta) {
		int orient = meta >> 2;

		// why can't this just be simple?
		if (orient == 2) {
			orient = 3;
		} else if (orient == 3) {
			orient = 2;
		}

		renderer.uvRotateTop = orient;
		renderer.uvRotateBottom = orient;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

}
