package twilightforest.entity.boss;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityGhoul;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityLongdead;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySpider;
import net.minecraft.EntityZombie;
import net.minecraft.EnchantmentHelper;
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.IBossDisplayData;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TFProgress;
import twilightforest.compat.TFParticles;
import twilightforest.entity.EntityTFSwarmSpider;
import twilightforest.item.TFItems;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;


/**
 * The Lich. Uses the MITE "old AI" (isAIEnabled() == false): targets are found by EntityCreature and the fight is run from
 * attackEntity(Entity, float). Because of that the Lich must only use getEntityToAttack()/setEntityToAttack()/getTarget(),
 * never getAttackTarget()/setAttackTarget().
 */
public class EntityTFLich extends EntityMob implements IBossDisplayData {


	private static final int DATA_ISCLONE = 21;
	private static final int DATA_SHIELDSTRENGTH = 17;
	private static final int DATA_MINIONSLEFT = 18;
	private static final int DATA_ATTACKTYPE = 20;
	// (TF 1.7.10 also kept the boss health in data watcher 19; MITE syncs health itself and the boss bar reads getHealth())

	EntityTFLich masterLich;

	/** Created lazily: the entity class is loaded during entity registration, before TFItems exist */
	private static ItemStack[] heldItems;
	/** Set once in onDeath (server side, real lich only), read by dropFewItems. */
	private TFProgress.KillResult killResult;
	public static final int MAX_SHADOW_CLONES = 2;
	public static final int INITIAL_SHIELD_STRENGTH = 5;
	public static final int MAX_ACTIVE_MINIONS = 3;
	public static final int INITIAL_MINIONS_TO_SUMMON = 9;
	/** MITE_BALANCE: 110 HP (TF: 100) */
	public static final int MAX_HEALTH = 110;
	/** MITE_BALANCE: melee 10 (TF: 6) */
	public static final double MELEE_DAMAGE = 10.0D;
	/** MITE_BALANCE: flat natural defense 3, 0 against magic */
	public static final float NATURAL_DEFENSE = 3.0F;
	/** MITE_BALANCE: 150 XP on the first kill (TF: 217) */
	public static final int EXPERIENCE = 150;


	/**
	 * Make a new master lich.
	 */
	public EntityTFLich(World world) {
		super(world);
		setSize(1.1F, 2.5F);
		//texture = TwilightForestMod.MODEL_DIR + "twilightlich64.png";
//        this.yOffset = .25F;

		setShadowClone(false);
		this.masterLich = null;
		setShieldStrength(INITIAL_SHIELD_STRENGTH);
		setMinionsToSummon(INITIAL_MINIONS_TO_SUMMON);
	}

	public EntityTFLich(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	/**
	 * Make a lich shadow clone
	 */
	public EntityTFLich(World world, EntityTFLich otherLich) {
		this(world);

		setShadowClone(true);
		this.masterLich = otherLich;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(DATA_ISCLONE, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(DATA_SHIELDSTRENGTH, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(DATA_MINIONSLEFT, Byte.valueOf((byte) 0));
		this.dataWatcher.addObject(DATA_ATTACKTYPE, Byte.valueOf((byte) 0));
	}

	/**
	 * Old AI: the Lich handles its own targeting and movement
	 */
	@Override
	protected boolean isAIEnabled()
	{
		return false;
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(MAX_HEALTH); // max health
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(MELEE_DAMAGE); // attack damage
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.800000011920929D); // movement speed
	}

	/**
	 * Natural defense (balance: 3, 0 against magic so the reflected bolts always do their full damage)
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return damage_source.hasMagicAspect() ? 0.0F : NATURAL_DEFENSE;
	}

	/**
	 * Balance: the 150 xp (x0.25 on repeat kills) are handed out to the credited players in onDeath, so no orbs.
	 */
	@Override
	public int getExperienceValue()
	{
		return 0;
	}

	/**
	 * Bosses never flee when hurt
	 */
	@Override
	public boolean fleesWhenBadlyWounded()
	{
		return false;
	}

	/**
	 * Immune to fire and lava (TF: isImmuneToFire = true)
	 */
	@Override
	public boolean isHarmedByFire()
	{
		return false;
	}

	@Override
	public boolean isHarmedByLava()
	{
		return false;
	}

	private static ItemStack[] getHeldItems()
	{
		if (heldItems == null) {
			heldItems = new ItemStack[] {new ItemStack(TFItems.scepterTwilight, 1), new ItemStack(TFItems.scepterZombie, 1), new ItemStack(Item.swordGold, 1)};
		}
		return heldItems;
	}

	/**
	 * 1.7.10 override of getHeldItem() (which returned an ItemStack)
	 */
	@Override
	public ItemStack getHeldItemStack()
	{
		return getHeldItems()[getPhase() - 1];
	}

	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		// MITE_BALANCE H8: the scepter and the trophy only on the first kill, the rest halved on repeat kills
		boolean unique = killResult == null || killResult.dropsUniques();
		if (unique) {
			dropScepter();
		}

		// 1-2 enchanted golden things
		int totalDrops = this.rand.nextInt(2) + 1;
		if (killResult != null) {
			totalDrops = killResult.material(totalDrops);
		}
		for (int i = 0; i < totalDrops; ++i)
		{
			dropGoldThing();
		}

		// ender pearls 1-2 (repeat: 1)
		totalDrops = this.rand.nextInt(2) + 1;
		if (killResult != null) {
			totalDrops = killResult.material(totalDrops);
		}
		for (int i = 0; i < totalDrops; ++i)
		{
			this.dropItem(Item.enderPearl, 1);
		}

		// bones 5-9
		totalDrops = this.rand.nextInt(5) + 5;
		if (killResult != null) {
			totalDrops = killResult.material(totalDrops);
		}
		for (int i = 0; i < totalDrops; ++i)
		{
			this.dropItem(Item.bone, 1);
		}

		// trophy, first kill only
		if (unique) {
			this.dropItemStack(new ItemStack(TFItems.trophy, 1, 2), 0.0F);
		}
	}

	private void dropScepter() {
		int scepterType = rand.nextInt(3);
		if (scepterType == 0) {
			this.dropItemStack(new ItemStack(TFItems.scepterZombie), 0.0F);
		}
		else if (scepterType == 1) {
			this.dropItemStack(new ItemStack(TFItems.scepterLifeDrain), 0.0F);
		}
		else {
			this.dropItemStack(new ItemStack(TFItems.scepterTwilight), 0.0F);
		}
	}

	private void dropGoldThing() {
		ItemStack goldThing;
		int thingType = rand.nextInt(5);
		if (thingType == 0) {
			goldThing = new ItemStack(Item.swordGold);
		}
		else if (thingType == 1) {
			goldThing = new ItemStack(Item.helmetGold);
		}
		else if (thingType == 2) {
			goldThing = new ItemStack(Item.plateGold);
		}
		else if (thingType == 3) {
			goldThing = new ItemStack(Item.legsGold);
		}
		else {
			goldThing = new ItemStack(Item.bootsGold);
		}
		// enchant! (balance: enchantment level cap 4 for T1 loot)
		EnchantmentHelper.addRandomEnchantment(rand, goldThing, 4);
		this.dropItemStack(goldThing, 0.0F);
	}

	/**
	 * Sets the Entity inside a web block.
	 * We are immune to webs.
	 */
	@Override
	public void setInWeb() {
		// nope!
	}

	/**
	 * Determines if an entity can be despawned, used on idle far away entities
	 */
	@Override
	protected boolean canDespawn()
	{
		return false;
	}


	/**
	 * Do not get slowed by lava.
	 */
	@Override
	public boolean handleLavaMovement()
	{
		return false;
	}

	/**
	 * Do not get slowed by water.
	 */
	@Override
	public boolean isInWater()
	{
		return false;
	}

	/**
	 * What phase of the fight are we on?
	 *
	 * 1 - reflecting bolts, shield up
	 * 2 - summoning minions
	 * 3 - melee
	 */
	public int getPhase() {
		if (isShadowClone() || getShieldStrength() > 0) {
			return 1;
		}
		else if (getMinionsToSummon() > 0) {
			return 2;
		}
		else {
			return 3;
		}
	}

	/**
	 * For now we'll just add some cute particles.
	 */
	@Override
	public void onLivingUpdate() {
		// determine the hand position
		float angle = ((renderYawOffset * 3.141593F) / 180F);

		double dx = posX + (MathHelper.cos(angle) * 0.65);
		double dy = posY + (height * 0.94);
		double dz = posZ + (MathHelper.sin(angle) * 0.65);


		// add particles!

		// how many particles do we want to add?!
		int factor = (80 - attackTime) / 10;
		int particles = factor > 0 ? rand.nextInt(factor) : 1;


		for (int j1 = 0; j1 < particles; j1++)
		{
			float sparkle = 1.0F - (attackTime + 1.0F) / 60.0F;
			sparkle *= sparkle;

			float red = 0.37F * sparkle;
			float grn = 0.99F * sparkle;
			float blu = 0.89F * sparkle;

			// change color for fireball
			if (this.getNextAttackType() != 0)
			{
				red = 0.99F * sparkle;
				grn = 0.47F * sparkle;
				blu = 0.00F * sparkle;
			}

			TFParticles.spawn(worldObj, "mobSpell", dx + (rand.nextGaussian() * 0.025), dy + (rand.nextGaussian() * 0.025), dz + (rand.nextGaussian() * 0.025), red, grn, blu);
		}

		if (isShadowClone()) {
			// clones despawn if they have strayed from the master
			checkForMaster();
		}

		super.onLivingUpdate();
	}


	/**
	 * Called when we get attacked.
	 *
	 * Never switch targets to any lich.
	 *
	 * TODO maybe we could compare masters to make teams or something.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		DamageSource source = damage.getSource();
		Entity responsible = source.getResponsibleEntity();

		// the void (and /kill) always works, so a Lich can never get stuck out of the world
		if (source == DamageSource.outOfWorld) {
			return super.attackEntityFrom(damage);
		}

		// if we're in a wall, teleport for gosh sakes
		if ("inWall".equals(source.getDamageType()) && getEntityToAttack() != null) {
			teleportToSightOfEntity(getEntityToAttack());
		}

		if (isShadowClone()) {
			this.worldObj.playSoundAtEntity(this, "random.fizz", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			return null;
		}

		Entity prevTarget = this.getEntityToAttack();

		// ignore all bolts that are not reflected
		if (responsible instanceof EntityTFLich) {
			return null;
		}

		// if our shield is up, ignore any damage that can be blocked.
		if (getShieldStrength() > 0)
		{
			// 1.7.10 isUnblockable() was set on magic damage too; MITE magic is "bypasses mundane armor" + magic aspect
			if ((source.isUnblockable() || source.hasMagicAspect()) && damage.getAmount() > 2)
			{
				// reduce shield for magic damage greater than 1 heart
				if (getShieldStrength() > 0) {
					setShieldStrength(getShieldStrength() - 1);
					this.worldObj.playSoundAtEntity(this, "random.break", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				}
			}
			else
			{
				this.worldObj.playSoundAtEntity(this, "random.break", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				// HACK for creative mode: but get annoyed at what's causing it.
				if (responsible instanceof EntityPlayer)
				{
					this.setEntityToAttack(responsible);
				}
			}

			return null;
		}

		// never attack another lich?
		//TODO: this could better check who is actually attacking against the masterLich variable thing
		EntityDamageResult result = super.attackEntityFrom(damage);
		if (result != null && result.entityWasNegativelyAffected())
		{
			if (getEntityToAttack() instanceof EntityTFLich) {
				this.setEntityToAttack(prevTarget);
			}

			// if we were attacked successfully during phase 1 or 2, teleport
			// during phase 3, 1 in 4 chance of teleport
			if (!this.isDead && (this.getPhase() < 3 || rand.nextInt(4) == 0))
			{
				this.teleportToSightOfEntity(this.getEntityToAttack());
			}
		}
		return result;
	}


	/**
	 * MITE only calls attackEntity() when the melee strike line is clear; the Lich fires bolts from afar and
	 * attackEntity() does its own canSeeEntity checks, so the strike-line gate is lifted.
	 */
	@Override
	public boolean hasLineOfStrike(Entity target)
	{
		return true;
	}

	/**
	 * Attack!
	 */
	@Override
	protected void attackEntity(Entity targetedEntity, float f)
	{
		if (!isShadowClone() && attackTime % 15 == 0) {
			popNearbyMob();
		}

		if (getPhase() == 1) {
			if (attackTime == 60 && !worldObj.isRemote) {

				teleportToSightOfEntity(targetedEntity);

				if (!isShadowClone()) {
					// if we are the master, check for clones
					checkAndSpawnClones(targetedEntity);
				}
			}

			if (canSeeEntity(targetedEntity) && attackTime == 0 && f < 20F)
			{
				if (this.getNextAttackType() == 0)
				{
					// if not, and we can see the target, launch a bolt
					launchBoltAt(targetedEntity);
				}
				else
				{
					launchBombAt(targetedEntity);
				}

				// decide next attack type
				if (rand.nextInt(3) > 0)
				{
					this.setNextAttackType(0);
				}
				else
				{
					this.setNextAttackType(1);
				}
				attackTime = 100;

			}
			// do not chase the player
			hasAttacked = true;
		}
		if (getPhase() == 2 && !isShadowClone()) {
			despawnClones();

			// spawn minions every so often
			if (attackTime % 15 == 0) {
				// spawn minion
				checkAndSpawnMinions(targetedEntity);
			}

			if (attackTime == 0) {
				// if we're in melee, attack
				if (f < 2.0F) {
					// melee attack
					this.attackEntityAsMob(targetedEntity);
					attackTime = 20;
				}
				else if (f < 20F && canSeeEntity(targetedEntity)) {
					// if not, and we can see the target, launch a bolt
					if (this.getNextAttackType() == 0)
					{
						// if not, and we can see the target, launch a bolt
						launchBoltAt(targetedEntity);
					}
					else
					{
						launchBombAt(targetedEntity);
					}

					// decide next attack type
					if (rand.nextInt(2) > 0)
					{
						this.setNextAttackType(0);
					}
					else
					{
						this.setNextAttackType(1);
					}
					attackTime = 60;
				}
				else {
					// if not, teleport around
					teleportToSightOfEntity(targetedEntity);
					attackTime = 20;

				}
			}
			// do not chase
			hasAttacked = true;

		}
		if (getPhase() == 3) {
			// melee!
			if (this.attackTime <= 0 && f < 2.0F && targetedEntity.boundingBox.maxY > this.boundingBox.minY && targetedEntity.boundingBox.minY < this.boundingBox.maxY)
			{
				this.attackTime = 20;
				this.attackEntityAsMob(targetedEntity);
				hasAttacked = true;
			}
		}


	}

	/**
	 * Fires a lich bolt at the target
	 * @param targetedEntity
	 */
	protected void launchBoltAt(Entity targetedEntity) {
		float bodyFacingAngle = ((renderYawOffset * 3.141593F) / 180F);
		double sx = posX + (MathHelper.cos(bodyFacingAngle) * 0.65);
		double sy = posY + (height * 0.82);
		double sz = posZ + (MathHelper.sin(bodyFacingAngle) * 0.65);

		double tx = targetedEntity.posX - sx;
		double ty = (targetedEntity.boundingBox.minY + (double) (targetedEntity.height / 2.0F)) - (posY + height / 2.0F);
		double tz = targetedEntity.posZ - sz;

		worldObj.playSoundAtEntity(this, "mob.ghast.fireball", getSoundVolume("mob.ghast.fireball"), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		EntityTFLichBolt projectile = new EntityTFLichBolt(worldObj, this);
		projectile.setThrowableHeading(tx, ty, tz, projectile.func_70182_d(), 1.0F);

		projectile.setLocationAndAngles(sx, sy, sz, rotationYaw, rotationPitch);

		worldObj.spawnEntityInWorld(projectile);
	}

	/**
	 * Fires a lich bomb at the target
	 * @param targetedEntity
	 */
	protected void launchBombAt(Entity targetedEntity) {
		float bodyFacingAngle = ((renderYawOffset * 3.141593F) / 180F);
		double sx = posX + (MathHelper.cos(bodyFacingAngle) * 0.65);
		double sy = posY + (height * 0.82);
		double sz = posZ + (MathHelper.sin(bodyFacingAngle) * 0.65);

		double tx = targetedEntity.posX - sx;
		double ty = (targetedEntity.boundingBox.minY + (double) (targetedEntity.height / 2.0F)) - (posY + height / 2.0F);
		double tz = targetedEntity.posZ - sz;

		worldObj.playSoundAtEntity(this, "mob.ghast.fireball", getSoundVolume("mob.ghast.fireball"), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		EntityTFLichBomb projectile = new EntityTFLichBomb(worldObj, this);
		projectile.setThrowableHeading(tx, ty, tz, projectile.func_70182_d(), 1.0F);

		projectile.setLocationAndAngles(sx, sy, sz, rotationYaw, rotationPitch);

		worldObj.spawnEntityInWorld(projectile);
	}

	/**
	 * Check the surrounding area for weaker monsters, and if we find any, magically destroy them.
	 */
	@SuppressWarnings("unchecked")
	protected void popNearbyMob() {
		List<Entity> nearbyMobs = worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));

		for (Entity entity : nearbyMobs) {
			if (entity instanceof EntityLiving && canPop(entity) && canSeeEntity(entity)) {
				EntityLiving mob = (EntityLiving) entity;

				if (!worldObj.isRemote) {
					mob.setDead();

					mob.spawnExplosionParticle();

					// play death sound
//					worldObj.playSoundAtEntity(mob, mob.getDeathSound(), mob.getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
				}

				// make trail so it's clear that we did it
				makeRedMagicTrail(mob.posX, mob.posY + mob.height / 2.0, mob.posZ, this.posX, this.posY + this.height / 2.0, this.posZ);

				// one per cycle, please
				break;
			}
		}


	}

	/**
	 * Can we pop the nearby thing? (exact class match, so TF mobs and our own minions are never popped; the MITE zombie/skeleton variants were added)
	 */
	@SuppressWarnings("rawtypes")
	protected boolean canPop(Entity nearby) {
		Class[] poppable = {EntitySkeleton.class, EntityLongdead.class, EntityZombie.class, EntityGhoul.class, EntityEnderman.class, EntitySpider.class, EntityCreeper.class, EntityTFSwarmSpider.class};

		for (Class pop : poppable) {
			if (nearby.getClass() == pop) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Look and see if there are enough clones in the surrounding area.  If there are not, spawn one.
	 * @param targetedEntity
	 */
	protected void checkAndSpawnClones(Entity targetedEntity) {
		// if not, spawn one!
		if (countMyClones() < EntityTFLich.MAX_SHADOW_CLONES) {
			spawnShadowClone(targetedEntity);
		}
	}

	@SuppressWarnings("unchecked")
	protected int countMyClones() {
		// check if there are enough clones.  we check a 32x16x32 area
		List<EntityTFLich> nearbyLiches = worldObj.getEntitiesWithinAABB(EntityTFLich.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));
		int count = 0;

		for (EntityTFLich nearbyLich : nearbyLiches) {
			if (nearbyLich.isShadowClone() && nearbyLich.masterLich == this) {
				count++;
			}
		}

		return count;
	}

	public boolean wantsNewClone(EntityTFLich clone) {
		return clone.isShadowClone() && countMyClones() < EntityTFLich.MAX_SHADOW_CLONES;
	}


	protected void spawnShadowClone(Entity targetedEntity) {
//		System.out.println("Lich is making a shadow clone!");


		// find a good spot
		Vec3 cloneSpot = findVecInLOSOf(targetedEntity);
		if (cloneSpot == null) {
			return;
		}

		// put a clone there
		EntityTFLich newClone = new EntityTFLich(worldObj, this);
		newClone.setPosition(cloneSpot.xCoord, cloneSpot.yCoord, cloneSpot.zCoord);
		worldObj.spawnEntityInWorld(newClone);

		newClone.setEntityToAttack(targetedEntity);
		newClone.attackTime = 60 + rand.nextInt(3) - rand.nextInt(3);

		// make sparkles leading to it
		makeTeleportTrail(posX, posY, posZ, cloneSpot.xCoord, cloneSpot.yCoord, cloneSpot.zCoord);
	}

	/**
	 * Despawn neaby clones
	 */
	@SuppressWarnings("unchecked")
	protected void despawnClones() {
		List<EntityTFLich> nearbyLiches = worldObj.getEntitiesWithinAABB(this.getClass(), AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));

		for (EntityTFLich nearbyLich : nearbyLiches) {
			if (nearbyLich.isShadowClone()) {
				nearbyLich.isDead = true;
			}
		}
	}



	protected void checkAndSpawnMinions(Entity targetedEntity) {
		if (!worldObj.isRemote && this.getMinionsToSummon() > 0 && targetedEntity instanceof EntityLivingBase) {
			int minions = countMyMinions();

			// if not, spawn one!
			if (minions < EntityTFLich.MAX_ACTIVE_MINIONS) {
				if (spawnMinionAt((EntityLivingBase) targetedEntity)) {
					this.setMinionsToSummon(this.getMinionsToSummon() - 1);
				}
			}
		}
		// if there's no minions left to summon, we should move into phase 3 naturally
	}

	@SuppressWarnings("unchecked")
	protected int countMyMinions() {
		// check if there are enough clones.  we check a 32x16x32 area
		List<EntityTFLichMinion> nearbyMinons = worldObj.getEntitiesWithinAABB(EntityTFLichMinion.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));
		int count = 0;

		for (EntityTFLichMinion nearbyMinon : nearbyMinons) {
			if (nearbyMinon.master == this) {
				count++;
			}
		}

		return count;
	}

	/**
	 * @return true if a minion was summoned
	 */
	protected boolean spawnMinionAt(EntityLivingBase targetedEntity) {

		// find a good spot
		Vec3 minionSpot = findVecInLOSOf(targetedEntity);
		if (minionSpot == null) {
			return false;
		}

		// put a clone there
		EntityTFLichMinion minion = new EntityTFLichMinion(worldObj, this);
		minion.setPosition(minionSpot.xCoord, minionSpot.yCoord, minionSpot.zCoord);
		//minion.initCreature();
		worldObj.spawnEntityInWorld(minion);

		minion.setAttackTarget(targetedEntity);

		minion.spawnExplosionParticle();
		this.worldObj.playSoundAtEntity(minion, "random.pop", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

		// make sparkles leading to it
		makeBlackMagicTrail(posX, posY + this.getEyeHeight(), posZ, minionSpot.xCoord, minionSpot.yCoord + minion.height / 2.0, minionSpot.zCoord);
		return true;
	}

	public boolean wantsNewMinion(EntityTFLichMinion minion) {
		return countMyMinions() < EntityTFLich.MAX_ACTIVE_MINIONS;
	}

	/**
	 * A method on the clones that despawns this clone if we are too far from the master
	 */
	protected void checkForMaster() {
		if (masterLich == null) {
			findNewMaster();
		}
		if (!worldObj.isRemote) {
			if (masterLich == null || masterLich.isDead) {
				this.isDead = true;
			}
		}
	}

	/**
	 * Find a new master for this clone
	 */
	@SuppressWarnings("unchecked")
	private void findNewMaster() {
		List<EntityTFLich> nearbyLiches = worldObj.getEntitiesWithinAABB(EntityTFLich.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));

		for (EntityTFLich nearbyLich : nearbyLiches) {
			if (!nearbyLich.isShadowClone() && nearbyLich.wantsNewClone(this)) {
				this.masterLich = nearbyLich;

				// animate our new linkage!
				makeTeleportTrail(posX, posY, posZ, nearbyLich.posX, nearbyLich.posY, nearbyLich.posZ);
				// become angry at our masters target
				setEntityToAttack(masterLich.getEntityToAttack());

				// quit looking
				break;
			}
		}
	}


	/**
	 * Teleports to within line of sight of (but not necessarily in view of) the target
	 */
	protected void teleportToSightOfEntity(Entity entity)
	{
		Vec3 dest = findVecInLOSOf(entity);
		double srcX = posX;
		double srcY = posY;
		double srcZ = posZ;

		if (dest != null) {
			teleportToNoChecks(dest.xCoord, dest.yCoord, dest.zCoord);
			faceEntity(entity, 100F, 100F);
			this.renderYawOffset = this.rotationYaw;

			if (!this.canSeeEntity(entity)) {
				// um teleport mishap, return to start
//            	System.out.println("Teleport fail!!!");
				teleportToNoChecks(srcX, srcY, srcZ);

			}
		}
	}

	/**
	 * Returns coords that would be good to teleport to.
	 *
	 * Returns null if we can't find anything (TF 1.7.10 returned the last random try even when every try had failed;
	 * the callers now skip the clone/minion in that case and teleports simply do not happen)
	 */
	protected Vec3 findVecInLOSOf(Entity targetEntity)
	{
		// for some reason we occasionally get null here
		if (targetEntity == null)
		{
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
			while (!groundFlag && ty > 0)
			{
				// (MITE: air is a null block, the material of air is not solid)
				if (!worldObj.getBlockMaterial(bx, by - 1, bz).isSolid())
				{
					ty--;
					by--;
				}
				else
				{
					groundFlag = true;
				}
			}

			// did we not find anything at all to stand on?
			if (by <= 0) {
//        		System.out.println("teleport find failed to find a block to stand on");
				continue;
			}

			//
			if (!canEntitySee(targetEntity, tx, ty, tz)) {
//        		System.out.println("teleport find failed because of lack of LOS");
				continue;
			}

			// check that we're not colliding and not in liquid
			float halfWidth = this.width / 2.0F;
			AxisAlignedBB destBox = AxisAlignedBB.getBoundingBox(tx - halfWidth, ty - yOffset + ySize, tz - halfWidth, tx + halfWidth, ty - yOffset + ySize + height, tz + halfWidth);
			if (worldObj.getCollidingBoundingBoxes(this, destBox).size() > 0)
			{
//        		System.out.println("teleport find failed because of collision");
				continue;
			}

			if (worldObj.isAnyLiquid(destBox)) {
//        		System.out.println("teleport find failed because of liquid at destination");
				continue;
			}

			// if we made it this far, we win!
			found = true;
			break;
		}

		if (!found) {
			return null;
		}

		return Vec3.createVectorHelper(tx, ty, tz);
	}

	/**
	 * Can the specified entity see the specified location?
	 */
	protected boolean canEntitySee(Entity entity, double dx, double dy, double dz) {
		return worldObj.checkForLineOfSight(Vec3.createVectorHelper(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ), Vec3.createVectorHelper(dx, dy, dz), false);
	}

	/**
	 * Does not check that the teleport destination is valid, we just go there
	 */
	protected boolean teleportToNoChecks(double destX, double destY, double destZ)
	{
		// save original position
		double srcX = posX;
		double srcY = posY;
		double srcZ = posZ;

		// change position
		setPosition(destX, destY, destZ);

		makeTeleportTrail(srcX, srcY, srcZ, destX, destY, destZ);

		worldObj.playSoundEffect(srcX, srcY, srcZ, "mob.endermen.portal", 1.0F, 1.0F);
		worldObj.playSoundAtEntity(this, "mob.endermen.portal", 1.0F, 1.0F);

		// sometimes we need to do this
		this.isJumping = false;

		return true;
	}

	/**
	 * Make a trail of particles from one point to another
	 */
	protected void makeTeleportTrail(double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
		// make particle trail
		int particles = 128;
		for (int i = 0; i < particles; i++)
		{
			double trailFactor = i / (particles - 1.0D);
			float f = (rand.nextFloat() - 0.5F) * 0.2F;
			float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
			float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
			double tx = srcX + (destX - srcX) * trailFactor + (rand.nextDouble() - 0.5D) * width * 2D;
			double ty = srcY + (destY - srcY) * trailFactor + rand.nextDouble() * height;
			double tz = srcZ + (destZ - srcZ) * trailFactor + (rand.nextDouble() - 0.5D) * width * 2D;
			TFParticles.spawn(worldObj, "spell", tx, ty, tz, f, f1, f2);
		}
	}

	/**
	 * Make a trail of particles from one point to another
	 */
	protected void makeRedMagicTrail(double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
		// make particle trail
		int particles = 32;
		for (int i = 0; i < particles; i++)
		{
			double trailFactor = i / (particles - 1.0D);
			float f = 1.0F;
			float f1 = 0.5F;
			float f2 = 0.5F;
			double tx = srcX + (destX - srcX) * trailFactor + rand.nextGaussian() * 0.005;
			double ty = srcY + (destY - srcY) * trailFactor + rand.nextGaussian() * 0.005;
			double tz = srcZ + (destZ - srcZ) * trailFactor + rand.nextGaussian() * 0.005;
			TFParticles.spawn(worldObj, "mobSpell", tx, ty, tz, f, f1, f2);
		}
	}

	/**
	 * Make a trail of particles from one point to another
	 */
	protected void makeBlackMagicTrail(double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
		// make particle trail
		int particles = 32;
		for (int i = 0; i < particles; i++)
		{
			double trailFactor = i / (particles - 1.0D);
			float f = 0.2F;
			float f1 = 0.2F;
			float f2 = 0.2F;
			double tx = srcX + (destX - srcX) * trailFactor + rand.nextGaussian() * 0.005;
			double ty = srcY + (destY - srcY) * trailFactor + rand.nextGaussian() * 0.005;
			double tz = srcZ + (destZ - srcZ) * trailFactor + rand.nextGaussian() * 0.005;
			TFParticles.spawn(worldObj, "mobSpell", tx, ty, tz, f, f1, f2);
		}
	}

	/**
	 * @return Is this lich a shadow clone or the real one?
	 */
	public boolean isShadowClone()
	{
		return (this.dataWatcher.getWatchableObjectByte(DATA_ISCLONE) & 2) != 0;
	}

	/**
	 * sets Is this lich a shadow clone or the real one?
	 */
	public void setShadowClone(boolean par1)
	{
		byte var2 = this.dataWatcher.getWatchableObjectByte(DATA_ISCLONE);

		if (par1)
		{
			this.dataWatcher.updateObject(DATA_ISCLONE, Byte.valueOf((byte) (var2 | 2)));
		}
		else
		{
			this.dataWatcher.updateObject(DATA_ISCLONE, Byte.valueOf((byte) (var2 & -3)));
		}
	}

	public byte getShieldStrength() {
		return this.dataWatcher.getWatchableObjectByte(DATA_SHIELDSTRENGTH);
	}

	public void setShieldStrength(int shieldStrength) {
		this.dataWatcher.updateObject(DATA_SHIELDSTRENGTH, Byte.valueOf((byte) shieldStrength));
	}

	public byte getMinionsToSummon() {
		return this.dataWatcher.getWatchableObjectByte(DATA_MINIONSLEFT);
	}

	public void setMinionsToSummon(int minionsToSummon) {
		this.dataWatcher.updateObject(DATA_MINIONSLEFT, Byte.valueOf((byte) minionsToSummon));
	}

	public byte getNextAttackType() {
		return this.dataWatcher.getWatchableObjectByte(DATA_ATTACKTYPE);
	}

	public void setNextAttackType(int attackType) {
		this.dataWatcher.updateObject(DATA_ATTACKTYPE, Byte.valueOf((byte) attackType));
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound()
	{
		return "mob.blaze.breathe";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound()
	{
		return "mob.blaze.hit";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound()
	{
		return "mob.blaze.death";
	}


	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("ShadowClone", isShadowClone());
		nbttagcompound.setByte("ShieldStrength", getShieldStrength());
		nbttagcompound.setByte("MinionsToSummon", getMinionsToSummon());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readEntityFromNBT(nbttagcompound);
		setShadowClone(nbttagcompound.getBoolean("ShadowClone"));
		setShieldStrength(nbttagcompound.getByte("ShieldStrength"));
		setMinionsToSummon(nbttagcompound.getByte("MinionsToSummon"));
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
		if (!worldObj.isRemote && !this.isShadowClone() && killResult == null) {
			killResult = TFProgress.onBossKilled(this, TFProgress.Flag.LICH, TFAchievementPage.twilightHunter, TFAchievementPage.twilightKillLich);
		}
		lootChest = twilightforest.TFBossLoot.begin(this);
		super.onDeath(par1DamageSource);
		twilightforest.TFBossLoot.finish(this, lootChest);
		lootChest = null;
		if (!worldObj.isRemote && killResult != null) {
			killResult.awardXp(EXPERIENCE);
		}
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			((EntityPlayer) par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
			((EntityPlayer) par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightKillLich);

		}

		// mark the tower as defeated
		if (!worldObj.isRemote && !this.isShadowClone()) {
			int dx = MathHelper.floor_double(this.posX);
			int dy = MathHelper.floor_double(this.posY);
			int dz = MathHelper.floor_double(this.posZ);

			if (worldObj.provider instanceof WorldProviderTwilightForest){
				ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) worldObj.provider).getChunkProvider();
				TFFeature nearbyFeature = ((TFWorldChunkManager) worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);

				if (nearbyFeature == TFFeature.lichTower) {
					chunkProvider.setStructureConquered(dx, dy, dz, true);
				}
			}
		}
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return EnumCreatureAttribute.UNDEAD;
	}

}
