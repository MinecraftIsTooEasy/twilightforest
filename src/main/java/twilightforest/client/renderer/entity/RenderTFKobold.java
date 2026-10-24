package twilightforest.client.renderer.entity;

import net.minecraft.Block;
import net.minecraft.EntityLiving;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.ModelBiped;
import net.minecraft.RenderBlocks;

import org.lwjgl.opengl.GL11;

public class RenderTFKobold extends RenderTFBiped {

	public RenderTFKobold(ModelBiped modelBiped, float scale, String textureName) {
		super(modelBiped, scale, textureName);
	}

	/**
	 * MITE: RenderBiped bridges renderEquippedItems(EntityLivingBase, float) to func_130005_c(EntityLiving, float),
	 * which is the 1.7.10 renderEquippedItems(EntityLiving, float).
	 */
	@Override
	protected void func_130005_c(EntityLiving entity, float partialTick) {
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		ItemStack itemstack = entity.getHeldItemStack();
		float f1;

		// no armor

		if (itemstack != null && itemstack.getItem() != null) {
			Item item = itemstack.getItem();
			GL11.glPushMatrix();

			this.modelBipedMain.bipedRightArm.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

			// TODO(mite-port): no Forge IItemRenderer.shouldUseRenderHelper(BLOCK_3D) in MITE, custom item renderers are ignored here
			if (item instanceof ItemBlock && Block.blocksList[itemstack.itemID] != null && RenderBlocks.renderItemIn3d(Block.blocksList[itemstack.itemID].getRenderType())) {
				f1 = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				f1 *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(-f1, -f1, f1);
			} else if (item == Item.bow) {
				f1 = 0.625F;
				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
				GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f1, -f1, f1);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else if (item.isFull3D()) {
				f1 = 0.625F;

				if (item.shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}

				this.func_82422_c();
				GL11.glScalef(f1, -f1, f1);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				f1 = 0.375F;
				GL11.glTranslatef(0.25F, 0.0F, -0.1875F);
				this.func_82422_c(); // include short arms even when rendering non-tools
				GL11.glScalef(f1, f1, f1);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			// MITE has no Item.getRenderPasses: multi-pass items render passes 0 and 1 (as RenderBiped does)
			int passes = item.requiresMultipleRenderPasses() ? 2 : 1;
			for (int i = 0; i < passes; ++i) {
				int j = item.getColorFromItemStack(itemstack, i);
				float r = (float) (j >> 16 & 255) / 255.0F;
				float g = (float) (j >> 8 & 255) / 255.0F;
				float b = (float) (j & 255) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
				this.renderManager.itemRenderer.renderItem(entity, itemstack, i);
			}

			GL11.glPopMatrix();
		}
	}

	/**
	 * How far down the arm should we render the equipped item?
	 */
	@Override
	protected void func_82422_c() {
		GL11.glTranslatef(0.0F, 0.01875F, 0.0F);
	}

}
