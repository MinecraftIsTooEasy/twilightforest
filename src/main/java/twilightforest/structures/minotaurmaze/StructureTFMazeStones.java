package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.StructurePieceBlockSelector;

import twilightforest.block.TFBlocks;

public class StructureTFMazeStones extends StructurePieceBlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean wall) {
        if (!wall)
        {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
        else
        {
            this.selectedBlockId = TFBlocks.mazestone.blockID;
            float rf = par1Random.nextFloat();

            if (rf < 0.2F)
            {
                this.selectedBlockMetaData = 5;
            }
            else if (rf < 0.5F)
            {
                this.selectedBlockMetaData = 4;
            }
            else
            {
                this.selectedBlockMetaData = 1;
            }
        }
	}

}
