package twilightforest.mixin.client;

import net.minecraft.BlockGrass;
import net.minecraft.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFBiomeColors;

/** Per-position grass colour of the TF biomes. */
@Mixin(BlockGrass.class)
public abstract class BlockGrassColorMixin {
    @Inject(method = "colorMultiplier(Lnet/minecraft/IBlockAccess;III)I", at = @At("HEAD"), cancellable = true)
    private void tf$biomeColor(IBlockAccess world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if (!TFBiomeColors.blendIsPositional(world, x, z)) {
            return;
        }
        int color = TFBiomeColors.blend(world, x, y, z, true);
        float trample = BlockGrass.getTramplingEffect(BlockGrass.getTramplings(world.getBlockMetadata(x, y, z)));
        if (trample > 0.0F) {
            float keep = 1.0F - trample;
            int r = (int) ((color >> 16 & 0xFF) * keep + 134.0F * trample);
            int g = (int) ((color >> 8 & 0xFF) * keep + 96.0F * trample);
            int b = (int) ((color & 0xFF) * keep + 67.0F * trample);
            color = r << 16 | g << 8 | b;
        }
        cir.setReturnValue(color);
    }
}
