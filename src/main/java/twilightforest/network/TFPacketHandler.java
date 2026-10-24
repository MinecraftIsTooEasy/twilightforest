package twilightforest.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.Chunk;
import net.minecraft.Entity;
import net.minecraft.Packet131MapData;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import twilightforest.TFMazeMapData;
import twilightforest.TFTestMode;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFProtectionBox;
import twilightforest.item.ItemTFMagicMap;
import twilightforest.item.ItemTFMazeMap;
import twilightforest.item.TFItems;

/**
 * Client-side handling of {@link TFPacket}. Only common classes are referenced here: everything that needs the
 * Minecraft instance, GUIs or renderers goes through {@link TwilightForestMod#proxy}.
 */
public final class TFPacketHandler {
    private TFPacketHandler() {}

    public static void handle(byte[] data) {
        if (data.length == 0) {
            return;
        }
        World world = TwilightForestMod.proxy.getClientWorld();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            int discriminator = in.readByte();
            if (discriminator == TFNetwork.TEST_MODE) {
                // the flag is stored statically, so it is valid even while the world is still being set up
                TFTestMode.setClientFlag(in.readBoolean());
                applyClientEnforcement(world);
                return;
            }
            if (world == null) {
                return;
            }
            switch (discriminator) {
                case TFNetwork.TRANSFORM_BIOME -> processTransformBiome(world, in);
                case TFNetwork.THROW_PLAYER -> processThrowPlayer(world, in);
                case TFNetwork.AREA_PROTECTION -> processAreaProtection(world, in);
                case TFNetwork.STRUCTURE_PROTECTION -> processStructureProtection(world, in);
                case TFNetwork.STRUCTURE_PROTECTION_CLEAR -> TwilightForestMod.proxy.setProtectedBox(world, null);
                case TFNetwork.ENFORCED_PROGRESSION_STATUS -> processEnforcedProgression(world, in);
                case TFNetwork.ANNIHILATE_BLOCK -> processAnnihilateBlock(world, in);
                case TFNetwork.OPEN_GUI -> processOpenGui(in);
                default -> TwilightForestMod.LOGGER.warn("Unknown TF packet discriminator {}", discriminator);
            }
        } catch (IOException e) {
            TwilightForestMod.LOGGER.warn("Malformed TF packet", e);
        }
    }

    private static void processTransformBiome(World world, DataInputStream in) throws IOException {
        int x = in.readInt();
        int z = in.readInt();
        byte biomeId = in.readByte();
        // Only sent to players in the tree's dimension, so no dimension check is needed.
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        chunk.getBiomeArray()[(z & 15) << 4 | (x & 15)] = biomeId;
        world.markBlockRangeForRenderUpdate(x, 0, z, x, 255, z);
    }

    private static void processThrowPlayer(World world, DataInputStream in) throws IOException {
        int entityId = in.readInt();
        float motionX = in.readFloat();
        float motionY = in.readFloat();
        float motionZ = in.readFloat();
        Entity target = world.getEntityByID(entityId);
        if (target != null) {
            target.addVelocity(motionX, motionY, motionZ);
        }
    }

    private static void processAreaProtection(World world, DataInputStream in) throws IOException {
        int minX = in.readInt();
        int minY = in.readInt();
        int minZ = in.readInt();
        int maxX = in.readInt();
        int maxY = in.readInt();
        int maxZ = in.readInt();
        int blockX = in.readInt();
        int blockY = in.readInt();
        int blockZ = in.readInt();

        world.addWeatherEffect(new EntityTFProtectionBox(world, minX, minY, minZ, maxX, maxY, maxZ));

        for (int i = 0; i < 20; i++) {
            double d0 = world.rand.nextGaussian() * 0.02D;
            double d1 = world.rand.nextGaussian() * 0.02D;
            double d2 = world.rand.nextGaussian() * 0.02D;
            float dx = blockX + 0.5F + world.rand.nextFloat() - world.rand.nextFloat();
            float dy = blockY + 0.5F + world.rand.nextFloat() - world.rand.nextFloat();
            float dz = blockZ + 0.5F + world.rand.nextFloat() - world.rand.nextFloat();
            TwilightForestMod.proxy.spawnParticle(world, "protection", dx, dy, dz, d0, d1, d2);
        }
    }

    private static void processStructureProtection(World world, DataInputStream in) throws IOException {
        int minX = in.readInt();
        int minY = in.readInt();
        int minZ = in.readInt();
        int maxX = in.readInt();
        int maxY = in.readInt();
        int maxZ = in.readInt();
        TwilightForestMod.proxy.setProtectedBox(world, new StructureBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
    }

    private static void processEnforcedProgression(World world, DataInputStream in) throws IOException {
        boolean enforced = in.readBoolean();
        TFTestMode.setClientServerEnforced(enforced);
        applyClientEnforcement(world);
    }

    /** The client keeps its own GameRules instance, so the rule is mirrored here; test mode masks it so client visuals relax. */
    private static void applyClientEnforcement(World world) {
        if (world == null) {
            return;
        }
        boolean effective = TFTestMode.clientServerEnforced() && !TFTestMode.clientFlag();
        world.getGameRules().setOrCreateGameRule(TwilightForestMod.ENFORCED_PROGRESSION_RULE, Boolean.toString(effective));
    }

    private static void processAnnihilateBlock(World world, DataInputStream in) throws IOException {
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        TwilightForestMod.proxy.doBlockAnnihilateEffect(world, x, y, z);
    }

    private static void processOpenGui(DataInputStream in) throws IOException {
        int windowId = in.readUnsignedByte();
        int guiId = in.readUnsignedByte();
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        TwilightForestMod.proxy.openClientGui(windowId, guiId, x, y, z);
    }

    /**
     * Called by the NetClientHandler.handleMapData mixin; vanilla only accepts Item.map there.
     *
     * @return true when the packet belonged to a TF map and was applied
     */
    public static boolean handleMapData(Packet131MapData packet, World world) {
        if (world == null) {
            return false;
        }
        if (TFItems.magicMap != null && packet.itemID == TFItems.magicMap.itemID) {
            ItemTFMagicMap.getMPMapData(packet.uniqueID, world).updateMPMapData(packet.itemData);
            return true;
        }
        if (TFItems.mazeMap != null && packet.itemID == TFItems.mazeMap.itemID
            || TFItems.oreMap != null && packet.itemID == TFItems.oreMap.itemID) {
            // The map texture is refreshed by TFMazeMapRenderer when it draws, there is no MapItemRenderer hook to call.
            TFMazeMapData data = ItemTFMazeMap.getMPMapData(packet.uniqueID, world);
            data.updateMPMapData(packet.itemData);
            return true;
        }
        return false;
    }
}
