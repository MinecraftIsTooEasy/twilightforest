package twilightforest.mixin.dimension;

import net.minecraft.Entity;
import net.minecraft.MathHelper;
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

    /**
     * transferEntityToWorld always calls destinationWorld.getDefaultTeleporter().placeInPortal(entity, fromDim, x, y, z, yaw).
     * Only a trip through a TF portal goes to the TF teleporter. A command or other mod teleport into or out of the forest
     * (/home, /back, /tpa) keeps the position its caller chose, so it neither builds a stray portal nor moves the player to
     * one; a nether portal inside the forest stays with MITE's own teleporter.
     */
    @Inject(method = "placeInPortal", at = @At("HEAD"), cancellable = true)
    private void tf$placeInPortal(Entity entity, int dimensionIdFrom, double x, double y, double z, float yaw, CallbackInfo ci) {
        if (!TFDimension.is(this.worldServerInstance.provider.dimensionId) && !TFDimension.is(dimensionIdFrom)) {
            return;
        }
        boolean inPortal = ((EntityPortalAccessor) entity).tf$isInPortal();
        if (TFDimension.isPortalTransfer(entity, inPortal)) {
            new TFTeleporter(this.worldServerInstance).placeInPortal(entity, x, y, z, yaw);
            ci.cancel();
        } else if (!inPortal) {
            tf$keepOutOfBlocks(entity);
            ci.cancel();
        }
    }

    /** A teleport that lands inside blocks is lifted onto the top of its column. */
    private void tf$keepOutOfBlocks(Entity entity) {
        WorldServer world = this.worldServerInstance;
        if (world.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()) {
            return;
        }
        int bx = MathHelper.floor_double(entity.posX);
        int bz = MathHelper.floor_double(entity.posZ);
        entity.setLocationAndAngles(entity.posX, world.getTopSolidOrLiquidBlock(bx, bz) + 0.1D, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        entity.fallDistance = 0.0F;
    }
}
