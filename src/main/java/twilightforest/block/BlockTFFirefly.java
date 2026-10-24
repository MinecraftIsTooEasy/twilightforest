package twilightforest.block;

import java.util.Random;

import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.tileentity.TileEntityTFFirefly;

public class BlockTFFirefly extends BlockTFCritter {

	public static int sprFirefly = 4;

	public static Random rand = new Random();

	protected BlockTFFirefly(int id) {
		super(id);
		// 1.7.10 getLightValue(IBlockAccess,..) returned 15 for every metadata
		this.setLightValue(1.0F);
	}

	/**
	 * How often do we check for incorrect lighting on fireflies, etc.
	 */
	public int tickRate() {
		return 50 + rand.nextInt(50);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFFirefly();
	}

	/**
	 * Ticks the block if it's been scheduled
	 * Check the lighting and make the world relight it if it's incorrect.
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote && world.getBlockLightValue(x, y, z) < 12) {
			world.markBlockForUpdate(x, y, z); // do we need this now?
			// do another update to check that we got it right
			world.scheduleBlockUpdate(x, y, z, this.blockID, tickRate());
		}
		return false;
	}
}
