package twilightforest.world;

import java.util.Random;

import twilightforest.block.TFBlocks;
import net.minecraft.Material;
import net.minecraft.Block;
import net.minecraft.IBlockAccess;
import net.minecraft.World;
import twilightforest.compat.TFWorldUtil;



public class TFGenHangingLamps extends TFGenerator
{
    private static final int MAX_HANG = 8;

	@Override
	public boolean generate(World par1World, Random par2Random, int x, int y, int z)
    {
        // this must be an air block, surrounded by air
        if (par1World.isAirBlock(x, y, z) && TFGenerator.surroundedByAir(par1World, x, y, z)) {
            // there should be leaves or wood within 12 blocks above
        	if (areLeavesAbove(par1World, x, y, z)) {
        		// we need to be at least 4 above ground
            	if (isClearBelow(par1World, x, y, z)) {
            		// generate lamp
            		TFWorldUtil.setBlock(par1World, x, y, z, TFBlocks.fireflyJar);
            		
            		for (int cy = 1; cy < MAX_HANG; cy++) {
            			Material above = materialAt(par1World, x, y + cy, z);
            			if (above.isSolid() || above == Material.tree_leaves) {
            				break;
            			} else {
                    		TFWorldUtil.setBlock(par1World, x, y + cy, z, Block.fence);
            			}
            		}
            	}
        	}

        }

        return false;
    }

	private boolean areLeavesAbove(World par1World, int x, int y, int z) {
		boolean areLeavesAbove = false;
		for (int cy = 1; cy < MAX_HANG; cy++) {
			Material above = materialAt(par1World, x, y + cy, z);
			if (above.isSolid() || above == Material.tree_leaves) {
				areLeavesAbove = true;
			}
		}
		return areLeavesAbove;
	}

	private boolean isClearBelow(World par1World, int x, int y, int z) {
		boolean isClearBelow = true;
		for (int cy = 1; cy < 4; cy++) {
			if (par1World.isBlockTopFlatAndSolid(x, y - cy, z)) {
				isClearBelow = false;
			}
		}
		return isClearBelow;
	}

	private static Material materialAt(IBlockAccess world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		return b == null ? Material.air : b.blockMaterial;
	}
}
