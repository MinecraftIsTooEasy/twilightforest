package twilightforest.mixin.client;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.EntityItem;
import net.minecraft.FontRenderer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Minecraft;
import net.minecraft.RenderBlocks;
import net.minecraft.RenderItem;
import net.minecraft.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;
import twilightforest.client.TFItemRenderers;

/**
 * Forge IItemRenderer INVENTORY and ENTITY (dropped items and item frames). The transforms are the ones
 * ForgeHooksClient applied before calling the renderer. Every renderer call is wrapped in glPushAttrib/glPopAttrib: the ice
 * and giant item renderers switched alpha test off on the way out, and every item drawn after them in the same frame showed
 * its transparent pixels (black or stray colour squares behind the icon).
 */
@Mixin(RenderItem.class)
public abstract class RenderItemMixin {
    @Shadow public float zLevel;
    @Shadow public boolean renderWithColor;
    @Shadow private RenderBlocks itemRenderBlocks;

    @Inject(method = "renderItemIntoGUI", at = @At("HEAD"), cancellable = true)
    private void tf$renderInventoryItem(FontRenderer font, TextureManager textures, ItemStack stack, int x, int y, CallbackInfo ci) {
        TFItemRenderer renderer = TFItemRenderers.get(stack, TFItemRenderType.INVENTORY);
        if (renderer == null) {
            return;
        }
        textures.bindTexture(textures.getResourceLocation(stack.getItemSpriteNumber()));
        int color = Item.itemsList[stack.itemID].getColorFromItemStack(stack, 0);
        if (renderer.block3d()) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x - 2, y + 3, -3.0F + this.zLevel);
            GL11.glScalef(10.0F, 10.0F, 10.0F);
            GL11.glTranslatef(1.0F, 0.5F, 1.0F);
            GL11.glScalef(1.0F, 1.0F, -1.0F);
            GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            if (this.renderWithColor) {
                GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
            }
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            this.itemRenderBlocks.useInventoryTint = this.renderWithColor;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                renderer.render(TFItemRenderType.INVENTORY, stack, this.itemRenderBlocks);
            } finally {
                GL11.glPopAttrib();
            }
            this.itemRenderBlocks.useInventoryTint = true;
            GL11.glPopMatrix();
        } else {
            GL11.glDisable(2896);
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, -3.0F + this.zLevel);
            if (this.renderWithColor) {
                GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
            }
            // the enchantment glint is drawn by renderItemAndEffectIntoGUI afterwards, renderers must not draw it
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                renderer.render(TFItemRenderType.INVENTORY, stack, this.itemRenderBlocks);
            } finally {
                GL11.glPopAttrib();
            }
            GL11.glPopMatrix();
            GL11.glEnable(2896);
        }
        GL11.glEnable(2884);
        ci.cancel();
    }

    @Inject(method = "doRenderItem", at = @At("HEAD"), cancellable = true)
    private void tf$renderEntityItem(EntityItem entity, double x, double y, double z, float yaw, float partialTick, CallbackInfo ci) {
        ItemStack stack = entity.getEntityItem();
        TFItemRenderer renderer = TFItemRenderers.get(stack, TFItemRenderType.ENTITY);
        if (renderer == null) {
            return;
        }
        float bob = MathHelper.sin((entity.age + partialTick) / 10.0F + entity.hoverStart) * 0.1F + 0.1F;
        float rotation = ((entity.age + partialTick) / 20.0F + entity.hoverStart) * (180.0F / (float) Math.PI);
        TextureManager textures = Minecraft.getMinecraft().getTextureManager();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + bob, (float) z);
        GL11.glEnable(32826);
        if (renderer.entityRotation()) {
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        }
        if (!renderer.entityBobbing()) {
            GL11.glTranslatef(0.0F, -bob, 0.0F);
        }
        textures.bindTexture(textures.getResourceLocation(stack.getItemSpriteNumber()));
        if (renderer.block3d()) {
            Block block = stack.itemID < Block.blocksList.length ? Block.blocksList[stack.itemID] : null;
            int renderType = block != null ? block.getRenderType() : 1;
            float scale = renderType == 1 || renderType == 19 || renderType == 12 || renderType == 2 ? 0.5F : 0.25F;
            if (RenderItem.renderInFrame) {
                GL11.glScalef(1.25F, 1.25F, 1.25F);
                GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            }
            GL11.glScalef(scale, scale, scale);
            int size = stack.stackSize;
            int count = size > 40 ? 5 : size > 20 ? 4 : size > 5 ? 3 : size > 1 ? 2 : 1;
            Random random = new Random(187L);
            for (int i = 0; i < count; i++) {
                GL11.glPushMatrix();
                if (i > 0) {
                    GL11.glTranslatef((random.nextFloat() * 2.0F - 1.0F) * 0.2F / scale, (random.nextFloat() * 2.0F - 1.0F) * 0.2F / scale,
                            (random.nextFloat() * 2.0F - 1.0F) * 0.2F / scale);
                }
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
                try {
                    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                renderer.render(TFItemRenderType.ENTITY, stack, this.itemRenderBlocks, entity);
            } finally {
                GL11.glPopAttrib();
            }
                } finally {
                    GL11.glPopAttrib();
                }
                GL11.glPopMatrix();
            }
        } else {
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                renderer.render(TFItemRenderType.ENTITY, stack, this.itemRenderBlocks, entity);
            } finally {
                GL11.glPopAttrib();
            }
        }
        GL11.glDisable(32826);
        GL11.glPopMatrix();
        ci.cancel();
    }
}
