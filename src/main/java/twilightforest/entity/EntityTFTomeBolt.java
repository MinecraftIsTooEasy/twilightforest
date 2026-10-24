package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;


public class EntityTFTomeBolt extends EntityThrowable {

	public EntityTFTomeBolt(World par1World, double par2, double par4,
			double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityTFTomeBolt(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	public EntityTFTomeBolt(World par1World) {
		super(par1World);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

        makeTrail();
	}
	
	/**
	 * How much this entity falls each tick
	 */
	@Override
    protected float getGravityVelocity()
    {
		return 0.003F;
    }
	
	/**
	 * Make sparkly trail
	 */
	public void makeTrail() {
		for (int i = 0; i < 5; i++) {
			double dx = posX + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dy = posY + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dz = posZ + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			TFParticles.spawn(worldObj, "crit", dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * What happens when we hit something?
	 */
	@Override
	protected void onImpact(RaycastCollision rc) {
		// only damage living things
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase)
		{
			if (TFDamage.hurt(rc.getEntityHit(), DamageSource.causeThrownDamage(this, this.getThrower()), 6))
			{
				// inflict move slowdown (difficultySetting: 0 peaceful, 2 normal)
				byte potionStrength = (byte) (worldObj.difficultySetting == 0 ? 3 : worldObj.difficultySetting == 2 ? 7 : 9);
				if(potionStrength > 0)
				{
					((EntityLivingBase)rc.getEntityHit()).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, potionStrength * 20, 1));
				}

			}
		}


		for (int i = 0; i < 8; ++i)
		{
			TFParticles.spawnItemCrack(this.worldObj, Item.fireballCharge.itemID, 0, this.posX, this.posY, this.posZ, rand.nextGaussian() * 0.05D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.05D);
		}

		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}

	}

}
