package twilightforest.item;

import java.util.List;

import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.IconRegister;
import net.minecraft.ItemSword;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;
import twilightforest.TwilightForestMod;

/**
 * Knightly metal sword (TF 1.7.10 ItemTFKnightlySword).
 * TODO(mite-port): TF 2.3.8's "bonus damage vs armor" (onLeftClickEntity + commented-out getDamageVsEntity) was dead
 * code in 1.7.10 and is deliberately not implemented (MITE_BALANCE 7.1); damage = MITE class base + material bonus.
 */
public class ItemTFKnightlySword extends ItemSword {

    /** @param id the Item constructor argument, i.e. TFIds.item(..) (final item id - 256) */
    public ItemTFKnightlySword(int id, Material material) {
        super(id, material);
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    /** Return an item rarity from EnumRarity. This is automatically rare. */
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    /** Repair with knightmetal nuggets (ancient anvil, gate comes from the material, MITE_BALANCE 6.7). */
    @Override
    public Item getRepairItem() {
        return TFItems.knightMetalNugget;
    }

    /** Properly register icon source. */
    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
    }

    /** Allows items to add custom lines of information to the mouseover description. */
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extendedInfo, Slot slot) {
        super.addInformation(stack, player, info, extendedInfo, slot);
        info.add(StatCollector.translateToLocal(this.getUnlocalizedName() + ".tooltip"));
    }
}
