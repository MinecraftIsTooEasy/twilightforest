package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import twilightforest.block.TFBlocks;

public class ComponentTFMazeRoomExit extends ComponentTFMazeRoom {

	public ComponentTFMazeRoomExit() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeRoomExit(int i, Random rand, int x, int y, int z) {
		super(i, rand, x, y, z);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
		super.addComponentParts(world, rand, sbb);

		// shaft down
		this.fillWithMetadataBlocks(world, sbb, 5, -5, 5, 10, 0, 10, TFBlocks.mazestone, 1, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 5, 1, 5, 10, 1, 10, TFBlocks.mazestone, 3, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 5, 2, 5, 10, 3, 10, Block.fenceIron, 0, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 5, 4, 5, 10, 4, 10, TFBlocks.mazestone, 3, null, 0, false);
		this.fillWithAir(world, sbb, 6, -5, 6, 9, 4, 9);

//		int var8 = this.getXWithOffset(0, 0);
//		int var9 = this.getYWithOffset(0);
//		int var10 = this.getZWithOffset(0, 0);

		//System.out.println("Drawing exit at " + var8 + ", " + var9 + ", " + var10);

		return true;
	}


}
