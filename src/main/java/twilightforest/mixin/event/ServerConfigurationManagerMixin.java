package twilightforest.mixin.event;

import net.minecraft.ServerConfigurationManager;
import net.minecraft.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;

/** PlayerRespawnEvent, PlayerLoggedOutEvent and PlayerChangedDimensionEvent equivalents. */
@Mixin(ServerConfigurationManager.class)
public abstract class ServerConfigurationManagerMixin {
    @Inject(method = "respawnPlayer(Lnet/minecraft/ServerPlayer;IZ)Lnet/minecraft/ServerPlayer;", at = @At("RETURN"))
    private void tf$onRespawn(ServerPlayer oldPlayer, int dimension, boolean fromEnd, CallbackInfoReturnable<ServerPlayer> cir) {
        TFEvents.onPlayerRespawn(cir.getReturnValue());
    }

    /** Head, while the player is still in the world. */
    @Inject(method = "playerLoggedOut(Lnet/minecraft/ServerPlayer;)V", at = @At("HEAD"))
    private void tf$onLogout(ServerPlayer player, CallbackInfo ci) {
        TFEvents.onPlayerLoggedOut(player);
    }

    @Inject(method = "transferPlayerToDimension(Lnet/minecraft/ServerPlayer;I)V", at = @At("RETURN"))
    private void tf$onDimensionChanged(ServerPlayer player, int toDimension, CallbackInfo ci) {
        TFEvents.onPlayerChangedDimension(player, toDimension);
    }
}
