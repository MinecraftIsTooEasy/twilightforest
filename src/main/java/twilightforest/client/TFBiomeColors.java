package twilightforest.client;

import net.minecraft.BiomeGenBase;
import net.minecraft.IBlockAccess;
import twilightforest.biomes.TFBiomeDarkForest;
import twilightforest.biomes.TFBiomeEnchantedForest;

/** Position-dependent grass/foliage colours of the TF biomes (MITE's BiomeGenBase colour methods take no position). */
public final class TFBiomeColors {
    private TFBiomeColors() {
    }

    private static boolean positional(BiomeGenBase biome) {
        return biome instanceof TFBiomeDarkForest || biome instanceof TFBiomeEnchantedForest;
    }

    public static int grass(BiomeGenBase biome, int x, int y, int z) {
        if (biome instanceof TFBiomeDarkForest dark) {
            return dark.getBiomeGrassColor(x, y, z);
        } else if (biome instanceof TFBiomeEnchantedForest enchanted) {
            return enchanted.getBiomeGrassColor(x, y, z);
        }
        return biome.getBiomeGrassColor();
    }

    public static int foliage(BiomeGenBase biome, int x, int y, int z) {
        if (biome instanceof TFBiomeDarkForest dark) {
            return dark.getBiomeFoliageColor(x, y, z);
        } else if (biome instanceof TFBiomeEnchantedForest enchanted) {
            return enchanted.getBiomeFoliageColor(x, y, z);
        }
        return biome.getBiomeFoliageColor();
    }

    /** True when any biome of the 3x3 colour blend is one of the position-dependent TF biomes. */
    public static boolean blendIsPositional(IBlockAccess world, int x, int z) {
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                if (positional(world.getBiomeGenForCoords(x + dx, z + dz))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 3x3 blended colour, as vanilla does it but with position-aware samples. */
    public static int blend(IBlockAccess world, int x, int y, int z, boolean grass) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                BiomeGenBase biome = world.getBiomeGenForCoords(x + dx, z + dz);
                int c = grass ? grass(biome, x + dx, y, z + dz) : foliage(biome, x + dx, y, z + dz);
                r += (c & 0xFF0000) >> 16;
                g += (c & 0xFF00) >> 8;
                b += c & 0xFF;
            }
        }
        return (r / 9 & 0xFF) << 16 | (g / 9 & 0xFF) << 8 | b / 9 & 0xFF;
    }
}
