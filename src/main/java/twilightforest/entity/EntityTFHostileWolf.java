package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityMob;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityWolf;
import net.minecraft.IMob;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;


public class EntityTFHostileWolf extends EntityWolf implements IMob {

	public EntityTFHostileWolf(World world) {
		super(world);
		// MITE's wolf is angry/hostile via its "hostile to players" flag (server side, synced through the DataWatcher)
		// world is null when MITE's ReferenceFileWriter builds every mob class at startup
		if (world != null && !world.isRemote) {
			setHostileToPlayers(true);
		}
		
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}
	
    public EntityTFHostileWolf(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }
    

	/**
	 * Set monster attributes (MITE_BALANCE: T1 mob, 16 HP, 10 damage)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 16.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 10.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 6;
	}

	/** IMob (MITE): hostile wolves may spawn in shallow water like other mobs. */
	@Override
	public boolean canSpawnInShallowWater()
	{
		return true;
	}

	/** Always snarling. */
	@Override
	public boolean looksAngry()
	{
		return true;
	}

    @Override
	public void onUpdate()
    {
        super.onUpdate();
        if (!worldObj.isRemote)
        {
        	if (worldObj.difficultySetting == 0)
        	{
        		setDead();
        	}
        	else if (!isHostileToPlayers())
        	{
        		// MITE's wolf calms down when its target countdown runs out; ours never does
        		setHostileToPlayers(true);
        	}
        }
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
     * Checks if the entity's current position is a valid location to spawn this entity.
     * (Replaces the EntityAnimal grass/light rules with the TF ones.)
     */
    @Override
	public boolean getCanSpawnHere(boolean perform_light_check)
    {
		if (worldObj.difficultySetting == 0) {
			return false;
		}
		// are we near a hedge maze?
		int chunkX = MathHelper.floor_double(posX) >> 4;
		int chunkZ = MathHelper.floor_double(posZ) >> 4;
		boolean inMaze = TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze;
		// don't check light level in a maze
		if (perform_light_check && !inMaze && !isValidLightLevel()) {
			return false;
		}
		return worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0 && !worldObj.isAnyLiquid(boundingBox);
    }
    
    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    @Override
    protected boolean isValidLightLevel()
    {
        return EntityMob.isValidLightLevel(this);
    }

    /**
     * Not edible, not tameable.
     */
    @Override
	public boolean isFoodItem(ItemStack par1ItemStack)
    {
    	return false;
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
	public boolean onEntityRightClicked(EntityPlayer player, ItemStack item_stack)
    {
    	return false;
    }

}
