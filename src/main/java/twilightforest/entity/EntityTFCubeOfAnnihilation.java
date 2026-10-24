package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.RaycastCollision;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFDamage;
import twilightforest.item.ItemTFCubeOfAnnihilation;
import twilightforest.network.TFNetwork;


public class EntityTFCubeOfAnnihilation extends EntityThrowable  {

	// TODO(mite-port): TF used Block.getExplosionResistance(this) < 8; MITE has no separate resistance, the hardness limit is a stand-in (creative-only item)
	private static final float MAX_ANNIHILATE_HARDNESS = 2.0F;

	boolean hasHitObstacle = false;

	public EntityTFCubeOfAnnihilation(World par1World) {
		super(par1World);

		this.setSize(1.1F, 1F);
	}


	public EntityTFCubeOfAnnihilation(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
		this.setSize(1F, 1F);
	}


	public EntityTFCubeOfAnnihilation(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);

		this.setSize(1F, 1F);
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return Item.ingotIron;
	}

	// 1.7.10 isImmuneToFire = true
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
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0F;
	}



	@Override
	protected void onImpact(RaycastCollision rc) {
		// only hit living things
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase)
		{
			EntityLivingBase thrower = this.getThrower();
			DamageSource source = thrower instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) thrower) : DamageSource.causeThrownDamage(this, thrower);
			if (TFDamage.hurt(rc.getEntityHit(), source, 10))
			{
				this.ticksExisted += 60;
			}
		}

		if (rc.isBlock()) {

			// demolish some blocks
			if (!this.worldObj.isRemote) {
				this.affectBlocksInAABB(this.boundingBox.expand(0.2F, 0.2F, 0.2F), this.getThrower());
			}

		}

	}


	/**
	 * Do our ball and chain effect on blocks we hit.  Harvest/destroy/whatevs
	 * @param entity
	 */
	private boolean affectBlocksInAABB(AxisAlignedBB par1AxisAlignedBB, EntityLivingBase entity) {
		int minX = MathHelper.floor_double(par1AxisAlignedBB.minX);
		int minY = MathHelper.floor_double(par1AxisAlignedBB.minY);
		int minZ = MathHelper.floor_double(par1AxisAlignedBB.minZ);
		int maxX = MathHelper.floor_double(par1AxisAlignedBB.maxX);
		int maxY = MathHelper.floor_double(par1AxisAlignedBB.maxY);
		int maxZ = MathHelper.floor_double(par1AxisAlignedBB.maxZ);
		boolean hitBlock = false;
		for (int dx = minX; dx <= maxX; ++dx) {
			for (int dy = minY; dy <= maxY; ++dy) {
				for (int dz = minZ; dz <= maxZ; ++dz) {
					Block block = this.worldObj.getBlock(dx, dy, dz);
					int currentMeta = this.worldObj.getBlockMetadata(dx, dy, dz);

					if (block != null) {
						if (canAnnihilate(dx, dy, dz, block, currentMeta)) {
							this.worldObj.setBlockToAir(dx, dy, dz);

							this.worldObj.playSoundAtEntity(this, "random.fizz", 0.125f, this.rand.nextFloat() * 0.25F + 0.75F);

							this.sendAnnihilateBlockPacket(worldObj, dx, dy, dz);

						} else {
							// return if we hit an obstacle
							this.hasHitObstacle = true;
						}
						hitBlock = true;
					}
				}
			}
		}

		return hitBlock;
	}


	private boolean canAnnihilate(int dx, int dy, int dz, Block block, int meta) {
		// whitelist many castle blocks
		if (block == TFBlocks.deadrock || block == TFBlocks.castleBlock || (block == TFBlocks.castleMagic && meta != 3) || block == TFBlocks.forceField || block == TFBlocks.thorns) {
			return true;
		}

		float hardness = block.getBlockHardness(meta);
		return hardness >= 0 && hardness <= MAX_ANNIHILATE_HARDNESS;
	}



	private void sendAnnihilateBlockPacket(World world, int x, int y, int z) {
		// send packet to everything within 64 blocks
		TFNetwork.sendAnnihilateBlock(world, x, y, z);
	}

	/**
	 * Skip most of the living update things
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		// all server side
		if (!this.worldObj.isRemote) {

			if (this.getThrower() == null) {
				this.setDead();
				return;
			}

			if (this.isReturning()) {
				// if we are returning, and are near enough to the player, then we are done
				List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));

				if (list.contains(this.getThrower())) {
					if (this.getThrower() instanceof EntityPlayer) {
						ItemTFCubeOfAnnihilation.setCubeAsReturned((EntityPlayer)this.getThrower());
					}
					this.setDead();
				}
			}



			// always head towards either the point or towards the player
			Vec3 destPoint = Vec3.createVectorHelper(this.getThrower().posX, this.getThrower().posY + this.getThrower().getEyeHeight(), this.getThrower().posZ);

			if (!this.isReturning()) {
				Vec3 look = this.getThrower().getLookVec();

				float dist = 16F;

				look.xCoord *= dist;
				look.yCoord *= dist;
				look.zCoord *= dist;

				destPoint.xCoord += look.xCoord;
				destPoint.yCoord += look.yCoord;
				destPoint.zCoord += look.zCoord;
			}

			// set motions
			Vec3 velocity = Vec3.createVectorHelper(this.posX - destPoint.xCoord, (this.posY + this.height / 2F) - destPoint.yCoord, this.posZ - destPoint.zCoord);

			this.motionX -= velocity.xCoord;
			this.motionY -= velocity.yCoord;
			this.motionZ -= velocity.zCoord;

			// normalize speed
			float currentSpeed = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

			float maxSpeed = 0.5F;


			if (currentSpeed > maxSpeed) {
				this.motionX /= currentSpeed / maxSpeed;
				this.motionY /= currentSpeed / maxSpeed;
				this.motionZ /= currentSpeed / maxSpeed;
			} else {
				float slow = 0.5F;
				this.motionX *= slow;
				this.motionY *= slow;
				this.motionZ *= slow;
			}


			// demolish some blocks
			this.affectBlocksInAABB(this.boundingBox.expand(0.2F, 0.2F, 0.2F), this.getThrower());

		}

	}

	public boolean isReturning() {
		if (this.hasHitObstacle || this.getThrower() == null || !(this.getThrower() instanceof EntityPlayer)) {
			return true;
		} else {
			EntityPlayer player = (EntityPlayer) this.getThrower();

			return !player.isUsingItem();
		}
	}



	/**
	 * Velocity
	 */
	@Override
	protected float func_70182_d()
	{
		return 1.5F;
	}


}
