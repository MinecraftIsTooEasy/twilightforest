package twilightforest;

import java.util.List;
import net.minecraft.ChunkCoordinates;
import net.minecraft.CommandBase;
import net.minecraft.ICommandSender;
import net.minecraft.MathHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.WrongUsageException;
import twilightforest.compat.TFChat;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

/** Debug/admin command: /tffeature info|reactivate|conquer|center. */
public class CommandTFFeature extends CommandBase {

    private static final String USAGE = "tffeature <info|reactivate|conquer|center>";

    @Override
    public String getCommandName() {
        return "tffeature";
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
        if (args.length == 0) {
            throw new WrongUsageException(USAGE);
        }

        if (args[0].equalsIgnoreCase("info")) {
            ServerPlayer player = getCommandSenderAsPlayer(sender);
            int dx = MathHelper.floor_double(player.posX);
            int dy = MathHelper.floor_double(player.posY);
            int dz = MathHelper.floor_double(player.posZ);

            if (!(player.worldObj.provider instanceof WorldProviderTwilightForest provider)) {
                throw new WrongUsageException("tf.cmd.feature.not_in_forest");
            }
            TFFeature nearbyFeature = ((TFWorldChunkManager) provider.worldChunkMgr).getFeatureAt(dx, dz, player.worldObj);
            TFChat.sendTranslated(sender, "tf.cmd.feature.nearest", nearbyFeature.name);

            ChunkProviderTwilightForest chunkProvider = provider.getChunkProvider();
            if (chunkProvider.isBlockInStructureBB(dx, dy, dz)) {
                TFChat.sendTranslated(sender, "tf.cmd.feature.in_structure");
                TFChat.sendTranslated(sender, "tf.cmd.feature.conquer_flag", String.valueOf(chunkProvider.isStructureConquered(dx, dy, dz)));
            } else {
                TFChat.sendTranslated(sender, "tf.cmd.feature.not_in_structure");
            }
        } else if (args[0].equalsIgnoreCase("reactivate")) {
            changeStructureActivity(sender, false);
        } else if (args[0].equalsIgnoreCase("conquer")) {
            changeStructureActivity(sender, true);
        } else if (args[0].equalsIgnoreCase("center")) {
            ServerPlayer player = getCommandSenderAsPlayer(sender);
            if (!(player.worldObj.provider instanceof WorldProviderTwilightForest provider)) {
                throw new WrongUsageException("tf.cmd.feature.not_in_forest");
            }
            int dx = MathHelper.floor_double(player.posX);
            int dz = MathHelper.floor_double(player.posZ);

            ChunkCoordinates cc = TFFeature.getNearestCenterXYZ(dx >> 4, dz >> 4, player.worldObj);
            TFWorldChunkManager wcm = (TFWorldChunkManager) provider.worldChunkMgr;
            boolean inFeatureChunk = wcm.isInFeatureChunk(player.worldObj, dx, dz);

            TFChat.sendTranslated(sender, "tf.cmd.feature.center", cc.posX, cc.posY, cc.posZ);
            TFChat.sendTranslated(sender, "tf.cmd.feature.in_chunk", String.valueOf(inFeatureChunk));
        } else {
            throw new WrongUsageException(USAGE);
        }
    }

    private static void changeStructureActivity(ICommandSender sender, boolean conquered) {
        ServerPlayer player = getCommandSenderAsPlayer(sender);
        int dx = MathHelper.floor_double(player.posX);
        int dy = MathHelper.floor_double(player.posY);
        int dz = MathHelper.floor_double(player.posZ);

        if (!(player.worldObj.provider instanceof WorldProviderTwilightForest provider)) {
            throw new WrongUsageException("tf.cmd.feature.not_in_forest");
        }
        ChunkProviderTwilightForest chunkProvider = provider.getChunkProvider();

        if (chunkProvider.isBlockInStructureBB(dx, dy, dz)) {
            TFChat.sendTranslated(sender, "tf.cmd.feature.conquer_change", String.valueOf(chunkProvider.isStructureConquered(dx, dy, dz)), String.valueOf(conquered));
            chunkProvider.setStructureConquered(dx, dy, dz, conquered);
        } else {
            TFChat.sendTranslated(sender, "tf.cmd.feature.no_structure");
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "info", "reactivate", "conquer", "center") : null;
    }
}
