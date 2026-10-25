package twilightforest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.Block;
import net.minecraft.BlockMounted;
import net.minecraft.Entity;
import net.minecraft.EntityList;
import net.minecraft.TileEntity;
import net.minecraft.World;
import net.minecraft.WorldProvider;
import net.minecraft.server.MinecraftServer;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.block.BlockTFBossSpawner;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFRegistry;
import twilightforest.dimension.TFDimension;
import twilightforest.entity.TFCreatures;
import twilightforest.tileentity.TileEntityTFBossSpawner;
import twilightforest.world.WorldProviderTwilightForest;

/**
 * Startup self check (MITE_BALANCE H1): when a critical item fails the portal can no longer be created (players already
 * inside can still leave). Runs once per server start from {@link TFEvents#onServerWorldsLoaded}.
 */
public final class TFSelfCheck {
    private static volatile boolean failed;

    private TFSelfCheck() {
    }

    /** True unless a check has run and failed (or the check is disabled by the config). */
    public static boolean passed() {
        return !TFConfig.selfCheck || !failed;
    }

    public static void run(MinecraftServer server) {
        boolean ok = true;

        if (server.worldServers == null || server.worldServers.length < TFDimension.WORLD_COUNT
                || server.worldServers[TFDimension.WORLD_INDEX] == null) {
            TwilightForestMod.LOGGER.error("Self check: the Twilight Forest world slot {} is missing", TFDimension.WORLD_INDEX);
            ok = false;
        } else if (server.worldServers[TFDimension.WORLD_INDEX].provider.dimensionId != TFDimension.id) {
            TwilightForestMod.LOGGER.error("Self check: world slot {} does not hold dimension {}", TFDimension.WORLD_INDEX, TFDimension.id);
            ok = false;
        }

        WorldProvider provider = WorldProvider.getProviderForDimension(TFDimension.id);
        if (!(provider instanceof WorldProviderTwilightForest)) {
            TwilightForestMod.LOGGER.error("Self check: dimension {} has no Twilight Forest provider", TFDimension.id);
            ok = false;
        }

        ok &= checkEntities(server);
        ok &= checkBlockMetadata();

        // self healing: other mods may have appended spawn entries to our biomes after construction
        TFBiomeBase.sanitizeAllSpawnLists();

        failed = !ok;
        if (!ok) {
            TwilightForestMod.LOGGER.error("Twilight Forest self check FAILED: portal creation is disabled");
        }
    }

    // ---- entity names ----

    /**
     * Every registered TF creature must round-trip through EntityList (name -> new instance of the same class -> same name), must
     * have a lang key in en_US and zh_CN, every boss spawner must be able to build its boss, and the named non-living entities
     * (projectiles, parts) must be constructible with a lang key as well.
     */
    private static boolean checkEntities(MinecraftServer server) {
        boolean ok = true;
        World world = server.worldServers != null && server.worldServers.length > 0 ? server.worldServers[0] : null;
        if (world == null) {
            TwilightForestMod.LOGGER.error("Self check: no world to construct entities in");
            return false;
        }
        Set<String> en = langKeys("en_US");
        Set<String> zh = langKeys("zh_CN");

        for (Map.Entry<Class<?>, String> entry : TFCreatures.registeredNames().entrySet()) {
            String full = TFRegistry.NAMESPACE + ":" + entry.getValue();
            Entity entity = null;
            try {
                entity = EntityList.createEntityByName(full, world);
            } catch (RuntimeException e) {
                TwilightForestMod.LOGGER.error("Self check: constructing {} threw", full, e);
            }
            if (entity == null) {
                TwilightForestMod.LOGGER.error("Self check: EntityList.createEntityByName(\"{}\") returned null (class {})", full, entry.getKey().getName());
                ok = false;
            } else {
                if (entity.getClass() != entry.getKey()) {
                    TwilightForestMod.LOGGER.error("Self check: entity name {} creates {} instead of {}", full, entity.getClass().getName(), entry.getKey().getName());
                    ok = false;
                }
                if (!full.equals(EntityList.getEntityString(entity))) {
                    TwilightForestMod.LOGGER.error("Self check: entity {} reports the name {}", full, EntityList.getEntityString(entity));
                    ok = false;
                }
            }
            ok &= checkLangKey(en, "en_US", full);
            ok &= checkLangKey(zh, "zh_CN", full);
        }

        for (Map.Entry<Class<?>, String> entry : TFCreatures.nonLivingNames().entrySet()) {
            String full = TFRegistry.NAMESPACE + ":" + entry.getValue();
            try {
                if (entry.getKey().getConstructors().length > 0 && java.util.Arrays.stream(entry.getKey().getConstructors())
                        .noneMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == World.class)) {
                    // spawned only through its own factory (projectile owner, box bounds, ...), never rebuilt from a (World) ctor
                    ok &= checkLangKey(en, "en_US", full);
                    ok &= checkLangKey(zh, "zh_CN", full);
                    continue;
                }
                Object o = entry.getKey().getConstructor(World.class).newInstance(world);
                if (!(o instanceof Entity)) {
                    throw new IllegalStateException("not an entity");
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                TwilightForestMod.LOGGER.error("Self check: cannot construct {} ({})", entry.getKey().getName(), e.toString());
                ok = false;
            }
            ok &= checkLangKey(en, "en_US", full);
            ok &= checkLangKey(zh, "zh_CN", full);
        }

        for (int meta = BlockTFBossSpawner.META_NAGA; meta <= BlockTFBossSpawner.META_MAX; meta++) {
            TileEntity te = BlockTFBossSpawner.createTileEntityForMeta(meta);
            if (!(te instanceof TileEntityTFBossSpawner spawner) || !spawner.canCreateBoss(world)) {
                TwilightForestMod.LOGGER.error("Self check: boss spawner metadata {} cannot construct its boss", meta);
                ok = false;
            }
        }
        return ok;
    }

    private static boolean checkLangKey(Set<String> keys, String language, String fullEntityName) {
        String key = "entity." + fullEntityName + ".name";
        if (keys.isEmpty() || keys.contains(key)) {
            return true;
        }
        // not fatal: a missing name only shows the raw key
        TwilightForestMod.LOGGER.error("Self check: {}.lang has no key {}", language, key);
        return true;
    }

    /** The keys of a TF lang file in the jar (empty if it cannot be read, which then skips the check instead of failing it). */
    private static Set<String> langKeys(String language) {
        Set<String> keys = new HashSet<>();
        try (InputStream in = TFSelfCheck.class.getResourceAsStream("/assets/" + TwilightForestMod.ID + "/lang/" + language + ".lang")) {
            if (in == null) {
                TwilightForestMod.LOGGER.warn("Self check: {}.lang is not on the class path, skipping its key check", language);
                return keys;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq > 0 && !line.startsWith("#")) {
                    keys.add(line.substring(0, eq).trim());
                }
            }
        } catch (java.io.IOException e) {
            TwilightForestMod.LOGGER.warn("Self check: cannot read {}.lang", language, e);
            keys.clear();
        }
        return keys;
    }

    // ---- block metadata ----

    /**
     * Metadata that TF structure and world generation place literally (found by scanning the generators for "TFBlocks.x, N"; computed
     * metadata is covered at placement time by TFWorldUtil.checkedMeta). Field name of TFBlocks -> metas.
     */
    private static final Map<String, int[]> PLACED_METAS = new LinkedHashMap<>();

    static {
        PLACED_METAS.put("bossSpawner", new int[] {0, 1, 2, 3, 4, 5});
        PLACED_METAS.put("castleDoor", new int[] {0, 1, 2});
        PLACED_METAS.put("castleMagic", new int[] {1, 3});
        PLACED_METAS.put("deadrock", new int[] {0});
        PLACED_METAS.put("firefly", new int[] {0, 1, 2, 3, 4, 5, 6});
        PLACED_METAS.put("cicada", new int[] {0, 1, 2, 3, 4, 5, 6});
        PLACED_METAS.put("fluffyCloud", new int[] {0});
        PLACED_METAS.put("forceField", new int[] {0});
        PLACED_METAS.put("giantCobble", new int[] {0});
        PLACED_METAS.put("giantLeaves", new int[] {0});
        PLACED_METAS.put("giantLog", new int[] {0});
        PLACED_METAS.put("giantObsidian", new int[] {0});
        PLACED_METAS.put("hugeGloomBlock", new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        PLACED_METAS.put("hugeLilyPad", new int[] {0, 1, 2, 3});
        PLACED_METAS.put("hedge", new int[] {0, 1});
        PLACED_METAS.put("leaves3", new int[] {0, 4});
        PLACED_METAS.put("leaves", new int[] {0, 3, 4, 7});
        PLACED_METAS.put("log", new int[] {0, 3, 4, 8, 12});
        PLACED_METAS.put("magicLogSpecial", new int[] {0});
        PLACED_METAS.put("mazestone", new int[] {1, 2, 3});
        PLACED_METAS.put("nagastone", new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        PLACED_METAS.put("plant", new int[] {13});
        PLACED_METAS.put("root", new int[] {0});
        PLACED_METAS.put("shield", new int[] {15});
        PLACED_METAS.put("thorns", new int[] {0, 1});
        PLACED_METAS.put("towerWood", new int[] {1});
        PLACED_METAS.put("trollSteinn", new int[] {0});
        PLACED_METAS.put("trophyPedestal", new int[] {15});
    }

    /**
     * Static table check: (1) every literally placed TF metadata is accepted by its block (logged only), (2) every TF BlockMounted gives
     * a non-null face for every metadata that can exist in a chunk, valid or legacy (MITE's isLegalOn dereferences it; fatal).
     */
    private static boolean checkBlockMetadata() {
        boolean ok = true;
        for (Map.Entry<String, int[]> entry : PLACED_METAS.entrySet()) {
            Block block = tfBlock(entry.getKey());
            if (block == null) {
                TwilightForestMod.LOGGER.error("Self check: TFBlocks.{} does not exist or is not registered", entry.getKey());
                continue;
            }
            for (int meta : entry.getValue()) {
                if (!block.isValidMetadata(meta)) {
                    // not fatal: TFWorldUtil.checkedMeta normalises such metadata when it is placed
                    TwilightForestMod.LOGGER.error("Self check: world generation places {} with metadata {}, which the block rejects", entry.getKey(), meta);
                }
            }
        }
        for (Block block : Block.blocksList) {
            if (block instanceof BlockMounted mounted && block.getClass().getName().startsWith("twilightforest.")) {
                for (int meta = 0; meta < 16; meta++) {
                    if (mounted.getFaceMountedTo(meta) == null) {
                        TwilightForestMod.LOGGER.error("Self check: {} has no mounting face for metadata {}", block.getClass().getSimpleName(), meta);
                        ok = false;
                    }
                }
            }
        }
        return ok;
    }

    private static Block tfBlock(String field) {
        try {
            Field f = TFBlocks.class.getField(field);
            return (Block) f.get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
