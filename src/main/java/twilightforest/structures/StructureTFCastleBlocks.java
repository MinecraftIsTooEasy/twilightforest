package twilightforest.structures;

import java.util.Random;

import net.minecraft.StructurePieceBlockSelector;
import twilightforest.block.TFBlocks;

public class StructureTFCastleBlocks extends StructurePieceBlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int x, int y, int z, boolean isWall) {
        if (!isWall)
        {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
        else
        {
            this.selectedBlockId = TFBlocks.castleBlock.blockID;
            float randFloat = par1Random.nextFloat();

            if (randFloat < 0.1F)
            {
                this.selectedBlockMetaData = 1;
            }
            else if (randFloat < 0.2F)
            {
                this.selectedBlockMetaData = 2;
            }
            else
            {
                this.selectedBlockMetaData = 0;
            }
        }
	}

}
