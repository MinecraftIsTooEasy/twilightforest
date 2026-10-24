package twilightforest.mixin.client;

import net.minecraft.BlockTallGrass;
import net.minecraft.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFBiomeColors;

/** Per-position grass colour of the TF biomes for tall grass and ferns. */
@Mixin(BlockTallGrass.class)
public abstract class BlockTallGrassColorMixin {
    @Inject(method = "colorMultiplier(Lnet/minecraft/IBlockAccess;III)I", at = @At("HEAD"), cancellable = true)
    private void tf$biomeColor(IBlockAccess world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if (world.getBlockMetadata(x, y, z) != 0) {
            cir.setReturnValue(TFBiomeColors.grass(world.getBiomeGenForCoords(x, z), x, y, z));
        }
    }
}
