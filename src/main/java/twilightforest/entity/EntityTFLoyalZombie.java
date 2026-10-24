package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIFollowOwner;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILeapAtTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAIOwnerHurtByTarget;
import net.minecraft.EntityAIOwnerHurtTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityTameable;
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;


public class EntityTFLoyalZombie extends EntityTameable {

	/** MITE_BALANCE 7.5: the zombie burns away 900 ticks after it was summoned */
	public static final int LIFESPAN_TICKS = 900;

	private int lifeTicks;

	public EntityTFLoyalZombie(World par1World) {
		super(par1World);
		this.setSize(0.6F, 1.8F);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, true));
		this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(9, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityMob.class, 0, true));
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityanimal)
	{
		return null;
	}

	@Override
	protected int getTamingOutcome(EntityPlayer player)
	{
		// never tamed by the player, the zombie wand sets the owner directly
		return 1;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 7.5: HP 30, attack 6, speed 0.3)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(30.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.3D); // movement speed
		this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 6.0D); // attack damage (EntityTameable is not an EntityMob)
	}

	/**
	 * Rawr! Fixed damage, does not inherit the EntityMob frenzy.
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity)
	{
		EntityDamageResult result = par1Entity.attackEntityFrom(new Damage(DamageSource.causeMobDamage(this), 6.0F));

		// throw some enemies around!
		if (result != null && result.entityWasNegativelyAffected())
		{
			par1Entity.motionY += 0.2000000059604645D;
		}

		return result;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate()
	{
		// once our lifespan is over, start to burn
		if (!this.worldObj.isRemote)
		{
			if (this.lifeTicks < Integer.MAX_VALUE)
			{
				this.lifeTicks++;
			}
			if (this.lifeTicks >= LIFESPAN_TICKS)
			{
				this.setFire(100);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("LifeTicks", this.lifeTicks);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		this.lifeTicks = par1NBTTagCompound.getInteger("LifeTicks");
	}

	/**
	 * Determines if an entity can be despawned, used on idle far away entities
	 */
	@Override
	protected boolean canDespawn()
	{
		return !this.isTamed();
	}

	@Override
	public boolean fleesWhenBadlyWounded()
	{
		return false;
	}

	/**
	 * The original zombie had 3 armor points.
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return super.getNaturalDefense(damage_source) + (damage_source.bypassesMundaneArmor() ? 0.0F : 1.5F);
	}

	@Override
	public int getExperienceValue()
	{
		return 0;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound()
	{
		return "mob.zombie.say";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound()
	{
		return "mob.zombie.hurt";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound()
	{
		return "mob.zombie.death";
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	@Override
	protected void playStepSound(int par1, int par2, int par3, int par4)
	{
		this.worldObj.playSoundAtEntity(this, "mob.zombie.step", 0.15F, 1.0F);
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId()
	{
		return 0;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.UNDEAD;
	}
}
