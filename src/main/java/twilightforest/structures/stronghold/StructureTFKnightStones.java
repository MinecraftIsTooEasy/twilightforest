package twilightforest.structures.stronghold;

import java.util.Random;


import net.minecraft.Block;
import net.minecraft.StructurePieceBlockSelector;
import twilightforest.block.TFBlocks;

public class StructureTFKnightStones extends StructurePieceBlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
        if (!par5)
        {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
        else
        {
            this.selectedBlockId = TFBlocks.underBrick.blockID;
            float var6 = par1Random.nextFloat();

            if (var6 < 0.2F)
            {
                this.selectedBlockMetaData = 2;
            }
            else if (var6 < 0.5F)
            {
                this.selectedBlockMetaData = 1;
            }
            else
            {
                this.selectedBlockMetaData = 0;
            }
        }
	}

}
