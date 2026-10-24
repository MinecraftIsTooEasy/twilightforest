package twilightforest.mixin.event;

import java.util.List;
import net.minecraft.CraftingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.item.TFRecipes;

/** Custom IRecipe objects (map cloning, scepter recharge) cannot go through the recipe registry event. */
@Mixin(CraftingManager.class)
public abstract class CraftingManagerMixin {
    @Shadow
    private List recipes;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tf$lateRecipes(CallbackInfo ci) {
        TFRecipes.registerLateRecipes(this.recipes);
    }
}
