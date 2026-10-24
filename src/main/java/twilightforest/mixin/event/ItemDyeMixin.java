package twilightforest.mixin.event;

import net.minecraft.Block;
import net.minecraft.EnumFace;
import net.minecraft.ItemDye;
import net.minecraft.ItemStack;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;
import twilightforest.block.BlockTFUberousSoil;

/** BonemealEvent equivalent. */
@Mixin(ItemDye.class)
public abstract class ItemDyeMixin {
    @Inject(method = "tryFertilize(Lnet/minecraft/ItemStack;Lnet/minecraft/World;IIILnet/minecraft/EnumFace;)Z", at = @At("HEAD"), cancellable = true)
    private static void tf$onBonemeal(ItemStack stack, World world, int x, int y, int z, EnumFace face, CallbackInfoReturnable<Boolean> cir) {
        if (BlockTFUberousSoil.tryBoneMeal(world, x, y, z)) {
            cir.setReturnValue(true);
            return;
        }
        if (TFEvents.onBonemeal(world, x, y, z, Block.blocksList[world.getBlockId(x, y, z)])) {
            cir.setReturnValue(true);
        }
    }
}
