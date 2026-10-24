package twilightforest.mixin.event;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFEvents;

/** WorldEvent.Load / FMLServerStartingEvent equivalent; both the dedicated and the integrated server call this. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerLoadMixin {
    @Inject(method = "initialWorldChunkLoad()V", at = @At("HEAD"))
    private void tf$onWorldsLoaded(CallbackInfo ci) {
        TFEvents.onServerWorldsLoaded((MinecraftServer) (Object) this);
    }
}
