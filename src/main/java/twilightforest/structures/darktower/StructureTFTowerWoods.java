package twilightforest.structures.darktower;

import java.util.Random;

import net.minecraft.StructurePieceBlockSelector;
import twilightforest.block.TFBlocks;

public class StructureTFTowerWoods extends StructurePieceBlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int x, int y, int z, boolean isWall) {
        if (!isWall)
        {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
        else
        {
            this.selectedBlockId = TFBlocks.towerWood.blockID;
            float randFloat = par1Random.nextFloat();

            if (randFloat < 0.1F)
            {
                this.selectedBlockMetaData = 2;
            }
            else if (randFloat < 0.2F)
            {
                this.selectedBlockMetaData = 3;
            }
            else if (randFloat < 0.225F)
            {
                this.selectedBlockMetaData = 4;
            }
            else
            {
                this.selectedBlockMetaData = 0;
            }
        }
	}

}
