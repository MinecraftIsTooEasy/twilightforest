package twilightforest.mixin.event;

import net.minecraft.TileEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.item.TFRecipes;

@Mixin(TileEntityFurnace.class)
public abstract class TileEntityFurnaceMixin {
    @Inject(method = "getHeatLevelRequired(I)I", at = @At("HEAD"), cancellable = true)
    private static void tf$heatLevel(int itemId, CallbackInfoReturnable<Integer> cir) {
        int level = TFRecipes.getHeatLevelOverride(itemId);
        if (level >= 0) {
            cir.setReturnValue(level);
        }
    }
}
