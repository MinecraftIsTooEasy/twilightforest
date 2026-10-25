package twilightforest;

import com.google.common.eventbus.Subscribe;
import net.xiaoyu233.fml.reload.event.AchievementRegistryEvent;
import net.xiaoyu233.fml.reload.event.CommandRegisterEvent;
import net.xiaoyu233.fml.reload.event.EntityRegisterEvent;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import net.xiaoyu233.fml.reload.event.PacketRegisterEvent;
import net.xiaoyu233.fml.reload.event.RecipeRegistryEvent;
import net.xiaoyu233.fml.reload.event.TileEntityRegisterEvent;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.compat.TFRegistry;
import twilightforest.block.TFBlocks;
import twilightforest.entity.TFCreatures;
import twilightforest.item.TFItems;
import twilightforest.item.TFRecipes;
import twilightforest.network.TFNetwork;

/** Common FML event listener; the order of the events is fixed by MITE (entities, then items, achievements, recipes). */
public class TFRegistryHandler {

    @Subscribe
    public void onEntities(EntityRegisterEvent e) {
        // Runs inside Item.<clinit>: classes, names and ids only, no Block/Item access
        TFRegistry.beginEntities(e);
        TFCreatures.registerCreatures(e);
    }

    @Subscribe
    public void onItems(ItemRegistryEvent e) {
        // Blocks, their ItemBlocks and the items all have to exist before BlockRegistryEvent, which is too late
        TFBlocks.registerBlocks(e);
        TFItems.registerItems(e);
        TFRecipes.registerPendingLateRecipes();
        TFBiomeBase.registerBiomes();
    }

    @Subscribe
    public void onAchievements(AchievementRegistryEvent e) {
        TFAchievementPage.registerAchievements();
    }

    @Subscribe
    public void onRecipes(RecipeRegistryEvent e) {
        TFRecipes.registerRecipes(e);
        TFRecipes.registerPendingLateRecipes();
    }

    @Subscribe
    public void onTileEntities(TileEntityRegisterEvent e) {
        TFRegistry.beginTileEntities(e);
        TwilightForestMod.registerTileEntities(e);
    }

    @Subscribe
    public void onPackets(PacketRegisterEvent e) {
        TFNetwork.register(e);
    }

    @Subscribe
    public void onCommands(CommandRegisterEvent e) {
        TFEvents.registerCommands(e);
    }
}
