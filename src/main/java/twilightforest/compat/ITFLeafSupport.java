package twilightforest.compat;

import net.minecraft.IBlockAccess;

/** Replaces Forge Block.canSustainLeaves overrides (thorns, huge stalk, burnt thorns); queried by {@link TFBlockUtil#canSustainLeaves}. */
public interface ITFLeafSupport {
    boolean canSustainLeaves(IBlockAccess world, int x, int y, int z);
}
