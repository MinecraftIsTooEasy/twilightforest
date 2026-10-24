package twilightforest.item;

import net.minecraft.EntityArrow;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemArrow;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntitySeekerArrow;

public class ItemTFSeekerBow extends ItemTFBowBase {

    public ItemTFSeekerBow(int id) {
        super(id);
        this.setTextureName(TwilightForestMod.ID + ":seekerbow");
        this.setMaxDamage(64); // MITE_BALANCE 7.4
    }

    /**
     * Get the arrow for this specific bow
     */
    @Override
    protected EntityArrow getArrow(World world, EntityPlayer player, float velocity, ItemArrow arrow, boolean launcherEnchanted) {
        // TODO(mite-port): assumes u062 EntitySeekerArrow(World, EntityLivingBase, float, ItemArrow, boolean)
        return new EntitySeekerArrow(world, player, velocity * 0.5F, arrow, launcherEnchanted);
    }

}
