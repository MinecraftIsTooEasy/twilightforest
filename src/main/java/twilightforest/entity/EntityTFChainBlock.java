package twilightforest.entity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockOre;
import net.minecraft.BlockRedstoneOre;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDragonPart;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityThrowable;
import net.minecraft.IEntityMultiPart;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.RaycastCollision;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFDamage;
import twilightforest.item.ItemTFChainBlock;


public class EntityTFChainBlock extends EntityThrowable implements IEntityMultiPart  {

	private static final int MAX_SMASH = 12;
	private static final int MAX_CHAIN = 16;
	// MITE_BALANCE 7.2: contact damage 8 (TF: 10); only soft blocks (hardness <= 2.0, harvest level <= 1, no ore, no tile entity) are smashed
	private static final float CONTACT_DAMAGE = 8F;
	private static final float MAX_SMASH_HARDNESS = 2.0F;
	private static final int MAX_SMASH_HARVEST_LEVEL = 1;

	private boolean isReturning = false;
	private int blocksSmashed = 0;

	// where are we headed?
	private double velX;
	private double velY;
	private double velZ;


	// for the client to show the chain
	private boolean isAttached;
	private EntityLivingBase attachedTo;

	public EntityTFGoblinChain chain1;
	public EntityTFGoblinChain chain2;
	public EntityTFGoblinChain chain3;
	public EntityTFGoblinChain chain4;
	public EntityTFGoblinChain chain5;
	public Entity[] partsArray = new Entity[0];

	public EntityTFChainBlock(World par1World) {
		super(par1World);

		this.setSize(0.6F, 0.6F);


		this.partsArray = (new Entity[]
				{
				chain1 = new EntityTFGoblinChain(this), chain2 = new EntityTFGoblinChain(this), chain3 = new EntityTFGoblinChain(this), chain4 = new EntityTFGoblinChain(this), chain5 = new EntityTFGoblinChain(this)
				});
	}


	public EntityTFChainBlock(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}


	public EntityTFChainBlock(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);

		this.setSize(0.6F, 0.6F);

		this.isReturning = false;
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return Item.ingotIron;
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
	 */
	@Override
	public void setThrowableHeading(double x, double y, double z, float speed, float accuracy)
	{
		super.setThrowableHeading(x, y, z, speed, accuracy);

		// save velocity
		this.velX = this.motionX;
		this.velY = this.motionY;
		this.velZ = this.motionZ;
	}

	/**
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0.05F;
	}


	@Override
	protected void onImpact(RaycastCollision rc) {
		// only hit living things
		if (rc.isEntity()) {
			Entity hit = rc.getEntityHit();
			if (hit instanceof EntityLivingBase && hit != this.getThrower()) {
				EntityLivingBase thrower = this.getThrower();
				DamageSource source = thrower instanceof EntityPlayer ? DamageSource.causePlayerDamage((EntityPlayer) thrower) : DamageSource.causeThrownDamage(this, thrower);
				if (TFDamage.hurt(hit, source, CONTACT_DAMAGE)) {
					// age when we hit a monster so that we go back to the player faster
					this.ticksExisted += 60;
					if (!this.worldObj.isRemote) {
						this.isReturning = true;
					}
				}
			}
			return;
		}

		if (rc.isBlock()) {
			Block hitBlock = rc.getBlockHit();

			// clang!
			if (!this.isReturning) {
				this.worldObj.playSoundAtEntity(this, "random.anvil_land", 0.125f, this.rand.nextFloat());
			}

			if (!this.worldObj.isRemote && this.blocksSmashed < MAX_SMASH) {
				if (hitBlock.getBlockHardness(rc.block_hit_metadata) > 0.3F) {
					// riccochet
					double bounce = 0.6;
					this.velX *= bounce;
					this.velY *= bounce;
					this.velZ *= bounce;

					if (rc.face_hit != null) {
						if (rc.face_hit.isBottom()) {
							if (this.velY > 0) {
								this.velY *= -bounce;
							}
						} else if (rc.face_hit.isTop()) {
							if (this.velY < 0) {
								this.velY *= -bounce;
							}
						} else if (rc.face_hit.isNorth()) {
							if (this.velZ > 0) {
								this.velZ *= -bounce;
							}
						} else if (rc.face_hit.isSouth()) {
							if (this.velZ < 0) {
								this.velZ *= -bounce;
							}
						} else if (rc.face_hit.isWest()) {
							if (this.velX > 0) {
								this.velX *= -bounce;
							}
						} else if (rc.face_hit.isEast()) {
							if (this.velX < 0) {
								this.velX *= -bounce;
							}
						}
					}
				}

				// demolish some blocks
				this.affectBlocksInAABB(this.boundingBox, this.getThrower());
			}

			// head back to owner
			if (!this.worldObj.isRemote) {
				this.isReturning = true;
			}

			// if we have smashed enough, add to ticks so that we go back faster
			if (this.blocksSmashed > MAX_SMASH && this.ticksExisted < 60) {
				this.ticksExisted += 60;
			}
		}

	}


	/**
	 * MITE_BALANCE 7.2: only soft, non-ore, non-tile-entity blocks can be smashed (stone and ores are safe).
	 */
	private boolean canSmash(Block block, int meta) {
		if (block == null) {
			return false;
		}
		float hardness = block.getBlockHardness(meta);
		return hardness >= 0 && hardness <= MAX_SMASH_HARDNESS
				&& block.getMinHarvestLevel(meta) <= MAX_SMASH_HARVEST_LEVEL
				&& !(block instanceof BlockOre) && !(block instanceof BlockRedstoneOre)
				&& !block.hasTileEntity();
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

					if (canSmash(block, currentMeta)) {

						if (entity instanceof EntityPlayer) {
							// drops the block and plays the break effect (the 2001 aux sfx of 1.7.10)
							TFBlockUtil.harvestBlockAs((EntityPlayer) entity, this.worldObj, dx, dy, dz);
						} else {
							this.worldObj.playAuxSFX(2001, dx, dy, dz, block.blockID + (currentMeta << 12));
							this.worldObj.setBlockToAir(dx, dy, dz);
						}

						this.blocksSmashed++;

						hitBlock = true;
					}
				}
			}
		}

		return hitBlock;
	}


	/**
	 * Skip most of the living update things
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		// chains are probably always null on the server
		if (this.chain1 != null) {
			chain1.onUpdate();
			chain2.onUpdate();
			chain3.onUpdate();
			chain4.onUpdate();
			chain5.onUpdate();
		}

		if (this.getThrower() == null && !this.worldObj.isRemote) {
			this.setDead();
		}

		if (this.getThrower() != null) {
			float distToPlayer = this.getDistanceToEntity(this.getThrower());
			// return if far enough away
			if (!this.isReturning && distToPlayer > MAX_CHAIN) {
				this.isReturning = true;
			}

			// despawn if close enough
			if (this.isReturning && distToPlayer < 1F) {
				if (this.getThrower() instanceof EntityPlayer) {
					ItemTFChainBlock.setChainAsReturned((EntityPlayer)this.getThrower());
				}
				this.setDead();
			}
		}

		// if we are returning, set course for the player
		if (this.isReturning && !this.worldObj.isRemote && this.getThrower() != null) {

			EntityLivingBase returnTo = this.getThrower();

			Vec3 back = Vec3.createVectorHelper(returnTo.posX - this.posX, returnTo.posY + (double)returnTo.getEyeHeight() - 1.200000023841858D - this.posY, returnTo.posZ - this.posZ).normalize();

			float age = Math.min(this.ticksExisted * 0.03F, 1.0F);

			// separate the return velocity from the normal bouncy velocity
			this.motionX = this.velX * (1.0 - age) + (back.xCoord * 2F * age);
			this.motionY = this.velY * (1.0 - age) + (back.yCoord * 2F * age) - this.getGravityVelocity();
			this.motionZ = this.velZ * (1.0 - age) + (back.zCoord * 2F * age);

		}


		// on the client, if we are not attached, assume we have just spawned, and attach to the player
		if (this.worldObj.isRemote && !this.isAttached) {
			List nearbyEntities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(-this.motionX, -this.motionY, -this.motionZ).expand(2.0D, 2.0D, 2.0D));
			for (int i = 0; i < nearbyEntities.size(); ++i) {
				Entity nearby = (Entity)nearbyEntities.get(i);

				// attach?  should we check for closest player?
				if (nearby instanceof EntityPlayer) {
					this.attachedTo = (EntityPlayer) nearby;
				}
			}

			this.isAttached = true;
		}

		// set chain positions, client only
		if (this.attachedTo != null && this.chain1 != null) {
			// interpolate chain position
			Vec3 handVec = this.attachedTo.getLookVec();

			handVec.rotateAroundY(-0.4F);

			double sx = this.attachedTo.posX + handVec.xCoord;
			double sy = this.attachedTo.posY + handVec.yCoord - 0.6F;
			double sz = this.attachedTo.posZ + handVec.zCoord;

			double ox = sx - this.posX;
			double oy = sy - this.posY - 0.25F;
			double oz = sz - this.posZ;

			this.chain1.setPosition(sx - ox * 0.05, sy - oy * 0.05, sz - oz * 0.05);
			this.chain2.setPosition(sx - ox * 0.25, sy - oy * 0.25, sz - oz * 0.25);
			this.chain3.setPosition(sx - ox * 0.45, sy - oy * 0.45, sz - oz * 0.45);
			this.chain4.setPosition(sx - ox * 0.65, sy - oy * 0.65, sz - oz * 0.65);
			this.chain5.setPosition(sx - ox * 0.85, sy - oy * 0.85, sz - oz * 0.85);
		}
	}

	// MITE: a thrown iron ingot would otherwise burn / be harmed by lava
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
	 * Velocity
	 */
	@Override
	protected float func_70182_d()
	{
		return 1.5F;
	}


	@Override
	public World func_82194_d() {
		return this.worldObj;
	}


	@Override
	public EntityDamageResult attackEntityFromPart(EntityDragonPart part, Damage damage) {
		return null;
	}

	/**
	 * We need to do this for the bounding boxes on the parts to become active
	 */
	@Override
	public Entity[] getParts()
	{
		return partsArray;
	}

}
