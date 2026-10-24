package twilightforest.structures.icetower;

import net.minecraft.Block;
import twilightforest.block.TFBlocks;
import twilightforest.structures.StructureTFDecorator;

public class StructureDecoratorIceTower extends StructureTFDecorator {

	public StructureDecoratorIceTower() 
	{
		this.blockID = TFBlocks.auroraBlock;
		this.blockMeta = 0;
		
		this.accentID = Block.planks;
		this.accentMeta = 2;
		
		this.fenceID = Block.fence;
		
		this.stairID = Block.stairsWoodBirch;
		
		this.pillarID = TFBlocks.auroraPillar;
		this.pillarMeta = 0;
		
		this.platformID = Block.woodSingleSlab;
		this.platformMeta = 2;

		this.floorID = Block.planks;
		this.floorMeta = 2;

		this.randomBlocks = new StructureTFAuroraBricks();
	}

}
