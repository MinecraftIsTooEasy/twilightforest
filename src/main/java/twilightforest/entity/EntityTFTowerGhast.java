package twilightforest.entity;

import net.minecraft.AxisAlignedBB;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityGhast;
import net.minecraft.EntityLargeFireball;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.RaycastCollision;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFFeature;
import twilightforest.compat.TFParticles;

public class EntityTFTowerGhast extends EntityGhast {

	private static final int AGGRO_STATUS = 16;
	protected EntityLivingBase targetedEntity;
	protected boolean isAggressive;
	protected int aggroCooldown;
	protected int explosionPower;
	protected int aggroCounter;

	protected float aggroRange;
	protected float stareRange;

	protected float wanderFactor;

	protected int inTrapCounter;

	private ChunkCoordinates homePosition = new ChunkCoordinates(0, 0, 0);
	/** If -1 there is no maximum distance */
	private float maximumHomeDistance = -1.0F;

	public EntityTFTowerGhast(World par1World) {
		super(par1World);

		this.setSize(4.0F, 6.0F);

		this.aggroRange = 64.0F;
		this.stareRange = 32.0F;

		this.wanderFactor = 16.0F;

		this.inTrapCounter = 0;
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// MITE_BALANCE 9.3: Tower Ghast HP 40
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D);
	}

	/** MITE_BALANCE 9.3: 18 XP. */
	@Override
	public int getExperienceValue() {
		return 18;
	}

	/**
	 * Returns the volume for the sounds this mob makes.
	 */
	@Override
	protected float getSoundVolume(String sound) {
		return 0.5F;
	}

	/**
	 * Get number of ticks, at least during which the living entity will be silent.
	 */
	@Override
	public int getTalkInterval() {
		return 160;
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	public int getAttackStatus() {
		return this.dataWatcher.getWatchableObjectByte(AGGRO_STATUS);
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		// age when in light, like mobs (MITE: the despawn counter replaces 1.7.10 entityAge)
		float var1 = this.getBrightness(1.0F);

		if (var1 > 0.5F) {
			this.despawn_counter += 2;
		}

		if (this.rand.nextBoolean()) {
			TFParticles.spawn(this.worldObj, "reddust", this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0, 0, 0);
		}

		super.onLivingUpdate();
	}

	/**
	 * Altered Ghast AI
	 */
	@Override
	protected void updateEntityActionState() {
		if (!this.worldObj.isRemote && this.worldObj.difficultySetting == 0) {
			this.setDead();
		}

		this.tryDespawnEntity();

		// tower home
		checkForTowerHome();

		// check if in trap
		if (this.inTrapCounter > 0) {
			this.inTrapCounter--;
			this.targetedEntity = null;
			return; // do nothing if in trap;
		}

		this.prevAttackCounter = this.attackCounter;

		// target
		if (this.targetedEntity != null && this.targetedEntity.isDead) {
			this.targetedEntity = null;
		}

		if (this.targetedEntity == null) {
			this.targetedEntity = findPlayerInRange();
		} else if (!this.isAggressive && this.targetedEntity instanceof EntityPlayer) {
			checkToIncreaseAggro((EntityPlayer) this.targetedEntity);
		}

		// check if we are at our waypoint target
		double offsetX = this.waypointX - this.posX;
		double offsetY = this.waypointY - this.posY;
		double offsetZ = this.waypointZ - this.posZ;
		double distanceDesired = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

		if ((distanceDesired < 1.0D || distanceDesired > 3600.0D) && this.wanderFactor > 0) {
			this.waypointX = this.posX + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * wanderFactor);
			this.waypointY = this.posY + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * wanderFactor);
			this.waypointZ = this.posZ + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * wanderFactor);
		}

		// move?
		if (this.targetedEntity == null && this.wanderFactor > 0) {
			if (this.courseChangeCooldown-- <= 0) {
				this.courseChangeCooldown += this.rand.nextInt(20) + 20;
				distanceDesired = (double) MathHelper.sqrt_double(distanceDesired);

				if (!this.isWithinHomeDistance(MathHelper.floor_double(waypointX), MathHelper.floor_double(waypointY), MathHelper.floor_double(waypointZ))) {
					// change waypoint to be more towards home
					ChunkCoordinates cc = TFFeature.getNearestCenterXYZ(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ), worldObj);

					Vec3 homeVector = Vec3.createVectorHelper(cc.posX - this.posX, cc.posY + 128 - this.posY, cc.posZ - this.posZ);
					homeVector = homeVector.normalize();

					this.waypointX = this.posX + homeVector.xCoord * 16.0F + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F);
					this.waypointY = this.posY + homeVector.yCoord * 16.0F + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F);
					this.waypointZ = this.posZ + homeVector.zCoord * 16.0F + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F);
				}

				if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, distanceDesired)) {
					this.motionX += offsetX / distanceDesired * 0.1D;
					this.motionY += offsetY / distanceDesired * 0.1D;
					this.motionZ += offsetZ / distanceDesired * 0.1D;
				} else {
					this.waypointX = this.posX;
					this.waypointY = this.posY;
					this.waypointZ = this.posZ;
				}
			}
		} else {
			this.motionX *= 0.75;
			this.motionY *= 0.75;
			this.motionZ *= 0.75;
		}

		// check if our target is still within range
		double targetRange = (this.aggroCounter > 0 || this.isAggressive) ? aggroRange : stareRange;

		if (this.targetedEntity != null && this.targetedEntity.getDistanceSqToEntity(this) < targetRange * targetRange && this.canSeeEntity(this.targetedEntity)) {
			// turn towards target
			this.faceEntity(this.targetedEntity, 10F, this.getVerticalFaceSpeed());

			// attack if aggressive
			if (this.isAggressive) {
				if (this.attackCounter == 10) {
					this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1007, (int) this.posX, (int) this.posY, (int) this.posZ, 0);
				}

				++this.attackCounter;

				if (this.attackCounter == 20) {
					spitFireball();
					this.attackCounter = -40;
				}
			}
		} else {
			// ignore player, move normally
			this.isAggressive = false;
			this.targetedEntity = null;
			this.renderYawOffset = this.rotationYaw = -((float) Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float) Math.PI;

			// changing the pitch with movement looks goofy and un-ghast-like
			this.rotationPitch = 0;
		}

		if (this.attackCounter > 0 && !this.isAggressive) {
			--this.attackCounter;
		}

		// set aggro status
		byte currentAggroStatus = this.dataWatcher.getWatchableObjectByte(AGGRO_STATUS);
		byte newAggroStatus = (byte) (this.attackCounter > 10 ? 2 : (this.aggroCounter > 0 || this.isAggressive) ? 1 : 0);

		if (currentAggroStatus != newAggroStatus) {
			this.dataWatcher.updateObject(AGGRO_STATUS, Byte.valueOf(newAggroStatus));
		}
	}

	/**
	 * Something is deeply wrong with the calculations based off of this value, so let's set it high enough that it's ignored.
	 */
	@Override
	public int getVerticalFaceSpeed() {
		return 500;
	}

	/** MITE_BALANCE 9.3: explosion size vs living entities of this ghast's fireball (block radius is always 0). */
	protected float getFireballSizeVsLiving() {
		return 0.95F;
	}

	/**
	 * Spit a fireball
	 */
	protected void spitFireball() {
		double offsetX = this.targetedEntity.posX - this.posX;
		double offsetY = this.targetedEntity.boundingBox.minY + (double) (this.targetedEntity.height / 2.0F) - (this.posY + (double) (this.height / 2.0F));
		double offsetZ = this.targetedEntity.posZ - this.posZ;

		// fireball sound effect
		this.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1008, (int) this.posX, (int) this.posY, (int) this.posZ, 0);

		TowerGhastFireball entityFireball = new TowerGhastFireball(this.worldObj, this, offsetX, offsetY, offsetZ);
		entityFireball.sizeVsLiving = this.getFireballSizeVsLiving();
		double shotSpawnDistance = 0.5D;
		Vec3 lookVec = this.getLook(1.0F);
		entityFireball.posX = this.posX + lookVec.xCoord * shotSpawnDistance;
		entityFireball.posY = this.posY + (double) (this.height / 2.0F) + lookVec.yCoord * shotSpawnDistance;
		entityFireball.posZ = this.posZ + lookVec.zCoord * shotSpawnDistance;
		this.worldObj.spawnEntityInWorld(entityFireball);

		// when we attack, there is a 1-in-6 chance we decide to stop attacking
		if (rand.nextInt(6) == 0) {
			this.isAggressive = false;
		}
	}

	/**
	 * Find a player in our aggro range.  If we feel the need to become aggressive towards that
	 * player, or it is within our stare range, return
	 */
	protected EntityLivingBase findPlayerInRange() {
		EntityPlayer closest = this.getClosestVulnerablePlayer(aggroRange);

		if (closest != null) {
			float range = this.getDistanceToEntity(closest);
			if (range < this.stareRange || this.shouldAttackPlayer(closest)) {
				return closest;
			}
		}
		return null;
	}

	/**
	 * Check if we should become aggressive towards the player.
	 */
	protected void checkToIncreaseAggro(EntityPlayer par1EntityPlayer) {
		if (this.shouldAttackPlayer(par1EntityPlayer)) {
			if (this.aggroCounter == 0) {
				this.worldObj.playSoundAtEntity(this, "mob.ghast.moan", 1.0F, 1.0F);
			}

			if (this.aggroCounter++ >= 20) {
				this.aggroCounter = 0;
				this.isAggressive = true;
			}
		} else {
			this.aggroCounter = 0;
		}
	}

	/**
	 * Checks to see if this tower ghast should be attacking this player.
	 * Tower ghasts attack if the block above the player is not Tower wood
	 */
	protected boolean shouldAttackPlayer(EntityPlayer par1EntityPlayer) {
		int dx = MathHelper.floor_double(par1EntityPlayer.posX);
		int dy = MathHelper.floor_double(par1EntityPlayer.posY);
		int dz = MathHelper.floor_double(par1EntityPlayer.posZ);

		return worldObj.canBlockSeeTheSky(dx, dy, dz) && par1EntityPlayer.canSeeEntity(this);
	}

	/**
	 * True if the ghast has an unobstructed line of travel to the waypoint.
	 */
	protected boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
		double var9 = (this.waypointX - this.posX) / par7;
		double var11 = (this.waypointY - this.posY) / par7;
		double var13 = (this.waypointZ - this.posZ) / par7;
		AxisAlignedBB var15 = this.boundingBox.copy();

		for (int var16 = 1; (double) var16 < par7; ++var16) {
			var15.offset(var9, var11, var13);

			if (!this.worldObj.getCollidingBoundingBoxes(this, var15).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		EntityDamageResult result = super.attackEntityFrom(damage);

		if (result != null && result.entityWasNegativelyAffected()) {
			DamageSource source = damage.getSource();
			Entity attacker = source == null ? null : source.getResponsibleEntity();
			if (!(attacker instanceof EntityLivingBase) && source != null) {
				attacker = source.getImmediateEntity();
			}

			if (attacker instanceof EntityLivingBase) {
				this.targetedEntity = (EntityLivingBase) attacker;
				this.isAggressive = true;
			}
		}

		return result;
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this entity.
	 */
	@Override
	public boolean getCanSpawnHere(boolean perform_light_check) {
		return this.worldObj.checkNoEntityCollision(this.boundingBox)
				&& this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
				&& !this.worldObj.isAnyLiquid(this.boundingBox)
				&& this.worldObj.difficultySetting > 0
				&& (!perform_light_check || this.isValidLightLevel());
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	@Override
	protected boolean isValidLightLevel() {
		return true;
	}

	/** MITE_BALANCE 9.3: gunpowder 0-2, ghast tear 20%. */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		int num = this.rand.nextInt(3);
		for (int i = 0; i < num; i++) {
			this.dropItem(Item.gunpowder.itemID, 1);
		}

		if (this.rand.nextInt(5) == 0) {
			this.dropItem(Item.ghastTear.itemID, 1);
		}
	}

	/**
	 * Check for our tower home and if we find it, set it
	 */
	protected void checkForTowerHome() {
		if (!this.hasHome()) {
			// check if we're near a dark forest tower and if so, set that as home
			int chunkX = MathHelper.floor_double(this.posX) >> 4;
			int chunkZ = MathHelper.floor_double(this.posZ) >> 4;

			TFFeature nearFeature = TFFeature.getFeatureForRegion(chunkX, chunkZ, this.worldObj);

			if (nearFeature != TFFeature.darkTower) {
				this.detachHome();

				// lost: let the despawn counter run
				this.despawn_counter += 5;
			} else {
				// set our home position to the center of the tower
				ChunkCoordinates cc = TFFeature.getNearestCenterXYZ(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ), worldObj);
				this.setHomeArea(cc.posX, cc.posY + 128, cc.posZ, 64);
			}
		}
	}

	/**
	 * Towers are so large, a simple radius doesn't really work, so we make it more of a cylinder
	 */
	public boolean isWithinHomeDistance(int x, int y, int z) {
		if (this.getMaximumHomeDistance() == -1.0F) {
			return true;
		} else {
			ChunkCoordinates home = this.getHomePosition();

			return y > 64 && y < 210 && home.getDistanceSquared(x, home.posY, z) < this.getMaximumHomeDistance() * this.getMaximumHomeDistance();
		}
	}

	public void setInTrap() {
		this.inTrapCounter = 10;
	}

	public void setHomeArea(int par1, int par2, int par3, int par4) {
		this.homePosition.set(par1, par2, par3);
		this.maximumHomeDistance = (float) par4;
	}

	public ChunkCoordinates getHomePosition() {
		return this.homePosition;
	}

	public float getMaximumHomeDistance() {
		return this.maximumHomeDistance;
	}

	public void detachHome() {
		this.maximumHomeDistance = -1.0F;
	}

	public boolean hasHome() {
		return this.maximumHomeDistance != -1.0F;
	}

	/**
	 * MITE_BALANCE 8.2 E12 / F20: fireball that never breaks blocks; the explosion only hurts living entities
	 * (size vs blocks 0, size vs living set by the ghast).
	 */
	public static class TowerGhastFireball extends EntityLargeFireball {

		public float sizeVsLiving = 0.95F;

		public TowerGhastFireball(World world) {
			super(world);
		}

		public TowerGhastFireball(World world, EntityLivingBase shooter, double ax, double ay, double az) {
			super(world, shooter, ax, ay, az);
		}

		@Override
		protected void onImpact(RaycastCollision rc) {
			if (!this.worldObj.isRemote) {
				// a fireball a player knocked back hits like the vanilla one (a ghast takes 1000 from it); the ghast's own shot
				// only explodes
				if (rc.isEntity() && this.shootingEntity instanceof EntityPlayer) {
					rc.getEntityHit().attackEntityFrom(new Damage(DamageSource.causeFireballDamage(this, this.shootingEntity), 6.0F));
				}
				this.worldObj.newExplosion((Entity) null, this.posX, this.posY, this.posZ, twilightforest.TFConfig.explosionBreakBlocks ? 4.0F * this.sizeVsLiving : 0.0F, this.sizeVsLiving, true, false);
				this.setDead();
			}
		}
	}
}
