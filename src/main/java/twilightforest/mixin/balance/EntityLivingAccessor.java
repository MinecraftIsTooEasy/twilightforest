package twilightforest.mixin.balance;

import net.minecraft.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLiving.class)
public interface EntityLivingAccessor {
    @Accessor("came_from_spawner")
    boolean tf$cameFromSpawner();
}
