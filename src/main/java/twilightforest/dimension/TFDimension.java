package twilightforest.dimension;

import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.World;
import net.minecraft.WorldProvider;
import twilightforest.TwilightForestMod;

/** Single source of truth for the Twilight Forest dimension identity and its tunables (server + client, common code). */
public final class TFDimension {
    public static final String KEY = "twilight_forest";
    public static final String NAME = "Twilight Forest";

    /** Slot of the TF world in MinecraftServer.worldServers[] and WorldInfoShared.totalTime[] (0..3 are taken by MITE). */
    public static final int WORLD_INDEX = 4;
    public static final int WORLD_COUNT = WORLD_INDEX + 1;

    /**
     * Must be identical on client and server (MITE has no handshake; Packet1Login/Packet9Respawn only carry the id).
     * Valid range 2..127 (byte on the wire). TFConfig calls {@link #setId(int)} before any world is created.
     */
    public static int id = 7;

    /** Ticks a player has to stand in a TF portal before travelling. MITE's nether portals use 80, TF 1.7.10 was instant. */
    public static int portalDelayTicks = 20;

    /** Replaces SpawnerAnimals' fixed 0.25 hostile spawn modifier of every non-overworld dimension. 0 disables hostile spawning. */
    public static float hostileSpawnRateModifier = 0.25F;

    /** Keep TF ticking and portals working when server.properties has allow-nether=false (MITE ties both to that flag). */
    public static boolean ignoreAllowNether = true;

    public static final String NO_SLEEP_MESSAGE = "The twilight is too restless to sleep in.";

    private TFDimension() {}

    /** Validates and applies the configured id; falls back to the default when it collides with a MITE dimension. */
    public static void setId(int newId) {
        if (newId < 2 || newId > 127) {
            TwilightForestMod.LOGGER.warn("Invalid Twilight Forest dimension id {} (valid: 2..127), keeping {}", newId, id);
            return;
        }
        id = newId;
    }

    public static boolean is(int dimensionId) { return dimensionId == id; }

    public static boolean is(WorldProvider provider) { return provider != null && provider.dimensionId == id; }

    public static boolean is(World world) { return world != null && world.provider.dimensionId == id; }

    /** The configured TF seed, or the world seed when none is set (MITE has no WorldProvider.getSeed hook; see MITE_DIMENSION 3.10). */
    public static long getSeed(World world) {
        String configured = TwilightForestMod.twilightForestSeed;
        if (configured == null || configured.length() == 0) {
            return world.getSeed();
        }
        return configured.hashCode();
    }

    /**
     * Portal block collision entry point (BlockTFPortal.onEntityCollidedWithBlock, both sides). Uses MITE's nether-portal
     * mechanism: Entity.setInPortal flags the entity and Entity.onEntityUpdate performs the travel on a later tick.
     * Players travel both ways, other entities may only leave the forest.
     */
    public static void travel(Entity entity) {
        if (entity == null || entity.ridingEntity != null || entity.riddenByEntity != null) {
            return;
        }
        if (is(entity.worldObj)) {
            sendToOverworld(entity);
        } else if (entity instanceof EntityPlayer) {
            sendToTwilightForest(entity);
        }
    }

    public static void sendToTwilightForest(Entity entity) {
        entity.setInPortal(id);
    }

    public static void sendToOverworld(Entity entity) {
        entity.setInPortal(0);
    }
}
