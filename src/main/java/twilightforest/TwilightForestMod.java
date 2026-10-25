package twilightforest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.GameRules;
import net.minecraft.World;
import net.xiaoyu233.fml.ModResourceManager;
import net.xiaoyu233.fml.config.ConfigRegistry;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import net.xiaoyu233.fml.reload.event.TileEntityRegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twilightforest.dimension.TFDimension;
import twilightforest.tileentity.TileEntityTFCReactorActive;
import twilightforest.tileentity.TileEntityTFCicada;
import twilightforest.tileentity.TileEntityTFCinderFurnace;
import twilightforest.tileentity.TileEntityTFFirefly;
import twilightforest.tileentity.TileEntityTFFlameJet;
import twilightforest.tileentity.TileEntityTFGhastTrapActive;
import twilightforest.tileentity.TileEntityTFGhastTrapInactive;
import twilightforest.tileentity.TileEntityTFHydraSpawner;
import twilightforest.tileentity.TileEntityTFKnightPhantomsSpawner;
import twilightforest.tileentity.TileEntityTFLichSpawner;
import twilightforest.tileentity.TileEntityTFMinoshroomSpawner;
import twilightforest.tileentity.TileEntityTFMoonworm;
import twilightforest.tileentity.TileEntityTFNagaSpawner;
import twilightforest.tileentity.TileEntityTFPoppingJet;
import twilightforest.tileentity.TileEntityTFReverter;
import twilightforest.tileentity.TileEntityTFSmoker;
import twilightforest.tileentity.TileEntityTFSnowQueenSpawner;
import twilightforest.tileentity.TileEntityTFTowerBossSpawner;
import twilightforest.tileentity.TileEntityTFTowerBuilder;
import twilightforest.tileentity.TileEntityTFTrophy;

import java.util.Optional;

public class TwilightForestMod implements ModInitializer {
    public static final String ID = "twilightforest";
    public static final String VERSION = "2.3.8dev";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final String LOAD_MESSAGE = "shiotsuki : 暮色森林 载入成功，这个模组是免费的！，许可：LGPL-2.1 如果你是花钱购买的请报告 QQ:1260247017 Github:https://github.com/MinecraftIsTooEasy/twilightforest";

    public static final String MODEL_DIR = "twilightforest:textures/model/";
    public static final String GUI_DIR = "twilightforest:textures/gui/";
    public static final String ENVRIO_DIR = "twilightforest:textures/environment/";
    public static final String ARMOR_DIR = "twilightforest:textures/armor/";
    public static final String ENFORCED_PROGRESSION_RULE = "tfEnforcedProgression";

    public static final int GUI_ID_UNCRAFTING = 1;
    public static final int GUI_ID_FURNACE = 2;

    // Set from TFConfig; the defaults apply until the config has been read.
    public static int dimensionID = TFIds.DIMENSION;

    public static boolean silentCicadas;
    public static String twilightForestSeed = "";
    public static boolean disableUncrafting;
    public static boolean oldMapGen;
    public static String portalCreationItemString = "diamond";

    public static float canopyCoverage = 1.7F;
    public static int twilightOakChance = 48;

    // Entity ids are fixed in TFIds
    public static final int idMobWildBoar = TFIds.MOB_WILD_BOAR;
    public static final int idMobBighornSheep = TFIds.MOB_BIGHORN_SHEEP;
    public static final int idMobWildDeer = TFIds.MOB_WILD_DEER;
    public static final int idMobRedcap = TFIds.MOB_REDCAP;
    public static final int idMobSwarmSpider = TFIds.MOB_SWARM_SPIDER;
    public static final int idMobNaga = TFIds.MOB_NAGA;
    public static final int idMobNagaSegment = TFIds.MOB_NAGA_SEGMENT;
    public static final int idMobSkeletonDruid = TFIds.MOB_SKELETON_DRUID;
    public static final int idMobHostileWolf = TFIds.MOB_HOSTILE_WOLF;
    public static final int idMobTwilightWraith = TFIds.MOB_TWILIGHT_WRAITH;
    public static final int idMobHedgeSpider = TFIds.MOB_HEDGE_SPIDER;
    public static final int idMobHydra = TFIds.MOB_HYDRA;
    public static final int idMobLich = TFIds.MOB_LICH;
    public static final int idMobPenguin = TFIds.MOB_PENGUIN;
    public static final int idMobLichMinion = TFIds.MOB_LICH_MINION;
    public static final int idMobLoyalZombie = TFIds.MOB_LOYAL_ZOMBIE;
    public static final int idMobTinyBird = TFIds.MOB_TINY_BIRD;
    public static final int idMobSquirrel = TFIds.MOB_SQUIRREL;
    public static final int idMobBunny = TFIds.MOB_BUNNY;
    public static final int idMobRaven = TFIds.MOB_RAVEN;
    public static final int idMobQuestRam = TFIds.MOB_QUEST_RAM;
    public static final int idMobKobold = TFIds.MOB_KOBOLD;
    public static final int idMobBoggard = TFIds.MOB_BOGGARD;
    public static final int idMobMosquitoSwarm = TFIds.MOB_MOSQUITO_SWARM;
    public static final int idMobDeathTome = TFIds.MOB_DEATH_TOME;
    public static final int idMobMinotaur = TFIds.MOB_MINOTAUR;
    public static final int idMobMinoshroom = TFIds.MOB_MINOSHROOM;
    public static final int idMobFireBeetle = TFIds.MOB_FIRE_BEETLE;
    public static final int idMobSlimeBeetle = TFIds.MOB_SLIME_BEETLE;
    public static final int idMobPinchBeetle = TFIds.MOB_PINCH_BEETLE;
    public static final int idMobMazeSlime = TFIds.MOB_MAZE_SLIME;
    public static final int idMobRedcapSapper = TFIds.MOB_REDCAP_SAPPER;
    public static final int idMobMistWolf = TFIds.MOB_MIST_WOLF;
    public static final int idMobKingSpider = TFIds.MOB_KING_SPIDER;
    public static final int idMobFirefly = TFIds.MOB_FIREFLY;
    public static final int idMobMiniGhast = TFIds.MOB_MINI_GHAST;
    public static final int idMobTowerGhast = TFIds.MOB_TOWER_GHAST;
    public static final int idMobTowerGolem = TFIds.MOB_TOWER_GOLEM;
    public static final int idMobTowerTermite = TFIds.MOB_TOWER_TERMITE;
    public static final int idMobTowerBroodling = TFIds.MOB_TOWER_BROODLING;
    public static final int idMobTowerBoss = TFIds.MOB_TOWER_BOSS;
    public static final int idMobBlockGoblin = TFIds.MOB_BLOCK_GOBLIN;
    public static final int idMobGoblinKnightUpper = TFIds.MOB_GOBLIN_KNIGHT_UPPER;
    public static final int idMobGoblinKnightLower = TFIds.MOB_GOBLIN_KNIGHT_LOWER;
    public static final int idMobHelmetCrab = TFIds.MOB_HELMET_CRAB;
    public static final int idMobKnightPhantom = TFIds.MOB_KNIGHT_PHANTOM;
    public static final int idMobYeti = TFIds.MOB_YETI;
    public static final int idMobYetiBoss = TFIds.MOB_YETI_BOSS;
    public static final int idMobWinterWolf = TFIds.MOB_WINTER_WOLF;
    public static final int idMobSnowGuardian = TFIds.MOB_SNOW_GUARDIAN;
    public static final int idMobStableIceCore = TFIds.MOB_STABLE_ICE_CORE;
    public static final int idMobUnstableIceCore = TFIds.MOB_UNSTABLE_ICE_CORE;
    public static final int idMobSnowQueen = TFIds.MOB_SNOW_QUEEN;
    public static final int idMobTroll = TFIds.MOB_TROLL;
    public static final int idMobGiantMiner = TFIds.MOB_GIANT_MINER;
    public static final int idMobArmoredGiant = TFIds.MOB_ARMORED_GIANT;
    public static final int idMobIceCrystal = TFIds.MOB_ICE_CRYSTAL;
    public static final int idMobHarbingerCube = TFIds.MOB_HARBINGER_CUBE;
    public static final int idMobAdherent = TFIds.MOB_ADHERENT;
    public static final int idMobRovingCube = TFIds.MOB_ROVING_CUBE;

    // Packet23VehicleSpawn types, shifted to 100+ because MITE uses 1..20 itself
    public static final int idVehicleSpawnNatureBolt = TFIds.VEHICLE_NATURE_BOLT;
    public static final int idVehicleSpawnLichBolt = TFIds.VEHICLE_LICH_BOLT;
    public static final int idVehicleSpawnTwilightWandBolt = TFIds.VEHICLE_TWILIGHT_WAND_BOLT;
    public static final int idVehicleSpawnTomeBolt = TFIds.VEHICLE_TOME_BOLT;
    public static final int idVehicleSpawnHydraMortar = TFIds.VEHICLE_HYDRA_MORTAR;
    public static final int idVehicleSpawnLichBomb = TFIds.VEHICLE_LICH_BOMB;
    public static final int idVehicleSpawnMoonwormShot = TFIds.VEHICLE_MOONWORM_SHOT;
    public static final int idVehicleSpawnSlimeBlob = TFIds.VEHICLE_SLIME_BLOB;
    public static final int idVehicleSpawnCharmEffect = TFIds.VEHICLE_CHARM_EFFECT;
    public static final int idVehicleSpawnThrownAxe = TFIds.VEHICLE_THROWN_AXE;
    public static final int idVehicleSpawnThrownPick = TFIds.VEHICLE_THROWN_PICK;
    public static final int idVehicleSpawnFallingIce = TFIds.VEHICLE_FALLING_ICE;
    public static final int idVehicleSpawnThrownIce = TFIds.VEHICLE_THROWN_ICE;
    public static final int idVehicleSpawnSeekerArrow = TFIds.VEHICLE_SEEKER_ARROW;
    public static final int idVehicleSpawnIceSnowball = TFIds.VEHICLE_ICE_SNOWBALL;
    public static final int idVehicleSpawnChainBlock = TFIds.VEHICLE_CHAIN_BLOCK;
    public static final int idVehicleSpawnCubeOfAnnihilation = TFIds.VEHICLE_CUBE_OF_ANNIHILATION;
    public static final int idVehicleSpawnSlideBlock = TFIds.VEHICLE_SLIDE_BLOCK;
    public static final int idVehicleSpawnHydraHead = TFIds.VEHICLE_HYDRA_HEAD;
    public static final int idMobHydraHead = TFIds.MOB_HYDRA_HEAD;

    // Biome ids are fixed in TFIds
    public static final int idBiomeLake = TFIds.BIOME_LAKE;
    public static final int idBiomeTwilightForest = TFIds.BIOME_TWILIGHT_FOREST;
    public static final int idBiomeTwilightForestVariant = TFIds.BIOME_TWILIGHT_FOREST_VARIANT;
    public static final int idBiomeHighlands = TFIds.BIOME_HIGHLANDS;
    public static final int idBiomeMushrooms = TFIds.BIOME_MUSHROOMS;
    public static final int idBiomeSwamp = TFIds.BIOME_SWAMP;
    public static final int idBiomeStream = TFIds.BIOME_STREAM;
    public static final int idBiomeSnowfield = TFIds.BIOME_SNOWFIELD;
    public static final int idBiomeGlacier = TFIds.BIOME_GLACIER;
    public static final int idBiomeClearing = TFIds.BIOME_CLEARING;
    public static final int idBiomeOakSavanna = TFIds.BIOME_OAK_SAVANNA;
    public static final int idBiomeFireflyForest = TFIds.BIOME_FIREFLY_FOREST;
    public static final int idBiomeDeepMushrooms = TFIds.BIOME_DEEP_MUSHROOMS;
    public static final int idBiomeDarkForestCenter = TFIds.BIOME_DARK_FOREST_CENTER;
    public static final int idBiomeHighlandsCenter = TFIds.BIOME_HIGHLANDS_CENTER;
    public static final int idBiomeDarkForest = TFIds.BIOME_DARK_FOREST;
    public static final int idBiomeEnchantedForest = TFIds.BIOME_ENCHANTED_FOREST;
    public static final int idBiomeFireSwamp = TFIds.BIOME_FIRE_SWAMP;
    public static final int idBiomeThornlands = TFIds.BIOME_THORNLANDS;

    // Biome id conflict reporting, filled in by TFBiomeBase
    public static boolean hasBiomeIdConflicts = false;
    public static boolean hasAssignedBiomeID = false;

    public static TwilightForestMod instance;
    // Replaced by TFClientProxy from the client entrypoint
    public static TFCommonProxy proxy = new TFCommonProxy();

    public TwilightForestMod() {
        instance = this;
    }

    @Override
    public Optional<ConfigRegistry> createConfig() {
        return Optional.of(TFConfig.REGISTRY);
    }

    @Override
    public void onInitialize() {
        ModResourceManager.addResourcePackDomain(ID);
        LOGGER.info(LOAD_MESSAGE);
        TFDimension.setId(dimensionID);
        MITEEvents.MITE_EVENT_BUS.register(new TFRegistryHandler());
        MITEEvents.MITE_EVENT_BUS.register(new TFEvents());
    }

    public static void registerTileEntities(TileEntityRegisterEvent e) {
        e.register(TileEntityTFFirefly.class, "twilightforest:Firefly");
        e.register(TileEntityTFCicada.class, "twilightforest:Cicada");
        e.register(TileEntityTFNagaSpawner.class, "twilightforest:NagaSpawner");
        e.register(TileEntityTFLichSpawner.class, "twilightforest:LichSpawner");
        e.register(TileEntityTFHydraSpawner.class, "twilightforest:HydraSpawner");
        e.register(TileEntityTFSmoker.class, "twilightforest:SwampSmoker");
        e.register(TileEntityTFPoppingJet.class, "twilightforest:PoppingFlameJet");
        e.register(TileEntityTFFlameJet.class, "twilightforest:LitFlameJet");
        e.register(TileEntityTFMoonworm.class, "twilightforest:Moonworm");
        e.register(TileEntityTFTowerBuilder.class, "twilightforest:TowerBuilder");
        e.register(TileEntityTFReverter.class, "twilightforest:TowerReverter");
        e.register(TileEntityTFTrophy.class, "twilightforest:TFTrophy");
        e.register(TileEntityTFTowerBossSpawner.class, "twilightforest:TowerBossSpawner");
        e.register(TileEntityTFGhastTrapInactive.class, "twilightforest:InactiveGhastTrap");
        e.register(TileEntityTFGhastTrapActive.class, "twilightforest:ActiveGhastTrap");
        e.register(TileEntityTFCReactorActive.class, "twilightforest:ActiveCarminiteReactor");
        e.register(TileEntityTFKnightPhantomsSpawner.class, "twilightforest:KnightPhantomSpawner");
        e.register(TileEntityTFSnowQueenSpawner.class, "twilightforest:SnowQueenSpawner");
        e.register(TileEntityTFMinoshroomSpawner.class, "twilightforest:MinoshroomSpawner");
        e.register(TileEntityTFCinderFurnace.class, "twilightforest:CinderFurnace");
    }

    /**
     * The server decides from progress.mode; the client only knows the value the server pushed into the game rule
     * (TFNetwork.sendEnforcedProgressionStatus), so it is read from there.
     */
    public static boolean isProgressionEnforced(World world) {
        if (world != null && world.isRemote) {
            GameRules rules = world.getGameRules();
            if (rules.hasRule(ENFORCED_PROGRESSION_RULE)) {
                return Boolean.parseBoolean(rules.getGameRuleStringValue(ENFORCED_PROGRESSION_RULE));
            }
        }
        return TFConfig.progressEnforced();
    }
}
