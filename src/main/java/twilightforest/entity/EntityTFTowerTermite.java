package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDamageSource;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumCreatureAttribute;
import net.minecraft.Facing;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.block.BlockTFTowerWood;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFWorldUtil;
import twilightforest.item.TFItems;

public class EntityTFTowerTermite extends EntityMob {

	/** Max termites alive around one termite before popping more infested blocks (anti-chain-reaction). */
	private static final int MAX_NEARBY_TERMITES = 6;

	private int allySummonCooldown;

	public EntityTFTowerTermite(World par1World) {
		super(par1World);

		this.setSize(0.3F, 0.7F);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(4, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
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
		// MITE_BALANCE 9.3: Tower Termite HP 24, damage 11
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(24.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.27D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(11.0D);
	}

	/** MITE_BALANCE 9.3: 10 XP. */
	@Override
	public int getExperienceValue() {
		return 10;
	}

	/**
	 * returns if this entity triggers Blocks.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound() {
		return "mob.silverfish.say";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return "mob.silverfish.hit";
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return "mob.silverfish.kill";
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		if (this.isEntityInvulnerable()) {
			return null;
		}

		DamageSource source = damage.getSource();
		if (this.allySummonCooldown <= 0 && (source instanceof EntityDamageSource || source == DamageSource.magic)) {
			this.allySummonCooldown = 20;
		}

		return super.attackEntityFrom(damage);
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();

		// if we are summoning allies, do that
		if (this.allySummonCooldown > 0) {
			--this.allySummonCooldown;

			if (this.allySummonCooldown == 0) {
				tryToSummonAllies();
			}
		}

		// if we have no target, burrow
		if (this.getAttackTarget() == null && this.getNavigator().noPath()) {
			tryToBurrow();
		}
	}

	/**
	 * Check nearby blocks.  If they are infested blocks, pop them!
	 */
	protected void tryToSummonAllies() {
		if (this.worldObj.isRemote) {
			return;
		}

		int sx = MathHelper.floor_double(this.posX);
		int sy = MathHelper.floor_double(this.posY);
		int sz = MathHelper.floor_double(this.posZ);
		boolean stopSummoning = false;

		for (int dy = 0; !stopSummoning && dy <= 5 && dy >= -5; dy = dy <= 0 ? 1 - dy : 0 - dy) {
			for (int dx = 0; !stopSummoning && dx <= 10 && dx >= -10; dx = dx <= 0 ? 1 - dx : 0 - dx) {
				for (int dz = 0; !stopSummoning && dz <= 10 && dz >= -10; dz = dz <= 0 ? 1 - dz : 0 - dz) {
					Block blockID = this.worldObj.getBlock(sx + dx, sy + dy, sz + dz);
					int blockMeta = this.worldObj.getBlockMetadata(sx + dx, sy + dy, sz + dz);

					if (isInfestedBlock(blockID, blockMeta)) {
						if (this.countNearbyTermites() >= MAX_NEARBY_TERMITES) {
							stopSummoning = true;
							break;
						}

						this.worldObj.playAuxSFX(2001, sx + dx, sy + dy, sz + dz, TFWorldUtil.id(blockID) + (blockMeta << 12));
						TFWorldUtil.setAir(this.worldObj, sx + dx, sy + dy, sz + dz);

						// the infested block releases its termite (see BlockTFTowerWood.dropBlockAsEntityItem)
						EntityTFTowerTermite termite = new EntityTFTowerTermite(this.worldObj);
						termite.setLocationAndAngles(sx + dx + 0.5D, sy + dy, sz + dz + 0.5D, 0.0F, 0.0F);
						this.worldObj.spawnEntityInWorld(termite);
						termite.spawnExplosionParticle();

						if (this.rand.nextBoolean()) {
							stopSummoning = true;
							break;
						}
					}
				}
			}
		}
	}

	private int countNearbyTermites() {
		AxisAlignedBB box = this.boundingBox.expand(16.0D, 8.0D, 16.0D);
		List list = this.worldObj.getEntitiesWithinAABB(EntityTFTowerTermite.class, box);
		return list.size();
	}

	/**
	 * Look at a random nearby block.  Is it the proper tower wood?  If so, burrow.
	 */
	protected void tryToBurrow() {
		int x = MathHelper.floor_double(this.posX);
		int y = MathHelper.floor_double(this.posY + 0.5D);
		int z = MathHelper.floor_double(this.posZ);
		int randomFacing = this.rand.nextInt(6);

		x += Facing.offsetsXForSide[randomFacing];
		y += Facing.offsetsYForSide[randomFacing];
		z += Facing.offsetsZForSide[randomFacing];

		Block blockIDNearby = this.worldObj.getBlock(x, y, z);
		int blockMetaNearby = this.worldObj.getBlockMetadata(x, y, z);

		if (canBurrowIn(blockIDNearby, blockMetaNearby)) {
			TFWorldUtil.setBlock(this.worldObj, x, y, z, TFBlocks.towerWood, BlockTFTowerWood.META_INFESTED, 3);
			this.spawnExplosionParticle();
			this.setDead();
		}
	}

	/**
	 * @return true if we can burrow in the specified block
	 */
	protected boolean canBurrowIn(Block blockIDNearby, int blockMetaNearby) {
		return blockIDNearby == TFBlocks.towerWood && blockMetaNearby == 0;
	}

	/**
	 * @return true if the nearby block is infested
	 */
	protected boolean isInfestedBlock(Block blockIDNearby, int blockMetaNearby) {
		return blockIDNearby == TFBlocks.towerWood && blockMetaNearby == BlockTFTowerWood.META_INFESTED;
	}

	/**
	 * Plays step sound at given x, y, z for the entity
	 */
	@Override
	protected void playStepSound(int x, int y, int z, int block_id) {
		this.playSound("mob.silverfish.step", 0.15F, 1.0F);
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId() {
		return TFItems.borerEssence.itemID;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		this.renderYawOffset = this.rotationYaw;
		super.onUpdate();
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

}
