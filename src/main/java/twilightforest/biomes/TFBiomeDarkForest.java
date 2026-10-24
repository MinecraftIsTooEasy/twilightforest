package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.Achievement;
import net.minecraft.BiomeDecorator;
import net.minecraft.ColorizerFoliage;
import net.minecraft.ColorizerGrass;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntityWitch;
import net.minecraft.EntityZombie;
import net.minecraft.EnumCreatureType;
import net.minecraft.MathHelper;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenShrub;
import net.minecraft.WorldGenerator;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.entity.EntityTFKingSpider;
import twilightforest.entity.EntityTFKobold;
import twilightforest.entity.EntityTFMistWolf;
import twilightforest.entity.EntityTFSkeletonDruid;

public class TFBiomeDarkForest extends TFBiomeBase {

    private static final int MONSTER_SPAWN_RATE = 20;
    Random monsterRNG;
    ArrayList<SpawnListEntry> emptyList = new ArrayList<SpawnListEntry>();

    @SuppressWarnings("unchecked")
    public TFBiomeDarkForest(int i) {
        super(i);

        this.temperature = 0.7F;
        this.rainfall = 0.8F;

        getTFBiomeDecorator().canopyPerChunk = 5.5F;

        getTFBiomeDecorator().setTreesPerChunk(10);
        getTFBiomeDecorator().setGrassPerChunk(-99);
        getTFBiomeDecorator().setFlowersPerChunk(-99);
        getTFBiomeDecorator().setMushroomsPerChunk(2);
        getTFBiomeDecorator().setDeadBushPerChunk(10);

        this.minHeight = 0.05F;
        this.maxHeight = 0.05F;

        this.monsterRNG = new Random();

        this.spawnableMonsterList.add(new SpawnListEntry(EntityTFMistWolf.class, 10, 1, 2));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityTFSkeletonDruid.class, 10, 1, 2));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityTFKingSpider.class, 5, 1, 1));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityTFKobold.class, 10, 1, 3));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 5, 1, 2));
        this.spawnableMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 5, 1, 2));

        this.theBiomeDecorator.generateLakes = false;
    }

    /**
     * We have our own decorator since we have strange generation
     */
    @Override
    protected BiomeDecorator createBiomeDecorator() {
        return new TFDarkForestBiomeDecorator(this);
    }

    /**
     * Occasional shrub, no big trees
     */
    @Override
    public WorldGenerator getRandomWorldGenForTrees(Random random) {
        if (random.nextInt(5) == 0) {
            return new WorldGenShrub(3, 0);
        }
        if (random.nextInt(8) == 0) {
            return this.worldGeneratorForest; // birch
        } else {
            return this.worldGeneratorTrees;
        }
    }

    /**
     * Provides the basic grass color based on the biome temperature and rainfall
     */
    @Override
    public int getBiomeGrassColor() {
        double var1 = (double) MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
        double var3 = (double) MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
        return ((ColorizerGrass.getGrassColor(var1, var3) & 0xFEFEFE) + 0x1E0E4E) / 2;
    }

    /**
     * Provides the basic foliage color based on the biome temperature and rainfall
     */
    @Override
    public int getBiomeFoliageColor() {
        double var1 = (double) MathHelper.clamp_float(this.getFloatTemperature(), 0.0F, 1.0F);
        double var3 = (double) MathHelper.clamp_float(this.getFloatRainfall(), 0.0F, 1.0F);
        return ((ColorizerFoliage.getFoliageColor(var1, var3) & 0xFEFEFE) + 0x1E0E4E) / 2;
    }

    /**
     * Position-aware variant (1.7.10 signature); MITE has no coordinate hook, kept for subclasses / a colour mixin.
     */
    public int getBiomeGrassColor(int x, int y, int z) {
        return getBiomeGrassColor();
    }

    public int getBiomeFoliageColor(int x, int y, int z) {
        return getBiomeFoliageColor();
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
        } else {
            return super.getSpawnableList(par1EnumCreatureType);
        }
    }

    /**
     * This just seems to act as a fire discouragement
     */
    @Override
    public boolean isHighHumidity() {
        return true;
    }

    /**
     * If there is a required achievement to be here, return it, otherwise return null
     */
    @Override
    protected Achievement getRequiredAchievement() {
        return TFAchievementPage.twilightProgressHydra;
    }

    /**
     * Do something bad to a player in the wrong biome.
     */
    @Override
    public void enforceProgession(EntityPlayer player, World world) {
        if (!world.isRemote && world.getTotalWorldTime() % 80 == 0) {
            player.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0));

            // hint monster?
            if (world.rand.nextInt(4) == 0) {
                TFFeature.tfStronghold.trySpawnHintMonster(world, player);
            }
        }
    }
}
