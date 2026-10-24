package twilightforest.mixin.event;

import net.minecraft.Block;
import net.minecraft.BlockMounted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.6.4 structure code places torches etc. with metadata 0, which vanilla fixes up but MITE maps to no face (null).
 * WorldGenLakes then NPEs in isLegalOn; treat such blocks as illegal instead so they get dropped.
 */
@Mixin(BlockMounted.class)
public abstract class BlockMountedLegalityMixin {
    @Inject(method = "isLegalOn(ILnet/minecraft/Block;I)Z", at = @At("HEAD"), cancellable = true)
    private void tf$noFaceIsIllegal(int metadata, Block below, int belowMetadata, CallbackInfoReturnable<Boolean> cir) {
        if (((BlockMounted) (Object) this).getFaceMountedTo(metadata) == null) {
            cir.setReturnValue(false);
        }
    }
}
