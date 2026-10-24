package twilightforest.world;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Material;
import net.minecraft.IBlockAccess;
import net.minecraft.World;
import twilightforest.compat.TFWorldUtil;

public class TFGenFireJet extends TFGenerator
{
    private Block plantBlockId;
    private int plantBlockMeta;

    public TFGenFireJet(Block fireJet, int meta)
    {
        this.plantBlockId = fireJet;
        this.plantBlockMeta = meta;
    }

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
        for (int var6 = 0; var6 < 4; ++var6)
        {
            int dx = x + rand.nextInt(8) - rand.nextInt(8);
            int dy = y + rand.nextInt(4) - rand.nextInt(4);
            int dz = z + rand.nextInt(8) - rand.nextInt(8);

            if (world.isAirBlock(dx, dy, dz) && world.canBlockSeeTheSky(dx, dy, dz) && materialAt(world, dx, dy - 1, dz) == Material.grass
            		 && materialAt(world, dx + 1, dy - 1, dz) == Material.grass && materialAt(world, dx - 1, dy - 1, dz) == Material.grass
            		 && materialAt(world, dx, dy - 1, dz + 1) == Material.grass && materialAt(world, dx, dy - 1, dz - 1) == Material.grass)
            {
            	// jet
            	TFWorldUtil.setBlock(world, dx, dy - 1, dz, this.plantBlockId, this.plantBlockMeta, 0);

            	// create reservoir with stone walls
            	for (int rx = -2; rx <= 2; rx++)
            	{
                	for (int rz = -2; rz <= 2; rz++)
                	{
                		if ((rx == 1 || rx == 0 || rx == -1) && (rz == 1 || rz == 0 || rz == -1))
                		{
                        	// lava reservoir
                    		TFWorldUtil.setBlock(world, dx + rx, dy - 2, dz + rz, Block.lavaMoving, 0, 0);
                		}
                		else if (materialAt(world, dx + rx, dy - 2, dz + rz) != Material.lava)
                		{
                			// only stone where there is no lava
                    		TFWorldUtil.setBlock(world, dx + rx, dy - 2, dz + rz, Block.stone, 0, 0);
                		}
                		TFWorldUtil.setBlock(world, dx + rx, dy - 3, dz + rz, Block.stone, 0, 0);
                	}
            	}
            }
        }

        return true;

	}

	private static Material materialAt(IBlockAccess world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b == null ? Material.air : b.blockMaterial;
	}
}
