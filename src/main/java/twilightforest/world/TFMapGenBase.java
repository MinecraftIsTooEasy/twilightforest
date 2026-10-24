package twilightforest.world;

import java.util.Random;

import net.minecraft.BiomeGenBase;
import net.minecraft.IChunkProvider;
import net.minecraft.World;

/**
 * Replacement for MapGenBase that works on TF's 256-high id arrays (index (x * 16 + z) * TFWorld.CHUNKHEIGHT + y).
 * Copy of MITE's MapGenBase.generate with a short[] terrain array.
 */
public class TFMapGenBase {
	protected int range = 8;
	protected Random rand = new Random();
	protected World worldObj;

	public void generate(IChunkProvider provider, World world, int chunkX, int chunkZ, short[] ids) {
		if (this.isGenAllowedInChunk(world, chunkX, chunkZ)) {
			int r = this.range;
			this.worldObj = world;
			this.rand.setSeed(world.getSeed());
			long a = this.rand.nextLong();
			long b = this.rand.nextLong();

			for (int x = chunkX - r; x <= chunkX + r; ++x) {
				for (int z = chunkZ - r; z <= chunkZ + r; ++z) {
					if (this.isGenAllowedInChunk(world, x, z)) {
						long sa = x * a;
						long sb = z * b;
						this.rand.setSeed(sa ^ sb ^ world.getSeed());
						this.recursiveGenerate(world, x, z, chunkX, chunkZ, ids);
					}
				}
			}
		}
	}

	protected void recursiveGenerate(World world, int chunkX, int chunkZ, int centerX, int centerZ, short[] ids) {
	}

	public boolean isGenAllowedInChunk(World world, int chunkX, int chunkZ) {
		return this.isGenAllowedInBiome(world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16));
	}

	public boolean isGenAllowedInBiome(BiomeGenBase biome) {
		return true;
	}
}
