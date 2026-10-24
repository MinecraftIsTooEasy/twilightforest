package twilightforest.mixin.balance;

import net.minecraft.EntityMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFBalance;

/** MITE_BALANCE 4.3: TF bosses and their minions are not frenzied by the mirrored blood moon (mirrorBloodMoonBosses). */
@Mixin(EntityMob.class)
public abstract class MobFrenzyMixin {
    @Inject(method = "isFrenzied", at = @At("HEAD"), cancellable = true)
    private void tf$bossNoFrenzy(CallbackInfoReturnable<Boolean> cir) {
        if (TFBalance.frenzyExempt((EntityMob) (Object) this)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
