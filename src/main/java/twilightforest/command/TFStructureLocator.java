package twilightforest.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.ChunkCoordinates;
import net.minecraft.World;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFBlockUtil;

/** Finds the nearest major feature of a type on the TF feature grid and picks a safe standing spot. */
public final class TFStructureLocator {
    private TFStructureLocator() {}

    /** Canonical (tab-completed) name -> feature. Insertion order is the listing order. */
    private static final Map<String, TFFeature> NAMES = new LinkedHashMap<>();
    /** Normalised alias -> feature. */
    private static final Map<String, TFFeature> ALIASES = new LinkedHashMap<>();

    private static void add(TFFeature f, String canonical, String... aliases) {
        NAMES.put(canonical, f);
        ALIASES.put(norm(canonical), f);
        for (String a : aliases) {
            ALIASES.put(norm(a), f);
        }
    }

    static {
        add(TFFeature.hill1, "hollow_hill1", "hill1", "small_hollow_hill", "small_hill", "hollowhill1");
        add(TFFeature.hill2, "hollow_hill2", "hill2", "medium_hollow_hill", "medium_hill");
        add(TFFeature.hill3, "hollow_hill3", "hill3", "large_hollow_hill", "large_hill");
        add(TFFeature.hedgeMaze, "hedge_maze", "hedge", "hedgemaze");
        add(TFFeature.nagaCourtyard, "naga_courtyard", "naga", "nagacourtyard");
        add(TFFeature.lichTower, "lich_tower", "lich", "lichtower");
        add(TFFeature.iceTower, "ice_tower", "icetower");
        add(TFFeature.iceTower, "aurora_palace", "aurora");
        add(TFFeature.questIsland, "quest_island", "questisland");
        add(TFFeature.questGrove, "quest_grove", "questgrove");
        add(TFFeature.druidGrove, "druid_grove", "druidgrove");
        add(TFFeature.floatRuins, "floating_ruins", "float_ruins", "floatruins");
        add(TFFeature.hydraLair, "hydra_lair", "hydra", "hydralair");
        add(TFFeature.labyrinth, "labyrinth", "maze");
        add(TFFeature.labyrinth, "minotaur_maze", "minotaurmaze", "minotaur");
        add(TFFeature.darkTower, "dark_tower", "darktower");
        add(TFFeature.tfStronghold, "knight_stronghold", "stronghold", "tfstronghold", "goblin_stronghold");
        add(TFFeature.worldTree, "world_tree", "worldtree");
        add(TFFeature.yetiCave, "yeti_cave", "yeti_lairs", "yeti", "yeticave");
        add(TFFeature.trollCave, "troll_cave", "troll_lairs", "troll", "trollcave");
        add(TFFeature.finalCastle, "final_castle", "castle", "finalcastle");
        add(TFFeature.mushroomTower, "mushroom_tower", "mushroomtower");
    }

    private static String norm(String s) {
        return s.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }

    public static List<String> names() {
        return new ArrayList<>(NAMES.keySet());
    }

    public static TFFeature byName(String name) {
        return name == null ? null : ALIASES.get(norm(name));
    }

    /** Grid cell size in chunks (the 1.7 grid is 16 chunks; the old map gen used 256). */
    private static int step() {
        return TwilightForestMod.oldMapGen ? 256 : 16;
    }

    private static int regionOf(int chunk) {
        return TwilightForestMod.oldMapGen ? Math.round(chunk / 256.0F) : (chunk + 8) >> 4;
    }

    /**
     * Searches rings of feature cells outward from the block position and returns {centerX, centerZ} of the nearest
     * instance of {@code target}, or null when none is found within {@code maxRings} cells.
     */
    public static int[] findNearest(World tfWorld, TFFeature target, int fromX, int fromZ, int maxRings) {
        int baseX = regionOf(fromX >> 4);
        int baseZ = regionOf(fromZ >> 4);
        int step = step();
        double bestDist = Double.MAX_VALUE;
        int[] best = null;
        int foundRing = -1;
        for (int r = 0; r <= maxRings; r++) {
            if (foundRing >= 0 && r > foundRing + 1) {
                break; // a diagonal cell one ring further out can still be closer, nothing beyond that can
            }
            for (int rx = -r; rx <= r; rx++) {
                for (int rz = -r; rz <= r; rz++) {
                    if (Math.max(Math.abs(rx), Math.abs(rz)) != r) {
                        continue;
                    }
                    ChunkCoordinates c = TFFeature.getNearestCenterXYZ((baseX + rx) * step, (baseZ + rz) * step, tfWorld);
                    TFFeature at = TFFeature.generateFeatureFor1Point7(c.posX >> 4, c.posZ >> 4, tfWorld);
                    if (at != target) {
                        continue;
                    }
                    double dx = c.posX - fromX;
                    double dz = c.posZ - fromZ;
                    double d = dx * dx + dz * dz;
                    if (d < bestDist) {
                        bestDist = d;
                        best = new int[] {c.posX, c.posZ};
                        if (foundRing < 0) {
                            foundRing = r;
                        }
                    }
                }
            }
        }
        return best;
    }

    public static int defaultMaxRings() {
        return TwilightForestMod.oldMapGen ? 8 : 48;
    }

    /** Loads (and thereby generates) the chunks around a block position so the structure exists and is populated. */
    public static void forceLoad(World world, int x, int z, int radiusChunks) {
        int cx = x >> 4;
        int cz = z >> 4;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                world.getChunkFromChunkCoords(cx + dx, cz + dz);
            }
        }
    }

    /**
     * Highest column near (x, z) with a solid, non-leaf, non-liquid floor and two free blocks above it.
     * @return {x, y, z} where y is the feet position, or null when nothing within 10 blocks qualifies.
     */
    public static int[] findSafeSpot(World world, int x, int z) {
        for (int radius = 0; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int y = safeY(world, x + dx, z + dz);
                    if (y > 0) {
                        return new int[] {x + dx, y, z + dz};
                    }
                }
            }
        }
        return null;
    }

    private static int safeY(World world, int x, int z) {
        for (int y = Math.min(world.getHeight() - 3, 250); y > 1; y--) {
            int id = world.getBlockId(x, y, z);
            if (id == 0) {
                continue;
            }
            Block b = Block.blocksList[id];
            Material m = world.getBlockMaterial(x, y, z);
            if (b == null || m == null || !m.isSolid() || m.isLiquid() || TFBlockUtil.isLeaves(b)) {
                if (m != null && m.isLiquid()) {
                    return -1; // water or lava surface, try another column
                }
                continue;
            }
            if (free(world, x, y + 1, z) && free(world, x, y + 2, z)) {
                return y + 1;
            }
            // solid but cramped (a ceiling): keep scanning downwards for the next floor
        }
        return -1;
    }

    private static boolean free(World world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (id == 0) {
            return true;
        }
        Material m = world.getBlockMaterial(x, y, z);
        return m != null && !m.isSolid() && !m.isLiquid();
    }

    /** Convenience for messages. */
    public static int dist(int x1, int z1, int x2, int z2) {
        return MathHelper.floor_double(Math.sqrt((double) (x1 - x2) * (x1 - x2) + (double) (z1 - z2) * (z1 - z2)));
    }
}
