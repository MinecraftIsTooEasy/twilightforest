package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.Item;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFBreathAttack;

public class EntityTFFireBeetle extends EntityMob implements IBreathAttacker 
{
	
    // balance (MITE_BALANCE 9.3): breath 4 damage + ignite 3 s
    public static final int BREATH_DURATION = 3;
    public static final int BREATH_DAMAGE = 4;


    public EntityTFFireBeetle(World world)
    {
        super(world);
        setSize(1.1F, .75F);

        this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAITFBreathAttack(this, 1.0F, 5F, 30, 0.1F));
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

    }
    
    public EntityTFFireBeetle(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(17, Byte.valueOf((byte)0));
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
	 * Set monster attributes (balance: HP 28, bite 11)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(28.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D); // movement speed
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 11.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 10;
	}

	/**
	 * Melee reach for the big body (MITE AI only attacks within getReach())
	 */
	@Override
	public float getReach()
	{
		return 1.5F + this.width * 0.75F;
	}

	// immune to fire (balance)
	@Override
	public boolean isHarmedByFire()
	{
		return false;
	}

	@Override
	public boolean isHarmedByLava()
	{
		return false;
	}

	@Override
	public boolean canCatchFire()
	{
		return false;
	}
	
    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
	protected String getLivingSound()
    {
        return null;
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
	protected String getHurtSound()
    {
        return "mob.spider.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
	protected String getDeathSound()
    {
        return "mob.spider.death";
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
	protected void playStepSound(int x, int y, int z, int blockId)
    {
        this.worldObj.playSoundAtEntity(this, "mob.spider.step", 0.15F, 1.0F);
    }


    @Override
	protected int getDropItemId()
    {
        return Item.gunpowder.itemID;
    }
    
    @Override
	public boolean isBreathing()
    {
        return dataWatcher.getWatchableObjectByte(17) != 0;
    }

    @Override
	public void setBreathing(boolean flag)
    {
        if (flag)
        {
            dataWatcher.updateObject(17, Byte.valueOf((byte)127));
        }
        else
        {
            dataWatcher.updateObject(17, Byte.valueOf((byte)0));
        }
    }

    
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
	public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    	
    	// when breathing fire, spew particles
    	if (isBreathing())
    	{
    		Vec3 look = this.getLookVec();

    		double dist = 0.9;
    		double px = this.posX + look.xCoord * dist;
    		double py = this.posY + 0.25 + look.yCoord * dist;
    		double pz = this.posZ + look.zCoord * dist;

    		for (int i = 0; i < 2; i++)
    		{
    			double dx = look.xCoord;
    			double dy = look.yCoord;
    			double dz = look.zCoord;

    			double spread = 5 + this.getRNG().nextDouble() * 2.5;
    			double velocity = 0.15 + this.getRNG().nextDouble() * 0.15;

    			// spread flame
    			dx += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dy += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dz += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dx *= velocity;
    			dy *= velocity;
    			dz *= velocity;

    			TFParticles.spawn(worldObj, getFlameParticle(), px, py, pz, dx, dy, dz);
    		}
    		
			playBreathSound();
    	}

    }

    public String getFlameParticle() {
		return "flame";
	}

	public void playBreathSound() {
		worldObj.playSoundEffect(this.posX + 0.5, this.posY + 0.5, this.posZ + 0.5, "mob.ghast.fireball", rand.nextFloat() * 0.5F, rand.nextFloat() * 0.5F);
	}
    
	@Override
	public int getBrightnessForRender(float par1) {
		if (isBreathing())
		{
			return 15728880;
		}
		else
		{
			return super.getBrightnessForRender(par1);
		}
	}

	/**
     * Trigger achievement when killed
     */
    @Override
    public void onDeath(DamageSource par1DamageSource) {
    	super.onDeath(par1DamageSource);
    	if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
    		((EntityPlayer)par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
    	}
    }

	/**
	 * Rather than speed, this seems to control how far up or down the heads can tilt?
	 */
	@Override
    public int getVerticalFaceSpeed()
    {
        return 500;
    }

	@Override
	public float getShadowSize() 
	{
		return 1.1F;
	}
	
	@Override
	public float getEyeHeight() {
		return 0.25F;
	}

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    @Override
	public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }

	@Override
	public void doBreathAttack(Entity target) 
	{
		// only living targets: Entity.isHarmedByFire() on arbitrary entities spams MITE's error message
		if (target instanceof EntityLivingBase && target.isHarmedByFire() && TFDamage.hurt(target, DamageSource.inFire, BREATH_DAMAGE))
    	{
    		target.setFire(BREATH_DURATION);
    	}
	}

	/** Hollow hill spawners sit among lava and glowstone: MITE's spawner check needs a path weight >= 0, which bright light makes negative. */
	@Override
	public float getBlockPathWeight(int x, int y, int z) {
		float weight = super.getBlockPathWeight(x, y, z);
		return this.came_from_spawner ? Math.max(0.0F, weight) : weight;
	}
}
