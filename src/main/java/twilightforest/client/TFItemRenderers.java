package twilightforest.client;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.Item;
import net.minecraft.ItemMap;
import net.minecraft.ItemStack;
import twilightforest.TwilightForestMod;

/** Replacement for MinecraftForgeClient.registerItemRenderer / getItemRenderer. */
public final class TFItemRenderers {
    private static final Map<Item, TFItemRenderer> RENDERERS = new IdentityHashMap<>();

    private TFItemRenderers() {
    }

    public static void register(Item item, TFItemRenderer renderer) {
        if (item == null) {
            TwilightForestMod.LOGGER.error("Not registering an item renderer for a null item (item was not created?)", new NullPointerException());
            return;
        }
        RENDERERS.put(item, renderer);
    }

    public static TFItemRenderer get(ItemStack stack) {
        return stack == null ? null : RENDERERS.get(stack.getItem());
    }

    /** Equivalent of MinecraftForgeClient.getItemRenderer(stack, type): the renderer if it handles the type. */
    public static TFItemRenderer get(ItemStack stack, TFItemRenderType type) {
        TFItemRenderer renderer = get(stack);
        return renderer != null && renderer.handles(stack, type) ? renderer : null;
    }

    /** True for ItemMap subclasses whose renderer takes over the first-person map view. */
    public static boolean handlesFirstPersonMap(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemMap && get(stack, TFItemRenderType.FIRST_PERSON_MAP) != null;
    }
}
