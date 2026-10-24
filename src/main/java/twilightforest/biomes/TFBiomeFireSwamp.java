package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.EntityPlayer;
import net.minecraft.World;
import net.minecraft.WorldGenShrub;
import net.minecraft.WorldGenVines;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.block.BlockTFFireJet;
import twilightforest.block.TFBlocks;
import twilightforest.world.TFGenFireJet;
import twilightforest.world.TFWorld;

public class TFBiomeFireSwamp extends TFBiomeBase {

	protected TFBiomeFireSwamp(int i) {
		super(i);

//		this.rootHeight = -0.25F;
//		this.heightVariation = 0.0F;

		this.temperature = 1.0F;
		this.rainfall = 0.4F;

		getTFBiomeDecorator().setDeadBushPerChunk(2);
		getTFBiomeDecorator().setMushroomsPerChunk(8);
		getTFBiomeDecorator().setReedsPerChunk(4);
		getTFBiomeDecorator().setClayPerChunk(1);
		getTFBiomeDecorator().setTreesPerChunk(3);
		getTFBiomeDecorator().setWaterlilyPerChunk(6);
		waterColorMultiplier = 0x6C2C2C;


		getTFBiomeDecorator().canopyPerChunk = -999;
		getTFBiomeDecorator().lavaPoolChance = 0.125F;
		getTFBiomeDecorator().mangrovesPerChunk = 3;
	}


	/**
	 * Gets a WorldGen appropriate for this biome.  (was func_150567_a)
	 */
	@Override
	public WorldGenerator getRandomWorldGenForTrees(Random random) {
		if (random.nextInt(3) == 0) {
			return new WorldGenShrub(3, 0);
		}

		return worldGeneratorSwamp;
	}

	/**
	 * Decorate this biome.  This is stolen from the jungle code
	 */
	@Override
	public void decorate(World par1World, Random par2Random, int mapX, int mapZ) {
		super.decorate(par1World, par2Random, mapX, mapZ);

		TFFeature nearFeature = TFFeature.getNearestFeature(mapX >> 4, mapZ >> 4, par1World);
		if (nearFeature.areChunkDecorationsEnabled) {
			WorldGenVines worldgenvines = new WorldGenVines();

			for (int i = 0; i < 50; i++) {
				int j = mapX + par2Random.nextInt(16) + 8;
				byte byte0 = (byte) TFWorld.SEALEVEL;
				int k = mapZ + par2Random.nextInt(16) + 8;
				worldgenvines.generate(par1World, par2Random, j, byte0, k);
			}

			TFGenFireJet genSmoker = new TFGenFireJet(TFBlocks.fireJet, BlockTFFireJet.META_SMOKER);

			if (par2Random.nextInt(4) == 0) {
				int j = mapX + par2Random.nextInt(16) + 8;
				byte byte0 = (byte) TFWorld.SEALEVEL;
				int k = mapZ + par2Random.nextInt(16) + 8;
				genSmoker.generate(par1World, par2Random, j, byte0, k);
			}

			TFGenFireJet genFireJet = new TFGenFireJet(TFBlocks.fireJet, BlockTFFireJet.META_JET_IDLE);

			for (int i = 0; i < 1; i++) {
				int j = mapX + par2Random.nextInt(16) + 8;
				byte byte0 = (byte) TFWorld.SEALEVEL;
				int k = mapZ + par2Random.nextInt(16) + 8;
				genFireJet.generate(par1World, par2Random, j, byte0, k);
			}
		}
	}

	/**
	 * Provides the basic grass color
	 */
	@Override
	public int getBiomeGrassColor() {
		return 0x572e23;//0x8c4c35;//0x3f3555;// 0x8C0C0C;//0xd79e2f;//
	}

	/**
	 * Provides the basic foliage color
	 */
	@Override
	public int getBiomeFoliageColor() {
		return 0x64260f;//0x6C2020;//0x8f2839;//0x6c464d;//
	}

	/**
	 * If there is a required achievement to be here, return it, otherwise return null
	 */
	@Override
	protected Achievement getRequiredAchievement() {
		return TFAchievementPage.twilightProgressLabyrinth;
	}

	/**
	 * Do something bad to a player in the wrong biome.
	 */
	@Override
	public void enforceProgession(EntityPlayer player, World world) {
		// balance 5.4: short burn every 100 ticks, not in water, never below the HP floor
		if (!world.isRemote && world.getTotalWorldTime() % 100 == 0) {
			if (canDamagePenalty(player) && !player.isInWater()) {
				player.setFire(2);
			}
			// hint monster?
			if (world.rand.nextInt(4) == 0) {
				TFFeature.hydraLair.trySpawnHintMonster(world, player);
			}
		}
	}
}
