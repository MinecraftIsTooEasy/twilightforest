package twilightforest.client;

import net.minecraft.ItemStack;

/** Replacement for Forge's IItemRenderer; dispatched from ItemRendererMixin / RenderItemMixin. */
public interface TFItemRenderer {
    boolean handles(ItemStack stack, TFItemRenderType type);

    /** Forge ItemRendererHelper.ENTITY_ROTATION */
    default boolean entityRotation() {
        return true;
    }

    /** Forge ItemRendererHelper.ENTITY_BOBBING */
    default boolean entityBobbing() {
        return true;
    }

    /** Forge ItemRendererHelper.BLOCK_3D / EQUIPPED_BLOCK / INVENTORY_BLOCK */
    default boolean block3d() {
        return false;
    }

    /**
     * data[0] is the shared RenderBlocks; data[1] is the EntityLivingBase (EQUIPPED*) or EntityItem (ENTITY);
     * INVENTORY only has data[0]; FIRST_PERSON_MAP has {MapData, EntityPlayer, TextureManager}.
     */
    void render(TFItemRenderType type, ItemStack stack, Object... data);
}
