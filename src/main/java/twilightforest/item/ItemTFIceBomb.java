package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.boss.EntityTFIceBomb;

/**
 * Ice bomb grenade. MITE_BALANCE: stack size 8 (was 16); damage / slowness live in EntityTFIceBomb.
 */
public class ItemTFIceBomb extends ItemTF {

    private Icon[] snowIcon = new Icon[4];

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.ICE_BOMB) */
    public ItemTFIceBomb(int id) {
        super(id, Material.snow);
        this.setMaxStackSize(8);
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    /**
     * Properly register icon source
     */
    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
        for (int i = 0; i < 4; i++) {
            this.snowIcon[i] = register.registerIcon(TwilightForestMod.ID + ":snow_" + i);
        }
    }

    public Icon getSnowIcon(int i) {
        return snowIcon[i];
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed (throws the bomb).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        if (player.onClient()) {
            player.bobItem();
        } else {
            if (!player.inCreativeMode()) {
                player.convertOneOfHeldItem(null);
            }

            World world = player.worldObj;
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntityInWorld(new EntityTFIceBomb(world, player));
        }

        return true;
    }
}
