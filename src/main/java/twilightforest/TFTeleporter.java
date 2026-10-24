package twilightforest;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BiomeGenBase;
import net.minecraft.Chunk;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Entity;
import net.minecraft.ExtendedBlockStorage;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import net.minecraft.WorldServer;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.block.TFBlocks;

/**
 * Finds or builds the TF portal at the destination. Not a subclass of MITE's Teleporter (different signatures and nether portal
 * metadata semantics): twilightforest.mixin.dimension.TeleporterMixin routes every transfer from or to the forest here.
 */
public class TFTeleporter {
    /** Horizontal search radius for an existing portal comes from teleporter.searchRadius (1.7.10 used 200). */
    /** Chunks within this radius of the entity are loaded (and generated) while searching; farther ones only if already loaded. */
    public static int PORTAL_LOAD_RADIUS_CHUNKS = 4;
    /** 1.7.10 clamped new portals to [30, 128 - 10] regardless of world height. */
    public static int PORTAL_MIN_Y = 30;
    public static int PORTAL_MAX_Y = 118;

    protected final WorldServer myWorld;
    protected final Random rand = new Random();

    public TFTeleporter(WorldServer world) {
        this.myWorld = world;
    }

    /**
     * Place an entity in a nearby portal, creating one if necessary. The entity is already in the destination world at the
     * arrival x/z; yaw is the pre-teleport yaw (transferEntityToWorld forces 90 for unknown dimensions).
     * x, y, z are the pre-teleport coordinates handed in by MITE and are not needed.
     */
    public void placeInPortal(Entity entity, double x, double y, double z, float yaw) {
        entity.rotationYaw = yaw;
        if (!this.placeInExistingPortal(entity)) {
            // in enforced progression mode, check the biomes for safety
            if (TwilightForestMod.isProgressionEnforced(entity.worldObj)) {
                int px = MathHelper.floor_double(entity.posX);
                int pz = MathHelper.floor_double(entity.posZ);
                if (!this.isSafeBiomeAt(px, pz, entity)) {
                    TwilightForestMod.LOGGER.info("Portal destination looks unsafe, rerouting!");

                    ChunkCoordinates safeCoords = this.findSafeCoords(200, px, pz, entity);
                    if (safeCoords == null) {
                        TwilightForestMod.LOGGER.info("Did not find a safe spot at first try, trying again with longer range.");
                        safeCoords = this.findSafeCoords(400, px, pz, entity);
                        if (safeCoords != null) {
                            TwilightForestMod.LOGGER.info("Safely rerouted to long range portal. Return trip not guaranteed.");
                        } else {
                            TwilightForestMod.LOGGER.info("Did not find a safe spot.");
                        }
                    } else {
                        TwilightForestMod.LOGGER.info("Safely rerouted!");
                    }

                    if (safeCoords != null) {
                        entity.setLocationAndAngles(safeCoords.posX, entity.posY, safeCoords.posZ, yaw, 0.0F);
                    }
                }
            }

            this.makePortal(entity);
            this.placeInExistingPortal(entity);
        }
    }

    /** Find some safe coords within range of the destination coords, or return null. */
    private ChunkCoordinates findSafeCoords(int range, int x, int z, Entity entity) {
        for (int i = 0; i < 25; i++) {
            int dx = x + (rand.nextInt(range) - rand.nextInt(range));
            int dz = z + (rand.nextInt(range) - rand.nextInt(range));
            if (this.isSafeBiomeAt(dx, dz, entity)) {
                return new ChunkCoordinates(dx, 100, dz);
            }
        }
        return null;
    }

    /** Check if the destination is safe. */
    boolean isSafeBiomeAt(int x, int z, Entity entity) {
        BiomeGenBase biomeAt = myWorld.getBiomeGenForCoords(x, z);

        if (biomeAt instanceof TFBiomeBase && entity instanceof ServerPlayer) {
            return ((TFBiomeBase) biomeAt).doesPlayerHaveRequiredAchievement((ServerPlayer) entity);
        }
        // I guess it's safe, or maybe I just have no way to be sure!
        return true;
    }

    private int portalBlockId() {
        return TFBlocks.portal.blockID;
    }

    /** Nearest TF portal block (lowest block of each vertical run, like 1.7.10) within the search radius, or null. */
    private int[] findNearestPortal(Entity entity) {
        int portalId = this.portalBlockId();
        int radius = TFConfig.teleporterSearchRadius;
        int minChunkX = MathHelper.floor_double(entity.posX - radius) >> 4;
        int maxChunkX = MathHelper.floor_double(entity.posX + radius) >> 4;
        int minChunkZ = MathHelper.floor_double(entity.posZ - radius) >> 4;
        int maxChunkZ = MathHelper.floor_double(entity.posZ + radius) >> 4;
        int entityChunkX = MathHelper.floor_double(entity.posX) >> 4;
        int entityChunkZ = MathHelper.floor_double(entity.posZ) >> 4;

        double bestDistance = -1.0D;
        int[] best = null;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                boolean near = Math.abs(cx - entityChunkX) <= PORTAL_LOAD_RADIUS_CHUNKS && Math.abs(cz - entityChunkZ) <= PORTAL_LOAD_RADIUS_CHUNKS;
                Chunk chunk = near ? myWorld.getChunkFromChunkCoords(cx, cz) : myWorld.getChunkIfItExists(cx, cz);
                if (chunk == null) {
                    continue;
                }

                ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
                for (ExtendedBlockStorage section : sections) {
                    if (section == null || section.isEmpty()) {
                        continue;
                    }
                    int baseY = section.getYLocation();
                    for (int ly = 0; ly < 16; ly++) {
                        for (int lz = 0; lz < 16; lz++) {
                            for (int lx = 0; lx < 16; lx++) {
                                if (section.getExtBlockID(lx, ly, lz) != portalId) {
                                    continue;
                                }
                                int wy = baseY + ly;
                                if (wy > 0 && chunk.getBlockID(lx, wy - 1, lz) == portalId) {
                                    continue; // not the bottom of its run
                                }
                                int wx = (cx << 4) + lx;
                                int wz = (cz << 4) + lz;
                                double dx = (wx + 0.5D) - entity.posX;
                                double dy = (wy + 0.5D) - entity.posY;
                                double dz = (wz + 0.5D) - entity.posZ;
                                double distance = dx * dx + dy * dy + dz * dz;
                                if (Math.abs(dx) <= radius && Math.abs(dz) <= radius && (bestDistance < 0.0D || distance < bestDistance)) {
                                    bestDistance = distance;
                                    best = new int[] {wx, wy, wz};
                                }
                            }
                        }
                    }
                }
            }
        }

        return best;
    }

    public boolean placeInExistingPortal(Entity entity) {
        int[] portal = this.findNearestPortal(entity);
        if (portal == null) {
            return false;
        }

        int px = portal[0];
        int py = portal[1];
        int pz = portal[2];
        double portalX = px + 0.5D;
        double portalY = py + 0.5D;
        double portalZ = pz + 0.5D;
        if (isBlockPortal(myWorld, px - 1, py, pz)) {
            portalX -= 0.5D;
        }
        if (isBlockPortal(myWorld, px + 1, py, pz)) {
            portalX += 0.5D;
        }
        if (isBlockPortal(myWorld, px, py, pz - 1)) {
            portalZ -= 0.5D;
        }
        if (isBlockPortal(myWorld, px, py, pz + 1)) {
            portalZ += 0.5D;
        }
        int xOffset = 0;
        int zOffset = 0;
        while (xOffset == 0 && zOffset == 0) {
            xOffset = rand.nextInt(3) - rand.nextInt(3);
            zOffset = rand.nextInt(3) - rand.nextInt(3);
        }
        entity.setLocationAndAngles(portalX + xOffset, portalY + 1, portalZ + zOffset, entity.rotationYaw, 0.0F);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
        entity.fallDistance = 0.0F;
        return true;
    }

    /** Is this block our portal? */
    public boolean isBlockPortal(World world, int x, int y, int z) {
        return world.getBlockId(x, y, z) == this.portalBlockId();
    }

    public boolean makePortal(Entity entity) {
        ChunkCoordinates spot = findPortalCoords(entity, true);

        if (spot != null) {
            TwilightForestMod.LOGGER.info("Found ideal portal spot");
            makePortalAt(myWorld, spot.posX, spot.posY, spot.posZ);
            return true;
        }

        TwilightForestMod.LOGGER.info("Did not find ideal portal spot, shooting for okay one");
        spot = findPortalCoords(entity, false);
        if (spot != null) {
            TwilightForestMod.LOGGER.info("Found okay portal spot");
            makePortalAt(myWorld, spot.posX, spot.posY, spot.posZ);
            return true;
        }

        // we can't just fail here
        TwilightForestMod.LOGGER.info("Did not even find an okay portal spot, just making a random one");

        // adjust the portal height based on what world we're traveling to
        double yFactor = myWorld.provider.dimensionId == 0 ? 2 : 0.5;
        int entityX = MathHelper.floor_double(entity.posX);
        int entityY = MathHelper.floor_double(entity.posY * yFactor);
        int entityZ = MathHelper.floor_double(entity.posZ);

        makePortalAt(myWorld, entityX, entityY, entityZ);

        return false;
    }

    public ChunkCoordinates findPortalCoords(Entity entity, boolean ideal) {
        // adjust the portal height based on what world we're traveling to
        double yFactor = myWorld.provider.dimensionId == 0 ? 2 : 0.5;
        int entityX = MathHelper.floor_double(entity.posX);
        int entityZ = MathHelper.floor_double(entity.posZ);

        double spotWeight = -1D;
        ChunkCoordinates spot = null;

        byte range = 16;
        for (int rx = entityX - range; rx <= entityX + range; rx++) {
            double xWeight = (rx + 0.5D) - entity.posX;
            for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
                double zWeight = (rz + 0.5D) - entity.posZ;

                for (int ry = 128 - 1; ry >= 0; ry--) {
                    if (!myWorld.isAirBlock(rx, ry, rz)) {
                        continue;
                    }
                    for (; ry > 0 && myWorld.isAirBlock(rx, ry - 1, rz); ry--) { }

                    if (ideal ? isIdealPortal(rx, rz, ry) : isOkayPortal(rx, rz, ry)) {
                        double yWeight = (ry + 0.5D) - entity.posY * yFactor;
                        double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;

                        if (spotWeight < 0.0D || rPosWeight < spotWeight) {
                            spotWeight = rPosWeight;
                            spot = new ChunkCoordinates(rx, ry, rz);
                        }
                    }
                }
            }
        }

        return spot;
    }

    public boolean isIdealPortal(int rx, int rz, int ry) {
        for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
            for (int potentialX = 0; potentialX < 4; potentialX++) {
                for (int potentialY = -1; potentialY < 3; potentialY++) {
                    int tx = rx + (potentialX - 1);
                    int ty = ry + potentialY;
                    int tz = rz + (potentialZ - 1);
                    Material material = myWorld.getBlockMaterial(tx, ty, tz);
                    if (potentialY == -1 && material != Material.grass || potentialY >= 0 && !material.isReplaceable()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isOkayPortal(int rx, int rz, int ry) {
        for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
            for (int potentialX = 0; potentialX < 4; potentialX++) {
                for (int potentialY = -1; potentialY < 3; potentialY++) {
                    int tx = rx + (potentialX - 1);
                    int ty = ry + potentialY;
                    int tz = rz + (potentialZ - 1);
                    Material material = myWorld.getBlockMaterial(tx, ty, tz);
                    if (potentialY == -1 && !material.isSolid() || potentialY >= 0 && !material.isReplaceable()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void makePortalAt(World world, int px, int py, int pz) {
        if (py < PORTAL_MIN_Y) {
            py = PORTAL_MIN_Y;
        }
        if (py > PORTAL_MAX_Y) {
            py = PORTAL_MAX_Y;
        }

        // sink the portal 1 into the ground
        py--;

        int grass = Block.grass.blockID;
        int portal = this.portalBlockId();

        // grass all around it
        world.setBlock(px - 1, py, pz - 1, grass);
        world.setBlock(px + 0, py, pz - 1, grass);
        world.setBlock(px + 1, py, pz - 1, grass);
        world.setBlock(px + 2, py, pz - 1, grass);

        world.setBlock(px - 1, py, pz + 0, grass);
        world.setBlock(px + 2, py, pz + 0, grass);

        world.setBlock(px - 1, py, pz + 1, grass);
        world.setBlock(px + 2, py, pz + 1, grass);

        world.setBlock(px - 1, py, pz + 2, grass);
        world.setBlock(px + 0, py, pz + 2, grass);
        world.setBlock(px + 1, py, pz + 2, grass);
        world.setBlock(px + 2, py, pz + 2, grass);

        // dirt under it
        int dirt = Block.dirt.blockID;
        world.setBlock(px + 0, py - 1, pz + 0, dirt);
        world.setBlock(px + 1, py - 1, pz + 0, dirt);
        world.setBlock(px + 0, py - 1, pz + 1, dirt);
        world.setBlock(px + 1, py - 1, pz + 1, dirt);

        // portal in it
        world.setBlock(px + 0, py, pz + 0, portal, 0, 2);
        world.setBlock(px + 1, py, pz + 0, portal, 0, 2);
        world.setBlock(px + 0, py, pz + 1, portal, 0, 2);
        world.setBlock(px + 1, py, pz + 1, portal, 0, 2);
        twilightforest.dimension.TFPortalSanctuary.register(world, px, py, pz);

        // a bunch of air over it for 4 squares
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                for (int dy = 1; dy <= 5; dy++) {
                    world.setBlock(px + dx, py + dy, pz + dz, 0);
                }
            }
        }

        // finally, "nature decorations"!
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                boolean inner = dx >= 0 && dx <= 1 && dz >= 0 && dz <= 1;
                if (!inner) {
                    placeNatureBlock(world, px + dx, py + 1, pz + dz);
                }
            }
        }
    }

    private void placeNatureBlock(World world, int x, int y, int z) {
        int[][] choices = {
            {Block.mushroomBrown.blockID, 0},
            {Block.mushroomRed.blockID, 0},
            {Block.tallGrass.blockID, 1},
            {Block.plantRed.blockID, 0},
            {Block.plantYellow.blockID, 0}
        };
        int[] choice = choices[world.rand.nextInt(choices.length)];
        world.setBlock(x, y, z, choice[0], choice[1], 2);
    }
}
