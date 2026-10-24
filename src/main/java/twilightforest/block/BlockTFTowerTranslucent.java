package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.Entity;
import net.minecraft.Explosion;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;

/**
 * Vanishing / reappearing tower blocks that are not items (builder output, reverter replacement, reactor debris and the
 * fake gold / diamond blocks of the reactor room).
 * <p>
 * Balance (MITE_BALANCE B4b): stone material, hardness 6.0, minimum harvest level 3, explosion resistance 30.
 * The block has no item form (TF dropped nothing, had no creative entry and refused silk touch).
 */
public class BlockTFTowerTranslucent extends Block {

	public static final int META_REAPPEARING_INACTIVE = 0;
	public static final int META_REAPPEARING_ACTIVE = 1;
	public static final int META_BUILT_INACTIVE = 2;
	public static final int META_BUILT_ACTIVE = 3;
	public static final int META_REVERTER_REPLACEMENT = 4;
	public static final int META_REACTOR_DEBRIS = 5;
	public static final int META_FAKE_GOLD = 6;
	public static final int META_FAKE_DIAMOND = 7;

	public static Icon TEX_REAPPEARING_INACTIVE;
	public static Icon TEX_REAPPEARING_ACTIVE;
	public static Icon TEX_BUILT_INACTIVE;
	public static Icon TEX_BUILT_ACTIVE;
	public static Icon TEX_REVERTER_REPLACEMENT;

	private static Random sideRNG = new Random();

	public BlockTFTowerTranslucent(int id) {
		super(id, Material.stone, new BlockConstants().setNeverHidesAdjacentFaces());
		this.setHardness(6.0F);
		this.setMinHarvestLevel(3);
		this.setResistance(2000.0F);
		this.setStepSound(Block.soundMetalFootstep);
		// no creative tab: the block cannot be carried (canBeCarried() == false)
	}

	// ---- subtypes / shape (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= META_REAPPEARING_INACTIVE && meta <= META_FAKE_DIAMOND;
	}

	/** TF getBlocksMovement: only the "trace" blocks (reappearing, either state) do not block movement. */
	@Override
	public boolean isSolid(boolean[] is_solid, int meta) {
		return meta != META_REAPPEARING_INACTIVE && meta != META_REAPPEARING_ACTIVE;
	}

	/** TF isOpaqueCube false / renderAsNormalBlock true; only the debris has random (non-cube) bounds. */
	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int meta) {
		return meta != META_REACTOR_DEBRIS;
	}

	/** TF getItemDropped returned null and canSilkHarvest returned false: the block has no item form. */
	@Override
	public boolean canBeCarried() {
		return false;
	}

	@Override
	public boolean canSilkHarvest(int meta) {
		return false;
	}

	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return 0;
	}

	@Override
	public int dropBlockAsItself(BlockBreakInfo info) {
		return 0;
	}

	// ---- collision / bounds ----

	/**
	 * Returns a bounding box (world coordinates) for collisions
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		int meta = world.getBlockMetadata(x, y, z) & 7;

		if (meta == META_REAPPEARING_INACTIVE || meta == META_REAPPEARING_ACTIVE) {
			return null;
		} else {
			return super.getCollisionBounds(world, x, y, z, entity);
		}
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);

		// (TF set the small "trace" box for the reappearing metas and then immediately overwrote it with the full box
		// in the trailing else; that effective behaviour is kept)
		if (meta == META_REACTOR_DEBRIS) {
			this.setBlockBoundsForCurrentThread(sideRNG.nextFloat() * 0.4F, sideRNG.nextFloat() * 0.4F, sideRNG.nextFloat() * 0.4F,
					1.0F - sideRNG.nextFloat() * 0.4F, 1.0F - sideRNG.nextFloat() * 0.4F, 1.0F - sideRNG.nextFloat() * 0.4F);
		} else {
			this.setBlockBoundsForCurrentThread(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	// ---- hardness / harvesting ----

	/**
	 * Returns the block hardness for a metadata. The reverter replacement and the debris are like glass.
	 */
	@Override
	public float getBlockHardness(int meta) {
		if (meta == META_REVERTER_REPLACEMENT || meta == META_REACTOR_DEBRIS) {
			return 0.3F;
		} else {
			return super.getBlockHardness(meta);
		}
	}

	@Override
	public int getMinHarvestLevel(int meta) {
		if (meta == META_REVERTER_REPLACEMENT || meta == META_REACTOR_DEBRIS) {
			return 0;
		} else {
			return super.getMinHarvestLevel(meta);
		}
	}

	/** Explosion resistance is static in MITE (balance B4b: 30). */
	@Override
	public float getExplosionResistance(Explosion explosion) {
		return 30F;
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
		case META_BUILT_INACTIVE:
			return TEX_BUILT_INACTIVE;
		case META_BUILT_ACTIVE:
			return TEX_BUILT_ACTIVE;
		case META_REVERTER_REPLACEMENT:
			return TEX_REVERTER_REPLACEMENT;
		case META_REACTOR_DEBRIS:
			Block toMimic = sideRNG.nextBoolean() ? (sideRNG.nextBoolean() ? Block.portal : Block.netherrack) : (sideRNG.nextBoolean() ? Block.bedrock : Block.obsidian);
			return toMimic.getIcon(side, 0);
		case META_FAKE_GOLD:
			return Block.blockGold.getIcon(side, 0);
		case META_FAKE_DIAMOND:
			return Block.blockDiamond.getIcon(side, 0);
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFTowerTranslucent.TEX_REAPPEARING_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reappearing_trace_off");
		BlockTFTowerTranslucent.TEX_REAPPEARING_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_reappearing_trace_on");
		BlockTFTowerTranslucent.TEX_BUILT_INACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_built_off");
		BlockTFTowerTranslucent.TEX_BUILT_ACTIVE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_built_on");
		BlockTFTowerTranslucent.TEX_REVERTER_REPLACEMENT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_antibuilt");
	}

	// ---- behaviour ----

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

			if (meta == META_BUILT_ACTIVE) {
				par1World.setBlock(x, y, z, 0, 0, 3);
				par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.pop", 0.3F, 0.5F);

				// activate all adjacent inactive vanish blocks
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x - 1, y, z);
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x + 1, y, z);
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x, y + 1, z);
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x, y - 1, z);
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x, y, z + 1);
				BlockTFTowerDevice.checkAndActivateVanishBlock(par1World, x, y, z - 1);
				return true;
			}
			if (meta == META_REAPPEARING_ACTIVE) {
				par1World.setBlock(x, y, z, TFIds.TOWER_DEVICE, BlockTFTowerDevice.META_REAPPEARING_INACTIVE, 3);
				par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.5F);
				return true;
			} else if (meta == META_REAPPEARING_INACTIVE) {
				BlockTFTowerDevice.changeToActiveVanishBlock(par1World, x, y, z, META_REAPPEARING_ACTIVE);
				return true;
			}
		}
		return false;
	}

}
