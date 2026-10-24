package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.BlockContainer;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.item.TFItems;
import twilightforest.tileentity.TileEntityTFTrophy;

/**
 * Head trophy similar to (and based partially on) BlockSkull
 *
 * Metadata: 1 = lying on the ground, 2-5 = wall mounted (set by ItemTFTrophy), bit 8 = "no drop" (creative break).
 * The trophy type (hydra, naga, ...) is the skull type of the tile entity.
 */
public class BlockTFTrophy extends BlockContainer {

	public BlockTFTrophy(int id) {
		super(id, Material.circuits, new BlockConstants().setNeverHidesAdjacentFaces());
		this.setBlockBoundsForAllThreads(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
		// MITE_BALANCE B8: trophy hardness 1.0, harvest level 0 (resistance 3 is not representable in MITE)
		this.setHardness(1.0F);
	}

	// ---- constructor-time virtuals (pure functions of metadata) ----

	@Override
	public boolean isValidMetadata(int metadata) {
		int m = metadata & 7;
		return m >= 1 && m <= 5;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return 0;
	}

	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	@Override
	public String getMetadataNotes() {
		return "1 if lying on ground (tile entity determines orientation), otherwise 2-5 if wall-mounted; +8 = creative break (no drop)";
	}

	/**
	 * The type of render function that is called for this block (-1 = tile entity special renderer only)
	 */
	@Override
	public int getRenderType() {
		return -1;
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z) & 7;
		TileEntity te = world.getBlockTileEntity(x, y, z);
		TileEntityTFTrophy trophy = te instanceof TileEntityTFTrophy ? (TileEntityTFTrophy) te : null;

		if (trophy != null && trophy.getSkullType() == 0) {
			// hydra bounds
			switch (meta) {
			case 1:
			default:
				this.setBlockBoundsForCurrentThread(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
				break;
			case 2:
			case 3:
				this.setBlockBoundsForCurrentThread(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 1.0F);
				break;
			case 4:
			case 5:
				this.setBlockBoundsForCurrentThread(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
				break;
			}
		} else if (trophy != null && trophy.getSkullType() == 3) {
			// urghast bounds
			this.setBlockBoundsForCurrentThread(0.25F, 0.5F, 0.25F, 0.75F, 1F, 0.75F);
		} else {
			// normal skull bounds
			switch (meta) {
			case 1:
			default:
				this.setBlockBoundsForCurrentThread(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
				break;
			case 2:
				this.setBlockBoundsForCurrentThread(0.25F, 0.25F, 0.5F, 0.75F, 0.75F, 1.0F);
				break;
			case 3:
				this.setBlockBoundsForCurrentThread(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.5F);
				break;
			case 4:
				this.setBlockBoundsForCurrentThread(0.5F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
				break;
			case 5:
				this.setBlockBoundsForCurrentThread(0.0F, 0.25F, 0.25F, 0.5F, 0.75F, 0.75F);
			}
		}
	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 */
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFTrophy();
	}

	/**
	 * only called by clickMiddleMouseButton, passed to inventory.setCurrentItem
	 */
	@Override
	public int idPicked(World world, int x, int y, int z) {
		// TODO(mite-port): MITE pick-block cannot carry the trophy type (damage), it always picks subtype 0
		return TFItems.trophy.itemID;
	}

	@Override
	public boolean canBeCarried() {
		return false;
	}

	/**
	 * Replaces onBlockHarvested + getDrops + getItemDropped: drop one trophy item whose damage is the skull type.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.isResponsiblePlayerInCreativeMode()) {
			info.setMetadata(info.getMetadata() | 8);
		}

		if ((info.getMetadata() & 8) != 0) {
			return 0;
		}

		TileEntity te = info.tile_entity;
		if (!(te instanceof TileEntityTFTrophy)) {
			return 0;
		}

		ItemStack stack = new ItemStack(TFItems.trophy.itemID, 1, ((TileEntityTFTrophy) te).getSkullType());
		return this.dropBlockAsEntityItem(info, stack);
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return Block.slowSand.getBlockTextureFromSide(side);
	}

	@Override
	public void registerIcons(IconRegister register) {
		// don't load anything
	}

}
