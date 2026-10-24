package twilightforest.mixin.dimension;

import net.minecraft.ChatMessageComponent;
import net.minecraft.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.dimension.TFDimension;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    /**
     * MITE keeps one global spawn point without a dimension, so sleeping in the forest would turn forest coordinates into an
     * overworld respawn. Beds stay unusable (the vanilla check silently fails there); say so instead.
     */
    @Inject(method = "tryToSleepInBedAt", at = @At("HEAD"), cancellable = true)
    private void tf$noSleepInForest(int x, int y, int z, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (TFDimension.is(self.worldObj)) {
            self.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("tf.bed.disabled"));
            ci.cancel();
        }
    }
}
