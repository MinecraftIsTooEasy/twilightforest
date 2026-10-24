package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.Achievement;
import net.minecraft.BiomeDecorator;
import net.minecraft.BiomeGenBase;
import net.minecraft.Block;
import net.minecraft.EntityBat;
import net.minecraft.EntityCaveSpider;
import net.minecraft.EntitySquid;
import net.minecraft.EntityChicken;
import net.minecraft.EntityCreeper;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySkeleton;
import net.minecraft.EntitySlime;
import net.minecraft.EntitySpider;
import net.minecraft.EntityWolf;
import net.minecraft.EntityZombie;
import net.minecraft.PlayerStatsHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.SpawnListEntry;
import net.minecraft.World;
import net.minecraft.WorldGenBigMushroom;
import net.minecraft.WorldGenBigTree;
import net.minecraft.WorldGenForest;
import net.minecraft.WorldGenTallGrass;
import net.minecraft.WorldGenerator;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFKobold;
import twilightforest.entity.passive.EntityTFBighorn;
import twilightforest.entity.passive.EntityTFBoar;
import twilightforest.entity.passive.EntityTFBunny;
import twilightforest.entity.passive.EntityTFDeer;
import twilightforest.entity.passive.EntityTFMobileFirefly;
import twilightforest.entity.passive.EntityTFRaven;
import twilightforest.entity.passive.EntityTFSquirrel;
import twilightforest.entity.passive.EntityTFTinyBird;


public abstract class TFBiomeBase extends BiomeGenBase  {

	/**
	 * 1.7.10 BiomeGenBase.Height, which MITE does not have. rootHeight maps to MITE minHeight, variation to maxHeight.
	 */
	public static class Height {
		public float rootHeight;
		public float variation;

		public Height(float rootHeight, float variation) {
			this.rootHeight = rootHeight;
			this.variation = variation;
		}
	}

	// 1.7.10 BiomeGenBase.height_* constants (MITE's own defaults differ: 0.1 / 0.3)
	protected static final Height height_Default = new Height(0.1F, 0.2F);
	protected static final Height height_ShallowWaters = new Height(-0.5F, 0.0F);
	protected static final Height height_DeepOceans = new Height(-1.8F, 0.1F);
	protected static final Height height_LowPlains = new Height(0.125F, 0.05F);
	protected static final Height height_MidPlains = new Height(0.2F, 0.2F);

	static
	{
		// ids are fixed in TFIds (no automatic assignment), just warn when one is taken
		areThereBiomeIdConflicts();
	}

	public static final BiomeGenBase tfLake = (new TFBiomeTwilightLake(TwilightForestMod.idBiomeLake)).setColor(0x0000ff).setBiomeName("Twilight Lake").setHeight(height_DeepOceans);
	public static final BiomeGenBase twilightForest = (new TFBiomeTwilightForest(TwilightForestMod.idBiomeTwilightForest)).setColor(0x005500).setBiomeName("Twilight Forest");
	public static final BiomeGenBase twilightForest2 = (new TFBiomeTwilightForestVariant(TwilightForestMod.idBiomeTwilightForestVariant)).setColor(0x005522).setBiomeName("Dense Twilight Forest").setHeight(height_MidPlains);
	public static final BiomeGenBase highlands = (new TFBiomeHighlands(TwilightForestMod.idBiomeHighlands)).setColor(0x556644).setBiomeName("Twilight Highlands").setHeight(new Height(3.5F, 0.05F));
	public static final BiomeGenBase mushrooms = (new TFBiomeMushrooms(TwilightForestMod.idBiomeMushrooms)).setColor(0x226622).setBiomeName("Mushroom Forest");
	public static final BiomeGenBase tfSwamp = (new TFBiomeSwamp(TwilightForestMod.idBiomeSwamp)).setColor(0x334422).setBiomeName("Twilight Swamp").setHeight(new Height(-0.125F, 0.125F));
	public static final BiomeGenBase stream = (new TFBiomeStream(TwilightForestMod.idBiomeStream)).setColor(0x3253b7).setBiomeName("Twilight Stream").setHeight(height_ShallowWaters);
	public static final BiomeGenBase tfSnow = (new TFBiomeSnow(TwilightForestMod.idBiomeSnowfield)).setColor(0xccffff).setBiomeName("Snowy Forest").setHeight(height_MidPlains);
	public static final BiomeGenBase glacier = (new TFBiomeGlacier(TwilightForestMod.idBiomeGlacier)).setColor(0x7777bb).setBiomeName("Twilight Glacier");
	public static final BiomeGenBase clearing = (new TFBiomeClearing(TwilightForestMod.idBiomeClearing)).setColor(0x349b34).setBiomeName("Twilight Clearing").setHeight(height_LowPlains);
	public static final BiomeGenBase oakSavanna = (new TFBiomeOakSavanna(TwilightForestMod.idBiomeOakSavanna)).setColor(0x446622).setBiomeName("Oak Savanna").setHeight(height_MidPlains);
	public static final BiomeGenBase fireflyForest = (new TFBiomeFireflyForest(TwilightForestMod.idBiomeFireflyForest)).setColor(0x006633).setBiomeName("Firefly Forest").setHeight(height_LowPlains);
	public static final BiomeGenBase deepMushrooms = (new TFBiomeDeepMushrooms(TwilightForestMod.idBiomeDeepMushrooms)).setColor(0x663322).setBiomeName("Deep Mushroom Forest").setHeight(height_LowPlains);
	public static final BiomeGenBase darkForest = (new TFBiomeDarkForest(TwilightForestMod.idBiomeDarkForest)).setColor(0x003311).setBiomeName("Dark Forest").setHeight(height_LowPlains);
	public static final BiomeGenBase enchantedForest = (new TFBiomeEnchantedForest(TwilightForestMod.idBiomeEnchantedForest)).setColor(0x115566).setBiomeName("Enchanted Forest");
	public static final BiomeGenBase fireSwamp = (new TFBiomeFireSwamp(TwilightForestMod.idBiomeFireSwamp)).setColor(0x42231a).setBiomeName("Fire Swamp").setHeight(height_Default);
	public static final BiomeGenBase darkForestCenter = (new TFBiomeDarkForestCenter(TwilightForestMod.idBiomeDarkForestCenter)).setColor(0x002200).setBiomeName("Dark Forest Center").setHeight(height_LowPlains);
	public static final BiomeGenBase highlandsCenter = (new TFBiomeFinalPlateau(TwilightForestMod.idBiomeHighlandsCenter)).setColor(0x334422).setBiomeName("Highlands Center").setHeight(new Height(10.5F, 0.025F));
	public static final BiomeGenBase thornlands = (new TFBiomeThornlands(TwilightForestMod.idBiomeThornlands)).setColor(0x223322).setBiomeName("Thornlands").setHeight(new Height(6F, 0.1F));

	/**
	 * MITE stores the top/filler block of a biome as a single byte id without metadata, which cannot hold TF block ids (above 255)
	 * or the metadata TF needs. These fields replace topBlock/fillerBlock and field_150604_aj/field_76754_C of 1.7.10.
	 * The MITE byte fields are deliberately left alone.
	 */
	public int topBlockId = Block.grass.blockID;
	public int topBlockMeta = 0;
	public int fillerBlockId = Block.dirt.blockID;
	public int fillerBlockMeta = 0;

	protected WorldGenBigMushroom bigMushroomGen;
	protected WorldGenForest birchGen;

	/** MITE's enableRain is private, so keep our own flag. */
	private boolean tfRainDisabled = false;

    /**
     * Holds the classes of IMobs (hostile mobs) that can be spawned in the biome.
     */
    protected List<SpawnListEntry> undergroundMonsterList;

	@SuppressWarnings("unchecked")
	public TFBiomeBase(int i) 
	{
		super(i);
		
		bigMushroomGen = new WorldGenBigMushroom();
		birchGen = new WorldGenForest(false);

		// MITE default is 0.1 / 0.3, TF (1.7.10 height_Default) is 0.1 / 0.2
		this.minHeight = height_Default.rootHeight;
		this.maxHeight = height_Default.variation;

		// try to stop world leaks
		this.worldGeneratorBigTree = null;
		
    	// remove normal monster spawns
        spawnableMonsterList.clear();
        // remove squids
        spawnableWaterCreatureList.clear();
        // custom creature list.
        spawnableCreatureList.clear();
        // balance 4.1: explicit whitelist only, groups of at most 3, animals never respawn on their own in MITE
        spawnableCreatureList.add(new SpawnListEntry(EntityTFBighorn.class, 12, 1, 3));
        spawnableCreatureList.add(new SpawnListEntry(EntityTFBoar.class, 10, 1, 3));
        spawnableCreatureList.add(new SpawnListEntry(EntityChicken.class, 10, 1, 3));
        spawnableCreatureList.add(new SpawnListEntry(EntityTFDeer.class, 15, 1, 3));
        spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 5, 1, 2));

        spawnableCreatureList.add(new SpawnListEntry(EntityTFTinyBird.class, 15, 2, 4));
        spawnableCreatureList.add(new SpawnListEntry(EntityTFSquirrel.class, 10, 1, 2));
        spawnableCreatureList.add(new SpawnListEntry(EntityTFBunny.class, 10, 1, 3));
        spawnableCreatureList.add(new SpawnListEntry(EntityTFRaven.class, 10, 1, 2));

        undergroundMonsterList = new ArrayList<SpawnListEntry>();

        undergroundMonsterList.add(new SpawnListEntry(EntitySpider.class, 10, 1, 2));
        undergroundMonsterList.add(new SpawnListEntry(EntityZombie.class, 10, 1, 2));
        undergroundMonsterList.add(new SpawnListEntry(EntitySkeleton.class, 10, 1, 2));
        undergroundMonsterList.add(new SpawnListEntry(EntityTFKobold.class, 10, 1, 3));
        undergroundMonsterList.add(new SpawnListEntry(EntityCreeper.class, 2, 1, 1));

        // no bats or fireflies in the cave list: not on the whitelist
        this.spawnableCaveCreatureList.clear();

        getTFBiomeDecorator().setTreesPerChunk(10);
        getTFBiomeDecorator().setGrassPerChunk(2);
	}

	// Covariant public wrappers for the protected/private MITE setters, so the static initialisers can chain them.

	@Override
    public TFBiomeBase setColor(int par1) {
    	return (TFBiomeBase) super.setColor(par1);
    }

	@Override
	public TFBiomeBase setBiomeName(String name) {
		return (TFBiomeBase) super.setBiomeName(name);
	}

	/**
	 * Set the terrain height (1.7.10 setHeight). rootHeight is MITE minHeight, variation is maxHeight.
	 */
	public TFBiomeBase setHeight(float rootHeight, float variation) {
		this.minHeight = rootHeight;
		this.maxHeight = variation;
		return this;
	}

	public TFBiomeBase setHeight(Height height) {
		return this.setHeight(height.rootHeight, height.variation);
	}

	/**
	 * MITE's setTemperatureRainfall is private and refuses 0.1 - 0.2, set the fields directly.
	 */
	public TFBiomeBase setTemperatureAndRainfall(float temperature, float rainfall) {
		this.temperature = temperature;
		this.rainfall = rainfall;
		return this;
	}

	@Override
	public TFBiomeBase setEnableSnow() {
		return (TFBiomeBase) super.setEnableSnow();
	}

	/**
	 * MITE's setDisableRain is private.
	 */
	public TFBiomeBase setDisableRain() {
		this.tfRainDisabled = true;
		return this;
	}

	@Override
	public boolean hasRainfall() {
		return !this.tfRainDisabled && super.hasRainfall();
	}

	/**
	 * Set the surface block of this biome (replaces 1.7.10 topBlock + field_150604_aj).
	 */
	public TFBiomeBase setTopBlock(Block block, int meta) {
		this.topBlockId = block == null ? 0 : block.blockID;
		this.topBlockMeta = meta;
		return this;
	}

	/**
	 * Set the sub-surface block of this biome (replaces 1.7.10 fillerBlock + field_76754_C).
	 */
	public TFBiomeBase setFillerBlock(Block block, int meta) {
		this.fillerBlockId = block == null ? 0 : block.blockID;
		this.fillerBlockMeta = meta;
		return this;
	}

	
    /**
     * returns the chance a creature has to spawn.
     */
    @Override
    public float getSpawningChance()
    {
    	// okay, 20% more animals
        return 0.12F;
    }

    /**
     * Allocate a new BiomeDecorator for this BiomeGenBase
     */
    @Override
    protected BiomeDecorator createBiomeDecorator()
    {
        return new TFBiomeDecorator(this);
    }
    
    protected TFBiomeDecorator getTFBiomeDecorator() {
    	return (TFBiomeDecorator)this.theBiomeDecorator;
    }

    /**
     * Forest trees all over
     * 
     * 1.7.10 func_150567_a, MITE getRandomWorldGenForTrees
     */
    @Override
    public WorldGenerator getRandomWorldGenForTrees(Random random)
    {
        if(random.nextInt(5) == 0)
        {
        	return birchGen;
        }
        if(random.nextInt(10) == 0)
        {
            return new WorldGenBigTree(false);
        } else
        {
            return worldGeneratorTrees;
        }
    }
	
    /**
     * Ferns!
     */
    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random par1Random)
    {
        if (par1Random.nextInt(4) == 0)
        {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 2);
        }
        else
        {
            return new WorldGenTallGrass(Block.tallGrass.blockID, 1);
        }
    }

	/**
     * Check all of our biome IDs.  If there is a biome in them that is not null and does not inherit from TFBiomeBase, make a warning.
     */
    public static boolean areThereBiomeIdConflicts()
    {
    	boolean conflict = TwilightForestMod.hasBiomeIdConflicts;
    	
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeLake);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeTwilightForest);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeTwilightForestVariant);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeHighlands);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeMushrooms);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeSwamp);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeStream);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeSnowfield);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeGlacier);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeClearing);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeOakSavanna);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeFireflyForest);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeDeepMushrooms);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeDarkForestCenter);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeHighlandsCenter);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeDarkForest);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeEnchantedForest);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeFireSwamp);
    	conflict |= isConflictAtBiomeID(TwilightForestMod.idBiomeThornlands);
    	
       	if (conflict) {
			TwilightForestMod.LOGGER.warn("Biome ID conflict detected. Another mod uses an id from TFIds.");
    	}
       	
       	TwilightForestMod.hasBiomeIdConflicts |= conflict;
       	
		return conflict;
    }

    /**
     * Check the ID for conflicts
     */
	public static boolean isConflictAtBiomeID(int id) {
		BiomeGenBase biomeAt = (id >= 0 && id < BiomeGenBase.biomeList.length) ? BiomeGenBase.biomeList[id] : null;
		
		if (biomeAt == null || biomeAt instanceof TFBiomeBase) {
			return false;
		} else {
			TwilightForestMod.LOGGER.warn("Biome ID conflict. Biome ID {} contains a biome named {}, but Twilight Forest is set to use that ID.", id, biomeAt.biomeName);
			return true;
		}
	}

	/**
	 * Create and register the Twilight Forest biomes. A BiomeGenBase registers itself in biomeList from its constructor, so
	 * touching this class (which runs the static initialiser above) is all that is needed.
	 * (1.7.10 also registered them with the Forge BiomeDictionary, which MITE does not have.)
	 */
	public static void registerBiomes()
	{
		TwilightForestMod.LOGGER.info("Registered {} Twilight Forest biomes (ids {} to {})", 19, TwilightForestMod.idBiomeLake, TwilightForestMod.idBiomeThornlands);
		areThereBiomeIdConflicts();
	}

    /**
     * Terrain gen for one column. Arrays are indexed (z * 16 + x) * 256 + y like in 1.7.10, but hold block ids (0..4095).
     */
    public void genTerrainBlocks(World world, Random rand, short[] blockStorage, byte[] metaStorage, int x, int z, double stoneNoise)
    {
        this.genTwilightBiomeTerrain(world, rand, blockStorage, metaStorage, x, z, stoneNoise);
    }

    /**
     * This is just the biome terrain gen function modified to have sea level at 32 instead of 64
     */
    public void genTwilightBiomeTerrain(World world, Random rand, short[] blockStorage, byte[] metaStorage, int x, int z, double stoneNoise)
    {
        int stoneId = Block.stone.blockID;
        int topBlock = this.topBlockId;
        byte topMeta = (byte)(this.topBlockMeta & 255);
        int fillerBlock = this.fillerBlockId;
        byte fillerMeta = (byte)(this.fillerBlockMeta & 255);
        int currentFillerDepth = -1;
        int maxFillerDepth = (int)(stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        int maskX = x & 15;
        int maskZ = z & 15;
        int worldHeight = blockStorage.length / 256;
        
        int seaLevel = 32;

        Block stoneReplacement = this.getStoneReplacementBlock();

        for (int y = 255; y >= 0; --y)
        {
            int index = (maskZ * 16 + maskX) * worldHeight + y;

            if (y <= 0 + rand.nextInt(5))
            {
                blockStorage[index] = (short) Block.bedrock.blockID;
            }
            else
            {
                int currentBlock = blockStorage[index];

                if (currentBlock != 0)
                {
                    if (currentBlock == stoneId)
                    {
                    	// ADDED STONE REPLACEMENT
                    	if (stoneReplacement != null) {
                    		blockStorage[index] = (short) stoneReplacement.blockID;
                    		metaStorage[index] = this.getStoneReplacementMeta();
                    	}
                        
                        if (currentFillerDepth == -1)
                        {
                            if (maxFillerDepth <= 0)
                            {
                                topBlock = 0;
                                topMeta = 0;
                                fillerBlock = stoneId;
                                fillerMeta = 0;
                            }
                            else if (y >= (seaLevel - 5) && y <= seaLevel)
                            {
                                topBlock = this.topBlockId;
                                topMeta = (byte)(this.topBlockMeta & 255);
                                fillerBlock = this.fillerBlockId;
                                fillerMeta = (byte)(this.fillerBlockMeta & 255);
                            }

                            if (y < (seaLevel - 1) && topBlock == 0)
                            {
                                if (this.getFloatTemperature() < 0.15F)
                                {
                                    topBlock = Block.ice.blockID;
                                    topMeta = 0;
                                }
                                else
                                {
                                    topBlock = Block.waterStill.blockID;
                                    topMeta = 0;
                                }
                            }

                            currentFillerDepth = maxFillerDepth;

                            if (y >= (seaLevel - 2))
                            {
                                blockStorage[index] = (short) topBlock;
                                metaStorage[index] = topMeta;
                            }
                            else if (y < (seaLevel - 8) - maxFillerDepth)
                            {
                                topBlock = 0;
                                fillerBlock = stoneId;
                                fillerMeta = 0;
                                blockStorage[index] = (short) Block.gravel.blockID;
                            }
                            else
                            {
                                blockStorage[index] = (short) fillerBlock;
                                metaStorage[index] = fillerMeta;
                            }
                        }
                        else if (currentFillerDepth > 0)
                        {
                            --currentFillerDepth;
                            blockStorage[index] = (short) fillerBlock;
                            metaStorage[index] = fillerMeta;

                            if (currentFillerDepth == 0 && fillerBlock == Block.sand.blockID)
                            {
                                currentFillerDepth = rand.nextInt(4) + Math.max(0, y - (seaLevel - 1));
                                fillerBlock = Block.sandStone.blockID;
                                fillerMeta = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Return a block if you want it to replace stone in the terrain generation
     */
	public Block getStoneReplacementBlock() {
		return null;
	}
	
    /**
     * Metadata for the stone replacement block
     */
	public byte getStoneReplacementMeta() {
		return 0;
	}

	private static boolean isWhitelisted(Class<?> clazz) {
		return clazz != null && (clazz.getName().startsWith("twilightforest.entity.")
				|| clazz == EntityZombie.class || clazz == EntitySkeleton.class || clazz == EntitySpider.class
				|| clazz == EntityCaveSpider.class || clazz == EntityCreeper.class || clazz == EntityWolf.class
				|| clazz == EntityChicken.class || clazz == EntitySquid.class);
	}

	private static void sanitize(List<?> list) {
		list.removeIf(o -> !isWhitelisted(((SpawnListEntry) o).entityClass));
	}

	/**
	 * Drops everything that is not on the balance whitelist (MITE biome constructors and other mods add deep mobs,
	 * endermen, slimes, witches and blazes). Runs on every TF biome after all mods have loaded.
	 */
	public void sanitizeSpawnLists() {
		sanitize(spawnableMonsterList);
		sanitize(spawnableCreatureList);
		sanitize(spawnableWaterCreatureList);
		sanitize(spawnableCaveCreatureList);
		sanitize(undergroundMonsterList);
	}

	/** Self healing pass over every registered TF biome. */
	public static void sanitizeAllSpawnLists() {
		for (BiomeGenBase biome : BiomeGenBase.biomeList) {
			if (biome instanceof TFBiomeBase tf) {
				tf.sanitizeSpawnLists();
			}
		}
	}

	/** Is this biome one of the balance relevant penalty zones; damage penalties never kill (H5). */
	protected static boolean canDamagePenalty(EntityPlayer player) {
		return twilightforest.TFConfig.penaltiesLethal || player.getHealth() > player.getMaxHealth() * (float) twilightforest.TFConfig.progressPenaltyHpFloor;
	}

	/**
	 * Does the player have the achievement needed to be in this biome?
	 * Defaults to true for no achievement required biomes.
	 */
	public boolean doesPlayerHaveRequiredAchievement(EntityPlayer player) {
		Achievement required = getRequiredAchievement();
		
		if (required != null && player instanceof ServerPlayer && player.stats != null) {
			return PlayerStatsHelper.getValue(required.statId, player.stats) == 1L;
		} else {
			// TODO(mite-port): the 1.7.10 client branch (EntityClientPlayerMP stat file) has no MITE equivalent, the client always says yes
			return true;
		}
	}

	/**
	 * If there is a required achievement to be here, return it, otherwise return null
	 */
	protected Achievement getRequiredAchievement() {
		return null;
	}

	/**
	 * Do something bad to a player in the wrong biome.
	 */
	public void enforceProgession(EntityPlayer player, World world) {
		;
	}
	
    /**
     * Returns the list of underground creatures.
     */
	public List<SpawnListEntry> getUndergroundSpawnableList() {
    	return this.undergroundMonsterList;
    }
}
