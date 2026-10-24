package twilightforest.entity.ai;

import net.minecraft.Block;
import net.minecraft.EntityAIBase;
import net.minecraft.MathHelper;
import net.minecraft.Vec3;
import twilightforest.entity.EntityTFRovingCube;

public class EntityAICubeMoveToRedstoneSymbols extends EntityAIBase {

	private EntityTFRovingCube myCube;
	private double xPosition;
	private double yPosition;
	private double zPosition;
	private double speed;

	public EntityAICubeMoveToRedstoneSymbols(EntityTFRovingCube entityTFRovingCube, double d) {
		this.myCube = entityTFRovingCube;
		this.speed = d;
	}

	@Override
	public boolean shouldExecute() {
		if (this.myCube.getRNG().nextInt(20) != 0) {
			return false;
		} else {
			Vec3 vec3 = this.searchForRedstoneSymbol(this.myCube, 16, 5);

			if (vec3 == null) {
				return false;
			} else {
				this.xPosition = vec3.xCoord;
				this.yPosition = vec3.yCoord;
				this.zPosition = vec3.zCoord;
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		return !this.myCube.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		this.myCube.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
	}

	/**
	 * Search the area for a redstone circle (8 redstone dust around a blank square)
	 */
	private Vec3 searchForRedstoneSymbol(EntityTFRovingCube myCube2, int xzRange, int yRange) {

		int curX = MathHelper.floor_double(myCube2.posX);
		int curY = MathHelper.floor_double(myCube2.posY);
		int curZ = MathHelper.floor_double(myCube2.posZ);

		for (int x = curX - xzRange; x < curX + xzRange; x++) {
			for (int z = curZ - xzRange; z < curZ + xzRange; z++) {
				for (int y = curY - yRange; y < curY + yRange; y++) {
					if (this.isRedstoneSymbol(x, y, z)) {

						this.myCube.hasFoundSymbol = true;
						this.myCube.symbolX = x;
						this.myCube.symbolY = y;
						this.myCube.symbolZ = z;

						return this.myCube.worldObj.getVec3(x, y, z);
					}
				}
			}
		}

		return null;
	}

	private boolean isRedstoneSymbol(int x, int y, int z) {

		if (!this.myCube.worldObj.blockExists(x, y, z) || !this.myCube.worldObj.isAirBlock(x, y, z)) {
			return false;
		} else {
			// we found an air block, is it surrounded by redstone?
			return this.isWire(x + 1, y, z)
					&& this.isWire(x - 1, y, z)
					&& this.isWire(x, y, z + 1)
					&& this.isWire(x, y, z - 1)
					&& this.isWire(x + 1, y, z + 1)
					&& this.isWire(x - 1, y, z + 1)
					&& this.isWire(x + 1, y, z - 1)
					&& this.isWire(x - 1, y, z - 1);
		}
	}

	private boolean isWire(int x, int y, int z) {
		return this.myCube.worldObj.getBlock(x, y, z) == Block.redstoneWire;
	}

}
