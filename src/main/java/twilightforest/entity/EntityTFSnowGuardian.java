package twilightforest.entity;

import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityLivingData;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.item.TFItems;

public class EntityTFSnowGuardian extends EntityMob {

	public EntityTFSnowGuardian(World par1World) {
		super(par1World);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.setSize(0.6F, 1.8F);

		// balance (MITE_BALANCE §8.7): TF mobs never drop their equipment
		for (int i = 0; i < this.equipmentDropChances.length; i++) {
			this.equipmentDropChances[i] = 0.0F;
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(3.0D);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(10.0D);
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected String getLivingSound() {
		return TwilightForestMod.ID + ":mob.ice.noise";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return TwilightForestMod.ID + ":mob.ice.hurt";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return TwilightForestMod.ID + ":mob.ice.death";
	}

	/**
	 * Gets the pitch of living sounds in living entities.
	 */
	@Override
	protected float getSoundPitch(String sound) {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.8F;
	}

	/**
	 * Makes entity wear random armor based on difficulty
	 */
	protected void addRandomArmor() {
		// always random armor

		// random armor type
		int type = rand.nextInt(4);

		this.setCurrentItemOrArmor(0, new ItemStack(this.makeItemForSlot(0, type)));

		this.setCurrentItemOrArmor(3, new ItemStack(this.makeItemForSlot(3, type)));
		this.setCurrentItemOrArmor(4, new ItemStack(this.makeItemForSlot(4, type)));
	}

	protected Item makeItemForSlot(int slot, int type) {
		switch (slot) {
		case 0: // sword
		default:
			switch (type) {
			case 0:
			default:
				return TFItems.ironwoodSword;
			case 1:
				return TFItems.steeleafSword;
			case 2:
				return TFItems.knightlySword;
			case 3:
				return TFItems.knightlySword;
			}
		case 1: // boots
			switch (type) {
			case 0:
			default:
				return TFItems.ironwoodBoots;
			case 1:
				return TFItems.steeleafBoots;
			case 2:
				return TFItems.knightlyBoots;
			case 3:
				return TFItems.arcticBoots;
			}
		case 2: // legs
			switch (type) {
			case 0:
			default:
				return TFItems.ironwoodLegs;
			case 1:
				return TFItems.steeleafLegs;
			case 2:
				return TFItems.knightlyLegs;
			case 3:
				return TFItems.arcticLegs;
			}
		case 3: // chest
			switch (type) {
			case 0:
			default:
				return TFItems.ironwoodPlate;
			case 1:
				return TFItems.steeleafPlate;
			case 2:
				return TFItems.knightlyPlate;
			case 3:
				return TFItems.arcticPlate;
			}
		case 4: // helm
			switch (type) {
			case 0:
			default:
				return TFItems.ironwoodHelm;
			case 1:
				return TFItems.steeleafHelm;
			case 2:
				return TFItems.knightlyHelm;
			case 3:
				return TFItems.arcticHelm;
			}
		}
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId() {
		return Item.snowball.itemID;
	}

	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData) {
		EntityLivingData data = super.onSpawnWithEgg(par1EntityLivingData);

		this.addRandomArmor();
		// this.enchantEquipment(...) was disabled in TF as well

		return data;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// make snow particles
		for (int i = 0; i < 3; i++) {
			float px = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;
			float py = this.getEyeHeight() + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.5F;
			float pz = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F;

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowguardian", this.lastTickPosX + px, this.lastTickPosY + py, this.lastTickPosZ + pz, 0, 0, 0);
		}
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}
}
