package twilightforest.mixin.balance;

import net.minecraft.Item;
import net.minecraft.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.compat.TFModCompat;
import twilightforest.item.TFMaterials;

/**
 * Difficulty profile weapon factor (compat.profile) for the port's tools and weapons. It stands in for MITE-ITE's forging
 * grade, which ITE looks up by getMaterialForRepairs: ironwood (repaired as iron) and the iron loot tools can be forged, the
 * other TF materials cannot, so only items repaired with a TF material are scaled. ItemTool caches this value in its
 * constructor (damageVsEntity, read by the attack attribute and the tooltip), so the factor is fixed when the items are built.
 */
@Mixin(ItemTool.class)
public abstract class ItemToolDamageMixin {
    @Inject(method = "getCombinedDamageVsEntity", at = @At("RETURN"), cancellable = true)
    private void tf$profileWeaponDamage(CallbackInfoReturnable<Float> cir) {
        if (this.getClass().getName().startsWith("twilightforest.") && ((Item) (Object) this).getMaterialForRepairs() instanceof TFMaterials.TFMaterial) {
            double mult = TFModCompat.profile().weaponDamageMult;
            if (mult != 1.0D) {
                cir.setReturnValue((float) (cir.getReturnValueF() * mult));
            }
        }
    }
}
