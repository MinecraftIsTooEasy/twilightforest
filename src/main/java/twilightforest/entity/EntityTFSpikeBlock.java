package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;

public class EntityTFSpikeBlock extends Entity {

	EntityTFBlockGoblin goblin;

	public EntityTFSpikeBlock(World par1World) {
		super(par1World);
		setSize(0.75F, 0.75F);
	}

	public EntityTFSpikeBlock(EntityTFBlockGoblin goblin) {
		this(goblin.func_82194_d());
		this.goblin = goblin;
	}

	/**
	 * Don't take damage from attacks
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		return null;
	}

	/**
	 * Keep position updated
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
		return false;
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
		return this == entity || this.goblin == entity;
	}

	// MITE: a bare Entity logs a red HUD error when it touches fire/lava unless these are overridden (MITE_ENTITY 3.4)
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
