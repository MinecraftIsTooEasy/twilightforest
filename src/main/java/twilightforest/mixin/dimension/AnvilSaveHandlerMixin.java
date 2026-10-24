package twilightforest.mixin.dimension;

import java.io.File;
import net.minecraft.AnvilChunkLoader;
import net.minecraft.AnvilSaveHandler;
import net.minecraft.IChunkLoader;
import net.minecraft.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.dimension.TFDimension;

@Mixin(AnvilSaveHandler.class)
public abstract class AnvilSaveHandlerMixin {
    /** Vanilla picks DIM-2 / DIM-1 / DIM1 by instanceof and falls back to the ROOT (overworld) folder for everything else. */
    @Inject(method = "getChunkLoader", at = @At("HEAD"), cancellable = true)
    private void tf$chunkLoader(WorldProvider provider, CallbackInfoReturnable<IChunkLoader> cir) {
        if (TFDimension.is(provider.dimensionId)) {
            File dir = new File(((SaveHandlerAccessor) this).tf$getWorldDirectory(), "DIM" + provider.dimensionId);
            dir.mkdirs();
            cir.setReturnValue(new AnvilChunkLoader(dir));
        }
    }
}
