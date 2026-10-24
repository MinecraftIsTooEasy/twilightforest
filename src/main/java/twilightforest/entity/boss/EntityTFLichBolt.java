package twilightforest.entity.boss;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;


public class EntityTFLichBolt extends EntityThrowable {

	/** MITE_BALANCE: Lich Bolt = 4 magic damage (TF: 6) */
	public static final float DAMAGE = 4.0F;

	public EntityLivingBase playerReflects = null;

	public EntityTFLichBolt(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityTFLichBolt(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	public EntityTFLichBolt(World par1World) {
		super(par1World);
	}

	/**
	 * MITE requires every thrown entity to name its model item
	 */
	@Override
	public Item getModelItem() {
		return Item.enderPearl;
	}

	// MITE: fire queries on the bolt itself must not log errors or ignite it
	@Override
	public boolean canCatchFire() {
		return false;
	}

	@Override
	public boolean isHarmedByFire() {
		return false;
	}

	@Override
	public boolean isHarmedByLava() {
		return false;
	}

	/**
	 * projectile speed (1.7.10 name func_70182_d, same name in MITE; public so the Lich can read it)
	 */
	@Override
	public float func_70182_d()
	{
		return 0.5F;
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

			double s1 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.17F;
			double s2 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.80F;
			double s3 = ((rand.nextFloat() * 0.5F) + 0.5F) * 0.69F;

			TFParticles.spawn(worldObj, "mobSpell", dx, dy, dz, s1, s2, s3);
		}
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	/**
	 * We need to set this so that the player can attack and reflect the bolt
	 */
	@Override
	public float getCollisionBorderSize(Entity for_raycast_from_this_entity)
	{
		return 1.0F;
	}


	/**
	 * Reflect!
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
	{
		setBeenAttacked();
		Entity attacker = damage.getSource().getResponsibleEntity();
		if (attacker != null)
		{
			if (!this.worldObj.isRemote)
			{
				Vec3 vec3d = attacker.getLookVec();
				if (vec3d != null)
				{
					this.setThrowableHeading(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord, 1.5F, 0.1F);  // reflect faster and more accurately
				}
				if (attacker instanceof EntityLivingBase)
				{
					playerReflects = (EntityLivingBase) attacker;
				}
			}
			return new EntityDamageResult(this).setEntityWasAffected();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Return who threw this projectile
	 */
	@Override
	public EntityLivingBase getThrower()
	{
		if (this.playerReflects != null)
		{
			return this.playerReflects;
		}
		else
		{
			return super.getThrower();
		}
	}

	/**
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0.001F;
	}

	@Override
	protected void onImpact(RaycastCollision rc) {
		boolean passThrough = false;
		Entity hit = rc.isEntity() ? rc.getEntityHit() : null;

		// pass through other lich bolts
		if (hit != null && (hit instanceof EntityTFLichBolt || hit instanceof EntityTFLichBomb)) {
			passThrough = true;
		}

		// only damage living things
		if (hit instanceof EntityLivingBase)
		{
			if (hit instanceof EntityTFLich) {
				EntityTFLich lich = (EntityTFLich) hit;
				if (lich.isShadowClone()) {
					passThrough = true;
				}
			}
			// if we're not set to pass, damage what we hit
			if (!passThrough) {
				TFDamage.hurt(hit, DamageSource.causeIndirectMagicDamage(this, this.getThrower()), DAMAGE);
			}
		}
		// if we don't pass through, then stop and die
		if (!passThrough) {
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


}
