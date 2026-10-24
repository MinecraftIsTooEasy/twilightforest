package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.ColorizerFoliage;
import net.minecraft.IBlockAccess;

public class BlockTFGiantLeaves extends BlockTFGiantBlock {

	public BlockTFGiantLeaves(int id) {
		// non-opaque full cube (1.7.10 isOpaqueCube() == false)
		super(id, Block.leaves, new BlockConstants().setNeverHidesAdjacentFaces());

		// balance (MITE_BALANCE B7): hardness 0.8, harvest level 0, resistance 1
		this.setHardness(0.8F);
		this.setResistance(1.0F);
		this.setMinHarvestLevel(0);
		this.setLightOpacity(1);
	}

	@Override
	public int getBlockColor() {
		double d0 = 0.5D;
		double d1 = 1.0D;
		return ColorizerFoliage.getFoliageColor(d0, d1);
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		return ColorizerFoliage.getFoliageColorBasic();
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int red = 0;
		int grn = 0;
		int blu = 0;

		for (int dz = -1; dz <= 1; ++dz) {
			for (int dx = -1; dx <= 1; ++dx) {
				int nearbyColor = world.getBiomeGenForCoords(x + dx, z + dz).getBiomeFoliageColor();
				red += (nearbyColor & 16711680) >> 16;
				grn += (nearbyColor & 65280) >> 8;
				blu += nearbyColor & 255;
			}
		}

		return (red / 9 & 255) << 16 | (grn / 9 & 255) << 8 | blu / 9 & 255;
	}

	/**
	 * Only the faces on the outside of the 4x4x4 cube are rendered. Args: blockAccess, x, y, z (neighbour), side
	 */
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		switch (side) {
		case 0:
			return (y & 3) == 3;
		case 1:
			return (y & 3) == 0;
		case 2:
			return (z & 3) == 3;
		case 3:
			return (z & 3) == 0;
		case 4:
			return (x & 3) == 3;
		case 5:
			return (x & 3) == 0;
		}

		return super.shouldSideBeRendered(world, x, y, z, side);
	}
}
