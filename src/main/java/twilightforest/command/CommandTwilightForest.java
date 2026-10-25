package twilightforest.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.CommandBase;
import net.minecraft.CommandException;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ICommandSender;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Minecraft;
import net.minecraft.ServerPlayer;
import net.minecraft.WorldServer;
import net.minecraft.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import twilightforest.TFBalance;
import twilightforest.TFConfig;
import twilightforest.TFProgress;
import twilightforest.TFProgressData;
import twilightforest.TFTestMode;
import twilightforest.TFEvents;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFChat;
import twilightforest.dimension.TFDimension;

/**
 * Developer / cheat command: /twilightforest (alias /tf) [latitude] tp &lt;structure|list&gt;, give, dump items|recipes.
 * MITE's own /give is a stub, so give is implemented here as a fallback.
 */
public class CommandTwilightForest extends CommandBase {
    private static final String USAGE = "twilightforest stuck | progress | test <on|off> | [latitude] tp <structure|list> | give <item> [count] [subtype] | dump <items|recipes>";
    /** /tf stuck cooldown: 24 hours of overworld time. */
    private static final long STUCK_COOLDOWN_TICKS = 24L * 60L * 60L * 20L;

    @Override
    public String getCommandName() {
        return "twilightforest";
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList("tf");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Always true so that CommandHandler does not print its generic "no permission" text on top of ours; the real check is
     * {@link #isAllowed} at the start of processCommand, which reports tf.cmd.denied.
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    /** Dev commands: the admin rules of {@link #isAdminAllowed}, or anybody while the world is in /tf test mode. */
    public static boolean isAllowed(ICommandSender sender) {
        return isAdminAllowed(sender) || sender.getEntityWorld() != null && TFTestMode.isOn(sender.getEntityWorld());
    }

    /** Dev mode, console, config override, owner/op (see TFEvents.isAdmin) or a world that allows cheats. */
    public static boolean isAdminAllowed(ICommandSender sender) {
        if (TFConfig.DEV_COMMANDS_ALWAYS.get() || Minecraft.inDevMode()) {
            return true;
        }
        if (!(sender instanceof EntityPlayer)) {
            return true;
        }
        if (TFEvents.isAdmin(sender)) {
            return true;
        }
        try {
            return sender.getEntityWorld().getWorldInfo().areCommandsAllowed();
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // the escape hatch is open to everybody
        if (args.length > 0 && args[0].equalsIgnoreCase("stuck")) {
            stuck(sender);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("progress")) {
            progress(sender);
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            if (!isAdminAllowed(sender)) {
                throw new CommandException("tf.cmd.denied");
            }
            test(sender, args);
            return;
        }
        if (!isAllowed(sender)) {
            throw new CommandException("tf.cmd.denied");
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("latitude")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        if (args.length == 0) {
            throw new WrongUsageException(USAGE);
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "tp" -> teleport(sender, args);
            case "give" -> give(sender, args);
            case "dump" -> dump(sender, args);
            default -> {
                // short form: /tf <structure>
                if (TFStructureLocator.byName(args[0]) != null) {
                    teleport(sender, new String[] {"tp", args[0]});
                } else {
                    throw new WrongUsageException(USAGE);
                }
            }
        }
    }

    // ------------------------------------------------------------------ progress

    /** Lists the progression flags of the sender; open to everybody. */
    private void progress(ICommandSender sender) {
        ServerPlayer player = getCommandSenderAsPlayer(sender);
        StringBuilder done = new StringBuilder();
        for (TFProgress.Flag flag : TFProgress.Flag.values()) {
            if (TFProgress.has(player, flag)) {
                done.append(done.length() == 0 ? "" : ", ").append(flag.key());
            }
        }
        if (done.length() == 0) {
            TFChat.sendTranslated(sender, "tf.cmd.progress.none");
        } else {
            TFChat.sendTranslated(sender, "tf.cmd.progress.list", done.toString());
        }
    }

    // ------------------------------------------------------------------ test

    /** /tf test [on|off]: per world switch that opens every gate of the port (see TFTestMode). */
    private void test(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            TFChat.sendTranslated(sender, TFTestMode.isOn() ? "tf.testmode.status.on" : "tf.testmode.status.off");
            if (TFTestMode.isOn()) {
                TFTestMode.sendBanner(sender);
            }
            return;
        }
        boolean value;
        if (args[1].equalsIgnoreCase("on")) {
            value = true;
        } else if (args[1].equalsIgnoreCase("off")) {
            value = false;
        } else {
            throw new WrongUsageException("twilightforest test <on|off>");
        }
        if (!TFTestMode.set(value)) {
            TFChat.sendTranslated(sender, "tf.testmode.unavailable");
            return;
        }
        TwilightForestMod.LOGGER.info("{} switched Twilight Forest test mode {}", sender.getCommandSenderName(), value ? "on" : "off");
        TFTestMode.announce(value);
    }

    // ------------------------------------------------------------------ stuck

    /** Sends the player from the forest to the global overworld spawn. Once per 24 hours, no safety net for items. */
    private void stuck(ICommandSender sender) {
        ServerPlayer player = getCommandSenderAsPlayer(sender);
        boolean testMode = TFTestMode.isOn(player.worldObj);
        if (!TFConfig.stuckCommand && !testMode) {
            TFChat.sendTranslated(sender, "tf.stuck.disabled");
            return;
        }
        if (!TFDimension.is(player.worldObj)) {
            TFChat.sendTranslated(sender, "tf.stuck.not_in_forest");
            return;
        }
        TFProgressData data = TFProgressData.get();
        long now = TFBalance.overworldTime();
        if (data != null) {
            long last = data.getLastStuck(player.getCommandSenderName());
            if (!testMode && last != Long.MIN_VALUE && now >= last && now - last < STUCK_COOLDOWN_TICKS) {
                long minutes = (STUCK_COOLDOWN_TICKS - (now - last)) / 1200L + 1L;
                TFChat.sendTranslated(sender, "tf.stuck.cooldown", minutes);
                return;
            }
            data.setLastStuck(player.getCommandSenderName(), now);
        }
        TwilightForestMod.LOGGER.info("{} used /tf stuck at {}, {}, {}", player.getCommandSenderName(),
            MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
        TFEvents.noteForestDeathSite(player);
        TFBalance.stuckTransfer = true;
        try {
            player.fallDistance = 0.0F;
            player.travelToDimension(0);
        } finally {
            TFBalance.stuckTransfer = false;
        }
        player.sendChatToPlayer(TFChat.translation("tf.stuck.done").setColor(EnumChatFormatting.YELLOW));
    }

    // ------------------------------------------------------------------ tp

    private void teleport(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException("twilightforest tp <structure|list>");
        }
        if (args[1].equalsIgnoreCase("list")) {
            TFChat.sendTranslated(sender, "tf.cmd.tp.list", String.join(", ", TFStructureLocator.names()));
            return;
        }
        TFFeature target = TFStructureLocator.byName(args[1]);
        if (target == null) {
            TFChat.sendTranslated(sender, "tf.cmd.tp.unknown", args[1]);
            return;
        }
        ServerPlayer player = getCommandSenderAsPlayer(sender);
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer tf = server.worldServerForDimension(TFDimension.id);
        if (tf == null) {
            TFChat.sendTranslated(sender, "tf.cmd.tp.noworld");
            return;
        }

        if (player.dimension != TFDimension.id) {
            // arrive at 0,0 so nether coordinate scaling cannot move us; a command transfer builds no portal, so stand on 0,0 first
            player.setLocationAndAngles(0.0D, player.posY, 0.0D, player.rotationYaw, player.rotationPitch);
            player.travelToDimension(TFDimension.id);
            tf = server.worldServerForDimension(TFDimension.id);
            TFStructureLocator.forceLoad(tf, 0, 0, 1);
            player.fallDistance = 0.0F;
            server.getConfigurationManager().teleportPlayerInsideDimension(player, 0.5D, tf.getTopSolidOrLiquidBlock(0, 0) + 0.1D, 0.5D, true);
        }

        int fromX = MathHelper.floor_double(player.posX);
        int fromZ = MathHelper.floor_double(player.posZ);
        int[] center = TFStructureLocator.findNearest(tf, target, fromX, fromZ, TFStructureLocator.defaultMaxRings());
        if (center == null) {
            TFChat.sendTranslated(sender, "tf.cmd.tp.notfound", target.name);
            return;
        }
        TFStructureLocator.forceLoad(tf, center[0], center[1], 2);
        int[] spot = TFStructureLocator.findSafeSpot(tf, center[0], center[1]);
        if (spot == null) {
            TFChat.sendTranslated(sender, "tf.cmd.tp.nospot", center[0], center[1]);
            return;
        }
        player.fallDistance = 0.0F;
        server.getConfigurationManager().teleportPlayerInsideDimension(player, spot[0] + 0.5D, spot[1], spot[2] + 0.5D, true);
        player.fallDistance = 0.0F;
        TwilightForestMod.LOGGER.info("{} teleported to {} at {}, {}, {}", player.getCommandSenderName(), target.name, spot[0], spot[1], spot[2]);
        TFChat.sendTranslated(sender, "tf.cmd.tp.done", target.name, spot[0], spot[1], spot[2],
            TFStructureLocator.dist(fromX, fromZ, spot[0], spot[2]));
    }

    // ------------------------------------------------------------------ give

    private void give(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException("twilightforest give <item name|id> [count] [subtype]");
        }
        ServerPlayer player = getCommandSenderAsPlayer(sender);
        Item item = TFItemLookup.find(args[1]);
        if (item == null) {
            TFChat.sendTranslated(sender, "tf.cmd.give.unknown", args[1]);
            return;
        }
        int count = args.length > 2 ? parseIntBounded(sender, args[2], 1, 2304) : 1;
        int subtype = args.length > 3 ? parseIntBounded(sender, args[3], 0, 32767) : 0;
        int remaining = count;
        int maxStack = Math.max(1, new ItemStack(item, 1, subtype).getMaxStackSize());
        while (remaining > 0) {
            int n = Math.min(remaining, maxStack);
            player.inventory.addItemStackToInventoryOrDropIt(new ItemStack(item, n, subtype));
            remaining -= n;
        }
        player.inventoryContainer.detectAndSendChanges();
        TFChat.sendTranslated(sender, "tf.cmd.give.done", count, item.itemID + ":" + subtype, player.getCommandSenderName());
    }

    // ------------------------------------------------------------------ dump

    private void dump(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException("twilightforest dump <items|recipes>");
        }
        try {
            if (args[1].equalsIgnoreCase("items")) {
                int rows = TFDumper.dumpItems();
                TFChat.sendTranslated(sender, "tf.cmd.dump.done", TFDumper.ITEMS_FILE, rows);
            } else if (args[1].equalsIgnoreCase("recipes")) {
                int rows = TFDumper.dumpRecipes();
                TFChat.sendTranslated(sender, "tf.cmd.dump.done", TFDumper.RECIPES_FILE, rows);
            } else {
                throw new WrongUsageException("twilightforest dump <items|recipes>");
            }
        } catch (IOException e) {
            TwilightForestMod.LOGGER.warn("dump failed", e);
            TFChat.sendTranslated(sender, "tf.cmd.dump.fail", String.valueOf(e.getMessage()));
        }
    }

    // ------------------------------------------------------------------ completion

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("latitude")) {
            args = Arrays.copyOfRange(args, 1, args.length);
            if (args.length == 0) {
                return null;
            }
        }
        if (args.length == 1) {
            List<String> first = new ArrayList<>(Arrays.asList("stuck", "progress", "test", "tp", "give", "dump", "latitude"));
            first.addAll(TFStructureLocator.names());
            return getListOfStringsFromIterableMatchingLastWord(args, first);
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            switch (sub) {
                case "tp" -> {
                    List<String> opts = new ArrayList<>(TFStructureLocator.names());
                    opts.add("list");
                    return getListOfStringsFromIterableMatchingLastWord(args, opts);
                }
                case "give" -> {
                    return getListOfStringsFromIterableMatchingLastWord(args, TFItemLookup.completionNames());
                }
                case "dump" -> {
                    return getListOfStringsMatchingLastWord(args, "items", "recipes");
                }
                case "test" -> {
                    return getListOfStringsMatchingLastWord(args, "on", "off");
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }
}
