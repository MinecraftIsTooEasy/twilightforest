package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.Minecraft;
import net.minecraft.RenderGlobal;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFRenderHandler;
import twilightforest.compat.ITFProviderRenderers;
import twilightforest.dimension.TFDimension;

/** Forge's WorldProvider.getSkyRenderer() hook, and the surface-world-only cloud check. */
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Shadow private Minecraft mc;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void tf$renderSky(float partialTick, CallbackInfo ci) {
        if (this.mc.theWorld.provider instanceof ITFProviderRenderers providerRenderers
                && providerRenderers.tfSkyRenderer() instanceof TFRenderHandler sky) {
            // MITE's renderSky starts with glDisable(GL_LIGHTING), which the cancel skips
            GL11.glDisable(2896);
            sky.render(partialTick, this.mc.theWorld, this.mc);
            ci.cancel();
        }
    }

    // WorldProvider.isSurfaceWorld() is final and only true for dimension 0
    @ModifyExpressionValue(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/WorldProvider;isSurfaceWorld()Z"))
    private boolean tf$cloudsInTwilightForest(boolean isSurfaceWorld) {
        return isSurfaceWorld || TFDimension.is(this.mc.theWorld);
    }
}
