package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityArrow;
import net.minecraft.IEntitySelector;
import net.minecraft.Item;
import net.minecraft.ItemArrow;
import net.minecraft.MathHelper;
import net.minecraft.Vec3;
import net.minecraft.World;

public class EntitySeekerArrow extends EntityArrow {

	private EntityLivingBase homingTarget;
	// MITE_BALANCE 7.4: seeker search radius 8 blocks (TF: 5)
	double seekDistance = 8.0;

	public EntitySeekerArrow(World par1World) {
		super(par1World);
		// MITE: EntityList/client spawn path has no arrow item, which the model item / fire checks need
		this.item_arrow = Item.arrowFlint;
	}

	/**
	 * MITE: arrows are created with the arrow item that was nocked (1.7.10 had (World, EntityPlayer, float)).
	 */
	public EntitySeekerArrow(World world, EntityLivingBase shooter, float velocity, ItemArrow itemArrow, boolean launcherWasEnchanted) {
		super(world, shooter, velocity, itemArrow, launcherWasEnchanted);
		// MITE_BALANCE 7.4: the seeker arrow cannot be picked up again
		this.canBePickedUp = 0;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		// seek!
		if (isThisArrowFlying()) {

			if (this.homingTarget == null) {
				// find new target

				// target BB
				double minX = this.lastTickPosX;
				double minY = this.lastTickPosY;
				double minZ = this.lastTickPosZ;
				double maxX = this.lastTickPosX;
				double maxY = this.lastTickPosY;
				double maxZ = this.lastTickPosZ;

				AxisAlignedBB targetBB = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

				// add two possible courses to our selection box
				Vec3 courseVec = Vec3.createVectorHelper(this.motionX * seekDistance, this.motionY * seekDistance, this.motionZ * seekDistance);
				courseVec.rotateAroundY((float) (Math.PI / 6F));
				targetBB = targetBB.addCoord(courseVec.xCoord, courseVec.yCoord, courseVec.zCoord);

				courseVec = Vec3.createVectorHelper(this.motionX * seekDistance, this.motionY * seekDistance, this.motionZ * seekDistance);
				courseVec.rotateAroundY(-(float) (Math.PI / 6F));
				targetBB = targetBB.addCoord(courseVec.xCoord, courseVec.yCoord, courseVec.zCoord);

				targetBB.minY -= 3;
				targetBB.maxY += 3;

				// find targets in box
				List targets = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, targetBB, IEntitySelector.selectAnything);

				double closestDot = 1;

				for (Object thing : targets) {
					if (thing instanceof EntityLivingBase && !(thing instanceof EntityPlayer)) {
						EntityLivingBase living = (EntityLivingBase)thing;

						courseVec = Vec3.createVectorHelper(this.motionX, this.motionY, this.motionZ);
						courseVec = courseVec.normalize();
						Vec3 targetVec = Vec3.createVectorHelper(this.posX - living.posX, this.posY - (living.posY + (double)living.getEyeHeight()), this.posZ - living.posZ);

						targetVec = targetVec.normalize();
						double dot = courseVec.dotProduct(targetVec);

						if (dot < closestDot) {
							this.homingTarget = living;
							closestDot = dot;
						}
					}
				}
			} else {
				// find ideal heading
				Vec3 targetVec = Vec3.createVectorHelper(this.posX - this.homingTarget.posX, this.posY - (this.homingTarget.posY + this.homingTarget.getEyeHeight()), this.posZ - this.homingTarget.posZ);
				targetVec = targetVec.normalize();

				Vec3 courseVec = Vec3.createVectorHelper(this.motionX * seekDistance, this.motionY * seekDistance, this.motionZ * seekDistance);
				courseVec = courseVec.normalize();

				double dotProduct = courseVec.dotProduct(targetVec);

				if (dotProduct < 0) {

					// match current speed
					float currentSpeed = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

					currentSpeed *= 1.0;

					targetVec.xCoord *= currentSpeed;
					targetVec.yCoord *= currentSpeed;
					targetVec.zCoord *= currentSpeed;

					// adjust current heading
					double dx = MathHelper.clamp_double(targetVec.xCoord, -2.0, 2.0);
					double dy = MathHelper.clamp_double(targetVec.yCoord, -1.0, 1.0);
					double dz = MathHelper.clamp_double(targetVec.zCoord, -2.0, 2.0);

					this.motionX -= dx;
					this.motionY -= dy;
					this.motionZ -= dz;
				} else {
					// abandon target, they're behind us!
					this.homingTarget = null;
				}
			}

			this.motionY += 0.045F;

		}

		// this is a slower arrow, adjust for gravity slightly

		super.onUpdate();

	}

	private boolean isThisArrowFlying() {
		return MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) > 1.0;
	}

}
