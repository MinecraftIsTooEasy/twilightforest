/**
 * 
 */
package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.EntityPlayer;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenTaiga1;
import net.minecraft.WorldGenTaiga2;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.world.TFGenPenguins;
import twilightforest.world.TFWorld;



/**
 * @author Ben
 *
 */
public class TFBiomeGlacier extends TFBiomeBase {

	/**
	 * @param i
	 */
	@SuppressWarnings("unchecked")
	public TFBiomeGlacier(int i) {
		super(i);

		getTFBiomeDecorator().setTreesPerChunk(1);
		getTFBiomeDecorator().setGrassPerChunk(0);

		this.temperature = 0.0F;
		this.rainfall = 0.1F;
		getTFBiomeDecorator().canopyPerChunk = -999;

		spawnableCreatureList.add(new SpawnListEntry(twilightforest.entity.passive.EntityTFPenguin.class, 10, 1, 2));

	}


	/**
	 * (was func_150567_a)
	 */
	@Override
	public WorldGenerator getRandomWorldGenForTrees(Random random) {
		if (random.nextInt(3) == 0) {
			return new WorldGenTaiga1();
		} else {
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
	 * Decorate this biome.  Weeeee should generate penguins
	 */
	@Override
	public void decorate(World par1World, Random par2Random, int par3, int par4) {
		super.decorate(par1World, par2Random, par3, par4);
		TFGenPenguins penguins = new TFGenPenguins();

		if (par2Random.nextInt(4) == 0) {
			int j = par3 + par2Random.nextInt(16) + 8;
			byte byte0 = (byte) TFWorld.SEALEVEL;
			int k = par4 + par2Random.nextInt(16) + 8;
			penguins.generate(par1World, par2Random, j, byte0, k);
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
			player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 2));
			// hint monster?
			if (world.rand.nextInt(4) == 0) {
				TFFeature.iceTower.trySpawnHintMonster(world, player);
			}
		}
	}
}
