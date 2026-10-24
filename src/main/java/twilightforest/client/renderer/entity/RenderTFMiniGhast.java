package twilightforest.client.renderer.entity;

import net.minecraft.ModelBase;
import net.minecraft.RenderLiving;
import net.minecraft.Entity;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFTowerGhast;

public class RenderTFMiniGhast extends RenderLiving {
	
	final ResourceLocation textureLocClosed;
	final ResourceLocation textureLocOpen;
	final ResourceLocation textureLocAttack;
	
	public RenderTFMiniGhast(ModelBase par1ModelBase, float par2) {
		super(par1ModelBase, par2);
		
		textureLocClosed = new ResourceLocation(TwilightForestMod.MODEL_DIR + "towerghast.png");
		textureLocOpen = new ResourceLocation(TwilightForestMod.MODEL_DIR + "towerghast_openeyes.png");
		textureLocAttack = new ResourceLocation(TwilightForestMod.MODEL_DIR + "towerghast_fire.png");
	}

	@Override
	protected void setTextures() {
		// textures are returned by getEntityTexture
	}

	/**
	 * Return our specific texture
	 */
	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity)
	{
		if (par1Entity instanceof EntityTFTowerGhast)
		{
			switch (((EntityTFTowerGhast)par1Entity).getAttackStatus())
			{
			default: 
			case 0:
				return textureLocClosed;

			case 1:
				return textureLocOpen;

			case 2:
				return textureLocAttack;
			}
		}

		// fallback
		return textureLocClosed;
    }

}
