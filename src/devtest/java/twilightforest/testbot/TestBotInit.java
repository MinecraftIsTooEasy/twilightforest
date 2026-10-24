package twilightforest.testbot;

import net.fabricmc.api.ClientModInitializer;

/** Entry point of the dev-only test bot mod. Does nothing unless -Dtwilightforest.testbot=true. */
public class TestBotInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (!TestBot.ENABLED) {
            return;
        }
        // runs before Minecraft is constructed: tee System.out/err and attach the log4j appender before the game's loggers exist
        LogWatch.install();
    }
}
