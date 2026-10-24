package twilightforest.compat;

import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;

/** Melee hooks for TF weapons (Forge onLeftClickEntity / damage modification), dispatched by mixins. */
public interface ITFMeleeItem {
    /** @return true to cancel the attack */
    default boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
        return false;
    }

    default float modifyMeleeDamage(EntityPlayer player, Entity target, float rawDamage) {
        return rawDamage;
    }
}
