package twilightforest.entity.boss;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDragonPart;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.IBossDisplayData;
import net.minecraft.IEntityMultiPart;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFProgress;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.entity.IBreathAttacker;
import twilightforest.entity.ai.EntityAITFHoverBeam;
import twilightforest.entity.ai.EntityAITFHoverSummon;
import twilightforest.entity.ai.EntityAITFHoverThenDrop;
import twilightforest.item.TFItems;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

public class EntityTFSnowQueen extends EntityMob implements IBossDisplayData, IEntityMultiPart, IBreathAttacker {

	private static final int MAX_SUMMONS = 6;
	/** MITE_BALANCE 9.4: the beam phase is forcibly ended after 40 accumulated damage (original 25). */
	private static final float MAX_DAMAGE_WHILE_BEAMING = 40.0F;
	/** MITE_BALANCE 9.4: ice breath = 4 magic damage per 10 ticks. */
	private static final float BREATH_DAMAGE = 4.0F;
	private static final int BREATH_HIT_INTERVAL = 10;

	public enum Phase { SUMMON, DROP, BEAM };

	/** DataWatcher slots, allocated dynamically (MITE subclasses occupy fixed low ids; TF's 21/22 would collide). */
	private int beamFlag;
	private int phaseFlag;

	public Entity[] iceArray;

	private int summonsRemaining = 0;
	private int successfulDrops;
	private int maxDrops;
	private float damageWhileBeaming;

	public EntityTFSnowQueen(World par1World) {
		super(par1World);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAITFHoverSummon(this, EntityPlayer.class, 1.0D));
		this.tasks.addTask(2, new EntityAITFHoverThenDrop(this, EntityPlayer.class, 80, 20));
		this.tasks.addTask(3, new EntityAITFHoverBeam(this, EntityPlayer.class, 80, 100));
		this.tasks.addTask(6, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, true));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.setSize(0.7F, 2.2F);

		// the shields must be created right after the queen so that their entity ids stay contiguous (MITE_ENTITY 14)
		this.iceArray = new Entity[7];
		for (int i = 0; i < this.iceArray.length; i++) {
			this.iceArray[i] = new EntityTFSnowQueenIceShield(this);
		}

		this.setCurrentPhase(Phase.SUMMON);
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.4: 280 HP, melee 14)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(14.0D);
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(280.0D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.beamFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.beamFlag, Byte.valueOf((byte) 0));
		this.phaseFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.phaseFlag, Byte.valueOf((byte) 0));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/** MITE_BALANCE 9.2 E3/9.4: natural defense 3, none against magic / mundane-armor-bypassing damage. */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return damage_source.bypassesMundaneArmor() ? 0.0F : 3.0F;
	}

	/** MITE_BALANCE 9.4: 400 xp (x0.25 on repeat kills) are handed out to the credited players in onDeath, so no orbs. */
	@Override
	public int getExperienceValue() {
		return 0;
	}

	/** Set once in onDeath (server side), read by dropFewItems. */
	private TFProgress.KillResult killResult;

	/** Bosses never despawn. */
	@Override
	protected boolean canDespawn() {
		return false;
	}

	/** Bosses never flee. */
	@Override
	public boolean fleesWhenBadlyWounded() {
		return false;
	}

	/** Bosses are not affected by the blood moon frenzy (MITE_BALANCE 9.2 E11). */
	@Override
	public boolean isFrenzied() {
		return twilightforest.TFConfig.mirrorBloodMoonBosses && super.isFrenzied();
	}

	// 1.7.10 isImmuneToFire = true
	@Override
	public boolean isHarmedByFire() {
		return false;
	}

	@Override
	public boolean isHarmedByLava() {
		return false;
	}

	@Override
	public boolean canCatchFire() {
		return false;
	}

	@Override
	protected String getLivingSound() {
		return TwilightForestMod.ID + ":mob.ice.noise";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return TwilightForestMod.ID + ":mob.ice.hurt";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return TwilightForestMod.ID + ":mob.ice.death";
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId() {
		return Item.snowball.itemID;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// make snow particles
		for (int i = 0; i < 3; i++) {
			float px = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
			float py = this.getEyeHeight() + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;
			float pz = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowguardian", this.lastTickPosX + px, this.lastTickPosY + py, this.lastTickPosZ + pz, 0, 0, 0);
		}

		// during drop phase, all the ice blocks should make particles
		if (this.getCurrentPhase() == Phase.DROP) {
			for (int i = 0; i < this.iceArray.length; i++) {
				float px = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;
				float py = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;
				float pz = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;

				TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowwarning", this.iceArray[i].lastTickPosX + px, this.iceArray[i].lastTickPosY + py, this.iceArray[i].lastTickPosZ + pz, 0, 0, 0);
			}
		}

		// when ice beaming, spew particles
		if (isBreathing() && this.isEntityAlive()) {
			Vec3 look = this.getLookVec();

			double dist = 0.5;
			double px = this.posX + look.xCoord * dist;
			double py = this.posY + 1.7F + look.yCoord * dist;
			double pz = this.posZ + look.zCoord * dist;

			for (int i = 0; i < 10; i++) {
				double dx = look.xCoord;
				double dy = 0;//look.yCoord;
				double dz = look.zCoord;

				double spread = 2 + this.getRNG().nextDouble() * 2.5;
				double velocity = 2.0 + this.getRNG().nextDouble() * 0.15;

				// beeeam
				dx += this.getRNG().nextGaussian() * 0.0075D * spread;
				dy += this.getRNG().nextGaussian() * 0.0075D * spread;
				dz += this.getRNG().nextGaussian() * 0.0075D * spread;
				dx *= velocity;
				dy *= velocity;
				dz *= velocity;

				TwilightForestMod.proxy.spawnParticle(this.worldObj, "icebeam", px, py, pz, dx, dy, dz);
			}
		}
	}

	/**
	 * Keep ice shield position updated
	 */
	@Override
	public void onUpdate() {

		super.onUpdate();

		for (int i = 0; i < this.iceArray.length; i++) {

			this.iceArray[i].onUpdate();

			if (i < this.iceArray.length - 1) {
				// set block position
				Vec3 blockPos = this.getIceShieldPosition(i);

				this.iceArray[i].setPosition(blockPos.xCoord, blockPos.yCoord, blockPos.zCoord);
				this.iceArray[i].rotationYaw = this.getIceShieldAngle(i);
			} else {
				// last block beneath
				this.iceArray[i].setPosition(this.posX, this.posY - 1, this.posZ);
				this.iceArray[i].rotationYaw = this.getIceShieldAngle(i);
			}

			// collide things with the block
			if (!worldObj.isRemote) {
				this.applyShieldCollisions(this.iceArray[i]);
			}
		}

		// death animation
		if (deathTime > 0) {
			for (int k = 0; k < 5; k++) {
				double d = rand.nextGaussian() * 0.02D;
				double d1 = rand.nextGaussian() * 0.02D;
				double d2 = rand.nextGaussian() * 0.02D;
				String explosionType = rand.nextBoolean() ? "hugeexplosion" : "explode";

				TFParticles.spawn(worldObj, explosionType, (posX + rand.nextFloat() * width * 2.0F) - width, posY + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
			}
		}
	}

	/**
	 * MITE_BALANCE 9.5: packed ice 1-4 stacks of 7, snowballs 5-9 stacks of 16, a Triple or Seeker bow and the trophy.
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		int looting = damage_source != null ? damage_source.getLootingModifier() : 0;

		// MITE_BALANCE H8: the bow and the trophy only on the first kill, the ice and snowballs halved on repeat kills
		boolean unique = killResult == null || killResult.dropsUniques();
		if (unique) {
			dropBow();
		}

		// ice cubes (packed ice does not exist in MITE)
		// TODO(mite-port): swap Block.ice for the TF packed ice block if one is added
		int totalDrops = this.rand.nextInt(4 + looting) + 1;
		if (killResult != null) {
			totalDrops = killResult.material(totalDrops);
		}
		for (int i = 0; i < totalDrops; ++i) {
			this.dropItem(Block.ice.blockID, 7);
		}

		// snowballs
		totalDrops = this.rand.nextInt(5 + looting) + 5;
		if (killResult != null) {
			totalDrops = killResult.material(totalDrops);
		}
		for (int i = 0; i < totalDrops; ++i) {
			this.dropItem(Item.snowball.itemID, 16);
		}

		// trophy, first kill only
		if (unique) {
			this.dropItemStack(new ItemStack(TFItems.trophy, 1, 4), 0.0F);
		}
	}

	private void dropBow() {
		int bowType = rand.nextInt(2);
		if (bowType == 0) {
			this.dropItemStack(new ItemStack(TFItems.tripleBow), 0.0F);
		} else {
			this.dropItemStack(new ItemStack(TFItems.seekerBow), 0.0F);
		}
	}

	/** Drops of this death, collected so they end up in a chest (TFBossLoot); null outside onDeath. */
	private java.util.List<net.minecraft.ItemStack> lootChest;

	@Override
	public net.minecraft.EntityItem dropItemStack(net.minecraft.ItemStack stack, float yOffset) {
		return twilightforest.TFBossLoot.capture(lootChest, stack) ? null : super.dropItemStack(stack, yOffset);
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		// progress credit first: the drops are decided inside super.onDeath
		if (!worldObj.isRemote && killResult == null) {
			killResult = TFProgress.onBossKilled(this, TFProgress.Flag.QUEEN, TFAchievementPage.twilightHunter);
		}
		lootChest = twilightforest.TFBossLoot.begin(this);
		super.onDeath(par1DamageSource);
		twilightforest.TFBossLoot.finish(this, lootChest);
		lootChest = null;
		if (!worldObj.isRemote && killResult != null) {
			killResult.awardXp(400);
		}

		// mark the tower as defeated
		if (!worldObj.isRemote) {
			int dx = MathHelper.floor_double(this.posX);
			int dy = MathHelper.floor_double(this.posY);
			int dz = MathHelper.floor_double(this.posZ);

			if (worldObj.provider instanceof WorldProviderTwilightForest) {
				ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) worldObj.provider).getChunkProvider();
				TFFeature nearbyFeature = ((TFWorldChunkManager) worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);

				// 1.7.10 only checked lichTower here (copy-paste slip: the queen lives in the ice tower), so the ice tower
				// was never marked as conquered; accept both.
				if (nearbyFeature == TFFeature.lichTower || nearbyFeature == TFFeature.iceTower) {
					chunkProvider.setStructureConquered(dx, dy, dz, true);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void applyShieldCollisions(Entity collider) {
		List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(collider, collider.boundingBox.expand(-0.2F, -0.2F, -0.2F));

		for (Entity collided : list) {

			if (collided.canBePushed()) {
				applyShieldCollision(collider, collided);
			}
		}
	}

	/**
	 * Do the effect where the shield hits something
	 */
	protected void applyShieldCollision(Entity collider, Entity collided) {
		if (collided != this) {
			collided.applyEntityCollision(collider);
			if (collided instanceof EntityLivingBase) {
				// do hit and throw
				EntityDamageResult result = super.attackEntityAsMob(collided);

				if (result != null && result.entityWasNegativelyAffected()) {
					collided.motionY += 0.4000000059604645D;
					this.playSound("mob.irongolem.throw", 1.0F, 1.0F);
				}
			}
		}
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();

		// switch phases
		if (this.getCurrentPhase() == Phase.SUMMON && this.getSummonsRemaining() == 0 && this.countMyMinions() <= 0) {
			this.setCurrentPhase(Phase.DROP);
		}
		if (this.getCurrentPhase() == Phase.DROP && this.successfulDrops >= this.maxDrops) {
			this.setCurrentPhase(Phase.BEAM);
		}
		if (this.getCurrentPhase() == Phase.BEAM && this.damageWhileBeaming >= MAX_DAMAGE_WHILE_BEAMING) {
			this.setCurrentPhase(Phase.SUMMON);
		}
	}

	/**
	 * Called when we get attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		EntityDamageResult result = super.attackEntityFrom(damage);

		if (result != null && result.entityWasNegativelyAffected() && this.getCurrentPhase() == Phase.BEAM) {
			this.damageWhileBeaming += result.getAmountOfHealthLost();
		}

		return result;
	}

	private Vec3 getIceShieldPosition(int i) {
		return this.getIceShieldPosition(getIceShieldAngle(i), 1F);
	}

	private float getIceShieldAngle(int i) {
		return 60F * i + (this.ticksExisted * 5F);
	}

	/**
	 * Get the ice shield position
	 */
	public Vec3 getIceShieldPosition(float angle, float distance) {
		double var1 = Math.cos((angle) * Math.PI / 180.0D) * distance;
		double var3 = Math.sin((angle) * Math.PI / 180.0D) * distance;

		return this.worldObj.getVec3(this.posX + var1, this.posY + this.getShieldYOffset(), this.posZ + var3);
	}

	/**
	 * How high is the shield
	 */
	public double getShieldYOffset() {
		return 0.1F;
	}

	/**
	 * Called when the mob is falling. Calculates and applies fall damage.
	 */
	@Override
	protected void fall(float par1) {
		; // no falling
	}

	@Override
	public World func_82194_d() {
		return this.worldObj;
	}

	@Override
	public EntityDamageResult attackEntityFromPart(EntityDragonPart entitydragonpart, Damage damage) {
		return null;
	}

	/**
	 * We need to do this for the bounding boxes on the parts to become active
	 */
	@Override
	public Entity[] getParts() {
		return iceArray;
	}

	/**
	 * Destroys all ice related blocks in the AABB
	 */
	public boolean destroyBlocksInAABB(AxisAlignedBB par1AxisAlignedBB) {
		int minX = MathHelper.floor_double(par1AxisAlignedBB.minX);
		int minY = MathHelper.floor_double(par1AxisAlignedBB.minY);
		int minZ = MathHelper.floor_double(par1AxisAlignedBB.minZ);
		int maxX = MathHelper.floor_double(par1AxisAlignedBB.maxX);
		int maxY = MathHelper.floor_double(par1AxisAlignedBB.maxY);
		int maxZ = MathHelper.floor_double(par1AxisAlignedBB.maxZ);
		boolean wasBlocked = false;
		for (int dx = minX; dx <= maxX; ++dx) {
			for (int dy = minY; dy <= maxY; ++dy) {
				for (int dz = minZ; dz <= maxZ; ++dz) {
					int blockId = this.worldObj.getBlockId(dx, dy, dz);

					if (blockId != 0) {
						int currentMeta = this.worldObj.getBlockMetadata(dx, dy, dz);

						// TODO(mite-port): also break TF packed ice if such a block is added
						if (blockId == Block.ice.blockID) {
							this.worldObj.setBlock(dx, dy, dz, 0, 0, 2);

							// here, this effect will have to do
							worldObj.playAuxSFX(2001, dx, dy, dz, blockId + (currentMeta << 12));
						} else {
							wasBlocked = true;
						}
					}
				}
			}
		}

		return wasBlocked;
	}

	@Override
	public boolean isBreathing() {
		return this.getDataWatcher().getWatchableObjectByte(this.beamFlag) == 1;
	}

	@Override
	public void setBreathing(boolean flag) {
		this.getDataWatcher().updateObject(this.beamFlag, Byte.valueOf((byte) (flag ? 1 : 0)));
	}

	public Phase getCurrentPhase() {
		return Phase.values()[this.getDataWatcher().getWatchableObjectByte(this.phaseFlag)];
	}

	public void setCurrentPhase(Phase currentPhase) {
		this.getDataWatcher().updateObject(this.phaseFlag, Byte.valueOf((byte) currentPhase.ordinal()));

		// set variables for current phase
		if (currentPhase == Phase.SUMMON) {
			this.setSummonsRemaining(MAX_SUMMONS);
		}
		if (currentPhase == Phase.DROP) {
			this.successfulDrops = 0;
			this.maxDrops = 2 + this.rand.nextInt(3);
		}
		if (currentPhase == Phase.BEAM) {
			this.damageWhileBeaming = 0;
		}
	}

	public int getSummonsRemaining() {
		return summonsRemaining;
	}

	public void setSummonsRemaining(int summonsRemaining) {
		this.summonsRemaining = summonsRemaining;
	}

	public void summonMinionAt(EntityLivingBase targetedEntity) {

		// find a good spot
		Vec3 minionSpot = findVecInLOSOf(targetedEntity);
		if (minionSpot == null) {
			return; // nowhere to put it: keep the summon for the next attempt
		}

		// put a minion there
		EntityTFIceCrystal minion = new EntityTFIceCrystal(worldObj);
		minion.setPosition(minionSpot.xCoord, minionSpot.yCoord, minionSpot.zCoord);
		worldObj.spawnEntityInWorld(minion);

		minion.setAttackTarget(targetedEntity);
		minion.setToDieIn30Seconds(); // don't stick around

		// reduce summons
		this.summonsRemaining--;
	}

	/**
	 * Returns coords that would be good to teleport to.
	 *
	 * Returns null if we can't find anything
	 */
	protected Vec3 findVecInLOSOf(Entity targetEntity) {
		// for some reason we occasionally get null here
		if (targetEntity == null) {
			return null;
		}

		double tx = 0, ty = 0, tz = 0;
		boolean found = false;
		int tries = 100;
		for (int i = 0; i < tries; i++) {
			tx = targetEntity.posX + rand.nextGaussian() * 16D;
			ty = targetEntity.posY + rand.nextGaussian() * 8D;
			tz = targetEntity.posZ + rand.nextGaussian() * 16D;

			// put the y on something solid
			boolean groundFlag = false;
			// we need to get the integer coordinates for this calculation
			int bx = MathHelper.floor_double(tx);
			int by = MathHelper.floor_double(ty);
			int bz = MathHelper.floor_double(tz);
			while (!groundFlag && ty > 0) {
				Block whatsThere = worldObj.getBlock(bx, by - 1, bz);
				if (whatsThere == null || !whatsThere.blockMaterial.isSolid()) {
					ty--;
					by--;
				} else {
					groundFlag = true;
				}
			}

			// did we not find anything at all to stand on?
			if (by == 0) {
				continue;
			}

			if (!canEntitySee(targetEntity, tx, ty, tz)) {
				continue;
			}

			// check that we're not colliding and not in liquid
			float halfWidth = this.width / 2.0F;
			AxisAlignedBB destBox = AxisAlignedBB.getBoundingBox(tx - halfWidth, ty - yOffset + ySize, tz - halfWidth, tx + halfWidth, ty - yOffset + ySize + height, tz + halfWidth);
			if (worldObj.getCollidingBoundingBoxes(this, destBox).size() > 0) {
				continue;
			}

			if (worldObj.isAnyLiquid(destBox)) {
				continue;
			}

			// if we made it this far, we win!
			found = true;
			break;
		}

		if (!found) {
			return null;
		}

		return worldObj.getVec3(tx, ty, tz);
	}

	/**
	 * Can the specified entity see the specified location?
	 */
	protected boolean canEntitySee(Entity entity, double dx, double dy, double dz) {
		return worldObj.checkForLineOfSight(worldObj.getVec3(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ), worldObj.getVec3(dx, dy, dz), false);
	}

	@SuppressWarnings("unchecked")
	public int countMyMinions() {
		// check if there are enough minions.  we check a 32x16x32 area
		List<EntityTFIceCrystal> nearbyMinons = worldObj.getEntitiesWithinAABB(EntityTFIceCrystal.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));

		return nearbyMinons.size();
	}

	public void incrementSuccessfulDrops() {
		this.successfulDrops++;
	}

	/**
	 * MITE_BALANCE 9.4: 4 magic damage per 10 ticks (the beam AI calls this every tick).
	 */
	@Override
	public void doBreathAttack(Entity target) {
		if (this.worldObj.isRemote || this.ticksExisted % BREATH_HIT_INTERVAL != 0) {
			return;
		}
		if (TFDamage.hurtByMagic(target, this, this, BREATH_DAMAGE)) {
			// slow target?
		}
	}

}
