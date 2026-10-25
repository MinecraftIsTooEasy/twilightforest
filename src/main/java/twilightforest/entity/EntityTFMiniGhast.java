package twilightforest.entity;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumSkyBlock;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;

public class EntityTFMiniGhast extends EntityTFTowerGhast {

	public boolean isMinion = false;

	public EntityTFMiniGhast(World par1World) {
		super(par1World);
		this.setSize(1.1F, 1.5F);

		this.aggroRange = 16.0F;
		this.stareRange = 8.0F;

		this.wanderFactor = 4.0F;
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk() {
		return 16;
	}

	/**
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// MITE_BALANCE 9.3: Mini Ghast HP 12 (boss minions keep the 1.7.10 value 6, see makeBossMinion)
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(12.0D);
	}

	/** MITE_BALANCE 9.3: 6 XP; boss minions give none. */
	@Override
	public int getExperienceValue() {
		return this.isMinion ? 0 : 6;
	}

	/** MITE_BALANCE 9.3: mini ghast fireball explosion sizeVsLiving 0.55. */
	@Override
	protected float getFireballSizeVsLiving() {
		return 0.55F;
	}

	/** Boss minions never despawn (MITE_BALANCE 9.3). */
	@Override
	protected boolean canDespawn() {
		return !this.isMinion && super.canDespawn();
	}

	@Override
	public boolean fleesWhenBadlyWounded() {
		return false;
	}

	/**
	 * Checks to see if this mini ghast should be attacking this player.
	 *
	 * Very similar to endermen code, but we also attack at a certain range (MITE: the whole stare range)
	 */
	@Override
	protected boolean shouldAttackPlayer(EntityPlayer par1EntityPlayer) {
		ItemStack playerHeadArmor = par1EntityPlayer.inventory.armorInventory[3];

		if (playerHeadArmor != null && playerHeadArmor.itemID == Block.pumpkin.blockID) {
			return false;
		} else if (par1EntityPlayer.getDistanceToEntity(this) <= this.stareRange && par1EntityPlayer.canSeeEntity(this)) {
			// MITE: any player it can see within its stare range (8) angers it after the one second moan, not only at 3.5
			// blocks; farther away (up to 16) staring at it still does
			return true;
		} else {
			Vec3 var3 = par1EntityPlayer.getLook(1.0F).normalize();
			Vec3 var4 = Vec3.createVectorHelper(this.posX - par1EntityPlayer.posX, this.boundingBox.minY + (double) (this.height / 2.0F) - (par1EntityPlayer.posY + (double) par1EntityPlayer.getEyeHeight()), this.posZ - par1EntityPlayer.posZ);
			double var5 = var4.lengthVector();
			var4 = var4.normalize();
			double var7 = var3.dotProduct(var4);
			return var7 > 1.0D - 0.025D / var5 ? par1EntityPlayer.canSeeEntity(this) : false;
		}
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	@Override
	protected boolean isValidLightLevel() {
		if (isMinion) {
			return true;
		}

		// MITE: spawner mobs skip the light check (only sunlight that would set them on fire stops them)
		if (this.came_from_spawner) {
			return EntityMob.isValidLightLevel(this);
		}

		int myX = MathHelper.floor_double(this.posX);
		int myY = MathHelper.floor_double(this.boundingBox.minY);
		int myZ = MathHelper.floor_double(this.posZ);

		if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, myX, myY, myZ) > this.rand.nextInt(32)) {
			return false;
		} else {
			int lightLevel = this.worldObj.getBlockLightValue(myX, myY, myZ);

			if (this.worldObj.isThundering(false)) {
				int var5 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				lightLevel = this.worldObj.getBlockLightValue(myX, myY, myZ);
				this.worldObj.skylightSubtracted = var5;
			}

			return lightLevel <= this.rand.nextInt(8);
		}
	}

	/**
	 * Turn this mini ghast into a boss minion
	 */
	public void makeBossMinion() {
		this.wanderFactor = 0.005F;
		this.isMinion = true;

		// the attribute is set in the (super) constructor, before isMinion exists, so adjust it here
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(6.0D);
		this.setHealth(this.getMaxHealth());
	}

	/** No loot from minions; others drop a little gunpowder and rarely a ghast tear. */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
		if (!this.isMinion) {
			int num = this.rand.nextInt(2);
			for (int i = 0; i < num; i++) {
				this.dropItem(Item.gunpowder.itemID, 1);
			}

			if (this.rand.nextInt(10) == 0) {
				this.dropItem(Item.ghastTear.itemID, 1);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("isMinion", this.isMinion);
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		if (nbttagcompound.getBoolean("isMinion")) {
			makeBossMinion();
		}
	}

}
