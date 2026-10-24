package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.RenderLiving;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.MathHelper;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.EntityTFBird;


public class RenderTFBird extends RenderLiving
{
	private final ResourceLocation textureLoc;

	
    public RenderTFBird(ModelBase par1ModelBase, float par2, String textureName)
    {
        super(par1ModelBase, par2);
        
		textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + textureName);
    }

    @Override
    protected void setTextures()
    {
        // textures are returned by getEntityTexture
    }

    public void renderTFBird(EntityTFBird par1EntityTFBird, double par2, double par4, double par6, float par8, float par9)
    {
        // RenderLiving.doRender dispatches to this.doRenderLiving, so calling it here would recurse forever
        super.doRenderLiving(par1EntityTFBird, par2, par4, par6, par8, par9);
    }

    protected float getWingRotation(EntityTFBird par1EntityTFBird, float time)
    {
        float var3 = par1EntityTFBird.lastFlapLength + (par1EntityTFBird.flapLength - par1EntityTFBird.lastFlapLength) * time;
        float var4 = par1EntityTFBird.lastFlapIntensity + (par1EntityTFBird.flapIntensity - par1EntityTFBird.lastFlapIntensity) * time;
        return (MathHelper.sin(var3) + 1.0F) * var4;
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    @Override
    protected float handleRotationFloat(EntityLivingBase par1EntityLiving, float par2)
    {
        return this.getWingRotation((EntityTFBird)par1EntityLiving, par2);
    }

    @Override
    public void doRenderLiving(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderTFBird((EntityTFBird)par1EntityLiving, par2, par4, par6, par8, par9);
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
        this.renderTFBird((EntityTFBird)par1Entity, par2, par4, par6, par8, par9);
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
