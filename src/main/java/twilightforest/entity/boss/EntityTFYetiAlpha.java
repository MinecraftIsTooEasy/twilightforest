package twilightforest.entity.boss;

import java.util.ArrayList;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIArrowAttack;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.IRangedAttackMob;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTBase;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagDouble;
import net.minecraft.NBTTagList;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFProgress;
import twilightforest.TFFeature;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAIStayNearHome;
import twilightforest.entity.ai.EntityAITFThrowRider;
import twilightforest.entity.ai.EntityAITFYetiRampage;
import twilightforest.entity.ai.EntityAITFYetiTired;
import twilightforest.item.TFItems;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

public class EntityTFYetiAlpha extends EntityMob implements IRangedAttackMob {

	/** DataWatcher slots, allocated dynamically (MITE subclasses occupy fixed low ids). */
	private int rampageFlag;
	private int tiredFlag;
	private int collisionCounter;
	private boolean canRampage;

	public EntityTFYetiAlpha(World par1World) {
		super(par1World);
		this.setSize(3.8F, 5.0F);

		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(1, new EntityAITFYetiTired(this, 100));
		this.tasks.addTask(2, new EntityAITFThrowRider(this, 1.0F));
		this.tasks.addTask(3, new EntityAIStayNearHome(this, 2.0F));
		this.tasks.addTask(4, new EntityAITFYetiRampage(this, 10, 180));

		this.tasks.addTask(5, new EntityAIArrowAttack(this, 1.0D, 40, 40, 40.0F));
		this.tasks.addTask(6, new EntityAIWander(this, 2.0F));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.rampageFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.rampageFlag, Byte.valueOf((byte) 0));
		this.tiredFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.tiredFlag, Byte.valueOf((byte) 0));
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.4: HP 240, melee 14)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(240.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.38D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(14.0D);
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(40.0D);
	}

	/** MITE_BALANCE 9.4: natural defense 2 (none against armor-bypassing damage). */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return damage_source.bypassesMundaneArmor() ? 0.0F : 2.0F;
	}

	/** MITE_BALANCE 5.3: the alpha yeti stays dormant until a player of level 28 is near (the cave spawns it at generation). */
	public static final int MIN_LEVEL = 28;
	private boolean gateOpen;
	/** Set once in onDeath (server side), read by dropFewItems. */
	private TFProgress.KillResult killResult;

	private boolean isLevelGated() {
		if (this.gateOpen) {
			return false;
		}
		if (TFProgress.levelGated(this, MIN_LEVEL)) {
			return true;
		}
		this.gateOpen = true;
		return false;
	}

	@Override
	public void setAttackTarget(EntityLivingBase target) {
		if (target != null && isLevelGated()) {
			return;
		}
		super.setAttackTarget(target);
	}

	/** MITE_BALANCE 9.4: boss XP is handed out directly in onDeath. */
	@Override
	public int getExperienceValue() {
		return 0;
	}

	@Override
	public boolean fleesWhenBadlyWounded() {
		return false;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		if (!this.worldObj.isRemote && this.ticksExisted % 20 == 0 && isLevelGated()) {
			TFProgress.hintGated(this, MIN_LEVEL);
			if (this.getAttackTarget() != null) {
				this.setAttackTarget(null);
			}
		}
		if (this.riddenByEntity != null) {
			// stop player sneaking so that they can't dismount!
			if (this.riddenByEntity.isSneaking()) {
				this.riddenByEntity.setSneaking(false);
			}
		}

		super.onLivingUpdate();

		// look at things we are holding
		if (this.riddenByEntity != null) {
			this.getLookHelper().setLookPositionWithEntity(riddenByEntity, 100F, 100F);
		}

		if (this.isCollided) {
			this.collisionCounter++;
		}

		if (this.collisionCounter >= 15) {
			if (!this.worldObj.isRemote) {
				this.destroyBlocksInAABB(this.boundingBox);
			}
			this.collisionCounter = 0;
		}

		// add rampage snow effects
		if (this.isRampaging()) {

			float rotation = this.ticksExisted / 10F;

			for (int i = 0; i < 20; i++) {
				addSnowEffect(rotation + (i * 50), i + rotation);
			}

			// also swing limbs
			this.limbSwingAmount += 0.6;
		}

		// when tired, spawn tears/sweat
		if (this.isTired()) {
			for (int i = 0; i < 20; i++) {
				TFParticles.spawn(this.worldObj, "splash", this.posX + (this.rand.nextDouble() - 0.5D) * this.width * 0.5, this.posY + this.getEyeHeight(),
						this.posZ + (this.rand.nextDouble() - 0.5D) * this.width * 0.5, (rand.nextFloat() - 0.5F) * 0.75F, 0, (rand.nextFloat() - 0.5F) * 0.75F);
			}
		}

	}

	private void addSnowEffect(float rotation, float hgt) {
		double px = 3F * Math.cos(rotation);
		double py = hgt % 5F;
		double pz = 3F * Math.sin(rotation);

		TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowstuff", this.lastTickPosX + px, this.lastTickPosY + py, this.lastTickPosZ + pz, 0, 0, 0);
	}

	/**
	 * Called when a player interacts with a mob: climb into the yeti's grip (MITE: called on both sides, same result).
	 */
	@Override
	public boolean onEntityRightClicked(EntityPlayer player, ItemStack item_stack) {
		if (super.onEntityRightClicked(player, item_stack)) {
			return true;
		} else if (this.riddenByEntity == null || this.riddenByEntity == player) {
			if (!this.worldObj.isRemote) {
				player.mountEntity(this);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Pick up things we attack!
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {
		if (this.riddenByEntity == null && par1Entity.ridingEntity == null) {
			par1Entity.mountEntity(this);
		}

		return super.attackEntityAsMob(par1Entity);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		if (!this.worldObj.isRemote && isLevelGated()) {
			return null;
		}
		// no arrow damage when in ranged mode
		if (!this.canRampage && !this.isTired() && damage.getSource().isProjectile()) {
			return null;
		}

		EntityDamageResult result = super.attackEntityFrom(damage);

		this.canRampage = true;

		return result;
	}

	/**
	 * MITE_BALANCE 9.5: alpha fur 6-10 and ice bombs 3-6 on a first kill, halved on repeat kills.
	 */
	@Override
	protected void dropFewItems(boolean recentlyHit, DamageSource damage_source) {
		int looting = damage_source.getLootingModifier();

		int drops = 6 + rand.nextInt(5 + looting);
		if (killResult != null) {
			drops = killResult.material(drops);
		}
		for (int d = 0; d < drops; d++) {
			this.dropItem(TFItems.alphaFur, 1);
		}

		drops = 3 + rand.nextInt(4 + looting);
		if (killResult != null) {
			drops = killResult.material(drops);
		}
		for (int d = 0; d < drops; d++) {
			this.dropItem(TFItems.iceBomb, 1);
		}
	}

	@Override
	protected int getDropItemId() {
		return TFItems.alphaFur.itemID;
	}

	/**
	 * Put the player out in front of where we are
	 */
	@Override
	public void updateRiderPosition() {
		if (this.riddenByEntity != null) {
			Vec3 riderPos = this.getRiderPosition();

			this.riddenByEntity.setPosition(riderPos.xCoord, riderPos.yCoord, riderPos.zCoord);
		}
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	@Override
	public double getMountedYOffset() {
		return 5.75D;
	}

	/**
	 * Used to both get a rider position and to push out of blocks
	 */
	public Vec3 getRiderPosition() {
		if (this.riddenByEntity != null) {
			float distance = 0.4F;

			double var1 = Math.cos((this.rotationYaw + 90) * Math.PI / 180.0D) * distance;
			double var3 = Math.sin((this.rotationYaw + 90) * Math.PI / 180.0D) * distance;

			return Vec3.createVectorHelper(this.posX + var1, this.posY + this.getMountedYOffset() + this.riddenByEntity.getYOffset(), this.posZ + var3);
		} else {
			return Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
		}
	}

	// canRiderInteract(): Forge hook, not needed in MITE (TF_FORGE_HOOKS: a ridden entity is selectable by raycast).

	/**
	 * Destroys all blocks that aren't associated with 'The End' inside the given bounding box.
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
					Block currentID = this.worldObj.getBlock(dx, dy, dz);

					// MITE: getBlock returns null for air
					if (currentID != null) {
						int currentMeta = this.worldObj.getBlockMetadata(dx, dy, dz);

						if (currentID != Block.obsidian && currentID != Block.whiteStone && currentID != Block.bedrock && currentID != Block.mantleOrCore) {
							this.worldObj.setBlock(dx, dy, dz, 0, 0, 2);

							// here, this effect will have to do
							worldObj.playAuxSFX(2001, dx, dy, dz, currentID.blockID + (currentMeta << 12));
						} else {
							wasBlocked = true;
						}
					}
				}
			}
		}

		return wasBlocked;
	}

	public void makeRandomBlockFall() {
		// begin turning blocks into falling blocks
		makeRandomBlockFall(30);
	}

	private void makeRandomBlockFall(int range) {
		// find a block nearby
		int bx = MathHelper.floor_double(this.posX) + this.getRNG().nextInt(range) - this.getRNG().nextInt(range);
		int bz = MathHelper.floor_double(this.posZ) + this.getRNG().nextInt(range) - this.getRNG().nextInt(range);
		int by = MathHelper.floor_double(this.posY + this.getEyeHeight());

		makeBlockFallAbove(bx, bz, by);
	}

	private void makeBlockFallAbove(int bx, int bz, int by) {
		if (worldObj.isAirBlock(bx, by, bz)) {
			for (int i = 1; i < 30; i++) {
				if (!worldObj.isAirBlock(bx, by + i, bz)) {
					makeBlockFall(bx, by + i, bz);
					break;
				}
			}
		}
	}

	public void makeNearbyBlockFall() {
		makeRandomBlockFall(15);
	}

	public void makeBlockAboveTargetFall() {
		if (this.getAttackTarget() != null) {

			int bx = MathHelper.floor_double(this.getAttackTarget().posX);
			int bz = MathHelper.floor_double(this.getAttackTarget().posZ);
			int by = MathHelper.floor_double(this.getAttackTarget().posY + this.getAttackTarget().getEyeHeight());

			makeBlockFallAbove(bx, bz, by);
		}
	}

	private void makeBlockFall(int bx, int by, int bz) {
		Block currentID = this.worldObj.getBlock(bx, by, bz);
		int currentMeta = this.worldObj.getBlockMetadata(bx, by, bz);

		// just set it to ice for now (MITE has no packed ice)
		worldObj.setBlock(bx, by, bz, Block.ice.blockID);

		worldObj.playAuxSFX(2001, bx, by, bz, (currentID == null ? 0 : currentID.blockID) + (currentMeta << 12));

		EntityTFFallingIce ice = new EntityTFFallingIce(worldObj, bx, by - 3, bz);
		worldObj.spawnEntityInWorld(ice);
	}

	/**
	 * Attack the specified entity using a ranged attack.
	 */
	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float par2) {
		if (!this.canRampage) {

			EntityTFIceBomb ice = new EntityTFIceBomb(this.worldObj, this);

			double d0 = target.posX - this.posX;
			double d1 = target.posY + (double) target.getEyeHeight() - 1.100000023841858D - target.posY;
			double d2 = target.posZ - this.posZ;
			float f1 = MathHelper.sqrt_double(d0 * d0 + d2 * d2) * 0.2F;
			ice.setThrowableHeading(d0, d1 + (double) f1, d2, 0.75F, 12.0F);

			this.playSound("random.bow", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
			this.worldObj.spawnEntityInWorld(ice);
		}
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	public boolean canRampage() {
		return this.canRampage;
	}

	/**
	 * Set whether this yeti is currently in rampage mode.
	 */
	public void setRampaging(boolean par1) {
		this.getDataWatcher().updateObject(this.rampageFlag, Byte.valueOf((byte) (par1 ? 1 : 0)));
	}

	/**
	 * Return whether this yeti is currently in rampage mode.
	 */
	public boolean isRampaging() {
		return this.getDataWatcher().getWatchableObjectByte(this.rampageFlag) == 1;
	}

	/**
	 * Set whether this yeti is currently tired.
	 */
	public void setTired(boolean par1) {
		this.getDataWatcher().updateObject(this.tiredFlag, Byte.valueOf((byte) (par1 ? 1 : 0)));
		this.canRampage = false;
	}

	/**
	 * Return whether this yeti is currently tired.
	 */
	public boolean isTired() {
		return this.getDataWatcher().getWatchableObjectByte(this.tiredFlag) == 1;
	}

	/**
	 * Called when the mob is falling. Calculates and applies fall damage.
	 */
	@Override
	protected void fall(float par1) {
		super.fall(par1);

		if (this.isRampaging()) {
			// make jump effects
			this.playSound("random.bow", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));

			// 1.7.10 also sent playAuxSFX(2006, ...) here, an event vanilla 1.7.10 does not define either (no effect).

			// hit everything around
			if (!this.worldObj.isRemote) {
				hitNearbyEntities();
			}
		}
	}

	/** MITE_BALANCE 9.4: rampage landing hit 17 raw within radius 3. */
	private void hitNearbyEntities() {
		ArrayList<?> nearby = new ArrayList<Object>(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(3, 0, 3)));

		for (Object o : nearby) {
			Entity entity = (Entity) o;
			if (entity instanceof EntityLivingBase) {

				boolean hit = TFDamage.hurtByMob(entity, this, 17F);

				if (hit) {
					entity.motionY += 0.4000000059604645D;
				}
			}
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
		if (!this.worldObj.isRemote && this.killResult == null) {
			this.killResult = TFProgress.onBossKilled(this, TFProgress.Flag.YETI, TFAchievementPage.twilightHunter);
		}
		lootChest = twilightforest.TFBossLoot.begin(this);
		super.onDeath(par1DamageSource);
		twilightforest.TFBossLoot.finish(this, lootChest);
		lootChest = null;
		if (!this.worldObj.isRemote && this.killResult != null) {
			// MITE_BALANCE 5.2/9.4: 250 xp for everyone credited, x0.25 on repeat kills
			this.killResult.awardXp(250);
		}

		// mark the lair as defeated
		if (!worldObj.isRemote) {
			int dx = MathHelper.floor_double(this.posX);
			int dy = MathHelper.floor_double(this.posY);
			int dz = MathHelper.floor_double(this.posZ);

			if (worldObj.provider instanceof WorldProviderTwilightForest) {
				ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) worldObj.provider).getChunkProvider();
				TFFeature nearbyFeature = ((TFWorldChunkManager) worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);

				if (nearbyFeature == TFFeature.yetiCave) {
					chunkProvider.setStructureConquered(dx, dy, dz, true);
				}
			}
		}
	}

	// MITE's EntityCreature does not save the home area, so it is stored here as in 1.7.10.
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		ChunkCoordinates home = this.getHomePosition();
		nbttagcompound.setTag("Home", newDoubleNBTList(home.posX, home.posY, home.posZ));
		nbttagcompound.setBoolean("HasHome", this.hasHome());
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("Home")) {
			NBTTagList nbttaglist = nbttagcompound.getTagList("Home");
			if (nbttaglist.tagCount() >= 3) {
				int hx = (int) ((NBTTagDouble) nbttaglist.tagAt(0)).data;
				int hy = (int) ((NBTTagDouble) nbttaglist.tagAt(1)).data;
				int hz = (int) ((NBTTagDouble) nbttaglist.tagAt(2)).data;
				this.setHomeArea(hx, hy, hz, 30);
			}
		}
		if (!nbttagcompound.getBoolean("HasHome")) {
			this.detachHome();
		}
	}

}
