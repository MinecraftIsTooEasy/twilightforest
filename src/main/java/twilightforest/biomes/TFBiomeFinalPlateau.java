package twilightforest.biomes;

import net.minecraft.Achievement;
import net.minecraft.Block;
import net.minecraft.SpawnListEntry;
import twilightforest.TFAchievementPage;
import twilightforest.block.TFBlocks;


public class TFBiomeFinalPlateau extends TFBiomeBase {

	@SuppressWarnings("unchecked")
	public TFBiomeFinalPlateau(int i) {
		super(i);

		this.topBlockId = TFBlocks.deadrock.blockID;
		this.topBlockMeta = 0;
		this.fillerBlockId = TFBlocks.deadrock.blockID;
		this.fillerBlockMeta = 1;

//		this.rootHeight = 7F;
//		this.heightVariation = 0.4F;

		this.temperature = 0.3F;
		this.rainfall = 0.2F;

		getTFBiomeDecorator().canopyPerChunk = -999;
		getTFBiomeDecorator().setTreesPerChunk(-999);

		this.theBiomeDecorator.generateLakes = false;

		// custom creature list.
		spawnableCreatureList.clear();
		spawnableCreatureList.add(new SpawnListEntry(twilightforest.entity.passive.EntityTFRaven.class, 10, 4, 4));
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
