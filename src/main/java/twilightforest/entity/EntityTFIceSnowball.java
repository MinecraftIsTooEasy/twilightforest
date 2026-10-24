package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;

public class EntityTFIceSnowball extends EntityThrowable {

	// balance: MITE_BALANCE mob hits should cost roughly 15-30% of a 10-12 HP player; TF's original value was 8
	private static final int DAMAGE = 5;

	public EntityTFIceSnowball(World par1World) {
		super(par1World);
	}

	public EntityTFIceSnowball(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		makeTrail();
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return Item.snowball;
	}

	/**
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity() {
		return 0.006F;
	}

	/**
	 * Make snowy trail
	 */
	public void makeTrail() {
		for (int i = 0; i < 2; i++) {
			double dx = posX + 0.5 * (rand.nextDouble() - rand.nextDouble());
			double dy = posY + 0.5 * (rand.nextDouble() - rand.nextDouble());
			double dz = posZ + 0.5 * (rand.nextDouble() - rand.nextDouble());
			TFParticles.spawn(worldObj, "snowballpoof", dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Reflect!
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
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
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase) {
			if (TFDamage.hurt(rc.getEntityHit(), DamageSource.causeThrownDamage(this, this.getThrower()), DAMAGE)) {
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
		for (int i = 0; i < 8; ++i) {
			TFParticles.spawn(this.worldObj, "snowballpoof", this.posX, this.posY, this.posZ, rand.nextGaussian() * 0.05D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.05D);
		}

		if (!this.worldObj.isRemote) {
			this.setDead();
		}
	}

}
