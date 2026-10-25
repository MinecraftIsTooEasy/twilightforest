package twilightforest.compat;

/**
 * Set while ContainerEnchantment.calcEnchantmentLevelsForSlot runs. Other mods' enchanting table hooks (beyond-extreme)
 * read item.getHardestMetalMaterial().durability without a null check, which crashed on enchantable items that are not
 * made of metal (giant pickaxe, naga/yeti/arctic armor, ice and glass swords); see ItemHardestMetalMixin.
 */
public final class TFEnchantGuard {
    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private TFEnchantGuard() {
    }

    public static void enter() {
        DEPTH.get()[0]++;
    }

    public static void exit() {
        int[] depth = DEPTH.get();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    public static boolean active() {
        return DEPTH.get()[0] > 0;
    }
}
