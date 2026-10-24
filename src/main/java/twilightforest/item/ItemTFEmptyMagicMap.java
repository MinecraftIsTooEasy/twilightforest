package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemMap;
import net.minecraft.ItemMapBase;
import net.minecraft.ItemStack;
import net.minecraft.MapData;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFMagicMapData;

public class ItemTFEmptyMagicMap extends ItemMapBase {

    protected ItemTFEmptyMagicMap(int id) {
        super(id, "twilightforest:emptyMagicMap");
        // no TF creative tab in MITE (PORTING_GUIDE §3)
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    /** Called whenever this item is equipped and the right mouse button is pressed. */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        if (player.onClient()) {
            return true;
        }

        World world = player.worldObj;
        if (world.peekUniqueDataId(ItemTFMagicMap.STR_ID) > ItemMap.highest_map_index) {
            return false;
        }

        ItemStack mapItem = new ItemStack(TFItems.magicMap, 1, world.getUniqueDataId(ItemTFMagicMap.STR_ID));
        String mapName = ItemTFMagicMap.STR_ID + "_" + mapItem.getItemSubtype();
        MapData mapData = new TFMagicMapData(mapName);
        world.setItemData(mapName, mapData);
        mapData.scale = 4;
        int step = 128 * (1 << mapData.scale);
        mapData.xCenter = (int) (Math.round(player.posX / step) * step);
        mapData.zCenter = (int) (Math.round(player.posZ / step) * step);
        mapData.dimension = (byte) world.provider.dimensionId;
        mapData.markDirty();

        // cheevo
        player.triggerAchievement(TFAchievementPage.twilightMagicMap);

        player.inventory.convertOneOfCurrentItem(mapItem);
        return true;
    }
}
