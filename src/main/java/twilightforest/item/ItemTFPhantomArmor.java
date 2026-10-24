package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.EnumRarity;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Phantom knight armor, helmet and chestplate only (TF 1.7.10 ItemTFPhantomArmor).
 * One class for all four slots (MITE splits armor into ItemHelmet/ItemCuirass/...), so getArmorType() and the
 * component count are derived from the armor type. Model and texture selection happens client side in
 * twilightforest.client.TFArmorRender (MITE has no getArmorModel/getArmorTexture hook).
 */
public class ItemTFPhantomArmor extends ItemArmor {

    /** ItemArmor's constructor already calls getArmorType()/getNumComponentsForDurability(), before armorType is assigned. */
    private static final ThreadLocal<Integer> PENDING_TYPE = new ThreadLocal<>();
    private static final String[] TYPE_NAMES = {"helmet", "chestplate", "leggings", "boots"};
    private static final int[] TYPE_COMPONENTS = {5, 8, 7, 4};

    private boolean constructed;

    /** @param id the Item constructor argument, i.e. TFIds.item(..) (final item id - 256) */
    public ItemTFPhantomArmor(int id, Material material, int renderIndex, int armorType) {
        super(id, material, stash(armorType), false);
        this.constructed = true;
        PENDING_TYPE.remove();
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    private static int stash(int armorType) {
        PENDING_TYPE.set(armorType);
        return armorType;
    }

    private int type() {
        if (this.constructed) {
            return this.armorType;
        }
        Integer pending = PENDING_TYPE.get();
        return pending == null ? 0 : pending;
    }

    @Override
    public String getArmorType() {
        return TYPE_NAMES[this.type()];
    }

    @Override
    public int getNumComponentsForDurability() {
        return TYPE_COMPONENTS[this.type()];
    }

    /** MITE_BALANCE 6.2/7.3: whole-set protection 8 for tf_knightmetal / tf_phantom (single piece = N * 8 / 24). */
    @Override
    public int getMaterialProtection() {
        return 8;
    }

    /** Return an item rarity from EnumRarity. */
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.epic;
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
}
