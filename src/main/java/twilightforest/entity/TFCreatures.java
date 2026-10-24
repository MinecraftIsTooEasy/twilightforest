package twilightforest.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import net.minecraft.Entity;
import net.minecraft.EntityList;
import net.minecraft.World;
import net.xiaoyu233.fml.reload.event.EntityRegisterEvent;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFRegistry;

public class TFCreatures {

    /**
     * Entity naming scheme: every TF entity is registered as "twilightforest:&lt;snake_case_id&gt;" (FML prefixes the namespace), e.g.
     * "twilightforest:twilight_lich". The language key is therefore "entity.twilightforest:twilight_lich.name" (Entity.getEntityName).
     * The ids are ASCII, lower case and without spaces, so they are safe in NBT, commands and lang files, and are independent of the
     * display name (which lives only in the lang files).
     */
    private static final Map<Class<?>, String> NAMES = new LinkedHashMap<Class<?>, String>();

    /** Entities that are not in EntityList (projectiles, parts): same naming, resolved by the Entity.getEntityName mixin. */
    private static final Map<Class<?>, String> NAMED_NON_LIVING = new LinkedHashMap<Class<?>, String>();

    /** This is a HashMap of the Creative Entity Eggs/Spawners. */
    public static HashMap<Integer, TFEntityEggInfo> entityEggs = new LinkedHashMap<Integer, TFEntityEggInfo>();

    /**
     * Registers every TF creature. Runs inside EntityList.<clinit> (EntityRegisterEvent): class literals, names and ids only,
     * nothing that reads Block.* / Item.* (PORTING_GUIDE section 3). The eggs end up in MITE's EntityList.entityEggs,
     * so the vanilla monster placer can spawn all of them.
     */
    public static void registerCreatures(EntityRegisterEvent e) {
        TFRegistry.beginEntities(e);

        registerTFCreature(twilightforest.entity.passive.EntityTFBoar.class, "wild_boar", TwilightForestMod.idMobWildBoar, 0x83653b, 0xffefca);
        registerTFCreature(twilightforest.entity.passive.EntityTFBighorn.class, "bighorn_sheep", TwilightForestMod.idMobBighornSheep, 0xdbceaf, 0xd7c771);
        registerTFCreature(twilightforest.entity.passive.EntityTFDeer.class, "wild_deer", TwilightForestMod.idMobWildDeer, 0x7b4d2e, 0x4b241d);
        registerTFCreature(twilightforest.entity.EntityTFRedcap.class, "redcap", TwilightForestMod.idMobRedcap, 0x3b3a6c, 0xab1e14);
        registerTFCreature(twilightforest.entity.EntityTFSwarmSpider.class, "swarm_spider", TwilightForestMod.idMobSwarmSpider, 0x32022e, 0x17251e);
        registerTFCreature(twilightforest.entity.boss.EntityTFNaga.class, "naga", TwilightForestMod.idMobNaga, 0xa4d316, 0x1b380b);
        //registerTFCreature(twilightforest.entity.EntityTFNagaSegmentOld.class, "Naga Segment", TwilightForestMod.idMobNagaSegment);  // no longer needed, regenerated instantly as required
        registerTFCreature(twilightforest.entity.EntityTFSkeletonDruid.class, "skeleton_druid", TwilightForestMod.idMobSkeletonDruid, 0xa3a3a3, 0x2a3b17);
        registerTFCreature(twilightforest.entity.EntityTFHostileWolf.class, "hostile_wolf", TwilightForestMod.idMobHostileWolf, 0xd7d3d3, 0xab1e14);
        registerTFCreature(twilightforest.entity.EntityTFWraith.class, "twilight_wraith", TwilightForestMod.idMobTwilightWraith, 0x505050, 0x838383);
        registerTFCreature(twilightforest.entity.EntityTFHedgeSpider.class, "hedge_spider", TwilightForestMod.idMobHedgeSpider, 0x235f13, 0x562653);
        registerTFCreature(twilightforest.entity.boss.EntityTFHydra.class, "hydra", TwilightForestMod.idMobHydra, 0x142940, 0x29806b);
        registerTFCreature(twilightforest.entity.boss.EntityTFLich.class, "twilight_lich", TwilightForestMod.idMobLich, 0xaca489, 0x360472);
        registerTFCreature(twilightforest.entity.passive.EntityTFPenguin.class, "glacier_penguin", TwilightForestMod.idMobPenguin, 0x12151b, 0xf9edd2);
        registerTFCreature(twilightforest.entity.boss.EntityTFLichMinion.class, "lich_minion", TwilightForestMod.idMobLichMinion);
        registerTFCreature(twilightforest.entity.EntityTFLoyalZombie.class, "loyal_zombie", TwilightForestMod.idMobLoyalZombie);
        registerTFCreature(twilightforest.entity.passive.EntityTFTinyBird.class, "tiny_bird", TwilightForestMod.idMobTinyBird, 0x33aadd, 0x1188ee);
        registerTFCreature(twilightforest.entity.passive.EntityTFSquirrel.class, "forest_squirrel", TwilightForestMod.idMobSquirrel, 0x904f12, 0xeeeeee);
        registerTFCreature(twilightforest.entity.passive.EntityTFBunny.class, "forest_bunny", TwilightForestMod.idMobBunny, 0xfefeee, 0xccaa99);
        registerTFCreature(twilightforest.entity.passive.EntityTFRaven.class, "forest_raven", TwilightForestMod.idMobRaven, 0x000011, 0x222233);
        registerTFCreature(twilightforest.entity.passive.EntityTFQuestRam.class, "questing_ram", TwilightForestMod.idMobQuestRam);
        registerTFCreature(twilightforest.entity.EntityTFKobold.class, "twilight_kobold", TwilightForestMod.idMobKobold, 0x372096, 0x895d1b);
        //registerTFCreature(twilightforest.entity.EntityTFBoggard.class, "Boggard", TwilightForestMod.idMobBoggard);
        registerTFCreature(twilightforest.entity.EntityTFMosquitoSwarm.class, "mosquito_swarm", TwilightForestMod.idMobMosquitoSwarm, 0x080904, 0x2d2f21);
        registerTFCreature(twilightforest.entity.EntityTFDeathTome.class, "death_tome", TwilightForestMod.idMobDeathTome, 0x774e22, 0xdbcdbe);
        registerTFCreature(twilightforest.entity.EntityTFMinotaur.class, "minotaur", TwilightForestMod.idMobMinotaur, 0x3f3024, 0xaa7d66);
        registerTFCreature(twilightforest.entity.boss.EntityTFMinoshroom.class, "minoshroom", TwilightForestMod.idMobMinoshroom, 0xa81012, 0xaa7d66);
        registerTFCreature(twilightforest.entity.EntityTFFireBeetle.class, "fire_beetle", TwilightForestMod.idMobFireBeetle, 0x1d0b00, 0xcb6f25);
        registerTFCreature(twilightforest.entity.EntityTFSlimeBeetle.class, "slime_beetle", TwilightForestMod.idMobSlimeBeetle, 0x0c1606, 0x60a74c);
        registerTFCreature(twilightforest.entity.EntityTFPinchBeetle.class, "pinch_beetle", TwilightForestMod.idMobPinchBeetle, 0xbc9327, 0x241609);
        registerTFCreature(twilightforest.entity.EntityTFMazeSlime.class, "maze_slime", TwilightForestMod.idMobMazeSlime, 0xa3a3a3, 0x2a3b17);
        registerTFCreature(twilightforest.entity.EntityTFRedcapSapper.class, "redcap_sapper", TwilightForestMod.idMobRedcapSapper, 0x575d21, 0xab1e14);
        registerTFCreature(twilightforest.entity.EntityTFMistWolf.class, "mist_wolf", TwilightForestMod.idMobMistWolf, 0x3a1411, 0xe2c88a);
        registerTFCreature(twilightforest.entity.EntityTFKingSpider.class, "king_spider", TwilightForestMod.idMobKingSpider, 0x2c1a0e, 0xffc017);
        registerTFCreature(twilightforest.entity.passive.EntityTFMobileFirefly.class, "firefly", TwilightForestMod.idMobFirefly, 0xa4d316, 0xbaee02);
        registerTFCreature(twilightforest.entity.EntityTFMiniGhast.class, "mini_ghast", TwilightForestMod.idMobMiniGhast, 0xbcbcbc, 0xa74343);
        registerTFCreature(twilightforest.entity.EntityTFTowerGhast.class, "tower_ghast", TwilightForestMod.idMobTowerGhast, 0xbcbcbc, 0xb77878);
        registerTFCreature(twilightforest.entity.EntityTFTowerGolem.class, "tower_golem", TwilightForestMod.idMobTowerGolem, 0x6b3d20, 0xe2ddda);
        registerTFCreature(twilightforest.entity.EntityTFTowerTermite.class, "tower_termite", TwilightForestMod.idMobTowerTermite, 0x5d2b21, 0xaca03a);
        registerTFCreature(twilightforest.entity.EntityTFTowerBroodling.class, "redscale_broodling", TwilightForestMod.idMobTowerBroodling, 0x343c14, 0xbaee02);
        registerTFCreature(twilightforest.entity.boss.EntityTFUrGhast.class, "tower_boss", TwilightForestMod.idMobTowerBoss, 0xbcbcbc, 0xb77878);
        registerTFCreature(twilightforest.entity.EntityTFBlockGoblin.class, "block_and_chain_goblin", TwilightForestMod.idMobBlockGoblin, 0xd3e7bc, 0x1f3fff);
        registerTFCreature(twilightforest.entity.EntityTFGoblinKnightUpper.class, "upper_goblin_knight", TwilightForestMod.idMobGoblinKnightUpper);
        registerTFCreature(twilightforest.entity.EntityTFGoblinKnightLower.class, "lower_goblin_knight", TwilightForestMod.idMobGoblinKnightLower, 0x566055, 0xd3e7bc);
        registerTFCreature(twilightforest.entity.EntityTFHelmetCrab.class, "helmet_crab", TwilightForestMod.idMobHelmetCrab, 0xfb904b, 0xd3e7bc);
        registerTFCreature(twilightforest.entity.boss.EntityTFKnightPhantom.class, "knight_phantom", TwilightForestMod.idMobKnightPhantom, 0xa6673b, 0xd3e7bc);
        registerTFCreature(twilightforest.entity.EntityTFYeti.class, "yeti", TwilightForestMod.idMobYeti, 0xdedede, 0x4675bb);
        registerTFCreature(twilightforest.entity.boss.EntityTFYetiAlpha.class, "yeti_boss", TwilightForestMod.idMobYetiBoss, 0xcdcdcd, 0x29486e);
        registerTFCreature(twilightforest.entity.EntityTFWinterWolf.class, "winter_wolf", TwilightForestMod.idMobWinterWolf, 0xdfe3e5, 0xb2bcca);
        registerTFCreature(twilightforest.entity.EntityTFSnowGuardian.class, "snow_guardian", TwilightForestMod.idMobSnowGuardian, 0xd3e7bc, 0xfefefe);
        registerTFCreature(twilightforest.entity.EntityTFIceShooter.class, "stable_ice_core", TwilightForestMod.idMobStableIceCore, 0xa1bff3, 0x7000f8);
        registerTFCreature(twilightforest.entity.EntityTFIceExploder.class, "unstable_ice_core", TwilightForestMod.idMobUnstableIceCore, 0x9aacf5, 0x9b0fa5);
        registerTFCreature(twilightforest.entity.boss.EntityTFSnowQueen.class, "snow_queen", TwilightForestMod.idMobSnowQueen, 0xb1b2d4, 0x87006e);
        registerTFCreature(twilightforest.entity.EntityTFTroll.class, "troll", TwilightForestMod.idMobTroll, 0x9ea98f, 0xb0948e);
        registerTFCreature(twilightforest.entity.EntityTFGiantMiner.class, "giant_miner", TwilightForestMod.idMobGiantMiner, 0x211b52, 0x9a9a9a);
        registerTFCreature(twilightforest.entity.EntityTFArmoredGiant.class, "armored_giant", TwilightForestMod.idMobArmoredGiant, 0x239391, 0x9a9a9a);
        registerTFCreature(twilightforest.entity.boss.EntityTFIceCrystal.class, "ice_crystal", TwilightForestMod.idMobIceCrystal, 0xdce9fe, 0xadcafb);
        registerTFCreature(twilightforest.entity.EntityTFHarbingerCube.class, "harbinger_cube", TwilightForestMod.idMobHarbingerCube, 0x00000a, 0x8b0000);
        registerTFCreature(twilightforest.entity.EntityTFAdherent.class, "adherent", TwilightForestMod.idMobAdherent, 0x0a0000, 0x00008b);
        registerTFCreature(twilightforest.entity.EntityTFRovingCube.class, "roving_cube", TwilightForestMod.idMobRovingCube, 0x0a0000, 0x00009b);
    

        // Not registered in EntityList (they are tracked through TFEntitySpawnRegistry) but they still need a stable name for
        // death messages and the like.
        registerNonLiving(twilightforest.entity.boss.EntityTFHydraHead.class, "hydra_head");
        registerNonLiving(twilightforest.entity.EntityTFNatureBolt.class, "nature_bolt");
        registerNonLiving(twilightforest.entity.boss.EntityTFLichBolt.class, "lich_bolt");
        registerNonLiving(twilightforest.entity.EntityTFTwilightWandBolt.class, "twilight_wand_bolt");
        registerNonLiving(twilightforest.entity.EntityTFTomeBolt.class, "tome_bolt");
        registerNonLiving(twilightforest.entity.boss.EntityTFHydraMortar.class, "hydra_mortar");
        registerNonLiving(twilightforest.entity.boss.EntityTFLichBomb.class, "lich_bomb");
        registerNonLiving(twilightforest.entity.EntityTFMoonwormShot.class, "moonworm_shot");
        registerNonLiving(twilightforest.entity.EntityTFSlimeProjectile.class, "slime_blob");
        registerNonLiving(twilightforest.entity.EntityTFCharmEffect.class, "charm_effect");
        registerNonLiving(twilightforest.entity.boss.EntityTFThrownAxe.class, "thrown_axe");
        registerNonLiving(twilightforest.entity.boss.EntityTFThrownPick.class, "thrown_pick");
        registerNonLiving(twilightforest.entity.boss.EntityTFFallingIce.class, "falling_ice");
        registerNonLiving(twilightforest.entity.boss.EntityTFIceBomb.class, "ice_bomb");
        registerNonLiving(twilightforest.entity.EntitySeekerArrow.class, "seeker_arrow");
        registerNonLiving(twilightforest.entity.EntityIceArrow.class, "ice_arrow");
        registerNonLiving(twilightforest.entity.EntityTFIceSnowball.class, "ice_snowball");
        registerNonLiving(twilightforest.entity.EntityTFChainBlock.class, "chain_block");
        registerNonLiving(twilightforest.entity.EntityTFGoblinChain.class, "goblin_chain");
        registerNonLiving(twilightforest.entity.EntityTFSpikeBlock.class, "spike_block");
        registerNonLiving(twilightforest.entity.EntityTFCubeOfAnnihilation.class, "cube_of_annihilation");
        registerNonLiving(twilightforest.entity.EntityTFSlideBlock.class, "slide_block");
        registerNonLiving(twilightforest.entity.boss.EntityTFUrGhastFireball.class, "ur_ghast_fireball");
        registerNonLiving(twilightforest.entity.boss.EntityTFNagaSegment.class, "naga_segment");
        registerNonLiving(twilightforest.entity.boss.EntityTFSnowQueenIceShield.class, "snow_queen_ice_shield");
        registerNonLiving(twilightforest.entity.EntityTFProtectionBox.class, "protection_box");
        registerNonLiving(twilightforest.entity.passive.EntityTFTinyFirefly.class, "tiny_firefly");
    }

    /** Names a TF entity that is not registered in EntityList (see {@link #NAMED_NON_LIVING}). */
    private static void registerNonLiving(Class<? extends Entity> entityClass, String entityName) {
        NAMED_NON_LIVING.put(entityClass, entityName);
    }

    /** Registered (EntityList) TF creatures: class to base name. */
    public static Map<Class<?>, String> registeredNames() {
        return Collections.unmodifiableMap(NAMES);
    }

    /** TF entities that are named but not registered in EntityList: class to base name. */
    public static Map<Class<?>, String> nonLivingNames() {
        return Collections.unmodifiableMap(NAMED_NON_LIVING);
    }

    /** The language key (without the "entity." prefix and ".name" suffix) of a TF entity class, or null if it is not ours. */
    public static String languageNameOf(Class<?> entityClass) {
        String name = NAMES.get(entityClass);
        if (name == null) {
            name = NAMED_NON_LIVING.get(entityClass);
        }
        return name == null ? null : TFRegistry.NAMESPACE + ":" + name;
    }

    public static void registerTFCreature(Class<? extends Entity> entityClass, String entityName, int id, int backgroundEggColour, int foregroundEggColour) {
        registerTFCreature(entityClass, entityName, id, backgroundEggColour, foregroundEggColour, 80, 3, true);
    }

    public static void registerTFCreature(Class<? extends Entity> entityClass, String entityName, int id, int backgroundEggColour, int foregroundEggColour, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        TFRegistry.registerCreature(entityClass, entityName, id, backgroundEggColour, foregroundEggColour, trackingRange, updateFrequency, sendsVelocityUpdates);
        NAMES.put(entityClass, entityName);
        entityEggs.put(Integer.valueOf(id), new TFEntityEggInfo(id, backgroundEggColour, foregroundEggColour));
    }

    /** A creature without a spawn egg. */
    public static void registerTFCreature(Class<? extends Entity> entityClass, String entityName, int id) {
        TFRegistry.registerCreature(entityClass, entityName, id, -1, -1, 80, 3, true);
        NAMES.put(entityClass, entityName);
    }

    /**
     * Create a new instance of an entity in the world by using an entity ID.
     * This is the version that creates twilight forest creatures for use by their spawn eggs.
     */
    public static Entity createEntityByID(int entityID, World par1World) {
        Entity entity = EntityList.createEntityByID(entityID, par1World);

        if (entity == null) {
            TwilightForestMod.LOGGER.warn("Skipping Entity with id {}", entityID);
        }

        return entity;
    }

    /**
     * Return the name of the monster with that ID.
     */
    public static String getStringFromID(int entityID) {
        return EntityList.getStringFromID(entityID);
    }

    /**
     * The name under which EntityList knows a TF creature, to be used for spawners (setMobID) and EntityList.createEntityByName.
     * FishModLoader 3.4.2 registers entities as "namespace:name", so the bare id finds nothing. Looked up by class so a name can
     * never drift from the registration.
     */
    public static String getSpawnerNameFor(Class<? extends Entity> entityClass) {
        String name = NAMES.get(entityClass);
        if (name == null) {
            throw new IllegalArgumentException(entityClass.getName() + " is not a registered TF creature");
        }
        return TFRegistry.NAMESPACE + ":" + name;
    }

    /** Same for a base name ("swarm_spider"); fails fast on a name that was never registered. */
    public static String getSpawnerNameFor(String baseName) {
        if (!NAMES.containsValue(baseName)) {
            throw new IllegalArgumentException("Unknown TF creature name '" + baseName + "'");
        }
        return TFRegistry.NAMESPACE + ":" + baseName;
    }

}
