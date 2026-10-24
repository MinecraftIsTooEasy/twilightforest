package twilightforest.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.EntityItem;
import net.minecraft.InventoryPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.TFEvents;

/** EntityItemPickupEvent equivalent. */
@Mixin(EntityItem.class)
public abstract class EntityItemPickupMixin {
    @WrapOperation(method = "onCollideWithPlayer(Lnet/minecraft/EntityPlayer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/ItemStack;)Z"))
    private boolean tf$onPickup(InventoryPlayer inventory, ItemStack stack, Operation<Boolean> original) {
        // the stack is emptied when it merges into the inventory, so read it first
        Item item = stack.getItem();
        int subtype = stack.getItemSubtype();
        boolean pickedUp = original.call(inventory, stack);
        if (pickedUp) {
            TFEvents.onItemPickedUp(inventory.player, item, subtype);
        }
        return pickedUp;
    }
}
