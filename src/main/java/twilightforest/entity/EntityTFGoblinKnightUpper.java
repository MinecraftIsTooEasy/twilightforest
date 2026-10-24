package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
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
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumEntityState;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFHeavySpearAttack;
import twilightforest.item.TFItems;

public class EntityTFGoblinKnightUpper extends EntityMob {

	private static final int SHIELD_DAMAGE_THRESHOLD = 10;

	/** The heavy spear hit does this multiple of the normal melee damage (1.7.10 had 20 vs 8 in a dead code path). */
	private static final double HEAVY_SPEAR_DAMAGE_FACTOR = 1.5D;

	// MITE: DataWatcher id allocated dynamically (1.7.10 used the fixed 17, see MITE_ENTITY 1 #32)
	private int dataEquip;

	public int shieldHits;

	public int heavySpearTimer;

	public EntityTFGoblinKnightUpper(World par1World) {
		super(par1World);
		setSize(1.1F, 1.3F);

		this.tasks.addTask(0, new EntityAITFHeavySpearAttack(this));
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));

		this.setHasArmor(true);
		this.setHasShield(true);

		this.shieldHits = 0;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE T3: HP 44, melee 15)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(44.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(15.0D); // attack damage
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataEquip = dataWatcher.getNextAvailableId();
		dataWatcher.addObject(this.dataEquip, Byte.valueOf((byte) 0));
	}

	public boolean hasArmor() {
		return (dataWatcher.getWatchableObjectByte(dataEquip) & 1) > 0;
	}

	public void setHasArmor(boolean flag) {
		byte otherFlags = dataWatcher.getWatchableObjectByte(dataEquip);
		otherFlags &= 126;

		if (flag) {
			dataWatcher.updateObject(dataEquip, Byte.valueOf((byte) (otherFlags | 1)));
		} else {
			dataWatcher.updateObject(dataEquip, Byte.valueOf((byte) otherFlags));
		}
	}

	public boolean hasShield() {
		return (dataWatcher.getWatchableObjectByte(dataEquip) & 2) > 0;
	}

	public void setHasShield(boolean flag) {
		byte otherFlags = dataWatcher.getWatchableObjectByte(dataEquip);
		otherFlags &= 125;

		if (flag) {
			dataWatcher.updateObject(dataEquip, Byte.valueOf((byte) (otherFlags | 2)));
		} else {
			dataWatcher.updateObject(dataEquip, Byte.valueOf((byte) otherFlags));
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setBoolean("hasArmor", this.hasArmor());
		par1NBTTagCompound.setBoolean("hasShield", this.hasShield());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setHasArmor(par1NBTTagCompound.getBoolean("hasArmor"));
		this.setHasShield(par1NBTTagCompound.getBoolean("hasShield"));
	}

	/**
	 * The rider strikes from higher up, so give it a bit more reach than a plain mob (MITE melee range check)
	 */
	@Override
	public float getReach() {
		return super.getReach() + (this.ridingEntity != null ? 1.0F : 0.0F);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.isEntityAlive()) {
			// synch target with lower goblin
			if (this.ridingEntity != null && this.ridingEntity instanceof EntityLiving && this.getAttackTarget() == null) {
				this.setAttackTarget(((EntityLiving) this.ridingEntity).getAttackTarget());
			}

			// keep doing attack timer
			if (this.heavySpearTimer > 0) {
				--this.heavySpearTimer;

				if (this.heavySpearTimer == 25) {
					this.landHeavySpearAttack();
				}
			}

			// break shield if we're on the ground
			if (this.ridingEntity == null && this.hasShield()) {
				this.breakShield();
			}
		}

		super.onUpdate();
	}

	@SuppressWarnings("unchecked")
	private void landHeavySpearAttack() {
		// find vector in front of us
		Vec3 vector = this.getLookVec();

		double dist = 1.25;
		double px = this.posX + vector.xCoord * dist;
		double py = this.boundingBox.minY - 0.75;
		double pz = this.posZ + vector.zCoord * dist;

		for (int i = 0; i < 50; i++) {
			TFParticles.spawn(worldObj, "largesmoke", px, py, pz, (rand.nextFloat() - rand.nextFloat()) * 0.25F, 0, (rand.nextFloat() - rand.nextFloat()) * 0.25F);
		}

		// MITE: damage can only be dealt on the server (the client call only draws the dust)
		if (this.worldObj.isRemote || this.ridingEntity == null) {
			return;
		}

		// damage things in front that aren't us or our "mount"
		double radius = 1.5D;

		AxisAlignedBB spearBB = AxisAlignedBB.getBoundingBox(px - radius, py - radius, pz - radius, px + radius, py + radius, pz + radius);

		List<Entity> inBox = worldObj.getEntitiesWithinAABB(Entity.class, spearBB);

		double baseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(baseDamage * HEAVY_SPEAR_DAMAGE_FACTOR);
		try {
			for (Entity entity : inBox) {
				// only living things can be speared (EntityMob melee expects a living target in MITE)
				if (entity instanceof EntityLivingBase && entity != this.ridingEntity && entity != this) {
					super.attackEntityAsMob(entity);
				}
			}
		} finally {
			this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(baseDamage);
		}
	}

	/**
	 * Handles updating while being ridden by an entity
	 */
	@Override
	public void updateRidden() {
		super.updateRidden();
		if (this.ridingEntity != null) {
			this.renderYawOffset = ((EntityLiving) this.ridingEntity).renderYawOffset;
		}
	}

	/**
	 * MITE: entity states are a closed enum, so the heavy spear wind-up (1.7.10 state byte 4) travels as golem_throw.
	 */
	@Override
	public void handleHealthUpdate(EnumEntityState par1) {
		if (par1 == EnumEntityState.golem_throw) {
			this.heavySpearTimer = 60;
		} else {
			super.handleHealthUpdate(par1);
		}
	}

	/**
	 * Swing arm when attacking
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {
		if (this.heavySpearTimer > 0) {
			return null;
		}

		if (rand.nextInt(2) == 0) {
			// do mega spear attack
			this.startHeavySpearAttack();

			return null;
		}

		// normal attack
		this.swingArm();

		return super.attackEntityAsMob(par1Entity);
	}

	private void startHeavySpearAttack() {
		this.heavySpearTimer = 60;
		this.worldObj.setEntityState(this, EnumEntityState.golem_throw);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		DamageSource source = damage.getSource();

		// don't take suffocation damage while riding
		if (source == DamageSource.inWall && this.ridingEntity != null) {
			return null;
		}

		// check the angle of attack, if applicable
		Entity attacker = null;
		if (source.getImmediateEntity() != null) {
			attacker = source.getImmediateEntity();
		}

		if (source.getResponsibleEntity() != null) {
			attacker = source.getResponsibleEntity();
		}

		if (attacker != null) {
			// determine angle
			double dx = this.posX - attacker.posX;
			double dz = this.posZ - attacker.posZ;
			float angle = (float) ((Math.atan2(dz, dx) * 180D) / Math.PI) - 90F;

			float difference = MathHelper.abs((this.renderYawOffset - angle) % 360);

			// if we have a shield, the shield will take the damage instead
			if (this.hasShield() && difference > 150 && difference < 230) {
				if (takeHitOnShield(source, damage.getAmount())) {
					return null;
				}
			} else {
				// unblocked hits may eventually break shield
				if (this.hasShield() && rand.nextBoolean()) {
					damageShield();
				}
			}

			// break armor?
			if (this.hasArmor() && (difference > 300 || difference < 60)) {
				breakArmor();
			}
		}

		EntityDamageResult result = super.attackEntityFrom(damage);

		boolean attackSuccess = result != null && result.entityWasNegativelyAffected();
		if (attackSuccess && this.ridingEntity != null && this.ridingEntity instanceof EntityLiving && attacker != null) {
			((EntityLiving) this.ridingEntity).knockBack(attacker, damage.getAmount(), 0.1, 0.1);
		}

		return result;
	}

	/**
	 * Break our armor
	 */
	public void breakArmor() {
		this.renderBrokenItemStack(Item.plateIron.itemID);
		this.renderBrokenItemStack(Item.plateIron.itemID);
		this.renderBrokenItemStack(Item.plateIron.itemID);

		this.setHasArmor(false);
	}

	/**
	 * Break our shield
	 */
	public void breakShield() {
		this.renderBrokenItemStack(Item.plateIron.itemID);
		this.renderBrokenItemStack(Item.plateIron.itemID);
		this.renderBrokenItemStack(Item.plateIron.itemID);

		this.setHasShield(false);
	}

	/**
	 * 1.7.10 EntityLivingBase.renderBrokenItemStack, which MITE does not have: item break particles in front of the face
	 */
	private void renderBrokenItemStack(int itemId) {
		for (int i = 0; i < 5; ++i) {
			double yawRad = -this.rotationYaw * Math.PI / 180.0D;
			double fx = (this.rand.nextFloat() - 0.5D) * 0.3D;
			double fz = 0.6D;
			double x = this.posX + fx * Math.cos(yawRad) + fz * Math.sin(yawRad);
			double z = this.posZ + fz * Math.cos(yawRad) - fx * Math.sin(yawRad);
			double y = this.posY + this.getEyeHeight() - this.rand.nextFloat() * 0.6D - 0.3D;
			TFParticles.spawnItemCrack(this.worldObj, itemId, 0, x, y, z,
					(this.rand.nextFloat() - 0.5D) * 0.1D, this.rand.nextDouble() * 0.1D + 0.15D, 0.0D);
		}
	}

	public boolean takeHitOnShield(DamageSource par1DamageSource, float damageAmount) {
		if (damageAmount > SHIELD_DAMAGE_THRESHOLD && !this.worldObj.isRemote) {
			damageShield();
		} else {
			this.worldObj.playSoundAtEntity(this, "random.break", 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		}

		// knock back slightly
		EntityLiving toKnockback = (this.ridingEntity != null && this.ridingEntity instanceof EntityLiving) ? (EntityLiving) this.ridingEntity : this;

		Entity responsible = par1DamageSource.getResponsibleEntity();
		if (responsible != null) {
			double d0 = responsible.posX - this.posX;
			double d1;

			for (d1 = responsible.posZ - this.posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
				d0 = (Math.random() - Math.random()) * 0.01D;
			}

			toKnockback.knockBack(responsible, 0, d0 / 4D, d1 / 4D);

			// also set revenge target
			if (responsible instanceof EntityLivingBase) {
				this.setRevengeTarget((EntityLivingBase) responsible);
			}
		}

		return true;
	}

	private void damageShield() {
		this.worldObj.playSoundAtEntity(this, "mob.zombie.metal", 0.25F, 0.25F);

		this.shieldHits++;

		if (!worldObj.isRemote && this.shieldHits >= 3) {
			this.breakShield();
		}
	}

	/**
	 * Flat natural defense instead of the 1.7.10 armor value (+20 capped at 20); MITE_BALANCE T3: defense 5 with the
	 * armor, 2 once it is broken.
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		float defense = super.getNaturalDefense(damage_source);

		if (!damage_source.bypassesMundaneArmor()) {
			defense += this.hasArmor() ? 5.0F : 2.0F;
		}

		return defense;
	}

	@Override
	public int getExperienceValue() {
		return 18;
	}

	/**
	 * Returns the item ID for the item the mob drops on death (armor shard, 0-2 per MITE_BALANCE).
	 */
	@Override
	protected int getDropItemId() {
		return TFItems.armorShard.itemID;
	}

}
