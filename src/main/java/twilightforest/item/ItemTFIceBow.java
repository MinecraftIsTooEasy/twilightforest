package twilightforest.item;

import net.minecraft.EntityArrow;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemArrow;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityIceArrow;

public class ItemTFIceBow extends ItemTFBowBase {

    public ItemTFIceBow(int id) {
        super(id);
        this.setTextureName(TwilightForestMod.ID + ":icebow");
        this.setMaxDamage(64); // MITE_BALANCE 7.4
        // TODO(mite-port): 1.7.10 getIsRepairable (repair with ice blocks) has no MITE hook; ItemBow(wood) is not anvil-repairable
    }

    /**
     * Get the arrow for this specific bow
     */
    @Override
    protected EntityArrow getArrow(World world, EntityPlayer player, float velocity, ItemArrow arrow, boolean launcherEnchanted) {
        // TODO(mite-port): assumes u062 EntityIceArrow(World, EntityLivingBase, float, ItemArrow, boolean)
        return new EntityIceArrow(world, player, velocity, arrow, launcherEnchanted);
    }
}
