package twilightforest.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Damage;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;
import twilightforest.compat.ITFNoIFrames;
import twilightforest.enchantment.TFEnchantment;

/** LivingAttackEvent and LivingHurtEvent equivalents. */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {
    /** Returning null is how MITE ignores an attack. */
    @Inject(method = "attackEntityFrom(Lnet/minecraft/Damage;)Lnet/minecraft/EntityDamageResult;", at = @At("HEAD"), cancellable = true)
    private void tf$onAttack(Damage damage, CallbackInfoReturnable<EntityDamageResult> cir) {
        if (damage.getSource().getImmediateEntity() instanceof ITFNoIFrames arrow && arrow.tf$isNoIFrames()) {
            ((EntityLivingBase) (Object) this).hurtResistantTime = 0;
        }
        if (TFEvents.shouldCancelAttack((EntityLivingBase) (Object) this, damage)) {
            cir.setReturnValue(null);
        }
    }

    /** The value after armor and defense modifiers is what 1.7.10 passed to LivingHurtEvent; 0 means no damage. */
    @WrapOperation(method = "attackEntityFromHelper(Lnet/minecraft/Damage;Lnet/minecraft/EntityDamageResult;)Lnet/minecraft/EntityDamageResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/Damage;applyTargetDefenseModifiers(Lnet/minecraft/EntityLivingBase;Lnet/minecraft/EntityDamageResult;)F"))
    private float tf$onHurt(Damage damage, EntityLivingBase target, EntityDamageResult result, Operation<Float> original) {
        float amount = original.call(damage, target, result);
        return TFEnchantment.reduceFireDamage(target, damage, TFEvents.onLivingHurt(target, damage, amount));
    }
}
