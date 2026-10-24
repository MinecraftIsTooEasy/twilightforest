package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.entity.ai.EntityAITFChargeAttack;
import twilightforest.item.TFItems;

public class EntityTFMinotaur extends EntityMob implements ITFCharger {

	/** Melee damage (balance: 13) */
	protected static final double MELEE_DAMAGE = 13.0D;
	/** Damage dealt by the hit that ends a charge (balance: 15) */
	protected static final double CHARGE_DAMAGE = 15.0D;

	public EntityTFMinotaur(World par1World) {
		super(par1World);
		//this.texture = TwilightForestMod.MODEL_DIR + "minotaur.png";
        //this.moveSpeed = 0.25F;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAITFChargeAttack(this, 2.0F));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));

        this.setCurrentItemOrArmor(0, new ItemStack(Item.axeGold));
	}

	/**
	 * Set monster attributes
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(50.0D); // max health (balance)
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.25D); // movement speed
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(MELEE_DAMAGE); // attack damage (balance)
    }

    /**
     * Natural defense (balance: 2)
     */
    @Override
	public float getNaturalDefense(DamageSource damage_source)
    {
        return 2.0F;
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

    @Override
	public boolean isCharging()
    {
        return dataWatcher.getWatchableObjectByte(17) != 0;
    }

    @Override
	public void setCharging(boolean flag)
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
     * Charge hits deal more damage (balance: 15 instead of 13) and throw the target up.
     */
    @Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity)
    {
        boolean charging = this.isCharging();
        double base = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();
        EntityDamageResult result;

        if (charging)
        {
            this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(CHARGE_DAMAGE);
        }
        try
        {
            result = super.attackEntityAsMob(par1Entity);
        }
        finally
        {
            if (charging)
            {
                this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(base);
            }
        }

        if (result != null && result.entityWasNegativelyAffected() && charging)
        {
            par1Entity.motionY += 0.4000000059604645D;
            this.worldObj.playSoundAtEntity(this, "mob.irongolem.throw", 1.0F, 1.0F);
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
    	super.onLivingUpdate();

    	//when charging, move legs fast
    	if (isCharging())
    	{
			this.limbSwingAmount += 0.6F;
    	}

    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
	protected String getLivingSound()
    {
        return "mob.cow.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
	protected String getHurtSound()
    {
        return "mob.cow.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
	protected String getDeathSound()
    {
        return "mob.cow.hurt";
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
	protected void playStepSound(int par1, int par2, int par3, int par4)
    {
        this.worldObj.playSoundAtEntity(this, "mob.cow.step", 0.15F, 0.8F);
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
	protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.7F;
    }

    /**
     * Experience (balance: 14)
     */
    @Override
	public int getExperienceValue()
    {
        return 14;
    }

    /**
     * Returns the item ID for the item the mob drops on death.
     */
    @Override
	protected int getDropItemId()
    {
        return TFItems.meefRaw.itemID;
    }

    /**
     * Drop 0-1 items of this living's type (+ looting), and the rare maze map focus (balance: 1 %)
     */
    @Override
	protected void dropFewItems(boolean par1, DamageSource source)
    {
        int numDrops = this.rand.nextInt(2) + this.rand.nextInt(1 + source.getLootingModifier());

        for (int i = 0; i < numDrops; ++i)
        {
            if (this.isBurning())
            {
                this.dropItem(TFItems.meefSteak, 1);
            }
            else
            {
                this.dropItem(TFItems.meefRaw, 1);
            }
        }

        if (par1 && this.rand.nextFloat() < 0.01F)
        {
            this.dropRareDrop();
        }
    }

    protected void dropRareDrop()
    {
        this.dropItem(TFItems.mazeMapFocus, 1);
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

}
