package twilightforest.mixin.dimension;

import net.minecraft.World;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.dimension.TFDimension;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin {
    /**
     * MITE freezes every world that has no player standing in "its" group (WorldServer.java:136-160): in single player / LAN an
     * unknown dimension only advances while a player is in dim -2/-1/0. TF: tick (time, random block ticks, ambience) iff a real player is inside.
     */
    @Inject(method = "shouldTimeProgress", at = @At("HEAD"), cancellable = true)
    private void tf$shouldTimeProgress(CallbackInfoReturnable<Boolean> cir) {
        World self = (World) (Object) this;
        if (TFDimension.is(self.provider.dimensionId)) {
            cir.setReturnValue(self.hasNonGhostPlayers());
        }
    }
}
