package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySpider;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;


/**
 * The hedge spider is just like a normal spider, but it can spawn in the daytime.
 * 
 * @author Ben
 *
 */
public class EntityTFHedgeSpider extends EntitySpider {

	public EntityTFHedgeSpider(World world) {
		super(world);
		//texture = TwilightForestMod.MODEL_DIR + "hedgespider.png";
	}

    public EntityTFHedgeSpider(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

	/**
	 * Set monster attributes (MITE_BALANCE: T1 mob, 18 HP, 10 damage)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 18.0D);
		this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 10.0D);
	}

	@Override
	public int getExperienceValue()
	{
		return 7;
	}

	/**
	 * Kill at all times! (MITE spiders are normally peaceful in daylight outdoors; the vanilla
	 * findPlayerToAttack(float)/checkSwitchingToPeaceful both consult this.)
	 */
	@Override
	public boolean peacefulDuringDay()
	{
		return false;
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
	
    /**
     * Trigger achievement when killed
     */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		Entity killer = par1DamageSource.getResponsibleEntity();
		if (killer instanceof EntityPlayer) {
			((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHunter);
			// are in a hedge maze?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hedgeMaze) {
				// award hedge maze cheevo
				((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHedge);
			}
		}
	}
}
