package twilightforest.mixin.event;

import java.util.Random;
import net.minecraft.BlockIce;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFFeature;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

/**
 * Ice inside the Aurora Palace never melts. 1.7.10 built the palace from packed ice; MITE has none, so plain ice next to the
 * palace glowstone turned into water.
 */
@Mixin(BlockIce.class)
public abstract class BlockIceMeltMixin {
    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    private void tf$keepPalaceIce(World world, int x, int y, int z, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (world.isRemote || !(world.provider instanceof WorldProviderTwilightForest provider)) {
            return;
        }
        ChunkProviderTwilightForest chunkProvider = provider.getChunkProvider();
        if (chunkProvider != null && chunkProvider.isBlockInStructureBB(x, y, z)
                && ((TFWorldChunkManager) world.provider.worldChunkMgr).getFeatureAt(x, z, world) == TFFeature.iceTower) {
            cir.setReturnValue(false);
        }
    }
}
