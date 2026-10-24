package twilightforest.mixin.dimension;

import net.minecraft.Entity;
import net.minecraft.ILogAgent;
import net.minecraft.ISaveHandler;
import net.minecraft.Profiler;
import net.minecraft.Vec3;
import net.minecraft.Vec3Pool;
import net.minecraft.World;
import net.minecraft.WorldProvider;
import net.minecraft.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.dimension.TFDimension;
import twilightforest.world.WorldProviderTwilightForest;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow @Final public WorldProvider provider;
    @Mutable @Shadow @Final private boolean has_skylight;
    @Shadow public abstract Vec3Pool getWorldVec3Pool();

    // World.<init> assigns has_skylight = !provider.hasNoSky && is_overworld. TF is not "overworld" but has skylight.
    // Both constructors; has_skylight is still false while registerWorld/createChunkProvider run inside them.
    @Inject(method = "<init>(Lnet/minecraft/ISaveHandler;Ljava/lang/String;Lnet/minecraft/WorldProvider;Lnet/minecraft/WorldSettings;Lnet/minecraft/Profiler;Lnet/minecraft/ILogAgent;JJ)V", at = @At("RETURN"))
    private void tf$skylightClient(ISaveHandler h, String name, WorldProvider p, WorldSettings s, Profiler prof, ILogAgent log, long creation, long total, CallbackInfo ci) {
        this.tf$fixSkylight();
    }

    @Inject(method = "<init>(Lnet/minecraft/ISaveHandler;Ljava/lang/String;Lnet/minecraft/WorldSettings;Lnet/minecraft/WorldProvider;Lnet/minecraft/Profiler;Lnet/minecraft/ILogAgent;)V", at = @At("RETURN"))
    private void tf$skylightServer(ISaveHandler h, String name, WorldSettings s, WorldProvider p, Profiler prof, ILogAgent log, CallbackInfo ci) {
        this.tf$fixSkylight();
    }

    private void tf$fixSkylight() {
        if (TFDimension.is(this.provider.dimensionId)) {
            this.has_skylight = !this.provider.hasNoSky;
        }
    }

    // Forge keeps these on WorldProvider; MITE computes them in World.
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void tf$skyColor(Entity viewer, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(this.getWorldVec3Pool().getVecFromPool(
                WorldProviderTwilightForest.SKY_COLOR_RED, WorldProviderTwilightForest.SKY_COLOR_GREEN, WorldProviderTwilightForest.SKY_COLOR_BLUE));
        }
    }

    @Inject(method = "getStarBrightness", at = @At("HEAD"), cancellable = true)
    private void tf$starBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(WorldProviderTwilightForest.STAR_BRIGHTNESS);
        }
    }

    @Inject(method = "getHorizon", at = @At("HEAD"), cancellable = true)
    private void tf$horizon(CallbackInfoReturnable<Double> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(WorldProviderTwilightForest.HORIZON);
        }
    }

    /**
     * The forest has no sun: it must never be "day" (undead burning, daylight AI, spawn rules), whatever its own time slot says.
     * Only the instance method; the static isDaytime(long) feeds the moon logic and stays untouched.
     */
    @Inject(method = "isDaytime()Z", at = @At("HEAD"), cancellable = true)
    private void tf$neverDaytime(CallbackInfoReturnable<Boolean> cir) {
        if (TFDimension.is(this.provider.dimensionId)) {
            cir.setReturnValue(false);
        }
    }
}
