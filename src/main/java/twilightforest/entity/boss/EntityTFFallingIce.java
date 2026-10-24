package twilightforest.entity.boss;

import java.util.ArrayList;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;

public class EntityTFFallingIce extends Entity {

	private static final int HANG_TIME = 100;
	/** MITE_BALANCE 9.4: Alpha Yeti rampage hit is 17 raw, so the falling block is capped there. */
	private static final int MAX_DAMAGE = 17;
	private int fallTime;
	private float hurtAmount;
	private int hurtMax;

	public EntityTFFallingIce(World par1World) {
		super(par1World);
		this.setSize(2.98F, 2.98F);

		this.hurtAmount = 10;
		this.hurtMax = MAX_DAMAGE;
	}

	public EntityTFFallingIce(World par1World, int x, int y, int z) {
		this(par1World);

		this.preventEntitySpawning = true;

		this.setPosition(x, y, z);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected void entityInit() {
	}

	/** MITE: non-living entities must say how they react to fire/lava, otherwise a red HUD error is shown. */
	@Override
	public boolean canCatchFire() {
		return false;
	}

	@Override
	public boolean isHarmedByFire() {
		return false;
	}

	@Override
	public boolean isHarmedByLava() {
		return false;
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		++this.fallTime;

		if (this.fallTime > HANG_TIME) {
			this.motionY -= 0.03999999910593033D;
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (!this.worldObj.isRemote) {
			if (this.onGround) {
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
				this.motionY *= -0.5D;

				this.setDead();
			}
		}

		// kill other nearby blocks if they are not as old as this one
		if (!this.worldObj.isRemote) {
			ArrayList<?> nearby = new ArrayList<Object>(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox));

			for (Object o : nearby) {
				if (o instanceof EntityTFFallingIce) {
					EntityTFFallingIce otherIce = (EntityTFFallingIce) o;

					if (otherIce.getFallTime() < this.fallTime) {
						otherIce.setDead();
					}
				}
			}

			destroyIceInAABB(this.boundingBox.expand(0.5, 0, 0.5));
		}

		makeTrail();
	}

	public void makeTrail() {
		for (int i = 0; i < 2; i++) {
			double dx = this.posX + 2F * (rand.nextFloat() - rand.nextFloat());
			double dy = this.posY - 3F + 3F * (rand.nextFloat() - rand.nextFloat());
			double dz = this.posZ + 2F * (rand.nextFloat() - rand.nextFloat());

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "snowwarning", dx, dy, dz, 0, -1, 0);
		}
	}

	/**
	 * Called when the mob is falling. Calculates and applies fall damage.
	 */
	@Override
	protected void fall(float par1) {
		int distance = MathHelper.ceiling_float_int(par1 - 1.0F);

		if (distance > 0) {
			ArrayList<?> nearby = new ArrayList<Object>(this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(2, 0, 2)));
			DamageSource damagesource = DamageSource.fallingBlock;
			for (Object o : nearby) {
				Entity entity = (Entity) o;
				if (!(entity instanceof EntityTFYetiAlpha)) {
					TFDamage.hurt(entity, damagesource, (float) Math.min(MathHelper.floor_float((float) distance * this.hurtAmount), this.hurtMax));
				}
			}
		}

		for (int i = 0; i < 200; i++) {
			double dx = this.posX + 3F * (rand.nextFloat() - rand.nextFloat());
			double dy = this.posY + 2 + 3F * (rand.nextFloat() - rand.nextFloat());
			double dz = this.posZ + 3F * (rand.nextFloat() - rand.nextFloat());

			// MITE has no packed ice: ice is used
			TFParticles.spawnBlockCrack(this.worldObj, Block.ice.blockID, 0, dx, dy, dz, 0, 0, 0);
		}

		this.playSound(Block.anvil.stepSound.getBreakSound(), 3F, 0.5F);
		this.playSound(Block.ice.stepSound.getBreakSound(), 3F, 0.5F);
	}

	/**
	 * Destroys all ice and stone blocks inside the given bounding box.
	 */
	public void destroyIceInAABB(AxisAlignedBB par1AxisAlignedBB) {
		int minX = MathHelper.floor_double(par1AxisAlignedBB.minX);
		int minY = MathHelper.floor_double(par1AxisAlignedBB.minY);
		int minZ = MathHelper.floor_double(par1AxisAlignedBB.minZ);
		int maxX = MathHelper.floor_double(par1AxisAlignedBB.maxX);
		int maxY = MathHelper.floor_double(par1AxisAlignedBB.maxY);
		int maxZ = MathHelper.floor_double(par1AxisAlignedBB.maxZ);

		for (int dx = minX; dx <= maxX; ++dx) {
			for (int dy = minY; dy <= maxY; ++dy) {
				for (int dz = minZ; dz <= maxZ; ++dz) {
					Block block = this.worldObj.getBlock(dx, dy, dz);

					// 1.7.10 packed_ice does not exist in MITE; the yeti turns blocks into ice (see EntityTFYetiAlpha)
					if (block == Block.ice || block == Block.stone) {
						this.worldObj.setBlock(dx, dy, dz, 0, 0, 3);
					}
				}
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	@Override
	public boolean canRenderOnFire() {
		return false;
	}

	public Block getBlock() {
		// MITE has no packed ice
		return Block.ice;
	}

	public int getFallTime() {
		return this.fallTime;
	}
}
