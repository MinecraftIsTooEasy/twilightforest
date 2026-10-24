package twilightforest.biomes;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.MathHelper;
import net.minecraft.World;
import net.minecraft.WorldGenBigTree;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenVines;
import net.minecraft.WorldGenerator;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;
import twilightforest.world.TFGenLargeRainboak;
import twilightforest.world.TFGenSmallRainboak;
import twilightforest.world.TFWorld;

public class TFBiomeEnchantedForest extends TFBiomeBase {

    Random colorRNG;

    public TFBiomeEnchantedForest(int i) {
        super(i);
        colorRNG = new Random();

        getTFBiomeDecorator().setGrassPerChunk(12);
        getTFBiomeDecorator().setFlowersPerChunk(8);
    }

    /**
     * Provides the basic grass color; MITE has no coordinate argument, so this uses the biome centre colour.
     */
    @Override
    public int getBiomeGrassColor() {
        // TODO(mite-port): position dependent colour needs a colorMultiplier mixin
        return (super.getBiomeGrassColor() & 0xFFFF00) + 128;
    }

    @Override
    public int getBiomeFoliageColor() {
        // TODO(mite-port): position dependent colour needs a colorMultiplier mixin
        return (super.getBiomeFoliageColor() & 0xFFFF00) + 128;
    }

    /**
     * Position-aware variant (1.7.10 signature), kept for a colour mixin.
     */
    public int getBiomeGrassColor(int x, int y, int z) {
        return (super.getBiomeGrassColor() & 0xFFFF00) + getEnchantedColor(x, z);
    }

    public int getBiomeFoliageColor(int x, int y, int z) {
        return (super.getBiomeFoliageColor() & 0xFFFF00) + getEnchantedColor(x, z);
    }

    /**
     * Find a number between 0 and 255.  currently returns concentric circles from the biome center
     */
    private int getEnchantedColor(int x, int z) {
        // center of the biome is at % 256 - 8
        int cx = 256 * Math.round((x - 8) / 256F) + 8;
        int cz = 256 * Math.round((z - 8) / 256F) + 8;

        int dist = (int) MathHelper.sqrt_float((cx - x) * (cx - x) + (cz - z) * (cz - z));
        int color = dist * 64;
        color %= 512;

        if (color > 255) {
            color = 511 - color;
        }

        color = 255 - color;

        int randomFlicker = colorRNG.nextInt(32) - 16;

        if (0 < color + randomFlicker && color + randomFlicker > 255) {
            color += randomFlicker;
        }

        return color;
    }

    /**
     * Occasional shrub, no big trees
     */
    @Override
    public WorldGenerator getRandomWorldGenForTrees(Random random) {
        if (random.nextInt(15) == 0) {
            return new TFGenSmallRainboak();
        } else if (random.nextInt(50) == 0) {
            return new TFGenLargeRainboak();
        } else if (random.nextInt(5) == 0) {
            return this.worldGeneratorForest; // birch
        } else if (random.nextInt(10) == 0) {
            return new WorldGenBigTree(false);
        } else {
            return this.worldGeneratorTrees;
        }
    }

    /**
     * EVEN MOAR FERNZ!
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
        if (par1Random.nextInt(3) > 0) {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 2);
        } else if (par1Random.nextInt(3) == 0) {
            return new WorldGenTallGrass(TFBlocks.plant.blockID, BlockTFPlant.META_FIDDLEHEAD);
        } else {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
        }
    }

    /**
     * Decorate this biome with extra decorations!
     */
    @Override
    public void decorate(World par1World, Random par2Random, int par3, int par4) {
        WorldGenVines worldgenvines = new WorldGenVines();

        for (int i = 0; i < 20; i++) {
            int j = par3 + par2Random.nextInt(16) + 8;
            byte byte0 = (byte) TFWorld.SEALEVEL;
            int k = par4 + par2Random.nextInt(16) + 8;
            worldgenvines.generate(par1World, par2Random, j, byte0, k);
        }

        // TODO(mite-port): tall ferns (WorldGenDoublePlant / genTallFlowers) do not exist in 1.6.4

        super.decorate(par1World, par2Random, par3, par4);
    }

    // TODO(mite-port): func_150572_a (custom flower colours) has no MITE hook and was only used by 1.7.10 flower gen
}
