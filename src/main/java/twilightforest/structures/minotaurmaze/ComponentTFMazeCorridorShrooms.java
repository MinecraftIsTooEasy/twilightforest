package twilightforest.structures.minotaurmaze;

import java.util.Random;


import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;

public class ComponentTFMazeCorridorShrooms extends ComponentTFMazeCorridor {

	public ComponentTFMazeCorridorShrooms() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeCorridorShrooms(int i, int x, int y, int z, int rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		

		// mycelium & mushrooms
		for (int x = 1; x < 5; x++)
		{
			for (int z = 0; z < 5; z++)
			{
				if (rand.nextInt(2) > 0)
				{
					this.placeBlockAtCurrentPosition(world, Block.mycelium, 0, x, 0, z, sbb);
				}
				if (rand.nextInt(2) > 0)
				{
					this.placeBlockAtCurrentPosition(world, rand.nextBoolean() ? Block.mushroomRed : Block.mushroomBrown, 0, x, 1, z, sbb);
				}
			}
		}
		
		// brackets?
		Block mushType = rand.nextBoolean() ? Block.mushroomCapRed : Block.mushroomCapBrown;
		int mushY = rand.nextInt(4) + 1;
		int mushZ = rand.nextInt(4) + 1;
		this.placeBlockAtCurrentPosition(world, mushType, 15, 1, mushY - 1, mushZ, sbb);
		this.fillWithMetadataBlocks(world, sbb, 1, 1, mushZ, 1, mushY, mushZ, mushType, 10, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 1, mushY, mushZ - 1, 2, mushY, mushZ + 1, mushType, 14, null, 0, false);

		mushType = mushType == Block.mushroomCapBrown ? Block.mushroomCapRed : Block.mushroomCapBrown;
		mushY = rand.nextInt(4) + 1;
		mushZ = rand.nextInt(4) + 1;
		this.fillWithMetadataBlocks(world, sbb, 4, 1, mushZ, 4, mushY, mushZ, mushType, 10, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 3, mushY, mushZ - 1, 4, mushY, mushZ + 1, mushType, 14, null, 0, false);

		
		return true;
	}
}
