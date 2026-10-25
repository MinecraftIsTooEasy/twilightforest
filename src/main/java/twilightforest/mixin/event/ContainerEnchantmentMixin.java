package twilightforest.mixin.event;

import java.util.Random;
import net.minecraft.ContainerEnchantment;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.compat.TFEnchantGuard;

/** Marks the enchanting level calculation for ItemHardestMetalMixin (see TFEnchantGuard). */
@Mixin(ContainerEnchantment.class)
public abstract class ContainerEnchantmentMixin {
    @Inject(method = "calcEnchantmentLevelsForSlot", at = @At("HEAD"))
    private void tf$enterGuard(Random random, int slot, int bookshelves, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TFEnchantGuard.enter();
    }

    @Inject(method = "calcEnchantmentLevelsForSlot", at = @At("RETURN"))
    private void tf$exitGuard(Random random, int slot, int bookshelves, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        TFEnchantGuard.exit();
    }
}
