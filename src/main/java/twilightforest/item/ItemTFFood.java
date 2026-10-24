package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemFood;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * TF food. MITE food has satiation + nutrition and three nutrient flags (protein / essential fats / phytonutrients);
 * they must be marked honestly (MITE_BALANCE food section), see the constructor arguments.
 */
public class ItemTFFood extends ItemFood {

    protected Item looksLike;
    protected boolean isSoup;

    /** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
    public ItemTFFood(int id, int satiation, int nutrition, boolean protein, boolean fats, boolean phytonutrients) {
        this(id, satiation, nutrition, 0, protein, fats, phytonutrients);
    }

    public ItemTFFood(int id, int satiation, int nutrition, int sugar, boolean protein, boolean fats, boolean phytonutrients) {
        super(id, protein ? Material.meat : Material.mashed_potato, satiation, nutrition, sugar, protein, fats, phytonutrients,
            TwilightForestMod.ID + ":missing");
        // no TF creative tab in MITE; ItemFood already puts itself on tabFood
        this.setCreativeTab(CreativeTabs.tabFood);
    }

    public Item getLooksLike() {
        return looksLike;
    }

    /**
     * Set this food to look like the specified item
     */
    public ItemTFFood setLooksLike(Item itemLike) {
        this.looksLike = itemLike;
        return this;
    }

    /**
     * Gets an icon index based on an item's subtype
     */
    @Override
    public Icon getIconFromSubtype(int subtype) {
        if (this.looksLike != null) {
            return looksLike.getIconFromSubtype(0);
        }
        return super.getIconFromSubtype(subtype);
    }

    /**
     * Properly register icon source
     */
    @Override
    public void registerIcons(IconRegister register) {
        // only try to load if we don't have a lookalike
        if (this.looksLike == null) {
            this.itemIcon = register.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
        }
    }

    public boolean isSoup() {
        return isSoup;
    }

    public void setSoup(boolean isSoup) {
        this.isSoup = isSoup;
        this.setMaxStackSize(1);
    }

    /**
     * Give a bowl back if we are soup (1.7.10 onEaten returned the bowl stack).
     */
    @Override
    public Item getItemProducedOnItemUseFinish() {
        return isSoup ? Item.bowlEmpty : null;
    }
}
