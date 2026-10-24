package twilightforest.client;

import net.minecraft.Minecraft;
import net.minecraft.StructureBoundingBox;
import net.minecraft.WorldClient;

/**
 * Replacement for Forge's IRenderHandler, the parent of TFSkyRenderer and TFWeatherRenderer. Reached through
 * ITFProviderRenderers from RenderGlobalMixin / EntityRendererMixin.
 */
public abstract class TFRenderHandler {
    public abstract void render(float partialTicks, WorldClient world, Minecraft mc);

    /** Only the weather renderer cares: the structure box shown by the enforced-progression protection effect. */
    public void setProtectedBox(StructureBoundingBox box) {
    }
}
