package twilightforest.client.renderer;

import net.minecraft.EntityItem;
import net.minecraft.GameSettings;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.RenderItem;
import net.minecraft.RenderManager;
import net.minecraft.ResourceLocation;
import net.minecraft.Tessellator;
import net.minecraft.TextureManager;

import org.lwjgl.opengl.GL11;

import twilightforest.TFMazeMapData;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;
import twilightforest.item.ItemTFMazeMap;
import twilightforest.item.TFItems;

public class TFMazeMapRenderer implements TFItemRenderer {

	private static final ResourceLocation mapBackgroundTextures = new ResourceLocation("textures/map/map_background.png");

	public TFMazeMapRenderer(GameSettings gameSettings, TextureManager textureManager) {
	}

	@Override
	public boolean handles(ItemStack item, TFItemRenderType type) {
		return (item.getItem() == TFItems.mazeMap || item.getItem() == TFItems.oreMap) && (RenderItem.renderInFrame && type == TFItemRenderType.ENTITY);
	}

	@Override
	public boolean entityRotation() {
		return false;
	}

	@Override
	public boolean entityBobbing() {
		return false;
	}

	@Override
	public void render(TFItemRenderType type, ItemStack item, Object... data) {
		if (RenderItem.renderInFrame) {
			EntityItem entity = (EntityItem) data[1];

			TFMazeMapData mapData = ((ItemTFMazeMap) TFItems.mazeMap).getMapData(item, entity.worldObj);

			// if we have data, render it
			if (mapData != null) {
				// TODO(mite-port): frame scale/translation was tuned for Forge's non-3D path; verify visually
				renderMapInFrame(item, RenderManager.instance, mapData);
			}
		}
	}

	private void renderMapInFrame(ItemStack item, RenderManager renderManager, TFMazeMapData mapData) {

		// do some rotations so that we get vaguely in the right place
		GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
		GL11.glScalef(0.00781250F, 0.00781250F, 0.00781250F);
		GL11.glTranslatef(-65.0F, -111.0F, -3.0F);
		GL11.glNormal3f(0.0F, 0.0F, -1.0F);

		// draw background
		renderManager.renderEngine.bindTexture(mapBackgroundTextures);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		byte b0 = 7;
		tessellator.addVertexWithUV((double) (0 - b0), (double) (128 + b0), 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV((double) (128 + b0), (double) (128 + b0), 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV((double) (128 + b0), (double) (0 - b0), 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV((double) (0 - b0), (double) (0 - b0), 0.0D, 0.0D, 0.0D);
		tessellator.draw();

		// push map texture slightly off background
		GL11.glTranslatef(0.0F, 0.0F, -1.0F);

		// draw map (colors are vanilla map colors)
		Minecraft mc = Minecraft.getMinecraft();
		mc.entityRenderer.itemRenderer.mapItemRenderer.renderMap(null, mc.getTextureManager(), mapData);
	}

}
