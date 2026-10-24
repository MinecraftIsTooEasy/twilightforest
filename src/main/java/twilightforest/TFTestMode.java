package twilightforest;

import net.minecraft.EntityPlayer;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ICommandSender;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import net.minecraft.WorldSavedData;
import net.minecraft.server.MinecraftServer;
import twilightforest.compat.TFChat;
import twilightforest.network.TFNetwork;

/**
 * Per world test switch (/tf test on|off). While on, every gate of the port is open: portal conditions, boss level gates,
 * enforced progression and biome penalties, structure protection, the uncrafting whitelist and the spawner exhaustion cap.
 * Stored in the overworld MapStorage so it survives restarts and applies to every dimension of the save.
 * Gates are server side; the flag is mirrored to clients (login, toggle, dimension change, respawn) so client-only effects
 * such as the biome penalty weather follow it.
 */
public class TFTestMode extends WorldSavedData {
    public static final String NAME = "tf_testmode";

    private boolean on;

    /** Client mirror of the flag, set by TFPacketHandler. */
    private static volatile boolean clientOn;
    /** Last progression state the server announced to this client, before test mode masks it. */
    private static volatile Boolean clientServerEnforced;

    public TFTestMode(String name) {
        super(name);
    }

    private static TFTestMode data() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.worldServers == null || server.worldServers[0] == null) {
            return null;
        }
        net.minecraft.World overworld = server.worldServers[0];
        if (overworld.mapStorage == null) {
            return null;
        }
        TFTestMode data = (TFTestMode) overworld.mapStorage.loadData(TFTestMode.class, NAME);
        if (data == null) {
            data = new TFTestMode(NAME);
            overworld.mapStorage.setData(NAME, data);
        }
        return data;
    }

    /** True when the test switch of the world's save is on. On the client this is the mirrored server flag. */
    public static boolean isOn(World world) {
        if (world != null && world.isRemote) {
            return clientOn;
        }
        return isOn();
    }

    /** Same without a world (server wide helpers such as the spawner cap). */
    public static boolean isOn() {
        TFTestMode data = data();
        return data != null && data.on;
    }

    public static boolean set(boolean value) {
        TFTestMode data = data();
        if (data == null) {
            return false;
        }
        data.on = value;
        data.markDirty();
        return true;
    }

    /** Client side: the server told us the flag. */
    public static void setClientFlag(boolean value) {
        clientOn = value;
    }

    public static boolean clientFlag() {
        return clientOn;
    }

    public static void setClientServerEnforced(boolean value) {
        clientServerEnforced = value;
    }

    /** Last announced progression state; falls back to the local config until the server said something. */
    public static boolean clientServerEnforced() {
        Boolean known = clientServerEnforced;
        return known != null ? known : TFConfig.progressEnforced();
    }

    /** Sends the current flag to one player (login, dimension change, respawn). */
    public static void syncTo(EntityPlayer player) {
        TFNetwork.sendTestMode(player, isOn());
    }

    /** Red chat banner, shown on login and on every toggle while the switch is on. */
    public static void sendBanner(ICommandSender to) {
        to.sendChatToPlayer(TFChat.translation("tf.testmode.banner").setColor(EnumChatFormatting.RED));
    }

    /** Login hook: remind the player when the world is in test mode. */
    public static void onLogin(EntityPlayer player) {
        syncTo(player);
        if (isOn()) {
            sendBanner(player);
        }
    }

    /** Toggle feedback to every player online: the banner when switched on, a green note when switched off. */
    public static void announce(boolean value) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        TFNetwork.sendToAll(TFNetwork.makeTestModePacket(value));
        for (Object o : server.getConfigurationManager().playerEntityList) {
            EntityPlayer player = (EntityPlayer) o;
            if (value) {
                sendBanner(player);
            } else {
                player.sendChatToPlayer(TFChat.translation("tf.testmode.off").setColor(EnumChatFormatting.GREEN));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        on = tag.getBoolean("on");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("on", on);
    }
}
