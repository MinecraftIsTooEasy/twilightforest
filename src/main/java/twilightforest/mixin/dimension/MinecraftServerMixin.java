package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.dimension.TFDimension;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    /** loadAllWorlds: `this.worldServers = new WorldServer[4]` is the only iconst_4 in the method. */
    @ModifyConstant(method = "loadAllWorlds", constant = @Constant(intValue = 4))
    private int tf$worldCount(int vanilla) {
        return Math.max(vanilla, TFDimension.WORLD_COUNT);
    }

    @Inject(method = "getWorldIndexForDimensionId", at = @At("HEAD"), cancellable = true)
    private static void tf$indexForDimension(int dimensionId, CallbackInfoReturnable<Integer> cir) {
        if (TFDimension.is(dimensionId)) {
            cir.setReturnValue(TFDimension.WORLD_INDEX);
        }
    }

    @Inject(method = "getWorldDimensionIdFromIndex", at = @At("HEAD"), cancellable = true)
    private static void tf$dimensionForIndex(int index, CallbackInfoReturnable<Integer> cir) {
        if (index == TFDimension.WORLD_INDEX) {
            cir.setReturnValue(TFDimension.id);
        }
    }

    /** `if (index == 0 || getAllowNether())` would freeze the forest on servers with allow-nether=false. */
    @WrapOperation(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllowNether()Z"))
    private boolean tf$tickForestWithoutNether(MinecraftServer server, Operation<Boolean> original, @Local(ordinal = 0) int worldIndex) {
        return (TFDimension.ignoreAllowNether && worldIndex == TFDimension.WORLD_INDEX) || original.call(server);
    }
}
