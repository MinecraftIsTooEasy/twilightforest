package twilightforest.entity;

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
import net.minecraft.EntityLivingData;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFRiderSpearAttack;
import twilightforest.item.TFItems;

public class EntityTFGoblinKnightLower extends EntityMob {

	// MITE: DataWatcher id allocated dynamically (1.7.10 used the fixed 17, see MITE_ENTITY 1 #32)
	private int dataEquip;

	public EntityTFGoblinKnightLower(World par1World) {
		super(par1World);
		setSize(0.7F, 1.1F);

		this.tasks.addTask(0, new EntityAITFRiderSpearAttack(this));
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));

		this.setHasArmor(true);
	}

	/**
	 * Set monster attributes (MITE_BALANCE T3: HP 32, melee 13)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(32.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(13.0D); // attack damage
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

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setBoolean("hasArmor", this.hasArmor());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setHasArmor(par1NBTTagCompound.getBoolean("hasArmor"));
	}

	/**
	 * Initialize this creature.
	 */
	public void initCreature() {
		// we start with the upper guy riding us
		EntityTFGoblinKnightUpper upper = new EntityTFGoblinKnightUpper(this.worldObj);
		upper.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
		upper.onSpawnWithEgg((EntityLivingData) null);
		this.worldObj.spawnEntityInWorld(upper);
		upper.mountEntity(this);
	}

	/**
	 * Init creature with mount
	 */
	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData) {
		EntityLivingData data = super.onSpawnWithEgg(par1EntityLivingData);

		// we start with the upper guy riding us
		EntityTFGoblinKnightUpper upper = new EntityTFGoblinKnightUpper(this.worldObj);
		upper.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
		upper.onSpawnWithEgg((EntityLivingData) null);
		this.worldObj.spawnEntityInWorld(upper);
		upper.mountEntity(this);

		return data;
	}

	/**
	 * Returns the Y offset from the entity's position for any entity riding this one.
	 */
	@Override
	public double getMountedYOffset() {
		return 1.0D;
	}

	/**
	 * The rider strikes from higher up, so give the pair a bit more reach than a plain mob (MITE melee range check)
	 */
	@Override
	public float getReach() {
		return super.getReach() + (this.riddenByEntity != null ? 1.0F : 0.0F);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.isEntityAlive()) {
			// synch target with lower goblin
			if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLiving && this.getAttackTarget() == null) {
				this.setAttackTarget(((EntityLiving) this.riddenByEntity).getAttackTarget());
			}
		}

		super.onUpdate();
	}

	/**
	 * Do not attack if we have a rider
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {
		if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLiving) {
			return ((EntityLiving) this.riddenByEntity).attackEntityAsMob(par1Entity);
		} else {
			return super.attackEntityAsMob(par1Entity);
		}
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		DamageSource source = damage.getSource();

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

			// shield?
			EntityTFGoblinKnightUpper upper = null;

			if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityTFGoblinKnightUpper) {
				upper = (EntityTFGoblinKnightUpper) this.riddenByEntity;
			}

			if (upper != null && upper.hasShield() && difference > 150 && difference < 230) {
				if (upper.takeHitOnShield(source, damage.getAmount())) {
					return null;
				}
			}

			// break armor?
			if (this.hasArmor() && (difference > 300 || difference < 60)) {
				breakArmor();
			}
		}

		return super.attackEntityFrom(damage);
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

	/**
	 * Flat natural defense instead of the 1.7.10 armor value (+17 capped at 20); MITE_BALANCE T3: defense 4 with
	 * the breastplate, 1 once it is broken.
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		float defense = super.getNaturalDefense(damage_source);

		if (!damage_source.bypassesMundaneArmor()) {
			defense += this.hasArmor() ? 4.0F : 1.0F;
		}

		return defense;
	}

	@Override
	public int getExperienceValue() {
		return 14;
	}

	/**
	 * Returns the item ID for the item the mob drops on death (armor shard, 0-2 per MITE_BALANCE).
	 */
	@Override
	protected int getDropItemId() {
		return TFItems.armorShard.itemID;
	}

}
