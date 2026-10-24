package twilightforest.block;

import java.util.Random;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.IBlockAccess;
import net.minecraft.ITileEntityProvider;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFWorldUtil;
import twilightforest.tileentity.TileEntityTFFlameJet;
import twilightforest.tileentity.TileEntityTFPoppingJet;
import twilightforest.tileentity.TileEntityTFSmoker;

/**
 * Fire jets, smokers and their encased (redstone driven) variants.
 * <p>
 * MITE has no per-metadata tile entities ({@code ITileEntityProvider.createNewTileEntity(World)} has no metadata), so every
 * metadata gets a tile entity from {@link #createNewTileEntity(World)} and the tile entities swap themselves for the
 * correct class (or remove themselves) through {@link #verifyTileEntity(TileEntity)} on their first tick.
 */
public class BlockTFFireJet extends Block implements ITileEntityProvider {

	public static final int META_SMOKER = 0;
	public static final int META_ENCASED_SMOKER_OFF = 1;
	public static final int META_ENCASED_SMOKER_ON = 2;
	public static final int META_JET_IDLE = 8;
	public static final int META_JET_POPPING = 9;
	public static final int META_JET_FLAME = 10;
	public static final int META_ENCASED_JET_IDLE = 11;
	public static final int META_ENCASED_JET_POPPING = 12;
	public static final int META_ENCASED_JET_FLAME = 13;

	private Icon iconJet;
	private Icon iconSide;
	private Icon iconSmokerInactive;
	private Icon iconSmokerActive;
	private Icon iconJetInactive;
	private Icon iconJetActive;

	public BlockTFFireJet(int id) {
		super(id, Material.stone, new BlockConstants());
		this.setHardness(1.5F);
		//this.setResistance(10.0F);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabRedstone);
		this.setTickRandomly(true);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta == META_SMOKER || meta == META_ENCASED_SMOKER_OFF || meta == META_ENCASED_SMOKER_ON
				|| (meta >= META_JET_IDLE && meta <= META_ENCASED_JET_FLAME);
	}

	/** The item subtype is the metadata the block drops as (TF damageDropped). */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return damageDropped(meta);
	}

	/**
	 * Determines the damage on the item the block drops.
	 */
	public int damageDropped(int meta) {
		switch (meta) {
		default:
			return meta;
		case META_ENCASED_SMOKER_ON:
			return META_ENCASED_SMOKER_OFF;
		case META_ENCASED_JET_POPPING:
		case META_ENCASED_JET_FLAME:
			return META_ENCASED_JET_IDLE;
		case META_JET_POPPING:
		case META_JET_FLAME:
			return META_JET_IDLE;
		}
	}

	// ---- icons ----

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (meta == META_ENCASED_SMOKER_OFF) {
			if (side >= 2) {
				return iconSmokerInactive;
			} else if (side == 1) {
				return TFBlocks.towerDevice.getIcon(side, BlockTFTowerDevice.META_GHASTTRAP_INACTIVE);
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		} else if (meta == META_ENCASED_SMOKER_ON) {
			if (side >= 2) {
				return iconSmokerActive;
			} else if (side == 1) {
				return TFBlocks.towerDevice.getIcon(side, BlockTFTowerDevice.META_GHASTTRAP_ACTIVE);
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		}
		if (meta == META_ENCASED_JET_IDLE) {
			if (side >= 2) {
				return iconJetInactive;
			} else if (side == 1) {
				return TFBlocks.towerDevice.getIcon(side, BlockTFTowerDevice.META_GHASTTRAP_INACTIVE);
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		} else if (meta == META_ENCASED_JET_POPPING || meta == META_ENCASED_JET_FLAME) {
			if (side >= 2) {
				return iconJetActive;
			} else if (side == 1) {
				return TFBlocks.towerDevice.getIcon(side, BlockTFTowerDevice.META_GHASTTRAP_ACTIVE);
			} else {
				return TFBlocks.towerWood.getIcon(side, 1);
			}
		} else {
			// normal fire jet
			if (side == 1) {
				return iconJet;
			} else {
				return iconSide;
			}
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.iconSide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":firejet_side");
		this.iconJet = par1IconRegister.registerIcon(TwilightForestMod.ID + ":firejet_top");
		this.iconSmokerInactive = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_smoker_off");
		this.iconSmokerActive = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_smoker_on");
		this.iconJetInactive = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_firejet_off");
		this.iconJetActive = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerdev_firejet_on");
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z) {
		int meta = par1IBlockAccess.getBlockMetadata(x, y, z);

		if (meta == META_ENCASED_SMOKER_OFF || meta == META_ENCASED_SMOKER_ON
				|| meta == META_ENCASED_JET_IDLE || meta == META_ENCASED_JET_POPPING || meta == META_ENCASED_JET_FLAME) {
			return super.colorMultiplier(par1IBlockAccess, x, y, z);
		} else {
			int red = 0;
			int grn = 0;
			int blu = 0;

			for (int var8 = -1; var8 <= 1; ++var8) {
				for (int var9 = -1; var9 <= 1; ++var9) {
					int biomeColor = par1IBlockAccess.getBiomeGenForCoords(x + var9, z + var8).getBiomeGrassColor();
					red += (biomeColor & 16711680) >> 16;
					grn += (biomeColor & 65280) >> 8;
					blu += biomeColor & 255;
				}
			}

			return (red / 9 & 255) << 16 | (grn / 9 & 255) << 8 | blu / 9 & 255;
		}
	}

	// TODO(mite-port): TF getLightValue (15 for META_JET_FLAME / META_ENCASED_JET_FLAME) is per-metadata; MITE light is a static per-id value, so the jets do not glow.

	// ---- behaviour ----

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		// idle jets may turn active
		if (!world.isRemote && world.getBlockMetadata(x, y, z) == META_JET_IDLE) {
			int[] lavaPos = findLavaAround(world, x, y - 1, z);

			if (isLava(world, lavaPos[0], lavaPos[1], lavaPos[2])) {
				// deplete lava reservoir
				TFWorldUtil.setBlock(world, lavaPos[0], lavaPos[1], lavaPos[2], null, 0, 2);
				// change jet state
				world.setBlock(x, y, z, this.blockID, META_JET_POPPING, 0);
			}
		}
		return false;
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own)
	 */
	@Override
	public boolean onNeighborBlockChange(World par1World, int x, int y, int z, int neighborBlockId) {
		int meta = par1World.getBlockMetadata(x, y, z);

		if (!par1World.isRemote) {
			if (meta == META_ENCASED_SMOKER_OFF && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				par1World.setBlock(x, y, z, this.blockID, META_ENCASED_SMOKER_ON, 3);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
				return false;
			}

			if (meta == META_ENCASED_SMOKER_ON && !par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				par1World.setBlock(x, y, z, this.blockID, META_ENCASED_SMOKER_OFF, 3);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
				return false;
			}

			if (meta == META_ENCASED_JET_IDLE && par1World.isBlockIndirectlyGettingPowered(x, y, z)) {
				par1World.setBlock(x, y, z, this.blockID, META_ENCASED_JET_POPPING, 3);
				par1World.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.3F, 0.6F);
				return false;
			}
		}
		return false;
	}

	/**
	 * Find a full block of lava near the designated block.  This is intentionally not really that reliable.
	 */
	private int[] findLavaAround(World world, int x, int y, int z) {
		if (isLava(world, x, y, z)) {
			return new int[] { x, y, z };
		}
		// try three random spots nearby
		for (int i = 0; i < 3; i++) {
			int rx = x + world.rand.nextInt(3) - 1;
			int rz = z + world.rand.nextInt(3) - 1;
			if (isLava(world, rx, y, rz)) {
				return new int[] { rx, y, rz };
			}
		}

		// finally, give up
		return new int[] { x, y, z };
	}

	private boolean isLava(World world, int x, int y, int z) {
		return world.getBlockMaterial(x, y, z) == Material.lava && world.getBlockMetadata(x, y, z) == 0;
	}

	// ---- tile entities ----

	/**
	 * MITE passes no metadata here: hand out a neutral tile entity that corrects itself on its first tick.
	 */
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFFlameJet(-1);
	}

	/** Remove the tile entity when the block (or its state) changes, like BlockContainer does. */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		super.breakBlock(world, x, y, z, blockId, meta);
		world.removeBlockTileEntity(x, y, z);
	}

	/** TF createTileEntity: the tile entity a given metadata needs, or null if it needs none. */
	public static TileEntity createTileEntityForMeta(int metadata) {
		if (metadata == META_SMOKER || metadata == META_ENCASED_SMOKER_ON) {
			return new TileEntityTFSmoker();
		} else if (metadata == META_JET_POPPING) {
			return new TileEntityTFPoppingJet(META_JET_FLAME);
		} else if (metadata == META_JET_FLAME) {
			return new TileEntityTFFlameJet(META_JET_IDLE);
		} else if (metadata == META_ENCASED_JET_POPPING) {
			return new TileEntityTFPoppingJet(META_ENCASED_JET_FLAME);
		} else if (metadata == META_ENCASED_JET_FLAME) {
			return new TileEntityTFFlameJet(META_ENCASED_JET_IDLE);
		} else {
			return null;
		}
	}

	/** The state a jet in the given (popping/flame) state moves to when its timer runs out; -1 for other states. */
	public static int nextMetaFor(int metadata) {
		switch (metadata) {
		case META_JET_POPPING:
			return META_JET_FLAME;
		case META_JET_FLAME:
			return META_JET_IDLE;
		case META_ENCASED_JET_POPPING:
			return META_ENCASED_JET_FLAME;
		case META_ENCASED_JET_FLAME:
			return META_ENCASED_JET_IDLE;
		default:
			return -1;
		}
	}

	/**
	 * Called by the jet tile entities at the start of every tick. Makes sure the tile entity at its position is the class
	 * the block's current metadata needs; if not it is replaced (or removed) and false is returned, so the caller must stop.
	 */
	public static boolean verifyTileEntity(TileEntity te) {
		World world = te.getWorldObj();
		if (world == null) {
			return false;
		}
		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;
		if (world.getBlockId(x, y, z) != TFIds.FIRE_JET) {
			return false;
		}

		TileEntity wanted = createTileEntityForMeta(world.getBlockMetadata(x, y, z));
		if (wanted != null && wanted.getClass() == te.getClass()) {
			return true;
		}

		world.removeBlockTileEntity(x, y, z);
		if (wanted != null) {
			world.setBlockTileEntity(x, y, z, wanted);
		}
		return false;
	}
}
