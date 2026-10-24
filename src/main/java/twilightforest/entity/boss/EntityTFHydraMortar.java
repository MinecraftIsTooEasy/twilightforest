package twilightforest.entity.boss;

import twilightforest.TFConfig;
import java.util.ArrayList;
import java.util.List;

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

public class EntityTFHydraMortar extends EntityThrowable {

	// MITE_BALANCE: mortar direct hit 10 + ignite 2 s (TF: 18 + 5 s)
	private static final int BURN_FACTOR = 2;
	private static final int DIRECT_DAMAGE = 10;

	public EntityLivingBase playerReflects = null;

	public int fuse = 80;

	public boolean megaBlast = false;

	public EntityTFHydraMortar(World par1World)
	{
		super(par1World);
		this.setSize(0.75F, 0.75F);
	}

	public EntityTFHydraMortar(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
		this.setSize(0.75F, 0.75F);
	}

	/**
	 * MITE requires every thrown entity to name its model item
	 */
	@Override
	public Item getModelItem() {
		return Item.fireballCharge;
	}

	// MITE: fire queries on the mortar itself (it is always burning) must not log errors or ignite it
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
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		this.pushOutOfBlocks(); // push out of block

		if (this.onGround)
		{
			if (!worldObj.isRemote)
			{
				// slow down
				this.motionX *= 0.9D;
				this.motionY *= 0.9D;
				this.motionZ *= 0.9D;
			}

			// eventually explode
			if (this.fuse-- <= 0)
			{
				detonate();
			}

		}
	}

	/**
	 * Converts this mortar into a blasting one.
	 */
	public void setToBlasting() {
		this.megaBlast = true;
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	@Override
	protected void onImpact(RaycastCollision rc) {
		if (!rc.isEntity() && !megaBlast)
		{
			// we hit the ground
			this.motionY = 0;
			this.onGround = true;
		}
		else
		{
			detonate();
		}
	}

	// TODO(mite-port): TF clamped block explosion resistance to 0.8 for megaBlast (func_145772_a, except bedrock/end portal);
	// MITE's Block.getExplosionResistance(Explosion) is per block and Entity has no resistance hook, so the stock MITE
	// resistance applies; strength is raised via the explosion size below instead.

	protected void detonate()
	{
		float explosionPower = megaBlast ? 4.0F : 0.1F;
		// explosion.breakBlocks=false (default): the blast hurts creatures only
		float blockPower = TFConfig.explosionBreakBlocks ? explosionPower : 0.0F;
		this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, blockPower, explosionPower, true, true);

		if (!worldObj.isRemote)
		{
			// damage nearby things
			List nearbyList = new ArrayList(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(1.0D, 1.0D, 1.0D)));

			for (Object o : nearbyList)
			{
				Entity nearby = (Entity)o;
				if (nearby.isDead) continue;
				DamageSource source = TFDamage.entitySource("fireball", this, this.getThrower() != null ? this.getThrower() : this).fire();
				if (TFDamage.hurt(nearby, source, DIRECT_DAMAGE) && nearby instanceof EntityLivingBase && nearby.isHarmedByFire())
				{
					nearby.setFire(BURN_FACTOR);
				}
			}
		}

		this.setDead();
	}

	/**
	 * Reflect!
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
	{
		setBeenAttacked();
		Entity attacker = damage.getSource().getResponsibleEntity();
		if (attacker != null && !this.worldObj.isRemote)
		{
			Vec3 vec3d = attacker.getLookVec();
			if (vec3d != null)
			{
				this.setThrowableHeading(vec3d.xCoord, vec3d.yCoord + 1, vec3d.zCoord, 1.5F, 0.1F);  // reflect faster and more accurately
				this.onGround = false;
				this.fuse += 20;
			}
			if (attacker instanceof EntityLivingBase)
			{
				this.playerReflects = (EntityLivingBase)attacker;
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
	 * Always be on fire!
	 */
	@Override
	public boolean isBurning()
	{
		return true;
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
	public float getCollisionBorderSize(Entity forRaycastFromThisEntity)
	{
		return 1.5F;
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0.05F;
	}

	@Override
	protected float func_70182_d()
	{
		return 0.75F;
	}

	@Override
	protected float func_70183_g()
	{
		return -20.0F;
	}
}
