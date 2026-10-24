package twilightforest.entity.ai;

import net.minecraft.EntityAIBase;
import twilightforest.entity.EntityTFRovingCube;

/**
 * This is a task that runs when we are near a symbol and have stopped pathfinding, but are not centered on the symbol.
 *
 * The goal of this task is to center on a symbol.
 *
 * @author benma_000
 */
public class EntityAICubeCenterOnSymbol extends EntityAIBase {

	private EntityTFRovingCube myCube;
	private double xPosition;
	private double yPosition;
	private double zPosition;
	private double speed;

	public EntityAICubeCenterOnSymbol(EntityTFRovingCube entityTFRovingCube, double d) {
		this.myCube = entityTFRovingCube;
		this.xPosition = this.myCube.symbolX;
		this.yPosition = this.myCube.symbolY;
		this.zPosition = this.myCube.symbolZ;
		this.speed = d;
	}

	@Override
	public boolean shouldExecute() {
		this.xPosition = this.myCube.symbolX;
		this.yPosition = this.myCube.symbolY;
		this.zPosition = this.myCube.symbolZ;

		if (!this.myCube.getNavigator().noPath()) {
			return false;
		} else {
			return this.isCloseToSymbol();
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		// inch towards it
		double dist = this.distanceFromSymbol();

		this.myCube.getMoveHelper().setMoveTo(this.xPosition + 0.5F, this.yPosition, this.zPosition + 0.5F, this.speed);

		return dist > 0.1F && this.isCourseTraversable();
	}

	private boolean isCourseTraversable() {
		return this.distanceFromSymbol() < 100;
	}

	private boolean isCloseToSymbol() {
		double dist = this.distanceFromSymbol();
		return dist > 0.25F && dist < 10F;
	}

	public double distanceFromSymbol() {
		double dx = this.xPosition - this.myCube.posX + 0.5F;
		double dy = this.yPosition - this.myCube.posY;
		double dz = this.zPosition - this.myCube.posZ + 0.5F;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

}
