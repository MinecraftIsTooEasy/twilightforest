package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.EnumRarity;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class ItemTF extends Item {

    private boolean isRare = false;

    /** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
    protected ItemTF(int id) {
        this(id, Material.bone);
    }

    protected ItemTF(int id, Material material) {
        super(id, material, TwilightForestMod.ID + ":missing");
        // no TF creative tab in MITE (PORTING_GUIDE §3)
        this.setCreativeTab(CreativeTabs.tabMaterials);
    }

    /**
     * Return an item rarity from EnumRarity
     *
     * This is automatically uncommon
     */
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return isRare ? EnumRarity.rare : EnumRarity.uncommon;
    }

    /**
     * Set rarity during creation
     */
    public ItemTF makeRare() {
        this.isRare = true;
        return this;
    }

    /**
     * Properly register icon source
     */
    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
    }
}
