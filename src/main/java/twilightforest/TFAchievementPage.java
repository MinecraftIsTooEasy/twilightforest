package twilightforest;

import net.minecraft.Achievement;
import net.minecraft.Block;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import twilightforest.block.TFBlocks;
import twilightforest.item.TFItems;

/**
 * MITE has no achievement pages, so the tree is merged into the one achievement screen, shifted to the right of the
 * vanilla tree. The fields stay null until registerAchievements() runs on AchievementRegistryEvent.
 */
public class TFAchievementPage {
    /** Vanilla and MITE occupy columns -5..13, so the Twilight tree starts well to the right of them. */
    public static final int COLUMN_OFFSET = 24;
    public static final int ROW_OFFSET = 0;

    public static Achievement twilightPortal;
    public static Achievement twilightArrival;
    public static Achievement twilightHunter;
    public static Achievement twilightMagicMapFocus;
    public static Achievement twilightHill1;
    public static Achievement twilightHill2;
    public static Achievement twilightHill3;
    public static Achievement twilightHedge;
    public static Achievement twilightMagicMap;
    public static Achievement twilightKillNaga;
    public static Achievement twilightProgressNaga;
    public static Achievement twilightKillLich;
    public static Achievement twilightProgressLich;
    public static Achievement twilightProgressLabyrinth;
    public static Achievement twilightKillHydra;
    public static Achievement twilightProgressHydra;
    public static Achievement twilightProgressTrophyPedestal;
    public static Achievement twilightProgressKnights;
    public static Achievement twilightProgressUrghast;
    public static Achievement twilightProgressYeti;
    public static Achievement twilightProgressGlacier;
    public static Achievement twilightProgressTroll;
    public static Achievement twilightProgressThorns;
    public static Achievement twilightProgressCastle;
    public static Achievement twilightNagaArmors;
    public static Achievement twilightLichScepters;
    public static Achievement twilightMazeMap;
    public static Achievement twilightOreMap;
    public static Achievement twilightHydraChop;
    public static Achievement twilightMazebreaker;
    public static Achievement twilightFierySet;
    public static Achievement twilightQuestRam;

    private TFAchievementPage() {}

    private static Achievement make(int id, String name, int column, int row, Object icon, Achievement parent, boolean special) {
        int c = column + COLUMN_OFFSET;
        int r = row + ROW_OFFSET;
        Achievement a;
        if (icon instanceof ItemStack) {
            a = new Achievement(id, name, c, r, (ItemStack) icon, parent);
        } else if (icon instanceof Block) {
            a = new Achievement(id, name, c, r, (Block) icon, parent);
        } else {
            a = new Achievement(id, name, c, r, (Item) icon, parent);
        }
        if (special) {
            a.setSpecial();
        }
        return a.registerAchievement();
    }

    public static void registerAchievements() {
        twilightPortal = make(TFIds.ACH_TWILIGHT_PORTAL, "twilightPortal", -2, 1, TFBlocks.portal, null, true);
        twilightArrival = make(TFIds.ACH_TWILIGHT_ARRIVAL, "twilightArrival", 0, 0, new ItemStack(TFBlocks.log, 1, 9), twilightPortal, false);
        twilightHunter = make(TFIds.ACH_TWILIGHT_HUNTER, "twilightHunter", 2, 2, TFItems.feather, twilightArrival, false);
        twilightMagicMapFocus = make(TFIds.ACH_TWILIGHT_MAGIC_MAP_FOCUS, "twilightMagicMapFocus", 2, 0, TFItems.magicMapFocus, twilightHunter, false);
        twilightHill1 = make(TFIds.ACH_TWILIGHT_HILL1, "twilightHill1", -2, -1, Block.oreIron, twilightArrival, false);
        twilightHill2 = make(TFIds.ACH_TWILIGHT_HILL2, "twilightHill2", -3, -2, Block.oreGold, twilightArrival, false);
        twilightHill3 = make(TFIds.ACH_TWILIGHT_HILL3, "twilightHill3", -1, -3, Block.oreDiamond, twilightArrival, false);
        twilightHedge = make(TFIds.ACH_TWILIGHT_HEDGE, "twilightHedge", 2, -3, TFBlocks.hedge, twilightArrival, false);
        twilightMagicMap = make(TFIds.ACH_TWILIGHT_MAGIC_MAP, "twilightMagicMap", 4, -1, TFItems.magicMap, twilightMagicMapFocus, false);
        twilightKillNaga = make(TFIds.ACH_TWILIGHT_KILL_NAGA, "twilightKillNaga", 4, 3, new ItemStack(TFItems.trophy, 1, 1), twilightHunter, false);
        twilightProgressNaga = make(TFIds.ACH_TWILIGHT_PROGRESS_NAGA, "twilightProgressNaga", 4, 5, TFItems.nagaScale, twilightKillNaga, false);
        twilightKillLich = make(TFIds.ACH_TWILIGHT_KILL_LICH, "twilightKillLich", 2, 5, new ItemStack(TFItems.trophy, 1, 2), twilightProgressNaga, false);
        twilightProgressLich = make(TFIds.ACH_TWILIGHT_PROGRESS_LICH, "twilightProgressLich", -1, 4, TFItems.scepterLifeDrain, twilightKillLich, false);
        twilightProgressLabyrinth = make(TFIds.ACH_TWILIGHT_PROGRESS_LABYRINTH, "twilightProgressLabyrinth", -4, 6, TFItems.meefStroganoff, twilightProgressLich, false);
        twilightKillHydra = make(TFIds.ACH_TWILIGHT_KILL_HYDRA, "twilightKillHydra", -6, 4, new ItemStack(TFItems.trophy, 1, 0), twilightProgressLabyrinth, false);
        twilightProgressHydra = make(TFIds.ACH_TWILIGHT_PROGRESS_HYDRA, "twilightProgressHydra", -8, 4, TFItems.fieryBlood, twilightKillHydra, false);
        twilightProgressTrophyPedestal = make(TFIds.ACH_TWILIGHT_PROGRESS_TROPHY_PEDESTAL, "twilightProgressTrophyPedestal", -5, 2, TFBlocks.trophyPedestal, twilightProgressHydra, false);
        twilightProgressKnights = make(TFIds.ACH_TWILIGHT_PROGRESS_KNIGHTS, "twilightProgressKnights", -5, -1, TFItems.phantomHelm, twilightProgressTrophyPedestal, false);
        twilightProgressUrghast = make(TFIds.ACH_TWILIGHT_PROGRESS_URGHAST, "twilightProgressUrghast", -7, -1, new ItemStack(TFItems.trophy, 1, 3), twilightProgressKnights, false);
        twilightProgressYeti = make(TFIds.ACH_TWILIGHT_PROGRESS_YETI, "twilightProgressYeti", -7, -3, TFItems.alphaFur, twilightProgressUrghast, false);
        twilightProgressGlacier = make(TFIds.ACH_TWILIGHT_PROGRESS_GLACIER, "twilightProgressGlacier", -5, -5, new ItemStack(TFItems.trophy, 1, 4), twilightProgressYeti, false);
        twilightProgressTroll = make(TFIds.ACH_TWILIGHT_PROGRESS_TROLL, "twilightProgressTroll", -5, -7, TFItems.lampOfCinders, twilightProgressGlacier, false);
        twilightProgressThorns = make(TFIds.ACH_TWILIGHT_PROGRESS_THORNS, "twilightProgressThorns", -3, -7, TFBlocks.thorns, twilightProgressTroll, false);
        twilightProgressCastle = make(TFIds.ACH_TWILIGHT_PROGRESS_CASTLE, "twilightProgressCastle", -1, -7, Block.stoneBrick, twilightProgressThorns, false);
        twilightNagaArmors = make(TFIds.ACH_TWILIGHT_NAGA_ARMORS, "twilightNagaArmors", 5, 1, TFItems.plateNaga, twilightKillNaga, true);
        twilightLichScepters = make(TFIds.ACH_TWILIGHT_LICH_SCEPTERS, "twilightLichScepters", 3, 7, TFItems.scepterZombie, twilightKillLich, true);
        twilightMazeMap = make(TFIds.ACH_TWILIGHT_MAZE_MAP, "twilightMazeMap", 1, 7, TFItems.mazeMap, twilightProgressLich, false);
        twilightOreMap = make(TFIds.ACH_TWILIGHT_ORE_MAP, "twilightOreMap", 1, 9, TFItems.oreMap, twilightMazeMap, true);
        twilightHydraChop = make(TFIds.ACH_TWILIGHT_HYDRA_CHOP, "twilightHydraChop", -6, 6, TFItems.hydraChop, twilightKillHydra, false);
        twilightMazebreaker = make(TFIds.ACH_TWILIGHT_MAZEBREAKER, "twilightMazebreaker", -3, 4, TFItems.mazebreakerPick, twilightProgressLich, true);
        twilightFierySet = make(TFIds.ACH_TWILIGHT_FIERY_SET, "twilightFierySet", -8, 7, TFItems.fierySword, twilightProgressHydra, true);
        twilightQuestRam = make(TFIds.ACH_TWILIGHT_QUEST_RAM, "twilightQuestRam", 1, -5, TFItems.crumbleHorn, twilightArrival, true);
    }
}
