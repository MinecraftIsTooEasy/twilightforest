package twilightforest.structures.trollcave;

import java.util.List;
import java.util.Random;

import net.minecraft.NBTTagCompound;
import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import net.minecraft.StructureComponent;
import twilightforest.TFTreasure;
import twilightforest.block.TFBlocks;
import twilightforest.entity.EntityTFArmoredGiant;
import twilightforest.entity.EntityTFGiantMiner;
import twilightforest.structures.StructureTFComponent;

public class ComponentTFTrollVault extends StructureTFComponent {

	public ComponentTFTrollVault() { }
	
	public ComponentTFTrollVault(int index, int x, int y, int z) {
		super(index);
		this.setCoordBaseMode(0);

		
		// adjust x, y, z
    	x = (x >> 2) << 2;
    	y = (y / 4) * 4;
    	z = (z >> 2) << 2;
    	
		// spawn list!
		this.spawnListIndex = -1;
    	
		this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -8, 0, -8, 12, 12, 12, 0);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildComponent(StructureComponent parent, List list, Random rand) {
		;
	}
	
	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
		// make walls
		this.fillWithMetadataBlocks(world, sbb, 0, 0, 0, 11, 11, 11, TFBlocks.giantObsidian, 0, TFBlocks.giantObsidian, 0, false); 
		
		// clear inside
		this.fillWithAir(world, sbb, 4, 4, 4, 7, 7, 7);
		
		// cobblestone platform
		this.fillWithMetadataBlocks(world, sbb, 5, 5, 5, 6, 5, 6, Block.cobblestone, 0, Block.cobblestone, 0, false); 
		
		// chests
		this.placeBlockAtCurrentPosition(world, Block.chest, 0, 5, 6, 5, sbb);
		this.placeTreasureAtCurrentPosition(world, rand, 5, 6, 6, TFTreasure.troll_vault, false, sbb);
		
		this.placeTreasureAtCurrentPosition(world, rand, 6, 6, 5, TFTreasure.troll_garden, true, sbb);
		this.placeBlockAtCurrentPosition(world, Block.chestTrapped, 0, 6, 6, 6, sbb);

		return true;
	}

}
