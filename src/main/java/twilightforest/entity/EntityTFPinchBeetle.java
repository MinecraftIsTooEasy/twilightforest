package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.entity.ai.EntityAITFChargeAttack;
import twilightforest.entity.ai.EntityAITFKidnapRider;

public class EntityTFPinchBeetle extends EntityMob
{
	/** Balance: a kidnapped rider is dropped after at most 60 ticks (3 s). */
	public static final int MAX_KIDNAP_TICKS = 60;
	/** Ticks the beetle will not grab anybody after letting go. */
	public static final int KIDNAP_COOLDOWN = 60;

	private int kidnapTicks;
	private int kidnapCooldown;

	public EntityTFPinchBeetle(World world) {
		super(world);
		setSize(1.2F, 1.1F);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAITFKidnapRider(this, 2.0F));
		this.tasks.addTask(2, new EntityAITFChargeAttack(this, 2.0F));
		this.tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
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
	 * Set monster attributes (balance: HP 40, attack 13, defense 2)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D); // movement speed
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 13.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 12;
	}

	/**
	 * Natural armor (1.7.10 getTotalArmorValue +2), balance: flat defense 2
	 */
	@Override
	public float getNaturalDefense(DamageSource damageSource)
	{
		return super.getNaturalDefense(damageSource) + (damageSource.bypassesMundaneArmor() ? 0.0F : 2.0F);
	}

	@Override
	public float getReach()
	{
		return 1.5F + this.width * 0.75F;
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

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
	public void onLivingUpdate()
    {
    	if (this.kidnapCooldown > 0)
    	{
    		this.kidnapCooldown--;
    	}

    	if (this.riddenByEntity != null)
    	{
    		this.setSize(1.9F, 2.0F);
    		
            // stop player sneaking so that they can't dismount!
            if (this.riddenByEntity.isSneaking())
            {
            	this.riddenByEntity.setSneaking(false);
            }

            // balance: the rider is let go after at most MAX_KIDNAP_TICKS
            this.kidnapTicks++;
            if (!this.worldObj.isRemote && this.kidnapTicks >= MAX_KIDNAP_TICKS)
            {
            	this.releaseRider();
            }
    	}
    	else
    	{
    		this.kidnapTicks = 0;
    		this.setSize(1.2F, 1.1F);
    	}
    	
    	super.onLivingUpdate();

    	// look at things in our jaws
    	if (this.riddenByEntity != null)
    	{
            this.getLookHelper().setLookPositionWithEntity(riddenByEntity, 100F, 100F);

            // push out of user in wall
            Vec3 riderPos = this.getRiderPosition();
            this.pushOutOfRider(riderPos);
    	}
    }

    /**
     * Drop whoever we are carrying and refuse to grab again for a moment
     */
    public void releaseRider()
    {
    	Entity rider = this.riddenByEntity;
    	if (rider != null)
    	{
    		rider.mountEntity(null);
    	}
    	this.kidnapTicks = 0;
    	this.kidnapCooldown = KIDNAP_COOLDOWN;
    }

    /**
     * 1.7.10 func_145771_j(x, y, z) pushed this entity out of blocks at the given point. MITE's pushOutOfBlocks() has no
     * position argument, so when the rider's position is inside a solid block we nudge ourselves away from it.
     */
    private void pushOutOfRider(Vec3 riderPos)
    {
    	int bx = (int) Math.floor(riderPos.xCoord);
    	int by = (int) Math.floor(riderPos.yCoord);
    	int bz = (int) Math.floor(riderPos.zCoord);

    	if (this.worldObj.blockExists(bx, by, bz) && this.worldObj.isBlockFullSolidCube(bx, by, bz))
    	{
    		// TODO(mite-port): approximation of func_145771_j(x, y, z); MITE only has pushOutOfBlocks() for the own position
    		this.motionX += (this.posX - riderPos.xCoord) * 0.1D;
    		this.motionZ += (this.posZ - riderPos.zCoord) * 0.1D;
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

	@Override
	public float getShadowSize() 
	{
		return 1.1F;
	}
	
	/**
	 * Pick up things we attack!
	 */
    @Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) 
    {
    	if (!this.worldObj.isRemote && this.kidnapCooldown <= 0 && this.riddenByEntity == null && par1Entity.ridingEntity == null)
    	{
    		par1Entity.mountEntity(this);
    		this.kidnapTicks = 0;
    	}
    	
		return super.attackEntityAsMob(par1Entity);
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
    
    /**
     * Put the player out in front of where we are
     */
    @Override
	public void updateRiderPosition()
    {
        if (this.riddenByEntity != null)
        {
        	Vec3 riderPos = this.getRiderPosition();
        	
            this.riddenByEntity.setPosition(riderPos.xCoord, riderPos.yCoord, riderPos.zCoord);
        }
    }
    
    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    @Override
	public double getMountedYOffset()
    {
        return 0.75D;
    }
    
    /**
     * Used to both get a rider position and to push out of blocks
     */
    public Vec3 getRiderPosition()
    {
    	if (this.riddenByEntity != null)
    	{
    		float distance = 0.9F;

    		double var1 = Math.cos((this.rotationYaw + 90) * Math.PI / 180.0D) * distance;
    		double var3 = Math.sin((this.rotationYaw + 90) * Math.PI / 180.0D) * distance;

    		return Vec3.createVectorHelper(this.posX + var1, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + var3);
    	}
    	else
    	{
    		return Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
    	}
    }
    
    /**
     * If a rider of this entity can interact with this entity. Forge hook in 1.7.10; MITE's raycast does not filter the
     * ridden entity, so this is only kept for API compatibility (nothing calls it).
     */
    public boolean canRiderInteract()
    {
        return true;
    }
}
