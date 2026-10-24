package twilightforest.item;

import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemCuirass;
import net.minecraft.ItemLeggings;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Naga armor. MITE port: the single 1.7.10 class (material, renderIndex, armorType) is split into part classes
 * extending ItemHelmet/ItemCuirass/ItemLeggings/ItemBoots (MITE_BLOCK_ITEM P-14); shared logic is static here.
 * Use {@link #create(int, int, Material)} with the 1.7.10 armor slot index.
 * Replaces Forge getArmorTexture via getTextureFilenamePrefix (assets/minecraft/textures/models/armor/twilightforest_naga_layer_N.png)
 * and the static {@link #getArmorTexture(int)} for a render mixin.
 */
public final class ItemTFNagaArmor {

    /** Whole-set protection from MITE_BALANCE 7.3. */
    public static final int SET_PROTECTION = 8;

    /** Wear texture prefix: assets/minecraft/textures/models/armor/twilightforest_naga_layer_N.png */
    public static final String TEXTURE_PREFIX = "twilightforest_naga";

    private ItemTFNagaArmor() {}

    /** Creates the armor part for a 1.7.10 armor slot index (0 helm, 1 plate, 2 legs, 3 boots). id is TFIds.item(..). */
    public static ItemArmor create(int id, int armorType, Material material) {
        switch (armorType) {
            case 1: return new Plate(id, material);
            case 2: return new Legs(id, material);
            default: throw new IllegalArgumentException("Invalid naga armor slot " + armorType);
        }
    }

    /** Original TF texture path for an armor slot (0 head, 1 chest, 2 legs, 3 boots); consumed by a render mixin if used. */
    public static String getArmorTexture(int slot) {
        return TwilightForestMod.ARMOR_DIR + (slot == 2 ? "naga_scale_2.png" : "naga_scale_1.png");
    }

    static EnumRarity rarity() {
        return EnumRarity.common;
    }

    /**
     * Naga chestplate: fire damage -50% (MITE_BALANCE 7.3). Call from the damage hook with the final damage amount;
     * returns the amount to heal back (0 if the wearer has no naga chestplate or the source is not fire).
     */
    public static float getFireDamageRefund(EntityLivingBase wearer, boolean fireDamage, float damageTaken) {
        if (!fireDamage || damageTaken <= 0.0F) {
            return 0.0F;
        }
        ItemStack[] worn = wearer.getWornItems();
        if (worn != null && worn.length > 1 && worn[1] != null && worn[1].getItem() == TFItems.plateNaga) {
            return damageTaken * 0.5F;
        }
        return 0.0F;
    }

    public static class Plate extends ItemCuirass {
        public Plate(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.nagaScale; }
    }

    public static class Legs extends ItemLeggings {
        public Legs(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.nagaScale; }
    }
}
