package twilightforest.mixin.entity;

import net.minecraft.Entity;
import net.minecraft.NetClientHandler;
import net.minecraft.Packet23VehicleSpawn;
import net.minecraft.SpatialScaler;
import net.minecraft.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.TFEntitySpawnRegistry;

/** Client half of the TF non-living entity spawn: builds the entity for types 100..127 and leaves the rest to vanilla. */
@Mixin(NetClientHandler.class)
public abstract class NetClientHandlerVehicleMixin {
    @Shadow
    private WorldClient worldClient;

    @Inject(method = "handleVehicleSpawn", at = @At("HEAD"), cancellable = true)
    private void tf$handleVehicleSpawn(Packet23VehicleSpawn packet, CallbackInfo ci) {
        TFEntitySpawnRegistry.Spec spec = TFEntitySpawnRegistry.forType(packet.type);
        if (spec == null) {
            return;
        }
        ci.cancel();

        double x;
        double y;
        double z;
        if (packet.position_set_using_unscaled_integers) {
            x = packet.unscaled_pos_x;
            y = packet.unscaled_pos_y;
            z = packet.unscaled_pos_z;
        } else {
            x = SpatialScaler.getPosX(packet.scaled_pos_x);
            y = SpatialScaler.getPosY(packet.scaled_pos_y);
            z = SpatialScaler.getPosZ(packet.scaled_pos_z);
        }

        Entity entity = spec.factory().create(this.worldClient, x, y, z, packet.throwerEntityId);
        entity.rotationYaw = SpatialScaler.getRotation(packet.scaled_yaw);
        entity.rotationPitch = SpatialScaler.getRotation(packet.scaled_pitch);

        Entity[] parts = entity.getParts();
        if (parts != null) {
            int offset = packet.entityId - entity.entityId;
            for (Entity part : parts) {
                part.entityId += offset;
            }
        }

        entity.entityId = packet.entityId;
        this.worldClient.addEntityToWorld(packet.entityId, entity);
        if (packet.throwerEntityId > 0) {
            entity.setVelocity(packet.approx_motion_x, packet.approx_motion_y, packet.approx_motion_z);
        }
    }
}
