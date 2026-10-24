package twilightforest.client;

/**
 * Custom Block.getRenderType() ids. Fixed (not IdUtil) so that this class can be read from common code
 * without initialising anything; they only have to be unique inside the client process.
 * CRITTER is -1: the critters are drawn by their TESRs, MITE skips type -1 in the world renderer.
 */
public final class TFRenderTypes {
    public static final int CRITTER = -1;
    public static final int PLANT = 7700;
    public static final int COMPLEX = 7701;
    public static final int NAGASTONE = 7702;
    public static final int MAGIC_LEAVES = 7703;
    public static final int PEDESTAL = 7704;
    public static final int THORNS = 7705;
    public static final int KNIGHTMETAL = 7706;
    public static final int HUGE_LILY_PAD = 7707;
    public static final int CASTLE_MAGIC = 7708;

    private TFRenderTypes() {
    }
}
