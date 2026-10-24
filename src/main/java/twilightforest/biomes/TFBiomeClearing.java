package twilightforest.biomes;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenerator;

public class TFBiomeClearing extends TFBiomeBase {

    public TFBiomeClearing(int i) {
        super(i);

        this.temperature = 0.8F;
        this.rainfall = 0.4F;

//        this.rootHeight = 0.01F;
//        this.heightVariation = 0F;

        getTFBiomeDecorator().canopyPerChunk = -999;
        getTFBiomeDecorator().setTreesPerChunk(-999);

        getTFBiomeDecorator().setFlowersPerChunk(4);
        getTFBiomeDecorator().setGrassPerChunk(10);
    }

    /**
     * No ferns
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
        return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
    }

}
