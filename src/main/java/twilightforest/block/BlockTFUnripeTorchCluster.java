package twilightforest.block;

import java.util.Random;

import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class BlockTFUnripeTorchCluster extends BlockTFTrollRoot {

	private static final int RIPEN_THRESHHOLD = 6;

	protected BlockTFUnripeTorchCluster(int id) {
		super(id);

		this.setTextureName(TwilightForestMod.ID + ":unripe_torch_cluster");
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random rand) {
		if (super.updateTick(world, x, y, z, rand)) {
			return true;
		}

		if (world.getBlockLightValue(x, y, z) >= RIPEN_THRESHHOLD) {
			// ripen!
			world.setBlock(x, y, z, TFBlocks.trollBer.blockID);
			return true;
		}
		return false;
	}
}
