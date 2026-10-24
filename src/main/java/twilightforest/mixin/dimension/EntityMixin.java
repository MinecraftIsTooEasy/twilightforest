package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Entity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.dimension.TFDimension;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public int dimension;
    @Shadow protected int portal_destination_dimension_id;

    /** Entity.onEntityUpdate only performs portal travel when allow-nether is on; TF portals must not depend on it. */
    @WrapOperation(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllowNether()Z"))
    private boolean tf$allowForestPortals(MinecraftServer server, Operation<Boolean> original) {
        if (TFDimension.ignoreAllowNether && (TFDimension.is(this.portal_destination_dimension_id) || TFDimension.is(this.dimension))) {
            return true;
        }
        return original.call(server);
    }
}
