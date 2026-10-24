package twilightforest.structures;

import net.minecraft.Block;
import twilightforest.block.TFBlocks;

public class StructureTFDecoratorCastle extends StructureTFDecorator 
{

	public StructureTFDecoratorCastle() 
	{
		this.blockID = TFBlocks.castleBlock;
		this.blockMeta = 0;
		
		this.accentID = Block.blockNetherQuartz;
		this.accentMeta = 1;
		
		this.roofID = TFBlocks.castleBlock;
		this.roofMeta = 3;
		
		this.pillarID = Block.blockNetherQuartz;
		this.pillarMeta = 2;
		
		this.fenceID = Block.fence;
		this.fenceMeta = 0;
		
		this.stairID = Block.stairsNetherQuartz;

		this.randomBlocks = new StructureTFCastleBlocks();
	}

}
