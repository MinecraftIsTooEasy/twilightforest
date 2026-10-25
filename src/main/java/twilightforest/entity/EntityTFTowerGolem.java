package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumEntityState;
import net.minecraft.IMob;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFParticles;

public class EntityTFTowerGolem extends EntityMob {

	private int attackTimer;

	public EntityTFTowerGolem(World par1World) {
		super(par1World);

		this.setSize(1.4F, 2.9F);

		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// MITE_BALANCE 9.3: Tower Golem HP 70, damage 15
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(70.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(15.0D);
	}

	/** MITE_BALANCE 9.3: natural defense 3 (replaces the 1.7.10 +2 armor points). */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		return damage_source.bypassesMundaneArmor() ? 0.0F : 3.0F;
	}

	/** MITE_BALANCE 9.3: 18 XP. */
	@Override
	public int getExperienceValue() {
		return 18;
	}

	/** Big body: MITE melee reach is measured from the centre, so widen it by half the width. */
	@Override
	public float getReach() {
		return super.getReach() + this.width * 0.5F;
	}

	@Override
	public EntityDamageResult attackEntityAsMob(Entity par1Entity) {
		this.attackTimer = 10;
		this.worldObj.setEntityState(this, EnumEntityState.golem_throw);
		EntityDamageResult result = super.attackEntityAsMob(par1Entity);

		if (result != null && result.entityWasNegativelyAffected()) {
			par1Entity.motionY += 0.4000000059604645D;
		}

		this.playSound("mob.irongolem.throw", 1.0F, 1.0F);
		return result;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound() {
		return null;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return "mob.irongolem.hit";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return "mob.irongolem.death";
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	@Override
	protected void playStepSound(int x, int y, int z, int block_id) {
		this.playSound("mob.irongolem.walk", 1.0F, 1.0F);
	}

	@Override
	protected void collideWithEntity(Entity par1Entity) {
		if (par1Entity instanceof IMob && this.getRNG().nextInt(10) == 0) {
			this.setAttackTarget((EntityLivingBase) par1Entity);
		}

		super.collideWithEntity(par1Entity);
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (this.attackTimer > 0) {
			--this.attackTimer;
		}

		if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7D && this.rand.nextInt(5) == 0) {
			int var1 = MathHelper.floor_double(this.posX);
			int var2 = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double) this.yOffset);
			int var3 = MathHelper.floor_double(this.posZ);
			int blockId = this.worldObj.getBlockId(var1, var2, var3);

			if (blockId > 0) {
				TFParticles.spawnBlockCrack(this.worldObj, blockId, this.worldObj.getBlockMetadata(var1, var2, var3), this.posX + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, this.boundingBox.minY + 0.1D, this.posZ + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width, 4.0D * ((double) this.rand.nextFloat() - 0.5D), 0.5D, ((double) this.rand.nextFloat() - 0.5D) * 4.0D);
			}
		}

		// redstone sparkles?
		if (this.rand.nextBoolean()) {
			TFParticles.spawn(this.worldObj, "reddust", this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0, 0, 0);
		}
	}

	@Override
	public void handleHealthUpdate(EnumEntityState state) {
		if (state == EnumEntityState.golem_throw) {
			this.attackTimer = 10;
			this.playSound("mob.irongolem.throw", 1.0F, 1.0F);
		} else {
			super.handleHealthUpdate(state);
		}
	}

	public int getAttackTimer() {
		return this.attackTimer;
	}

	/**
	 * MITE_BALANCE 9.3: iron NUGGETS 0-2 (1.7.10: iron ingots, no iron farm), tower wood 0-2.
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		// iron: half the time 3-8 nuggets, otherwise one ingot
		if (this.rand.nextBoolean()) {
			int nuggets = 3 + this.rand.nextInt(6);

			for (int i = 0; i < nuggets; ++i) {
				this.dropItem(Item.ironNugget.itemID, 1);
			}
		} else {
			this.dropItem(Item.ingotIron.itemID, 1);
		}

		int var4 = this.rand.nextInt(3);

		for (int i = 0; i < var4; ++i) {
			this.dropItem(TFBlocks.towerWood.blockID, 1);
		}
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk() {
		return 16;
	}

}
