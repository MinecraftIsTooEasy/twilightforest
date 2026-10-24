package twilightforest.world;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;

import twilightforest.block.TFBlocks;

public class TFGenBigMushgloom extends TFGenerator {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int height = 3 + rand.nextInt(2) + rand.nextInt(2);
		
		if (!this.isAreaSuitable(world, rand, x - 1, y, z - 1, 3, height, 3)) {
			return false;
		}
		
        Block blockUnder = world.getBlock(x, y - 1, z);
        if (blockUnder != Block.dirt && blockUnder != Block.grass && blockUnder != Block.mycelium) {
            return false;
        }
        
        // generate!
        for (int dy = 0; dy < height - 2; dy++) {
            this.setBlockAndMetadata(world, x, y + dy, z, TFBlocks.hugeGloomBlock, 10);
        }
         
		makeMushroomCap(world, x, z, y + (height - 2));
		if (rand.nextBoolean()) {
			makeMushroomCap(world, x, z, y + (height - 1));
		}
		
		return true;
	}

	private void makeMushroomCap(World world, int x, int z, int dy) {
		this.setBlockAndMetadata(world, x - 1, dy, z - 1, TFBlocks.hugeGloomBlock, 1);
        this.setBlockAndMetadata(world, x + 0, dy, z - 1, TFBlocks.hugeGloomBlock, 2);
        this.setBlockAndMetadata(world, x + 1, dy, z - 1, TFBlocks.hugeGloomBlock, 3);
        this.setBlockAndMetadata(world, x - 1, dy, z + 0, TFBlocks.hugeGloomBlock, 4);
        this.setBlockAndMetadata(world, x + 0, dy, z + 0, TFBlocks.hugeGloomBlock, 5);
        this.setBlockAndMetadata(world, x + 1, dy, z + 0, TFBlocks.hugeGloomBlock, 6);
        this.setBlockAndMetadata(world, x - 1, dy, z + 1, TFBlocks.hugeGloomBlock, 7);
        this.setBlockAndMetadata(world, x + 0, dy, z + 1, TFBlocks.hugeGloomBlock, 8);
        this.setBlockAndMetadata(world, x + 1, dy, z + 1, TFBlocks.hugeGloomBlock, 9);
	}

}
