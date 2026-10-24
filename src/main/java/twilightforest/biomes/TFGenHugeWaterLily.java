package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Material;
import net.minecraft.World;
import net.minecraft.WorldGenerator;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFWorldUtil;


/**
 * Generate huge lily pads
 * 
 * @author Ben
 *
 */
public class TFGenHugeWaterLily extends WorldGenerator
{

	private Random rand = new Random();


    @Override
    public boolean generate(World world, Random random, int x, int y, int z)
    {
    	for (int i = 0; i < 4; i++) {
    		int dx = x + random.nextInt(8) - random.nextInt(8);
    		int dy = y + random.nextInt(4) - random.nextInt(4);
    		int dz = z + random.nextInt(8) - random.nextInt(8);

    		if (shouldPlacePadAt(world, dx, dy, dz)) {
    			TFWorldUtil.setBlock(world, dx, dy, dz, TFBlocks.hugeWaterLily);
    		}
    	}

        return true;
    }


	private boolean shouldPlacePadAt(World world, int dx, int dy, int dz) {
		return world.isAirBlock(dx, dy, dz) && world.getBlockMaterial(dx, dy - 1, dz) == Material.water;
	}
}
