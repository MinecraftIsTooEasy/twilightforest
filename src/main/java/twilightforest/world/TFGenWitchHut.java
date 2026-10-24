package twilightforest.world;

import java.util.Random;

import net.minecraft.MobSpawnerBaseLogic;
import net.minecraft.TileEntityMobSpawner;
import net.minecraft.Block;
import net.minecraft.World;
import twilightforest.entity.TFCreatures;
import twilightforest.entity.EntityTFSkeletonDruid;


public class TFGenWitchHut extends TFGenerator {

	/**
	 * Make a cute witch's hut
	 */
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z)
	{
		return generateTinyHut(world, rand, x, y, z);
	}
	
	/**
	 * Make the smallest size hut
	 */
	public boolean generateTinyHut(World world, Random rand, int x, int y, int z)
	{
		if (!isAreaSuitable(world, rand, x, y, z, 5, 7, 6))
		{
			return false;
		}
		
		// walls!
		
		setBlock(world, x + 1, y + 0, z + 1, randStone(rand, 1));
		setBlock(world, x + 2, y + 0, z + 1, randStone(rand, 1));
		setBlock(world, x + 3, y + 0, z + 1, randStone(rand, 1));
		setBlock(world, x + 5, y + 0, z + 1, randStone(rand, 1));
		setBlock(world, x + 0, y + 0, z + 2, Block.brick);
		setBlock(world, x + 1, y + 0, z + 2, Block.brick);
		setBlock(world, x + 5, y + 0, z + 2, randStone(rand, 1));
		setBlock(world, x + 0, y + 0, z + 3, Block.brick);
		setBlock(world, x + 5, y + 0, z + 3, randStone(rand, 1));
		setBlock(world, x + 0, y + 0, z + 4, Block.brick);
		setBlock(world, x + 1, y + 0, z + 4, Block.brick);
		setBlock(world, x + 5, y + 0, z + 4, randStone(rand, 1));
		setBlock(world, x + 1, y + 0, z + 5, randStone(rand, 1));
		setBlock(world, x + 2, y + 0, z + 5, randStone(rand, 1));
		setBlock(world, x + 3, y + 0, z + 5, randStone(rand, 1));
		setBlock(world, x + 5, y + 0, z + 5, randStone(rand, 1));

		setBlock(world, x + 1, y + 1, z + 1, randStone(rand, 2));
		setBlock(world, x + 3, y + 1, z + 1, randStone(rand, 2));
		setBlock(world, x + 5, y + 1, z + 1, randStone(rand, 2));
		setBlock(world, x + 0, y + 1, z + 2, Block.brick);
		setBlock(world, x + 1, y + 1, z + 2, Block.brick);
		setBlock(world, x + 5, y + 1, z + 2, randStone(rand, 2));
		setBlock(world, x + 0, y + 1, z + 3, Block.brick);
		setBlock(world, x + 0, y + 1, z + 4, Block.brick);
		setBlock(world, x + 1, y + 1, z + 4, Block.brick);
		setBlock(world, x + 5, y + 1, z + 4, randStone(rand, 2));
		setBlock(world, x + 1, y + 1, z + 5, randStone(rand, 2));
		setBlock(world, x + 3, y + 1, z + 5, randStone(rand, 2));
		setBlock(world, x + 5, y + 1, z + 5, randStone(rand, 2));

		setBlock(world, x + 1, y + 2, z + 1, randStone(rand, 3));
		setBlock(world, x + 2, y + 2, z + 1, randStone(rand, 3));
		setBlock(world, x + 3, y + 2, z + 1, randStone(rand, 3));
		setBlock(world, x + 4, y + 2, z + 1, randStone(rand, 3));
		setBlock(world, x + 5, y + 2, z + 1, randStone(rand, 3));
		setBlock(world, x + 0, y + 2, z + 2, Block.brick);
		setBlock(world, x + 1, y + 2, z + 2, Block.brick);
		setBlock(world, x + 5, y + 2, z + 2, randStone(rand, 3));
		setBlock(world, x + 0, y + 2, z + 3, Block.brick);
		setBlock(world, x + 5, y + 2, z + 3, randStone(rand, 3));
		setBlock(world, x + 0, y + 2, z + 4, Block.brick);
		setBlock(world, x + 1, y + 2, z + 4, Block.brick);
		setBlock(world, x + 5, y + 2, z + 4, randStone(rand, 1));
		setBlock(world, x + 1, y + 2, z + 5, randStone(rand, 3));
		setBlock(world, x + 2, y + 2, z + 5, randStone(rand, 3));
		setBlock(world, x + 3, y + 2, z + 5, randStone(rand, 3));
		setBlock(world, x + 4, y + 2, z + 5, randStone(rand, 3));
		setBlock(world, x + 5, y + 2, z + 5, randStone(rand, 3));

		setBlock(world, x + 0, y + 3, z + 2, Block.brick);
		setBlock(world, x + 0, y + 3, z + 3, Block.brick);
		setBlock(world, x + 0, y + 3, z + 4, Block.brick);
		setBlock(world, x + 2, y + 3, z + 1, randStone(rand, 4));
		setBlock(world, x + 3, y + 3, z + 1, randStone(rand, 4));
		setBlock(world, x + 4, y + 3, z + 1, randStone(rand, 4));
		setBlock(world, x + 2, y + 3, z + 5, randStone(rand, 4));
		setBlock(world, x + 3, y + 3, z + 5, randStone(rand, 4));
		setBlock(world, x + 4, y + 3, z + 5, randStone(rand, 4));

		setBlock(world, x + 0, y + 4, z + 3, Block.brick);
		setBlock(world, x + 3, y + 4, z + 1, randStone(rand, 5));
		setBlock(world, x + 3, y + 4, z + 5, randStone(rand, 5));

		setBlock(world, x + 0, y + 5, z + 3, Block.brick);

		setBlock(world, x + 0, y + 6, z + 3, Block.brick);

		// roof!
		setBlockAndMetadata(world, x + 0, y + 2, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 0, y + 2, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 0, y + 2, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 0, y + 2, z + 6, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 3, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 6, y + 2, z + 6, Block.woodDoubleSlab, 0);
		
		setBlockAndMetadata(world, x + 1, y + 3, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 3, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 3, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 3, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 3, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 3, z + 6, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 3, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 3, z + 6, Block.woodDoubleSlab, 0);
		
		setBlockAndMetadata(world, x + 1, y + 4, z + 0, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 3, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 4, z + 6, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 1, y + 4, z + 6, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 4, z + 0, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 3, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 4, z + 6, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 5, y + 4, z + 6, Block.woodSingleSlab, 0);

		setBlockAndMetadata(world, x + 2, y + 5, z + 0, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 5, z + 1, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 5, z + 0, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 5, z + 1, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 2, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 3, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 4, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 5, z + 6, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 5, z + 5, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 2, y + 5, z + 6, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 5, z + 5, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 4, y + 5, z + 6, Block.woodSingleSlab, 0);

		setBlockAndMetadata(world, x + 3, y + 6, z + 0, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 6, z + 1, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 6, z + 2, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 6, z + 4, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 6, z + 5, Block.woodDoubleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 6, z + 6, Block.woodDoubleSlab, 0);

		setBlockAndMetadata(world, x + 3, y + 7, z + 0, Block.woodSingleSlab, 0);
		setBlockAndMetadata(world, x + 3, y + 7, z + 6, Block.woodSingleSlab, 0);

		// fire in chimney!
		setBlock(world, x + 1, y - 1, z + 3, Block.netherrack);
		setBlock(world, x + 1, y + 0, z + 3, Block.fire); // oh god the roof!
		
		// skeleton spawner!
        world.setBlock(x + 3, y + 1, z + 3, Block.mobSpawner.blockID, 0, 2);
        TileEntityMobSpawner ms = (TileEntityMobSpawner)world.getBlockTileEntity(x + 3, y + 1, z + 3);
        if (ms != null) {
        	MobSpawnerBaseLogic spawnerLogic = ms.getSpawnerLogic();
        	if (spawnerLogic != null) {
        		spawnerLogic.setMobID(TFCreatures.getSpawnerNameFor(EntityTFSkeletonDruid.class));
        	}
        }

		return true;
	}
	
}
