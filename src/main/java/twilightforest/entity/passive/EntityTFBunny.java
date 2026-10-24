package twilightforest.entity.passive;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAvoidEntity;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAIPanic;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAITempt;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityCreature;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumCreatureType;
import net.minecraft.IAnimals;
import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.compat.TFBlockUtil;


public class EntityTFBunny extends EntityCreature implements IAnimals {

	public EntityTFBunny(World par1World) {
		super(par1World);
		//texture = TwilightForestMod.MODEL_DIR + "bunnydutch.png";
		
        this.setSize(0.3F, 0.7F);
		
		// maybe this will help them move cuter?
		this.stepHeight = 1;
		
		// squirrel AI
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 2.0F));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0F, Item.seeds.itemID, true));
        this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityPlayer.class, 2.0F, 0.8F, 1.33F));
        this.tasks.addTask(5, new EntityAIWander(this, 0.8F));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0F));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
        this.tasks.addTask(8, new EntityAILookIdle(this));

        // random color
        setBunnyType(rand.nextInt(4));

	}


	/**
	 * Set monster attributes (MITE_BALANCE 9.3: 3 health)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(3.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.3D);
    }
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
    }

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
	    return true;
	}

	/**
	 * MITE only knows EntityAnimal for the animal spawn category; this is an IAnimals EntityCreature.
	 */
	@Override
	public EnumCreatureType getCreatureType() {
		return EnumCreatureType.animal;
	}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
	@Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("BunnyType", this.getBunnyType());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
	@Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setBunnyType(par1NBTTagCompound.getInteger("BunnyType"));
    }

    public int getBunnyType()
    {
        return this.dataWatcher.getWatchableObjectByte(16);
    }

    public void setBunnyType(int par1)
    {
        this.dataWatcher.updateObject(16, Byte.valueOf((byte)par1));
    }
	

	/**
	 * Actually only used for the shadow
	 */
	@Override
	public float getRenderSizeModifier() {
		 return 0.3F;
	}
	
	/**
     * Determines if an entity can be despawned, used on idle far away entities
     */
	@Override
    protected boolean canDespawn()
    {
        return false;
    }

	/**
	 * Passive critters give no (or almost no) experience (MITE_BALANCE 9.3: XP 0-1).
	 */
	@Override
	public int getExperienceValue() {
		return this.rand.nextInt(2);
	}
	
	
    /**
     * Takes a coordinate in and returns a weight to determine how likely this creature will try to path to the block.
     * Args: x, y, z
     */
	@Override
    public float getBlockPathWeight(int par1, int par2, int par3)
    {
    	// avoid leaves & wood
		if (TFBlockUtil.isLeaves(this.worldObj, par1, par2 - 1, par3)) {
			return -1.0F;
		}
		Material underMaterial = this.worldObj.getBlockMaterial(par1, par2 - 1, par3);
		if (underMaterial == Material.wood) {
			return -1.0F;
		}
		if (underMaterial == Material.grass) {
			return 10.0F;
		}
		// default to just prefering lighter areas
		return this.worldObj.getLightBrightness(par1, par2, par3) - 0.5F;
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
}
