package twilightforest.network;

/**
 * Implemented on ServerPlayer by {@code twilightforest.mixin.network.ServerPlayerGuiMixin}. Replaces
 * {@code EntityPlayer.openGui(mod, id, ...)}; block code calls {@link TFNetwork#openGui} instead of casting.
 */
public interface ITFServerPlayer {
    /** Server only: opens the TF container {@code guiId} and tells the client to show the matching screen. */
    void tf$openGui(int guiId, int x, int y, int z);
}
