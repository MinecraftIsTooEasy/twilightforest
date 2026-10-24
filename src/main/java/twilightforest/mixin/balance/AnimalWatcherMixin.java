package twilightforest.mixin.balance;

import net.minecraft.EntityAnimalWatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFConfig;
import twilightforest.dimension.TFDimension;

/** MITE_BALANCE X5 / H9: zombies, ghouls and friends cannot dig through TF structures. */
@Mixin(EntityAnimalWatcher.class)
public abstract class AnimalWatcherMixin {
    @Inject(method = "canDestroyBlock(IIIZ)Z", at = @At("HEAD"), cancellable = true)
    private void tf$noDiggingInForest(int x, int y, int z, boolean checkClipping, CallbackInfoReturnable<Boolean> cir) {
        EntityAnimalWatcher self = (EntityAnimalWatcher) (Object) this;
        if (!TFConfig.mobDigging && TFDimension.is(self.worldObj)) {
            cir.setReturnValue(false);
        }
    }
}
