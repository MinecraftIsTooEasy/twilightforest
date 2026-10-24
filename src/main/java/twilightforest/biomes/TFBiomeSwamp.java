package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.Block;
import net.minecraft.ColorizerFoliage;
import net.minecraft.ColorizerGrass;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityZombie;
import net.minecraft.EnumCreatureType;
import net.minecraft.MathHelper;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenShrub;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenVines;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;
import twilightforest.entity.EntityTFMosquitoSwarm;
import twilightforest.world.TFGenHugeLilyPad;
import twilightforest.world.TFWorld;



public class TFBiomeSwamp extends TFBiomeBase {

	private static final int MONSTER_SPAWN_RATE = 20;
	Random monsterRNG = new Random(53439L);
	ArrayList<SpawnListEntry> emptyList = new ArrayList<SpawnListEntry>();

	WorldGenVines worldgenvines = new WorldGenVines();
	WorldGenerator hugeLilyPadGen = new TFGenHugeLilyPad();
	WorldGenerator hugeWaterLilyGen = new TFGenHugeWaterLily();



	@SuppressWarnings("unchecked")
	public TFBiomeSwamp(int i) {
		super(i);

//		this.rootHeight = -0.25F;
//		this.heightVariation = 0.0F;

		this.temperature = 0.8F;
		this.rainfall = 0.9F;

		getTFBiomeDecorator().setDeadBushPerChunk(1);
		getTFBiomeDecorator().setMushroomsPerChunk(8);
		getTFBiomeDecorator().setReedsPerChunk(10);
		getTFBiomeDecorator().setClayPerChunk(1);
		getTFBiomeDecorator().setTreesPerChunk(2);
		getTFBiomeDecorator().setWaterlilyPerChunk(20);
		waterColorMultiplier = 0xE0FFAE;


		getTFBiomeDecorator().canopyPerChunk = -999;
		getTFBiomeDecorator().lakesPerChunk = 2;
		getTFBiomeDecorator().mangrovesPerChunk = 3;

		this.spawnableMonsterList.add(new SpawnListEntry(EntityTFMosquitoSwarm.class, 10, 1, 1));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 5, 1, 2));
	}

	/**
	 * (was func_150567_a)
	 */
	@Override
	public WorldGenerator getRandomWorldGenForTrees(Random random) {
		if (random.nextInt(3) == 0) {
			return new WorldGenShrub(3, 0);
		}

		return worldGeneratorSwamp;
	}

	/**
	 * Ferns!
	 */
	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
		if (par1Random.nextInt(4) == 0) {
			return new WorldGenTallGrass(Block.tallGrass.blockID, 2);
		}
		else if (par1Random.nextInt(4) == 0) {
			return new WorldGenTallGrass(TFBlocks.plant.blockID, BlockTFPlant.META_MAYAPPLE);
		}
		else {
			return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
		}
	}

	/**
	 * Decorate this biome.  This is stolen from the jungle code
	 */
	@Override
	public void decorate(World par1World, Random par2Random, int par3, int par4) {
		super.decorate(par1World, par2Random, par3, par4);

		for (int i = 0; i < 50; i++) {
			int j = par3 + par2Random.nextInt(16) + 8;
			byte byte0 = (byte) TFWorld.SEALEVEL;
			int k = par4 + par2Random.nextInt(16) + 8;
			worldgenvines.generate(par1World, par2Random, j, byte0, k);
		}
		for (int i = 0; i < 25; i++) {
			int x = par3 + par2Random.nextInt(16) + 8;
			int y = TFWorld.SEALEVEL;
			int z = par4 + par2Random.nextInt(16) + 8;
			hugeLilyPadGen.generate(par1World, par2Random, x, y, z);
		}
		for (int i = 0; i < 2; i++) {
			int x = par3 + par2Random.nextInt(16) + 8;
			int y = TFWorld.SEALEVEL;
			int z = par4 + par2Random.nextInt(16) + 8;
			hugeWaterLilyGen.generate(par1World, par2Random, x, y, z);
		}
	}

	/**
	 * Provides the basic grass color based on the biome temperature and rainfall
	 */
	@Override
	public int getBiomeGrassColor() {
		double var1 = (double) MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
		double var3 = (double) MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
		return ((ColorizerGrass.getGrassColor(var1, var3) & 0xFEFEFE) + 0x4E0E4E) / 2;
	}

	/**
	 * Provides the basic foliage color based on the biome temperature and rainfall
	 */
	@Override
	public int getBiomeFoliageColor() {
		double var1 = (double) MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
		double var3 = (double) MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
		return ((ColorizerFoliage.getFoliageColor(var1, var3) & 0xFEFEFE) + 0x4E0E4E) / 2;
	}


	/**
	 * Returns the correspondent list of the EnumCreatureType informed.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List getSpawnableList(EnumCreatureType par1EnumCreatureType) {
		// if is is monster, then only give it the real list 1/MONSTER_SPAWN_RATE of the time
		if (par1EnumCreatureType == EnumCreatureType.monster) {
			return monsterRNG.nextInt(MONSTER_SPAWN_RATE) == 0 ? this.spawnableMonsterList : emptyList;
		}
		else {
			return super.getSpawnableList(par1EnumCreatureType);
		}
	}

	/**
	 * If there is a required achievement to be here, return it, otherwise return null
	 */
	@Override
	protected Achievement getRequiredAchievement() {
		return TFAchievementPage.twilightProgressLich;
	}

	/**
	 * Do something bad to a player in the wrong biome.
	 */
	@Override
	public void enforceProgession(EntityPlayer player, World world) {
		if (!world.isRemote && world.getTotalWorldTime() % 60 == 0) {
			// balance 5.4: Hunger I, renewed, never stacked up
			player.addPotionEffect(new PotionEffect(Potion.hunger.id, 100, 0));

			// hint monster?
			if (world.rand.nextInt(4) == 0) {
				TFFeature.labyrinth.trySpawnHintMonster(world, player);
			}
		}
	}
}
