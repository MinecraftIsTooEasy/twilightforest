package twilightforest.client.renderer.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.ModelBiped;
import net.minecraft.Render;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.ItemStack;
import net.minecraft.ResourceLocation;

public class RenderTFAdherent extends RenderTFBiped {

	public RenderTFAdherent(ModelBiped modelBiped, float scale, String textureName) {
		super(modelBiped, scale, textureName);
	}

	
    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRenderLiving(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
    	//GL11.glTranslatef(0.0F, 1.0F, 0.0F);

        super.doRenderLiving(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

}
