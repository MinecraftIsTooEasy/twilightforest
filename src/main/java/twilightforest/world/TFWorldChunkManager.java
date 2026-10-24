package twilightforest.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.BiomeCache;
import net.minecraft.BiomeGenBase;
import net.minecraft.ChunkCoordinates;
import net.minecraft.ChunkPosition;
import net.minecraft.GenLayer;
import net.minecraft.IntCache;
import net.minecraft.World;
import net.minecraft.WorldChunkManager;
import net.minecraft.WorldType;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.dimension.TFDimension;
import twilightforest.world.layer.GenLayerTF;

public class TFWorldChunkManager extends WorldChunkManager {
    private GenLayer unzoomedBiomes;

    /** A GenLayer containing the indices into BiomeGenBase.biomeList[] */
    private GenLayer zoomedBiomes;

    /** The BiomeCache object for this world. */
    private BiomeCache myBiomeCache;

    /** A list of biomes that the player can spawn in. */
    private List<BiomeGenBase> myBiomesToSpawnIn;

    protected TFWorldChunkManager() {
        // MITE's WorldChunkManager has a private BiomeCache and layers; the protected no-arg super ctor only fills the vanilla spawn list
        myBiomeCache = new BiomeCache(this);
        myBiomesToSpawnIn = new ArrayList<BiomeGenBase>();
        myBiomesToSpawnIn.add(TFBiomeBase.twilightForest);
        myBiomesToSpawnIn.add(TFBiomeBase.twilightForest2);
        myBiomesToSpawnIn.add(TFBiomeBase.clearing);
        myBiomesToSpawnIn.add(TFBiomeBase.tfSwamp);
        myBiomesToSpawnIn.add(TFBiomeBase.mushrooms);
    }

    public TFWorldChunkManager(long seed, WorldType worldType) {
        this();
        GenLayer[] layers;
        if (TwilightForestMod.oldMapGen) {
            layers = GenLayerTF.makeTheWorldOldMapGen(seed);
        } else {
            layers = GenLayerTF.makeTheWorld(seed);
        }
        unzoomedBiomes = layers[0];
        zoomedBiomes = layers[1];
    }

    public TFWorldChunkManager(World world) {
        this(TFDimension.getSeed(world), world.getWorldInfo().getTerrainType());
    }

    /** Biome for a GenLayer id; invalid ids (the layers sometimes yield -1 near cache edges) become the default forest. */
    private static BiomeGenBase biomeFor(int id) {
        BiomeGenBase biome = id >= 0 && id < BiomeGenBase.biomeList.length ? BiomeGenBase.biomeList[id] : null;
        return biome != null ? biome : TFBiomeBase.twilightForest;
    }

    /** MITE's GenLayer.getInts has a 5th argument (block Z for the latitude layer); TF's layers ignore it. */
    private static int[] zoomedInts(GenLayer layer, int x, int z, int width, int length) {
        return layer.getInts(x, z, width, length, z);
    }

    @Override
    public List getBiomesToSpawnIn() {
        return myBiomesToSpawnIn;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        BiomeGenBase biome = myBiomeCache.getBiomeGenAt(x, z);
        return biome != null ? biome : TFBiomeBase.twilightForest;
    }

    @Override
    public float[] getRainfall(float[] listToReuse, int x, int z, int width, int length) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new float[width * length];
        }

        int[] ids = zoomedInts(zoomedBiomes, x, z, width, length);

        for (int i = 0; i < width * length; i++) {
            float rainfall = (float) biomeFor(ids[i]).getIntRainfall() / 65536F;
            listToReuse[i] = rainfall > 1.0F ? 1.0F : rainfall;
        }

        return listToReuse;
    }

    /**
     * 1.7.10 dropped getTemperatures, MITE's BiomeCacheBlock still calls it for every cache block and the base version
     * dereferences a layer this subclass never sets.
     */
    @Override
    public float[] getTemperatures(float[] listToReuse, int x, int z, int width, int length) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new float[width * length];
        }

        int[] ids = zoomedInts(zoomedBiomes, x, z, width, length);

        for (int i = 0; i < width * length; i++) {
            float temperature = (float) biomeFor(ids[i]).getIntTemperature() / 65536F;
            listToReuse[i] = temperature > 1.0F ? 1.0F : temperature;
        }

        return listToReuse;
    }

    @Override
    public float getTemperatureAtHeight(float temperature, int height) {
        return temperature;
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] listToReuse, int x, int z, int length, int width) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < length * width) {
            listToReuse = new BiomeGenBase[length * width];
        }

        int[] ids = zoomedInts(unzoomedBiomes, x, z, length, width);

        for (int i = 0; i < length * width; i++) {
            listToReuse[i] = biomeFor(ids[i]);
        }

        return listToReuse;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] listToReuse, int x, int z, int width, int length) {
        return getBiomeGenAt(listToReuse, x, z, width, length, true);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] listToReuse, int x, int z, int width, int length, boolean useCache) {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new BiomeGenBase[width * length];
        }

        if (useCache && width == 16 && length == 16 && (x & 0xf) == 0 && (z & 0xf) == 0) {
            BiomeGenBase[] cached = myBiomeCache.getCachedBiomes(x, z);
            System.arraycopy(cached, 0, listToReuse, 0, width * length);
            return listToReuse;
        }

        int[] ids = zoomedInts(zoomedBiomes, x, z, width, length);

        for (int i = 0; i < width * length; i++) {
            listToReuse[i] = biomeFor(ids[i]);
        }

        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List allowed) {
        IntCache.resetIntCache();
        int i = x - radius >> 2;
        int j = z - radius >> 2;
        int k = x + radius >> 2;
        int l = z + radius >> 2;
        int width = (k - i) + 1;
        int length = (l - j) + 1;
        int[] ids = unzoomedBiomes.getInts(i, j, width, length, z);

        for (int n = 0; n < width * length; n++) {
            if (ids[n] < 0 || ids[n] >= BiomeGenBase.biomeList.length) {
                return false;
            }
            if (!allowed.contains(BiomeGenBase.biomeList[ids[n]])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ChunkPosition findBiomePosition(int x, int z, int radius, List allowed, Random random) {
        IntCache.resetIntCache();
        int i = x - radius >> 2;
        int j = z - radius >> 2;
        int k = x + radius >> 2;
        int l = z + radius >> 2;
        int width = (k - i) + 1;
        int length = (l - j) + 1;
        int[] ids = unzoomedBiomes.getInts(i, j, width, length, z);
        ChunkPosition position = null;
        int found = 0;

        for (int n = 0; n < ids.length; n++) {
            if (ids[n] < 0 || ids[n] >= BiomeGenBase.biomeList.length) {
                continue;
            }
            int blockX = i + n % width << 2;
            int blockZ = j + n / width << 2;
            BiomeGenBase biome = BiomeGenBase.biomeList[ids[n]];

            if (allowed.contains(biome) && (position == null || random.nextInt(found + 1) == 0)) {
                position = new ChunkPosition(blockX, 0, blockZ);
                found++;
            }
        }

        return position;
    }

    @Override
    public void cleanupCache() {
        myBiomeCache.cleanupCache();
    }

    /** Returns feature ID at the specified location. */
    public int getFeatureID(int mapX, int mapZ, World world) {
        return getFeatureAt(mapX, mapZ, world).featureID;
    }

    /** Returns feature at the specified location. */
    public TFFeature getFeatureAt(int mapX, int mapZ, World world) {
        return TFFeature.generateFeatureFor1Point7(mapX >> 4, mapZ >> 4, world);
    }

    /** Checks if the coordinates are in a feature chunk. */
    public boolean isInFeatureChunk(World world, int mapX, int mapZ) {
        // legacy support
        if (TwilightForestMod.oldMapGen) {
            return isInFeatureChunkOld(world, mapX, mapZ);
        }

        int chunkX = mapX >> 4;
        int chunkZ = mapZ >> 4;
        ChunkCoordinates cc = TFFeature.getNearestCenterXYZ(chunkX, chunkZ, world);

        return chunkX == (cc.posX >> 4) && chunkZ == (cc.posZ >> 4);
    }

    /** Legacy feature placement. */
    public boolean isInFeatureChunkOld(World world, int mapX, int mapZ) {
        int chunkX = mapX >> 4;
        int chunkZ = mapZ >> 4;

        return chunkX % 16 == 0 && chunkZ % 16 == 0;
    }
}
