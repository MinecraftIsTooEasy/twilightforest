package twilightforest.block;

import java.util.Random;

import net.minecraft.IBlockAccess;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.tileentity.TileEntityTFMoonworm;

public class BlockTFMoonworm extends BlockTFCritter {

	public static int sprMoonworm = 52;

	protected BlockTFMoonworm(int id) {
		super(id);
		// 1.7.10 getLightValue(IBlockAccess,..) returned 14 for every metadata ((int)(15 * 0.94) == 14)
		this.setLightValue(0.94F);
	}

	/**
	 * How often do we check for incorrect lighting on fireflies, etc.
	 */
	public int tickRate() {
		return 50;
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		int facing = world.getBlockMetadata(x, y, z) & 7;
		float wide = 0.25F;
		if (facing == 1) {
			setBlockBoundsForCurrentThread(0.0F, 0.25F, 0.5F - wide, wide, 0.75F, 0.5F + wide);
		} else if (facing == 2) {
			setBlockBoundsForCurrentThread(1.0F - wide, 0.25F, 0.5F - wide, 1.0F, 0.75F, 0.5F + wide);
		} else if (facing == 3) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.25F, 0.0F, 0.5F + wide, 0.75F, wide);
		} else if (facing == 4) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.25F, 1.0F - wide, 0.5F + wide, 0.75F, 1.0F);
		} else if (facing == 5) {
			setBlockBoundsForCurrentThread(0.5F - wide, 0.0F, 0.25F, 0.5F + wide, wide, 0.75F);
		} else if (facing == 6) {
			setBlockBoundsForCurrentThread(0.5F - wide, 1.0F - wide, 0.25F, 0.5F + wide, 1.0F, 0.75F);
		} else {
			float f1 = 0.1F;
			setBlockBoundsForCurrentThread(0.5F - f1, 0.0F, 0.5F - f1, 0.5F + f1, 0.6F, 0.5F + f1);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFMoonworm();
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		if (world.getBlock(x, y, z) == this) {
			world.scheduleBlockUpdate(x, y, z, this.blockID, tickRate());
		}
	}

	/**
	 * Ticks the block if it's been scheduled
	 * Check the lighting and make the world relight it if it's incorrect.
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (world.getBlockLightValue(x, y, z) < 12) {
			// do another update to check that we got it right
			world.scheduleBlockUpdate(x, y, z, this.blockID, tickRate());
		}
		return false;
	}

	/**
	 * Don't drop anything when it loses its support
	 */
	@Override
	public boolean onNotLegal(World world, int x, int y, int z, int metadata) {
		world.setBlockToAir(x, y, z);
		return true;
	}

	/**
	 * Don't drop anything at all (1.7.10 quantityDropped returned 0)
	 */
	@Override
	public int dropBlockAsEntityItem(net.minecraft.BlockBreakInfo info) {
		return 0;
	}

}
