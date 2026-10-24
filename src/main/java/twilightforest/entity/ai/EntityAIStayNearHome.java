package twilightforest.entity.ai;

import net.minecraft.EntityAIBase;
import net.minecraft.EntityCreature;
import net.minecraft.RandomPositionGenerator;
import net.minecraft.Vec3;

public class EntityAIStayNearHome extends EntityAIBase {

	private EntityCreature entity;
	private float speed;

	public EntityAIStayNearHome(EntityCreature entityTFYetiAlpha, float sp) {
		this.entity = entityTFYetiAlpha;
		this.speed = sp;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		// MITE name of isWithinHomeDistanceCurrentPosition()
		return !this.entity.func_110173_bK();
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		return !this.entity.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		if (this.entity.getDistanceSq(this.entity.getHomePosition().posX, this.entity.getHomePosition().posY, this.entity.getHomePosition().posZ) > 256.0D) {
			Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entity, 14, 3,
					Vec3.createVectorHelper(this.entity.getHomePosition().posX + 0.5D, this.entity.getHomePosition().posY, this.entity.getHomePosition().posZ + 0.5D));

			if (vec3 != null) {
				this.entity.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, speed);
			}
		} else {
			this.entity.getNavigator().tryMoveToXYZ(this.entity.getHomePosition().posX + 0.5D, this.entity.getHomePosition().posY, this.entity.getHomePosition().posZ + 0.5D, speed);
		}
	}

}
