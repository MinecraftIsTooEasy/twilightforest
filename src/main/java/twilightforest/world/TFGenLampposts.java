package twilightforest.world;

import java.util.Random;

import twilightforest.block.TFBlocks;
import net.minecraft.Block;
import net.minecraft.World;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFWorldUtil;



public class TFGenLampposts extends TFGenerator {
    private static final int MAX_HANG = 8;

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		// we should start on a grass block
		if (world.getBlock(x, y - 1, z) == Block.grass) {
				// generate a height
				int height = 1 + rand.nextInt(4);
				boolean clear = true;
				
				// is it air or replaceable above our grass block
				for (int dy = 0; dy < height; dy++) {
					if (!world.isAirBlock(x, y + dy, z) && !TFBlockUtil.isReplaceable(world, x, y + dy, z)) {
						clear = false;
					}
				}
				
				// generate lamp
				if (clear) {
					for (int dy = 0; dy < height; dy++) {
						TFWorldUtil.setBlock(world, x, y + dy, z, Block.fence);
						TFWorldUtil.setBlock(world, x, y + height, z, TFBlocks.fireflyJar);
					}
				} 
				return clear;
		} else {
			return false;
		}
    }

}
