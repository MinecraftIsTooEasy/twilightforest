package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import twilightforest.block.TFBlocks;

public class ComponentTFMazeDeadEndFountain extends ComponentTFMazeDeadEnd {

	public ComponentTFMazeDeadEndFountain() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeDeadEndFountain(int i, int x, int y, int z, int rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		
		// normal doorway
		super.addComponentParts(world, rand, sbb);
		
		// back wall brick
		this.fillWithMetadataBlocks(world, sbb, 1, 1, 4, 4, 4, 4, TFBlocks.mazestone, 1, null, 0, false);

		// water
		this.placeBlockAtCurrentPosition(world, Block.waterMoving, 0, 2, 3, 4, sbb);
		this.placeBlockAtCurrentPosition(world, Block.waterMoving, 0, 3, 3, 4, sbb);

		// receptacle
		this.placeBlockAtCurrentPosition(world, null, 0, 2, 0, 3, sbb);
		this.placeBlockAtCurrentPosition(world, null, 0, 3, 0, 3, sbb);
		
		return true;
	}
}
