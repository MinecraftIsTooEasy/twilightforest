package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.dimension.TFDimension;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin {
    /** MITE players wait 80 ticks in a portal (0 in creative); TF portals use TFDimension.portalDelayTicks. */
    @ModifyReturnValue(method = "getMaxInPortalTime", at = @At("RETURN"))
    private int tf$portalDelay(int original) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        int destination = ((EntityPortalAccessor) self).tf$getPortalDestinationDimensionId();
        if (TFDimension.is(destination) || TFDimension.is(self.worldObj)) {
            return Math.min(original, TFDimension.portalDelayTicks);
        }
        return original;
    }
}
