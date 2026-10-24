package twilightforest.compat;

import net.minecraft.EnumFace;

/** Replacement for Forge's ForgeDirection. Ordinals match 1.7.10, {@code net.minecraft.Facing} arrays and {@link EnumFace}. */
public enum ForgeDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;

    public static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};

    ForgeDirection(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.flag = 1 << this.ordinal();
    }

    public static ForgeDirection getOrientation(int id) {
        return id >= 0 && id < VALID_DIRECTIONS.length ? VALID_DIRECTIONS[id] : UNKNOWN;
    }

    public ForgeDirection getOpposite() {
        return getOrientation(OPPOSITES[this.ordinal()]);
    }

    /** MITE face of this direction, or null for UNKNOWN. */
    public EnumFace toEnumFace() {
        return this == UNKNOWN ? null : EnumFace.get(this.ordinal());
    }

    public static ForgeDirection fromEnumFace(EnumFace face) {
        return face == null ? UNKNOWN : VALID_DIRECTIONS[face.ordinal()];
    }
}
