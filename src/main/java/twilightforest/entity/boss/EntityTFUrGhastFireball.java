package twilightforest.entity.boss;

import net.minecraft.Entity;
import net.minecraft.EntityFireball;
import net.minecraft.EntityLargeFireball;
import net.minecraft.RaycastCollision;
import net.minecraft.World;

public class EntityTFUrGhastFireball extends EntityLargeFireball {

	/** Explosion size vs living entities (balance: ~14 damage at ground zero, fading to 4 * size blocks). */
	private static final float EXPLOSION_VS_LIVING = 1.3F;

	public EntityTFUrGhastFireball(World worldObj, EntityTFUrGhast entityTFTowerBoss, double x, double y, double z)
	{
		super(worldObj, entityTFTowerBoss, x, y, z);
	}

	/**
	 * Called when this EntityFireball hits a block or entity.
	 * Balance: block radius 0 (no block damage), damage via sizeVsLiving, plus 3 s of burning for a direct hit.
	 */
	@Override
	protected void onImpact(RaycastCollision rc)
	{
		if (!this.worldObj.isRemote)
		{
			Entity hit = rc.isEntity() ? rc.getEntityHit() : null;

			if (hit instanceof EntityFireball)
			{
				return;
			}

			if (hit != null)
			{
				hit.setFire(3);
			}

			this.worldObj.createExplosion((Entity) null, this.posX, this.posY, this.posZ, twilightforest.TFConfig.explosionBreakBlocks ? 4.0F * EXPLOSION_VS_LIVING : 0.0F, EXPLOSION_VS_LIVING, false);
			this.setDead();
		}
	}
}
