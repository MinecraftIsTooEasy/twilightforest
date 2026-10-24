package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.BiomeDecorator;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.dimension.TFDimension;

@Mixin(BiomeDecorator.class)
public abstract class BiomeDecoratorMixin {
    @Shadow protected World currentWorld;

    /** generateOres(): `if (currentWorld.isOverworld()) {overworld ores} else if (isUnderworld()) ... else if (!isTheEnd()) setErrorMessage(...)` */
    @ModifyExpressionValue(method = "generateOres", at = @At(value = "INVOKE", target = "Lnet/minecraft/World;isOverworld()Z", ordinal = 0))
    private boolean tf$oresLikeOverworld(boolean isOverworld) {
        return isOverworld || TFDimension.is(this.currentWorld);
    }
}
