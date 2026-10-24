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

/**
 * Ironwood armor. MITE's ItemArmor constructor virtually calls getArmorType()/getNumComponentsForDurability()
 * before subclass state exists (MITE_BLOCK_ITEM P-14), so the single 1.7.10 class (renderIndex/armorType ctor
 * args) is split into four part classes that extend ItemHelmet/ItemCuirass/ItemLeggings/ItemBoots.
 * This class holds the shared logic and the part classes; use {@link #create(int, int, Material)}.
 */
public final class ItemTFIronwoodArmor {

    /** Whole-set protection for ironwood (MITE_BALANCE 6.2): 8, same as iron. */
    private static final int SET_PROTECTION = 8;

    /** Wear texture prefix: assets/minecraft/textures/models/armor/twilightforest_ironwood_layer_N.png */
    public static final String TEXTURE_PREFIX = "twilightforest_ironwood";

    private ItemTFIronwoodArmor() {}

    /**
     * Creates the armor part for a 1.7.10 armor slot index (0 helm, 1 plate, 2 legs, 3 boots).
     *
     * @param id item ctor id (TFIds.item(..))
     */
    public static ItemArmor create(int id, int armorType, Material material) {
        switch (armorType) {
            case 0: return new Helm(id, material);
            case 1: return new Plate(id, material);
            case 2: return new Legs(id, material);
            case 3: return new Boots(id, material);
            default: throw new IllegalArgumentException("Invalid ironwood armor slot " + armorType);
        }
    }

    /** Rarity shared by all parts (uncommon like 1.7.10; enchanted stacks stay rare). */
    static EnumRarity rarity(ItemStack stack) {
        return stack.isItemEnchanted() ? EnumRarity.rare : EnumRarity.uncommon;
    }

    public static class Helm extends ItemHelmet {
        public Helm(int id, Material material) {
            super(id, material, false);
            this.setTextureName("twilightforest:ironwoodHelm");
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(stack); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        @Override public Item getRepairItem() { return TFItems.ironwoodNugget; }
        @Override public Material getHardestMetalMaterial() { return Material.iron; }
        @Override public Material getMaterialForRepairs() { return Material.iron; }
    }

    public static class Plate extends ItemCuirass {
        public Plate(int id, Material material) {
            super(id, material, false);
            this.setTextureName("twilightforest:ironwoodPlate");
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(stack); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        @Override public Item getRepairItem() { return TFItems.ironwoodNugget; }
        @Override public Material getHardestMetalMaterial() { return Material.iron; }
        @Override public Material getMaterialForRepairs() { return Material.iron; }
    }

    public static class Legs extends ItemLeggings {
        public Legs(int id, Material material) {
            super(id, material, false);
            this.setTextureName("twilightforest:ironwoodLegs");
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(stack); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        @Override public Item getRepairItem() { return TFItems.ironwoodNugget; }
        @Override public Material getHardestMetalMaterial() { return Material.iron; }
        @Override public Material getMaterialForRepairs() { return Material.iron; }
    }

    public static class Boots extends ItemBoots {
        public Boots(int id, Material material) {
            super(id, material, false);
            this.setTextureName("twilightforest:ironwoodBoots");
        }

        @Override public EnumRarity getRarity(ItemStack stack) { return rarity(stack); }
        @Override public int getMaterialProtection() { return SET_PROTECTION; }
        @Override public String getTextureFilenamePrefix() { return TEXTURE_PREFIX; }
        @Override public Item getRepairItem() { return TFItems.ironwoodNugget; }
        @Override public Material getHardestMetalMaterial() { return Material.iron; }
        @Override public Material getMaterialForRepairs() { return Material.iron; }
    }
}
