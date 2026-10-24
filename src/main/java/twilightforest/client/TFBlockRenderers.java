package twilightforest.client;

import java.util.Arrays;

public final class TFBlockRenderers {
    private static TFBlockRenderer[] byType = new TFBlockRenderer[0];

    private TFBlockRenderers() {
    }

    /** Replacement for RenderingRegistry.registerBlockHandler; renderType is one of {@link TFRenderTypes}. */
    public static void register(int renderType, TFBlockRenderer renderer) {
        if (renderType < 0) {
            throw new IllegalArgumentException("render type must not be negative: " + renderType);
        }
        if (renderType >= byType.length) {
            byType = Arrays.copyOf(byType, renderType + 1);
        }
        byType[renderType] = renderer;
    }

    public static TFBlockRenderer get(int renderType) {
        TFBlockRenderer[] types = byType;
        return renderType >= 0 && renderType < types.length ? types[renderType] : null;
    }
}
