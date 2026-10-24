package twilightforest.client.renderer.entity;

import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.TextureMap;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.Block;
import net.minecraft.ResourceLocation;

import org.lwjgl.opengl.GL11;

import twilightforest.TwilightForestMod;

public class RenderTFMinoshroom extends RenderBiped {

    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "minoshroomtaur.png");

	public RenderTFMinoshroom(ModelBiped par1ModelBase, float par2) {
		super(par1ModelBase, par2);
	}
	
    @Override
    protected void setTextures() {
        // textures are returned by getEntityTexture
    }

    protected void renderMooshroomEquippedItems(EntityLivingBase par1EntityLiving, float par2)
    {
        super.renderEquippedItems(par1EntityLiving, par2);

        this.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPushMatrix();
        GL11.glScalef(1.0F, -1.0F, 1.0F);
        GL11.glTranslatef(0.2F, 0.375F, 0.5F);
        GL11.glRotatef(42.0F, 0.0F, 1.0F, 0.0F);
        this.renderBlocks.renderBlockAsItem(Block.mushroomRed, 0, 1.0F);
        GL11.glTranslatef(0.1F, 0.0F, -0.6F);
        GL11.glRotatef(42.0F, 0.0F, 1.0F, 0.0F);
        this.renderBlocks.renderBlockAsItem(Block.mushroomRed, 0, 1.0F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        ((ModelBiped)this.mainModel).bipedHead.postRender(0.0625F);
        GL11.glScalef(1.0F, -1.0F, 1.0F);
        GL11.glTranslatef(0.0F, 1.0F, 0F);
        GL11.glRotatef(12.0F, 0.0F, 1.0F, 0.0F);
        this.renderBlocks.renderBlockAsItem(Block.mushroomRed, 0, 1.0F);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
    
    @Override
    protected void renderEquippedItems(EntityLivingBase par1EntityLiving, float par2)
    {
        this.renderMooshroomEquippedItems(par1EntityLiving, par2);
    }

	/**
	 * Return our specific texture
	 */
    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return textureLoc;
    }

}
