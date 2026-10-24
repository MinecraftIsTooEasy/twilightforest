package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.entity.ai.EntityAITFThrowRider;
import twilightforest.item.TFItems;

public class EntityTFYeti extends EntityMob {

	/** DataWatcher slot, allocated dynamically (MITE subclasses occupy fixed low ids). */
	private int angerFlag;

	public EntityTFYeti(World par1World) {
		super(par1World);
		this.setSize(1.4F, 2.4F);

		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(1, new EntityAITFThrowRider(this, 1.0F));
		this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0F));
		this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(4, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3 T4: HP 44, push attack 12, defense 0, XP 15)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(44.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.38D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(12.0D);
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(4.0D);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.angerFlag = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.angerFlag, Byte.valueOf((byte) 0));
	}

	@Override
	public int getExperienceValue() {
		return 15;
	}

	/** MITE: the yeti is a big mob, the default 1.5 block reach would let players stand "inside" it unharmed. */
	@Override
	public float getReach() {
		return this.width * 0.75F + 1.5F;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		if (this.riddenByEntity != null) {
			// stop player sneaking so that they can't dismount!
			if (this.riddenByEntity.isSneaking()) {
				this.riddenByEntity.setSneaking(false);
			}
		}

		super.onLivingUpdate();

		// look at things in our jaws
		if (this.riddenByEntity != null) {
			this.getLookHelper().setLookPositionWithEntity(riddenByEntity, 100F, 100F);

			// TODO(mite-port): 1.7.10 pushed the yeti out of blocks the rider is stuck in (func_145771_j with the rider
			// position); MITE's EntityLiving.onUpdate already calls pushOutOfBlocks() for the yeti itself.
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
		if (damage.getSource().getImmediateEntity() != null) {
			// become angry
			this.setAngry(true);
		}

		return super.attackEntityFrom(damage);
	}

	/**
	 * Determines whether this yeti is angry or not.
	 */
	public boolean isAngry() {
		return (this.dataWatcher.getWatchableObjectByte(this.angerFlag) & 2) != 0;
	}

	/**
	 * Sets whether this yeti is angry or not.
	 */
	public void setAngry(boolean anger) {
		byte b0 = this.dataWatcher.getWatchableObjectByte(this.angerFlag);

		if (anger) {
			this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(40.0D);
			this.dataWatcher.updateObject(this.angerFlag, Byte.valueOf((byte) (b0 | 2)));
		} else {
			this.dataWatcher.updateObject(this.angerFlag, Byte.valueOf((byte) (b0 & -3)));
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setBoolean("Angry", this.isAngry());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setAngry(par1NBTTagCompound.getBoolean("Angry"));
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
		return 2.25D;
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

	/** MITE_BALANCE 9.3: natural defense 0. */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return 0.0F;
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			((EntityPlayer) par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
		}
	}

	/**
	 * We're allowed to spawn in bright light only in snow
	 */
	@Override
	public boolean getCanSpawnHere(boolean perform_light_check) {
		// are we in the snow
		if (worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX), MathHelper.floor_double(posZ)) == TFBiomeBase.tfSnow) {
			// don't check light level
			return super.getCanSpawnHere(false);
		} else {
			// normal EntityMob spawn check, checks light level
			return super.getCanSpawnHere(perform_light_check);
		}
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	@Override
	protected boolean isValidLightLevel() {
		int x = MathHelper.floor_double(this.posX);
		int z = MathHelper.floor_double(this.posZ);

		if (worldObj.getBiomeGenForCoords(x, z) == TFBiomeBase.tfSnow) {
			return true;
		} else {
			return super.isValidLightLevel();
		}
	}

	/** MITE_BALANCE 9.3: arctic fur 0-2 (default EntityLiving.dropFewItems rolls 0-2 plus looting). */
	@Override
	protected int getDropItemId() {
		return TFItems.arcticFur.itemID;
	}

}
