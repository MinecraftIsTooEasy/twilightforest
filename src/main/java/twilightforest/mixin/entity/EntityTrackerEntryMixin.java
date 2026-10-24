package twilightforest.mixin.entity;

import java.util.List;
import net.minecraft.Entity;
import net.minecraft.EntityTrackerEntry;
import net.minecraft.Packet;
import net.minecraft.Packet28EntityVelocity;
import net.minecraft.Packet34EntityTeleport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.TFEntitySpawnRegistry;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow
    public Entity myEntity;
    @Shadow
    public int ticks;
    @Shadow
    public int updateFrequency;

    @Shadow
    public abstract void sendPacketToAllTrackingPlayers(Packet packet);

    /** The vanilla chain throws IllegalArgumentException for entities it does not know. */
    @Inject(method = "getPacketForThisEntity", at = @At("HEAD"), cancellable = true)
    private void tf$spawnPacket(CallbackInfoReturnable<Packet> cir) {
        TFEntitySpawnRegistry.Spec spec = TFEntitySpawnRegistry.forEntity(this.myEntity);
        if (spec != null) {
            cir.setReturnValue(TFEntitySpawnRegistry.createSpawnPacket(this.myEntity, spec));
        }
    }

    /** sendLocationToAllClients never sends location packets for EntityArrow subclasses (seeker arrows home in on targets). */
    @Inject(method = "sendLocationToAllClients", at = @At("TAIL"))
    private void tf$syncArrowPosition(List players, CallbackInfo ci) {
        Entity entity = this.myEntity;
        if (this.ticks % Math.max(1, this.updateFrequency) != 0 || entity.isDead) {
            return;
        }
        TFEntitySpawnRegistry.Spec spec = TFEntitySpawnRegistry.forEntity(entity);
        if (spec != null && spec.forcePositionSync() && (entity.motionX != 0.0 || entity.motionY != 0.0 || entity.motionZ != 0.0)) {
            this.sendPacketToAllTrackingPlayers(new Packet34EntityTeleport(entity));
            this.sendPacketToAllTrackingPlayers(new Packet28EntityVelocity((EntityTrackerEntry) (Object) this));
        }
    }
}
