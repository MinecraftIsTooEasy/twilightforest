package twilightforest.entity;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
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
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.IRangedAttackMob;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFWorldUtil;
import twilightforest.entity.ai.EntityAITFCollideAttackFixed;
import twilightforest.entity.boss.EntityTFIceBomb;
import twilightforest.item.TFItems;

public class EntityTFTroll extends EntityMob implements IRangedAttackMob
{
    
    /** DataWatcher slot, allocated dynamically (MITE subclasses occupy fixed low ids). */
    private int rockFlag;

    private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
    private EntityAITFCollideAttackFixed aiAttackOnCollide = new EntityAITFCollideAttackFixed(this, EntityPlayer.class, 1.2D, false);

	public EntityTFTroll(World par1World)
    {
        super(par1World);
        this.setSize(1.4F, 2.4F);

        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIRestrictSun(this));
        this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

        if (par1World != null && !par1World.isRemote)
        {
            this.setCombatTask();
        }
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
	 * Set monster attributes (MITE_BALANCE 9.3 T5: HP 60, melee 16)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(60.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(16.0D);
    }

	/** MITE_BALANCE 9.3 T5: natural defense 2, none against magic / mundane-armor-bypassing damage. */
	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return damage_source.bypassesMundaneArmor() ? 0.0F : 2.0F;
	}

	/** MITE_BALANCE 9.3 T5: 18 XP. */
	@Override
	public int getExperienceValue()
	{
		return 18;
	}

	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.rockFlag = this.dataWatcher.getNextAvailableId();
        this.dataWatcher.addObject(this.rockFlag, Byte.valueOf((byte)0));
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
	public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    }


    /**
     * Determines whether this troll has a rock or not.
     */
    public boolean hasRock() {
        return (this.dataWatcher.getWatchableObjectByte(this.rockFlag) & 2) != 0;
    }

    /**
     * Sets whether this troll has a rock or not.
     */
    public void setHasRock(boolean rock) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(this.rockFlag);

        if (rock) {
            this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(40.0D);
            this.dataWatcher.updateObject(this.rockFlag, Byte.valueOf((byte)(b0 | 2)));
        } else {
            this.dataWatcher.updateObject(this.rockFlag, Byte.valueOf((byte)(b0 & -3)));
        }

        this.setCombatTask();
    }

    /**
     * Swing!
     */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {

        this.swingArm();

		return super.attackEntityAsMob(par1Entity);
	}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
	@Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("HasRock", this.hasRock());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
	@Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setHasRock(par1NBTTagCompound.getBoolean("HasRock"));
    }


    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask()
    {
        this.tasks.removeTask(this.aiAttackOnCollide);
        this.tasks.removeTask(this.aiArrowAttack);

        if (this.hasRock()) {
            this.tasks.addTask(4, this.aiArrowAttack);
        } else {
            this.tasks.addTask(4, this.aiAttackOnCollide);
        }
    }

    /**
	 * handles entity death timer, experience orb and particle creation
	 */
	@Override
	protected void onDeathUpdate() {
		super.onDeathUpdate();

		if (this.deathTime % 5 == 0) {
			this.ripenTrollBerNearby(this.deathTime / 5);
		}

		if (this.deathTime == 1) {
			//this.makeTrollStoneInAABB(this.boundingBox);
		}
	}


	private void ripenTrollBerNearby(int offset) {
		int sx = MathHelper.floor_double(this.posX);
		int sy = MathHelper.floor_double(this.posY);
		int sz = MathHelper.floor_double(this.posZ);

		int range = 12;
		for (int dx = -range; dx < range; dx++) {
			for (int dy = -range; dy < range; dy++) {
				for (int dz = -range; dz < range; dz++) {
					int cx = sx + dx;
					int cy = sy + dy;
					int cz = sz + dz;

					ripenBer(offset, cx, cy, cz);
				}
			}
		}
	}


	private void ripenBer(int offset, int cx, int cy, int cz) {
		if (TFWorldUtil.is(this.worldObj, cx, cy, cz, TFBlocks.unripeTrollBer) && this.rand.nextBoolean() && (Math.abs(cx + cy + cz) % 5 == offset)) {
			TFWorldUtil.setBlock(this.worldObj, cx, cy, cz, TFBlocks.trollBer);
			worldObj.playAuxSFX(2004, cx, cy, cz, 0);

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
     * Turns blocks to trollstone inside the given bounding box.
     */
    private void makeTrollStoneInAABB(AxisAlignedBB par1AxisAlignedBB)
    {
        int minX = MathHelper.ceiling_double_int(par1AxisAlignedBB.minX);
        int minY = MathHelper.ceiling_double_int(par1AxisAlignedBB.minY);
        int minZ = MathHelper.ceiling_double_int(par1AxisAlignedBB.minZ);
        int maxX = MathHelper.floor_double(par1AxisAlignedBB.maxX);
        int maxY = MathHelper.floor_double(par1AxisAlignedBB.maxY);
        int maxZ = MathHelper.floor_double(par1AxisAlignedBB.maxZ);

        for (int dx = minX; dx <= maxX; ++dx)
        {
            for (int dy = minY; dy <= maxY; ++dy)
            {
                for (int dz = minZ; dz <= maxZ; ++dz)
                {
                    Block current = this.worldObj.getBlock(dx, dy, dz);

                    if (current == null)
                    {
                    	TFWorldUtil.setBlock(this.worldObj, dx, dy, dz, TFBlocks.trollSteinn);
                        // here, this effect will have to do
            			worldObj.playAuxSFX(2001, dx, dy, dz, TFWorldUtil.id(TFBlocks.trollSteinn) + (0 << 12));
                     }
                }
            }
        }
    }

    @Override
    protected int getDropItemId()
    {
        return 0;
    }

    /**
     * No regular drops; the rare magic beans drop is 1% and ignores looting (MITE_BALANCE 9.3 T5).
     */
    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
    {
        if (recently_hit_by_player && this.rand.nextDouble() < twilightforest.TFConfig.beansTrollDropChance) {
            this.dropItem(TFItems.magicBeans, 1);
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float par2)
    {
    	if (this.hasRock()) {

    		EntityTFIceBomb ice = new EntityTFIceBomb(this.worldObj, this);

    		double d0 = target.posX - this.posX;
    		double d1 = target.posY + (double)target.getEyeHeight() - 1.100000023841858D - target.posY;
    		double d2 = target.posZ - this.posZ;
    		float f1 = MathHelper.sqrt_double(d0 * d0 + d2 * d2) * 0.2F;
    		ice.setThrowableHeading(d0, d1 + (double)f1, d2, 0.75F, 12.0F);

    		this.playSound("random.bow", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
    		this.worldObj.spawnEntityInWorld(ice);
    	}
    }

}
