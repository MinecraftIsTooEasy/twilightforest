package twilightforest.mixin.client;

import net.minecraft.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFClientTicker;

/** Stands in for Forge's ClientTickEvent. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("RETURN"))
    private void tf$clientTick(CallbackInfo ci) {
        TFClientTicker.clientTick((Minecraft) (Object) this);
    }
}
