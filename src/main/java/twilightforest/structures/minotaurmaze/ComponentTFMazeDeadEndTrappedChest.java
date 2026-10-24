package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;

public class ComponentTFMazeDeadEndTrappedChest extends ComponentTFMazeDeadEndChest {

	public ComponentTFMazeDeadEndTrappedChest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ComponentTFMazeDeadEndTrappedChest(int i, int x, int y, int z, int rotation) {
		super(i, x, y, z, rotation);
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {		
		// normal chest room
		super.addComponentParts(world, rand, sbb);
		
		// add tripwire hooks
		this.placeBlockAtCurrentPosition(world, Block.tripWireSource, getHookMeta(3), 1, 1, 2, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tripWireSource, getHookMeta(1), 4, 1, 2, sbb);
		
		// add string
		this.placeBlockAtCurrentPosition(world, Block.tripWire, 0, 2, 1, 2, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tripWire, 0, 3, 1, 2, sbb);
		
		// TNT!
		this.placeBlockAtCurrentPosition(world, Block.tnt, 0, 0, 0, 2, sbb);
		this.placeBlockAtCurrentPosition(world, null, 0, 0, -1, 2, sbb);
		this.placeBlockAtCurrentPosition(world, null, 0, 1, -1, 2, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tnt, 0, 2, 0, 4, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tnt, 0, 3, 0, 4, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tnt, 0, 2, 0, 3, sbb);
		this.placeBlockAtCurrentPosition(world, Block.tnt, 0, 3, 0, 3, sbb);

		return true;
	}
	
	/**
	 * Gets the metadata necessary to make stairs facing the proper direction.
	 * 
	 * @param dir
	 * @return
	 */
	protected int getHookMeta(int dir) 
	{
		return(this.getCoordBaseMode() + dir) % 4;
	}
}
