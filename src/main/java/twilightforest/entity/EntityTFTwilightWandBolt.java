package twilightforest.entity;

import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;


public class EntityTFTwilightWandBolt extends EntityThrowable {

	public EntityTFTwilightWandBolt(World par1World) {
		super(par1World);
	}


	public EntityTFTwilightWandBolt(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}


	public EntityTFTwilightWandBolt(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return Item.enderPearl;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		makeTrail();
	}

	public void makeTrail() {
		for (int i = 0; i < 5; i++) {
			double dx = posX + 0.5 * (rand.nextDouble() - rand.nextDouble());
			double dy = posY + 0.5 * (rand.nextDouble() - rand.nextDouble());
			double dz = posZ + 0.5 * (rand.nextDouble() - rand.nextDouble());

			double s1 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.17F;  // color
			double s2 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.80F;  // color
			double s3 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.69F;  // color

			TFParticles.spawn(worldObj, "mobSpell", dx, dy, dz, s1, s2, s3);
		}
	}

	/**
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0.003F;
	}



	@Override
	protected void onImpact(RaycastCollision rc) {
		// only hit living things
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase)
		{
			TFDamage.hurtByMagic(rc.getEntityHit(), this, this.getThrower(), 6);
		}

		for (int i = 0; i < 8; ++i)
		{
			TFParticles.spawnItemCrack(this.worldObj, Item.enderPearl.itemID, 0, this.posX, this.posY, this.posZ, rand.nextGaussian() * 0.05D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.05D);
		}

		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}

	}

}
