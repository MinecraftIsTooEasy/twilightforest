package twilightforest.biomes;

import java.util.Random;
import net.minecraft.NoiseGeneratorOctaves;

public class TFBiomeDarkForestCenter extends TFBiomeDarkForest {

    /** Replaces 1.7.10 BiomeGenBase.plantNoise (func_151601_a). */
    private static final NoiseGeneratorOctaves PLANT_NOISE = new NoiseGeneratorOctaves(new Random(2345L), 1);

    public TFBiomeDarkForestCenter(int i) {
        super(i);
    }

    private static double sampleNoise(int x, int z, double scale) {
        // TODO(mite-port): MITE has no func_151601_a; sample a 1x1 box of the octave noise instead
        return PLANT_NOISE.generateNoiseOctaves(null, x, z, 1, 1, scale, scale, 0.5D)[0];
    }

    /**
     * Provides the basic grass color; MITE has no coordinate argument, so this is the constant mid colour.
     */
    @Override
    public int getBiomeGrassColor() {
        // TODO(mite-port): position dependent colour needs a colorMultiplier mixin, use the dark variant
        return 0x554114;
    }

    @Override
    public int getBiomeFoliageColor() {
        // TODO(mite-port): position dependent colour needs a colorMultiplier mixin, use the dark variant
        return 0xe94e14;
    }

    @Override
    public int getBiomeGrassColor(int x, int y, int z) {
        double d0 = sampleNoise(x, z, 0.0225D);
        return d0 < -0.2D ? 0x667540 : 0x554114;
    }

    @Override
    public int getBiomeFoliageColor(int x, int y, int z) {
        double d0 = sampleNoise(x, z, 0.0225D);
        return d0 < -0.1D ? 0xf9821e : 0xe94e14;
    }
}
