
package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
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
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.EnumSkyBlock;
import net.minecraft.IRangedAttackMob;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.item.TFItems;



public class EntityTFSkeletonDruid extends EntityMob implements IRangedAttackMob
{

	public EntityTFSkeletonDruid(World world)
	{
		super(world);
		//texture = TwilightForestMod.MODEL_DIR + "skeletondruid.png";

		//this.moveSpeed = 0.25F;
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIRestrictSun(this));
		this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
		this.tasks.addTask(4, new EntityAIArrowAttack(this, 1.0D, 60, 10.0F));
		this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		
        this.setCurrentItemOrArmor(0, new ItemStack(Item.hoeGold));

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
	 * Set monster attributes (MITE_BALANCE: T3 mob, 28 HP, melee 6)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 28.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.movementSpeed, 0.25D); // movement speed
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 6.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 12;
	}

    /**
     * Returns the sound this mob makes while it's alive.
     */
	@Override
    protected String getLivingSound()
    {
        return "mob.skeleton.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
	@Override
    protected String getHurtSound()
    {
        return "mob.skeleton.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
	@Override
    protected String getDeathSound()
    {
        return "mob.skeleton.death";
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
	@Override
    protected void playStepSound(int x, int y, int z, int block_id)
    {
        this.makeSound("mob.skeleton.step", 0.15F, 1.0F);
    }

	@Override
	protected int getDropItemId()
    {
        return TFItems.torchberries.itemID;
    }
    
    /**
     * Drop 0-2 items of this living's type.
     */
	@Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
    {
		if (!recently_hit_by_player)
		{
			return;
		}

    	int lootingModifier = damage_source == null ? 0 : damage_source.getLootingModifier();
    	int numberOfItemsToDrop;
    	int i;

    	numberOfItemsToDrop = this.rand.nextInt(3 + lootingModifier);

    	for (i = 0; i < numberOfItemsToDrop; ++i)
    	{
    		this.dropItem(TFItems.torchberries.itemID, 1);
    	}

    	numberOfItemsToDrop = this.rand.nextInt(3 + lootingModifier);

    	for (i = 0; i < numberOfItemsToDrop; ++i)
    	{
    		this.dropItem(Item.bone.itemID, 1);
    	}
    }
    
    /**
     * Get this Entity's EnumCreatureAttribute
     */
	@Override
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
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
     * Checks to make sure the light is not too bright where the mob is spawning
     */
	@Override
    protected boolean isValidLightLevel()
    {
        // MITE: spawner mobs skip the light check (only sunlight that would set them on fire stops them)
        if (this.came_from_spawner) {
            return EntityMob.isValidLightLevel(this);
        }

    	boolean valid = false;
        int dx = MathHelper.floor_double(this.posX);
        int dy = MathHelper.floor_double(this.boundingBox.minY);
        int dz = MathHelper.floor_double(this.posZ);

        if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, dx, dy, dz) > this.rand.nextInt(32))
        {
        	valid = false;
        }
        else
        {
            int light = this.worldObj.getBlockLightValue(dx, dy, dz);

             valid = light <= this.rand.nextInt(12);

        }
    	return valid;
    }
}
