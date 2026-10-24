package twilightforest.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.EntityPlayer;
import net.minecraft.Packet;
import net.minecraft.ServerPlayer;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import net.minecraft.server.MinecraftServer;
import net.xiaoyu233.fml.reload.event.PacketRegisterEvent;

/**
 * Builders and send helpers for {@link TFPacket}; replaces TFGenericPacketHandler's make*Packet methods and the
 * FMLEventChannel send calls. Every send helper is a no-op on the client.
 */
public final class TFNetwork {
    private TFNetwork() {}

    // Discriminators keep the TF 1.7.10 values; 1 (CHANGE_DIM_ID) is dropped because the dimension id is fixed.
    public static final byte TRANSFORM_BIOME = 2;
    public static final byte THROW_PLAYER = 3;
    public static final byte AREA_PROTECTION = 4;
    public static final byte STRUCTURE_PROTECTION = 5;
    public static final byte STRUCTURE_PROTECTION_CLEAR = 6;
    public static final byte ENFORCED_PROGRESSION_STATUS = 7;
    public static final byte ANNIHILATE_BLOCK = 8;
    public static final byte OPEN_GUI = 9;
    public static final byte TEST_MODE = 10;

    public static final double BIOME_CHANGE_RANGE = 128;
    public static final double AREA_PROTECTION_RANGE = 64;
    public static final double ANNIHILATE_RANGE = 64;

    /**
     * Called from TFRegistryHandler's PacketRegisterEvent listener. S2C only. The id comes from FML's allocator: MITE-ITE takes
     * its ids from there too, so a fixed id (140 before) collides with it and the TF packet never got registered.
     */
    public static void register(PacketRegisterEvent event) {
        event.register(true, false, TFPacket.class);
    }

    @FunctionalInterface
    private interface Body {
        void write(DataOutputStream out) throws IOException;
    }

    private static TFPacket build(byte discriminator, Body body) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeByte(discriminator);
            body.write(out);
            out.flush();
            return new TFPacket(bytes.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeBox(DataOutputStream out, StructureBoundingBox sbb) throws IOException {
        out.writeInt(sbb.minX);
        out.writeInt(sbb.minY);
        out.writeInt(sbb.minZ);
        out.writeInt(sbb.maxX);
        out.writeInt(sbb.maxY);
        out.writeInt(sbb.maxZ);
    }

    public static TFPacket makeBiomeChangePacket(int x, int z, byte biomeId) {
        return build(TRANSFORM_BIOME, out -> {
            out.writeInt(x);
            out.writeInt(z);
            out.writeByte(biomeId);
        });
    }

    /** The target entity id is added to the TF 1.7.10 layout because the client has no Minecraft.thePlayer access here. */
    public static TFPacket makeThrowPlayerPacket(int entityId, float motionX, float motionY, float motionZ) {
        return build(THROW_PLAYER, out -> {
            out.writeInt(entityId);
            out.writeFloat(motionX);
            out.writeFloat(motionY);
            out.writeFloat(motionZ);
        });
    }

    public static TFPacket makeAreaProtectionPacket(StructureBoundingBox sbb, int blockX, int blockY, int blockZ) {
        return build(AREA_PROTECTION, out -> {
            writeBox(out, sbb);
            out.writeInt(blockX);
            out.writeInt(blockY);
            out.writeInt(blockZ);
        });
    }

    public static TFPacket makeStructureProtectionPacket(StructureBoundingBox sbb) {
        return build(STRUCTURE_PROTECTION, out -> writeBox(out, sbb));
    }

    public static TFPacket makeStructureProtectionClearPacket() {
        return build(STRUCTURE_PROTECTION_CLEAR, out -> {});
    }

    public static TFPacket makeEnforcedProgressionStatusPacket(boolean isEnforced) {
        return build(ENFORCED_PROGRESSION_STATUS, out -> out.writeBoolean(isEnforced));
    }

    public static TFPacket makeAnnihilateBlockPacket(int blockX, int blockY, int blockZ) {
        return build(ANNIHILATE_BLOCK, out -> {
            out.writeInt(blockX);
            out.writeInt(blockY);
            out.writeInt(blockZ);
        });
    }

    public static TFPacket makeTestModePacket(boolean on) {
        return build(TEST_MODE, out -> out.writeBoolean(on));
    }

    public static TFPacket makeOpenGuiPacket(int windowId, int guiId, int x, int y, int z) {
        return build(OPEN_GUI, out -> {
            out.writeByte(windowId);
            out.writeByte(guiId);
            out.writeInt(x);
            out.writeInt(y);
            out.writeInt(z);
        });
    }

    // ---- sending ----

    public static void sendTo(EntityPlayer player, Packet packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendPacket(packet);
        }
    }

    public static void sendToAll(Packet packet) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            server.getConfigurationManager().sendPacketToAllPlayers(packet);
        }
    }

    /** TargetPoint equivalent; matches the dimension through the player's own dimension field. */
    public static void sendToAllAround(World world, double x, double y, double z, double range, Packet packet) {
        MinecraftServer server = MinecraftServer.getServer();
        if (world.isRemote || server == null) {
            return;
        }
        server.getConfigurationManager().sendToAllNear(x, y, z, range, world.getDimensionId(), packet);
    }

    public static void sendBiomeChange(World world, int x, int z, byte biomeId) {
        sendToAllAround(world, x, 128, z, BIOME_CHANGE_RANGE, makeBiomeChangePacket(x, z, biomeId));
    }

    public static void sendThrowPlayer(EntityPlayer player, float motionX, float motionY, float motionZ) {
        sendTo(player, makeThrowPlayerPacket(player.entityId, motionX, motionY, motionZ));
    }

    public static void sendAreaProtection(World world, StructureBoundingBox sbb, int blockX, int blockY, int blockZ) {
        sendToAllAround(world, blockX, blockY, blockZ, AREA_PROTECTION_RANGE, makeAreaProtectionPacket(sbb, blockX, blockY, blockZ));
    }

    public static void sendStructureProtection(EntityPlayer player, StructureBoundingBox sbb) {
        sendTo(player, makeStructureProtectionPacket(sbb));
    }

    public static void sendStructureProtectionClear(EntityPlayer player) {
        sendTo(player, makeStructureProtectionClearPacket());
    }

    public static void sendEnforcedProgressionStatus(EntityPlayer player, boolean isEnforced) {
        sendTo(player, makeEnforcedProgressionStatusPacket(isEnforced));
    }

    public static void sendEnforcedProgressionStatusToAll(boolean isEnforced) {
        sendToAll(makeEnforcedProgressionStatusPacket(isEnforced));
    }

    public static void sendTestMode(EntityPlayer player, boolean on) {
        sendTo(player, makeTestModePacket(on));
    }

    public static void sendAnnihilateBlock(World world, int blockX, int blockY, int blockZ) {
        sendToAllAround(world, blockX, blockY, blockZ, ANNIHILATE_RANGE, makeAnnihilateBlockPacket(blockX, blockY, blockZ));
    }

    /** Server-side replacement for EntityPlayer.openGui(mod, id, world, x, y, z). */
    public static void openGui(EntityPlayer player, int guiId, int x, int y, int z) {
        if (player instanceof ITFServerPlayer tfPlayer) {
            tfPlayer.tf$openGui(guiId, x, y, z);
        }
    }
}
