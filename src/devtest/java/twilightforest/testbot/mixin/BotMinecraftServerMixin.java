package twilightforest.testbot.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.testbot.TestBot;

/** Dev only: runs the bot's server side tasks on the integrated server thread, at the end of a server tick. */
@Mixin(MinecraftServer.class)
public abstract class BotMinecraftServerMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void tfbot$serverTick(CallbackInfo ci) {
        TestBot.onServerTick((MinecraftServer) (Object) this);
    }
}
