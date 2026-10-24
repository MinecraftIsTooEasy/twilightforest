package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.block.TFBlocks;
import twilightforest.world.TFGenThorns;
import twilightforest.world.TFWorld;

public class TFBiomeThornlands extends TFBiomeBase {

	private TFGenThorns tfGenThorns;


	public TFBiomeThornlands(int i) {
		super(i);

		this.topBlockId = TFBlocks.deadrock.blockID;
		this.topBlockMeta = 0;
		this.fillerBlockId = TFBlocks.deadrock.blockID;
		this.fillerBlockMeta = 1;

		this.temperature = 0.3F;
		this.rainfall = 0.2F;

		getTFBiomeDecorator().canopyPerChunk = -999;
		getTFBiomeDecorator().setTreesPerChunk(-999);
		getTFBiomeDecorator().setDeadBushPerChunk(2);
		// TODO(mite-port): cactiPerChunk is protected in MITE BiomeDecorator; TFBiomeDecorator needs a setCactiPerChunk(int) (or AW) to apply -9999 here
		getTFBiomeDecorator().setCactiPerChunk(-9999);
		this.spawnableCreatureList.clear();

		this.tfGenThorns = new TFGenThorns();

		this.theBiomeDecorator.generateLakes = false;
	}


	@Override
	public void decorate(World world, Random rand, int mapX, int mapZ) {
		super.decorate(world, rand, mapX, mapZ);

		// add thorns!
		for (int i = 0; i < 128; i++) {
			int rx = mapX + rand.nextInt(16) + 8;
			int rz = mapZ + rand.nextInt(16) + 8;
			int ry = getGroundLevel(world, rx, rz);

			this.tfGenThorns.generate(world, rand, rx, ry, rz);
		}
	}

	public int getGroundLevel(World world, int x, int z) {
		// go from sea level up.  If we get grass, return that, otherwise return the last dirt, stone or gravel we got
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		int lastDirt = TFWorld.SEALEVEL;
		for (int y = TFWorld.SEALEVEL; y < TFWorld.CHUNKHEIGHT - 1; y++) {
			int blockID = chunk.getBlockID(x & 15, y, z & 15);
			// grass = return immediately
			if (blockID == Block.grass.blockID) {
				return y + 1;
			}
			else if (blockID == Block.dirt.blockID || blockID == Block.stone.blockID || blockID == Block.gravel.blockID || blockID == Block.sandStone.blockID || blockID == Block.sand.blockID || blockID == Block.blockClay.blockID || blockID == TFBlocks.deadrock.blockID) {
				lastDirt = y + 1;
			}
		}

		return lastDirt;
	}

	/**
	 * Return a block if you want it to replace stone in the terrain generation
	 */
	@Override
	public Block getStoneReplacementBlock() {
		return TFBlocks.deadrock;
	}

	/**
	 * Metadata for the stone replacement block
	 */
	@Override
	public byte getStoneReplacementMeta() {
		return 2;
	}

	@Override
	protected Achievement getRequiredAchievement() {
		return TFAchievementPage.twilightProgressGlacier;
	}
}
