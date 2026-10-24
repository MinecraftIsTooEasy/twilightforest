package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILeapAtTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFFlockToSameKind;
import twilightforest.entity.ai.EntityAITFPanicOnFlockDeath;


public class EntityTFKobold extends EntityMob {

	/** DataWatcher slot for the panic flag (MITE: ids >= 24 are free of vanilla subclass use). */
	private static final int DATA_PANICKED = 24;

	private boolean shy;

	public EntityTFKobold(World world)
	{
		super(world);
		setSize(0.8F, 1.1F);

		shy = true;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAITFPanicOnFlockDeath(this, 2.0F));
		this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.3F));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(4, new EntityAITFFlockToSameKind(this, 1.0D));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	public EntityTFKobold(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		dataWatcher.addObject(DATA_PANICKED, Byte.valueOf((byte)0));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled()
	{
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3: HP 20, melee 10, XP 6)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(10.0D); // attack damage
	}

	@Override
	public int getExperienceValue()
	{
		return 6;
	}

	@Override
	protected String getLivingSound()
	{
		return TwilightForestMod.ID + ":mob.kobold.kobold";
	}

	@Override
	protected String getHurtSound()
	{
		return TwilightForestMod.ID + ":mob.kobold.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return TwilightForestMod.ID + ":mob.kobold.die";
	}

	@Override
	protected int getDropItemId()
	{
		return Item.wheat.itemID;
	}

	/**
	 * Wheat 0-2, gold nugget 5% (MITE_BALANCE 9.3; original was 50%).
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
		int wheat = rand.nextInt(3);
		if (wheat > 0)
		{
			this.dropItem(Item.wheat.itemID, wheat);
		}

		if (rand.nextInt(20) == 0)
		{
			this.dropItem(Item.goldNugget.itemID, 1);
		}
	}

	public boolean isShy() {
		return shy && this.recentlyHit <= 0;
	}

	public boolean isPanicked()
	{
		return dataWatcher.getWatchableObjectByte(DATA_PANICKED) != 0;
	}

	public void setPanicked(boolean flag)
	{
		if (flag)
		{
			dataWatcher.updateObject(DATA_PANICKED, Byte.valueOf((byte)127));
		}
		else
		{
			dataWatcher.updateObject(DATA_PANICKED, Byte.valueOf((byte)0));
		}
	}

	/**
	 * Called frequently so the entity can update its state every tick as required.
	 */
	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		//when panicked, spawn tears/sweat
		if (isPanicked())
		{
			for (int i = 0; i < 2; i++)
			{
				TFParticles.spawn(this.worldObj, "splash", this.posX + (this.rand.nextDouble() - 0.5D) * this.width * 0.5, this.posY + this.getEyeHeight(), this.posZ + (this.rand.nextDouble() - 0.5D) * this.width * 0.5, 0, 0, 0);
			}
		}
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		Entity killer = par1DamageSource.getResponsibleEntity();
		if (killer instanceof EntityPlayer) {
			((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHunter);
		}
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk()
	{
		return 8;
	}

}
