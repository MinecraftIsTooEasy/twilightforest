package twilightforest.mixin.event;

import net.minecraft.Block;
import net.minecraft.BlockPlant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.block.BlockTFUberousSoil;

/** Forge canSustainPlant equivalent: vanilla plants accept ITFPlantSoil blocks. */
@Mixin(BlockPlant.class)
public abstract class BlockPlantLegalityMixin {
    @Inject(method = "isLegalOn(ILnet/minecraft/Block;I)Z", at = @At("HEAD"), cancellable = true)
    private void tf$soilLegal(int metadata, Block below, int belowMetadata, CallbackInfoReturnable<Boolean> cir) {
        if (BlockTFUberousSoil.canVanillaPlantUse((Block) (Object) this, below)) {
            cir.setReturnValue(true);
        }
    }
}
