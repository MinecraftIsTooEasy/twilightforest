package twilightforest.structures.mushroomtower;

import net.minecraft.Block;
import twilightforest.structures.StructureTFDecorator;

public class StructureDecoratorMushroomTower extends StructureTFDecorator {

	
	
	public StructureDecoratorMushroomTower() 
	{
		this.blockID = Block.mushroomCapRed;
		this.blockMeta = 10;
		
		this.accentID = Block.mushroomCapRed;
		this.accentMeta = 14;
		
		this.fenceID = Block.fence;
		
		this.stairID = Block.stairsWoodSpruce;
		
		this.pillarID = Block.mushroomCapRed;
		this.pillarMeta = 10;
		
		this.floorID = Block.planks;
		this.floorMeta = 0;
	}
	
}
