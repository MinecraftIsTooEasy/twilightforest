package twilightforest.item;

import java.util.List;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityEggInfo;
import net.minecraft.EntityList;
import net.minecraft.ItemMonsterPlacer;
import net.minecraft.ItemStack;
import net.minecraft.StatCollector;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFRegistry;

/**
 * TF spawn egg. TF creatures are registered through EntityRegisterEvent with egg colours (TFRegistry.registerCreature),
 * so MITE's EntityList.entityEggs already holds them and ItemMonsterPlacer provides colours, overlay icon, right-click
 * spawning and baby spawning. This subclass only restricts the creative list to TF creatures and uses the TF lang keys.
 */
public class ItemTFSpawnEgg extends ItemMonsterPlacer {

    /** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
    protected ItemTFSpawnEgg(int id) {
        super(id);
        this.setUnlocalizedName("tfspawnegg");
    }

    @Override
    public String getItemDisplayName(ItemStack stack) {
        String prefix = ("" + StatCollector.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
        if (stack == null) {
            return prefix;
        }
        String entityname = EntityList.getStringFromID(stack.getItemSubtype());
        if (entityname != null) {
            // the registered name already carries the namespace: "twilightforest:wild_boar"
            prefix = prefix + " " + StatCollector.translateToLocal("entity." + entityname + ".name");
        }
        return prefix;
    }

    /**
     * Spawns the creature specified by the egg's type in the location specified by the last three parameters.
     * Parameters: world, entityID, x, y, z.
     */
    public static Entity spawnCreature(World world, int entityId, double x, double y, double z) {
        return ItemMonsterPlacer.spawnCreature(world, entityId, x, y, z, false, null);
    }

    /**
     * returns a list of items with the same ID, but different meta (only TF creatures)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(int id, CreativeTabs tab, List list) {
        for (Object o : EntityList.entityEggs.values()) {
            EntityEggInfo info = (EntityEggInfo) o;
            Class c = EntityList.getClassFromID(info.spawnedID);
            if (c != null && TFRegistry.getTrackInfo(c) != null) {
                list.add(new ItemStack(id, 1, info.spawnedID));
            }
        }
    }
}
