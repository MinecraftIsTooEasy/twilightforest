package twilightforest.block;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.Block;
import net.minecraft.BlockDoubleSlab;
import net.minecraft.BlockFire;
import net.minecraft.BlockSlab;
import net.minecraft.ItemBlock;
import net.minecraft.ItemSlab;
import net.xiaoyu233.fml.api.block.IBlock;
import net.xiaoyu233.fml.api.item.IItem;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFRegistry;
import twilightforest.creativetab.TFCreativeTabs;
import twilightforest.item.ItemBlockTFAuroraSlab;
import twilightforest.item.ItemBlockTFHugeLilyPad;
import twilightforest.item.ItemBlockTFHugeWaterLily;
import twilightforest.item.ItemBlockTFDeadrock;
import twilightforest.item.ItemBlockTFMeta;
import twilightforest.item.ItemBlockTFPlant;
import twilightforest.item.TFMaterials;
import twilightforest.item.ItemBlockTFThorns;

/**
 * Static block registry. The blocks and their ItemBlocks are created inside the ItemRegistryEvent handler
 * ({@link #registerBlocks}); BlockRegistryEvent is too late for them (PORTING_GUIDE section 3).
 * Every carriable block is put on TFCreativeTabs.BLOCKS in registerMyBlock.
 */
public class TFBlocks {

    public static Block log;
    public static Block leaves;
    public static Block firefly;
    public static Block portal;
    public static Block mazestone;
    public static Block hedge;
    public static Block bossSpawner;
    public static Block fireflyJar;
    public static Block plant;
    public static Block cicada;
    public static Block root;
    public static Block uncraftingTable;
    public static Block fireJet;
    public static Block nagastone;
    public static Block sapling;
    public static Block magicLog;
    public static Block magicLogSpecial;
    public static Block magicLeaves;
    public static Block moonworm;
    public static Block towerWood;
    public static Block towerDevice;
    //public static Block towerAntenna;
    public static Block towerTranslucent;
    public static Block trophy;
    public static Block shield;
    public static Block trophyPedestal;
    public static Block auroraBlock;
    public static Block underBrick;
    public static Block thorns;
    public static Block burntThorns;
    public static Block thornRose;
    public static Block leaves3;
    public static Block deadrock;
    public static Block darkleaves;
    public static Block auroraPillar;
    public static BlockSlab auroraSlab;
    /** MITE has a separate BlockDoubleSlab class (not a BlockSlab), so this is a plain Block. */
    public static Block auroraDoubleSlab;
    public static Block trollSteinn;
    public static Block wispyCloud;
    public static Block fluffyCloud;
    public static Block giantCobble;
    public static Block giantLog;
    public static Block giantLeaves;
    public static Block giantObsidian;
    public static Block uberousSoil;
    public static Block hugeStalk;
    public static Block hugeGloomBlock;
    public static Block trollVidr;
    public static Block unripeTrollBer;
    public static Block trollBer;
    public static Block knightmetalStorage;
    public static Block hugeLilyPad;
    public static Block hugeWaterLily;
    public static Block slider;
    public static Block castleBlock;
    public static Block castleMagic;
    public static Block forceField;
    public static Block cinderFurnace;
    public static Block cinderFurnaceLit;
    public static Block cinderLog;
    public static Block castleDoor;
    public static Block castleDoorVanished;
    public static Block castleUnlock;

    /** Called from the ItemRegistryEvent handler (TFRegistryHandler). Blocks first, then their ItemBlocks. */
    public static void registerBlocks(ItemRegistryEvent e) {
        TFBlockIds.resolve();
        log = new BlockTFLog(TFIds.LOG);
        leaves = new BlockTFLeaves(TFIds.LEAVES);
        firefly = new BlockTFFirefly(TFIds.FIREFLY);
        cicada = new BlockTFCicada(TFIds.CICADA);
        portal = new BlockTFPortal(TFIds.PORTAL);
        mazestone = new BlockTFMazestone(TFIds.MAZESTONE);
        hedge = new BlockTFHedge(TFIds.HEDGE);
        bossSpawner = new BlockTFBossSpawner(TFIds.BOSS_SPAWNER);
        fireflyJar = new BlockTFFireflyJar(TFIds.FIREFLY_JAR);
        plant = new BlockTFPlant(TFIds.PLANT);
        root = new BlockTFRoots(TFIds.ROOT);
        uncraftingTable = new BlockTFUncraftingTable(TFIds.UNCRAFTING_TABLE);
        fireJet = new BlockTFFireJet(TFIds.FIRE_JET);
        nagastone = new BlockTFNagastone(TFIds.NAGASTONE);
        sapling = new BlockTFSapling(TFIds.SAPLING);
        magicLog = new BlockTFMagicLog(TFIds.MAGIC_LOG);
        magicLogSpecial = new BlockTFMagicLogSpecial(TFIds.MAGIC_LOG_SPECIAL);
        magicLeaves = new BlockTFMagicLeaves(TFIds.MAGIC_LEAVES);
        moonworm = new BlockTFMoonworm(TFIds.MOONWORM);
        towerWood = new BlockTFTowerWood(TFIds.TOWER_WOOD);
        towerDevice = new BlockTFTowerDevice(TFIds.TOWER_DEVICE);
        towerTranslucent = new BlockTFTowerTranslucent(TFIds.TOWER_TRANSLUCENT);
        trophy = new BlockTFTrophy(TFIds.TROPHY);
        shield = new BlockTFShield(TFIds.SHIELD);
        trophyPedestal = new BlockTFTrophyPedestal(TFIds.TROPHY_PEDESTAL);
        auroraBlock = new BlockTFAuroraBrick(TFIds.AURORA_BLOCK);
        underBrick = new BlockTFUnderBrick(TFIds.UNDER_BRICK);
        thorns = new BlockTFThorns(TFIds.THORNS);
        burntThorns = new BlockTFBurntThorns(TFIds.BURNT_THORNS);
        thornRose = new BlockTFThornRose(TFIds.THORN_ROSE);
        leaves3 = new BlockTFLeaves3(TFIds.LEAVES3);
        deadrock = new BlockTFDeadrock(TFIds.DEADROCK);
        darkleaves = new BlockTFDarkLeaves(TFIds.DARKLEAVES);
        auroraPillar = new BlockTFAuroraPillar(TFIds.AURORA_PILLAR);
        // MITE: BlockDoubleSlab fetches its single slab at (double id + 1) and the single slab must exist first.
        auroraSlab = new BlockTFAuroraSlab(TFIds.AURORA_SLAB);
        auroraDoubleSlab = new BlockTFAuroraSlab.DoubleSlab(TFIds.AURORA_DOUBLE_SLAB, (BlockTFAuroraSlab) auroraSlab);
        trollSteinn = new BlockTFTrollSteinn(TFIds.TROLL_STEINN);
        wispyCloud = new BlockTFWispyCloud(TFIds.WISPY_CLOUD);
        fluffyCloud = new BlockTFFluffyCloud(TFIds.FLUFFY_CLOUD);
        giantCobble = new BlockTFGiantCobble(TFIds.GIANT_COBBLE);
        giantLog = new BlockTFGiantLog(TFIds.GIANT_LOG);
        giantLeaves = new BlockTFGiantLeaves(TFIds.GIANT_LEAVES);
        giantObsidian = new BlockTFGiantObsidian(TFIds.GIANT_OBSIDIAN);
        uberousSoil = new BlockTFUberousSoil(TFIds.UBEROUS_SOIL);
        hugeStalk = new BlockTFHugeStalk(TFIds.HUGE_STALK);
        hugeGloomBlock = new BlockTFHugeGloomBlock(TFIds.HUGE_GLOOM_BLOCK);
        trollVidr = new BlockTFTrollRoot(TFIds.TROLL_VIDR);
        unripeTrollBer = new BlockTFUnripeTorchCluster(TFIds.UNRIPE_TROLL_BER);
        trollBer = new BlockTFRipeTorchCluster(TFIds.TROLL_BER);
        knightmetalStorage = new BlockTFKnightmetalBlock(TFIds.KNIGHTMETAL_STORAGE, TFMaterials.KNIGHTMETAL);
        hugeLilyPad = new BlockTFHugeLilyPad(TFIds.HUGE_LILY_PAD);
        hugeWaterLily = new BlockTFHugeWaterLily(TFIds.HUGE_WATER_LILY);
        slider = new BlockTFSlider(TFIds.SLIDER);
        castleBlock = new BlockTFCastleBlock(TFIds.CASTLE_BLOCK);
        castleMagic = new BlockTFCastleMagic(TFIds.CASTLE_MAGIC);
        forceField = new BlockTFForceField(TFIds.FORCE_FIELD);
        cinderFurnace = new BlockTFCinderFurnace(TFIds.CINDER_FURNACE, false);
        cinderFurnaceLit = new BlockTFCinderFurnace(TFIds.CINDER_FURNACE_LIT, true);
        cinderLog = new BlockTFCinderLog(TFIds.CINDER_LOG);
        castleDoor = new BlockTFCastleDoor(TFIds.CASTLE_DOOR, false);
        castleDoorVanished = new BlockTFCastleDoor(TFIds.CASTLE_DOOR_VANISHED, true);
        castleUnlock = new BlockTFCastleUnlock(TFIds.CASTLE_UNLOCK);

        // register blocks with their ItemBlocks (name = the 1.7.10 setBlockName, kept for the lang keys)
        registerMyBlock(log, "TFLog");
        registerMyBlock(root, "TFRoots");
        registerMyBlock(leaves, "TFLeaves");
        registerMyBlock(firefly, ItemBlock.class, "TFFirefly");
        registerMyBlock(cicada, ItemBlock.class, "TFCicada");
        registerMyBlock(portal, ItemBlock.class, "TFPortal");
        registerMyBlock(mazestone, "TFMazestone");
        registerMyBlock(hedge, "TFHedge");
        registerMyBlock(bossSpawner, "TFBossSpawner");
        registerMyBlock(fireflyJar, ItemBlock.class, "TFFireflyJar");
        registerMyBlock(plant, ItemBlockTFPlant.class, "TFPlant");
        registerMyBlock(uncraftingTable, ItemBlock.class, "TFUncraftingTable");
        registerMyBlock(fireJet, "TFFireJet");
        registerMyBlock(nagastone, "TFNagastone");
        registerMyBlock(sapling, "TFSapling");
        registerMyBlock(moonworm, ItemBlock.class, "TFMoonworm");
        registerMyBlock(magicLog, "TFMagicLog");
        registerMyBlock(magicLeaves, "TFMagicLeaves");
        registerMyBlock(magicLogSpecial, "TFMagicLogSpecial");
        registerMyBlock(towerWood, "TFTowerStone");
        registerMyBlock(towerDevice, "TFTowerDevice");
        registerMyBlock(towerTranslucent, "TFTowerTranslucent");
        registerMyBlock(trophy, "TFTrophy");
        registerMyBlock(shield, "TFShield");
        registerMyBlock(trophyPedestal, "TFTrophyPedestal");
        registerMyBlock(auroraBlock, ItemBlock.class, "TFAuroraBrick");
        registerMyBlock(underBrick, "TFUnderBrick");
        // MITE ItemMultiTextureTile has no second Block argument: ItemBlockTFThorns(Block, String[] names)
        registerMyBlock(thorns, ItemBlockTFThorns.class, "TFThorns", (Object) ((BlockTFThorns) thorns).getNames());
        registerMyBlock(burntThorns, ItemBlock.class, "TFBurntThorns");
        registerMyBlock(thornRose, ItemBlock.class, "TFThornRose");
        registerMyBlock(leaves3, "TFLeaves3");
        registerMyBlock(deadrock, ItemBlockTFDeadrock.class, "TFDeadrock", (Object) BlockTFDeadrock.names);
        registerMyBlock(darkleaves, ItemBlock.class, "DarkLeaves");
        registerMyBlock(auroraPillar, ItemBlock.class, "AuroraPillar");
        registerMyBlock(auroraSlab, ItemBlockTFAuroraSlab.class, "AuroraSlab", auroraDoubleSlab, Boolean.FALSE);
        registerAuroraDoubleSlab();
        registerMyBlock(trollSteinn, ItemBlock.class, "TrollSteinn");
        registerMyBlock(wispyCloud, ItemBlock.class, "WispyCloud");
        registerMyBlock(fluffyCloud, ItemBlock.class, "FluffyCloud");
        registerMyBlock(giantCobble, ItemBlock.class, "GiantCobble");
        registerMyBlock(giantLog, ItemBlock.class, "GiantLog");
        registerMyBlock(giantLeaves, ItemBlock.class, "GiantLeaves");
        registerMyBlock(giantObsidian, ItemBlock.class, "GiantObsidian");
        registerMyBlock(uberousSoil, ItemBlock.class, "UberousSoil");
        registerMyBlock(hugeStalk, ItemBlock.class, "HugeStalk");
        registerMyBlock(hugeGloomBlock, ItemBlock.class, "HugeGloomBlock");
        registerMyBlock(trollVidr, ItemBlock.class, "TrollVidr");
        registerMyBlock(unripeTrollBer, ItemBlock.class, "UnripeTrollBer");
        registerMyBlock(trollBer, ItemBlock.class, "TrollBer");
        registerMyBlock(knightmetalStorage, ItemBlock.class, "KnightmetalBlock");
        registerMyBlock(hugeLilyPad, ItemBlockTFHugeLilyPad.class, "HugeLilyPad");
        registerMyBlock(hugeWaterLily, ItemBlockTFHugeWaterLily.class, "HugeWaterLily");
        registerMyBlock(slider, "Slider");
        registerMyBlock(castleBlock, "CastleBrick");
        registerMyBlock(castleMagic, "CastleMagic");
        registerMyBlock(forceField, "ForceField");
        registerMyBlock(cinderFurnace, ItemBlock.class, "CinderFurnaceIdle");
        registerMyBlock(cinderFurnaceLit, ItemBlock.class, "CinderFurnaceLit");
        registerMyBlock(cinderLog, "CinderLog");
        registerMyBlock(castleDoor, "CastleDoor");
        registerMyBlock(castleDoorVanished, "CastleDoorVanished");
        // 1.7.10 never registered castleUnlock; in MITE every constructed block needs an ItemBlock (P-06), so it is registered too
        registerMyBlock(castleUnlock, ItemBlock.class, "CastleUnlock");

        // texture names that differ from the registered name (1.7.10 setBlockTextureName)
        setTexture(leaves, "leaves_oak");
        setTexture(leaves3, "leaves_oak");
        setTexture(thornRose, TwilightForestMod.ID + ":thornRose");
        setTexture(hugeWaterLily, TwilightForestMod.ID + ":huge_waterlily");

        // fire info
        setFireInfo(log, 5, 5);
        setFireInfo(leaves, 30, 60);
        setFireInfo(leaves3, 30, 60);
    }

    private static void setTexture(Block block, String texture) {
        ((IBlock) block).setBlockTextureName(texture);
    }

    /** The double slab shares the single slab's ItemSlab class, but its own item id, so it cannot go through the reflection ctor. */
    private static void registerAuroraDoubleSlab() {
        String name = "AuroraDoubleSlab";
        auroraDoubleSlab.setUnlocalizedName(name);
        ((IBlock) auroraDoubleSlab).setNamespace(TFRegistry.NAMESPACE);
        ((IBlock) auroraDoubleSlab).setBlockTextureName(name);
        ItemSlab item = new ItemBlockTFAuroraSlab(auroraDoubleSlab, auroraSlab, (BlockDoubleSlab) auroraDoubleSlab, true);
        item.setUnlocalizedName(name);
        ((IItem) item).setNamespace(TFRegistry.NAMESPACE);
        item.setMaxStackSize(auroraDoubleSlab.getItemStackLimit());
        applyDifficulty(item, auroraDoubleSlab);
    }

    private static void registerMyBlock(Block block, Class<? extends ItemBlock> itemClass, String name, Object... extraCtorArgs) {
        // TFRegistry.registerBlock names the block texture after the registered name; keep the texture the block class chose itself
        String ownTexture = textureNameOf(block);
        ItemBlock item = TFRegistry.registerBlock(block, itemClass, name, extraCtorArgs);
        if (ownTexture != null) {
            setTexture(block, ownTexture);
        }
        applyDifficulty(item, block);
        // MITE validates: carriable blocks must have a creative tab, non-carriable ones must not
        if (block.canBeCarried()) {
            block.setCreativeTab(TFCreativeTabs.BLOCKS);
        }
    }

    /** Block.textureName is protected and has no getter. */
    private static String textureNameOf(Block block) {
        try {
            return (String) TEXTURE_NAME_FIELD.get(block);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    private static final Field TEXTURE_NAME_FIELD = findTextureNameField();

    private static Field findTextureNameField() {
        try {
            Field field = Block.class.getDeclaredField("textureName");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    /** The default ItemBlock of 1.7.10 TFBlocks.registerMyBlock(Block): ItemBlockTFMeta. */
    private static void registerMyBlock(Block block, String name) {
        registerMyBlock(block, ItemBlockTFMeta.class, name);
    }

    /** Every TF ItemBlock needs a crafting difficulty as component; unbreakable blocks (hardness -1) fall back to 100. */
    private static void applyDifficulty(ItemBlock item, Block block) {
        float difficulty = block.getCraftingDifficultyAsComponent(0);
        item.setCraftingDifficultyAsComponent(difficulty >= 0.0F ? difficulty : 100.0F);
    }

    /**
     * Blocks.fire.setFireInfo. BlockFire.setBurnRate(int,int,int) is private.
     * TODO(mite-port): call Block.fire.setBurnRate directly once the access widener line
     * "accessible method net/minecraft/BlockFire setBurnRate (III)V" is merged.
     */
    private static void setFireInfo(Block block, int encouragement, int flammability) {
        try {
            Method method = BlockFire.class.getDeclaredMethod("setBurnRate", int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(Block.fire, block.blockID, encouragement, flammability);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            TwilightForestMod.LOGGER.warn("Could not set fire info for block {}", block.blockID, ex);
        }
    }
}
