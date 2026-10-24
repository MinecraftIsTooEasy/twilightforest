package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.EntityLivingData;
import net.minecraft.EntitySpider;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;

public class EntityTFKingSpider extends EntitySpider {

	public EntityTFKingSpider(World world) {
		super(world);
		//texture = TwilightForestMod.MODEL_DIR + "kingspider.png";
        this.setSize(1.6F, 1.6F);
        //this.moveSpeed = 0.35F;
        
        this.getNavigator().setAvoidsWater(true);
        // TODO(mite-port): the 1.7.10 new-AI task list (attack on collide, leap, wander, watch...) is dropped on purpose:
        // MITE spiders use the old EntityCreature AI and isAIEnabled()==true makes EntityArachnid.checkSwitchingToPeaceful
        // spam Minecraft.setErrorMessage (MITE_ENTITY 7.7). The old AI already provides chase, melee and the spider leap.
	}
    
	/**
	 * Set monster attributes (MITE_BALANCE: T3 mob, 40 HP, 14 damage, 1 natural defense)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 40.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 14.0D); // attack damage
        // a bit faster than a normal spider (TF: 0.35 vs 0.3)
        this.setEntityAttribute(SharedMonsterAttributes.movementSpeed, this.getEntityAttributeBaseValue(SharedMonsterAttributes.movementSpeed) * 1.15D);
    }

	@Override
	public int getExperienceValue()
	{
		return 15;
	}

	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return damage_source != null && damage_source.bypassesMundaneArmor() ? 0.0F : 1.0F;
	}

	/**
	 * Kill at all times!
	 */
	@Override
	public boolean peacefulDuringDay()
	{
		return false;
	}

    /**
     * How large the spider should be scaled.
     */
	//@Override
    public float spiderScaleAmount()
    {
        return 1.9F;
    }

	/**
	 * Actually only used for the shadow
	 */
	@Override
	public float getRenderSizeModifier() {
		 return 2.0F;
	}

    
    /**
     * returns true if this entity is by a ladder, false otherwise
     */
	@Override
    public boolean isOnLadder()
    {
    	// let's not climb
        return false;
    }
    
    /**
     * Init creature with mount
     */
	@Override
    public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData)
    {
        Object par1EntityLivingData1 = super.onSpawnWithEgg(par1EntityLivingData);

    	// always a spider jockey
        EntityTFSkeletonDruid druid = new EntityTFSkeletonDruid(this.worldObj);
        druid.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
        druid.onSpawnWithEgg((EntityLivingData)null);
        this.worldObj.spawnEntityInWorld(druid);
        druid.mountEntity(this);
        
        return (EntityLivingData)par1EntityLivingData1;
    }

    /**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
	@Override
    public double getMountedYOffset()
    {
        return (double)this.height * 0.5D;
    }

}
