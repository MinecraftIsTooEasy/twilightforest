package twilightforest.biomes;

import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySlime;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenHugeTrees;
import net.minecraft.WorldGenTaiga1;
import net.minecraft.WorldGenTaiga2;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.block.BlockTFPlant;
import twilightforest.TFIds;
import twilightforest.compat.TFDamage;
import twilightforest.entity.EntityTFTroll;
import twilightforest.world.TFGenTrollRoots;


public class TFBiomeHighlands extends TFBiomeBase {

	/**
	 * 1.7.10 WorldGenBlockBlob (mossy boulders), which MITE does not have. Port of the vanilla generator, taking block ids.
	 */
	private static class WorldGenBlockBlob extends WorldGenerator {
		private final int blockId;
		private final int size;

		public WorldGenBlockBlob(int blockId, int size) {
			super(false);
			this.blockId = blockId;
			this.size = size;
		}

		@Override
		public boolean generate(World world, Random rand, int x, int y, int z) {
			// find the ground
			while (y > 3) {
				if (!world.isAirBlock(x, y - 1, z)) {
					int below = world.getBlockId(x, y - 1, z);

					if (below == Block.grass.blockID || below == Block.dirt.blockID || below == Block.stone.blockID) {
						break;
					}
				}

				--y;
			}

			if (y <= 3) {
				return false;
			}

			int radius = this.size;

			for (int l = 0; radius >= 0 && l < 3; ++l) {
				int rx = radius + rand.nextInt(2);
				int ry = radius + rand.nextInt(2);
				int rz = radius + rand.nextInt(2);
				float f = (float) (rx + ry + rz) * 0.333F + 0.5F;

				for (int bx = x - rx; bx <= x + rx; ++bx) {
					for (int bz = z - rz; bz <= z + rz; ++bz) {
						for (int by = y - ry; by <= y + ry; ++by) {
							float f1 = (float) (bx - x);
							float f2 = (float) (bz - z);
							float f3 = (float) (by - y);

							if (f1 * f1 + f2 * f2 + f3 * f3 <= f * f) {
								world.setBlock(bx, by, bz, this.blockId, 0, 4);
							}
						}
					}
				}

				x += -(radius + 1) + rand.nextInt(2 + radius * 2);
				z += -(radius + 1) + rand.nextInt(2 + radius * 2);
				y += -rand.nextInt(2);
			}

			return true;
		}
	}

	private static final WorldGenTaiga1 taigaGen1 = new WorldGenTaiga1();
	private static final WorldGenTaiga2 taigaGen2 = new WorldGenTaiga2(false);
	// TODO(mite-port): WorldGenMegaPineTree does not exist in MITE; spruce-metadata WorldGenHugeTrees is the closest substitute
	private static final WorldGenHugeTrees megaPineGen1 = new WorldGenHugeTrees(false, 13, 1, 1);
	private static final WorldGenHugeTrees megaPineGen2 = new WorldGenHugeTrees(false, 13, 1, 1);
	private static final WorldGenBlockBlob genBoulder = new WorldGenBlockBlob(Block.cobblestoneMossy.blockID, 0);
	private static final TFGenTrollRoots genTrollRoots = new TFGenTrollRoots();
	private static final WorldGenTallGrass worldGenMushgloom = new WorldGenTallGrass(TFIds.PLANT, BlockTFPlant.META_MUSHGLOOM);


	@SuppressWarnings("unchecked")
	public TFBiomeHighlands(int i) {
		super(i);

		this.temperature = 0.4F;
		this.rainfall = 0.7F;

		getTFBiomeDecorator().canopyPerChunk = -999;

		getTFBiomeDecorator().setGrassPerChunk(7);
		getTFBiomeDecorator().setDeadBushPerChunk(1);
		this.theBiomeDecorator.generateLakes = false; // actually underground water sources


		//this.spawnableMonsterList.add(new SpawnListEntry(EntityTFTroll.class, 10, 4, 4));


		undergroundMonsterList.clear();
		undergroundMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10, 1, 2));
		undergroundMonsterList.add(new SpawnListEntry(EntityCreeper.class, 2, 1, 1));
		undergroundMonsterList.add(new SpawnListEntry(EntityTFTroll.class, 10, 1, 2));
	}

	/**
	 * Birches and large trees only  (was func_150567_a)
	 */
	@Override
	public WorldGenerator getRandomWorldGenForTrees(Random random) {
		if (random.nextInt(4) == 0) {
			return taigaGen1;
		} else if (random.nextInt(10) == 0) {
			return taigaGen2;
		} else if (random.nextInt(3) == 0) {
			return megaPineGen1;
		} else if (random.nextInt(13) == 0) {
			return megaPineGen2;
		} else {
			return birchGen;
		}
	}

	/**
	 * Gets a WorldGen appropriate for this biome.
	 */
	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
		return par1Random.nextInt(5) > 0 ? new WorldGenTallGrass(Block.tallGrass.blockID, 2) : new WorldGenTallGrass(Block.tallGrass.blockID, 1);
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, short[] blockStorage, byte[] metaStorage, int x, int z, double noiseVal) {
		this.topBlockId = Block.grass.blockID;
		this.topBlockMeta = 0;
		this.fillerBlockId = Block.dirt.blockID;

		// TODO(mite-port): 1.7.10 used coarse dirt (meta 1) / podzol (meta 2) tops here; MITE BlockDirt has no subtypes, so plain dirt is used
		if (noiseVal > 1.75D) {
			this.topBlockId = Block.dirt.blockID;
			this.topBlockMeta = 0;
		}
		else if (noiseVal > -0.95D) {
			this.topBlockId = Block.dirt.blockID;
			this.topBlockMeta = 0;
		}

		this.genTwilightBiomeTerrain(world, rand, blockStorage, metaStorage, x, z, noiseVal);
	}

	/**
	 * Add extra decorations
	 */
	@Override
	public void decorate(World par1World, Random par2Random, int mapX, int mapZ) {
		int dx, dy, dz;

		// boulders
		int maxBoulders = par2Random.nextInt(2);
		for (int i = 0; i < maxBoulders; ++i) {
			dx = mapX + par2Random.nextInt(16) + 8;
			dz = mapZ + par2Random.nextInt(16) + 8;
			dy = par1World.getHeightValue(dx, dz);
			genBoulder.generate(par1World, par2Random, dx, dy, dz);
		}

		// giant ferns
		// TODO(mite-port): giant ferns used WorldGenDoublePlant (genTallFlowers), which does not exist in 1.6.4; skipped

		// mushglooms
		for (int i = 0; i < 1; ++i) {
			int rx = mapX + par2Random.nextInt(16) + 8;
			int rz = mapZ + par2Random.nextInt(16) + 8;
			int ry = par2Random.nextInt(64);
			// mushglooms
			worldGenMushgloom.generate(par1World, par2Random, rx, ry, rz);
		}

		// generate roots
		for (int i = 0; i < 24; ++i) {
			int rx = mapX + par2Random.nextInt(16) + 8;
			byte ry = 64;
			int rz = mapZ + par2Random.nextInt(16) + 8;
			genTrollRoots.generate(par1World, par2Random, rx, ry, rz);
		}

		super.decorate(par1World, par2Random, mapX, mapZ);
	}

	// TODO(mite-port): func_150572_a (flower choice, poppy/oxeye daisy) has no MITE hook; BiomeDecorator uses its own flower generators

	/**
	 * If there is a required achievement to be here, return it, otherwise return null
	 */
	@Override
	protected Achievement getRequiredAchievement() {
		return TFAchievementPage.twilightProgressGlacier;
	}

	/**
	 * Do something bad to a player in the wrong biome.
	 */
	@Override
	public void enforceProgession(EntityPlayer player, World world) {
		// balance 5.4: MITE rounds any magic hit up to 1 HP, so deal exactly 1 every 100 ticks, never below the HP floor
		if (!world.isRemote && world.getTotalWorldTime() % 100 == 0) {
			if (canDamagePenalty(player)) {
				TFDamage.hurt(player, DamageSource.magic, 1.0F);
				world.playSoundAtEntity(player, "random.fizz", 1.0F, 1.0F);
			}

			// hint monster?
			if (world.rand.nextInt(4) == 0) {
				TFFeature.trollCave.trySpawnHintMonster(world, player);
			}
		}
	}

}
