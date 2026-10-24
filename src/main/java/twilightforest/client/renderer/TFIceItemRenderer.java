package twilightforest.client.renderer;

import net.minecraft.EntityItem;
import net.minecraft.EntityLivingBase;
import net.minecraft.Icon;
import net.minecraft.ItemRenderer;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.RenderItem;
import net.minecraft.ResourceLocation;
import net.minecraft.Tessellator;
import net.minecraft.TextureManager;
import net.minecraft.TextureMap;
import net.minecraft.GameSettings;
import org.lwjgl.opengl.GL11;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;

/** Ice/glass sword and ice bow: translucent crystal rendering. */
public class TFIceItemRenderer implements TFItemRenderer {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static RenderItem inventoryRenderItem;
    private final TextureManager texturemanager;

    public TFIceItemRenderer(GameSettings gameSettings, TextureManager textureManager) {
        this.texturemanager = textureManager;
    }

    @Override
    public boolean handles(ItemStack stack, TFItemRenderType type) {
        return type == TFItemRenderType.ENTITY || type == TFItemRenderType.EQUIPPED || type == TFItemRenderType.EQUIPPED_FIRST_PERSON || type == TFItemRenderType.INVENTORY;
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
    public void render(TFItemRenderType type, ItemStack stack, Object... data) {
        if (type == TFItemRenderType.EQUIPPED_FIRST_PERSON) {
            Icon icon = ((EntityLivingBase) data[1]).getItemIcon(stack, 0);
            renderIcyItemEquipped(icon, stack);
        } else if (type == TFItemRenderType.EQUIPPED) {
            Icon icon = ((EntityLivingBase) data[1]).getItemIcon(stack, 0);
            renderIcyItemEquipped(icon, stack);
        } else if (type == TFItemRenderType.ENTITY) {
            this.renderDroppedItem((EntityItem) data[1], stack);
        } else if (type == TFItemRenderType.INVENTORY) {
            this.renderInventoryItem(stack);
        }
    }

    private void renderIcyItemEquipped(Icon icon, ItemStack stack) {
        if (icon == null) {
            GL11.glPopMatrix();
            return;
        }
        texturemanager.bindTexture(texturemanager.getResourceLocation(stack.getItemSpriteNumber()));
        // TODO(mite-port): 1.7.10 TextureUtil.func_152777_a/func_147945_b (blur/mipmap toggle) do not exist in MITE; 1.6.4 has no such state
        Tessellator tessellator = Tessellator.instance;
        float f = icon.getMinU();
        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMaxV();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        GL11.glDisable(GL11.GL_BLEND);

        if (stack.hasEffect()) {
            renderGlint(stack, tessellator);
        }

        texturemanager.bindTexture(texturemanager.getResourceLocation(stack.getItemSpriteNumber()));
    }

    private void renderGlint(ItemStack stack, Tessellator tessellator) {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        texturemanager.bindTexture(RES_ITEM_GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(768, 1);
        float f7 = 0.76F;
        GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        float f8 = 0.125F;
        GL11.glScalef(f8, f8, f8);
        float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
        GL11.glTranslatef(f9, 0.0F, 0.0F);
        GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef(f8, f8, f8);
        f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
        GL11.glTranslatef(-f9, 0.0F, 0.0F);
        GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, 0.0625F);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private void renderDroppedItem(EntityItem entityItem, ItemStack item) {
        Tessellator tessellator = Tessellator.instance;
        float f12 = 0.0625F;
        float f11 = 0.021875F;

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.25F, -(f12 + f11));
        GL11.glTranslatef(0.0F, 0.0F, f12 + f11);
        this.texturemanager.bindTexture(TextureMap.locationItemsTexture);
        Icon icon = item.getIconIndex();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ItemRenderer.renderItemIn2D(tessellator, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), f12);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private void renderInventoryItem(ItemStack stack) {
        Icon icon = stack.getItem().getIconFromSubtypeForRenderPass(stack.getItemSubtype(), 0);
        if (icon == null) {
            icon = stack.getIconIndex();
        }
        if (inventoryRenderItem == null) {
            inventoryRenderItem = new RenderItem();
        }
        // the enchantment glint is drawn by RenderItem.renderItemAndEffectIntoGUI afterwards
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        inventoryRenderItem.renderIcon(0, 0, icon, 16, 16);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
