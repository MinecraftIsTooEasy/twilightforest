package twilightforest.entity;

import net.minecraft.DamageSource;
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
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.Item;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.item.TFItems;


public class EntityTFHelmetCrab extends EntityMob {
	

    public EntityTFHelmetCrab(World world)
    {
        super(world);
        setSize(0.8F, 1.1F);
      
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.28F));
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

    }
    
    public EntityTFHelmetCrab(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
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
	 * Set monster attributes (balance: HP 26, attack 12)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(26.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 12.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 12;
	}

	@Override
	public float getReach()
	{
		return 1.5F + this.width * 0.75F;
	}

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
        this.playSound("mob.spider.step", 0.15F, 1.0F);
    }

    /**
     * Returns the item ID for the item the mob drops on death.
     */
    @Override
    protected int getDropItemId()
    {
        return TFItems.armorShard.itemID;
    }
    
    @Override
    protected void dropFewItems(boolean recentlyHit, DamageSource damageSource)
    {
    	super.dropFewItems(recentlyHit, damageSource);
    	
        if (rand.nextInt(2) == 0)
        {
            this.dropItem(Item.fishRaw, 1 + damageSource.getLootingModifier());
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
     * Will return how many at most can spawn in a chunk at once.
     */
    @Override
	public int getMaxSpawnedInChunk()
    {
        return 8;
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
     * Natural armor (1.7.10 getTotalArmorValue +6), balance: flat defense 3
     */
    @Override
    public float getNaturalDefense(DamageSource damageSource)
    {
        return super.getNaturalDefense(damageSource) + (damageSource.bypassesMundaneArmor() ? 0.0F : 3.0F);
    }

}
