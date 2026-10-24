package twilightforest.compat;

import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;

/**
 * Replacement for the Forge Item.onUsingTick hook, which MITE does not have. Dispatched from the end of every EntityPlayer.onUpdate (both sides,
 * see TFEvents.onPlayerTick) while the player has a stack of this item in use, so it does not depend on Item.onUpdate or on the held-slot flag.
 */
public interface ITFUsingTickItem {
    /**
     * @param count remaining use ticks, already decremented for this tick (1.7.10 passed {@code count + 1})
     */
    void onUsingTick(ItemStack stack, EntityPlayer player, int count);
}
