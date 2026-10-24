package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;

public class ComponentTFMazeDeadEndFountainLava extends ComponentTFMazeDeadEndFountain {
	
	public ComponentTFMazeDeadEndFountainLava() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeDeadEndFountainLava(int i, int x, int y, int z, int rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		
		// normal fountain
		super.addComponentParts(world, rand, sbb);
		
		// remove water
		this.placeBlockAtCurrentPosition(world, null, 0, 2, 3, 4, sbb);
		this.placeBlockAtCurrentPosition(world, null, 0, 3, 3, 4, sbb);
		
		// lava instead of water
		this.placeBlockAtCurrentPosition(world, Block.lavaMoving, 0, 2, 3, 4, sbb);
		this.placeBlockAtCurrentPosition(world, Block.lavaMoving, 0, 3, 3, 4, sbb);
		
		return true;
	}


}
