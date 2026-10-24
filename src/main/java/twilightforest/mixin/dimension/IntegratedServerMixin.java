package twilightforest.mixin.dimension;

import net.minecraft.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import twilightforest.dimension.TFDimension;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    /** IntegratedServer overrides loadAllWorlds with its own copy of the `new WorldServer[4]`. */
    @ModifyConstant(method = "loadAllWorlds", constant = @Constant(intValue = 4))
    private int tf$worldCount(int vanilla) {
        return Math.max(vanilla, TFDimension.WORLD_COUNT);
    }
}
