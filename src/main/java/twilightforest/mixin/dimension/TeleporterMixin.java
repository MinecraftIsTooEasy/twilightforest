package twilightforest.mixin.dimension;

import net.minecraft.Entity;
import net.minecraft.Teleporter;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFTeleporter;
import twilightforest.dimension.TFDimension;

@Mixin(Teleporter.class)
public abstract class TeleporterMixin {
    @Shadow @Final private WorldServer worldServerInstance;

    /** transferEntityToWorld always calls destinationWorld.getDefaultTeleporter().placeInPortal(entity, fromDim, x, y, z, yaw). */
    @Inject(method = "placeInPortal", at = @At("HEAD"), cancellable = true)
    private void tf$placeInPortal(Entity entity, int dimensionIdFrom, double x, double y, double z, float yaw, CallbackInfo ci) {
        if (TFDimension.is(this.worldServerInstance.provider.dimensionId) || TFDimension.is(dimensionIdFrom)) {
            new TFTeleporter(this.worldServerInstance).placeInPortal(entity, x, y, z, yaw);
            ci.cancel();
        }
    }
}
