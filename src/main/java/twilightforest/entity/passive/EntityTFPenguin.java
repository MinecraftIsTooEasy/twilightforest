package twilightforest.entity.passive;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIFollowParent;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAIMate;
import net.minecraft.EntityAIPanic;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAITempt;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityAIWatchClosest2;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;


public class EntityTFPenguin extends EntityTFBird {

	public EntityTFPenguin(World world) {
		super(world);
		//texture = TwilightForestMod.MODEL_DIR + "penguin.png";
		
        this.setSize(0.5F, 0.9F);
		
		// reset tasks for fish
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.75F));
        tasks.addTask(2, new EntityAIMate(this, 1.0F));
        tasks.addTask(3, new EntityAITempt(this, 0.75F, Item.fishRaw.itemID, false));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.15F));
        tasks.addTask(5, new EntityAIWander(this, 1.0F));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
        tasks.addTask(7, new EntityAIWatchClosest2(this, EntityTFPenguin.class, 5F, 0.02F));
        tasks.addTask(8, new EntityAILookIdle(this));

	}
	
    /**
     * Returns the sound this mob makes while it's alive.
     */
	@Override
    protected String getLivingSound()
    {
        return null;//"mob.chicken";
    }
    
    
    /**
     * [This function is used when two same-species animals in 'love mode' breed to generate the new baby animal.]
     */
	@Override
	public EntityAgeable createChild(EntityAgeable entityanimal)
    {
        return new EntityTFPenguin(worldObj);
    }
	
    /**
     * Checks if the parameter is an item which this animal can be fed to breed it
     * (1.7.10 isBreedingItem; MITE isFoodItem).
     */
	@Override
    public boolean isFoodItem(ItemStack par1ItemStack)
    {
        return par1ItemStack != null && par1ItemStack.itemID == Item.fishRaw.itemID;
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
	 * Set monster attributes (MITE_BALANCE: 10 HP)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 10.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.movementSpeed, 0.2D);
    }
}
