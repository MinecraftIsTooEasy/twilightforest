package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import net.minecraft.RenderLiving;
import net.minecraft.RenderManager;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.entity.boss.EntityTFHydra;
import twilightforest.entity.boss.EntityTFHydraPart;
import twilightforest.entity.boss.HydraHeadContainer;

public class RenderTFHydraHead extends RenderLiving {

    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.MODEL_DIR + "hydra4.png");

	public RenderTFHydraHead(ModelBase modelbase, float f) {
		super(modelbase, f);
		
	}
	
	/**
	 * Override render to perform a few special rendering tricks, like not rendering "dead" heads & their necks
	 */
	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
		// get the HydraHeadContainer that we're taking about
		HydraHeadContainer headCon = getHeadObject(entity);

		if (headCon != null)
		{
			// see whether we want to render these
			if (headCon.shouldRenderHead())
			{
				super.doRender(entity, d, d1, d2, f, f1);
			}

			if (headCon.shouldRenderNeck(0))
			{
				renderEntitySimple(headCon.necka, f1);
			}
			if (headCon.shouldRenderNeck(1))
			{
				renderEntitySimple(headCon.neckb, f1);
			}
			if (headCon.shouldRenderNeck(2))
			{
				renderEntitySimple(headCon.neckc, f1);
			}
			if (headCon.shouldRenderNeck(3))
			{
				renderEntitySimple(headCon.neckd, f1);
			}
			if (headCon.shouldRenderNeck(4))
			{
				renderEntitySimple(headCon.necke, f1);
			}
		}
		else
		{
			super.doRender(entity, d, d1, d2, f, f1);

		}
	}

	/**
	 * Port of 1.7.10 RenderManager.renderEntitySimple (MITE's renderEntity adds a world-renderer visibility check
	 * that would skip our part entities).
	 */
	public static void renderEntitySimple(Entity entity, float partialTick) {
		RenderManager rm = RenderManager.instance;
		if (entity.ticksExisted == 0) {
			entity.lastTickPosX = entity.posX;
			entity.lastTickPosY = entity.posY;
			entity.lastTickPosZ = entity.posZ;
		}
		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick;
		int light = entity.getBrightnessForRender(partialTick);
		if (entity.isBurning()) {
			light = 15728880;
		}
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(light % 65536), (float)(light / 65536));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		rm.renderEntityWithPosYaw(entity, x - RenderManager.renderPosX, y - RenderManager.renderPosY, z - RenderManager.renderPosZ, yaw, partialTick);
	}

	private HydraHeadContainer getHeadObject(Entity entity) {
		EntityTFHydra hydra = ((EntityTFHydraPart)entity).hydraObj;
		
		if (hydra != null)
		{
			for (int i = 0; i < hydra.numHeads; i++)
			{
				if (hydra.hc[i].headEntity == entity)
				{
					return hydra.hc[i];
				}

			}
		}
		return null;
	}

	/**
	 * Return our specific texture
	 */
    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return textureLoc;
    }

	@Override
	protected void setTextures() {
		// textures are returned by getEntityTexture
	}

}
