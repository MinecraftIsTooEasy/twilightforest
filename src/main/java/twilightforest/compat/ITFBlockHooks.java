package twilightforest.compat;

import net.minecraft.EntityPlayer;
import net.minecraft.World;

/** Hooks for Forge Block overrides; recognised by the harvest mixin. */
public interface ITFBlockHooks {
    /** Forge Block.removedByPlayer: true means the block handled its own removal and vanilla harvesting must be skipped. */
    default boolean tf$removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        return false;
    }
}
