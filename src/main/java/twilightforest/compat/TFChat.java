package twilightforest.compat;

import net.minecraft.ChatMessageComponent;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ICommandSender;

/** Chat helpers replacing ChatComponentText / ChatComponentTranslation / addChatMessage(IChatComponent). */
public final class TFChat {
    private TFChat() {
    }

    /** Literal text component (ChatComponentText). */
    public static ChatMessageComponent text(String text) {
        return ChatMessageComponent.createFromText(text);
    }

    /** Translation component with %s substitutions (ChatComponentTranslation); an unknown key is shown as given. */
    public static ChatMessageComponent translation(String key, Object... args) {
        return args.length == 0 ? ChatMessageComponent.createFromTranslationKey(key)
            : ChatMessageComponent.createFromTranslationWithSubstitutions(key, args);
    }

    /** player.addChatMessage(new ChatComponentText(text)). */
    public static void send(ICommandSender to, String text) {
        to.sendChatToPlayer(text(text));
    }

    public static void send(ICommandSender to, String text, EnumChatFormatting color) {
        to.sendChatToPlayer(text(text).setColor(color));
    }

    /** player.addChatMessage(new ChatComponentTranslation(key, args)). */
    public static void sendTranslated(ICommandSender to, String key, Object... args) {
        to.sendChatToPlayer(translation(key, args));
    }
}
