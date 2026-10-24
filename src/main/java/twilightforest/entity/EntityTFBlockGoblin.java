package twilightforest.entity;

import java.util.List;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIAvoidEntity;
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
import net.minecraft.EntityTNTPrimed;
import net.minecraft.IEntityMultiPart;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.item.TFItems;

public class EntityTFBlockGoblin extends EntityMob implements IEntityMultiPart {

	private static final float CHAIN_SPEED = 16F;

	// MITE: DataWatcher ids are allocated dynamically (1.7.10 used the fixed 17/18, see MITE_ENTITY 1 #32)
	private int dataChainLength;
	private int dataChainPos;

	int recoilCounter;
	float chainAngle;

	public EntityTFSpikeBlock block;
	public EntityTFGoblinChain chain1;
	public EntityTFGoblinChain chain2;
	public EntityTFGoblinChain chain3;

	public Entity[] partsArray;

	public EntityTFBlockGoblin(World world) {
		super(world);
		setSize(0.9F, 1.4F);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityTNTPrimed.class, 2.0F, 0.8D, 1.5D));
		this.tasks.addTask(5, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		this.recoilCounter = 0;

		// the parts must be created right after the parent so their entity ids are consecutive (MITE_ENTITY 14)
		this.partsArray = new Entity[] {
				block = new EntityTFSpikeBlock(this),
				chain1 = new EntityTFGoblinChain(this),
				chain2 = new EntityTFGoblinChain(this),
				chain3 = new EntityTFGoblinChain(this)
		};
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataChainLength = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.dataChainLength, Byte.valueOf((byte) 0));
		this.dataChainPos = this.dataWatcher.getNextAvailableId();
		this.dataWatcher.addObject(this.dataChainPos, Byte.valueOf((byte) 0));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE T3: HP 38, melee 14)
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(38.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(14.0D); // attack damage
	}

	@Override
	protected String getLivingSound() {
		return TwilightForestMod.ID + ":mob.redcap.redcap";
	}

	@Override
	protected String getHurtSound() {
		return TwilightForestMod.ID + ":mob.redcap.hurt";
	}

	@Override
	protected String getDeathSound() {
		return TwilightForestMod.ID + ":mob.redcap.die";
	}

	/**
	 * Returns the item ID for the item the mob drops on death (armor shard, 0-2 per MITE_BALANCE).
	 */
	@Override
	protected int getDropItemId() {
		return TFItems.armorShard.itemID;
	}

	@Override
	public int getExperienceValue() {
		return 16;
	}

	/**
	 * Flat natural defense instead of the 1.7.10 armor value (+11 capped at 20); MITE_BALANCE T3: defense 4
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return super.getNaturalDefense(damage_source) + (damage_source.bypassesMundaneArmor() ? 0.0F : 4.0F);
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
	 * How high is the chain
	 */
	public double getChainYOffset() {
		return 1.5 - this.getChainLength() / 4.0;
	}

	/**
	 * Get the block & chain position
	 */
	public Vec3 getChainPosition() {
		return this.getChainPosition(getChainAngle(), getChainLength());
	}

	/**
	 * Get the block & chain position
	 */
	public Vec3 getChainPosition(float angle, float distance) {
		double var1 = Math.cos((angle) * Math.PI / 180.0D) * distance;
		double var3 = Math.sin((angle) * Math.PI / 180.0D) * distance;

		return Vec3.createVectorHelper(this.posX + var1, this.posY + this.getChainYOffset(), this.posZ + var3);
	}

	public boolean isSwingingChain() {
		return this.isSwingInProgress || (this.getAttackTarget() != null && this.recoilCounter == 0);
	}

	/**
	 * Do not deal damage by colliding with the goblin
	 */
	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {
		this.swingArm();
		return null;
	}

	/**
	 * Keep block position updated
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();
		block.onUpdate();
		chain1.onUpdate();
		chain2.onUpdate();
		chain3.onUpdate();

		if (recoilCounter > 0) {
			recoilCounter--;
		}

		chainAngle += CHAIN_SPEED;
		chainAngle %= 360;

		if (!this.worldObj.isRemote) {
			this.dataWatcher.updateObject(dataChainLength, Byte.valueOf((byte) Math.floor(getChainLength() * 127F)));
			this.dataWatcher.updateObject(dataChainPos, Byte.valueOf((byte) Math.floor(getChainAngle() / 360F * 255F)));
		} else {
			// synch chain pos if it's wrong
			if (Math.abs(this.chainAngle - this.getChainAngle()) > CHAIN_SPEED * 2) {
				this.chainAngle = getChainAngle();
			}
		}

		// set block position
		Vec3 blockPos = this.getChainPosition();
		this.block.setPosition(blockPos.xCoord, blockPos.yCoord, blockPos.zCoord);
		this.block.rotationYaw = getChainAngle();

		// interpolate chain position
		double sx = this.posX;
		double sy = this.posY + this.height - 0.1;
		double sz = this.posZ;

		double ox = sx - blockPos.xCoord;
		double oy = sy - blockPos.yCoord - (block.height / 3D);
		double oz = sz - blockPos.zCoord;

		this.chain1.setPosition(sx - ox * 0.4, sy - oy * 0.4, sz - oz * 0.4);
		this.chain2.setPosition(sx - ox * 0.5, sy - oy * 0.5, sz - oz * 0.5);
		this.chain3.setPosition(sx - ox * 0.6, sy - oy * 0.6, sz - oz * 0.6);

		// collide things with the block
		if (!worldObj.isRemote && this.isSwingingChain()) {
			this.applyBlockCollisions(this.block);
		}
	}

	/**
	 * Check if the block is colliding with any nearby entities
	 */
	@SuppressWarnings("unchecked")
	protected void applyBlockCollisions(Entity collider) {
		List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(collider, collider.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

		if (list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); ++i) {
				Entity entity = list.get(i);

				if (entity.canBePushed()) {
					applyBlockCollision(collider, entity);
				}
			}
		}
	}

	/**
	 * Do the effect where the block hits something
	 */
	protected void applyBlockCollision(Entity collider, Entity collided) {
		if (collided != this) {
			collided.applyEntityCollision(collider);
			if (collided instanceof EntityLivingBase) {
				// do hit and throw
				EntityDamageResult result = super.attackEntityAsMob(collided);
				boolean attackSuccess = result != null && result.entityWasNegativelyAffected();

				if (attackSuccess) {
					collided.motionY += 0.4000000059604645D;
					this.playSound("mob.irongolem.throw", 1.0F, 1.0F);

					this.recoilCounter = 40;
				}
			}
		}
	}

	/**
	 * Angle between 0 and 360 to place the chain at
	 */
	public float getChainAngle() {
		if (!this.worldObj.isRemote) {
			return this.chainAngle;
		} else {
			return (this.dataWatcher.getWatchableObjectByte(dataChainPos) & 0xFF) / 255F * 360F;
		}
	}

	/**
	 * Between 0.0F and 2.0F, how long is the chain right now?
	 */
	public float getChainLength() {
		if (!this.worldObj.isRemote) {
			if (isSwingingChain()) {
				return 0.9F;
			} else {
				return 0.3F;
			}
		} else {
			return (this.dataWatcher.getWatchableObjectByte(dataChainLength) & 0xFF) / 127F;
		}
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
		return partsArray;
	}

}
