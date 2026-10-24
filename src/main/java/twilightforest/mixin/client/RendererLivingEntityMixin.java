package twilightforest.mixin.client;

import net.minecraft.EntityLivingBase;
import net.minecraft.RendererLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFClientEvents;

/** Stands in for Forge's RenderLivingEvent.Post. */
@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Inject(method = "doRenderLiving(Lnet/minecraft/EntityLivingBase;DDDFF)V", at = @At("TAIL"))
    private void tf$renderLivingPost(EntityLivingBase entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        TFClientEvents.renderLivingPost(entity, (RendererLivingEntity) (Object) this, x, y, z);
    }
}
