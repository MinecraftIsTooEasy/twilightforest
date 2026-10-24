package twilightforest.mixin.client;

import net.minecraft.BlockLeaves;
import net.minecraft.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFBiomeColors;

/** Per-position foliage colour of the TF biomes for vanilla oak/jungle-style leaves (pine and birch keep their fixed colours). */
@Mixin(BlockLeaves.class)
public abstract class BlockLeavesColorMixin {
    @Inject(method = "colorMultiplier(Lnet/minecraft/IBlockAccess;III)I", at = @At("HEAD"), cancellable = true)
    private void tf$biomeColor(IBlockAccess world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if ((world.getBlockMetadata(x, y, z) & 3) == 0 || (world.getBlockMetadata(x, y, z) & 3) == 3) {
            if (TFBiomeColors.blendIsPositional(world, x, z)) {
                cir.setReturnValue(TFBiomeColors.blend(world, x, y, z, false));
            }
        }
    }
}
