package twilightforest.mixin.event;

import net.minecraft.DamageSource;
import net.minecraft.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFEvents;

/** ServerPlayer.onDeath never calls super.onDeath, so hooking EntityLivingBase.onDeath would miss players. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMixin {
    @Inject(method = "onDeath(Lnet/minecraft/DamageSource;)V", at = @At("HEAD"))
    private void tf$onDeath(DamageSource source, CallbackInfo ci) {
        TFEvents.onPlayerDeath((ServerPlayer) (Object) this, source);
    }
}
