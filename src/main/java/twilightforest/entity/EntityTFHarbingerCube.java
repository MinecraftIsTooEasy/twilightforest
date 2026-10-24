package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;

public class EntityTFHarbingerCube extends EntityMob {

	public EntityTFHarbingerCube(World world) {
		super(world);

		this.setSize(1.9F, 2.4F);

		this.tasks.addTask(1, new EntityAISwimming(this));
		// MITE_BALANCE 9.3: the cube hits for 15; the 1.7.10 original had no attack task at all
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(15.0D);
	}

	/** MITE_BALANCE 9.3: natural defense 2. */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return damage_source.bypassesMundaneArmor() ? 0.0F : 2.0F;
	}

	/** MITE_BALANCE 9.3: 20 XP. */
	@Override
	public int getExperienceValue() {
		return 20;
	}

	/** Big body: MITE melee reach is measured from the centre, so widen it by half the width. */
	@Override
	public float getReach() {
		return super.getReach() + this.width * 0.5F;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}
}
