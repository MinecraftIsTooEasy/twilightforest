package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.Render;
import net.minecraft.RenderManager;
import net.minecraft.Entity;
import net.minecraft.MathHelper;
import net.minecraft.ResourceLocation;

import org.lwjgl.opengl.GL11;

import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFBlockGoblin;
import twilightforest.entity.EntityTFChainBlock;

public class RenderTFChainBlock extends Render {

	private ModelBase model;
    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "blockgoblin.png");

	public RenderTFChainBlock(ModelBase modelTFSpikeBlock, float f) {
		this.model = modelTFSpikeBlock;
	}

    /**
     * The render method used in RenderBoat that renders the boat model.
     */
    public void renderSpikeBlock(EntityTFChainBlock par1Entity, double par2, double par4, double par6, float par8, float time)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);

        this.bindEntityTexture(par1Entity);

        GL11.glScalef(-1.0F, -1.0F, 1.0F);
        
        
        GL11.glRotatef(MathHelper.wrapAngleTo180_float((float)par4), 1, 0, 1);
        GL11.glRotatef(MathHelper.wrapAngleTo180_float(((float)par2 + (float)par6) * 11F), 0, 1, 0);
//        GL11.glRotatef(MathHelper.wrapAngleTo180_float((float)par8), 0, 0, 1);
        

        
        this.model.render(par1Entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        EntityTFChainBlock chainBlock = (EntityTFChainBlock)par1Entity;

        this.renderSpikeBlock(chainBlock, par2, par4, par6, par8, par9);
                
		RenderManager.instance.renderEntity(chainBlock.chain1, par9);
		RenderManager.instance.renderEntity(chainBlock.chain2, par9);
		RenderManager.instance.renderEntity(chainBlock.chain3, par9);
		RenderManager.instance.renderEntity(chainBlock.chain4, par9);
		RenderManager.instance.renderEntity(chainBlock.chain5, par9);
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
