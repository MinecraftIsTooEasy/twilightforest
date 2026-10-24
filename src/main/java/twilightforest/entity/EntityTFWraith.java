package twilightforest.entity;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityFlying;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumSkyBlock;
import net.minecraft.IMob;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;


public class EntityTFWraith extends EntityFlying implements IMob {

	public EntityTFWraith(World world)
	{
		super(world);
	}

	public EntityTFWraith(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3: HP 24, attack 12)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(24.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.5D); // movement speed

		// need to initialize damage since we're not an EntityMob
		this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 12.0D); // attack damage
	}

	/**
	 * MITE_BALANCE E2: AI mode is enabled so the movement speed attribute and getTarget() behave; the ghast style
	 * movement is driven from updateAITasks().
	 */
	@Override
	protected boolean isAIEnabled()
	{
		return true;
	}

	@Override
	public int getExperienceValue()
	{
		return 10;
	}

	@Override
	public boolean canSpawnInShallowWater()
	{
		return true;
	}

	@Override
	public boolean fleesWhenBadlyWounded()
	{
		return false;
	}

	/**
	 * Supress walking sounds
	 */
	@Override
	public boolean canTriggerWalking()
	{
		return false;
	}

	/**
	 * Adapted from the ghast code.  The ghost wanders randomly until it targets a player, at which point it moves towards the player.
	 *
	 * TODO: pathfinding!
	 */
	@Override
	protected void updateAITasks()
	{
		if(!worldObj.isRemote && worldObj.difficultySetting == 0)
		{
			setDead();
		}
		tryDespawnEntity();
		prevAttackCounter = attackCounter;
		double d = waypointX - posX;
		double d1 = waypointY - posY;
		double d2 = waypointZ - posZ;
		double d3 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
		if(d3 < 1.0D || d3 > 60D)
		{
			waypointX = posX + (rand.nextFloat() * 2.0F - 1.0F) * 16F;
			waypointY = posY + (rand.nextFloat() * 2.0F - 1.0F) * 16F;
			waypointZ = posZ + (rand.nextFloat() * 2.0F - 1.0F) * 16F;
		}
		if(courseChangeCooldown-- <= 0)
		{
			courseChangeCooldown += rand.nextInt(5) + 2;
			if(isCourseTraversable(waypointX, waypointY, waypointZ, d3))
			{
				motionX += (d / d3) * 0.10000000000000001D;
				motionY += (d1 / d3) * 0.10000000000000001D;
				motionZ += (d2 / d3) * 0.10000000000000001D;
			} else
			{
				waypointX = posX;
				waypointY = posY;
				waypointZ = posZ;

				// clear target?
				targetedEntity = null;
			}
		}
		if(targetedEntity != null && targetedEntity.isDead)
		{
			targetedEntity = null;
		}
		if(targetedEntity == null || aggroCooldown-- <= 0)
		{
			targetedEntity = findPlayerToAttack();
			if(targetedEntity != null)
			{
				aggroCooldown = 20;
			}
		} else {
			float f1 = targetedEntity.getDistanceToEntity(this);
			if(canSeeEntity(targetedEntity))
			{
				attackEntity(targetedEntity, f1);
			} else
			{
				attackBlockedEntity(targetedEntity, f1);
			}
		}
		double d4 = 64D;
		if(targetedEntity != null && targetedEntity.getDistanceSqToEntity(this) < d4 * d4)
		{
			double d5 = targetedEntity.posX - posX;
			double d7 = targetedEntity.posZ - posZ;
			renderYawOffset = rotationYaw = (-(float)Math.atan2(d5, d7) * 180F) / 3.141593F;
			if(canSeeEntity(targetedEntity))
			{
				attackCounter++;
				if(attackCounter == 20)
				{
					waypointX = targetedEntity.posX;
					waypointY = targetedEntity.posY - targetedEntity.height + 0.5;
					waypointZ = targetedEntity.posZ;

					attackCounter = -40;
				}
			} else
			if(attackCounter > 0)
			{
				attackCounter--;
			}
		} else
		{
			renderYawOffset = rotationYaw = (-(float)Math.atan2(motionX, motionZ) * 180F) / 3.141593F;
			if(attackCounter > 0)
			{
				attackCounter--;
			}
		}
	}

	protected void attackEntity(Entity entity, float f)
	{
		if(attackTime <= 0 && f < 2.0F && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY)
		{
			attackTime = 20;

			float damage = (float)this.getEntityAttributeValue(SharedMonsterAttributes.attackDamage);

			entity.attackEntityFrom(new Damage(DamageSource.causeMobDamage(this), damage));
		}
	}

	protected void attackBlockedEntity(Entity entity, float f)
	{
	}


	/**
	 * Adapted from EntityMob
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
	{
		EntityDamageResult result = super.attackEntityFrom(damage);
		if(result != null && result.entityWasNegativelyAffected())
		{
			Entity entity = damage.getSource().getResponsibleEntity();
			if(riddenByEntity == entity || ridingEntity == entity)
			{
				return result;
			}
			if(entity != this)
			{
				targetedEntity = entity;
			}
		}
		return result;
	}

	/**
	 * Copied from EntityMob
	 */
	protected Entity findPlayerToAttack()
	{
		EntityPlayer entityplayer = this.getClosestVulnerablePlayer(16D);
		if(entityplayer != null && canSeeEntity(entityplayer))
		{
			return entityplayer;
		} else
		{
			return null;
		}
	}

	private boolean isCourseTraversable(double d, double d1, double d2, double d3)
	{
		double d4 = (waypointX - posX) / d3;
		double d5 = (waypointY - posY) / d3;
		double d6 = (waypointZ - posZ) / d3;
		AxisAlignedBB axisalignedbb = boundingBox.copy();
		for(int i = 1; i < d3; i++)
		{
			axisalignedbb.offset(d4, d5, d6);
			if(worldObj.getCollidingBoundingBoxes(this, axisalignedbb).size() > 0)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	protected String getLivingSound()
	{
		return TwilightForestMod.ID + ":mob.wraith.wraith";
	}

	@Override
	protected String getHurtSound()
	{
		return TwilightForestMod.ID + ":mob.wraith.wraith";
	}

	@Override
	protected String getDeathSound()
	{
		return TwilightForestMod.ID + ":mob.wraith.wraith";
	}

	@Override
	protected int getDropItemId()
	{
		return Item.glowstone.itemID;
	}

	public int courseChangeCooldown;
	public double waypointX;
	public double waypointY;
	public double waypointZ;
	private Entity targetedEntity;
	private int aggroCooldown;
	public int prevAttackCounter;
	public int attackCounter;

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)par1DamageSource.getResponsibleEntity();
			player.triggerAchievement(TFAchievementPage.twilightHunter);
			// are we in a level 3 hill?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hill3) {
				// award level 3 hill cheevo
				player.triggerAchievement(TFAchievementPage.twilightHill3);
			}
		}
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	@Override
	protected boolean isValidLightLevel()
	{
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);

		if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > this.rand.nextInt(32))
		{
			return false;
		}
		else
		{
			int l = this.worldObj.getBlockLightValue(i, j, k);

			if (this.worldObj.isThundering(false))
			{
				int i1 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				l = this.worldObj.getBlockLightValue(i, j, k);
				this.worldObj.skylightSubtracted = i1;
			}

			return l <= this.rand.nextInt(8);
		}
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this entity.
	 */
	@Override
	public boolean getCanSpawnHere(boolean perform_light_check)
	{
		return this.worldObj.difficultySetting > 0 && (!perform_light_check || this.isValidLightLevel()) && super.getCanSpawnHere(perform_light_check);
	}
}
