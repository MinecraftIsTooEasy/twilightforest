package twilightforest.client.renderer.blocks;

import net.minecraft.Block;
import net.minecraft.RenderBlocks;
import net.minecraft.Tessellator;
import net.minecraft.IBlockAccess;

import org.lwjgl.opengl.GL11;

import twilightforest.block.BlockTFFireflyJar;
import twilightforest.client.TFBlockRenderer;

public class RenderBlockTFFireflyJar implements TFBlockRenderer {
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, RenderBlocks renderer) {
		renderInvJar(renderer, block, metadata);

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
		return renderJar(renderer, world, x, y, z, block);
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	/**
	 * Look, this is no longer in the Block class!  I'm an object oriented genius!
	 */
	public static boolean renderJar(RenderBlocks renderblocks, IBlockAccess world, int x, int y, int z, Block block) {
        renderblocks.clearOverrideBlockTexture();
        renderblocks.setRenderBounds(0.1875F, 0.0F, 0.1875F, 0.8125F, 0.875F, 0.8125F);
        renderblocks.renderStandardBlock(block, x, y, z);
        
        renderblocks.setOverrideBlockTexture(BlockTFFireflyJar.jarCork);
        renderblocks.setRenderBounds(0.25F, 0.75F, 0.25F, 0.75F, 1.0F, 0.75F);
        renderblocks.renderStandardBlock(block, x, y, z);
        renderblocks.clearOverrideBlockTexture();
        
        block.setBlockBoundsForItemRender(0);
        return true;
	}
	
	public static void renderInvJar(RenderBlocks renderblocks, Block par1Block, int meta) {
		Tessellator tessellator =  Tessellator.instance;
		
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);        

		// render jar portion
        renderblocks.setRenderBounds(0.1875F, 0.0F, 0.1875F, 0.8125F, 0.875F, 0.8125F);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderblocks.renderFaceYNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarTop);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderblocks.renderFaceYPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarTop);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderblocks.renderFaceXPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarSide);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderblocks.renderFaceXNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarSide);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarSide);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarSide);
        tessellator.draw();
        
        // render lid thing
        renderblocks.setRenderBounds(0.25F, 0.75F, 0.25F, 0.75F, 1.0F, 0.75F);
        
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderblocks.renderFaceYNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderblocks.renderFaceYPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderblocks.renderFaceXPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderblocks.renderFaceXNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZNeg(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZPos(par1Block, 0.0D, 0.0D, 0.0D, BlockTFFireflyJar.jarCork);
        tessellator.draw();
//        renderblocks.renderBottomFace(par1Block, 0.0D, 0.0D, 0.0D, sprLid);
//        renderblocks.renderFaceYPos(par1Block, 0.0D, 0.0D, 0.0D, sprLid);
//        renderblocks.renderFaceXPos(par1Block, 0.0D, 0.0D, 0.0D, sprLid);
//        renderblocks.renderFaceXNeg(par1Block, 0.0D, 0.0D, 0.0D, sprLid);
//        renderblocks.renderFaceZNeg(par1Block, 0.0D, 0.0D, 0.0D, sprLid);
//        renderblocks.renderFaceZPos(par1Block, 0.0D, 0.0D, 0.0D, sprLid);

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);

        par1Block.setBlockBoundsForItemRender(meta);
	}


}
