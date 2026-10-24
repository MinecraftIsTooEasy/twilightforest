package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.Render;
import net.minecraft.RenderLiving;
import net.minecraft.RenderManager;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.MathHelper;
import net.minecraft.ResourceLocation;

import org.lwjgl.opengl.GL11;

import twilightforest.TwilightForestMod;

public class RenderTFHarbingerCube extends RenderLiving {

    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "apocalypse2.png");

	public RenderTFHarbingerCube() {
        super(new ModelTFApocalypseCube(), 1.0F);
	}

    
	@Override
	protected void setTextures() {
		// textures are returned by getEntityTexture
	}

	/**
	 * Return our specific texture
	 */
    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return textureLoc;
    }
    
    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
    	float scale = 1.0F;
        GL11.glScalef(scale, scale, scale);
    }
}
