package twilightforest.client;

import net.minecraft.Minecraft;
import net.minecraft.World;
import twilightforest.dimension.TFDimension;

/**
 * Called at the end of Minecraft.runTick (MinecraftMixin). The Twilight Forest provider also turns the vignette
 * off through WorldProvider.drawGuiVignette(); clearing the brightness here keeps the 1.7.10 behaviour even if
 * another mod re-enables it.
 */
public final class TFClientTicker {
    private TFClientTicker() {
    }

    public static void clientTick(Minecraft mc) {
        World world = mc.theWorld;

        if (world != null && TFDimension.is(world) && mc.ingameGUI != null) {
            mc.ingameGUI.prevVignetteBrightness = 0.0F;
        }
    }
}
