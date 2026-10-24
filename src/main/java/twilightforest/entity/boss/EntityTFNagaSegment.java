package twilightforest.entity.boss;

import java.util.List;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAnimal;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import twilightforest.compat.TFParticles;

public class EntityTFNagaSegment extends Entity {

	/** Damage dealt to anything the segment pushes into (balance: 6, x3 against animals like the original) */
	private static final float CONTACT_DAMAGE = 6.0F;

	EntityTFNaga naga;
	int segment;

	String texture;

	private int deathCounter;

	public EntityTFNagaSegment(World par1World) {
		super(par1World);
		//this.texture = TwilightForestMod.MODEL_DIR + "nagasegment.png";
		setSize(1.8F, 1.8F);
		this.stepHeight = 2;

	}

	public EntityTFNagaSegment(EntityTFNaga myNaga, int segNum) {
		this(myNaga.func_82194_d());
		this.naga = myNaga;
		this.segment = segNum;
	}

	/**
	 * When we're attacked transfer damage to the head.  Segments only transfer 2/3 normal damage to the head.
	 * Segments do not transfer damage from fire, explosions or lava.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		// do not transfer (or take) fire, explosion, or lava damage.
		if (damage.isExplosion() || damage.isFireDamage() || damage.isLavaDamage()) {
			return null;
		}

		// do not transfer damage from segments that are disconnected and about to explode
		if (naga != null) {
			damage.setAmount(Math.round(damage.getAmount() * 2.0F / 3.0F));
			return naga.attackEntityFrom(damage);
		} else {
			return null;
		}
	}


	/**
	 * Skip most of the living update things
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		// remove if invalid
		if (this.naga == null || this.naga.isDead) {
			this.setDead();
		}

		this.ticksExisted++;

		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;

		for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
		for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
		for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
		for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }

		if (!this.onGround) {
			this.motionY -= 0.08D;
		} else {
			this.motionX *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		}

		this.moveEntity(motionX, motionY, motionZ);

		collideWithOthers();

		if (deathCounter > 0) {
			deathCounter--;

			if (deathCounter == 0) {

				for (int k = 0; k < 20; k++) {
					double d = rand.nextGaussian() * 0.02D;
					double d1 = rand.nextGaussian() * 0.02D;
					double d2 = rand.nextGaussian() * 0.02D;
					String explosionType = rand.nextBoolean() ? "largeexplode" : "explode";

					TFParticles.spawn(worldObj, explosionType, (posX + rand.nextFloat() * width * 2.0F) - width, posY + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
				}

				setDead();
				worldObj.removeEntity(this);
			}
		}

	}

	protected void collideWithOthers() {
		List<?> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

		for (Object o : list) {
			Entity entity = (Entity) o;
			if (entity.canBePushed()) {
				this.collideWithEntity(entity);
			}
		}
	}

	private void collideWithEntity(Entity entity) {
		entity.applyEntityCollision(this);

		// attack anything that's not us (the damage is dealt on the server only)
		if (naga != null && !worldObj.isRemote && (entity instanceof EntityLivingBase) && !(entity instanceof EntityTFNaga) && !(entity instanceof EntityTFNagaSegment)) {
			naga.attackTime = 10;
			float attackStrength = CONTACT_DAMAGE;

			// get rid of nearby deer & look impressive
			if (entity instanceof EntityAnimal) {
				attackStrength *= 3;
			}

			entity.attackEntityFrom(new Damage(DamageSource.causeMobDamage(naga), attackStrength));
		}


	}

	/**
	 * Sets the rotation of the entity (MITE's Entity.setRotation is protected, the head calls this one)
	 */
	@Override
	public void setRotation(float par1, float par2) {
		this.rotationYaw = MathHelper.wrapAngleTo180_float(par1 % 360.0F);
		this.rotationPitch = par2 % 360.0F;
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean isEntityEqual(Entity entity) {
		return this == entity || this.naga == entity;
	}

	// MITE: custom non-living entities must answer these or the HUD shows a red error message
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

	@Override
	protected void entityInit() { }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) { }

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) { }


	/**
	 * Plays step sound at given x, y, z for the entity (func_145780_a in 1.7.10): segments are silent
	 */
	@Override
	protected void playStepSound(int x, int y, int z, int blockId) {
	}

	/**
	 * Returns the texture's file path as a String.
	 */
	public String getTexture() {
		return this.texture;
	}

	/**
	 * Starts the self destruction process
	 */
	public void selfDestruct() {
		this.deathCounter = 30;
	}

}
