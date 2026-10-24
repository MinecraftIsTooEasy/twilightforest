package twilightforest.client;

import net.fabricmc.api.ClientModInitializer;
import twilightforest.TwilightForestMod;

public class TFClientInit implements ClientModInitializer {
    /**
     * Runs before Minecraft is constructed: only swaps the proxy and subscribes the registry to the FML
     * events, the renderers themselves are created when those events fire during startGame.
     */
    @Override
    public void onInitializeClient() {
        TwilightForestMod.proxy = new TFClientProxy();
        TFClientRegistry.register();
    }
}
