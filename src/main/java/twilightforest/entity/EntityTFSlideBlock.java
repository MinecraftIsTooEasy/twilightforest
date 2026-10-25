package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;
import twilightforest.network.ITFSpawnData;

public class EntityTFSlideBlock extends Entity implements ITFSpawnData {

	private static final int WARMUP_TIME = 20;
	private Block myBlock;
	private int myMeta;
	private short slideTime;
	private boolean canDropItem = true;

	float moveX;
	float moveY;
	float moveZ;

	public EntityTFSlideBlock(World world) {
		super(world);
		this.preventEntitySpawning = true;
		this.entityCollisionReduction = 1F;
		this.setSize(0.98F, 0.98F);
	}

	public EntityTFSlideBlock(World world, double x, double y, double z, Block block, int meta) {
		super(world);

		this.myBlock = block;
		this.myMeta = meta;
		this.preventEntitySpawning = true;
		this.entityCollisionReduction = 1F;
		this.setSize(0.98F, 0.98F);
		this.setPosition(x, y, z);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;

		this.determineMoveDirection();
	}

	/**
	 * Called when creating the entity to determine which direction the block will slide.  The logic here is a little tricky
	 */
	private void determineMoveDirection() {
		this.moveX = 0F;
		this.moveY = 0F;
		this.moveZ = 0F;

		int bx = MathHelper.floor_double(this.posX);
		int by = MathHelper.floor_double(this.posY);
		int bz = MathHelper.floor_double(this.posZ);

		if ((this.myMeta & 12) == 4) {
			// horizontal blocks will go up or down if there is a block on one side and air on the other
			if (!this.worldObj.isAirBlock(bx, by + 1, bz) && this.worldObj.isAirBlock(bx, by - 1, bz)) {
				this.moveY = -1F;
			} else if (!this.worldObj.isAirBlock(bx, by - 1, bz) && this.worldObj.isAirBlock(bx, by + 1, bz)) {
				this.moveY = 1F;
			} else if (!this.worldObj.isAirBlock(bx, by, bz + 1) && this.worldObj.isAirBlock(bx, by, bz - 1)) { // then try Z
				this.moveZ = -1F;
			} else if (!this.worldObj.isAirBlock(bx, by, bz - 1) && this.worldObj.isAirBlock(bx, by, bz + 1)) {
				this.moveZ = 1F;
			} else if (this.worldObj.isAirBlock(bx, by - 1, bz)) { // if no wall, travel towards open air
				this.moveY = -1F;
			} else if (this.worldObj.isAirBlock(bx, by + 1, bz)) {
				this.moveY = 1F;
			} else if (this.worldObj.isAirBlock(bx, by, bz - 1)) {
				this.moveZ = -1F;
			} else if (this.worldObj.isAirBlock(bx, by, bz + 1)) {
				this.moveZ = 1F;
			}
		} else if ((this.myMeta & 12) == 8) {
			// horizontal blocks will go up or down if there is a block on one side and air on the other
			if (!this.worldObj.isAirBlock(bx, by + 1, bz) && this.worldObj.isAirBlock(bx, by - 1, bz)) {
				this.moveY = -1F;
			} else if (!this.worldObj.isAirBlock(bx, by - 1, bz) && this.worldObj.isAirBlock(bx, by + 1, bz)) {
				this.moveY = 1F;
			} else if (!this.worldObj.isAirBlock(bx + 1, by, bz) && this.worldObj.isAirBlock(bx - 1, by, bz)) { // then try X
				this.moveX = -1F;
			} else if (!this.worldObj.isAirBlock(bx - 1, by, bz) && this.worldObj.isAirBlock(bx + 1, by, bz)) {
				this.moveX = 1F;
			} else if (this.worldObj.isAirBlock(bx, by - 1, bz)) { // if no wall, travel towards open air
				this.moveY = -1F;
			} else if (this.worldObj.isAirBlock(bx, by + 1, bz)) {
				this.moveY = 1F;
			} else if (this.worldObj.isAirBlock(bx - 1, by, bz)) {
				this.moveX = -1F;
			} else if (this.worldObj.isAirBlock(bx + 1, by, bz)) {
				this.moveX = 1F;
			}
		} else if ((this.myMeta & 12) == 0) {
			// vertical blocks priority is -x, +x, -z, +z
			if (!this.worldObj.isAirBlock(bx + 1, by, bz) && this.worldObj.isAirBlock(bx - 1, by, bz)) {
				this.moveX = -1F;
			} else if (!this.worldObj.isAirBlock(bx - 1, by, bz) && this.worldObj.isAirBlock(bx + 1, by, bz)) {
				this.moveX = 1F;
			} else if (!this.worldObj.isAirBlock(bx, by, bz + 1) && this.worldObj.isAirBlock(bx, by, bz - 1)) {
				this.moveZ = -1F;
			} else if (!this.worldObj.isAirBlock(bx, by, bz - 1) && this.worldObj.isAirBlock(bx, by, bz + 1)) {
				this.moveZ = 1F;
			} else if (this.worldObj.isAirBlock(bx - 1, by, bz)) { // if no wall, travel towards open air
				this.moveX = -1F;
			} else if (this.worldObj.isAirBlock(bx + 1, by, bz)) {
				this.moveX = 1F;
			} else if (this.worldObj.isAirBlock(bx, by, bz - 1)) {
				this.moveZ = -1F;
			} else if (this.worldObj.isAirBlock(bx, by, bz + 1)) {
				this.moveZ = 1F;
			}
		}
	}

	@Override
	protected void entityInit() {
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	@Override
	public float getShadowSize() {
		return 0.0F;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.myBlock == null) {
			this.setDead();
		} else {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			++this.slideTime;
			// start moving after warmup
			if (this.slideTime > WARMUP_TIME) {
				this.motionX += this.moveX * 0.03999999910593033D;
				this.motionY += this.moveY * 0.03999999910593033D;
				this.motionZ += this.moveZ * 0.03999999910593033D;
				this.moveEntity(this.motionX, this.motionY, this.motionZ);
			}
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;

			// sound
			if (this.slideTime % 5 == 0) {
				this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, TwilightForestMod.ID + ":random.slider", 1.0F, 0.9F + (this.rand.nextFloat() * 0.4F));
			}

			if (!this.worldObj.isRemote) {
				int bx = MathHelper.floor_double(this.posX);
				int by = MathHelper.floor_double(this.posY);
				int bz = MathHelper.floor_double(this.posZ);

				if (this.slideTime == 1) {
					if (this.worldObj.getBlock(bx, by, bz) != this.myBlock) {
						this.setDead();
						return;
					}

					this.worldObj.setBlockToAir(bx, by, bz);
				}

				// if we have not hit anything after 2 seconds of movement, reverse direction
				if (this.slideTime == WARMUP_TIME + 40) {
					this.motionX = 0;
					this.motionY = 0;
					this.motionZ = 0;

					this.moveX *= -1F;
					this.moveY *= -1F;
					this.moveZ *= -1F;
				}

				if (this.isCollided || this.isStopped()) {
					this.motionX *= 0.699999988079071D;
					this.motionZ *= 0.699999988079071D;
					this.motionY *= 0.699999988079071D;

					this.setDead();

					if (this.canPlaceBlockAt(bx, by, bz) && this.worldObj.setBlock(bx, by, bz, this.myBlock.blockID, this.myMeta, 3)) {
						// successfully set block
					} else if (this.canDropItem) {
						this.dropItemStack(new ItemStack(this.myBlock, 1, this.myBlock.getItemSubtype(this.myMeta)), 0.0F);
					}
				} else if (this.slideTime > 100 && !this.worldObj.isRemote && (by < 1 || by > 256) || this.slideTime > 600) {
					if (this.canDropItem) {
						this.dropItemStack(new ItemStack(this.myBlock, 1, this.myBlock.getItemSubtype(this.myMeta)), 0.0F);
					}

					this.setDead();
				}

				// push things out and damage them
				this.damageKnockbackEntities(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox), this);
			}
		}
	}

	/**
	 * Replacement for World.canPlaceEntityOnSide(block, x, y, z, true, ...): the spot must be air or replaceable by the block.
	 */
	private boolean canPlaceBlockAt(int x, int y, int z) {
		if (this.worldObj.isAirBlock(x, y, z)) {
			return true;
		}
		Block existing = this.worldObj.getBlock(x, y, z);
		return existing != null && existing.canBeReplacedBy(this.worldObj.getBlockMetadata(x, y, z), this.myBlock, this.myMeta);
	}

	/**
	 * Pushes all entities inside the list out and damages them
	 */
	private void damageKnockbackEntities(List<?> par1List, Entity me) {
		for (Object o : par1List) {
			Entity entity = (Entity) o;
			if (entity instanceof EntityLivingBase) {
				TFDamage.hurtGeneric(entity, 5);

				double kx = (this.posX - entity.posX) * 2.0;
				double kz = (this.posZ - entity.posZ) * 2.0;

				// strength 1: MITE multiplies by it, the 1.7.10 5 was ignored
				((EntityLivingBase) entity).knockBack(this, 1.0F, kx, kz);
			}
		}
	}

	/**
	 * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
	 * pushable on contact, like boats or minecarts.
	 */
	@Override
	public AxisAlignedBB getCollisionBox(Entity p_70114_1_) {
		return null;
	}

	private boolean isStopped() {
		return this.moveX == 0 && this.moveY == 0 && this.moveZ == 0;
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	@Override
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbtTagCompound) {
		this.myBlock = Block.getBlock(nbtTagCompound.getInteger("TileID"));
		this.myMeta = nbtTagCompound.getByte("Meta");
		this.slideTime = nbtTagCompound.getShort("Time");
		this.moveX = nbtTagCompound.getFloat("MoveX");
		this.moveY = nbtTagCompound.getFloat("MoveY");
		this.moveZ = nbtTagCompound.getFloat("MoveZ");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbtTagCompound) {
		nbtTagCompound.setInteger("TileID", this.myBlock == null ? 0 : this.myBlock.blockID);
		nbtTagCompound.setByte("Meta", (byte) this.myMeta);
		nbtTagCompound.setShort("Time", this.slideTime);
		nbtTagCompound.setFloat("MoveX", this.moveX);
		nbtTagCompound.setFloat("MoveY", this.moveY);
		nbtTagCompound.setFloat("MoveZ", this.moveZ);
	}

	public Block getBlock() {
		return this.myBlock;
	}

	public int getMeta() {
		return this.myMeta;
	}

	/**
	 * Replacement for IEntityAdditionalSpawnData.writeSpawnData: blockId + (meta << 16), carried in the
	 * Packet23VehicleSpawn throwerEntityId (see TFEntitySpawnRegistry). Must stay positive: meta is 0..15.
	 */
	@Override
	public int tf$writeSpawnData() {
		int id = this.myBlock == null ? 0 : this.myBlock.blockID;
		return id + ((this.myMeta & 15) << 16);
	}

	@Override
	public void tf$readSpawnData(int blockData) {
		this.myBlock = Block.getBlock(blockData & 65535);
		this.myMeta = blockData >> 16;
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}
}
