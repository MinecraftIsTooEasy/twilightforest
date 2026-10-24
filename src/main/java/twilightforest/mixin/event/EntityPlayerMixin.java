package twilightforest.mixin.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;
import twilightforest.compat.ITFMeleeItem;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin {
    /** PlayerTickEvent END. Runs on both sides; TFEvents filters. */
    @Inject(method = "onUpdate()V", at = @At("RETURN"))
    private void tf$onTick(CallbackInfo ci) {
        TFEvents.onPlayerTick((EntityPlayer) (Object) this);
    }

    /** Sneaking must not dismount the player from a pinch beetle or yeti. */
    @ModifyExpressionValue(method = "updateRidden()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityPlayer;isSneaking()Z"))
    private boolean tf$noDismountFromUnfriendly(boolean sneaking) {
        return sneaking && !TFEvents.isRidingUnfriendly((EntityPlayer) (Object) this);
    }

    /** PlayerInteractEvent useBlock = DENY inside locked structures. */
    @Inject(method = "checkForBlockActivation(Lnet/minecraft/RaycastCollision;)Z", at = @At("HEAD"), cancellable = true)
    private void tf$protectBlocks(RaycastCollision rc, CallbackInfoReturnable<Boolean> cir) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        if (rc != null && rc.isBlock() && TFEvents.shouldDenyBlockUse(self, self.worldObj, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z)) {
            cir.setReturnValue(false);
        }
    }

    /** Item.onLeftClickEntity. */
    @Inject(method = "attackTargetEntityWithCurrentItem(Lnet/minecraft/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void tf$onLeftClickEntity(Entity target, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        ItemStack held = self.getHeldItemStack();
        if (held != null && held.getItem() instanceof ITFMeleeItem melee && melee.onLeftClickEntity(held, self, target)) {
            ci.cancel();
        }
    }

    /** Bonus damage of TF weapons (Item.getDamageVsEntity replacement). */
    @ModifyReturnValue(method = "calcRawMeleeDamageVs(Lnet/minecraft/Entity;ZZ)F", at = @At("RETURN"))
    private float tf$modifyMeleeDamage(float damage, Entity target, boolean critical, boolean suspendedInLiquid) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        ItemStack held = self.getHeldItemStack();
        if (held != null && held.getItem() instanceof ITFMeleeItem melee) {
            return melee.modifyMeleeDamage(self, target, damage);
        }
        return damage;
    }
}
