package twilightforest.mixin.event;

import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.inventory.ContainerTFUncrafting;

/** Container.slotClick is final in MITE, so the ContainerTFUncrafting override is done here. */
@Mixin(Container.class)
public abstract class ContainerMixin {
    @Unique
    private boolean tf$remapping;

    @Inject(method = "slotClick(IIIZLnet/minecraft/EntityPlayer;)Lnet/minecraft/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void tf$remapSlot(int slot, int button, int mode, boolean shift, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (this.tf$remapping || !((Object) this instanceof ContainerTFUncrafting uncrafting)) {
            return;
        }
        int remapped = uncrafting.tf$remapSlot(slot, button, mode, player);
        if (remapped != slot) {
            this.tf$remapping = true;
            try {
                cir.setReturnValue(((Container) (Object) this).slotClick(remapped, button, mode, shift, player));
            } finally {
                this.tf$remapping = false;
            }
        }
    }

    @Inject(method = "slotClick(IIIZLnet/minecraft/EntityPlayer;)Lnet/minecraft/ItemStack;", at = @At("RETURN"))
    private void tf$afterClick(int slot, int button, int mode, boolean shift, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ContainerTFUncrafting uncrafting) {
            uncrafting.tf$afterClick(slot, button, mode, player);
        }
    }
}
