package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.EntityAIArrowAttack;
import net.minecraft.EntityAIFleeSun;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAIRestrictSun;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.IRangedAttackMob;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;

public class EntityTFAdherent extends EntityMob implements IRangedAttackMob, ITFCharger {

	/** DataWatcher slot, allocated dynamically (MITE subclasses occupy fixed low ids). */
	private int chargeFlag;

	public EntityTFAdherent(World world) {
		super(world);

		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIRestrictSun(this));
		this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
		this.tasks.addTask(4, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
		this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		this.setSize(0.8F, 2.2F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.chargeFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.chargeFlag, Byte.valueOf((byte) 0));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3 T5: HP 36)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(36.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.25D); // movement speed
	}

	/** MITE_BALANCE 9.3: natural defense 1, none against magic / mundane-armor-bypassing damage. */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return damage_source.bypassesMundaneArmor() ? 0.0F : 1.0F;
	}

	/** MITE_BALANCE 9.3: 14 XP. */
	@Override
	public int getExperienceValue() {
		return 14;
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			((EntityPlayer) par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
		}
	}

	/**
	 * Attack the specified entity using a ranged attack.
	 */
	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase attackTarget, float extraDamage) {
		EntityTFNatureBolt natureBolt = new EntityTFNatureBolt(this.worldObj, this);
		this.worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));

		natureBolt.setTarget(attackTarget);

		double tx = attackTarget.posX - this.posX;
		double ty = attackTarget.posY + attackTarget.getEyeHeight() - 2.699999988079071D - this.posY;
		double tz = attackTarget.posZ - this.posZ;
		float heightOffset = MathHelper.sqrt_double(tx * tx + tz * tz) * 0.2F;
		natureBolt.setThrowableHeading(tx, ty + heightOffset, tz, 0.6F, 6.0F); // 0.6 speed, 6.0 inaccuracy
		this.worldObj.spawnEntityInWorld(natureBolt);
	}

	/**
	 * In this case we're not charging like a bull, but charging up a ranged attack
	 */
	@Override
	public boolean isCharging() {
		return dataWatcher.getWatchableObjectByte(this.chargeFlag) != 0;
	}

	@Override
	public void setCharging(boolean flag) {
		if (flag) {
			dataWatcher.updateObject(this.chargeFlag, Byte.valueOf((byte) 127));
		} else {
			dataWatcher.updateObject(this.chargeFlag, Byte.valueOf((byte) 0));
		}
	}

}
