package twilightforest.mixin.client;

import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFItemIcons;

/** Item.getIcon(stack, pass, player, usingItem, useRemaining) for the dynamic TF item icons. */
@Mixin(EntityPlayer.class)
public abstract class EntityPlayerIconMixin {
    @Shadow protected ItemStack itemInUse;
    @Shadow protected int itemInUseCount;

    @Inject(method = "getItemIcon(Lnet/minecraft/ItemStack;I)Lnet/minecraft/Icon;", at = @At("HEAD"), cancellable = true)
    private void tf$dynamicIcon(ItemStack stack, int pass, CallbackInfoReturnable<Icon> cir) {
        Icon icon = TFItemIcons.get((EntityPlayer) (Object) this, stack, pass, this.itemInUse, this.itemInUseCount);
        if (icon != null) {
            cir.setReturnValue(icon);
        }
    }
}
