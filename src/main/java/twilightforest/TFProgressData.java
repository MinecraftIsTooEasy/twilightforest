package twilightforest;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.NBTBase;
import net.minecraft.NBTTagCompound;
import net.minecraft.WorldSavedData;
import net.minecraft.server.MinecraftServer;

/**
 * Per player progression flags (MITE_BALANCE 5.1/5.2), stored server side in the overworld MapStorage. Keys are lower case
 * player names. Flags are granted by kills, never by picking items up.
 */
public class TFProgressData extends WorldSavedData {
    public static final String NAME = "tf_progress";

    /** Read-only statistic bits besides the boss flags. */
    public static final int BIT_PORTAL = 1 << 20;

    private final Map<String, Integer> bits = new HashMap<>();
    private final Map<String, Long> stuckTimes = new HashMap<>();

    public TFProgressData(String name) {
        super(name);
    }

    /** The server's data, created on first use; null when there is no server (client side). */
    public static TFProgressData get() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.worldServers == null || server.worldServers[0] == null) {
            return null;
        }
        net.minecraft.World world = server.worldServers[0];
        TFProgressData data = (TFProgressData) world.mapStorage.loadData(TFProgressData.class, NAME);
        if (data == null) {
            data = new TFProgressData(NAME);
            world.mapStorage.setData(NAME, data);
        }
        return data;
    }

    public int getBits(String player) {
        Integer value = bits.get(player.toLowerCase());
        return value == null ? 0 : value;
    }

    public void setBits(String player, int value) {
        bits.put(player.toLowerCase(), value);
        markDirty();
    }

    /** Overworld time of the last /tf stuck, or Long.MIN_VALUE when never used. */
    public long getLastStuck(String player) {
        Long value = stuckTimes.get(player.toLowerCase());
        return value == null ? Long.MIN_VALUE : value;
    }

    public void setLastStuck(String player, long time) {
        stuckTimes.put(player.toLowerCase(), time);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        bits.clear();
        NBTTagCompound players = tag.getCompoundTag("players");
        for (Object o : players.getTags()) {
            String name = ((NBTBase) o).getName();
            bits.put(name, players.getInteger(name));
        }
        stuckTimes.clear();
        NBTTagCompound stuck = tag.getCompoundTag("stuck");
        for (Object o : stuck.getTags()) {
            String name = ((NBTBase) o).getName();
            stuckTimes.put(name, stuck.getLong(name));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound players = new NBTTagCompound();
        for (Map.Entry<String, Integer> entry : bits.entrySet()) {
            players.setInteger(entry.getKey(), entry.getValue());
        }
        tag.setCompoundTag("players", players);
        NBTTagCompound stuck = new NBTTagCompound();
        for (Map.Entry<String, Long> entry : stuckTimes.entrySet()) {
            stuck.setLong(entry.getKey(), entry.getValue());
        }
        tag.setCompoundTag("stuck", stuck);
    }
}
