package twilightforest.entity.boss;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFParticles;


public class EntityTFLichBomb extends EntityThrowable {

	/** MITE_BALANCE: Lich Bomb explosion, sizeVsLiving 0.85 and it never destroys blocks (TF: power 2, no block damage) */
	public static final float EXPLOSION_SIZE_VS_LIVING = 0.85F;

	public EntityTFLichBomb(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityTFLichBomb(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	public EntityTFLichBomb(World par1World) {
		super(par1World);
	}

	/**
	 * MITE requires every thrown entity to name its model item
	 */
	@Override
	public Item getModelItem() {
		return Item.fireballCharge;
	}

	// MITE: fire queries on the bomb itself (it is always burning) must not log errors or ignite it
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
	 * projectile speed (1.7.10 name func_40077_c, in MITE this is func_70182_d; public so the Lich can read it)
	 */
	@Override
	public float func_70182_d()
	{
		return 0.35F;
	}


	@Override
	public void onUpdate() {
		super.onUpdate();

		makeTrail();
	}


	public void makeTrail() {
		for (int i = 0; i < 1; i++) {
			double sx = 0.5 * (rand.nextDouble() - rand.nextDouble()) + this.motionX;
			double sy = 0.5 * (rand.nextDouble() - rand.nextDouble()) + this.motionY;
			double sz = 0.5 * (rand.nextDouble() - rand.nextDouble()) + this.motionZ;


			double dx = posX + sx;
			double dy = posY + sy;
			double dz = posZ + sz;

			TFParticles.spawn(worldObj, "flame", dx, dy, dz, sx * -0.25, sy * -0.25, sz * -0.25);
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
	 * We need to set this so that the player can attack and detonate the bomb
	 */
	@Override
	public float getCollisionBorderSize(Entity for_raycast_from_this_entity)
	{
		return 1.0F;
	}


	/**
	 * Hitting the bomb makes it explode
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
	{
		setBeenAttacked();
		if (damage.getSource().getResponsibleEntity() != null)
		{
			// explode
			explode();

			return new EntityDamageResult(this).setEntityWasAffected();
		}
		else
		{
			return null;
		}
	}

	protected void explode() {
		if (!this.worldObj.isRemote)
		{
			// (blocks size 0: never destroys blocks; living size per MITE_BALANCE; not flaming, smoking for the visuals)
			this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, twilightforest.TFConfig.explosionBreakBlocks ? 2.0F : 0.0F, EXPLOSION_SIZE_VS_LIVING, false, true);

			this.setDead();
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

		// pass through liches
		if (hit instanceof EntityTFLich)
		{
			passThrough = true;
		}

		// if we're not set to pass, blow up
		if (!passThrough)
		{
			explode();
		}

	}


}
