package twilightforest.client.renderer.entity;

import net.minecraft.Entity;
import net.minecraft.ModelBase;
import net.minecraft.RenderGelatinousCube;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;

/**
 * MITE: RenderSlime is a broken class (no setTextures) and EntitySlime is final, so this extends RenderGelatinousCube.
 * The entity must therefore be an EntityGelatinousCube (see MITE_CLIENT.md).
 */
public class RenderTFMazeSlime extends RenderGelatinousCube {

    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "mazeslime.png");

	public RenderTFMazeSlime(ModelBase par1ModelBase, ModelBase par2ModelBase, float par3) {
		super(par1ModelBase, par2ModelBase, par3);
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
