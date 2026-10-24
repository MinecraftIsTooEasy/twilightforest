package twilightforest.dimension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Chunk;
import net.minecraft.EntityPlayer;
import net.minecraft.ExtendedBlockStorage;
import net.minecraft.MathHelper;
import net.minecraft.World;
import net.minecraft.WorldServer;
import twilightforest.TFConfig;
import twilightforest.block.TFBlocks;

/**
 * Portal sanctuary (MITE_BALANCE 3.3, portal.sanctuaryRadius): hostile mobs never spawn within the configured radius of a
 * TF portal. Portals are remembered in memory per dimension: new ones are registered when they are made, existing ones are
 * found by a time sliced scan of the loaded chunks around players (a few chunks per call, every chunk once). Entries are
 * validated before use, so a destroyed portal stops protecting at once.
 */
public final class TFPortalSanctuary {
    /** How far (chunks) around a player unscanned chunks are looked at; hostile spawns happen up to 128 blocks away. */
    private static final int SCAN_CHUNK_RADIUS = 8;
    private static final int CHUNKS_PER_CALL = 6;
    private static final int MAX_SCANNED = 60000;

    private static final Map<Integer, Set<Long>> PORTALS = new ConcurrentHashMap<>();
    private static final Map<Integer, Set<Long>> SCANNED = new ConcurrentHashMap<>();

    private TFPortalSanctuary() {
    }

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }

    private static int unpackX(long k) {
        return (int) (k >> 38) << 6 >> 6;
    }

    private static int unpackZ(long k) {
        return (int) (k >> 12) << 6 >> 6;
    }

    private static int unpackY(long k) {
        return (int) (k & 0xFFF);
    }

    private static Set<Long> portals(int dim) {
        return PORTALS.computeIfAbsent(dim, d -> ConcurrentHashMap.newKeySet());
    }

    /** Remember a portal block (any block of the 2x2 pad is enough). */
    public static void register(World world, int x, int y, int z) {
        if (world != null && !world.isRemote) {
            portals(world.provider.dimensionId).add(pack(x, y, z));
        }
    }

    /** Forget everything, called when a server (re)starts so a new save does not inherit portals. */
    public static void reset() {
        PORTALS.clear();
        SCANNED.clear();
    }

    private static boolean isPortal(World world, int x, int y, int z) {
        return TFBlocks.portal != null && world.getBlockId(x, y, z) == TFBlocks.portal.blockID;
    }

    /** True when a hostile spawn at x,y,z is inside a portal sanctuary of this world. */
    public static boolean protects(World world, int x, int y, int z) {
        int radius = TFConfig.portalSanctuaryRadius;
        if (radius <= 0 || world == null || world.isRemote) {
            return false;
        }
        Set<Long> set = PORTALS.get(world.provider.dimensionId);
        if (set == null || set.isEmpty()) {
            return false;
        }
        long r2 = (long) radius * radius;
        for (long key : set) {
            long dx = unpackX(key) - x;
            long dz = unpackZ(key) - z;
            long dy = unpackY(key) - y;
            if (dx * dx + dz * dz + dy * dy <= r2) {
                return true;
            }
        }
        return false;
    }

    /** Called from the player tick (server): scans a few unscanned loaded chunks and drops portals that are gone. */
    public static void tick(EntityPlayer player, long time) {
        if (TFConfig.portalSanctuaryRadius <= 0 || !(player.worldObj instanceof WorldServer world) || TFBlocks.portal == null) {
            return;
        }
        int dim = world.provider.dimensionId;
        Set<Long> scanned = SCANNED.computeIfAbsent(dim, d -> ConcurrentHashMap.newKeySet());
        if (scanned.size() > MAX_SCANNED) {
            scanned.clear();
        }
        int pcx = MathHelper.floor_double(player.posX) >> 4;
        int pcz = MathHelper.floor_double(player.posZ) >> 4;
        int budget = CHUNKS_PER_CALL;
        for (int r = 0; r <= SCAN_CHUNK_RADIUS && budget > 0; r++) {
            for (int cx = pcx - r; cx <= pcx + r && budget > 0; cx++) {
                for (int cz = pcz - r; cz <= pcz + r && budget > 0; cz++) {
                    if (Math.max(Math.abs(cx - pcx), Math.abs(cz - pcz)) != r) {
                        continue;
                    }
                    long ck = ((long) cx << 32) ^ (cz & 0xFFFFFFFFL);
                    if (scanned.contains(ck)) {
                        continue;
                    }
                    Chunk chunk = world.getChunkIfItExists(cx, cz);
                    if (chunk == null) {
                        continue;
                    }
                    scanned.add(ck);
                    scanChunk(world, chunk, cx, cz);
                    budget--;
                }
            }
        }
        if (time % 200 == 0) {
            validate(world);
        }
    }

    private static void scanChunk(WorldServer world, Chunk chunk, int cx, int cz) {
        int id = TFBlocks.portal.blockID;
        byte lsb = (byte) id;
        Set<Long> set = portals(world.provider.dimensionId);
        for (ExtendedBlockStorage section : chunk.getBlockStorageArray()) {
            if (section == null || section.isEmpty()) {
                continue;
            }
            byte[] lsbArray = section.getBlockLSBArray();
            int baseY = section.getYLocation();
            for (int i = 0; i < lsbArray.length; i++) {
                if (lsbArray[i] != lsb) {
                    continue;
                }
                int ly = i >> 8;
                int lz = (i >> 4) & 15;
                int lx = i & 15;
                if (section.getExtBlockID(lx, ly, lz) == id) {
                    set.add(pack((cx << 4) + lx, baseY + ly, (cz << 4) + lz));
                }
            }
        }
    }

    private static void validate(WorldServer world) {
        Set<Long> set = PORTALS.get(world.provider.dimensionId);
        if (set == null) {
            return;
        }
        Iterator<Long> it = set.iterator();
        while (it.hasNext()) {
            long key = it.next();
            int x = unpackX(key);
            int z = unpackZ(key);
            if (world.getChunkIfItExists(x >> 4, z >> 4) != null && !isPortal(world, x, unpackY(key), z)) {
                it.remove();
            }
        }
    }
}
