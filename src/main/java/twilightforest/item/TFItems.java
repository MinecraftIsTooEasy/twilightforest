package twilightforest.item;

import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemFood;
import net.minecraft.ItemMeat;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.xiaoyu233.fml.api.item.IItem;
import net.xiaoyu233.fml.api.item.NuggetItem;
import net.xiaoyu233.fml.reload.event.ItemRegistryEvent;
import twilightforest.TFConfig;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFRegistry;
import twilightforest.creativetab.TFCreativeTabs;

/**
 * Static item registry. Items are created inside the ItemRegistryEvent handler ({@link #registerItems}), after the blocks.
 * Numbers follow MITE_BALANCE.md (materials: {@link TFMaterials}, food: section 10, difficulty as component: section 6.6).
 * Constructors take the item id first (PORTING_GUIDE section 3): {@code TFIds.item(final id)}.
 */
public class TFItems {

    // Armor materials (EnumHelper.addArmorMaterial in 1.7.10): MITE Material instances, see TFMaterials
    public static Material ARMOR_NAGA = TFMaterials.NAGA;
    public static Material ARMOR_IRONWOOD = TFMaterials.IRONWOOD;
    public static Material ARMOR_FIERY = TFMaterials.FIERY;
    /** 1.7.10 steeleaf armor used ARMOR_IRONWOOD; the balance spec gives steeleaf its own material. */
    public static Material ARMOR_STEELEAF = TFMaterials.STEELEAF;
    public static Material ARMOR_KNIGHTLY = TFMaterials.KNIGHTMETAL;
    public static Material ARMOR_PHANTOM = TFMaterials.PHANTOM;
    public static Material ARMOR_YETI = TFMaterials.YETI;
    public static Material ARMOR_ARCTIC = TFMaterials.ARCTIC;

    // Tool materials (EnumHelper.addToolMaterial in 1.7.10)
    public static Material TOOL_IRONWOOD = TFMaterials.IRONWOOD;
    public static Material TOOL_FIERY = TFMaterials.FIERY;
    public static Material TOOL_STEELEAF = TFMaterials.STEELEAF;
    public static Material TOOL_KNIGHTLY = TFMaterials.KNIGHTMETAL;
    public static Material TOOL_GIANT = TFMaterials.GIANTSTONE;
    public static Material TOOL_ICE = TFMaterials.ICE;
    public static Material TOOL_GLASS = TFMaterials.GLASS;

    public static Item nagaScale;
    public static Item plateNaga;
    public static Item legsNaga;
    public static Item scepterTwilight;
    public static Item scepterLifeDrain;
    public static Item scepterZombie;
    public static Item wandPacification;
    public static Item oreMeter;
    public static Item magicMap;
    public static Item mazeMap;
    public static Item oreMap;
    public static Item feather;
    public static Item magicMapFocus;
    public static Item mazeMapFocus;
    public static Item liveRoot;
    public static Item ironwoodRaw;
    public static Item ironwoodIngot;
    public static Item ironwoodHelm;
    public static Item ironwoodPlate;
    public static Item ironwoodLegs;
    public static Item ironwoodBoots;
    public static Item ironwoodSword;
    public static Item ironwoodShovel;
    public static Item ironwoodPick;
    public static Item ironwoodAxe;
    public static Item ironwoodHoe;
    public static Item torchberries;
    public static Item spawnEgg;
    public static Item venisonRaw;
    public static Item venisonCooked;
    public static Item hydraChop;
    public static Item fieryBlood;
    public static Item trophy;
    public static Item fieryIngot;
    public static Item fieryHelm;
    public static Item fieryPlate;
    public static Item fieryLegs;
    public static Item fieryBoots;
    public static Item fierySword;
    public static Item fieryPick;
    public static Item steeleafIngot;
    public static Item steeleafHelm;
    public static Item steeleafPlate;
    public static Item steeleafLegs;
    public static Item steeleafBoots;
    public static Item steeleafSword;
    public static Item steeleafShovel;
    public static Item steeleafPick;
    public static Item steeleafAxe;
    public static Item steeleafHoe;
    public static Item minotaurAxe;
    public static Item mazebreakerPick;
    public static Item transformPowder;
    public static Item meefRaw;
    public static Item meefSteak;
    public static Item meefStroganoff;
    public static Item mazeWafer;
    public static Item emptyMagicMap;
    public static Item emptyMazeMap;
    public static Item emptyOreMap;
    public static Item oreMagnet;
    public static Item crumbleHorn;
    public static Item peacockFan;
    public static Item moonwormQueen;
    public static Item charmOfLife1;
    public static Item charmOfLife2;
    public static Item charmOfKeeping1;
    public static Item charmOfKeeping2;
    public static Item charmOfKeeping3;
    public static Item towerKey;
    public static Item borerEssence;
    public static Item carminite;
    public static Item experiment115;
    public static Item armorShard;
    public static Item knightMetal;
    public static Item shardCluster;
    public static Item knightlyHelm;
    public static Item knightlyPlate;
    public static Item knightlyLegs;
    public static Item knightlyBoots;
    public static Item knightlySword;
    public static Item knightlyPick;
    public static Item knightlyAxe;
    public static Item phantomHelm;
    public static Item phantomPlate;
    public static Item lampOfCinders;
    public static Item fieryTears;
    public static Item alphaFur;
    public static Item yetiHelm;
    public static Item yetiPlate;
    public static Item yetiLegs;
    public static Item yetiBoots;
    public static Item iceBomb;
    public static Item arcticFur;
    public static Item arcticHelm;
    public static Item arcticPlate;
    public static Item arcticLegs;
    public static Item arcticBoots;
    public static Item magicBeans;
    public static Item giantPick;
    public static Item giantSword;
    public static Item tripleBow;
    public static Item seekerBow;
    public static Item iceBow;
    public static Item enderBow;
    public static Item iceSword;
    public static Item glassSword;
    public static Item knightmetalRing;
    public static Item chainBlock;
    public static Item cubeTalisman;
    public static Item cubeOfAnnihilation;
    // Anvil repair "nuggets" of the four TF metals (balance 6.7); not part of 1.7.10
    public static Item ironwoodNugget;
    public static Item steeleafNugget;
    public static Item knightMetalNugget;
    public static Item fieryNugget;

    /**
     * There is no Twilight Forest creative tab (MITE's tab array is fixed at 12 entries); every item and block is listed
     * in a vanilla tab. Kept only so code ported literally from 1.7.10 still compiles.
     */

    /** Called from the ItemRegistryEvent handler (TFRegistryHandler), after TFBlocks.registerBlocks. */
    public static void registerItems(ItemRegistryEvent e) {
        // The item ids are the fixed TFIds values (final id; the Item constructors add 256 themselves)
        // scales and furs are NuggetItem subclasses so that the anvil accepts them as repair material (balance 6.7)
        nagaScale = new TFNugget(id(TFIds.NAGA_SCALE), TFMaterials.NAGA, 100.0F, false);
        plateNaga = ItemTFNagaArmor.create(id(TFIds.PLATE_NAGA), 1, ARMOR_NAGA);
        legsNaga = ItemTFNagaArmor.create(id(TFIds.LEGS_NAGA), 2, ARMOR_NAGA);
        scepterTwilight = new ItemTFTwilightWand(id(TFIds.SCEPTER_TWILIGHT)).setMaxStackSize(1).setFull3D();
        scepterLifeDrain = new ItemTFScepterLifeDrain(id(TFIds.SCEPTER_LIFE_DRAIN)).setMaxStackSize(1).setFull3D();
        scepterZombie = new ItemTFZombieWand(id(TFIds.SCEPTER_ZOMBIE)).setMaxStackSize(1).setFull3D();
        //wandPacification = new ItemTF().setIconIndex(6).setUnlocalizedName("wandPacification").setMaxStackSize(1).setFull3D();
        oreMeter = new ItemTFOreMeter(id(TFIds.ORE_METER)).setMaxStackSize(1);
        magicMap = new ItemTFMagicMap(id(TFIds.MAGIC_MAP)).setMaxStackSize(1);
        mazeMap = new ItemTFMazeMap(id(TFIds.MAZE_MAP), false).setMaxStackSize(1);
        oreMap = new ItemTFMazeMap(id(TFIds.ORE_MAP), true).setMaxStackSize(1);
        feather = new ItemTF(id(TFIds.FEATHER));
        magicMapFocus = new ItemTF(id(TFIds.MAGIC_MAP_FOCUS));
        mazeMapFocus = new ItemTF(id(TFIds.MAZE_MAP_FOCUS));
        liveRoot = new ItemTF(id(TFIds.LIVE_ROOT));
        ironwoodRaw = new ItemTF(id(TFIds.IRONWOOD_RAW));
        ironwoodIngot = new ItemTF(id(TFIds.IRONWOOD_INGOT));
        ironwoodHelm = ItemTFIronwoodArmor.create(id(TFIds.IRONWOOD_HELM), 0, ARMOR_IRONWOOD);
        ironwoodPlate = ItemTFIronwoodArmor.create(id(TFIds.IRONWOOD_PLATE), 1, ARMOR_IRONWOOD);
        ironwoodLegs = ItemTFIronwoodArmor.create(id(TFIds.IRONWOOD_LEGS), 2, ARMOR_IRONWOOD);
        ironwoodBoots = ItemTFIronwoodArmor.create(id(TFIds.IRONWOOD_BOOTS), 3, ARMOR_IRONWOOD);
        ironwoodSword = new ItemTFIronwoodSword(id(TFIds.IRONWOOD_SWORD), TOOL_IRONWOOD);
        ironwoodShovel = new ItemTFIronwoodShovel(id(TFIds.IRONWOOD_SHOVEL), TOOL_IRONWOOD);
        ironwoodPick = new ItemTFIronwoodPick(id(TFIds.IRONWOOD_PICK), TOOL_IRONWOOD);
        ironwoodAxe = new ItemTFIronwoodAxe(id(TFIds.IRONWOOD_AXE), TOOL_IRONWOOD);
        ironwoodHoe = new ItemTFIronwoodHoe(id(TFIds.IRONWOOD_HOE), TOOL_IRONWOOD);
        torchberries = new ItemTF(id(TFIds.TORCHBERRIES));
        spawnEgg = new ItemTFSpawnEgg(id(TFIds.SPAWN_EGG));
        // Food (balance section 10): satiation / nutrition; cooked meat ids directly follow the raw ids (ItemMeat composting)
        venisonRaw = new ItemMeat(id(TFIds.VENISON_RAW), 4, 4, false, false, "venisonRaw");
        venisonCooked = new ItemMeat(id(TFIds.VENISON_COOKED), 9, 9, false, true, "venisonCooked");
        ItemFood.setCookingResult((ItemFood) venisonRaw, (ItemFood) venisonCooked, 3);
        // balance: 16 / 16, protein + essential fats, Regeneration I 10s
        hydraChop = new ItemTFHydraChops(id(TFIds.HYDRA_CHOP), hydraChopValue(0), hydraChopValue(1), true, true, false);
        fieryBlood = new ItemTF(id(TFIds.FIERY_BLOOD)).makeRare();
        trophy = new ItemTFTrophy(id(TFIds.TROPHY_ITEM));
        fieryIngot = new ItemTF(id(TFIds.FIERY_INGOT)).makeRare();
        fieryHelm = new ItemTFFieryArmor.Helm(id(TFIds.FIERY_HELM), ARMOR_FIERY);
        fieryPlate = new ItemTFFieryArmor.Plate(id(TFIds.FIERY_PLATE), ARMOR_FIERY);
        fieryLegs = new ItemTFFieryArmor.Legs(id(TFIds.FIERY_LEGS), ARMOR_FIERY);
        fieryBoots = new ItemTFFieryArmor.Boots(id(TFIds.FIERY_BOOTS), ARMOR_FIERY);
        fierySword = new ItemTFFierySword(id(TFIds.FIERY_SWORD), TOOL_FIERY);
        fieryPick = new ItemTFFieryPick(id(TFIds.FIERY_PICK), TOOL_FIERY);
        steeleafIngot = new ItemTF(id(TFIds.STEELEAF_INGOT));
        steeleafHelm = new ItemTFSteeleafArmor.Helm(id(TFIds.STEELEAF_HELM), ARMOR_STEELEAF);
        steeleafPlate = new ItemTFSteeleafArmor.Plate(id(TFIds.STEELEAF_PLATE), ARMOR_STEELEAF);
        steeleafLegs = new ItemTFSteeleafArmor.Legs(id(TFIds.STEELEAF_LEGS), ARMOR_STEELEAF);
        steeleafBoots = new ItemTFSteeleafArmor.Boots(id(TFIds.STEELEAF_BOOTS), ARMOR_STEELEAF);
        steeleafSword = new ItemTFSteeleafSword(id(TFIds.STEELEAF_SWORD), TOOL_STEELEAF);
        steeleafShovel = new ItemTFSteeleafShovel(id(TFIds.STEELEAF_SHOVEL), TOOL_STEELEAF);
        steeleafPick = new ItemTFSteeleafPick(id(TFIds.STEELEAF_PICK), TOOL_STEELEAF);
        steeleafAxe = new ItemTFSteeleafAxe(id(TFIds.STEELEAF_AXE), TOOL_STEELEAF);
        steeleafHoe = new ItemTFSteeleafHoe(id(TFIds.STEELEAF_HOE), TOOL_STEELEAF);
        // Minotaur axe / mazebreaker use plain iron (balance 7.2), not a TF material
        minotaurAxe = new ItemTFMinotaurAxe(id(TFIds.MINOTAUR_AXE), Material.iron);
        mazebreakerPick = new ItemTFMazebreakerPick(id(TFIds.MAZEBREAKER_PICK), Material.iron);
        transformPowder = new ItemTFTransformPowder(id(TFIds.TRANSFORM_POWDER)).makeRare();
        meefRaw = new ItemMeat(id(TFIds.MEEF_RAW), 3, 3, false, false, "meefRaw");
        meefSteak = new ItemMeat(id(TFIds.MEEF_STEAK), 6, 6, false, true, "meefSteak");
        ItemFood.setCookingResult((ItemFood) meefRaw, (ItemFood) meefSteak, 2);
        // Stroganoff: soup (bowl comes back, does not stack): 12 / 12, protein + phytonutrients
        ItemTFFood stroganoff = new ItemTFFood(id(TFIds.MEEF_STROGANOFF), 12, 12, true, false, true);
        stroganoff.setSoup(true);
        meefStroganoff = stroganoff;
        mazeWafer = new ItemTFFood(id(TFIds.MAZE_WAFER), 4, 1, 400, false, false, false);
        emptyMagicMap = new ItemTFEmptyMagicMap(id(TFIds.EMPTY_MAGIC_MAP));
        emptyMazeMap = new ItemTFEmptyMazeMap(id(TFIds.EMPTY_MAZE_MAP), false);
        emptyOreMap = new ItemTFEmptyMazeMap(id(TFIds.EMPTY_ORE_MAP), true);
        oreMagnet = new ItemTFOreMagnet(id(TFIds.ORE_MAGNET));
        crumbleHorn = new ItemTFCrumbleHorn(id(TFIds.CRUMBLE_HORN));
        peacockFan = new ItemTFPeacockFan(id(TFIds.PEACOCK_FAN));
        moonwormQueen = new ItemTFMoonwormQueen(id(TFIds.MOONWORM_QUEEN));
        charmOfLife1 = new ItemTFCharm(id(TFIds.CHARM_OF_LIFE1));
        charmOfLife2 = new ItemTFCharm(id(TFIds.CHARM_OF_LIFE2));
        charmOfKeeping1 = new ItemTFCharm(id(TFIds.CHARM_OF_KEEPING1));
        charmOfKeeping2 = new ItemTFCharm(id(TFIds.CHARM_OF_KEEPING2));
        charmOfKeeping3 = new ItemTFCharm(id(TFIds.CHARM_OF_KEEPING3));
        towerKey = new ItemTFTowerKey(id(TFIds.TOWER_KEY));
        borerEssence = new ItemTF(id(TFIds.BORER_ESSENCE));
        carminite = new ItemTF(id(TFIds.CARMINITE)).makeRare();
        experiment115 = new ItemTFFood(id(TFIds.EXPERIMENT115), 4, 1, 300, false, false, false);
        armorShard = new ItemTF(id(TFIds.ARMOR_SHARD));
        knightMetal = new ItemTF(id(TFIds.KNIGHT_METAL));
        shardCluster = new ItemTF(id(TFIds.SHARD_CLUSTER));
        knightlyHelm = new ItemTFKnightlyArmor(id(TFIds.KNIGHTLY_HELM), ARMOR_KNIGHTLY, 0, 0);
        knightlyPlate = new ItemTFKnightlyArmor(id(TFIds.KNIGHTLY_PLATE), ARMOR_KNIGHTLY, 0, 1);
        knightlyLegs = new ItemTFKnightlyArmor(id(TFIds.KNIGHTLY_LEGS), ARMOR_KNIGHTLY, 0, 2);
        knightlyBoots = new ItemTFKnightlyArmor(id(TFIds.KNIGHTLY_BOOTS), ARMOR_KNIGHTLY, 0, 3);
        knightlySword = new ItemTFKnightlySword(id(TFIds.KNIGHTLY_SWORD), TOOL_KNIGHTLY);
        knightlyPick = new ItemTFKnightlyPick(id(TFIds.KNIGHTLY_PICK), TOOL_KNIGHTLY);
        knightlyAxe = new ItemTFKnightlyAxe(id(TFIds.KNIGHTLY_AXE), TOOL_KNIGHTLY);
        phantomHelm = new ItemTFPhantomArmor(id(TFIds.PHANTOM_HELM), ARMOR_PHANTOM, 0, 0);
        phantomPlate = new ItemTFPhantomArmor(id(TFIds.PHANTOM_PLATE), ARMOR_PHANTOM, 0, 1);
        lampOfCinders = new ItemTFLampOfCinders(id(TFIds.LAMP_OF_CINDERS));
        fieryTears = new ItemTF(id(TFIds.FIERY_TEARS)).makeRare();
        alphaFur = new TFNugget(id(TFIds.ALPHA_FUR), TFMaterials.YETI, 100.0F, true);
        yetiHelm = ItemTFYetiArmor.create(id(TFIds.YETI_HELM), 0, ARMOR_YETI);
        yetiPlate = ItemTFYetiArmor.create(id(TFIds.YETI_PLATE), 1, ARMOR_YETI);
        yetiLegs = ItemTFYetiArmor.create(id(TFIds.YETI_LEGS), 2, ARMOR_YETI);
        yetiBoots = ItemTFYetiArmor.create(id(TFIds.YETI_BOOTS), 3, ARMOR_YETI);
        // balance 7.7: stack size 8 (was 16)
        iceBomb = new ItemTFIceBomb(id(TFIds.ICE_BOMB)).makeRare().setMaxStackSize(8);
        arcticFur = new TFNugget(id(TFIds.ARCTIC_FUR), TFMaterials.ARCTIC, 100.0F, false);
        arcticHelm = ItemTFArcticArmor.create(id(TFIds.ARCTIC_HELM), 0, ARMOR_ARCTIC);
        arcticPlate = ItemTFArcticArmor.create(id(TFIds.ARCTIC_PLATE), 1, ARMOR_ARCTIC);
        arcticLegs = ItemTFArcticArmor.create(id(TFIds.ARCTIC_LEGS), 2, ARMOR_ARCTIC);
        arcticBoots = ItemTFArcticArmor.create(id(TFIds.ARCTIC_BOOTS), 3, ARMOR_ARCTIC);
        magicBeans = new ItemTFMagicBeans(id(TFIds.MAGIC_BEANS));
        giantPick = new ItemTFGiantPick(id(TFIds.GIANT_PICK), TOOL_GIANT);
        giantSword = new ItemTFGiantSword(id(TFIds.GIANT_SWORD), TOOL_GIANT);
        tripleBow = new ItemTFTripleBow(id(TFIds.TRIPLE_BOW));
        seekerBow = new ItemTFSeekerBow(id(TFIds.SEEKER_BOW));
        iceBow = new ItemTFIceBow(id(TFIds.ICE_BOW));
        enderBow = new ItemTFEnderBow(id(TFIds.ENDER_BOW));
        iceSword = new ItemTFIceSword(id(TFIds.ICE_SWORD), TOOL_ICE);
        glassSword = new ItemTFGlassSword(id(TFIds.GLASS_SWORD), TOOL_GLASS);
        knightmetalRing = new ItemTF(id(TFIds.KNIGHTMETAL_RING));
        chainBlock = new ItemTFChainBlock(id(TFIds.CHAIN_BLOCK));
        cubeTalisman = new ItemTF(id(TFIds.CUBE_TALISMAN));
        cubeOfAnnihilation = new ItemTFCubeOfAnnihilation(id(TFIds.CUBE_OF_ANNIHILATION));

        // repair nuggets of the metals: difficulty D * 100 / 9 is set by ItemNugget
        ironwoodNugget = new TFMetalNugget(id(TFIds.IRONWOOD_NUGGET), TFMaterials.IRONWOOD);
        steeleafNugget = new TFMetalNugget(id(TFIds.STEELEAF_NUGGET), TFMaterials.STEELEAF);
        knightMetalNugget = new TFMetalNugget(id(TFIds.KNIGHT_METAL_NUGGET), TFMaterials.KNIGHTMETAL);
        fieryNugget = new TFMetalNugget(id(TFIds.FIERY_NUGGET), TFMaterials.FIERY);

        setBalanceValues();

        // Registered names are the 1.7.10 unlocalized names (lang keys and texture file names); every item goes to TFCreativeTabs.ITEMS

        registerTFItem(nagaScale, "nagaScale");
        registerTFItem(plateNaga, "plateNaga");
        registerTFItem(legsNaga, "legsNaga");
        registerTFItem(scepterTwilight, "scepterTwilight");
        registerTFItem(scepterLifeDrain, "scepterLifeDrain");
        registerTFItem(scepterZombie, "scepterZombie");
        //registerTFItem(wandPacification, "wandPacification");   // Wand of Pacification [NYI]
        registerTFItem(oreMeter, "oreMeter");
        registerTFItem(magicMap, "magicMap");
        registerTFItem(mazeMap, "mazeMap");
        registerTFItem(oreMap, "oreMap");
        registerTFItem(feather, "tfFeather");
        registerTFItem(magicMapFocus, "magicMapFocus");
        registerTFItem(mazeMapFocus, "mazeMapFocus");
        registerTFItem(liveRoot, "liveRoot");
        registerTFItem(ironwoodRaw, "ironwoodRaw");
        registerTFItem(ironwoodIngot, "ironwoodIngot");
        registerTFItem(ironwoodHelm, "ironwoodHelm");
        registerTFItem(ironwoodPlate, "ironwoodPlate");
        registerTFItem(ironwoodLegs, "ironwoodLegs");
        registerTFItem(ironwoodBoots, "ironwoodBoots");
        registerTFItem(ironwoodSword, "ironwoodSword");
        registerTFItem(ironwoodShovel, "ironwoodShovel");
        registerTFItem(ironwoodPick, "ironwoodPick");
        registerTFItem(ironwoodAxe, "ironwoodAxe");
        registerTFItem(ironwoodHoe, "ironwoodHoe");
        registerTFItem(torchberries, "torchberries");
        registerTFItem(venisonRaw, "venisonRaw");
        registerTFItem(venisonCooked, "venisonCooked");
        registerTFItem(hydraChop, "hydraChop");
        registerTFItem(fieryBlood, "fieryBlood");
        registerTFItem(trophy, "trophy");
        registerTFItem(fieryIngot, "fieryIngot");
        registerTFItem(fieryHelm, "fieryHelm");
        registerTFItem(fieryPlate, "fieryPlate");
        registerTFItem(fieryLegs, "fieryLegs");
        registerTFItem(fieryBoots, "fieryBoots");
        registerTFItem(fierySword, "fierySword");
        registerTFItem(fieryPick, "fieryPick");
        registerTFItem(steeleafIngot, "steeleafIngot");
        registerTFItem(steeleafHelm, "steeleafHelm");
        registerTFItem(steeleafPlate, "steeleafPlate");
        registerTFItem(steeleafLegs, "steeleafLegs");
        registerTFItem(steeleafBoots, "steeleafBoots");
        registerTFItem(steeleafSword, "steeleafSword");
        registerTFItem(steeleafShovel, "steeleafShovel");
        registerTFItem(steeleafPick, "steeleafPick");
        registerTFItem(steeleafAxe, "steeleafAxe");
        registerTFItem(steeleafHoe, "steeleafHoe");
        registerTFItem(minotaurAxe, "minotaurAxe");
        registerTFItem(mazebreakerPick, "mazebreakerPick");
        registerTFItem(transformPowder, "transformPowder");
        registerTFItem(meefRaw, "meefRaw");
        registerTFItem(meefSteak, "meefSteak");
        registerTFItem(meefStroganoff, "meefStroganoff");
        registerTFItem(mazeWafer, "mazeWafer");
        registerTFItem(emptyMagicMap, "emptyMagicMap");
        registerTFItem(emptyMazeMap, "emptyMazeMap");
        registerTFItem(emptyOreMap, "emptyOreMap");
        registerTFItem(oreMagnet, "oreMagnet");
        registerTFItem(crumbleHorn, "crumbleHorn");
        registerTFItem(peacockFan, "peacockFan");
        registerTFItem(moonwormQueen, "moonwormQueen");
        registerTFItem(charmOfLife1, "charmOfLife1");
        registerTFItem(charmOfLife2, "charmOfLife2");
        registerTFItem(charmOfKeeping1, "charmOfKeeping1");
        registerTFItem(charmOfKeeping2, "charmOfKeeping2");
        registerTFItem(charmOfKeeping3, "charmOfKeeping3");
        registerTFItem(towerKey, "towerKey");
        registerTFItem(borerEssence, "borerEssence");
        registerTFItem(carminite, "carminite");
        registerTFItem(experiment115, "experiment115");
        registerTFItem(armorShard, "armorShards");
        registerTFItem(knightMetal, "knightMetal");
        registerTFItem(shardCluster, "shardCluster");
        registerTFItem(knightlyHelm, "knightlyHelm");
        registerTFItem(knightlyPlate, "knightlyPlate");
        registerTFItem(knightlyLegs, "knightlyLegs");
        registerTFItem(knightlyBoots, "knightlyBoots");
        registerTFItem(knightlySword, "knightlySword");
        registerTFItem(knightlyPick, "knightlyPick");
        registerTFItem(knightlyAxe, "knightlyAxe");
        registerTFItem(phantomHelm, "phantomHelm");
        registerTFItem(phantomPlate, "phantomPlate");
        registerTFItem(lampOfCinders, "lampOfCinders");
        registerTFItem(fieryTears, "fieryTears");
        registerTFItem(alphaFur, "alphaFur");
        registerTFItem(yetiHelm, "yetiHelm");
        registerTFItem(yetiPlate, "yetiPlate");
        registerTFItem(yetiLegs, "yetiLegs");
        registerTFItem(yetiBoots, "yetiBoots");
        registerTFItem(iceBomb, "iceBomb");
        registerTFItem(arcticFur, "arcticFur");
        registerTFItem(arcticHelm, "arcticHelm");
        registerTFItem(arcticPlate, "arcticPlate");
        registerTFItem(arcticLegs, "arcticLegs");
        registerTFItem(arcticBoots, "arcticBoots");
        registerTFItem(magicBeans, "magicBeans");
        registerTFItem(giantPick, "giantPick");
        registerTFItem(giantSword, "giantSword");
        registerTFItem(tripleBow, "tripleBow");
        registerTFItem(seekerBow, "seekerBow");
        registerTFItem(iceBow, "iceBow");
        registerTFItem(enderBow, "enderBow");
        registerTFItem(iceSword, "iceSword");
        registerTFItem(glassSword, "glassSword");
        registerTFItem(knightmetalRing, "knightmetalRing");
        registerTFItem(chainBlock, "chainBlock");
        registerTFItem(cubeTalisman, "cubeTalisman");
        registerTFItem(cubeOfAnnihilation, "cubeOfAnnihilation");

        registerTFItem(spawnEgg, "tfspawnegg");

        registerNugget(ironwoodNugget, "ironwoodNugget", "ironwoodNugget");
        registerNugget(steeleafNugget, "steeleafNugget", "steeleafNugget");
        registerNugget(knightMetalNugget, "knightMetalNugget", "knightMetalNugget");
        registerNugget(fieryNugget, "fieryNugget", "fieryNugget");
    }

    /** Item.<init> adds 256 to the constructor argument (PORTING_GUIDE section 2). */
    private static int id(int finalId) {
        return TFIds.item(finalId);
    }

    /**
     * Crafting difficulty as recipe component (balance 6.6), smelting XP (6.4) and "no recipe" markers.
     */
    private static void setBalanceValues() {
        // vanilla items MITE leaves unset (no recipe produces them), but TF recipes consume them
        Item.ghastTear.setCraftingDifficultyAsComponent(400.0F);
        Item.glassBottle.setCraftingDifficultyAsComponent(25.0F);
        liveRoot.setCraftingDifficultyAsComponent(25.0F);
        torchberries.setCraftingDifficultyAsComponent(25.0F);
        feather.setCraftingDifficultyAsComponent(25.0F);
        borerEssence.setCraftingDifficultyAsComponent(50.0F);
        carminite.setCraftingDifficultyAsComponent(200.0F);
        armorShard.setCraftingDifficultyAsComponent(155.6F);
        shardCluster.setCraftingDifficultyAsComponent(1400.0F);
        fieryBlood.setCraftingDifficultyAsComponent(800.0F);
        fieryTears.setCraftingDifficultyAsComponent(800.0F);
        magicMapFocus.setCraftingDifficultyAsComponent(100.0F);
        mazeMapFocus.setCraftingDifficultyAsComponent(100.0F);
        ironwoodRaw.setCraftingDifficultyAsComponent(869.0F);
        ironwoodIngot.setCraftingDifficultyAsComponent(1200.0F);
        steeleafIngot.setCraftingDifficultyAsComponent(500.0F);
        knightMetal.setCraftingDifficultyAsComponent(1400.0F);
        fieryIngot.setCraftingDifficultyAsComponent(1600.0F);
        knightmetalRing.setCraftingDifficultyAsComponent(5600.0F);
        // loot-only items that are components of other recipes (maps, charms) or craft-by-recharge
        mazeMap.setCraftingDifficultyAsComponent(400.0F);
        charmOfLife1.setCraftingDifficultyAsComponent(300.0F);
        charmOfKeeping1.setCraftingDifficultyAsComponent(300.0F);
        // smelting XP is the output item's reward (balance 6.4): ironwood 0 (the iron ingot already paid), knightmetal 10
        ironwoodIngot.setXPReward(0);
        knightMetal.setXPReward(10);

        // Damageable items without a crafting recipe (loot only) would raise the "no way to produce" HUD warning
        markLootOnly(minotaurAxe, 3000.0F);
        markLootOnly(mazebreakerPick, 3000.0F);
        markLootOnly(phantomHelm, 4000.0F);
        markLootOnly(phantomPlate, 4000.0F);
        markLootOnly(tripleBow, 2000.0F);
        markLootOnly(seekerBow, 2000.0F);
        markLootOnly(iceBow, 2000.0F);
        markLootOnly(enderBow, 2000.0F);
        markLootOnly(iceSword, 1500.0F);
        markLootOnly(glassSword, 1500.0F);
        markLootOnly(scepterTwilight, 2000.0F);
        markLootOnly(scepterLifeDrain, 2000.0F);
        markLootOnly(scepterZombie, 2000.0F);
        markLootOnly(oreMeter, 1000.0F);
        markLootOnly(oreMagnet, 1000.0F);
        markLootOnly(crumbleHorn, 1000.0F);
        markLootOnly(peacockFan, 1000.0F);
        markLootOnly(moonwormQueen, 1000.0F);
        markLootOnly(lampOfCinders, 1000.0F);
        markLootOnly(cubeOfAnnihilation, 5000.0F);
    }

    /** Config food.hydraChop "satiation/nutrition" (default 16/16); index 0 = satiation, 1 = nutrition. */
    private static int hydraChopValue(int index) {
        int[] values = {16, 16};
        String raw = TFConfig.foodHydraChop;
        if (raw != null) {
            String[] parts = raw.split("/");
            for (int i = 0; i < 2 && i < parts.length; i++) {
                try {
                    values[i] = Math.max(1, Math.min(40, Integer.parseInt(parts[i].trim())));
                } catch (NumberFormatException ex) {
                    TwilightForestMod.LOGGER.warn("Invalid food.hydraChop '{}', using the default for part {}", raw, i);
                }
            }
        }
        return values[index];
    }

    private static void markLootOnly(Item item, float difficulty) {
        item.setLowestCraftingDifficultyToProduce(difficulty);
    }

    private static void registerTFItem(Item item, String name) {
        TFRegistry.registerItem(item, name, TFCreativeTabs.ITEMS);
    }

    private static void registerNugget(Item item, String name, String iconName) {
        TFRegistry.registerItem(item, name, TFCreativeTabs.ITEMS);
        ((IItem) item).setItemTextureName(TwilightForestMod.ID + ":" + iconName);
    }

    /**
     * Repair nugget of a TF metal. The anvil compares the nugget's repair material with its own metal, so the nugget repairs
     * at the material's workbench gate: ironwood (ironwood.gateProxy) on an iron anvil, the others by their own durability
     * (steeleaf iron anvil, knightmetal and fiery ancient metal anvil).
     */
    private static class TFMetalNugget extends NuggetItem {
        private final TFMaterials.TFMaterial metal;

        TFMetalNugget(int id, TFMaterials.TFMaterial metal) {
            super(id, metal);
            this.metal = metal;
        }

        @Override
        public Material getMaterialForRepairs() {
            return this.metal == null ? super.getMaterialForRepairs() : this.metal.getGate();
        }
    }

    /** Naga scale / fur: an ItemNugget (so the anvil accepts it for repairs) with the TF difficulty and rarity. */
    private static class TFNugget extends NuggetItem {
        private final boolean rare;

        TFNugget(int id, Material material, float difficulty, boolean rare) {
            super(id, material);
            this.rare = rare;
            this.setCraftingDifficultyAsComponent(difficulty);
        }

        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return this.rare ? EnumRarity.rare : EnumRarity.uncommon;
        }
    }
}
