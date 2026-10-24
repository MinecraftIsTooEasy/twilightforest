package twilightforest.mixin.balance;

import net.minecraft.World;
import net.minecraft.WorldProvider;
import net.minecraft.WorldServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFConfig;
import twilightforest.dimension.TFDimension;

/** MITE_BALANCE X1/X2 (overworld day and blood moon mirrored into the forest) and 4.2 (no weather in the forest). */
@Mixin(World.class)
public abstract class WorldRulesMixin {
    @Shadow @Final public WorldProvider provider;

    private static WorldServer tf$overworld() {
        MinecraftServer server = MinecraftServer.getServer();
        return server == null || server.worldServers == null ? null : server.worldServers[0];
    }

    @Inject(method = "getDayOfWorld()I", at = @At("HEAD"), cancellable = true)
    private void tf$overworldDay(CallbackInfoReturnable<Integer> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            WorldServer overworld = tf$overworld();
            if (overworld != null) {
                cir.setReturnValue(overworld.getDayOfWorld());
            }
        }
    }

    @Inject(method = "isBloodMoon(Z)Z", at = @At("HEAD"), cancellable = true)
    private void tf$mirrorBloodMoon(boolean exclusivelyAtNight, CallbackInfoReturnable<Boolean> cir) {
        if (TFConfig.mirrorBloodMoon && TFDimension.is(this.provider.dimensionId)) {
            WorldServer overworld = tf$overworld();
            if (overworld != null) {
                cir.setReturnValue(overworld.isBloodMoon(exclusivelyAtNight));
            }
        }
    }

    @Inject(method = "isPrecipitating(Z)Z", at = @At("HEAD"), cancellable = true)
    private void tf$noRain(boolean byStrength, CallbackInfoReturnable<Boolean> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isThundering(Z)Z", at = @At("HEAD"), cancellable = true)
    private void tf$noStorm(boolean byStrength, CallbackInfoReturnable<Boolean> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRainStrength(F)F", at = @At("HEAD"), cancellable = true)
    private void tf$noRainStrength(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(0.0F);
        }
    }
}
