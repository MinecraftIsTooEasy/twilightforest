package twilightforest.client.renderer.entity;

import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;

public class RenderTFBiped extends RenderBiped {

	private final ResourceLocation textureLoc;

	public RenderTFBiped(ModelBiped modelBiped, float scale, String textureName) {
		super(modelBiped, scale);
		
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
