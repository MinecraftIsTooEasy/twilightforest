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
        allowWorkerMeshing();
    }

    /**
     * FML's Vulkan renderer meshes chunk sections on worker threads, but only for vanilla blocks unless a mod opts in; every section
     * holding a TF block (leaves, logs, plants: nearly all of the dimension) was meshed on the render thread, so turning the camera
     * dropped to ~60 fps. The TF block renderers only read the IBlockAccess they are handed and write through Tessellator.instance
     * (per thread on that renderer), so they are safe there. Read when the renderer starts, which is after this entrypoint.
     */
    private static void allowWorkerMeshing() {
        String key = "fml.vulkan.meshAllowPackages";
        String current = System.getProperty(key, "");
        if (!(',' + current + ',').contains(",twilightforest.,")) {
            System.setProperty(key, current.isEmpty() ? "twilightforest." : current + ",twilightforest.");
        }
    }
}
