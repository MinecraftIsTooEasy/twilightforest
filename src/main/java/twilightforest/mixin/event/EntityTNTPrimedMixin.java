package twilightforest.mixin.event;

import net.minecraft.EnumEntityFX;
import net.minecraft.EntityTNTPrimed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.EntityTFRedcap;

/** Redcap TNT hurts creatures; it breaks blocks only when explosion.breakBlocks is on. */
@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMixin {
    @Inject(method = "explode()V", at = @At("HEAD"), cancellable = true)
    private void tf$redcapExplode(CallbackInfo ci) {
        EntityTNTPrimed self = (EntityTNTPrimed) (Object) this;
        if (self.getTntPlacedBy() instanceof EntityTFRedcap) {
            self.worldObj.createExplosion(self, self.posX, self.posY, self.posZ, EntityTFRedcap.tntSizeVsBlocks(),
                EntityTFRedcap.TNT_SIZE_VS_LIVING, false);
            self.entityFX(EnumEntityFX.frags);
            ci.cancel();
        }
    }
}
