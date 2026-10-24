package twilightforest.client;

import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import twilightforest.item.ItemTFChainBlock;
import twilightforest.item.ItemTFCubeOfAnnihilation;
import twilightforest.item.ItemTFMoonwormQueen;
import twilightforest.item.ItemTFOreMagnet;
import twilightforest.mixin.client.ItemTFMoonwormQueenAccessor;

/** Replacement for Forge's Item.getIcon(stack, pass, player, usingItem, useRemaining); null means "vanilla icon". */
public final class TFItemIcons {
    private static final int MOONWORM_FIRING_TIME = 12;

    private TFItemIcons() {
    }

    public static Icon get(EntityPlayer player, ItemStack stack, int pass, ItemStack using, int useRemaining) {
        Item item = stack.getItem();
        if (item instanceof ItemTFChainBlock chain) {
            return chain.getIcon(stack, pass, player, using, useRemaining);
        } else if (item instanceof ItemTFCubeOfAnnihilation cube) {
            return cube.getIcon(stack, pass, player, using, useRemaining);
        } else if (item instanceof ItemTFOreMagnet magnet) {
            if (using != null && using.getItem() == item) {
                return magnet.getIconForUseTime(using.getMaxItemUseDuration() - useRemaining);
            }
        } else if (item instanceof ItemTFMoonwormQueen queen) {
            Icon[] icons = ((ItemTFMoonwormQueenAccessor) queen).tf$getIcons();
            if (icons != null && using != null && using.getItem() == item) {
                int useTime = using.getMaxItemUseDuration() - useRemaining;
                if (useTime >= MOONWORM_FIRING_TIME) {
                    return (useTime >> 1) % 2 == 0 ? icons[0] : icons[1];
                }
            }
        }
        return null;
    }
}
