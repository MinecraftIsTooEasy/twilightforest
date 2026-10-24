package twilightforest.mixin.event;

import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.SlotCrafting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFEvents;

/** ItemCraftedEvent equivalent; hooked before the crafting grid is consumed. */
@Mixin(value = SlotCrafting.class, priority = 1100)
public abstract class SlotCraftingMixin {
    @Shadow @Final private IInventory craftMatrix;

    @Inject(method = "onPickupFromSlot(Lnet/minecraft/EntityPlayer;Lnet/minecraft/ItemStack;)V", at = @At("HEAD"))
    private void tf$onCrafted(EntityPlayer player, ItemStack crafted, CallbackInfo ci) {
        TFEvents.onItemCrafted(player, crafted, this.craftMatrix);
    }
}
