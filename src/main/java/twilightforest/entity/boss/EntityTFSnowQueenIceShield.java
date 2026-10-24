package twilightforest.entity.boss;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;

public class EntityTFSnowQueenIceShield extends Entity {

	EntityTFSnowQueen queen;

	public EntityTFSnowQueenIceShield(World par1World) {
		super(par1World);
		setSize(0.75F, 0.75F);
	}

	public EntityTFSnowQueenIceShield(EntityTFSnowQueen goblin) {
		this(goblin.func_82194_d());
		this.queen = goblin;
	}

	/**
	 * Don't take damage from attacks
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		this.worldObj.playSoundAtEntity(this, "random.break", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

		return null;
	}

	/**
	 * Skip most of the living update things
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		this.ticksExisted++;

		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;

		for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
		for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
		for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
		for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
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
		return this == entity || this.queen == entity;
	}

	// MITE requires these three overrides on every custom non-living entity (MITE_ENTITY 17): the default
	// implementations need a model item and otherwise flood the HUD with error messages.
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

}
