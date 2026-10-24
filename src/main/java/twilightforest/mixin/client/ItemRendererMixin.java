package twilightforest.mixin.client;

import net.minecraft.EntityLivingBase;
import net.minecraft.ItemRenderer;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.RenderBlocks;
import net.minecraft.TextureManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;
import twilightforest.client.TFItemRenderers;

/** Forge IItemRenderer EQUIPPED / EQUIPPED_FIRST_PERSON. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow private Minecraft mc;
    @Shadow private RenderBlocks renderBlocksInstance;
    @Unique private boolean tf$inFirstPerson;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void tf$firstPersonBegin(float partialTick, CallbackInfo ci) {
        this.tf$inFirstPerson = true;
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void tf$firstPersonEnd(float partialTick, CallbackInfo ci) {
        this.tf$inFirstPerson = false;
    }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void tf$renderHeldItem(EntityLivingBase entity, ItemStack stack, int pass, CallbackInfo ci) {
        TFItemRenderer renderer = TFItemRenderers.get(stack);
        if (renderer == null) {
            return;
        }
        TFItemRenderType type = this.tf$inFirstPerson && entity == this.mc.thePlayer
                ? TFItemRenderType.EQUIPPED_FIRST_PERSON : TFItemRenderType.EQUIPPED;
        if (!renderer.handles(stack, type)) {
            return;
        }
        TextureManager textures = this.mc.getTextureManager();
        GL11.glPushMatrix();
        textures.bindTexture(textures.getResourceLocation(stack.getItemSpriteNumber()));
        if (renderer.block3d()) {
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        } else {
            // the flat-item prelude of ItemRenderer.renderItem
            GL11.glEnable(32826);
            GL11.glTranslatef(0.0F, -0.3F, 0.0F);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        }
        renderer.render(type, stack, this.renderBlocksInstance, entity);
        GL11.glDisable(32826);
        GL11.glPopMatrix();
        ci.cancel();
    }
}
