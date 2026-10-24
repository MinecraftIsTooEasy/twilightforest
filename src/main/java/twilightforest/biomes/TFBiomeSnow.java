/**
 * 
 */
package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumCreatureType;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenTaiga1;
import net.minecraft.WorldGenTaiga2;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.entity.EntityTFWinterWolf;
import twilightforest.entity.EntityTFYeti;
import twilightforest.world.TFGenLargeWinter;


/**
 * @author Ben
 *
 */
public class TFBiomeSnow extends TFBiomeBase {


	private static final int MONSTER_SPAWN_RATE = 10;
	Random monsterRNG = new Random(53439L);
	ArrayList<SpawnListEntry> emptyList = new ArrayList<SpawnListEntry>();

	/**
	 * @param i
	 */
	@SuppressWarnings("unchecked")
	public TFBiomeSnow(int i) {
		super(i);

		getTFBiomeDecorator().setTreesPerChunk(7);
		getTFBiomeDecorator().setGrassPerChunk(1);

		this.temperature = 0.125F;
		this.rainfall = 0.9F;

		getTFBiomeDecorator().canopyPerChunk = -999;
		getTFBiomeDecorator().generateLakes = false;

		this.spawnableMonsterList.add(new SpawnListEntry(EntityTFYeti.class, 20, 1, 3));
		this.spawnableMonsterList.add(new SpawnListEntry(EntityTFWinterWolf.class, 5, 1, 2));

	}

	/**
	 * (was func_150567_a)
	 */
	@Override
	public WorldGenerator getRandomWorldGenForTrees(Random random) {
		if (random.nextInt(3) == 0) {
			return new WorldGenTaiga1();
		}
		else if (random.nextInt(8) == 0) {
			return new TFGenLargeWinter();
		}
		else {
			return new WorldGenTaiga2(true);
		}
	}

	/**
	 * Let it snow!
	 */
	@Override
	public boolean getEnableSnow() {
		return true;
	}

	/**
	 * Required for actual snow?
	 */
	@Override
	public boolean canSpawnLightningBolt(boolean is_blood_moon) {
		return false;
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
		return TFAchievementPage.twilightProgressUrghast;
	}

	/**
	 * Do something bad to a player in the wrong biome.
	 */
	@Override
	public void enforceProgession(EntityPlayer player, World world) {
		if (!world.isRemote && world.getTotalWorldTime() % 60 == 0) {
			player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 1));

			// hint monster?
			if (world.rand.nextInt(4) == 0) {
				TFFeature.yetiCave.trySpawnHintMonster(world, player);
			}
		}
	}


}
