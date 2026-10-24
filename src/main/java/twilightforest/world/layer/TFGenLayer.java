package twilightforest.world.layer;

import net.minecraft.GenLayer;

/**
 * Bridge base class for the Twilight Forest layers. MITE's {@link GenLayer#getInts} has a fifth argument
 * (block Z, only used by MITE's latitude biome layer); TF's layers ignore it, so subclasses keep the 1.7.10
 * four argument method and read their parent through {@link #parentInts}.
 */
public abstract class TFGenLayer extends GenLayer {

	protected TFGenLayer(long seed) {
		super(seed);
	}

	/** MITE entry point. */
	@Override
	public final int[] getInts(int x, int z, int width, int depth, int blockZ) {
		return getInts(x, z, width, depth);
	}

	/** TF's 1.7.10 signature. */
	public abstract int[] getInts(int x, int z, int width, int depth);

	/** Replaces {@code parent.getInts(x, z, w, d)}; forwards the chunk Z as MITE's fifth argument like the vanilla layers do. */
	protected final int[] parentInts(int x, int z, int width, int depth) {
		return parent.getInts(x, z, width, depth, z);
	}
}
