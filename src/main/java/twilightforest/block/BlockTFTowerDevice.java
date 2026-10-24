package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.EnumFace;
import net.minecraft.EntityPlayer;
import net.minecraft.Explosion;
import net.minecraft.ITileEntityProvider;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.tileentity.TileEntityTFCReactorActive;
import twilightforest.tileentity.TileEntityTFGhastTrapActive;
import twilightforest.tileentity.TileEntityTFGhastTrapInactive;
import twilightforest.tileentity.TileEntityTFReverter;
import twilightforest.tileentity.TileEntityTFTowerBuilder;

/**
 * The Dark Tower machinery block: reappearing / vanishing doors, locks, builders, antibuilders, ghast traps and reactors,
 * selected by metadata.
 * <p>
 * Balance (MITE_BALANCE B4b): stone material, hardness 5.0, minimum harvest level 2 (copper pickaxe), explosion resistance 20;
 * the locked / vanishing door states are unbreakable (hardness -1, level 100).
 * <p>
 * MITE has no per-metadata tile entities ({@code ITileEntityProvider.createNewTileEntity(World)} has no metadata), so the
 * server side manages the tile entities by hand: {@link #onBlockAdded} (which also runs on every metadata change, because
 * those are done with {@code world.setBlock(x,y,z,sameId,newMeta,..)}) creates the tile entity the new metadata needs and
 * {@link #breakBlock} removes the old one, exactly like 1.7.10 did through {@code hasTileEntity/createTileEntity}. The
 * server therefore never uses {@link #createNewTileEntity(World)} (it returns null); the client, which gets neither
 * callback, gets a {@link ClientProxyTileEntity} that hosts the right tile entity for the current metadata.
 */
public class BlockTFTowerDevice extends Block implements ITileEntityProvider {

	private static Icon TEX_REAPPEARING_INACTIVE;
	private static Icon TEX_REAPPEARING_ACTIVE;
	private static Icon TEX_VANISH_INACTIVE;
	private static Icon TEX_VANISH_ACTIVE;
	private static Icon TEX_VANISH_LOCKED;
	private static Icon TEX_VANISH_UNLOCKED;
	private static Icon TEX_BUILDER_INACTIVE;
	private static Icon TEX_BUILDER_ACTIVE;
	private static Icon TEX_ANTIBUILDER;
	private static Icon TEX_BUILDER_TIMEOUT;
	private static Icon TEX_GHASTTRAP_INACTIVE;
	private static Icon TEX_GHASTTRAP_ACTIVE;
	private static Icon TEX_REACTOR_INACTIVE;
	private static Icon TEX_REACTOR_ACTIVE;
	private static Icon TEX_GHASTTRAP_LID_INACTIVE;
	private static Icon TEX_GHASTTRAP_LID_ACTIVE;
	private static Icon TEX_SMOKER_ACTIVE;
	private static Icon TEX_SMOKER_INACTIVE;
	private static Icon TEX_FIREJET_ACTIVE;
	private static Icon TEX_FIREJET_INACTIVE;

	public static final int META_REAPPEARING_INACTIVE = 0;
	public static final int META_REAPPEARING_ACTIVE = 1;
	public static final int META_VANISH_INACTIVE = 2;
	public static final int META_VANISH_ACTIVE = 3;
	public static final int META_VANISH_LOCKED = 4;
	public static final int META_VANISH_UNLOCKED = 5;
	public static final int META_BUILDER_INACTIVE = 6;
	public static final int META_BUILDER_ACTIVE = 7;
	public static final int META_BUILDER_TIMEOUT = 8;
	public static final int META_ANTIBUILDER = 9;
	public static final int META_GHASTTRAP_INACTIVE = 10;
	public static final int META_GHASTTRAP_ACTIVE = 11;
	public static final int META_REACTOR_INACTIVE = 12;
	public static final int META_REACTOR_ACTIVE = 13;

	public BlockTFTowerDevice(int id) {
		super(id, Material.stone, new BlockConstants());
		this.setHardness(5.0F);
		this.setMinHarvestLevel(2);
		this.setResistance(35F);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= META_REAPPEARING_INACTIVE && meta <= META_REACTOR_ACTIVE;
	}

	/** The item subtype is the metadata the block drops as (TF damageDropped); this gives the TF creative list. */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return damageDropped(meta);
	}

	/**
	 * Determines the damage on the item the block drops.
	 */
	public int damageDropped(int meta) {
		switch (meta) {
		case META_REAPPEARING_ACTIVE:
			return META_REAPPEARING_INACTIVE;
		case META_BUILDER_ACTIVE:
		case META_BUILDER_TIMEOUT:
			return META_BUILDER_INACTIVE;
		case META_VANISH_ACTIVE:
			return META_VANISH_INACTIVE;
		case META_GHASTTRAP_ACTIVE:
			return META_GHASTTRAP_INACTIVE;
		case META_REACTOR_ACTIVE:
			return META_REACTOR_INACTIVE;
		default:
			return meta;
		}
	}

	// ---- icons ----

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		switch (meta) {
		case META_REAPPEARING_INACTIVE:
		default:
			return TEX_REAPPEARING_INACTIVE;
		case META_REAPPEARING_ACTIVE:
			return TEX_REAPPEARING_ACTIVE;
		case META_VANISH_INACTIVE:
			return TEX_VANISH_INACTIVE;
		case META_VANISH_ACTIVE:
			return TEX_VANISH_ACTIVE;
		case META_VANISH_LOCKED:
			return TEX_VANISH_LOCKED;
		case META_VANISH_UNLOCKED:
			return TEX_VANISH_UNLOCKED;
		case META_BUILDER_INACTIVE:
			return TEX_BUILDER_INACTIVE;
		case META_BUILDER_TIMEOUT:
			return TEX_BUILDER_TIMEOUT;
		case META_BUILDER_ACTIVE:
			return TEX_BUILDER_ACTIVE;
		case META_ANTIBUILDER:
			return TEX_ANTIBUILDER;
		case META_GHASTTRAP_INACTIVE:
			if (side >= 2) {
				return TEX_GHASTTRAP_INACTIVE;
			} else if (side == 1) {
				return TEX_GHASTTRAP_LID_INACTIVE;
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		case META_GHASTTRAP_ACTIVE:
			if (side >= 2) {
				return TEX_GHASTTRAP_ACTIVE;
			} else if (side == 1) {
				return TEX_GHASTTRAP_LID_ACTIVE;
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		case META_REACTOR_INACTIVE:
			return TEX_REACTOR_INACTIVE;
		case META_REACTOR_ACTIVE:
			return TEX_REACTOR_ACTIVE;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFTowerDevice.TEX_REAPPEARING_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reappearing_off");
		BlockTFTowerDevice.TEX_REAPPEARING_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reappearing_on");
		BlockTFTowerDevice.TEX_VANISH_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_vanish_off");
		BlockTFTowerDevice.TEX_VANISH_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_vanish_on");
		BlockTFTowerDevice.TEX_VANISH_LOCKED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_lock_on");
		BlockTFTowerDevice.TEX_VANISH_UNLOCKED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_lock_off");
		BlockTFTowerDevice.TEX_BUILDER_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_builder_off");
		BlockTFTowerDevice.TEX_BUILDER_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_builder_on");
		BlockTFTowerDevice.TEX_ANTIBUILDER = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_antibuilder");
		BlockTFTowerDevice.TEX_BUILDER_TIMEOUT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_builder_timeout");
		BlockTFTowerDevice.TEX_GHASTTRAP_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_ghasttrap_off");
		BlockTFTowerDevice.TEX_GHASTTRAP_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_ghasttrap_on");
		BlockTFTowerDevice.TEX_REACTOR_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reactor_off");
		BlockTFTowerDevice.TEX_REACTOR_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reactor_on");
		BlockTFTowerDevice.TEX_GHASTTRAP_LID_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_ghasttraplid_off");
		BlockTFTowerDevice.TEX_GHASTTRAP_LID_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_ghasttraplid_on");
		BlockTFTowerDevice.TEX_SMOKER_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_smoker_off");
		BlockTFTowerDevice.TEX_SMOKER_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_smoker_on");
		BlockTFTowerDevice.TEX_FIREJET_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_firejet_off");
		BlockTFTowerDevice.TEX_FIREJET_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_firejet_on");
	}

	// TODO(mite-port): TF getLightValue is per-metadata (4 for active builder / vanish / reappearing, 10 for the antibuilder,
	// 15 for active ghast trap / reactor); MITE light is a static per-id value, so the device blocks do not glow.

	// ---- hardness / harvesting / explosions ----

	/**
	 * Returns the block hardness for a metadata. Most vanish blocks can't be broken.
	 */
	@Override
	public float getBlockHardness(int meta) {
		switch (meta) {
		case META_REAPPEARING_ACTIVE:
		case META_VANISH_INACTIVE:
		case META_VANISH_ACTIVE:
		case META_VANISH_LOCKED:
		case META_VANISH_UNLOCKED:
			return -1;
		default:
			return super.getBlockHardness(meta);
		}
	}

	/** Balance B4b: the unbreakable door states need harvest level 100, i.e. only Tower Key / machinery can open them. */
	@Override
	public int getMinHarvestLevel(int meta) {
		switch (meta) {
		case META_REAPPEARING_ACTIVE:
		case META_VANISH_INACTIVE:
		case META_VANISH_ACTIVE:
		case META_VANISH_LOCKED:
		case META_VANISH_UNLOCKED:
			return 100;
		default:
			return super.getMinHarvestLevel(meta);
		}
	}

	/**
	 * Explosion resistance is static in MITE (balance B4b: 20).
	 * TODO(mite-port): TF gave the inactive vanish block 6000 and the locked one 6000000 (per-metadata, per-position); the
	 * MITE signature has neither, so every state uses the device value.
	 */
	@Override
	public float getExplosionResistance(Explosion explosion) {
		return 20F;
	}

	// ---- drops ----

	/**
	 * Replaces TF getItemDropped / damageDropped: every state drops its inactive form, the antibuilder drops nothing.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.getMetadata() == META_ANTIBUILDER || info.wasCrushed()) {
			return 0;
		}
		return this.dropBlockAsEntityItem(info, this.createStackedBlock(info.getMetadata()));
	}

	// ---- interaction ----

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, EnumFace face, float par7, float par8, float par9) {
		int meta = par1World.getBlockMetadata(x, y, z);

		if (meta == META_VANISH_INACTIVE || meta == META_REAPPEARING_INACTIVE) {
			// called on both sides: the state change only happens on the server, the client just swings the arm
			if (!par1World.isRemote) {
				if (areNearbyLockBlocks(par1World, x, y, z)) {
					par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 1.0F, 0.3F);
				} else {
					changeToActiveVanishBlock(par1World, x, y, z, meta == META_VANISH_INACTIVE ? META_VANISH_ACTIVE : META_REAPPEARING_ACTIVE);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Are any of the 26 adjacent blocks a locked vanishing block?
	 */
	public static boolean areNearbyLockBlocks(World world, int x, int y, int z) {
		boolean locked = false;

		//TODO: this is hacky.  We really need to determine the exact blocks of the door and check those for locks.
		for (int dx = x - 2; dx <= x + 2; dx++) {
			for (int dy = y - 2; dy <= y + 2; dy++) {
				for (int dz = z - 2; dz <= z + 2; dz++) {
					if (world.getBlockId(dx, dy, dz) == TFIds.TOWER_DEVICE && world.getBlockMetadata(dx, dy, dz) == META_VANISH_LOCKED) {
						locked = true;
					}
				}
			}
		}

		return locked;
	}

	/**
	 * Change this block into an different device block
	 */
	public static void unlockBlock(World par1World, int x, int y, int z) {
		int thereBlockID = par1World.getBlockId(x, y, z);
		int thereBlockMeta = par1World.getBlockMetadata(x, y, z);

		// (TF tested "device || locked" here, which would also have matched any other block with metadata 4)
		if (thereBlockID == TFIds.TOWER_DEVICE && thereBlockMeta == META_VANISH_LOCKED) {
			changeToBlockMeta(par1World, x, y, z, META_VANISH_UNLOCKED);
			par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
		}
	}

	/**
	 * Change this block into an different device block
	 */
	private static void changeToBlockMeta(World par1World, int x, int y, int z, int meta) {
		int thereBlockID = par1World.getBlockId(x, y, z);

		if (thereBlockID == TFIds.TOWER_DEVICE || thereBlockID == TFIds.TOWER_TRANSLUCENT) {
			par1World.setBlock(x, y, z, thereBlockID, meta, 3);
			par1World.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
			par1World.notifyBlocksOfNeighborChange(x, y, z, thereBlockID);
		}
	}

	// ---- tile entities ----

	/**
	 * Server: the tile entity is managed by {@link #onBlockAdded}/{@link #breakBlock}, so nothing is created here (the engine
	 * also calls this when it looks up a missing tile entity, and null is handled). Client: neither callback exists there, so
	 * a proxy is handed out that creates the matching tile entity on its first tick (and follows metadata changes).
	 */
	@Override
	public TileEntity createNewTileEntity(World world) {
		return world != null && world.isRemote ? new ClientProxyTileEntity() : null;
	}

	/** TF hasTileEntity(meta). */
	public static boolean hasTileEntityFor(int metadata) {
		return metadata == META_BUILDER_ACTIVE || metadata == META_ANTIBUILDER || metadata == META_REACTOR_ACTIVE
				|| metadata == META_GHASTTRAP_INACTIVE || metadata == META_GHASTTRAP_ACTIVE;
	}

	/** TF createTileEntity(world, meta): the tile entity a metadata needs, or null. */
	public static TileEntity createTileEntityForMeta(int metadata) {
		if (metadata == META_BUILDER_ACTIVE) {
			return new TileEntityTFTowerBuilder();
		} else if (metadata == META_ANTIBUILDER) {
			return new TileEntityTFReverter();
		} else if (metadata == META_GHASTTRAP_INACTIVE) {
			return new TileEntityTFGhastTrapInactive();
		} else if (metadata == META_GHASTTRAP_ACTIVE) {
			return new TileEntityTFGhastTrapActive();
		} else if (metadata == META_REACTOR_ACTIVE) {
			return new TileEntityTFCReactorActive();
		} else {
			return null;
		}
	}

	/** Remove the tile entity when the block (or its metadata) changes, like BlockContainer does. */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		super.breakBlock(world, x, y, z, blockId, meta);
		world.removeBlockTileEntity(x, y, z);
	}

	/**
	 * Called whenever the block is added into the world (server only; also for every metadata change). Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World par1World, int x, int y, int z) {
		super.onBlockAdded(par1World, x, y, z);

		int meta = par1World.getBlockMetadata(x, y, z);

		if (!par1World.isRemote) {
			// create the tile entity this metadata needs
			TileEntity tileEntity = createTileEntityForMeta(meta);
			if (tileEntity != null) {
				par1World.setBlockTileEntity(x, y, z, tileEntity);
			}

			if (meta == META_BUILDER_INACTIVE && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				changeToBlockMeta(par1World, x, y, z, META_BUILDER_ACTIVE);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
			}
		}
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own).
	 */
	@Override
	public boolean onNeighborBlockChange(World par1World, int x, int y, int z, int neighborBlockId) {
		if (super.onNeighborBlockChange(par1World, x, y, z, neighborBlockId)) {
			return true;
		}

		int meta = par1World.getBlockMetadata(x, y, z);

		if (!par1World.isRemote) {
			if (meta == META_VANISH_INACTIVE && par1World.isBlockIndirectlyGettingPowered(x, y, z) && !areNearbyLockBlocks(par1World, x, y, z)) {
				changeToActiveVanishBlock(par1World, x, y, z, META_VANISH_ACTIVE);
			}

			if (meta == META_REAPPEARING_INACTIVE && par1World.isBlockIndirectlyGettingPowered(x, y, z) && !areNearbyLockBlocks(par1World, x, y, z)) {
				changeToActiveVanishBlock(par1World, x, y, z, META_REAPPEARING_ACTIVE);
			}

			if (meta == META_BUILDER_INACTIVE && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				changeToBlockMeta(par1World, x, y, z, META_BUILDER_ACTIVE);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);

				par1World.scheduleBlockUpdate(x, y, z, this.blockID, 4);
			}

			if (meta == META_BUILDER_ACTIVE && !par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				changeToBlockMeta(par1World, x, y, z, META_BUILDER_INACTIVE);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);

				par1World.scheduleBlockUpdate(x, y, z, this.blockID, 4);
			}

			if (meta == META_BUILDER_TIMEOUT && !par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				changeToBlockMeta(par1World, x, y, z, META_BUILDER_INACTIVE);
			}

			if (meta == META_GHASTTRAP_INACTIVE && isInactiveTrapCharged(par1World, x, y, z) && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				changeToBlockMeta(par1World, x, y, z, META_GHASTTRAP_ACTIVE);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);

				par1World.scheduleBlockUpdate(x, y, z, this.blockID, 4);
			}

			if (meta == META_REACTOR_INACTIVE && isReactorReady(par1World, x, y, z)) {
				// check if we should fire up the reactor
				changeToBlockMeta(par1World, x, y, z, META_REACTOR_ACTIVE);
			}
		}
		return false;
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World par1World, int x, int y, int z, Random par5Random) {
		if (super.updateTick(par1World, x, y, z, par5Random)) {
			return true;
		}

		if (!par1World.isRemote) {
			int meta = par1World.getBlockMetadata(x, y, z);

			if (meta == META_VANISH_ACTIVE || meta == META_REAPPEARING_ACTIVE) {
				if (meta == META_VANISH_ACTIVE) {
					par1World.setBlock(x, y, z, 0, 0, 3);
				} else {
					par1World.setBlock(x, y, z, TFIds.TOWER_TRANSLUCENT, BlockTFTowerTranslucent.META_REAPPEARING_INACTIVE, 3);
					par1World.scheduleBlockUpdate(x, y, z, TFIds.TOWER_TRANSLUCENT, 80);
				}
				par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.pop", 0.3F, 0.5F);

				// activate all adjacent inactive vanish blocks
				checkAndActivateVanishBlock(par1World, x - 1, y, z);
				checkAndActivateVanishBlock(par1World, x + 1, y, z);
				checkAndActivateVanishBlock(par1World, x, y + 1, z);
				checkAndActivateVanishBlock(par1World, x, y - 1, z);
				checkAndActivateVanishBlock(par1World, x, y, z + 1);
				checkAndActivateVanishBlock(par1World, x, y, z - 1);
				return true;
			}

			if (meta == META_BUILDER_ACTIVE && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				this.letsBuild(par1World, x, y, z);
			}

			if (meta == META_BUILDER_INACTIVE || meta == META_BUILDER_TIMEOUT) {
				// activate all adjacent inactive vanish blocks
				checkAndActivateVanishBlock(par1World, x - 1, y, z);
				checkAndActivateVanishBlock(par1World, x + 1, y, z);
				checkAndActivateVanishBlock(par1World, x, y + 1, z);
				checkAndActivateVanishBlock(par1World, x, y - 1, z);
				checkAndActivateVanishBlock(par1World, x, y, z + 1);
				checkAndActivateVanishBlock(par1World, x, y, z - 1);
			}
		}
		return false;
	}

	/**
	 * Start the builder block tileentity building!
	 */
	private void letsBuild(World par1World, int x, int y, int z) {
		TileEntity te = par1World.getBlockTileEntity(x, y, z);

		if (te instanceof TileEntityTFTowerBuilder) {
			TileEntityTFTowerBuilder tileEntity = (TileEntityTFTowerBuilder) te;
			if (!tileEntity.makingBlocks) {
				tileEntity.startBuilding();
			}
		}
	}

	/**
	 * Check if the inactive trap block is fully charged
	 */
	private boolean isInactiveTrapCharged(World par1World, int x, int y, int z) {
		TileEntity te = par1World.getBlockTileEntity(x, y, z);

		return te instanceof TileEntityTFGhastTrapInactive && ((TileEntityTFGhastTrapInactive) te).isCharged();
	}

	/**
	 * Check if the reactor has all the specified things around it
	 */
	private boolean isReactorReady(World world, int x, int y, int z) {
		return world.getBlockId(x, y + 1, z) == Block.blockRedstone.blockID
				&& world.getBlockId(x, y - 1, z) == Block.blockRedstone.blockID
				&& world.getBlockId(x + 1, y, z) == Block.blockRedstone.blockID
				&& world.getBlockId(x - 1, y, z) == Block.blockRedstone.blockID
				&& world.getBlockId(x, y, z + 1) == Block.blockRedstone.blockID
				&& world.getBlockId(x, y, z - 1) == Block.blockRedstone.blockID;
	}

	// ---- client effects ----

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World par1World, int x, int y, int z, Random par5Random) {
		int meta = par1World.getBlockMetadata(x, y, z);

		if (meta == META_VANISH_ACTIVE || meta == META_REAPPEARING_ACTIVE || meta == META_BUILDER_ACTIVE) {
			for (int i = 0; i < 1; ++i) {
				this.sparkle(par1World, x, y, z, par5Random);
			}
		}
	}

	/**
	 * Shine bright like a DIAMOND! (or actually, sparkle like redstone ore)
	 */
	public void sparkle(World world, int x, int y, int z, Random rand) {
		double offset = 0.0625D;

		for (int side = 0; side < 6; ++side) {
			double rx = x + rand.nextFloat();
			double ry = y + rand.nextFloat();
			double rz = z + rand.nextFloat();

			if (side == 0 && !world.isBlockStandardFormOpaqueCube(x, y + 1, z)) {
				ry = y + 1 + offset;
			}

			if (side == 1 && !world.isBlockStandardFormOpaqueCube(x, y - 1, z)) {
				ry = y + 0 - offset;
			}

			if (side == 2 && !world.isBlockStandardFormOpaqueCube(x, y, z + 1)) {
				rz = z + 1 + offset;
			}

			if (side == 3 && !world.isBlockStandardFormOpaqueCube(x, y, z - 1)) {
				rz = z + 0 - offset;
			}

			if (side == 4 && !world.isBlockStandardFormOpaqueCube(x + 1, y, z)) {
				rx = x + 1 + offset;
			}

			if (side == 5 && !world.isBlockStandardFormOpaqueCube(x - 1, y, z)) {
				rx = x + 0 - offset;
			}

			if (rx < x || rx > x + 1 || ry < 0.0D || ry > y + 1 || rz < z || rz > z + 1) {
				TFParticles.spawn(world, "reddust", rx, ry, rz, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	// ---- shared state machine helpers ----

	/**
	 * If the targeted block is a vanishing block, activate it
	 */
	public static void checkAndActivateVanishBlock(World world, int x, int y, int z) {
		int thereID = world.getBlockId(x, y, z);
		int thereMeta = world.getBlockMetadata(x, y, z);

		if (thereID == TFIds.TOWER_DEVICE && (thereMeta == META_VANISH_INACTIVE || thereMeta == META_VANISH_UNLOCKED) && !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_VANISH_ACTIVE);
		} else if (thereID == TFIds.TOWER_DEVICE && thereMeta == META_REAPPEARING_INACTIVE && !areNearbyLockBlocks(world, x, y, z)) {
			changeToActiveVanishBlock(world, x, y, z, META_REAPPEARING_ACTIVE);
		} else if (thereID == TFIds.TOWER_TRANSLUCENT && thereMeta == BlockTFTowerTranslucent.META_BUILT_INACTIVE) {
			changeToActiveVanishBlock(world, x, y, z, BlockTFTowerTranslucent.META_BUILT_ACTIVE);
		}
	}

	/**
	 * Change this block into an active vanishing block
	 */
	public static void changeToActiveVanishBlock(World par1World, int x, int y, int z, int meta) {
		changeToBlockMeta(par1World, x, y, z, meta);
		par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.pop", 0.3F, 0.6F);

		int thereBlockID = par1World.getBlockId(x, y, z);
		par1World.scheduleBlockUpdate(x, y, z, thereBlockID, getTickRateFor(thereBlockID, meta, par1World.rand));
	}

	/**
	 * We need variable, metadata-based tick rates
	 */
	private static int getTickRateFor(int thereBlockID, int meta, Random rand) {
		if (thereBlockID == TFIds.TOWER_DEVICE && (meta == META_VANISH_ACTIVE || meta == META_REAPPEARING_ACTIVE)) {
			return 2 + rand.nextInt(5);
		} else if (thereBlockID == TFIds.TOWER_TRANSLUCENT && meta == BlockTFTowerTranslucent.META_BUILT_ACTIVE) {
			return 10;
		}

		return 15;
	}

	/**
	 * Client-side stand-in for the per-metadata tile entities. It is never registered (it is never saved: only the client
	 * world creates it) and hosts the tile entity the block's current metadata needs, swapping it when the metadata changes.
	 */
	public static class ClientProxyTileEntity extends TileEntity {
		private TileEntity delegate;
		private int delegateMeta = -1;

		@Override
		public void updateEntity() {
			World world = this.worldObj;
			if (world == null) {
				return;
			}

			int meta = world.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
			if (meta != this.delegateMeta) {
				this.delegateMeta = meta;
				TileEntity wanted = createTileEntityForMeta(meta);
				if (wanted == null) {
					this.delegate = null;
				} else if (this.delegate == null || this.delegate.getClass() != wanted.getClass()) {
					wanted.setWorldObj(world);
					wanted.xCoord = this.xCoord;
					wanted.yCoord = this.yCoord;
					wanted.zCoord = this.zCoord;
					this.delegate = wanted;
				}
			}

			if (this.delegate != null) {
				this.delegate.updateEntity();
			}
		}
	}

}
