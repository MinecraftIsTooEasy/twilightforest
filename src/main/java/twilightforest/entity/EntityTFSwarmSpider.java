package twilightforest.entity;

import java.util.List;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySpider;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;


public class EntityTFSwarmSpider extends EntitySpider {

	/** MITE_BALANCE: at most this many live swarm spiders within 16 blocks. */
	private static final int MAX_NEARBY = 6;
	
	protected boolean shouldSpawn = false;
	
	public EntityTFSwarmSpider(World world) {
		this(world, true);
	}
	
	public EntityTFSwarmSpider(World world, boolean spawnMore) {
		super(world);
		
		setSize(0.8F, 0.4F);
		setSpawnMore(spawnMore);
		//texture = TwilightForestMod.MODEL_DIR + "swarmspider.png";
	}
	
    public EntityTFSwarmSpider(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

	/**
	 * Set monster attributes (MITE_BALANCE: T1 mob, 4 HP, 4 damage)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 4.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 4.0D); // attack damage
    }

	/** XP value */
	@Override
	public int getExperienceValue()
	{
		return 1;
	}

    /**
     * How large the spider should be scaled.
     */
	//@Override
    public float spiderScaleAmount()
    {
        return 0.5F;
    }

	/**
	 * Actually only used for the shadow
	 */
	@Override
	public float getRenderSizeModifier() {
		 return 0.5F;
	}

	/**
	 * Spawn more if that flag is set.
	 */
	@Override
	public void onUpdate() {
		if (shouldSpawnMore()) {
			// don't spawn if we're connected in multiplayer 
			if (!worldObj.isRemote) {
				int more = 1 + rand.nextInt(2);
				for (int i = 0; i < more; i++) {
					// try twice to spawn
					if (!spawnAnother()) {
						spawnAnother();
					}
				}
			}
			setSpawnMore(false);
		}

		super.onUpdate();
	}
    
	/**
	 * Only have the spider do damage a fraction of the time
	 */
	@Override
    protected void attackEntity(Entity entity, float f)
    {
    	if (this.attackTime <= 0 && (!this.isAirBorne || rand.nextInt(4) != 0))
        {
            this.attackTime = 20;
        }
    	else
    	{
    		super.attackEntity(entity, f);
    	}

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
	 * Spawn another spider!
	 * 
	 * @return
	 */
	protected boolean spawnAnother() {
		// MITE_BALANCE: cap the swarm
		List nearby = worldObj.getEntitiesWithinAABB(EntityTFSwarmSpider.class, boundingBox.expand(16.0D, 16.0D, 16.0D));
		if (nearby.size() >= MAX_NEARBY) {
			return false;
		}

		EntityTFSwarmSpider another = new EntityTFSwarmSpider(worldObj, false);


		double sx = posX + (rand.nextBoolean() ? 0.9 : -0.9);
		double sy = posY;
		double sz = posZ + (rand.nextBoolean() ? 0.9 : -0.9);
		another.setLocationAndAngles(sx, sy, sz, rand.nextFloat() * 360F, 0.0F);
		// the parent already passed the light check, so only test for room
		if(!another.getCanSpawnHere(false))
		{
			another.setDead();
			return false;
		}
		worldObj.spawnEntityInWorld(another);
		
		return true;
	}

	/**
     * Checks to make sure the light is not too bright where the mob is spawning
	 */
	@Override
    protected boolean isValidLightLevel()
    {
		int chunkX = MathHelper.floor_double(posX) >> 4;
		int chunkZ = MathHelper.floor_double(posZ) >> 4;
		// We're allowed to spawn in bright light only in hedge mazes.
		if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze) 
		{
			return true;
		}
		else
		{
			return super.isValidLightLevel();
		}
    }
	
    public boolean shouldSpawnMore()
    {
    	return shouldSpawn;
    }

    public void setSpawnMore(boolean flag)
    {
    	this.shouldSpawn = flag;
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setBoolean("SpawnMore", shouldSpawnMore());
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setSpawnMore(nbttagcompound.getBoolean("SpawnMore"));
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
			// are we in a hedge maze?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze) {
				// award hedge maze cheevo
				((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHedge);
			}
		}
	}
	
    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
	protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F;
    }
	
    /**
     * Will return how many at most can spawn in a chunk at once.
     */
	@Override
    public int getMaxSpawnedInChunk()
    {
        return 16;
    }

	/** Hollow hill spawners sit among lava and glowstone: MITE's spawner check needs a path weight >= 0, which bright light makes negative. */
	@Override
	public float getBlockPathWeight(int x, int y, int z) {
		float weight = super.getBlockPathWeight(x, y, z);
		return this.came_from_spawner ? Math.max(0.0F, weight) : weight;
	}
}
