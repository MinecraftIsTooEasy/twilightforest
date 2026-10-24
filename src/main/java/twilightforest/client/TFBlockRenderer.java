package twilightforest.client;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;

/** Replacement for Forge's ISimpleBlockRenderingHandler; dispatched from RenderBlocksMixin. */
public interface TFBlockRenderer {
    /** The render bounds of the block are already set when this is called. */
    boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer);

    /** Called with the inventory tint and render bounds already applied, like Forge did. */
    void renderInventoryBlock(Block block, int metadata, RenderBlocks renderer);

    boolean shouldRender3DInInventory();
}
