package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.biomes.TFBiomeBase;

public class EntityTFMosquitoSwarm extends EntityMob {

	public EntityTFMosquitoSwarm(World par1World) {
		super(par1World);

		setSize(.7F, 1.9F);
		this.stepHeight = 2.1f;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
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
	 * Set monster attributes (MITE_BALANCE 9.3: HP 14, attack 9)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(14.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(9.0D); // attack damage
	}

	@Override
	public int getExperienceValue() {
		return 8;
	}

	@Override
	protected String getLivingSound()
	{
		return TwilightForestMod.ID + ":mob.mosquito.mosquito";
	}


	/**
	 * Like cave spiders, but we add the hunger effect (Hunger I, 5 s, does not stack)
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity)
	{
		EntityDamageResult result = super.attackEntityAsMob(par1Entity);

		if (result != null && result.entityWasNegativelyAffected())
		{
			if (par1Entity instanceof EntityLivingBase)
			{
				EntityLivingBase target = (EntityLivingBase)par1Entity;

				if (!target.isPotionActive(Potion.hunger.id))
				{
					target.addPotionEffect(new PotionEffect(Potion.hunger.id, 5 * 20, 0));
				}
			}
		}

		return result;
	}

	/**
	 * We're allowed to spawn in bright light only in swamps
	 */
	@Override
	public boolean getCanSpawnHere(boolean perform_light_check)
	{
		// are we in the swamp
		if (worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ)) == TFBiomeBase.tfSwamp) {
			// don't check light level
			return worldObj.difficultySetting > 0 && worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0;
		}
		else {
			// normal EntityMob spawn check, checks light level
			return super.getCanSpawnHere(perform_light_check);
		}
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk()
	{
		// we are solitary creatures
		return 1;
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
