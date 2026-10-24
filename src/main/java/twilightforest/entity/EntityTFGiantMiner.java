package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFProgress;
import twilightforest.entity.ai.EntityAITFGiantAttackOnCollide;
import twilightforest.item.TFItems;

public class EntityTFGiantMiner extends EntityMob {

	/** MITE_BALANCE 9.3 T5: the giant tool drops 25% of the time (was always). */
	protected static final float TOOL_DROP_CHANCE = 0.25F;

	public EntityTFGiantMiner(World par1World) {
		super(par1World);
		this.setSize(this.width * 4.0F, this.height * 4.0F);

		this.tasks.addTask(1, new EntityAISwimming(this));

		this.tasks.addTask(4, new EntityAITFGiantAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		// MITE has no stone pickaxe, the copper one is the closest tier
		this.setCurrentItemOrArmor(0, new ItemStack(Item.pickaxeCopper));

		for (int i = 0; i < this.equipmentDropChances.length; ++i) {
			this.equipmentDropChances[i] = 0F;
		}
	}

	/** MITE_BALANCE 9.3 T5: Giant Miner HP 140. */
	protected double getBaseMaxHealth() {
		return 140.0D;
	}

	/** MITE_BALANCE 9.3 T5: Giant Miner melee 19. */
	protected double getBaseAttackDamage() {
		return 19.0D;
	}

	/** MITE_BALANCE 9.3 T5: Giant Miner defense 3. */
	protected float getBaseDefense() {
		return 3.0F;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(this.getBaseMaxHealth());
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(this.getBaseAttackDamage());
		this.getEntityAttribute(SharedMonsterAttributes.followRange).setAttribute(40.0D);
	}

	/**
	 * Natural defense; whatever the worn armour already contributes is subtracted so the total
	 * matches the MITE_BALANCE figure.
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source) {
		if (damage_source.bypassesMundaneArmor()) {
			return 0.0F;
		}
		return Math.max(0.0F, this.getBaseDefense() - this.getProtectionFromArmor(damage_source, false));
	}

	/** MITE_BALANCE 9.3 T5: Giant Miner 30 XP. */
	@Override
	public int getExperienceValue() {
		return 30;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected int getDropItemId() {
		return TFItems.giantPick.itemID;
	}

	/**
	 * Drops the giant tool with a 25% chance when killed by a player.
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		int itemId = this.getDropItemId();
		if (itemId > 0 && recently_hit_by_player && this.rand.nextFloat() < TOOL_DROP_CHANCE) {
			this.dropItem(itemId, 1);
		}
	}

	/** MITE_BALANCE 5.3: the giants stay dormant until a player of level 32 is near (cloud castle and troll vault spawn them at generation). */
	public static final int MIN_LEVEL = 32;
	private boolean gateOpen;

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

	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		if (!this.worldObj.isRemote && isLevelGated()) {
			return null;
		}
		return super.attackEntityFrom(damage);
	}

	@Override
	public void onLivingUpdate() {
		if (!this.worldObj.isRemote && this.ticksExisted % 20 == 0 && isLevelGated()) {
			TFProgress.hintGated(this, MIN_LEVEL);
			if (this.getAttackTarget() != null) {
				this.setAttackTarget(null);
			}
		}
		super.onLivingUpdate();
	}

	public void makeNonDespawning() {
		this.func_110163_bv();
	}

}
