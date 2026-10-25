package twilightforest.mixin.event;

import net.minecraft.Item;
import net.minecraft.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.compat.TFEnchantGuard;

/**
 * Inside the enchanting level calculation an item without any metal reports copper instead of null, so hooks that read
 * getHardestMetalMaterial().durability (beyond-extreme's bookshelf cap) do not crash the server. Copper is far below the
 * vibranium checks of those hooks, so nothing else changes; outside the enchanting table the result stays null.
 */
@Mixin(Item.class)
public abstract class ItemHardestMetalMixin {
    @Inject(method = "getHardestMetalMaterial", at = @At("RETURN"), cancellable = true)
    private void tf$noNullWhileEnchanting(CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null && TFEnchantGuard.active()) {
            cir.setReturnValue(Material.copper);
        }
    }
}
