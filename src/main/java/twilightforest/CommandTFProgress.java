package twilightforest;

import java.util.List;
import net.minecraft.CommandBase;
import net.minecraft.ICommandSender;
import net.minecraft.ServerPlayer;
import net.minecraft.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import twilightforest.compat.TFChat;

public class CommandTFProgress extends CommandBase {

    private static final String[] BOSSES = {"none", "naga", "lich", "mooshroom", "hydra", "knights", "urghast", "yeti", "snowqueen", "giants", "final"};

    @Override
    public String getCommandName() {
        return "tfprogress";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "tfprogress <player> <boss>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /** MITE has no op list, so the permission level alone would lock everyone out; see TFEvents.isAdmin. */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return TFEvents.isAdmin(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        ServerPlayer player = getPlayer(sender, args[0]);
        int bossIndex = getBossIndex(args[1]);
        if (bossIndex < 0) {
            throw new WrongUsageException("tfprogress <player> <" + String.join("|", BOSSES) + ">");
        }

        TFChat.sendTranslated(sender, "tf.cmd.progress.set", player.getCommandSenderName(), BOSSES[bossIndex]);
        setProgress(player, bossIndex);
    }

    /**
     * Grants the progress flag of the boss with TFProgress.grantChain (flag, every parent flag and their achievements). The
     * pure kill achievements that sit between two flags in the achievement tree are triggered first, in tree order.
     */
    private static void setProgress(ServerPlayer player, int bossIndex) {
        // give previous achievements first
        for (int i = 0; i < bossIndex; i++) {
            setProgress(player, i);
        }

        switch (bossIndex) {
            case 1 -> {
                player.triggerAchievement(TFAchievementPage.twilightPortal);
                player.triggerAchievement(TFAchievementPage.twilightArrival);
                player.triggerAchievement(TFAchievementPage.twilightHunter);
                player.triggerAchievement(TFAchievementPage.twilightKillNaga);
                TFProgress.grantChain(player, TFProgress.Flag.NAGA);
            }
            case 2 -> {
                player.triggerAchievement(TFAchievementPage.twilightKillLich);
                TFProgress.grantChain(player, TFProgress.Flag.LICH);
            }
            case 3 -> TFProgress.grantChain(player, TFProgress.Flag.MINO);
            case 4 -> {
                player.triggerAchievement(TFAchievementPage.twilightKillHydra);
                TFProgress.grantChain(player, TFProgress.Flag.HYDRA);
            }
            case 5 -> TFProgress.grantChain(player, TFProgress.Flag.KNIGHTS);
            case 6 -> TFProgress.grantChain(player, TFProgress.Flag.URGHAST);
            case 7 -> TFProgress.grantChain(player, TFProgress.Flag.YETI);
            case 8 -> TFProgress.grantChain(player, TFProgress.Flag.QUEEN);
            case 9 -> TFProgress.grantChain(player, TFProgress.Flag.TROLL);
            case 10 -> {
                TFProgress.grantChain(player, TFProgress.Flag.TROLL);
                player.triggerAchievement(TFAchievementPage.twilightProgressThorns);
                player.triggerAchievement(TFAchievementPage.twilightProgressCastle);
            }
            default -> { }
        }
    }

    private static int getBossIndex(String name) {
        for (int i = 0; i < BOSSES.length; i++) {
            if (BOSSES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, BOSSES) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}
