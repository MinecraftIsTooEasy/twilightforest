package twilightforest.client.renderer;

import net.minecraft.Block;
import net.minecraft.GameSettings;
import net.minecraft.ItemStack;
import net.minecraft.RenderBlocks;
import net.minecraft.TextureManager;
import org.lwjgl.opengl.GL11;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;

/** Giant blocks: the block rendered as an item at 4x scale. */
public class TFGiantBlockRenderer implements TFItemRenderer {

    public TFGiantBlockRenderer(GameSettings gameSettings, TextureManager textureManager) {
    }

    @Override
    public boolean handles(ItemStack stack, TFItemRenderType type) {
        return type == TFItemRenderType.ENTITY || type == TFItemRenderType.EQUIPPED || type == TFItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean entityRotation() {
        return true;
    }

    @Override
    public boolean entityBobbing() {
        return true;
    }

    @Override
    public boolean block3d() {
        return true;
    }

    @Override
    public void render(TFItemRenderType type, ItemStack stack, Object... data) {
        float scale = 4.0F;
        if (type == TFItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glScalef(scale, scale, scale);
            // move for first person
            GL11.glTranslatef(1.0F, 0.3F, 0.75F);
            GL11.glRotatef(45.0F, 0.0F, 0.0F, 1.0F);
        } else if (type == TFItemRenderType.EQUIPPED) {
            GL11.glScalef(scale, scale, scale);
            // move item for equipped
            GL11.glTranslatef(0.25F, 0.3F, -0.5F);
            GL11.glRotatef(15.0F, 0.0F, 0.0F, 1.0F);
        } else if (type == TFItemRenderType.ENTITY) {
            GL11.glScalef(scale, scale, scale);
            // move item for dropped
            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
        } else {
            return;
        }
        Block block = stack.itemID < Block.blocksList.length ? Block.blocksList[stack.itemID] : null;
        if (block != null) {
            ((RenderBlocks) data[0]).renderBlockAsItem(block, stack.getItemSubtype(), 1.0F);
        }
    }
}
