package twilightforest.entity.boss;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDragonPart;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.IBossDisplayData;
import net.minecraft.IEntityMultiPart;
import net.minecraft.IMob;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTBase;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagDouble;
import net.minecraft.NBTTagList;
import net.minecraft.PathEntity;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFProgress;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;
import twilightforest.item.TFItems;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;


public class EntityTFNaga extends EntityMob
implements IMob, IBossDisplayData, IEntityMultiPart {

	/** Balance (MITE_BALANCE 9.4): fixed HP, the 1.7.10 per-difficulty values are gone */
	private static final float MAX_HEALTH = 180.0F;
	/** Balance: bite 11, charge hit 13 */
	private static final double BITE_DAMAGE = 11.0D;
	private static final double CHARGE_DAMAGE = 13.0D;
	/** Balance: a charge may start at most every 6 seconds */
	private static final int CHARGE_COOLDOWN = 120;
	/** Balance: players this close to the naga (or inside its home area) get the kill credit */
	private static final double CREDIT_RADIUS = 48.0D;

	private static int TICKS_BEFORE_HEALING = 600;

	private static int MAX_SEGMENTS = 12;

	int currentSegments = 0; // not including head

	float segmentHealth;

	int LEASH_X = 46;
	int LEASH_Y = 7;
	int LEASH_Z = 46;

	EntityTFNagaSegment[] body;

	protected PathEntity pathToEntity;
	protected Entity targetEntity;

	int circleCount;
	int intimidateTimer;
	int crumblePlayerTimer;
	int chargeCount;
	int chargeCooldown;

	boolean clockwise;

	public int ticksSinceDamaged = 0;

	/** Names of the players that hurt the naga (server only, kill credit) */
	/** Set once in onDeath (server side), read by dropFewItems. */
	private TFProgress.KillResult killResult;
	private final Set<String> damagers = new HashSet<String>();

	public EntityTFNaga(World world) {
		super(world);

		//this.texture = TwilightForestMod.MODEL_DIR + "nagahead.png";
		this.setSize(1.75f, 3.0f);
		//this.moveSpeed = 0.6f;
		this.stepHeight = 2;

		this.setHealth(getMaxHealth());
		this.segmentHealth = getMaxHealth() / 10;

		this.ignoreFrustumCheck = true;

		circleCount = 15;

		// make segments (they must be created right after the head so the client entity ids line up)
		// the array exists before setSegmentsPerHealth so that call does not spawn a second set of segments
		this.body = new EntityTFNagaSegment[MAX_SEGMENTS];
		if (world != null) {
			for (int i = 0; i < body.length; i++) {
				this.body[i] = new EntityTFNagaSegment(this, i);
				world.spawnEntityInWorld(body[i]);
			}
		}
		setSegmentsPerHealth();

		this.goNormal();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	public float getMaxHealthPerDifficulty() {
		return MAX_HEALTH;
	}

	/**
	 * Determines if an entity can be despawned, used on idle far away entities
	 */
	@Override
	protected boolean canDespawn() {
		return false;
	}

	/**
	 * Bosses never flee
	 */
	@Override
	public boolean fleesWhenBadlyWounded() {
		return false;
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(MAX_HEALTH); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(2.0D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(BITE_DAMAGE); // attack damage
	}

	/**
	 * Natural defense (balance: 2)
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return 2.0F;
	}

	/** Balance: the 120 xp (x0.25 on repeat kills) are handed out to the credited players in onDeath, so no orbs. */
	@Override
	public int getExperienceValue() {
		return 0;
	}

	/**
	 * Determine how many segments, from 2-12, the naga should have, dependent on its current health
	 */
	protected int setSegmentsPerHealth() {
		int oldSegments = this.currentSegments;
		int newSegments = (int) ((this.getHealth() / segmentHealth) + (getHealth() > 0 ? 2 : 0));

		// certain types of overkill seem to make this happen by setting health to a negative number
		if (newSegments < 0) {
			newSegments = 0;
		}

		// why is this happening?  healing rays?
		if (newSegments > MAX_SEGMENTS) {
			newSegments = MAX_SEGMENTS;
		}

		if (newSegments != oldSegments) {
			// detonate unused segments
			if (newSegments < oldSegments) {
				for (int i = newSegments; i < oldSegments; i++) {
					if (body != null && body[i] != null) {
						body[i].selfDestruct();
					}
				}
			} else {
				// grow new segments?
				this.currentSegments = newSegments;
				this.spawnBodySegments();
			}
		}

		// change current variables
		this.currentSegments = newSegments;
		setMovementFactorPerSegments();

		return currentSegments;
	}

	/**
	 * Sets how fast the naga should be moving, depending on how many segments it has left;
	 */
	protected void setMovementFactorPerSegments() {
		float movementFactor = 0.6F - (currentSegments / 12f * 0.2f);

		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(movementFactor); // movement speed
	}

	/**
	 * Supress walking sounds
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * Do not get slowed by lava.
	 */
	@Override
	public boolean handleLavaMovement() {
		return false;
	}

	/**
	 * Die on peaceful
	 */
	@Override
	public void onUpdate() {
		despawnIfInvalid();

		if (deathTime > 0) {
			for (int k = 0; k < 5; k++) {
				double d = rand.nextGaussian() * 0.02D;
				double d1 = rand.nextGaussian() * 0.02D;
				double d2 = rand.nextGaussian() * 0.02D;
				String explosionType = rand.nextBoolean() ? "hugeexplosion" : "explode";

				TFParticles.spawn(worldObj, explosionType, (posX + rand.nextFloat() * width * 2.0F) - width, posY + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
			}
		}

		// update health
		this.ticksSinceDamaged++;

		if (this.chargeCooldown > 0) {
			this.chargeCooldown--;
		}

		if (!this.worldObj.isRemote && this.ticksSinceDamaged > TICKS_BEFORE_HEALING && this.ticksSinceDamaged % 20 == 0) {
			this.heal(1);
		}

		setSegmentsPerHealth();

		super.onUpdate();

		// move bodies
		moveSegments();

		if (!worldObj.isRemote) {
			for (int i = 0; i < currentSegments; i++) {
				if (body[i] != null && !body[i].isDead && body[i].getChunkAddedTo() == null) {
					worldObj.spawnEntityInWorld(body[i]);
				}
			}
		}
	}


	/**
	 * Semi-copied from EntityCreature, but this adds the capability to just path around and not target the player
	 */
	@Override
	protected void updateAITasks() {
		super.updateAITasks();

		// NAGA SMASH!
		if (isCollidedHorizontally && hasTarget()) {
			breakNearbyBlocks();
		}

		// break targeting if our target goes outside the walls
		if (targetEntity != null && !this.isEntityWithinHomeArea(targetEntity)) {
			this.targetEntity = null;
		}

		// perform target and path maintenance
		if (targetEntity == null) {
			targetEntity = findTarget();
			if (targetEntity != null) {
				acquireNewPath();
			}
		} else if (!targetEntity.isEntityAlive()) {
			targetEntity = null;
		} else {
			float targetDistance = targetEntity.getDistanceToEntity(this);

			if (targetDistance > 80) {
				targetEntity = null;
			} else {
				if (canSeeEntity(targetEntity)) {
					attackEntity(targetEntity, targetDistance);
				}
			}
		}

		if (!hasPath()) {
			acquireNewPath();
		}

		boolean inWater = isInWater();
		boolean inLava = handleLavaMovement();

		Vec3 vec3d = hasPath() ? pathToEntity.getPosition(this) : null;

		// if we are very close to the path point, go to the next point, unless the path is finished
		for (double d = width * 4.0F; vec3d != null && vec3d.squareDistanceTo(posX, vec3d.yCoord, posZ) < d * d;) {
			pathToEntity.incrementPathIndex();
			if (pathToEntity.isFinished()) {
				vec3d = null;
				pathToEntity = null;
			} else {
				vec3d = pathToEntity.getPosition(this);
			}
		}

		isJumping = false;
		if (vec3d != null) {
			double d1 = vec3d.xCoord - posX;
			double d2 = vec3d.zCoord - posZ;

			double dist = MathHelper.sqrt_double(d1 * d1 + d2 * d2);

			int i = MathHelper.floor_double(boundingBox.minY + 0.5D);
			double d3 = vec3d.yCoord - i;
			float f2 = (float) ((Math.atan2(d2, d1) * 180D) / 3.1415927410125732D) - 90F;
			float f3 = f2 - rotationYaw;
			moveForward = getMoveSpeed();
			this.setAIMoveSpeed(0.5f);

			// slither!
			if (dist > 4 && chargeCount == 0) {
				moveStrafing = MathHelper.cos(this.ticksExisted * 0.3F) * getMoveSpeed() * 0.6F;
			}

			for (; f3 < -180F; f3 += 360F) { }
			for (; f3 >= 180F; f3 -= 360F) { }
			if (f3 > 30F) {
				f3 = 30F;
			}
			if (f3 < -30F) {
				f3 = -30F;
			}
			rotationYaw += f3;
			if (d3 > 0.6D) {
				isJumping = true;
			}
		}
		if (intimidateTimer > 0 && hasTarget()) {
			faceEntity(targetEntity, 30F, 30F);
			moveForward = 0.1f;
		}
		if (rand.nextFloat() < 0.8F && (inWater || inLava)) {
			isJumping = true;
		}
	}

	private float getMoveSpeed() {
		return 0.5F;
	}

	/**
	 * Utility method to set our move speed
	 */
	private void setMoveSpeed(float f) {
		this.setAIMoveSpeed(f);
	}

	/**
	 * Is the naga in the middle of a charge?
	 */
	protected boolean isCharging() {
		return chargeCount > 0;
	}

	/**
	 * Breaks blocks near the naga
	 */
	protected void breakNearbyBlocks() {
		int minx = MathHelper.floor_double(boundingBox.minX - 0.5D);
		int miny = MathHelper.floor_double(boundingBox.minY + 1.01D);
		int minz = MathHelper.floor_double(boundingBox.minZ - 0.5D);
		int maxx = MathHelper.floor_double(boundingBox.maxX + 0.5D);
		int maxy = MathHelper.floor_double(boundingBox.maxY + 0.001D);
		int maxz = MathHelper.floor_double(boundingBox.maxZ + 0.5D);
		if (worldObj.checkChunksExist(minx, miny, minz, maxx, maxy, maxz)) {
			for (int dx = minx; dx <= maxx; dx++) {
				for (int dy = miny; dy <= maxy; dy++) {
					for (int dz = minz; dz <= maxz; dz++) {
						if (worldObj.getBlockId(dx, dy, dz) != 0) {
							breakBlock(dx, dy, dz);
						}
					}
				}
			}
		}
	}


	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound() {
		return rand.nextInt(3) != 0 ? TwilightForestMod.ID + ":mob.naga.hiss" : TwilightForestMod.ID + ":mob.naga.rattle";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return TwilightForestMod.ID + ":mob.naga.hurt";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return TwilightForestMod.ID + ":mob.naga.hurt";
	}

	/**
	 * Finds a new path.  Currently this is designed to circle the target entity
	 */
	protected void acquireNewPath() {

		// if we don't have a target, do nuffin
		if (!hasTarget()) {
			wanderRandomly();
			return;
		}

		// if we're supposed to intimidate, don't set a path
		if (intimidateTimer > 0) {

			pathToEntity = null;
			intimidateTimer--;

			if (intimidateTimer == 0) {
				clockwise = !clockwise;

				// check to see if we want to charge or crumble blocks around the player
				if (targetEntity.boundingBox.minY > boundingBox.maxY) {
					// crumble!
					doCrumblePlayer();
				} else {
					doCharge();
				}
			}

			return;
		}

		// crumbling is like intimidating, but we just charge after it
		if (crumblePlayerTimer > 0) {
			pathToEntity = null;
			crumblePlayerTimer--;

			crumbleBelowTarget(2);
			crumbleBelowTarget(3);

			if (crumblePlayerTimer == 0) {
				doCharge();
			}
		}

		// looks like we're charging!
		if (chargeCount > 0) {
			chargeCount--;

			Vec3 tpoint = findCirclePoint(targetEntity, 14, Math.PI);
			pathToEntity = worldObj.getEntityPathToXYZ(this, MathHelper.floor_double(tpoint.xCoord), MathHelper.floor_double(tpoint.yCoord), MathHelper.floor_double(tpoint.zCoord), 40F, true, true, true, true);

			if (chargeCount == 0) {
				doCircle();
			}
		}

		// circle if we're supposed to
		if (circleCount > 0) {

			circleCount--;

			// normal radius is 13
			double radius = circleCount % 2 == 0 ? 12.0 : 14.0;
			double rotation = 1; // in radians

			// hook out slightly before circling
			if (circleCount > 1 && circleCount < 3) {
				radius = 16;
			}

			// head almost straight at the player at the end
			if (circleCount == 1) {
				rotation = 0.1;
			}

			Vec3 tpoint = findCirclePoint(targetEntity, radius, rotation);

			pathToEntity = worldObj.getEntityPathToXYZ(this, (int) tpoint.xCoord, (int) tpoint.yCoord, (int) tpoint.zCoord, 40F, true, true, true, true);

			if (circleCount == 0) {
				doIntimidate();
			}


		}

	}

	/**
	 * Crumbles blocks below our current targetEntity.
	 */
	protected void crumbleBelowTarget(int range) {
		int floor = (int) boundingBox.minY; // the block level the naga is standing on.
		int targetY = (int) targetEntity.boundingBox.minY; // the block level the target is standing on.

		if (targetY > floor) {
			int dx = (int) targetEntity.posX + rand.nextInt(range) - rand.nextInt(range);
			int dz = (int) targetEntity.posZ + rand.nextInt(range) - rand.nextInt(range);
			int dy = targetY - rand.nextInt(range) + rand.nextInt(range > 1 ? range - 1 : range);

			if (dy <= floor) {
				dy = targetY;
			}

			if (worldObj.getBlockId(dx, dy, dz) != 0) {
				breakBlock(dx, dy, dz);

				// sparkle!!
				for (int k = 0; k < 20; k++) {
					double d = rand.nextGaussian() * 0.02D;
					double d1 = rand.nextGaussian() * 0.02D;
					double d2 = rand.nextGaussian() * 0.02D;

					TFParticles.spawn(worldObj, "crit", (posX + rand.nextFloat() * width * 2.0F) - width, posY + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
				}
			}
		}
	}

	/**
	 * Breaks a block, no questions asked (except that unbreakable blocks such as bedrock and boss spawners stay).
	 */
	protected void breakBlock(int dx, int dy, int dz) {
		if (worldObj.isRemote) {
			return;
		}

		Block whatsThere = worldObj.getBlock(dx, dy, dz);

		if (whatsThere != null && whatsThere.getBlockHardness(worldObj.getBlockMetadata(dx, dy, dz)) >= 0.0F) {
			// drops the item, plays the 2001 break effect and clears the block
			worldObj.destroyBlock(new BlockBreakInfo(worldObj, dx, dy, dz).setDestroyedBy(this), true);
		}
	}

	/**
	 * Begin the circle cycle
	 */
	protected void doCircle() {
		circleCount += 10 + rand.nextInt(10);
		goNormal();
	}

	/**
	 * Crumble blocks around the player.  This could use maybe some sort of animation or effect to show it is happening.
	 */
	protected void doCrumblePlayer() {
		crumblePlayerTimer = 20 + rand.nextInt(20);
		goSlow();
	}

	/**
	 * Charge the player.  Although the count is 4, we actually charge only 3 times.
	 * Balance: the charge may only start every 6 seconds, otherwise the naga just keeps circling.
	 */
	protected void doCharge() {
		if (chargeCooldown > 0) {
			doCircle();
			return;
		}

		// charrrrrge!
		chargeCount = 4;
		chargeCooldown = CHARGE_COOLDOWN;
		goFast();
	}

	/**
	 * Intimidate the player.  This mostly involves pausing and staring (this is also the wind-up of the charge).
	 */
	protected void doIntimidate() {
		// start the timer
		intimidateTimer += 15 + rand.nextInt(10);
		goSlow();

	}

	/**
	 * Sets the naga to move slowly, such as when he is intimidating the player
	 */
	protected void goSlow() {
		// move slowly
		moveStrafing = 0;
		setMoveSpeed(0.1f);
		pathToEntity = null;
	}

	/**
	 * Normal speed, like when he is circling
	 */
	protected void goNormal() {
		setMoveSpeed(0.6F);
	}

	/**
	 * Fast, like when he is charging
	 */
	protected void goFast() {
		setMoveSpeed(1.0F);
	}


	/**
	 * Do not get pushed while we're intimidating.
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}

	/**
	 * Finds a point that allows us to circle the player clockwise.
	 */
	protected Vec3 findCirclePoint(Entity toCircle, double radius, double rotation) {
		// compute angle
		double vecx = posX - toCircle.posX;
		double vecz = posZ - toCircle.posZ;
		float rangle = (float) (Math.atan2(vecz, vecx));

		// add a little, so he circles (clockwise)
		rangle += clockwise ? rotation : -rotation;

		// figure out where we're headed from the target angle
		double dx = MathHelper.cos(rangle) * radius;
		double dz = MathHelper.sin(rangle) * radius;

		double dy = Math.min(boundingBox.minY, toCircle.posY);

		// add that to the target entity's position, and we have our destination
		return Vec3.createVectorHelper(toCircle.posX + dx, dy, toCircle.posZ + dz);
	}

	public boolean hasTarget() {
		return targetEntity != null;
	}

	/**
	 * Copied from EntityMob
	 */
	protected Entity findTarget() {
		EntityPlayer entityplayer = worldObj.getClosestVulnerablePlayer(this, 32D, true);
		if (entityplayer != null && canSeeEntity(entityplayer) && isEntityWithinHomeArea(entityplayer)) {
			// check range
			return entityplayer;
		} else {
			return null;
		}
	}

	/**
	 * We take damage from another entity.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		DamageSource damagesource = damage.getSource();
		Entity immediate = damagesource.getImmediateEntity();
		Entity responsible = damagesource.getResponsibleEntity();

		// reject damage from outside of our home radius
		if (immediate != null) {
			if (!this.isEntityWithinHomeArea(immediate)) {
				return null;
			}
		}

		if (responsible != null) {
			if (!this.isEntityWithinHomeArea(responsible)) {
				return null;
			}
		}

		// normal damage processing
		EntityDamageResult result = super.attackEntityFrom(damage);
		if (result != null && result.entityWasNegativelyAffected()) {
			setSegmentsPerHealth();

			if (responsible != null && responsible != this) {
				targetEntity = responsible;
			}
			if (responsible instanceof EntityPlayer) {
				damagers.add(((EntityPlayer) responsible).getCommandSenderName());
			}

			this.ticksSinceDamaged = 0;
		}
		return result;
	}

	/**
	 * We attempt to attack another entity.  Checks y height and attackTime counter
	 */
	@Override
	protected void attackEntity(Entity toAttack, float f) {
		if (attackTime <= 0 && f < 4.0F && toAttack.boundingBox.maxY > (boundingBox.minY - 2.5) && toAttack.boundingBox.minY < (boundingBox.maxY + 2.5)) {
			attackTime = 20;
			attackEntityAsMob(toAttack);

			if (isCharging()) {
				// charging, apply extra pushback
				toAttack.addVelocity(-MathHelper.sin((rotationYaw * 3.141593F) / 180F) * 1.0F, 0.10000000000000001D, MathHelper.cos((rotationYaw * 3.141593F) / 180F) * 1.0F);
			}
		}
	}

	/**
	 * The charge hit is stronger than the bite (balance: 13 instead of 11)
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity target) {
		boolean charging = this.isCharging();
		double base = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();

		if (charging) {
			this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(base + (CHARGE_DAMAGE - BITE_DAMAGE));
		}
		try {
			return super.attackEntityAsMob(target);
		} finally {
			if (charging) {
				this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(base);
			}
		}
	}

	/**
	 * Like it says in the method title, find a random destinaton and set sail.
	 */
	protected void wanderRandomly() {
		goNormal();

		boolean flag = false;
		int tx = -1;
		int ty = -1;
		int tz = -1;
		float worstweight = -99999F;
		for (int l = 0; l < 10; l++) {
			int dx = MathHelper.floor_double((posX + rand.nextInt(21)) - 6D);
			int dy = MathHelper.floor_double((posY + rand.nextInt(7)) - 3D);
			int dz = MathHelper.floor_double((posZ + rand.nextInt(21)) - 6D);

			// if we are thinking about out of bounds, head back home instead
			if (!this.isWithinHomeDistance(dx, dy, dz)) {
				dx = this.getHomePosition().posX + rand.nextInt(21) - rand.nextInt(21);
				dy = this.getHomePosition().posY + rand.nextInt(7) - rand.nextInt(7);
				dz = this.getHomePosition().posZ + rand.nextInt(21) - rand.nextInt(21);
			}

			float weight = getBlockPathWeight(dx, dy, dz);
			if (weight > worstweight) {
				worstweight = weight;
				tx = dx;
				ty = dy;
				tz = dz;
				flag = true;
			}
		}

		if (flag) {
			pathToEntity = worldObj.getEntityPathToXYZ(this, tx, ty, tz, 80F, true, true, true, true);
		}
	}

	/**
	 * Copied from EntityCreature
	 */
	@Override
	public float getBlockPathWeight(int i, int j, int k) {
		// if it's out of bounds, we hate it
		if (!this.isWithinHomeDistance(i, j, k)) {
			return Float.MIN_VALUE;
		} else {
			return 0.0F;
		}
	}

	/**
	 * Copied from EntityCreature
	 */
	@Override
	public boolean hasPath() {
		return pathToEntity != null;
	}

	@Override
	protected int getDropItemId() {
		return TFItems.nagaScale.itemID;
	}

	/**
	 * Balance: naga scale 6-10 and the trophy; repeat kills drop half the scales and no trophy.
	 */
	@Override
	protected void dropFewItems(boolean flag, DamageSource damageSource) {
		Item i = TFItems.nagaScale;
		if (i != null) {
			int j = 6 + rand.nextInt(5);
			if (killResult != null) {
				j = killResult.material(j);
			}
			for (int k = 0; k < j; k++) {
				this.dropItem(i, 1);
			}

		}

		// trophy, first kill only
		if (killResult == null || killResult.dropsUniques()) {
			this.dropItemStack(new ItemStack(TFItems.trophy, 1, 1), 0);
		}
	}

	/**
	 * Check to make sure all parts exist.  If they don't, despawn.
	 */
	protected void despawnIfInvalid() {
		// check to see if we're valid
		if (!worldObj.isRemote && worldObj.difficultySetting == 0) {
			despawnMe();
		}
	}

	/**
	 * Despawn the naga, and restore the boss spawner at our home location, if set
	 */
	protected void despawnMe() {
		if (isLeashed()) {
			ChunkCoordinates home = this.getHomePosition();
			TFWorldUtil.setBlock(worldObj, home.posX, home.posY, home.posZ, TFBlocks.bossSpawner, 0, 2);
		}
		setDead();
	}

	/**
	 * Are there free-roaming nagas out there?  Should there be?
	 */
	public boolean isLeashed() {
		return this.getMaximumHomeDistance() > -1;
	}

	/**
	 * The naga does not keep a circular home (replaces EntityCreature.func_110176_b, used by the vanilla AI helpers too)
	 */
	@Override
	public boolean func_110176_b(int x, int y, int z) {
		return isWithinHomeDistance(x, y, z);
	}

	/**
	 * The naga does not keep a circular home
	 */
	public boolean isWithinHomeDistance(int x, int y, int z) {
		if (this.getMaximumHomeDistance() == -1) {
			return true;
		} else {
			int distX = Math.abs(this.getHomePosition().posX - x);
			int distY = Math.abs(this.getHomePosition().posY - y);
			int distZ = Math.abs(this.getHomePosition().posZ - z);

			return distX <= LEASH_X && distY <= LEASH_Y && distZ <= LEASH_Z;
		}
	}

	/**
	 * Is the entity within our home area?
	 */
	public boolean isEntityWithinHomeArea(Entity entity) {
		return isWithinHomeDistance(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
	}


	/**
	 * Spawns the body of the naga
	 */
	protected void spawnBodySegments() {
		// worldObj is null when MITE's ReferenceFileWriter builds every mob class with a null world at startup
		if (worldObj != null && !worldObj.isRemote) {
			if (body == null) {
				body = new EntityTFNagaSegment[MAX_SEGMENTS];
			}
			for (int i = 0; i < currentSegments; i++) {
				if (body[i] == null || body[i].isDead) {
					body[i] = new EntityTFNagaSegment(this, i);
					body[i].setLocationAndAngles(posX + 0.1 * i, posY + 0.5D, posZ + 0.1 * i, rand.nextFloat() * 360F, 0.0F);
					worldObj.spawnEntityInWorld(body[i]);
				}
			}
		}
	}

	/**
	 * Sets the heading (ha ha) of the body segments
	 */
	protected void moveSegments() {
		for (int i = 0; i < this.currentSegments; i++) {
			double followX, followY, followZ;
			Entity leader;

			if (i == 0) {
				leader = this;
			} else {
				leader = this.body[i - 1];
			}

			followX = leader.posX;
			followY = leader.posY;
			followZ = leader.posZ;


			// also weight the position so that the segments straighten out a little bit, and the front ones straighten more
			float angle = (((leader.rotationYaw + 180) * 3.141593F) / 180F);


			double straightenForce = 0.05D + (1.0 / (float) (i + 1)) * 0.5D;

			double idealX = -MathHelper.sin(angle) * straightenForce;
			double idealZ = MathHelper.cos(angle) * straightenForce;


			Vec3 diff = Vec3.createVectorHelper(body[i].posX - followX, body[i].posY - followY, body[i].posZ - followZ);
			diff = diff.normalize();

			// weight so segments drift towards their ideal position
			diff = diff.addVector(idealX, 0, idealZ);
			diff = diff.normalize();

			double f = 2.0D;


			double destX = followX + f * diff.xCoord;
			double destY = followY + f * diff.yCoord;
			double destZ = followZ + f * diff.zCoord;

			body[i].setPosition(destX, destY, destZ);

			body[i].motionX = f * diff.xCoord;
			body[i].motionY = f * diff.yCoord;
			body[i].motionZ = f * diff.zCoord;

			double distance = (double) MathHelper.sqrt_double(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);

			double diffY = diff.yCoord;
			if (i == 0) {
				diffY -= 0.15D;
			}

			body[i].setRotation((float) (Math.atan2(diff.zCoord, diff.xCoord) * 180.0D / Math.PI) + 90.0F, -(float) (Math.atan2(diffY, distance) * 180.0D / Math.PI));

		}

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		ChunkCoordinates home = this.getHomePosition();
		nbttagcompound.setTag("Home", newDoubleNBTList(new double[] {
				home.posX, home.posY, home.posZ
			}));
		nbttagcompound.setBoolean("HasHome", this.hasHome());
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("Home")) {
			NBTTagList nbttaglist = nbttagcompound.getTagList("Home");
			if (nbttaglist.tagCount() >= 3) {
				int hx = (int) readDouble(nbttaglist.tagAt(0));
				int hy = (int) readDouble(nbttaglist.tagAt(1));
				int hz = (int) readDouble(nbttaglist.tagAt(2));
				this.setHomeArea(hx, hy, hz, 20);
			}
		}
		if (!nbttagcompound.getBoolean("HasHome")) {
			this.detachHome();
		}

		// check health and segments
		setSegmentsPerHealth();
	}

	private static double readDouble(NBTBase tag) {
		return tag instanceof NBTTagDouble ? ((NBTTagDouble) tag).data : 0.0D;
	}

	/** Drops of this death, collected so they end up in a chest (TFBossLoot); null outside onDeath. */
	private java.util.List<net.minecraft.ItemStack> lootChest;

	@Override
	public net.minecraft.EntityItem dropItemStack(net.minecraft.ItemStack stack, float yOffset) {
		return twilightforest.TFBossLoot.capture(lootChest, stack) ? null : super.dropItemStack(stack, yOffset);
	}

	/**
	 * Progress credit (TFProgress.onBossKilled): every survival player within the credit radius gets the naga flag, the
	 * hunter achievements and the experience. The result is computed before the drops, which happen in super.onDeath.
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		if (!worldObj.isRemote && killResult == null) {
			killResult = TFProgress.onBossKilled(this, TFProgress.Flag.NAGA, TFAchievementPage.twilightHunter, TFAchievementPage.twilightKillNaga);
		}
		lootChest = twilightforest.TFBossLoot.begin(this);
		super.onDeath(par1DamageSource);
		twilightforest.TFBossLoot.finish(this, lootChest);
		lootChest = null;

		if (!worldObj.isRemote && killResult != null) {
			killResult.awardXp(120);
		}

		// mark the courtyard as defeated
		if (!worldObj.isRemote && worldObj.provider instanceof WorldProviderTwilightForest) {
			int dx = MathHelper.floor_double(this.posX);
			int dy = MathHelper.floor_double(this.posY);
			int dz = MathHelper.floor_double(this.posZ);

			ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) worldObj.provider).getChunkProvider();
			TFFeature nearbyFeature = ((TFWorldChunkManager) worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);

			if (nearbyFeature == TFFeature.nagaCourtyard) {
				chunkProvider.setStructureConquered(dx, dy, dz, true);
			}
		}

	}

	@Override
	public World func_82194_d() {
		return this.worldObj;
	}

	/**
	 * Direct hits on the parts of the naga body are handled by the segments themselves
	 */
	@Override
	public EntityDamageResult attackEntityFromPart(EntityDragonPart entitydragonpart, Damage damage) {
		return null;
	}


	/**
	 * We need to do this for the bounding boxes on the parts to become active
	 */
	@Override
	public Entity[] getParts() {
		return body;
	}

	public float getMaximumHomeDistance() {
		return this.func_110174_bM();
	}
}
