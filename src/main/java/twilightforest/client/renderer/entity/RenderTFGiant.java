package twilightforest.client.renderer.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.Minecraft;
import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.ResourceLocation;

public class RenderTFGiant extends RenderBiped {

	private ResourceLocation textureLoc;

	public RenderTFGiant() {
		super(new ModelBiped(), 0.625F);
		
		this.textureLoc = new ResourceLocation("textures/entity/steve.png");
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
    	if (Minecraft.getMinecraft().thePlayer.getLocationSkin() != null) {
    		return Minecraft.getMinecraft().thePlayer.getLocationSkin();
    	} else {
    		return textureLoc;
    	}
    }
    
    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
    	float scale = 4.0F;
        GL11.glScalef(scale, scale, scale);
    }
}
