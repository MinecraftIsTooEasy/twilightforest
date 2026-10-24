package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.BlockMounted;
import net.minecraft.CreativeTabs;
import net.minecraft.EnumFace;
import net.minecraft.IBlockAccess;
import net.minecraft.IconRegister;
import net.minecraft.ITileEntityProvider;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/**
 * Small wall/floor/ceiling critters (firefly, cicada, moonworm).
 * Metadata: 0 = unattached (legal if anything around can hold it), 1..6 = supported by the block at
 * x-1, x+1, z-1, z+1, below, above (same as 1.7.10). Support logic is MITE's BlockMounted/isLegalAt.
 * Drawn by a TESR, so the render type is -1 (see TFRenderTypes.CRITTER).
 */
public abstract class BlockTFCritter extends BlockMounted implements ITileEntityProvider {

	protected BlockTFCritter(int id) {
		super(id, Material.circuits, new BlockConstants().setNotAlwaysLegal().setNeverHidesAdjacentFaces());
		this.setHardness(0.0F);
		this.setCreativeTab(CreativeTabs.tabDecorations);

		this.setStepSound(new StepSoundTFInsect("squish", 0.25F, 0.6F));
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata <= 6;
	}

	@Override
	public EnumFace getFaceMountedTo(int metadata) {
		switch (metadata) {
		case 1:
			return EnumFace.EAST;
		case 2:
			return EnumFace.WEST;
		case 3:
			return EnumFace.SOUTH;
		case 4:
			return EnumFace.NORTH;
		case 6:
			return EnumFace.BOTTOM;
		case 5:
		default:
			// metadata 0 (unattached) never uses this: isLegalAt/isLegalOn are overridden for it
			return EnumFace.TOP;
		}
	}

	@Override
	public int getDefaultMetadataForFaceMountedTo(EnumFace face) {
		if (face.isEast()) {
			return 1;
		} else if (face.isWest()) {
			return 2;
		} else if (face.isSouth()) {
			return 3;
		} else if (face.isNorth()) {
			return 4;
		} else if (face.isTop()) {
			return 5;
		} else {
			return 6;
		}
	}

	/**
	 * We can place critters on any normal block, on leaves or on cactus.
	 */
	@Override
	public boolean canMountToBlock(int metadata, Block neighbor_block, int neighbor_block_metadata, EnumFace face) {
		Material material = neighbor_block.blockMaterial;
		if (material == Material.tree_leaves || material == Material.cactus) {
			return true;
		}
		return super.canMountToBlock(metadata, neighbor_block, neighbor_block_metadata, face);
	}

	@Override
	public boolean isLegalAt(World world, int x, int y, int z, int metadata) {
		if (metadata == 0) {
			return canPlaceBlockAt(world, x, y, z);
		}
		return super.isLegalAt(world, x, y, z, metadata);
	}

	@Override
	public boolean isLegalOn(int metadata, Block block_below, int block_below_metadata) {
		return metadata == 0 || super.isLegalOn(metadata, block_below, block_below_metadata);
	}

	/**
	 * True if at least one neighbouring block can hold a critter (1.7.10 canPlaceBlockAt).
	 */
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		for (int meta = 1; meta <= 6; meta++) {
			if (super.isLegalAt(world, x, y, z, meta)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		int facing = world.getBlockMetadata(x, y, z) & 7;
		float wide = 0.15F;
		if (facing == 1) {
			setBlockBoundsForCurrentThread(0.0F, 0.2F, 0.5F - wide, wide * 2.0F, 0.8F, 0.5F + wide);
		} else if (facing == 2) {
			setBlockBoundsForCurrentThread(1.0F - wide * 2.0F, 0.2F, 0.5F - wide, 1.0F, 0.8F, 0.5F + wide);
		} else if (facing == 3) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.2F, 0.0F, 0.5F + wide, 0.8F, wide * 2.0F);
		} else if (facing == 4) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.2F, 1.0F - wide * 2.0F, 0.5F + wide, 0.8F, 1.0F);
		} else if (facing == 5) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.0F, 0.2F, 0.5F + wide, wide * 2.0F, 0.8F);
		} else if (facing == 6) {
			setBlockBoundsForCurrentThread(0.5F - wide, 1.0F - wide * 2.0F, 0.2F, 0.5F + wide, 1.0F, 0.8F);
		} else {
			float f1 = 0.1F;
			setBlockBoundsForCurrentThread(0.5F - f1, 0.0F, 0.5F - f1, 0.5F + f1, 0.6F, 0.5F + f1);
		}
	}

	/** No collision box (1.7.10 getCollisionBoundingBoxFromPool returned null). */
	@Override
	public boolean isSolid(boolean[] is_solid, int metadata) {
		return false;
	}

	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getCritterBlockRenderID();
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if (world.getBlockMetadata(x, y, z) == 0) {
			// attach to the first neighbour that can hold us (same order as 1.7.10)
			for (int meta = 1; meta <= 6; meta++) {
				if (super.isLegalAt(world, x, y, z, meta)) {
					world.setBlockMetadataWithNotify(x, y, z, meta, 2);
					break;
				}
			}
		}

		dropCritterIfCantStay(world, x, y, z);

		// for fireflies, schedule a lighting update
		if (world.getBlockMetadata(x, y, z) == 0 && world.getBlock(x, y, z) == this) {
			world.scheduleBlockUpdate(x, y, z, this.blockID, tickRate(world));
		}
	}

	/**
	 * @return true if the critter can stay where it is, false if it was removed
	 */
	public boolean dropCritterIfCantStay(World world, int x, int y, int z) {
		return !checkIfNotLegal(world, x, y, z);
	}

	/**
	 * Replaces BlockContainer.breakBlock: remove the tile entity with the block.
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int block_id, int metadata) {
		super.breakBlock(world, x, y, z, block_id, metadata);
		world.removeBlockTileEntity(x, y, z);
	}

	@Override
	public boolean onBlockEventReceived(World world, int x, int y, int z, int event_id, int event_data) {
		super.onBlockEventReceived(world, x, y, z, event_id, event_data);
		net.minecraft.TileEntity te = world.getBlockTileEntity(x, y, z);
		return te != null ? te.receiveClientEvent(event_id, event_data) : false;
	}

	/**
	 * Properly register icon source
	 */
	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
	}
}
