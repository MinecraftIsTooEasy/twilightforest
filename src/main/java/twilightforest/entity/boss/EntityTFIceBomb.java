package twilightforest.entity.boss;

import java.util.ArrayList;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFWorldUtil;
import twilightforest.entity.EntityTFYeti;

public class EntityTFIceBomb extends EntityThrowable {

	// MITE_BALANCE 7.4: 6 damage in total (8 pulses of 0.75) + Slowness II for 4 seconds (TF: 1 per pulse, Slowness III for 5 seconds)
	private static final float PULSE_DAMAGE = 0.75F;
	private static final int CHILL_LEVEL = 1;
	private static final int CHILL_TICKS = 20 * 4;

	private int zoneTimer = 80;
	private boolean hasHit;

	public EntityTFIceBomb(World par1World) {
		super(par1World);
	}

	public EntityTFIceBomb(World par1World, EntityLivingBase thrower) {
		super(par1World, thrower);
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return Item.snowball;
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	@Override
	protected void onImpact(RaycastCollision rc) {
		if (this.getThrower() != null && this.getThrower() instanceof EntityTFYetiAlpha) {
			double dist = this.getDistanceSqToEntity(this.getThrower());

			if (dist <= 100) {
				this.setDead();
			}
		}

		this.motionY = 0;
		this.hasHit = true;

		if (!worldObj.isRemote) {
			this.doTerrainEffects();
		}
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	private void doTerrainEffects() {
		int range = 3;
		int ix = MathHelper.floor_double(this.lastTickPosX);
		int iy = MathHelper.floor_double(this.lastTickPosY);
		int iz = MathHelper.floor_double(this.lastTickPosZ);

		for (int x = -range; x <= range; x++) {
			for (int y = -range; y <= range; y++) {
				for (int z = -range; z <= range; z++) {
					this.doTerrainEffect(ix + x, iy + y, iz + z);
				}
			}
		}
	}

	/**
	 * Freeze water, put snow on snowable surfaces
	 */
	private void doTerrainEffect(int x, int y, int z) {
		Block block = this.worldObj.getBlock(x, y, z);
		if (block != null && block.blockMaterial == Material.water) {
			TFWorldUtil.setBlock(this.worldObj, x, y, z, Block.ice);
		}
		if (block != null && block.blockMaterial == Material.lava) {
			TFWorldUtil.setBlock(this.worldObj, x, y, z, Block.obsidian);
		}
		if (this.worldObj.isAirBlock(x, y, z) && Block.snow.canBePlacedAt(this.worldObj, x, y, z, 0)) {
			TFWorldUtil.setBlock(this.worldObj, x, y, z, Block.snow);
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (this.hasHit)
		{
			if (!worldObj.isRemote)
			{
				// slow down
				this.motionX *= 0.1D;
				this.motionY *= 0.1D;
				this.motionZ *= 0.1D;
			}

			this.zoneTimer--;

			makeIceZone();

			// eventually explode
			if (this.zoneTimer <= 0)
			{
				detonate();
			}

		} else {
			makeTrail();
		}
	}

	public void makeTrail() {
		for (int i = 0; i < 10; i++) {
			double dx = posX + 0.75F * (rand.nextFloat() - 0.5F);
			double dy = posY + 0.75F * (rand.nextFloat() - 0.5F);
			double dz = posZ + 0.75F * (rand.nextFloat() - 0.5F);

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowstuff", dx, dy, dz, 0, 0, 0);
		}
	}

	private void makeIceZone() {
		// nearby sparkles
		if (this.worldObj.isRemote) {
			// sparkles
			for (int i = 0; i < 20; i++) {
				double dx = this.posX + (rand.nextFloat() - rand.nextFloat()) * 3.0F;
				double dy = this.posY + (rand.nextFloat() - rand.nextFloat()) * 3.0F;
				double dz = this.posZ + (rand.nextFloat() - rand.nextFloat()) * 3.0F;


				TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowstuff", dx, dy, dz, 0.0D, 0.0D, 0.0D);
			}
		} else {
			// damage
			if (this.zoneTimer % 10 == 0) {
				hitNearbyEntities();

			}
		}
	}

	private void hitNearbyEntities() {
		ArrayList<Entity> nearby = new ArrayList<Entity>(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(3, 2, 3)));

		for (Entity entity : nearby) {
			if (entity instanceof EntityLivingBase && entity != this.getThrower()) {

				if (entity instanceof EntityTFYeti) {
					// TODO: make "frozen yeti" entity?
					entity.setDead();
					int ix = MathHelper.floor_double(entity.lastTickPosX);
					int iy = MathHelper.floor_double(entity.lastTickPosY);
					int iz = MathHelper.floor_double(entity.lastTickPosZ);

					TFWorldUtil.setBlock(worldObj, ix, iy, iz, Block.ice);
					TFWorldUtil.setBlock(worldObj, ix, iy + 1, iz, Block.ice);

				} else {
					TFDamage.hurt(entity, DamageSource.magic, PULSE_DAMAGE);
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, CHILL_TICKS, CHILL_LEVEL, true));
				}

			}
		}
	}

	private void detonate() {
		this.setDead();
	}

	/**
	 * 1.7.10 returned packed ice; MITE has none, so the plain ice block stands in.
	 */
	public Block getBlock() {
		return Block.ice;
	}

	@Override
	protected float func_70182_d()
	{
		// velocity
		return 0.75F;
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	@Override
	protected float getGravityVelocity()
	{
		return this.hasHit ? 0F : 0.025F;
	}

	@Override
	protected float func_70183_g()
	{
		return -20.0F;
	}
}
