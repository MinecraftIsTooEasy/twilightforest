package twilightforest.world;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.World;
import net.minecraft.WorldGenerator;
import twilightforest.compat.TFWorldUtil;

import twilightforest.block.TFBlocks;


/**
 * Generate huge lily pads
 * 
 * @author Ben
 *
 */
public class TFGenHugeLilyPad extends WorldGenerator
{

	private Random rand = new Random();


    @Override
    public boolean generate(World world, Random random, int x, int y, int z)
    {
    	for (int i = 0; i < 10; i++) {
    		int dx = x + random.nextInt(8) - random.nextInt(8);
    		int dy = y + random.nextInt(4) - random.nextInt(4);
    		int dz = z + random.nextInt(8) - random.nextInt(8);

    		dx = (dx >> 1) << 1;
    		dz = (dz >> 1) << 1;

    		if (shouldPlacePadAt(world, dx, dy, dz)) {
    			// this seems like a difficult way to generate 2 pseudorandom bits
    			rand .setSeed(8890919293L);
    			rand.setSeed((dx * rand.nextLong()) ^ (dz * rand.nextLong()) ^ 8890919293L);
    			int orient = rand.nextInt(4) << 2;

    			TFWorldUtil.setBlock(world, dx, dy, dz, TFBlocks.hugeLilyPad, 0 | orient, 2);
    			TFWorldUtil.setBlock(world, dx + 1, dy, dz, TFBlocks.hugeLilyPad, 1 | orient, 2);
    			TFWorldUtil.setBlock(world, dx + 1, dy, dz + 1, TFBlocks.hugeLilyPad, 2 | orient, 2);
    			TFWorldUtil.setBlock(world, dx, dy, dz + 1, TFBlocks.hugeLilyPad, 3 | orient, 2);
    		}
    	}

        return true;
    }


	private boolean shouldPlacePadAt(World world, int dx, int dy, int dz) {
		return world.isAirBlock(dx, dy, dz) && isWater(world, dx, dy - 1, dz)
				&& world.isAirBlock(dx + 1, dy, dz) && isWater(world, dx + 1, dy - 1, dz)
				&& world.isAirBlock(dx, dy, dz + 1) && isWater(world, dx, dy - 1, dz + 1)
				&& world.isAirBlock(dx + 1, dy, dz + 1) && isWater(world, dx + 1, dy - 1, dz + 1);
	}

	private static boolean isWater(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b != null && b.blockMaterial == Material.water;
	}
}
