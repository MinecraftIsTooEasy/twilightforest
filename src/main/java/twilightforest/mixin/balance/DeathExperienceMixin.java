package twilightforest.mixin.balance;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.TFBalance;

/** MITE_BALANCE X11: experience of TF mobs; spawner mobs give a quarter (spawner.xpMult), mob.xpMult tunes the rest. */
@Mixin(EntityLivingBase.class)
public abstract class DeathExperienceMixin {
    @WrapOperation(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;getExperienceValue()I"))
    private int tf$scaleExperience(EntityLivingBase self, Operation<Integer> original) {
        int xp = original.call(self);
        boolean fromSpawner = self instanceof EntityLiving && ((EntityLivingAccessor) self).tf$cameFromSpawner();
        return TFBalance.scaleMobXp(self, xp, fromSpawner);
    }
}
