package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Entity;
import net.minecraft.MathHelper;
import net.minecraft.ServerConfigurationManager;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.dimension.TFDimension;

/**
 * PlayerChangedDimension / PlayerRespawn hooks (transferPlayerToDimension, respawnPlayer RETURN) belong to the event area
 * (twilightforest.mixin.event, TFEvents.onPlayerChangedDimension / onPlayerRespawn) and are not duplicated here.
 */
@Mixin(ServerConfigurationManager.class)
public abstract class ServerConfigurationManagerMixin {
    /**
     * transferEntityToWorld sends every destination that is not -1/0/-2 down the "End" branch and asks the destination
     * provider for getEntrancePortalLocation() (null -> NPE). TF wants to arrive at the SAME x/z (movement factor 1).
     */
    @WrapOperation(method = "transferEntityToWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/WorldServer;getEntrancePortalLocation()Lnet/minecraft/ChunkCoordinates;"))
    private ChunkCoordinates tf$arrivalLocation(WorldServer destination, Operation<ChunkCoordinates> original, @Local(argsOnly = true) Entity entity) {
        if (TFDimension.is(destination.provider.dimensionId)) {
            return new ChunkCoordinates(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
        }
        return original.call(destination);
    }
}
