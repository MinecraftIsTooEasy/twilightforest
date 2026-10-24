package twilightforest.mixin.event;

import net.minecraft.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.BlockTFUberousSoil;

/** Uberous soil is not a full-height block, which MITE requires below a placed plant. */
@Mixin(Block.class)
public abstract class BlockPlacementMixin {
    @Inject(method = "canBePlacedOnBlock(ILnet/minecraft/Block;ID)Z", at = @At("HEAD"), cancellable = true)
    private void tf$placeOnSoil(int metadata, Block below, int belowMetadata, double belowMaxY, CallbackInfoReturnable<Boolean> cir) {
        if (BlockTFUberousSoil.canVanillaPlantUse((Block) (Object) this, below)) {
            cir.setReturnValue(true);
        }
    }
}
