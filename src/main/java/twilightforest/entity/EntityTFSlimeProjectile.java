package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;

public class EntityTFSlimeProjectile extends EntityThrowable {

	public EntityTFSlimeProjectile(World par1World) {
		super(par1World);
	}

	public EntityTFSlimeProjectile(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
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
		return 0.006F;
    }
	
	/**
	 * Make slimy trail
	 */
	public void makeTrail() {
		for (int i = 0; i < 2; i++) {
			double dx = posX + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dy = posY + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dz = posZ + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			TFParticles.spawn(worldObj, "slime", dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}
	
	/**
	 * Reflect!
	 */
	@Override
    public EntityDamageResult attackEntityFrom(Damage damage)
    {
        setBeenAttacked();
        
		pop();

        return new EntityDamageResult(this).setEntityWasAffected();
     }

	/**
	 * What happens when we hit something?
	 */
	@Override
	protected void onImpact(RaycastCollision rc) {
		// only damage living things
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase)
		{
			if (TFDamage.hurt(rc.getEntityHit(), DamageSource.causeThrownDamage(this, this.getThrower()), 8))
			{
				// damage armor?
				// TODO(mite-port): armor damage was never implemented in TF either
			}
		}

		pop();

	}

	/**
	 * Yeah, do that
	 */
	protected void pop() {
		for (int i = 0; i < 8; ++i)
		{
			TFParticles.spawn(this.worldObj, "slime", this.posX, this.posY, this.posZ, rand.nextGaussian() * 0.05D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.05D);
		}
		
		// noise
		this.worldObj.playSoundAtEntity(this, "mob.slime.big", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));


		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}
	}


}
