package twilightforest.entity;

import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;

public class EntityTFTowerBroodling extends EntityTFSwarmSpider {

	public EntityTFTowerBroodling(World world) {
		this(world, true);
	}

	public EntityTFTowerBroodling(World world, boolean spawnMore) {
		super(world, spawnMore);
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// MITE_BALANCE 9.3: Tower Broodling HP 8, damage 7
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(7.0D);
	}

	/** MITE_BALANCE 9.3: 2 XP (1.7.10: 3). */
	@Override
	public int getExperienceValue() {
		return 2;
	}

	/**
	 * Spawn another spider!
	 *
	 * @return
	 */
	@Override
	protected boolean spawnAnother() {
		EntityTFSwarmSpider another = new EntityTFTowerBroodling(worldObj, false);

		double sx = posX + (rand.nextBoolean() ? 0.9 : -0.9);
		double sy = posY;
		double sz = posZ + (rand.nextBoolean() ? 0.9 : -0.9);
		another.setLocationAndAngles(sx, sy, sz, rand.nextFloat() * 360F, 0.0F);
		if (!another.getCanSpawnHere(false)) {
			another.setDead();
			return false;
		}
		worldObj.spawnEntityInWorld(another);

		return true;
	}
}
