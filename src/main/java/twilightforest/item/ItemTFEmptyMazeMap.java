package twilightforest.item;

import net.minecraft.ChunkCoordinates;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemMap;
import net.minecraft.ItemMapBase;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TFMazeMapData;
import twilightforest.world.WorldProviderTwilightForest;

public class ItemTFEmptyMazeMap extends ItemMapBase {

    boolean mapOres;

    protected ItemTFEmptyMazeMap(int id, boolean mapOres) {
        super(id, mapOres ? "twilightforest:emptyOreMap" : "twilightforest:emptyMazeMap");
        // no TF creative tab in MITE (PORTING_GUIDE §3)
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.mapOres = mapOres;
    }

    /** Called whenever this item is equipped and the right mouse button is pressed. */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        if (player.onClient()) {
            return true;
        }

        World world = player.worldObj;
        if (world.peekUniqueDataId(ItemTFMazeMap.STR_ID) > ItemMap.highest_map_index) {
            return false;
        }

        ItemStack mapItem = new ItemStack(mapOres ? TFItems.oreMap : TFItems.mazeMap, 1, world.getUniqueDataId(ItemTFMazeMap.STR_ID));
        String mapName = ItemTFMazeMap.STR_ID + "_" + mapItem.getItemSubtype();
        TFMazeMapData mapData = new TFMazeMapData(mapName);
        world.setItemData(mapName, mapData);
        mapData.scale = 0;
        int step = 128 * (1 << mapData.scale);
        int chunkX = MathHelper.floor_double(player.posX) >> 4;
        int chunkZ = MathHelper.floor_double(player.posZ) >> 4;
        // need to fix center for feature offset
        if (world.provider instanceof WorldProviderTwilightForest && TFFeature.getFeatureForRegion(chunkX, chunkZ, world) == TFFeature.labyrinth) {
            ChunkCoordinates mc = TFFeature.getNearestCenterXYZ(chunkX, chunkZ, world);
            mapData.xCenter = mc.posX;
            mapData.zCenter = mc.posZ;
            mapData.yCenter = MathHelper.floor_double(player.posY);
        } else {
            mapData.xCenter = (int) (Math.round(player.posX / step) * step) + 10; // mazes are offset slightly
            mapData.zCenter = (int) (Math.round(player.posZ / step) * step) + 10; // mazes are offset slightly
            mapData.yCenter = MathHelper.floor_double(player.posY);
        }
        mapData.dimension = (byte) world.provider.dimensionId;
        mapData.markDirty();

        // cheevos
        if (mapItem.getItem() == TFItems.mazeMap) {
            player.triggerAchievement(TFAchievementPage.twilightMazeMap);
        }
        if (mapItem.getItem() == TFItems.oreMap) {
            player.triggerAchievement(TFAchievementPage.twilightOreMap);
        }

        player.inventory.convertOneOfCurrentItem(mapItem);
        return true;
    }
}
