package twilightforest.client.renderer;

import java.util.Collection;
import java.util.List;

import net.minecraft.BiomeGenBase;
import net.minecraft.DynamicTexture;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.GameSettings;
import net.minecraft.ItemStack;
import net.minecraft.MapCoord;
import net.minecraft.MapData;
import net.minecraft.RenderItem;
import net.minecraft.RenderManager;
import net.minecraft.ResourceLocation;
import net.minecraft.Tessellator;
import net.minecraft.TextureManager;

import org.lwjgl.opengl.GL11;

import twilightforest.TFMagicMapData;
import twilightforest.TwilightForestMod;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;
import twilightforest.item.ItemTFMagicMap;
import twilightforest.item.TFItems;

public class TFMagicMapRenderer implements TFItemRenderer {

	private static final ResourceLocation vanillaMapIcons = new ResourceLocation("textures/map/map_icons.png");
	private static final ResourceLocation twilightMapIcons = new ResourceLocation(TwilightForestMod.GUI_DIR + "mapicons.png");
	private static final ResourceLocation mapBackgroundTextures = new ResourceLocation("textures/map/map_background.png");
	private int[] intArray = new int[16384];
	private DynamicTexture bufferedImage;
	private final ResourceLocation textureLoc;

	public TFMagicMapRenderer(GameSettings par1GameSettings, TextureManager par2TextureManager) {
		this.bufferedImage = new DynamicTexture(128, 128);
		this.textureLoc = par2TextureManager.getDynamicTextureLocation("map", this.bufferedImage);
		this.intArray = this.bufferedImage.getTextureData();

		for (int i = 0; i < this.intArray.length; ++i) {
			this.intArray[i] = 0;
		}
	}

	@Override
	public boolean handles(ItemStack item, TFItemRenderType type) {
		return item.getItem() == TFItems.magicMap && (type == TFItemRenderType.FIRST_PERSON_MAP || (RenderItem.renderInFrame && type == TFItemRenderType.ENTITY));
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
		if (type == TFItemRenderType.FIRST_PERSON_MAP) {
			// data = {MapData, EntityPlayer, TextureManager}
			MapData mapData = (MapData) data[0];
			EntityPlayer player = (EntityPlayer) data[1];
			TextureManager renderEngine = (TextureManager) data[2];
			// if we have data, render it
			if (mapData instanceof TFMagicMapData) {
				renderMap(player, renderEngine, (TFMagicMapData) mapData);
			}
		} else if (RenderItem.renderInFrame) {
			EntityItem entity = (EntityItem) data[1];

			TFMagicMapData mapData = ((ItemTFMagicMap) TFItems.magicMap).getMapData(item, entity.worldObj);

			// if we have data, render it
			if (mapData != null) {
				// TODO(mite-port): frame scale/translation was tuned for Forge's non-3D path; verify visually
				renderMapInFrame(item, RenderManager.instance, mapData);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void renderMap(EntityPlayer par1EntityPlayer, TextureManager par2TextureManager, TFMagicMapData par3MapData) {
		TFMagicMapData magicMapData = par3MapData;

		for (int i = 0; i < 16384; ++i) {
			int colorByte = par3MapData.colors[i] & 0xFF;

			if (colorByte == 0) {
				this.intArray[i] = (i + i / 128 & 1) * 8 + 16 << 24;
			} else {
				int biomeID = colorByte - 1;
				BiomeGenBase biome = BiomeGenBase.biomeList[biomeID];
				if (biome != null) {
					this.intArray[i] = -16777216 | biome.color;
				}
			}
		}

		this.bufferedImage.updateDynamicTexture();
		byte var15 = 0;
		byte var16 = 0;
		Tessellator tesselator = Tessellator.instance;
		float var18 = 0.0F;
		par2TextureManager.bindTexture(this.textureLoc);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		tesselator.startDrawingQuads();
		tesselator.addVertexWithUV((double) ((float) (var15 + 0) + var18), (double) ((float) (var16 + 128) - var18), -0.009999999776482582D, 0.0D, 1.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 128) - var18), (double) ((float) (var16 + 128) - var18), -0.009999999776482582D, 1.0D, 1.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 128) - var18), (double) ((float) (var16 + 0) + var18), -0.009999999776482582D, 1.0D, 0.0D);
		tesselator.addVertexWithUV((double) ((float) (var15 + 0) + var18), (double) ((float) (var16 + 0) + var18), -0.009999999776482582D, 0.0D, 0.0D);
		tesselator.draw();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		par2TextureManager.bindTexture(vanillaMapIcons);
		for (MapCoord mapCoord : (Collection<MapCoord>) par3MapData.playersVisibleOnMap.values()) {
			GL11.glPushMatrix();
			GL11.glTranslatef((float) var15 + (float) mapCoord.centerX / 2.0F + 64.0F, (float) var16 + (float) mapCoord.centerZ / 2.0F + 64.0F, -0.04F);
			GL11.glRotatef((float) (mapCoord.iconRotation * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(4.0F, 4.0F, 3.0F);
			GL11.glTranslatef(-0.125F, 0.125F, 0.0F);
			float var21 = (float) (mapCoord.iconSize % 4 + 0) / 4.0F;
			float var23 = (float) (mapCoord.iconSize / 4 + 0) / 4.0F;
			float var22 = (float) (mapCoord.iconSize % 4 + 1) / 4.0F;
			float var24 = (float) (mapCoord.iconSize / 4 + 1) / 4.0F;
			tesselator.startDrawingQuads();
			tesselator.addVertexWithUV(-1.0D, 1.0D, 0.0D, (double) var21, (double) var23);
			tesselator.addVertexWithUV(1.0D, 1.0D, 0.0D, (double) var22, (double) var23);
			tesselator.addVertexWithUV(1.0D, -1.0D, 0.0D, (double) var22, (double) var24);
			tesselator.addVertexWithUV(-1.0D, -1.0D, 0.0D, (double) var21, (double) var24);
			tesselator.draw();
			GL11.glPopMatrix();
		}

		par2TextureManager.bindTexture(twilightMapIcons);

		for (MapCoord mapCoord : (List<MapCoord>) magicMapData.featuresVisibleOnMap) {
			GL11.glPushMatrix();
			GL11.glTranslatef((float) var15 + (float) mapCoord.centerX / 2.0F + 64.0F, (float) var16 + (float) mapCoord.centerZ / 2.0F + 64.0F, -0.02F);
			GL11.glRotatef((float) (mapCoord.iconRotation * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(4.0F, 4.0F, 3.0F);
			GL11.glTranslatef(-0.125F, 0.125F, 0.0F);
			float var21 = (float) (mapCoord.iconSize % 8 + 0) / 8.0F;
			float var23 = (float) (mapCoord.iconSize / 8 + 0) / 8.0F;
			float var22 = (float) (mapCoord.iconSize % 8 + 1) / 8.0F;
			float var24 = (float) (mapCoord.iconSize / 8 + 1) / 8.0F;
			tesselator.startDrawingQuads();
			tesselator.addVertexWithUV(-1.0D, 1.0D, 0.0D, (double) var21, (double) var23);
			tesselator.addVertexWithUV(1.0D, 1.0D, 0.0D, (double) var22, (double) var23);
			tesselator.addVertexWithUV(1.0D, -1.0D, 0.0D, (double) var22, (double) var24);
			tesselator.addVertexWithUV(-1.0D, -1.0D, 0.0D, (double) var21, (double) var24);
			tesselator.draw();
			GL11.glPopMatrix();
		}
	}

	private void renderMapInFrame(ItemStack item, RenderManager renderManager, TFMagicMapData mapData) {

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

		// draw map
		renderMap(null, renderManager.renderEngine, mapData);
	}

}
