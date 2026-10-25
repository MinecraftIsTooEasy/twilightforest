package twilightforest.entity;

import twilightforest.TFConfig;
import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityXPOrb;
import net.minecraft.Item;
import net.minecraft.MapColor;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;

public class EntityTFIceExploder extends EntityMob {

	private static final float EXPLOSION_RADIUS = 1;

	/** MITE hardness cutoff approximating 1.7.10 explosion resistance < 8 (planks/dirt/sand/wool/leaves yes; cobble 1.5, brick 1.5, stone, ores no). */
	private static final float SOFT_HARDNESS = 1.5F;

	/** Map colours of the 16 wool/dye metas (1.7.10 BlockColored map colours), MITE has no per-meta map colour. */
	private static final int[] DYE_COLORS = {
			0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C,
			0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919
	};

	public EntityTFIceExploder(World par1World) {
		super(par1World);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.setSize(0.8F, 1.8F);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(3.0D);
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId() {
		return Item.snowball.itemID;
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

	@Override
	public float getEyeHeight() {
		return this.height * 0.6F;
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
	 * handles entity death timer, experience orb and particle creation
	 */
	@Override
	protected void onDeathUpdate() {
		++this.deathTime;

		if (this.deathTime == 60) {
			int i;

			boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
			// explosion.breakBlocks=false (default): the blast hurts creatures only
			float blockRadius = TFConfig.explosionBreakBlocks ? EntityTFIceExploder.EXPLOSION_RADIUS : 0.0F;
			this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, blockRadius, EntityTFIceExploder.EXPLOSION_RADIUS, flag);

			if (flag) {
				this.detonate();
			}

			// func_146066_aG (can drop loot) -> !isChild()
			if (!this.worldObj.isRemote && (this.recentlyHit > 0 || this.isEntityPlayer()) && !this.isChild() && this.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) {
				i = this.getExperienceValue();

				while (i > 0) {
					int j = EntityXPOrb.getXPSplit(i);
					i -= j;
					this.worldObj.spawnEntityInWorld(new EntityXPOrb(this.worldObj, this.posX, this.posY, this.posZ, j));
				}
			}

			this.setDead();

			for (i = 0; i < 20; ++i) {
				double d2 = this.rand.nextGaussian() * 0.02D;
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				TFParticles.spawn(this.worldObj, "explode", this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
			}
		}
	}

	private void detonate() {
		int range = 4;

		int sx = MathHelper.floor_double(this.posX);
		int sy = MathHelper.floor_double(this.posY);
		int sz = MathHelper.floor_double(this.posZ);

		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

					float randRange = range + (rand.nextFloat() - rand.nextFloat()) * 2F;

					if (distance < randRange) {
						this.transformBlock(sx + dx, sy + dy, sz + dz);
					}
				}
			}
		}
	}

	private void transformBlock(int x, int y, int z) {
		Block block = this.worldObj.getBlock(x, y, z);
		if (block == null) {
			return; // air
		}
		int meta = this.worldObj.getBlockMetadata(x, y, z);

		// check if we should even explode this
		// 1.7.10 compared explosion resistance < 8 (soft blocks only, not stone/cobble/brick/ores); MITE has hardness only, approximated by SOFT_HARDNESS
		if (block.getBlockHardness(meta) >= 0 && block.getBlockHardness(meta) < SOFT_HARDNESS) {

			int blockColor = 16777215;

			// colorMultiplier may touch client-only colourizers (grass/foliage) on a dedicated server
			try {
				// figure out color
				blockColor = block.colorMultiplier(worldObj, x, y, z);
			} catch (NoSuchMethodError | NoClassDefFoundError e) {
				// fine, we're on a server
			}

			if (blockColor == 16777215) {
				blockColor = this.getMapColorValue(block, meta);
			}

			// do appropriate transformation
			if (this.shouldTransformGlass(block, x, y, z)) {
				// TODO(mite-port): MITE (1.6.4) has no stained glass, plain glass loses the colour
				TFWorldUtil.setBlock(this.worldObj, x, y, z, Block.glass, 0, 3);
			} else if (this.shouldTransformClay(block, x, y, z)) {
				TFWorldUtil.setBlock(this.worldObj, x, y, z, Block.stainedClay, this.getMetaForColor(blockColor), 3);
			}
		}
	}

	/**
	 * Replacement for Block.getMapColor(meta)
	 */
	private int getMapColorValue(Block block, int meta) {
		if (block == Block.cloth || block == Block.stainedClay) {
			return DYE_COLORS[meta & 15];
		}
		MapColor color = block.blockMaterial.map_color;
		return color != null ? color.colorValue : 16777215;
	}

	private boolean shouldTransformClay(Block block, int x, int y, int z) {
		return TFBlockUtil.isNormalCube(this.worldObj, x, y, z);
	}

	private boolean shouldTransformGlass(Block block, int x, int y, int z) {
		int meta = this.worldObj.getBlockMetadata(x, y, z);
		return block != null && block != Block.glass && this.isBlockNormalBounds(block, meta)
				&& (!block.isOpaqueStandardFormCube(this.worldObj, x, y, z) || TFBlockUtil.isLeaves(block) || block == Block.ice || block == TFBlocks.auroraBlock);
	}

	/**
	 * Full 0..1 bounds on all axes (MITE: standard form cube)
	 */
	private boolean isBlockNormalBounds(Block block, int meta) {
		return block.isStandardFormCube(meta);
	}

	private int getMetaForColor(int blockColor) {
		int red = (blockColor >> 16) & 255;
		int green = (blockColor >> 8) & 255;
		int blue = blockColor & 255;

		int bestColor = 0;
		int bestDifference = 1024;

		for (int i = 0; i < 15; i++) {
			int iColor = DYE_COLORS[i];

			int iRed = (iColor >> 16) & 255;
			int iGreen = (iColor >> 8) & 255;
			int iBlue = iColor & 255;

			int difference = Math.abs(red - iRed) + Math.abs(green - iGreen) + Math.abs(blue - iBlue);

			if (difference < bestDifference) {
				bestColor = i;
				bestDifference = difference;
			}
		}

		return bestColor;
	}

	/**
	 * Will return how many at most can spawn in a chunk at once.
	 */
	@Override
	public int getMaxSpawnedInChunk() {
		return 8;
	}

	/**
	 * MITE: the Aurora Palace is lit by glowstone and its ice lets the daylight in (1.7.10 used packed ice), so the light
	 * check would stop every spawn there; this mob only spawns from the palace spawn list.
	 */
	@Override
	protected boolean isValidLightLevel() {
		return true;
	}
}
