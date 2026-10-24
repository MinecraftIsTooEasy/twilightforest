package twilightforest.item;

import net.minecraft.EnumQuality;
import net.minecraft.MapColor;
import net.minecraft.Material;
import net.xiaoyu233.fml.api.item.CustomMaterial;
import twilightforest.TFConfig;

/**
 * Replacement for the EnumHelper tool/armor materials of TFItems: one MITE {@link Material} per TF "metal"
 * (numbers from MITE_BALANCE section 6.2). {@code Material.durability} must be set before any item/block that uses the
 * material is constructed, so everything is set inside the constructor; this class is only loaded from the item registration.
 */
public final class TFMaterials {
    private TFMaterials() {}

    /** A TF material (ironwood.gateProxy=false: ironwood is gated as itself, i.e. ancient-metal tier by durability): FML {@link CustomMaterial} (tool efficiency) plus the damage bonus, armor protection and workbench gate. */
    public static final class TFMaterial extends CustomMaterial {
        private final float damageBonus;
        private final int protection;
        private final Material gate;

        TFMaterial(String name, float durability, int enchantability, EnumQuality maxQuality, int minHarvestLevel, boolean metal,
                   boolean harmedByAcid, float toolEfficiency, float damageBonus, int protection, Material gate) {
            super(name, toolEfficiency);
            this.setMapColor(MapColor.ironColor);
            this.setDurability(durability);
            this.setEnchantability(enchantability);
            this.setMaxQuality(maxQuality);
            // items.toolLevelCap (H6): TF tools never exceed this harvest level
            this.setMinHarvestLevel(minHarvestLevel <= 0 ? minHarvestLevel : Math.min(minHarvestLevel, TFConfig.itemsToolLevelCap));
            if (metal) {
                this.setRequiresTool();
                this.setMetal(harmedByAcid);
            }
            this.damageBonus = damageBonus;
            this.protection = protection;
            this.gate = gate;
        }

        /** Damage added on top of the tool class base damage (MITE reads it through ItemTool). */
        @Override
        public float getDamageVsEntity() {
            return this.damageBonus;
        }

        /** Whole-set armor protection (MITE: ItemArmor.getMaterialProtection returns this per set, pieces scale by N/24). */
        public int getProtection() {
            return this.protection;
        }

        /** Material the workbench/anvil gate is checked against (getHardestMetalMaterial / getMaterialForRepairs). */
        public Material getGate() {
            return this.gate == null ? this : this.gate;
        }
    }

    //                                                          name             D      ench quality                 harv metal acid   eff   dmg  P  gate
    public static final TFMaterial IRONWOOD = new TFMaterial("tf_ironwood",    12.0F, 30, EnumQuality.excellent, 3, true,  true,  2.0F, 4.0F, 8,
        TFConfig.ironwoodGateProxy ? Material.iron : null);
    public static final TFMaterial STEELEAF = new TFMaterial("tf_steeleaf",     5.0F, 30, EnumQuality.fine,      3, true,  true,  2.4F, 4.0F, 8, null);
    public static final TFMaterial KNIGHTMETAL = new TFMaterial("tf_knightmetal", 14.0F, 20, EnumQuality.excellent, 3, true,  true,  2.0F, 4.0F, 8, null);
    public static final TFMaterial FIERY = new TFMaterial("tf_fiery",          16.0F, 25, EnumQuality.excellent, 3, true,  false, 2.2F, 4.0F, 8, null);
    /** Phantom helm + plate (loot only), repaired with knightmetal. */
    public static final TFMaterial PHANTOM = new TFMaterial("tf_phantom",      14.0F, 20, EnumQuality.average,   3, true,  true,  0.0F, 0.0F, 8, null);
    public static final TFMaterial NAGA = new TFMaterial("tf_naga",            10.0F, 25, EnumQuality.fine,      0, false, true,  0.0F, 0.0F, 8, null);
    public static final TFMaterial YETI = new TFMaterial("tf_yeti",             8.0F, 25, EnumQuality.fine,      0, false, true,  0.0F, 0.0F, 8, null);
    public static final TFMaterial ARCTIC = new TFMaterial("tf_arctic",         3.0F, 20, EnumQuality.fine,      0, false, true,  0.0F, 0.0F, 6, null);
    /** Giant pickaxe / sword; min harvest 3 minus 1 for non-metal = tool level 2. */
    public static final TFMaterial GIANTSTONE = new TFMaterial("tf_giantstone", 3.0F, 5,  EnumQuality.fine,      3, false, true,  1.5F, 2.0F, 0, null);
    public static final TFMaterial ICE = new TFMaterial("tf_ice",               0.5F, 5,  EnumQuality.average,   0, false, true,  1.0F, 3.0F, 0, null);
    /** One-shot sword: durability 0.002 gives max damage 1. */
    public static final TFMaterial GLASS = new TFMaterial("tf_glass",           0.002F, 5, EnumQuality.average,  0, false, true,  1.0F, 19.0F, 0, null);

    static {
        // Fiery items must survive lava (balance 6.2)
        FIERY.setHarmedByLava(false);
    }
}
