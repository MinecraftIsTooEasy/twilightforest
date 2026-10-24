package twilightforest.client.renderer.blocks;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;
import twilightforest.block.BlockTFPlant;
import twilightforest.client.TFBlockRenderer;

public class RenderBlockTFPlants implements TFBlockRenderer {

	@Override
	public void renderInventoryBlock(Block block, int metadata, RenderBlocks renderer) {
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == BlockTFPlant.META_MOSSPATCH) {
			renderMossPatch(x, y, z, block, renderer);
		} else if (meta == BlockTFPlant.META_CLOVERPATCH) {
			renderCloverPatch(x, y, z, block, renderer);
		} else if (meta == BlockTFPlant.META_MAYAPPLE) {
			renderMayapple(x, y, z, block, renderer);
		} else if (meta == BlockTFPlant.META_ROOT_STRAND) {
			renderer.renderBlockCrops(block, x, y, z);
		} else {
			renderer.renderCrossedSquares(block, x, y, z);
		}

		return true;
	}

	private void renderMayapple(int x, int y, int z, Block block, RenderBlocks renderer) {
		renderer.clearOverrideBlockTexture();
		renderer.setRenderBounds(4F / 16F, 6F / 16F, 4F / 16F, 13F / 16F, 6F / 16F, 13F / 16F);
		renderer.renderStandardBlock(block, x, y, z);

		renderer.setOverrideBlockTexture(BlockTFPlant.mayappleSide);
		renderer.setRenderBounds(8F / 16F, 0F, 8F / 16F, 9F / 16F, 5.99F / 16F, 9F / 16F);
		renderer.renderStandardBlock(block, x, y, z);
		renderer.clearOverrideBlockTexture();
	}

	private void renderCloverPatch(int x, int y, int z, Block block, RenderBlocks renderer) {
		double minX = renderer.renderMinX;
		double maxX = renderer.renderMaxX;
		double maxY = renderer.renderMaxY;
		double minZ = renderer.renderMinZ;
		double maxZ = renderer.renderMaxZ;

		renderer.setRenderBounds(minX, maxY, minZ, maxX, maxY, maxZ);
		renderer.renderStandardBlock(block, x, y, z);

		renderer.setRenderBounds(minX + 1F / 16F, 0F, minZ + 1F / 16F, maxX - 1F / 16F, maxY - 0.01F, maxZ - 1F / 16F);
		renderer.renderStandardBlock(block, x, y, z);
	}

	private void renderMossPatch(int x, int y, int z, Block block, RenderBlocks renderer) {
		renderer.renderStandardBlock(block, x, y, z);

		// the bounds set by the dispatcher (what setRenderBoundsFromBlock restored in 1.7.10)
		final double x0 = renderer.renderMinX;
		final double x1 = renderer.renderMaxX;
		final double y0 = renderer.renderMinY;
		final double y1 = renderer.renderMaxY;
		final double z0 = renderer.renderMinZ;
		final double z1 = renderer.renderMaxZ;

		// add on shaggy edges
		if (x0 > 0) {
			long seed = (long) (x * 3129871) ^ (long) y * 116129781L ^ (long) z;
			seed = seed * seed * 42317861L + seed * 7L;
			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			double minX = x0 - 1F / 16F;
			double maxX = x0;
			double minZ = z0 + num0 / 16F;
			if (z1 - ((num1 + num2 + num3) / 16F) > minZ) {
				// draw two blobs
				renderer.setRenderBounds(minX, y0, minZ, maxX, y1, minZ + num1 / 16F);
				renderer.renderStandardBlock(block, x, y, z);
				double maxZ = z1 - num2 / 16F;
				renderer.setRenderBounds(minX, y0, maxZ - num3 / 16F, maxX, y1, maxZ);
				renderer.renderStandardBlock(block, x, y, z);
			} else {
				// draw one blob
				renderer.setRenderBounds(minX, y0, minZ, maxX, y1, z1 - num2 / 16F);
				renderer.renderStandardBlock(block, x, y, z);
			}
			renderer.setRenderBounds(x0, y0, z0, x1, y1, z1);
		}
		if (x1 < 1F) {
			long seed = (long) (x * 3129871) ^ (long) y * 116129781L ^ (long) z;
			seed = seed * seed * 42317861L + seed * 17L;
			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			double minX = x1;
			double maxX = x1 + 1F / 16F;
			double minZ = z0 + num0 / 16F;
			if (z1 - ((num1 + num2 + num3) / 16F) > minZ) {
				// draw two blobs
				renderer.setRenderBounds(minX, y0, minZ, maxX, y1, minZ + num1 / 16F);
				renderer.renderStandardBlock(block, x, y, z);
				double maxZ = z1 - num2 / 16F;
				renderer.setRenderBounds(minX, y0, maxZ - num3 / 16F, maxX, y1, maxZ);
				renderer.renderStandardBlock(block, x, y, z);
			} else {
				// draw one blob
				renderer.setRenderBounds(minX, y0, minZ, maxX, y1, z1 - num2 / 16F);
				renderer.renderStandardBlock(block, x, y, z);
			}
			renderer.setRenderBounds(x0, y0, z0, x1, y1, z1);
		}
		if (z0 > 0) {
			long seed = (long) (x * 3129871) ^ (long) y * 116129781L ^ (long) z;
			seed = seed * seed * 42317861L + seed * 23L;
			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			double minZ = z0 - 1F / 16F;
			double maxZ = z0;
			double minX = x0 + num0 / 16F;
			renderer.setRenderBounds(minX, y0, minZ, minX + num1 / 16F, y1, maxZ);
			renderer.renderStandardBlock(block, x, y, z);
			double maxX = x1 - num2 / 16F;
			renderer.setRenderBounds(maxX - num3 / 16F, y0, minZ, maxX, y1, maxZ);
			renderer.renderStandardBlock(block, x, y, z);
			renderer.setRenderBounds(x0, y0, z0, x1, y1, z1);
		}
		if (z1 < 1F) {
			long seed = (long) (x * 3129871) ^ (long) y * 116129781L ^ (long) z;
			seed = seed * seed * 42317861L + seed * 11L;
			int num0 = (int) (seed >> 12 & 3L) + 1;
			int num1 = (int) (seed >> 15 & 3L) + 1;
			int num2 = (int) (seed >> 18 & 3L) + 1;
			int num3 = (int) (seed >> 21 & 3L) + 1;

			double minZ = z1;
			double maxZ = z1 + 1F / 16F;
			double minX = x0 + num0 / 16F;
			renderer.setRenderBounds(minX, y0, minZ, minX + num1 / 16F, y1, maxZ);
			renderer.renderStandardBlock(block, x, y, z);
			double maxX = x1 - num2 / 16F;
			renderer.setRenderBounds(maxX - num3 / 16F, y0, minZ, maxX, y1, maxZ);
			renderer.renderStandardBlock(block, x, y, z);
			renderer.setRenderBounds(x0, y0, z0, x1, y1, z1);
		}
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

}
