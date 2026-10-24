package twilightforest.entity.boss;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLightningBolt;
import net.minecraft.EntityPlayer;
import net.minecraft.IBossDisplayData;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFProgress;
import twilightforest.TFFeature;
import twilightforest.TFTreasure;
import twilightforest.TwilightForestMod;
import twilightforest.block.BlockTFTowerDevice;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFParticles;
import twilightforest.entity.EntityTFMiniGhast;
import twilightforest.entity.EntityTFTowerGhast;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

public class EntityTFUrGhast extends EntityTFTowerGhast implements IBossDisplayData {

//	private static final int DATA_BOSSHEALTH = 17;
	private static final int DATA_TANTRUM = 18;

	//private static final int CRUISING_ALTITUDE = 235; // absolute cruising altitude
	private static final int HOVER_ALTITUDE = 20; // how far, relatively, do we hover over ghast traps?

	/** Balance: damage the boss can take before it toggles tantrum mode (TF: 48). */
	private static final float DAMAGE_PER_PHASE = 52.0F;
	/** Balance: raw damage to players standing in the open below a tantruming ghast. */
	private static final float TANTRUM_DAMAGE = 15.0F;
	/** Balance: telegraph (ticks) before the tantrum slam lands. */
	private static final int TANTRUM_WINDUP = 20;

    public double courseX;
    public double courseY;
    public double courseZ;

    ArrayList<ChunkCoordinates> trapLocations;
    ArrayList<ChunkCoordinates> travelCoords;

    int currentTravelCoordIndex;

    int travelPathRepetitions;
    int desiredRepetitions;

    int nextTantrumCry;

    float damageUntilNextPhase; // how much damage can we take before we toggle tantrum mode

    boolean noTrapMode; // are there no traps nearby?  just float around

    /** Ticks left until a telegraphed tantrum slam lands; 0 = no slam pending. */
    int tantrumWindup;

	public EntityTFUrGhast(World par1World)
	{
		super(par1World);

        this.setSize(14.0F, 18.0F);
        this.aggroRange = 128.0F;
    	this.wanderFactor = 32.0F;

    	this.noClip = true;

    	this.trapLocations = new ArrayList<ChunkCoordinates>();
    	this.travelCoords = new ArrayList<ChunkCoordinates>();

    	this.setInTantrum(false);

    	this.damageUntilNextPhase = DAMAGE_PER_PHASE;

        this.noTrapMode = false;
	}

	/**
	 * Set monster attributes
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(260.0D); // max health (balance)
    }

	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(DATA_TANTRUM, Byte.valueOf((byte) 0));
    }

    /**
     * Balance: boss natural defense 3 (not applied against defense-bypassing damage).
     */
	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return super.getNaturalDefense(damage_source) + (damage_source.bypassesMundaneArmor() ? 0.0F : 3.0F);
	}

    /**
     * Balance: the 300 xp (x0.25 on repeat kills) are handed out to the credited players in onDeath, so no orbs.
     */
	@Override
	public int getExperienceValue()
	{
		return 0;
	}

	/** Set once in onDeath (server side), read when the chest is made. */
	private TFProgress.KillResult killResult;

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
	protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Keep health updated
     */
    @Override
	public void onUpdate() {

		super.onUpdate();

        // extra death explosions
		if (deathTime > 0) {
            for(int k = 0; k < 5; k++)
            {
                double d = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                String explosionType = rand.nextBoolean() ?  "hugeexplosion" : "explode";

                TFParticles.spawn(worldObj, explosionType, (posX + rand.nextFloat() * width * 2.0F) - width, posY + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
            }
		}
    }

    /**
     * Called when the entity is attacked.
     */
	@Override
    public EntityDamageResult attackEntityFrom(Damage damage)
    {
    	// ignore suffocation
    	if (damage.getSource() == DamageSource.inWall)
    	{
    		return null;
    	}

    	// in tantrum mode take only half damage (balance; TF: 1/4)
    	if (this.isInTantrum())
    	{
    		damage.scaleAmount(0.5F);
    	}

    	EntityDamageResult result;

        if (damage.isFireballFromPlayer())
        {
        	// 'hide' fireball attacks so that we don't take 1000 damage.
        	DamageSource source = damage.getSource();
        	result = super.attackEntityFrom(new Damage(DamageSource.causeThrownDamage(source.getImmediateEntity(), source.getResponsibleEntity()), damage.getAmount()));
        }
        else
        {
        	result = super.attackEntityFrom(damage);
        }

        if (!worldObj.isRemote && result != null && result.entityLostHealth())
        {
        	this.damageUntilNextPhase -= result.getAmountOfHealthLost();

        	TwilightForestMod.LOGGER.debug("[Urghast] Attack successful, {} damage until phase switch.", this.damageUntilNextPhase);

        	if (this.damageUntilNextPhase <= 0 && this.getHealth() > 0)
        	{
        		this.switchPhase();
        	}
        }

        return result;
    }

	/**
	 * Move to the next phase of our behavior
	 */
	private void switchPhase() {
		if (this.isInTantrum()) {
			this.stopTantrum();
		} else {
			this.startTantrum();
		}

		this.damageUntilNextPhase = DAMAGE_PER_PHASE;
	}

	/**
     * Start throwing a tantrum
     */
	protected void startTantrum() {
		this.setInTantrum(true);

		// TODO(mite-port): TF forced rain and thunder here; MITE weather is a deterministic per-day event list and the balance spec removes the forced thunderstorm.

		spawnGhastsAtTraps();
	}

	/**
	 * Spawn ghasts at two of the traps
	 */
	protected void spawnGhastsAtTraps() {
		// spawn ghasts around two of the traps
		ArrayList<ChunkCoordinates> ghastSpawns = new ArrayList<ChunkCoordinates>(this.trapLocations);

		int numSpawns = Math.min(2, ghastSpawns.size());

		for (int i = 0; i < numSpawns; i++)
		{
			int index = this.rand.nextInt(ghastSpawns.size());

			ChunkCoordinates spawnCoord = ghastSpawns.get(index);
			ghastSpawns.remove(index);

			spawnMinionGhastsAt(spawnCoord.posX, spawnCoord.posY, spawnCoord.posZ);
		}
	}

	public void stopTantrum()
	{
		this.setInTantrum(false);
		this.tantrumWindup = 0;
	}

	/**
	 * Spawn up to 6 minon ghasts around the indicated area
	 */
    private void spawnMinionGhastsAt(int x, int y, int z) {
    	int tries = 24;
    	int spawns = 0;
    	int maxSpawns = 6;

    	int rangeXZ = 4;
    	int rangeY = 8;

		// lightning strike
		this.worldObj.addWeatherEffect(new EntityLightningBolt(worldObj, x, y + 4, z));

    	for (int i = 0; i < tries; i++)
    	{
    		EntityTFMiniGhast minion = new EntityTFMiniGhast(worldObj);

    		double sx = x + ((rand.nextDouble() - rand.nextDouble()) * rangeXZ);
    		double sy = y + (rand.nextDouble() * rangeY);
    		double sz = z + ((rand.nextDouble() - rand.nextDouble()) * rangeXZ);

    		minion.setLocationAndAngles(sx, sy, sz, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
    		minion.makeBossMinion();

    		// MITE's EntityGhast.getCanSpawnHere(boolean) has a 1-in-20 roll built in, so only check for free space
    		if (this.worldObj.getCollidingBoundingBoxes(minion, minion.boundingBox).isEmpty())
    		{
    			this.worldObj.spawnEntityInWorld(minion);
    			minion.spawnExplosionParticle();

    			// limit the total number of successful spawns
    			if (++spawns >= maxSpawns)
    			{
    				break;
    			}
    		}
    	}
	}

	/**
     * Altered Ghast AI
     */
	@Override
    @SuppressWarnings("unchecked")
	protected void updateEntityActionState()
    {
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting == 0)
        {
            this.setDead();
        }

        this.tryDespawnEntity();

        // despawn mini ghasts that are in our AABB
		List<EntityTFMiniGhast> nearbyGhasts = worldObj.getEntitiesWithinAABB(EntityTFMiniGhast.class, this.boundingBox.expand(1, 1, 1));
		for (EntityTFMiniGhast ghast : nearbyGhasts)
		{
			ghast.setDead();
			this.heal(2);
		}

        // trap locations?
        if (this.trapLocations.isEmpty() && !this.noTrapMode)
        {
        	this.scanForTrapsTwice();
        }

        // did we find any traps?
        if (this.trapLocations.isEmpty() && !this.noTrapMode)
        {
        	TwilightForestMod.LOGGER.info("[TwilightForest] Ur-ghast cannot find traps nearby, entering trap-less mode");
        	this.noTrapMode = true;
        }

        // check if in trap
        if (this.inTrapCounter > 0)
        {
        	this.inTrapCounter--;
        	this.targetedEntity = null;
        	return; // do nothing if in trap;
        }

        this.prevAttackCounter = this.attackCounter;

        // target
        if (this.targetedEntity != null && this.targetedEntity.isDead)
        {
            this.targetedEntity = null;
        }

        if (this.targetedEntity == null)
        {
            this.targetedEntity = findPlayerInRange();
        }
        else if (!this.isAggressive && this.targetedEntity instanceof EntityPlayer)
        {
        	checkToIncreaseAggro((EntityPlayer) this.targetedEntity);
        }

        // tantrum?
        if (this.isInTantrum())
        {
        	shedTear();

        	this.targetedEntity = null;

        	// cry?
        	if (--this.nextTantrumCry <= 0)
        	{
        		this.playSound(getHurtSound(), this.getSoundVolume(getHurtSound()), this.getSoundPitch(getHurtSound()));
        		this.nextTantrumCry = 20 + rand.nextInt(30);
        	}

        	if (this.ticksExisted % 10 == 0)
        	{
        		doTantrumDamageEffects();
        	}

        	if (this.tantrumWindup > 0 && --this.tantrumWindup == 0)
        	{
        		doTantrumSlam();
        	}
        }
        else
        {
        	this.tantrumWindup = 0;
        }

        // do this
		checkAndChangeCourse();

        // check if we are at our waypoint target
		double offsetX = this.waypointX - this.posX;
		double offsetY = this.waypointY - this.posY;
		double offsetZ = this.waypointZ - this.posZ;
        double distanceToWaypoint = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

        if (distanceToWaypoint < 1.0D || distanceToWaypoint > 3600.0D)
        {
            makeNewWaypoint();
        }

        // move?
        if (this.courseChangeCooldown-- <= 0)
        	{
                this.courseChangeCooldown += this.rand.nextInt(5) + 0;
        		distanceToWaypoint = (double)MathHelper.sqrt_double(distanceToWaypoint);

        		double speed = 0.05D;
        		this.motionX += offsetX / distanceToWaypoint * speed;
        		this.motionY += offsetY / distanceToWaypoint * speed;
        		this.motionZ += offsetZ / distanceToWaypoint * speed;
        	}

        // check if our target is still within range
		double targetRange = (this.aggroCounter > 0 || this.isAggressive) ? aggroRange : stareRange;

        if (this.targetedEntity != null && this.targetedEntity.getDistanceSqToEntity(this) < targetRange * targetRange && this.canSeeEntity(this.targetedEntity))
        {
            // turn towards target
        	this.faceEntity(this.targetedEntity, 10F, this.getVerticalFaceSpeed());

        	// attack if aggressive
            if (this.isAggressive)
            {
                if (this.attackCounter == 10)
                {
            		this.playSound("mob.ghast.charge", this.getSoundVolume("mob.ghast.charge"), this.getSoundPitch("mob.ghast.charge"));
                }

                ++this.attackCounter;

                if (this.attackCounter == 20)
                {
                	spitFireball();
            		this.attackCounter = -40;
                }
            }
         }
        else
        {
        	// ignore player, move normally
            this.isAggressive = false;
        	this.targetedEntity = null;
        	this.rotationYaw = -((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI;

        	// changing the pitch with movement looks goofy and un-ghast-like
        	this.rotationPitch = 0;
        }

        if (this.attackCounter > 0 && !this.isAggressive)
        {
            --this.attackCounter;
        }

        // set aggro status
        byte currentAggroStatus = this.dataWatcher.getWatchableObjectByte(16);
        byte newAggroStatus = (byte)(this.attackCounter > 10 ? 2 : (this.aggroCounter > 0 || this.isAggressive) ? 1 : 0);

        if (currentAggroStatus != newAggroStatus)
        {
        	this.dataWatcher.updateObject(16, Byte.valueOf(newAggroStatus));
        }
    }

	/**
	 * The area below us that the tantrum affects.
	 */
	private AxisAlignedBB getTantrumArea() {
		return this.boundingBox.getOffsetBoundingBox(0, -16, 0).expand(0, 16, 0);
	}

	/**
	 * True if the player stands in the open below us.
	 */
	private boolean isExposedPlayer(EntityPlayer player) {
		int dx = MathHelper.floor_double(player.posX);
		int dy = MathHelper.floor_double(player.posY);
		int dz = MathHelper.floor_double(player.posZ);

		return worldObj.canBlockSeeTheSky(dx, dy, dz);
	}

	@SuppressWarnings("unchecked")
	private void doTantrumDamageEffects() {
		// harm player below (balance: telegraphed, the slam lands TANTRUM_WINDUP ticks after we notice a victim)
		AxisAlignedBB below = getTantrumArea();

		if (this.tantrumWindup <= 0)
		{
			List<EntityPlayer> playersBelow = worldObj.getEntitiesWithinAABB(EntityPlayer.class, below);

			for (EntityPlayer player : playersBelow)
			{
				if (isExposedPlayer(player))
				{
					this.tantrumWindup = TANTRUM_WINDUP;
					this.playSound("mob.ghast.charge", this.getSoundVolume("mob.ghast.charge"), this.getSoundPitch("mob.ghast.charge"));
					break;
				}
			}
		}

		// also suck up mini ghasts
		List<EntityTFMiniGhast> ghastsBelow = worldObj.getEntitiesWithinAABB(EntityTFMiniGhast.class, below);

		for (EntityTFMiniGhast ghast : ghastsBelow)
		{
			ghast.motionY += 1;
		}
	}

	/**
	 * The telegraphed tantrum slam: hurt every exposed player currently below us.
	 */
	@SuppressWarnings("unchecked")
	private void doTantrumSlam() {
		List<EntityPlayer> playersBelow = worldObj.getEntitiesWithinAABB(EntityPlayer.class, getTantrumArea());

		for (EntityPlayer player : playersBelow)
		{
			if (isExposedPlayer(player))
			{
				player.attackEntityFrom(new Damage(DamageSource.anvil, TANTRUM_DAMAGE));
			}
		}
	}

    /**
     * Make a tear particle fall off the ghast
     */
    private void shedTear()
    {
		TwilightForestMod.proxy.spawnParticle(this.worldObj, "bosstear", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0);
	}

	/**
     * Waypoints should be closer to our course than we are now.  Try 50 times and take the closest
     */
	protected void makeNewWaypoint()
	{
        double closestDistance = this.getDistanceSq(this.courseX, this.courseY, this.courseZ);

		for (int i = 0; i < 50; i++)
		{
			double potentialX = this.posX + (double)((this.rand.nextFloat() * 2.0F - 1.0F) * wanderFactor);
			double potentialY = this.courseY + this.rand.nextFloat() * 8.0F - 4.0F;
			double potentialZ = this.posZ + (double)((this.rand.nextFloat() * 2.0F - 1.0F) * wanderFactor);

			double offsetX = this.courseX - potentialX;
			double offsetY = this.courseY - potentialY;
			double offsetZ = this.courseZ - potentialZ;

			double potentialDistanceToCourse = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

			if (potentialDistanceToCourse < closestDistance)
			{
				this.waypointX = potentialX;
				this.waypointY = potentialY;
				this.waypointZ = potentialZ;

				closestDistance = potentialDistanceToCourse;
			}
		}
	}

	protected void checkAndChangeCourse() {
		// check course.
        if (courseX == 0 && courseY == 0 && courseZ == 0)
        {
        	changeCourse();
        }

        double offsetX = this.courseX - this.posX;
        double offsetY = this.courseY - this.posY;
        double offsetZ = this.courseZ - this.posZ;
        double distanceToCourse = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;

        if (distanceToCourse < 100.0D)
        {
        	changeCourse();
        }
	}

	/**
	 * Change to a new course target
	 */
    private void changeCourse() {
		if (this.travelCoords.isEmpty())
		{
			makeTravelPath();
		}

		if (!this.travelCoords.isEmpty())
		{
			if (this.currentTravelCoordIndex >= travelCoords.size())
			{
				this.currentTravelCoordIndex = 0;
				this.travelPathRepetitions++;

				// when we're in tantrum mode, this is a good time to check if we need to spawn more ghasts
				if (!checkGhastsAtTraps())
				{
					this.spawnGhastsAtTraps();
				}
			}

			this.courseX = travelCoords.get(currentTravelCoordIndex).posX;
			this.courseY = travelCoords.get(currentTravelCoordIndex).posY + HOVER_ALTITUDE;
			this.courseZ = travelCoords.get(currentTravelCoordIndex).posZ;

			this.currentTravelCoordIndex++;
		}
	}

    /**
     * Check if there are at least 4 ghasts near at least 2 traps.  Return false if not.
     */
	@SuppressWarnings("unchecked")
	private boolean checkGhastsAtTraps() {
		int trapsWithEnoughGhasts = 0;

		for (ChunkCoordinates trap : this.trapLocations)
		{
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(trap.posX, trap.posY, trap.posZ, trap.posX + 1, trap.posY + 1, trap.posZ + 1).expand(8D, 16D, 8D);

			List<EntityTFMiniGhast> nearbyGhasts = worldObj.getEntitiesWithinAABB(EntityTFMiniGhast.class, aabb);

			if (nearbyGhasts.size() >= 4)
			{
				trapsWithEnoughGhasts++;
			}
		}

		return trapsWithEnoughGhasts >= 1;
	}

	private void makeTravelPath()
	{
		ArrayList<ChunkCoordinates> potentialPoints;

		int px = MathHelper.floor_double(this.posX);
		int py = MathHelper.floor_double(this.posY);
		int pz = MathHelper.floor_double(this.posZ);

		if (!this.noTrapMode)
		{
			// make a copy of the trap locations list
			potentialPoints = new ArrayList<ChunkCoordinates>(this.trapLocations);
		}
		else
		{
			potentialPoints = new ArrayList<ChunkCoordinates>();
			potentialPoints.add(new ChunkCoordinates(px + 20, py - HOVER_ALTITUDE, pz));
			potentialPoints.add(new ChunkCoordinates(px, py - HOVER_ALTITUDE, pz - 20));
			potentialPoints.add(new ChunkCoordinates(px - 20, py - HOVER_ALTITUDE, pz));
			potentialPoints.add(new ChunkCoordinates(px, py - HOVER_ALTITUDE, pz + 20));
		}

		// put the potential points on our travel coords list in random order
		travelCoords.clear();

		while (!potentialPoints.isEmpty())
		{
			int index = rand.nextInt(potentialPoints.size());

			travelCoords.add(potentialPoints.get(index));

			potentialPoints.remove(index);
		}

		if (this.noTrapMode)
		{
			// if in no trap mode, head back to the middle when we're done
			travelCoords.add(new ChunkCoordinates(px, py - HOVER_ALTITUDE, pz));
		}
	}

	/**
     * Spit a fireball
     */
	@Override
	protected void spitFireball() {
		double offsetX = this.targetedEntity.posX - this.posX;
		double offsetY = this.targetedEntity.boundingBox.minY + (double)(this.targetedEntity.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
		double offsetZ = this.targetedEntity.posZ - this.posZ;

		// fireball sound effect
		this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, (int)this.posX, (int)this.posY, (int)this.posZ, 0);

		EntityTFUrGhastFireball entityFireball = new EntityTFUrGhastFireball(this.worldObj, this, offsetX, offsetY, offsetZ);
		entityFireball.field_92057_e = 1;
		double shotSpawnDistance = 8.5D;
		Vec3 lookVec = this.getLook(1.0F);
		entityFireball.setPosition(this.posX + lookVec.xCoord * shotSpawnDistance,
				this.posY + (double)(this.height / 2.0F) + lookVec.yCoord * shotSpawnDistance,
				this.posZ + lookVec.zCoord * shotSpawnDistance);
		this.worldObj.spawnEntityInWorld(entityFireball);

		for (int i = 0; i < 2; i++)
		{
			entityFireball = new EntityTFUrGhastFireball(this.worldObj, this, offsetX + (rand.nextFloat() - rand.nextFloat()) * 8, offsetY, offsetZ + (rand.nextFloat() - rand.nextFloat()) * 8);
			entityFireball.field_92057_e = 1;
			entityFireball.setPosition(this.posX + lookVec.xCoord * shotSpawnDistance,
					this.posY + (double)(this.height / 2.0F) + lookVec.yCoord * shotSpawnDistance,
					this.posZ + lookVec.zCoord * shotSpawnDistance);
			this.worldObj.spawnEntityInWorld(entityFireball);
		}
	}

	/**
	 * Scan a few chunks around us for ghast trap blocks and if we find any, add them to our list
	 */
	private void scanForTrapsTwice()
	{
		int scanRangeXZ = 48;
		int scanRangeY = 32;

		int px = MathHelper.floor_double(this.posX);
		int py = MathHelper.floor_double(this.posY);
		int pz = MathHelper.floor_double(this.posZ);

		scanForTraps(scanRangeXZ, scanRangeY, px, py, pz);

		if (trapLocations.size() > 0)
		{
			// average the location of the traps we've found, and scan again from there
			int ax = 0, ay = 0, az = 0;

			for(ChunkCoordinates trapCoords : trapLocations)
			{
				ax += trapCoords.posX;
				ay += trapCoords.posY;
				az += trapCoords.posZ;
			}

			ax /= trapLocations.size();
			ay /= trapLocations.size();
			az /= trapLocations.size();

			scanForTraps(scanRangeXZ, scanRangeY, ax, ay, az);
		}
	}

	/**
	 * Scan in the specified range for traps
	 */
	protected void scanForTraps(int scanRangeXZ, int scanRangeY, int px, int py, int pz) {
		for (int sx = -scanRangeXZ; sx <= scanRangeXZ; sx++) {
			for (int sz = -scanRangeXZ; sz <= scanRangeXZ; sz++) {
				for (int sy = -scanRangeY; sy <= scanRangeY; sy++) {
					if (isTrapAt(px + sx, py + sy, pz + sz)) {
						// add to list
						ChunkCoordinates trapCoords = new ChunkCoordinates(px + sx, py + sy, pz + sz);
						if (!trapLocations.contains(trapCoords))
						{
							trapLocations.add(trapCoords);
						}
					}
				}
			}
		}
	}

	private boolean isTrapAt(int x, int y, int z)
	{
		if (!worldObj.blockExists(x, y, z) || worldObj.getBlockId(x, y, z) != TFBlocks.towerDevice.blockID)
		{
			return false;
		}

		int meta = worldObj.getBlockMetadata(x, y, z);
		return meta == BlockTFTowerDevice.META_GHASTTRAP_INACTIVE || meta == BlockTFTowerDevice.META_GHASTTRAP_ACTIVE;
	}

    /**
     * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    @Override
	public boolean isBurning()
    {
    	// never burn
    	return false;
    }

    /**
     * No point in ever being pushed
     */
	@Override
	public boolean canBePushed() {
		return false;
	}

    /**
     * Are we throwing a tantrum?
     */
    public boolean isInTantrum()
    {
        return this.dataWatcher.getWatchableObjectByte(DATA_TANTRUM) != (byte)0;
    }

    /**
     * Sets whether we are throwing a tantrum
     */
    public void setInTantrum(boolean par1)
    {
    	this.dataWatcher.updateObject(DATA_TANTRUM, par1 ? Byte.valueOf((byte)-1) : Byte.valueOf((byte)0));

    	// can we just reset this each time it is called?
    	this.damageUntilNextPhase = DAMAGE_PER_PHASE;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
	@Override
    protected float getSoundVolume(String sound)
    {
        return 16F;
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
	@Override
    protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.5F;
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setBoolean("inTantrum", this.isInTantrum());
        super.writeEntityToNBT(nbttagcompound);
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        this.setInTantrum(nbttagcompound.getBoolean("inTantrum"));
    }

    /**
     * handles entity death timer, experience orb and particle creation
     */
    @Override
    protected void onDeathUpdate()
    {
    	super.onDeathUpdate();

        if (this.deathTime == 20 && !worldObj.isRemote)
        {
        	// make chest
        	ChunkCoordinates chestCoords = this.findChestCoords();
        	TFTreasure.darktower_boss.generate(worldObj, null, chestCoords.posX, chestCoords.posY, chestCoords.posZ);
        	if (killResult != null && killResult.repeat())
        	{
        		TFProgress.halveChest(worldObj, chestCoords.posX, chestCoords.posY, chestCoords.posZ);
        	}
        }
    }

    /**
     * Trigger achievement when killed
     */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		if (!worldObj.isRemote && killResult == null) {
			killResult = TFProgress.onBossKilled(this, TFProgress.Flag.URGHAST, TFAchievementPage.twilightHunter);
		}
		super.onDeath(par1DamageSource);
		if (!worldObj.isRemote && killResult != null) {
			killResult.awardXp(300);
		}

		// mark the tower as defeated
		if (!worldObj.isRemote && worldObj.provider instanceof WorldProviderTwilightForest) {
        	ChunkCoordinates chestCoords = this.findChestCoords();
			int dx = chestCoords.posX;
			int dy = chestCoords.posY;
			int dz = chestCoords.posZ;

			ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest)worldObj.provider).getChunkProvider();
			TFFeature nearbyFeature = ((TFWorldChunkManager)worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);

			if (nearbyFeature == TFFeature.darkTower) {
				chunkProvider.setStructureConquered(dx, dy, dz, true);
			}
		}
	}

    private ChunkCoordinates findChestCoords() {
    	if (trapLocations.size() > 0)
    	{
    		// average the location of the traps we've found, and scan again from there
    		int ax = 0, ay = 0, az = 0;

    		for(ChunkCoordinates trapCoords : trapLocations)
    		{
    			ax += trapCoords.posX;
    			ay += trapCoords.posY;
    			az += trapCoords.posZ;
    		}

    		ax /= trapLocations.size();
    		ay /= trapLocations.size();
    		az /= trapLocations.size();

    		return new ChunkCoordinates(ax, ay + 2, az);
    	}
    	else
    	{
    		return new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
    	}
	}
}
