package twilightforest.structures.icetower;

import java.util.Random;

import net.minecraft.StructurePieceBlockSelector;
import twilightforest.block.TFBlocks;

public class StructureTFAuroraBricks extends StructurePieceBlockSelector {

	@Override
	public void selectBlocks(Random par1Random, int x, int y, int z, boolean wall) {
        if (!wall)
        {
            this.selectedBlockId = 0;
            this.selectedBlockMetaData = 0;
        }
        else
        {
            this.selectedBlockId = TFBlocks.auroraBlock.blockID;
            this.selectedBlockMetaData = Math.abs(x + z) % 16;
        }
	}

}
