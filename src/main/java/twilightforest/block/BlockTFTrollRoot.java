package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/**
 * Troll root: hangs from the ceiling of troll caves. Drops nothing when broken, but can be sheared (MITE: shears
 * right click drops the block as itself, which replaces the 1.7.10 IShearable interface).
 */
public class BlockTFTrollRoot extends Block {

	protected BlockTFTrollRoot(int id) {
		super(id, Material.plants, new BlockConstants().setNotAlwaysLegal().setNeverHidesAdjacentFaces());
		this.setTickRandomly(true);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setStepSound(soundGrassFootstep);

		this.setTextureName(TwilightForestMod.ID + ":troll_root");
	}

	/**
	 * Can this block stay at this position (replaces canBlockStay / canPlaceBlockAt, see MITE legality system).
	 */
	@Override
	public boolean isLegalAt(World world, int x, int y, int z, int metadata) {
		return canPlaceRootBelow(world, x, y + 1, z);
	}

	public static boolean canPlaceRootBelow(World world, int x, int y, int z) {
		Block blockAbove = world.getBlock(x, y, z);

		return blockAbove != null && (blockAbove.blockMaterial == Material.stone || blockAbove == TFBlocks.trollVidr || blockAbove == TFBlocks.trollBer || blockAbove == TFBlocks.unripeTrollBer);
	}

	/** Not a solid block: no collision, not a standard form cube (constant per metadata, called from the Block ctor). */
	@Override
	public boolean isSolid(boolean[] is_solid, int metadata) {
		return false;
	}

	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	/**
	 * Cross-shaped plant render.
	 */
	@Override
	public int getRenderType() {
		return 1;
	}

	/**
	 * 1.7.10 quantityDropped was 0: roots drop nothing, except when sheared / picked up as itself.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.wasSilkHarvested() || info.wasSelfDropped()) {
			return this.dropBlockAsItself(info);
		}
		return 0;
	}
}
