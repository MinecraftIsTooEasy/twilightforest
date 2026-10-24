package twilightforest.client.renderer.entity;

import net.minecraft.RenderSpider;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;

public class RenderTFHedgeSpider extends RenderSpider {

    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "hedgespider.png");

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return textureLoc;
	}

}
