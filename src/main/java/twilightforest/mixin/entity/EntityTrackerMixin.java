package twilightforest.mixin.entity;

import net.minecraft.Entity;
import net.minecraft.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.TFEntitySpawnRegistry;

/** MITE's tracker only knows a fixed instanceof chain; TF projectiles and hydra heads would never be tracked. */
@Mixin(EntityTracker.class)
public abstract class EntityTrackerMixin {
    @Shadow
    public abstract void addEntityToTracker(Entity entity, int range, int frequency, boolean sendVelocity);

    @Inject(method = "addEntityToTracker(Lnet/minecraft/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void tf$trackTFEntity(Entity entity, CallbackInfo ci) {
        TFEntitySpawnRegistry.Spec spec = TFEntitySpawnRegistry.forEntity(entity);
        if (spec != null) {
            this.addEntityToTracker(entity, spec.range(), spec.freq(), spec.sendVelocity());
            ci.cancel();
        }
    }
}
