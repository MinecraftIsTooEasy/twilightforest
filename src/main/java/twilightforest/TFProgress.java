package twilightforest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.Achievement;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityPlayer;
import net.minecraft.PlayerStatsHelper;
import net.minecraft.ServerPlayer;
import twilightforest.compat.TFChat;

/**
 * Kill and participation based progression (MITE_BALANCE 5.2, H4, H8). Picking items up grants nothing. A flag also
 * unlocks the matching achievement so the achievement based structure and biome checks keep working.
 */
public final class TFProgress {
    public enum Flag {
        NAGA(null), LICH(NAGA), MINO(LICH), HYDRA(MINO), PEDESTAL(HYDRA), KNIGHTS(PEDESTAL),
        URGHAST(KNIGHTS), YETI(URGHAST), QUEEN(YETI), TROLL(QUEEN);

        public final Flag parent;

        Flag(Flag parent) {
            this.parent = parent;
        }

        public int bit() {
            return 1 << ordinal();
        }

        public String key() {
            return name().toLowerCase();
        }

        public Achievement achievement() {
            return switch (this) {
                case NAGA -> TFAchievementPage.twilightProgressNaga;
                case LICH -> TFAchievementPage.twilightProgressLich;
                case MINO -> TFAchievementPage.twilightProgressLabyrinth;
                case HYDRA -> TFAchievementPage.twilightProgressHydra;
                case PEDESTAL -> TFAchievementPage.twilightProgressTrophyPedestal;
                case KNIGHTS -> TFAchievementPage.twilightProgressKnights;
                case URGHAST -> TFAchievementPage.twilightProgressUrghast;
                case YETI -> TFAchievementPage.twilightProgressYeti;
                case QUEEN -> TFAchievementPage.twilightProgressGlacier;
                case TROLL -> TFAchievementPage.twilightProgressTroll;
            };
        }

        public static Flag byName(String name) {
            for (Flag flag : values()) {
                if (flag.key().equalsIgnoreCase(name)) {
                    return flag;
                }
            }
            return null;
        }
    }

    /** Outcome of a boss death: who got credit and whether this was a repeat kill (everybody already had the flag). */
    public record KillResult(List<EntityPlayer> players, boolean repeat) {
        public int xp(int base) {
            return repeat ? Math.max(0, (int) (base * TFConfig.bossRepeatXpMult)) : base;
        }

        /** Gives every credited player the boss experience (repeat kills pay mult x0.25); bosses report 0 xp themselves. */
        public void awardXp(int base) {
            int xp = xp(base);
            if (xp <= 0) {
                return;
            }
            for (EntityPlayer player : players) {
                player.addExperience(xp);
            }
        }

        public int material(int count) {
            return repeat ? (int) Math.ceil(count * TFConfig.bossRepeatMaterialMult) : count;
        }

        /** Unique items, trophies and charms are only dropped on the first kill. */
        public boolean dropsUniques() {
            return !repeat;
        }
    }

    private TFProgress() {
    }

    public static boolean isExempt(EntityPlayer player) {
        return player.capabilities.isCreativeMode;
    }

    public static boolean has(EntityPlayer player, Flag flag) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        TFProgressData data = TFProgressData.get();
        if (data != null && (data.getBits(player.getCommandSenderName()) & flag.bit()) != 0) {
            return true;
        }
        // flags set before this data existed, or by commands that only touch achievements
        Achievement achievement = flag.achievement();
        return achievement != null && player instanceof ServerPlayer && player.stats != null
                && PlayerStatsHelper.getValue(achievement.statId, player.stats) == 1L;
    }

    /** Grants the flag; refused when the parent flag is missing. */
    public static boolean grant(EntityPlayer player, Flag flag) {
        if (flag.parent != null && !has(player, flag.parent)) {
            return false;
        }
        TFProgressData data = TFProgressData.get();
        if (data == null) {
            return false;
        }
        String name = player.getCommandSenderName();
        data.setBits(name, data.getBits(name) | flag.bit());
        Achievement achievement = flag.achievement();
        if (achievement != null) {
            player.triggerAchievement(achievement);
        }
        return true;
    }

    /** Grants the flag and every parent (OP command). */
    public static void grantChain(EntityPlayer player, Flag flag) {
        if (flag.parent != null) {
            grantChain(player, flag.parent);
        }
        grant(player, flag);
    }

    /** Survival players of the boss's world inside the credit radius. */
    public static List<EntityPlayer> creditedPlayers(Entity boss) {
        List<EntityPlayer> out = new ArrayList<>();
        double radius = TFConfig.progressCreditRadius;
        for (Object o : boss.worldObj.playerEntities) {
            EntityPlayer player = (EntityPlayer) o;
            if (!player.capabilities.isCreativeMode && !player.isDead && player.getDistanceSqToEntity(boss) <= radius * radius) {
                out.add(player);
            }
        }
        return out;
    }

    public static boolean isRepeat(Collection<EntityPlayer> players, Flag flag) {
        if (players.isEmpty()) {
            return false;
        }
        for (EntityPlayer player : players) {
            if (!has(player, flag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Call once when a boss (or the last phantom) dies on the server. Decides first kill or repeat from the flags the
     * credited players hold before this kill, then grants the flag to everybody who can receive it.
     */
    public static KillResult onBossKilled(Entity boss, Flag flag, Achievement... killAchievements) {
        List<EntityPlayer> players = creditedPlayers(boss);
        boolean repeat = isRepeat(players, flag);
        // the plain kill achievements sit between the flags in the achievement tree, so they come before the flag's own
        for (EntityPlayer player : players) {
            for (Achievement achievement : killAchievements) {
                if (achievement != null) {
                    player.triggerAchievement(achievement);
                }
            }
        }
        if (TFConfig.progressByKill) {
            for (EntityPlayer player : players) {
                grant(player, flag);
            }
        }
        return new KillResult(players, repeat);
    }

    /**
     * Level gate of the bosses that start out in the structure (Minoshroom 14, Alpha Yeti 28, the giants 32): true while no
     * player within boss.spawnRange is creative or has the level (base + boss.minLevelOffset). A gated boss neither targets
     * nor takes damage, so low level players can walk past it. Off with boss.minLevelGate=false and in /tf test mode.
     */
    public static boolean levelGated(EntityLiving mob, int baseLevel) {
        net.minecraft.World world = mob.worldObj;
        if (world == null || world.isRemote || !TFConfig.bossMinLevelGate || TFTestMode.isOn(world)) {
            return false;
        }
        int needed = baseLevel + TFConfig.bossMinLevelOffset;
        double range = TFConfig.bossSpawnRange;
        for (Object o : world.playerEntities) {
            EntityPlayer player = (EntityPlayer) o;
            if (player.isDead || player.getDistanceSqToEntity(mob) > range * range) {
                continue;
            }
            if (player.capabilities.isCreativeMode || player.getExperienceLevel() >= needed) {
                return false;
            }
        }
        return true;
    }

    /** Tells the players near a gated boss why it does not react (tf.boss.too_heavy), every 5 seconds. */
    public static void hintGated(EntityLiving mob, int baseLevel) {
        if (mob.ticksExisted % 100 != 0) {
            return;
        }
        int needed = baseLevel + TFConfig.bossMinLevelOffset;
        double range = TFConfig.bossSpawnRange;
        for (Object o : mob.worldObj.playerEntities) {
            EntityPlayer player = (EntityPlayer) o;
            if (player.getDistanceSqToEntity(mob) <= range * range) {
                TFChat.sendTranslated(player, "tf.boss.too_heavy", needed);
            }
        }
    }

    /**
     * Repeat kills of a boss with a treasure chest (MITE_BALANCE 9.5): the chest at the position keeps floor(n / 2) of its
     * n stacks, at least 2 (or all of them when it has fewer). Removes the later slots first.
     */
    public static void halveChest(net.minecraft.World world, int x, int y, int z) {
        net.minecraft.TileEntity te = world.getBlockTileEntity(x, y, z);
        if (!(te instanceof net.minecraft.IInventory inv)) {
            return;
        }
        int filled = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (inv.getStackInSlot(i) != null) {
                filled++;
            }
        }
        int keep = Math.min(filled, Math.max(2, filled / 2));
        for (int i = inv.getSizeInventory() - 1; i >= 0 && filled > keep; i--) {
            if (inv.getStackInSlot(i) != null) {
                inv.setInventorySlotContents(i, null);
                filled--;
            }
        }
    }

    /** Highest level among survival players within range of the point, or -1 when there are none. */
    public static int highestLevelNear(net.minecraft.World world, double x, double y, double z, double range) {
        int best = -1;
        for (Object o : world.playerEntities) {
            EntityPlayer player = (EntityPlayer) o;
            if (player.capabilities.isCreativeMode || player.isDead) {
                continue;
            }
            double dx = player.posX - x;
            double dy = player.posY - y;
            double dz = player.posZ - z;
            if (dx * dx + dy * dy + dz * dz <= range * range) {
                best = Math.max(best, player.getExperienceLevel());
            }
        }
        return best;
    }
}
