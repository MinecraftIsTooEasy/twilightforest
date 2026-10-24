package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.WorldGenBigTree;
import net.minecraft.WorldGenShrub;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenerator;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;


public class TFBiomeTwilightForestVariant extends TFBiomeBase {

	public TFBiomeTwilightForestVariant(int i) {
		super(i);
		
		this.temperature = 0.7F;
		this.rainfall = 0.8F;
		
		getTFBiomeDecorator().setTreesPerChunk(25);
		getTFBiomeDecorator().setGrassPerChunk(15);
		getTFBiomeDecorator().setFlowersPerChunk(8);
	}

	
    /**
     * Occasional shrub, no birches
     */
    @Override
    public WorldGenerator getRandomWorldGenForTrees(Random random)
    {
        if(random.nextInt(5) == 0)
        {
        	return new WorldGenShrub(3, 0);
        }
        else if(random.nextInt(10) == 0)
        {
            return new WorldGenBigTree(false);
        } 
        else
        {
            return worldGeneratorTrees;
        }
    }
	
    /**
     * EVEN MOAR FERNZ!
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random)
    {
        if (par1Random.nextInt(4) != 0)
        {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 2);
        }
        else if (par1Random.nextBoolean())
        {
            return new WorldGenTallGrass(TFBlocks.plant.blockID, BlockTFPlant.META_MAYAPPLE);
        }
        else
        {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
        }
    }
    
    // TODO(mite-port): 1.7.10 decorate() added 7 patches of tall ferns (WorldGenDoublePlant); MITE 1.6.4 has no double plants.

}
