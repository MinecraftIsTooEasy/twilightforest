package twilightforest.client.renderer.entity;


import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.ResourceLocation;

import org.lwjgl.opengl.GL11;

import twilightforest.TwilightForestMod;


public class RenderTFWraith extends RenderBiped {
	
    //texture = TwilightForestMod.MODEL_DIR + "ghost-crown.png";

    private static final ResourceLocation textureWraith = new ResourceLocation(TwilightForestMod.MODEL_DIR + "ghost.png");
    private static final ResourceLocation textureCrown = new ResourceLocation(TwilightForestMod.MODEL_DIR + "ghost-crown.png");

	public RenderTFWraith(ModelBiped modelbiped, float f) {
		super(modelbiped, f);
	}


    @Override


    protected int shouldRenderPass(EntityLivingBase entityliving, int i, float f)
    {
		setRenderPassModel(this.mainModel);

    	if (i == 1) {
            this.bindTexture(textureWraith);
	        GL11.glEnable(3042 /*GL_BLEND*/);
	        GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
	        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
	        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        //GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_DST_ALPHA);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
	    	
	        return 1;
    	} else {
    		return 0;
    	}
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
        return textureCrown;
    }

}
