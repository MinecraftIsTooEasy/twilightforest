package twilightforest.structures.stronghold;


import net.minecraft.Block;
import twilightforest.block.TFBlocks;
import twilightforest.structures.StructureTFDecorator;

public class StructureTFDecoratorStronghold extends StructureTFDecorator {

	
	public StructureTFDecoratorStronghold() 
	{
		this.blockID = TFBlocks.underBrick;
		this.blockMeta = 0;
		
		this.accentID = TFBlocks.underBrick;
		this.accentMeta = 3;
		
		this.fenceID = Block.cobblestoneWall;
		
		this.stairID = Block.stairsStoneBrick;
		
		this.pillarID = Block.stoneBrick;
		this.pillarMeta = 1;
		
		this.platformID = Block.stoneSingleSlab;
		this.platformMeta = 5 + 8;

		this.randomBlocks = new StructureTFKnightStones();
	}
}
