package twilightforest.structures.darktower;

import java.util.List;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import net.minecraft.StructureComponent;
import twilightforest.structures.StructureTFComponent;
import twilightforest.structures.lichtower.ComponentTFTowerWing;

public class ComponentTFDarkTowerBalcony extends ComponentTFTowerWing
{
	
	public ComponentTFDarkTowerBalcony() {
		super();
		}

	protected ComponentTFDarkTowerBalcony(int i, int x, int y, int z, int direction) {
		super(i, x, y, z, 5, 5, direction);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void buildComponent(StructureComponent parent, List list, Random rand) 
	{
		if (parent != null && parent instanceof StructureTFComponent)
		{
			this.deco = ((StructureTFComponent)parent).deco;
		}
	}
	
	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {

		// make floor
		fillWithMetadataBlocks(world, sbb, 0, 0, 0, 2, 0, 4, deco.accentID, deco.accentMeta, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 0, 0, 1, 1, 0, 3, deco.blockID, deco.blockMeta, null, 0, false);
		
		fillWithMetadataBlocks(world, sbb, 0, 1, 0, 2, 1, 4, deco.fenceID, deco.fenceMeta, null, 0, false);

		this.placeBlockAtCurrentPosition(world, deco.accentID, deco.accentMeta, 2, 1, 0, sbb);
		this.placeBlockAtCurrentPosition(world, deco.accentID, deco.accentMeta, 2, 1, 4, sbb);

		// clear inside
		fillWithAir(world, sbb, 0, 1, 1, 1, 1, 3);
		
		return true;
	}


}
