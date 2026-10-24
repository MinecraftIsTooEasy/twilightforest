package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.RenderLiving;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;

public class RenderTFGenericLiving extends RenderLiving {
	
	final ResourceLocation textureLoc;

	public RenderTFGenericLiving(ModelBase par1ModelBase, float par2, String textureName) {
		super(par1ModelBase, par2);
		
		if (textureName.startsWith("textures"))
		{
			textureLoc = new ResourceLocation(textureName);
		}
		else
		{
			textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + textureName);
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
        return textureLoc;
    }

}
