package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import twilightforest.block.TFBlocks;

public class ComponentTFMazeCorridorIronFence extends ComponentTFMazeCorridor {

	public ComponentTFMazeCorridorIronFence() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeCorridorIronFence(int i, int x, int y, int z, int rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		
		this.fillWithMetadataBlocks(world, sbb, 1, 4, 2, 4, 4, 3, TFBlocks.mazestone, 3, null, 0, false);
		this.fillWithMetadataBlocks(world, sbb, 1, 1, 2, 4, 3, 3, TFBlocks.mazestone, 2, null, 0, false);
		this.fillWithBlocks(world, sbb, 2, 1, 2, 3, 3, 3, Block.fenceIron, null, false);
		return true;
	}
}
