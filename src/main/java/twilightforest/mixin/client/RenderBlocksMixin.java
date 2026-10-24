package twilightforest.mixin.client;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.RenderBlocks;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFBlockRenderer;
import twilightforest.client.TFBlockRenderers;

/** Replaces Forge's ISimpleBlockRenderingHandler lookup: MITE draws nothing for unknown render types. */
@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {
    @Shadow public IBlockAccess blockAccess;
    @Shadow public boolean useInventoryTint;

    @Shadow public abstract void setRenderBoundsForStandardFormBlock();

    @Shadow public abstract void setRenderBoundsForNonStandardFormBlock(Block block);

    // Runs after MITE's dispatch chain came up empty; the render bounds were set before the chain.
    @Inject(method = "renderBlockByRenderType", at = @At("RETURN"), cancellable = true)
    private void tf$renderWorldBlock(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        TFBlockRenderer renderer = TFBlockRenderers.get(block.getRenderType());
        if (renderer != null) {
            cir.setReturnValue(renderer.renderWorldBlock(this.blockAccess, x, y, z, block, (RenderBlocks) (Object) this));
        }
    }

    @Inject(method = "renderBlockAsItem", at = @At("HEAD"), cancellable = true)
    private void tf$renderInventoryBlock(Block block, int metadata, float brightness, CallbackInfo ci) {
        TFBlockRenderer renderer = TFBlockRenderers.get(block.getRenderType());
        if (renderer == null) {
            return;
        }
        // same prelude as MITE's own renderBlockAsItem, which Forge ran before calling the handler
        if (this.useInventoryTint) {
            int color = block.getRenderColor(metadata);
            GL11.glColor4f((color >> 16 & 255) / 255.0F * brightness, (color >> 8 & 255) / 255.0F * brightness, (color & 255) / 255.0F * brightness, 1.0F);
        }
        if (block.isAlwaysStandardFormCube()) {
            this.setRenderBoundsForStandardFormBlock();
        } else {
            this.setRenderBoundsForNonStandardFormBlock(block);
        }
        renderer.renderInventoryBlock(block, metadata, (RenderBlocks) (Object) this);
        ci.cancel();
    }

    // lets ItemRenderer/RenderItem/RenderBiped/RenderPlayer treat TF block items as 3D blocks
    @Inject(method = "renderItemIn3d", at = @At("RETURN"), cancellable = true)
    private static void tf$renderItemIn3d(int renderType, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        TFBlockRenderer renderer = TFBlockRenderers.get(renderType);
        if (renderer != null) {
            cir.setReturnValue(renderer.shouldRender3DInInventory());
        }
    }
}
