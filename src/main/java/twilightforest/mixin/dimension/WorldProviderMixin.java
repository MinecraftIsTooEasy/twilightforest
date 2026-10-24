package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.WorldProvider;
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

@Mixin(WorldProvider.class)
public abstract class WorldProviderMixin {
    @Mutable @Shadow @Final public boolean hasNoSky;
    @Shadow @Final public int dimensionId;

    /** WorldServer/WorldClient both build their provider through this factory (unknown ids -> null -> overworld fallback!). */
    @Inject(method = "getProviderForDimension", at = @At("HEAD"), cancellable = true)
    private static void tf$getProviderForDimension(int dimensionId, CallbackInfoReturnable<WorldProvider> cir) {
        if (TFDimension.is(dimensionId)) {
            cir.setReturnValue(new WorldProviderTwilightForest());
        }
    }

    /** ctor: else-branch calls Minecraft.setErrorMessage("WorldProvider: Unrecognized dimension id N") -> red HUD text. */
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;setErrorMessage(Ljava/lang/String;)V"))
    private void tf$muteUnrecognizedDimension(String text, Operation<Void> original) {
        if (!TFDimension.is(this.dimensionId)) {
            original.call(text);
        }
    }

    /** ctor sets hasNoSky = !is_overworld (true for every non 0 id). TF needs sky light. */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void tf$enableSky(int dimensionId, String name, CallbackInfo ci) {
        if (TFDimension.is(dimensionId)) {
            this.hasNoSky = false;
        }
    }
}
