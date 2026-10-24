package twilightforest.mixin.dimension;

import net.minecraft.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityPortalAccessor {
    @Accessor("portal_destination_dimension_id")
    int tf$getPortalDestinationDimensionId();
}
