package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.EntityAIAvoidEntity;
import net.minecraft.EntityAIHurtByTarget;
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
import twilightforest.entity.ai.EntityAITFMagicAttack;

public class EntityTFSlimeBeetle extends EntityMob
{

	public EntityTFSlimeBeetle(World world) {
		super(world);
		setSize(0.9F, 1.75F);

		this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAvoidEntity(this, EntityPlayer.class, 3.0F, 1.25F, 2.0F));
		this.tasks.addTask(3, new EntityAITFMagicAttack(this, 1.0F, EntityAITFMagicAttack.SLIME, 30));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
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
	 * Set monster attributes (balance: HP 28; its real attack is the slime projectile, 7 damage)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(28.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D); // movement speed
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 7.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 10;
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
	protected int getDropItemId()
    {
        return Item.slimeBall.itemID;
    }
	
}
