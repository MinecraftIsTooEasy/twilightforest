package twilightforest.biomes;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.WorldGenBigTree;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenerator;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;
import twilightforest.world.TFGenCanopyOak;
import twilightforest.world.TFGenNoTree;

public class TFBiomeOakSavanna extends TFBiomeTwilightForest {

    public TFBiomeOakSavanna(int i) {
        super(i);

        getTFBiomeDecorator().canopyTreeGen = new TFGenCanopyOak();

        getTFBiomeDecorator().alternateCanopyChance = 0.8F;
        getTFBiomeDecorator().alternateCanopyGen = new TFGenNoTree();

        this.temperature = 0.9F;
        this.rainfall = 0.0F;

        getTFBiomeDecorator().setTreesPerChunk(1);
        getTFBiomeDecorator().setFlowersPerChunk(4);
        getTFBiomeDecorator().setGrassPerChunk(20);
    }

    /**
     * Oak trees all over
     */
    @Override
    public WorldGenerator getRandomWorldGenForTrees(Random random) {
        if (random.nextInt(10) == 0) {
            return new WorldGenBigTree(false);
        } else {
            return this.worldGeneratorTrees;
        }
    }

    /**
     * Oak savanna fern mix
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
        if (par1Random.nextInt(10) == 0) {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 2);
        } else if (par1Random.nextInt(10) == 0) {
            return new WorldGenTallGrass(TFBlocks.plant.blockID, BlockTFPlant.META_MAYAPPLE);
        } else {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
        }
    }

    // TODO(mite-port): func_150572_a (multi-colour flowers, plantNoise) has no MITE hook

    @Override
    public void decorate(World par1World, Random par2Random, int par3, int par4) {
        // TODO(mite-port): 2-tall grass (WorldGenDoublePlant / genTallFlowers) does not exist in 1.6.4

        super.decorate(par1World, par2Random, par3, par4);
    }
}
