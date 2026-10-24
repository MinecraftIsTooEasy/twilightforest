package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.RenderCow;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;

public class RenderTFDeer extends RenderCow {
    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "wilddeer.png");

	public RenderTFDeer(ModelBase par1ModelBase, float par2) {
		super(par1ModelBase, par2);
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
