package twilightforest.mixin.client;

import net.minecraft.EntityRenderer;
import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFRenderHandler;
import twilightforest.compat.ITFProviderRenderers;

/** Forge's WorldProvider.getWeatherRenderer() hook. HEAD, so the biome effects also draw while it is not raining. */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow private Minecraft mc;

    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    private void tf$renderWeather(float partialTick, CallbackInfo ci) {
        if (this.mc.theWorld.provider instanceof ITFProviderRenderers providerRenderers
                && providerRenderers.tfWeatherRenderer() instanceof TFRenderHandler weather) {
            weather.render(partialTick, this.mc.theWorld, this.mc);
            ci.cancel();
        }
    }
}
