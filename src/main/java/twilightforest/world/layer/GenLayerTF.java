package twilightforest.world.layer;

import net.minecraft.GenLayer;
import net.minecraft.GenLayerSmooth;
import net.minecraft.GenLayerVoronoiZoom;
import net.minecraft.GenLayerZoom;

public abstract class GenLayerTF extends GenLayer {

	public GenLayerTF(long l) {
		super(l);
	}

	public static GenLayer[] makeTheWorld(long l) {
		GenLayer biomes = new GenLayerTFBiomes1Point7(1L);
		biomes = new GenLayerTFKeyBiomes(1000L, biomes);
		biomes = new GenLayerTFCompanionBiomes(1000L, biomes);

		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerZoom(1001, biomes);

		biomes = new GenLayerTFBiomeStabilize(700L, biomes);

		biomes = new GenLayerTFThornBorder(500L, biomes);
		
		biomes = new GenLayerZoom(1002, biomes);
		biomes = new GenLayerZoom(1003, biomes);
		biomes = new GenLayerZoom(1004, biomes);
		biomes = new GenLayerZoom(1005, biomes);

		GenLayer riverLayer = new GenLayerTFStream(1L, biomes);
		riverLayer = new GenLayerSmooth(7000L, riverLayer);
		biomes = new GenLayerTFRiverMix(100L, biomes, riverLayer);

		// do "voronoi" zoom
		GenLayer genlayervoronoizoom = new GenLayerVoronoiZoom(10L, biomes);

		biomes.initWorldGenSeed(l);
		genlayervoronoizoom.initWorldGenSeed(l);

		return (new GenLayer[] { biomes, genlayervoronoizoom });
	}

	public static GenLayer[] makeTheWorldOldMapGen(long l) {
		GenLayer biomes = new GenLayerTFBiomes(1L);

		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerZoom(1001, biomes);

		biomes = new GenLayerTFBiomeStabilize(700L, biomes);

		biomes = new GenLayerZoom(1002, biomes);

		biomes = new GenLayerTFBiomeBorders(500L, biomes);

		biomes = new GenLayerZoom(1003, biomes);
		biomes = new GenLayerZoom(1004, biomes);
		biomes = new GenLayerZoom(1005, biomes);

		GenLayer riverLayer = new GenLayerTFStream(1L, biomes);
		riverLayer = new GenLayerSmooth(7000L, riverLayer);
		biomes = new GenLayerTFRiverMix(100L, biomes, riverLayer);

		// do "voronoi" zoom
		GenLayer genlayervoronoizoom = new GenLayerVoronoiZoom(10L, biomes);

		biomes.initWorldGenSeed(l);
		genlayervoronoizoom.initWorldGenSeed(l);

		return (new GenLayer[] { biomes, genlayervoronoizoom });
	}

}
