package twilightforest.mixin.balance;

import net.minecraft.Entity;
import net.minecraft.Teleporter;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFBalance;

/** /tf stuck: the player arrives at the global overworld spawn instead of a freshly built portal (runs before the forest teleporter). */
@Mixin(value = Teleporter.class, priority = 500)
public abstract class TeleporterStuckMixin {
    @Shadow @Final private WorldServer worldServerInstance;

    @Inject(method = "placeInPortal", at = @At("HEAD"), cancellable = true)
    private void tf$placeAtSpawn(Entity entity, int dimensionIdFrom, double x, double y, double z, float yaw, CallbackInfo ci) {
        if (!TFBalance.stuckTransfer) {
            return;
        }
        WorldServer world = this.worldServerInstance;
        int sx = world.getSpawnX();
        int sz = world.getSpawnZ();
        int sy = world.getTopSolidOrLiquidBlock(sx, sz);
        entity.setLocationAndAngles(sx + 0.5D, sy + 0.1D, sz + 0.5D, yaw, 0.0F);
        ci.cancel();
    }
}
