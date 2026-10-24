package twilightforest;

import java.util.List;
import net.minecraft.CommandBase;
import net.minecraft.GameRules;
import net.minecraft.ICommandSender;
import net.minecraft.WrongUsageException;
import twilightforest.compat.TFChat;
import twilightforest.network.TFNetwork;

/**
 * Replacement for /gamerule, which is a stub in MITE: /tfrule enforcedProgression [true|false].
 * Backed by the tfEnforcedProgression game rule (saved with the world) and synced to every client.
 */
public class CommandTFRule extends CommandBase {

    private static final String RULE_NAME = "enforcedProgression";
    private static final String USAGE = "tfrule " + RULE_NAME + " [true|false]";

    @Override
    public String getCommandName() {
        return "tfrule";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return TFEvents.isAdmin(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !RULE_NAME.equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(USAGE);
        }

        GameRules rules = sender.getEntityWorld().getGameRules();
        if (args.length == 1) {
            TFChat.sendTranslated(sender, "tf.cmd.rule.value", RULE_NAME, String.valueOf(TwilightForestMod.isProgressionEnforced(sender.getEntityWorld())));
            return;
        }

        boolean enforced;
        if (args[1].equalsIgnoreCase("true")) {
            enforced = true;
        } else if (args[1].equalsIgnoreCase("false")) {
            enforced = false;
        } else {
            throw new WrongUsageException(USAGE);
        }

        rules.setOrCreateGameRule(TwilightForestMod.ENFORCED_PROGRESSION_RULE, String.valueOf(enforced));
        TFNetwork.sendEnforcedProgressionStatusToAll(enforced);
        TFChat.sendTranslated(sender, "tf.cmd.rule.set", RULE_NAME, String.valueOf(enforced));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, RULE_NAME);
        }
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, "true", "false") : null;
    }
}
