package twilightforest.entity.ai;

import net.minecraft.EntityAIBase;
import net.minecraft.EntityCreature;
import net.minecraft.EntityLivingBase;
import net.minecraft.ServerPlayer;
import net.minecraft.Vec3;
import twilightforest.compat.TFDamage;
import twilightforest.entity.EntityTFYeti;
import twilightforest.network.TFNetwork;

public class EntityAITFThrowRider extends EntityAIBase {

	private EntityCreature theEntityCreature;
	private int throwTimer;

	public EntityAITFThrowRider(EntityCreature par1EntityCreature, float par2) {
		this.theEntityCreature = par1EntityCreature;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		return this.theEntityCreature.riddenByEntity instanceof EntityLivingBase && this.theEntityCreature.getRNG().nextInt(5) <= 0;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		EntityLivingBase rider = (EntityLivingBase) this.theEntityCreature.riddenByEntity;
		rider.mountEntity(null);

		Vec3 look = this.theEntityCreature.getLookVec();
		double throwX = look.xCoord * 2F;
		double throwZ = look.zCoord * 2F;

		// let's throw the player a fixed value in the air
		double throwY = 0.9;
		if (this.theEntityCreature instanceof EntityTFYeti) {
			// MITE_BALANCE 9.3 (Yeti): no vertical lift (prevents fall deaths) + fixed 4 damage instead
			throwY = 0.3;
			TFDamage.hurtByMob(rider, this.theEntityCreature, 4.0F);
		}

		rider.addVelocity(throwX, throwY, throwZ);

		// if we're throwing a player (probably!), send a packet with the velocity
		if (rider instanceof ServerPlayer) {
			TFNetwork.sendThrowPlayer((ServerPlayer) rider, (float) throwX, (float) throwY, (float) throwZ);
		}

		this.throwTimer = 0;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		if (this.theEntityCreature.riddenByEntity == null) {
			this.throwTimer++;
		}

		return this.throwTimer <= 40;
	}

}
