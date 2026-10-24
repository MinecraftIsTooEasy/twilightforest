package twilightforest.item;

import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemBoots;
import net.minecraft.ItemCuirass;
import net.minecraft.ItemHelmet;
import net.minecraft.ItemLeggings;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Arctic armor. MITE port: the single 1.7.10 class (material, renderIndex, armorType) is split into part classes
 * extending ItemHelmet/ItemCuirass/ItemLeggings/ItemBoots (MITE_BLOCK_ITEM P-14); shared logic is static here.
 * Use {@link #create(int, int, Material)} with the 1.7.10 armor slot index.
 * Replaces Forge getArmorTexture via getTextureFilenamePrefix (assets/minecraft/textures/models/armor/twilightforest_arctic_layer_N.png)
 * and the static {@link #getArmorTexture(int)} for a render mixin.
 */
public final class ItemTFArcticArmor {

    /** Whole-set protection from MITE_BALANCE 7.3. */
    public static final int SET_PROTECTION = 6;

    /** Wear texture prefix: assets/minecraft/textures/models/armor/twilightforest_arctic_layer_N.png */
    public static final String TEXTURE_PREFIX = "twilightforest_arctic";

    private ItemTFArcticArmor() {}

    /** True for any of the four arctic armor part classes. */
    public static boolean isArcticArmor(Item item) {
        return item instanceof Helm || item instanceof Plate || item instanceof Legs || item instanceof Boots;
    }

    /** Creates the armor part for a 1.7.10 armor slot index (0 helm, 1 plate, 2 legs, 3 boots). id is TFIds.item(..). */
    public static ItemArmor create(int id, int armorType, Material material) {
        switch (armorType) {
            case 0: return new Helm(id, material);
            case 1: return new Plate(id, material);
            case 2: return new Legs(id, material);
            case 3: return new Boots(id, material);
            default: throw new IllegalArgumentException("Invalid arctic armor slot " + armorType);
        }
    }

    /** Original TF texture path for an armor slot (0 head, 1 chest, 2 legs, 3 boots); consumed by a render mixin if used. */
    public static String getArmorTexture(int slot) {
        return TwilightForestMod.ARMOR_DIR + (slot == 2 ? "arcticarmor_2.png" : "arcticarmor_1.png");
    }

    static EnumRarity rarity() {
        return EnumRarity.uncommon;
    }

    /** Model hook for the render mixin (replaces Forge getArmorModel); Object so common code never names ModelBiped. */
    public static Object getArmorModel(int armorSlot) {
        return TwilightForestMod.proxy.getArcticArmorModel(armorSlot);
    }

    public static class Helm extends ItemHelmet {
        public Helm(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.arcticFur; }
    }

    public static class Plate extends ItemCuirass {
        public Plate(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.arcticFur; }
    }

    public static class Legs extends ItemLeggings {
        public Legs(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.arcticFur; }
    }

    public static class Boots extends ItemBoots {
        public Boots(int id, Material material) {
            super(id, material, false);
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        /** Replaces getIsRepairable. */
        @Override public Item getRepairItem() { return TFItems.arcticFur; }
    }
}
