package twilightforest.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.BiomeDecorator;
import net.minecraft.BiomeGenBase;
import net.minecraft.Block;
import net.minecraft.WeightedRandom;
import net.minecraft.WeightedRandomItem;
import net.minecraft.World;
import net.minecraft.WorldGenLakes;
import net.minecraft.WorldGenLiquids;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.world.TFGenCanopyMushroom;
import twilightforest.world.TFGenCanopyTree;
import twilightforest.world.TFGenFallenHollowLog;
import twilightforest.world.TFGenFallenSmallLog;
import twilightforest.world.TFGenFoundation;
import twilightforest.world.TFGenGroveRuins;
import twilightforest.world.TFGenTorchBerries;
import twilightforest.world.TFGenHollowStump;
import twilightforest.world.TFGenHollowTree;
import twilightforest.world.TFGenMangroveTree;
import twilightforest.world.TFGenMonolith;
import twilightforest.world.TFGenMyceliumBlob;
import twilightforest.world.TFGenOutsideStalagmite;
import twilightforest.world.TFGenPlantRoots;
import twilightforest.world.TFGenStoneCircle;
import twilightforest.world.TFGenWell;
import twilightforest.world.TFGenWitchHut;
import twilightforest.world.TFGenWoodRoots;
import twilightforest.world.TFGenerator;
import twilightforest.world.TFTreeGenerator;



public class TFBiomeDecorator extends BiomeDecorator {
	
	TFGenCanopyTree canopyTreeGen;
	TFTreeGenerator alternateCanopyGen;
	TFGenHollowTree hollowTreeGen;
	TFGenMyceliumBlob myceliumBlobGen;
	WorldGenLakes extraLakeGen;
	WorldGenLakes extraLavaPoolGen;
	TFGenMangroveTree mangroveTreeGen;
	
	TFGenPlantRoots plantRootGen;
	TFGenWoodRoots woodRootGen;
	TFGenTorchBerries torchBerryGen;
	   
    public float canopyPerChunk;
    public float alternateCanopyChance;
    public int myceliumPerChunk;
    public int mangrovesPerChunk;
    public int lakesPerChunk;
    public float lavaPoolChance;
    
    static final List<RuinEntry> ruinList = new ArrayList<RuinEntry>();
    static {
    	// make list of ruins
    	ruinList.add(new RuinEntry(new TFGenStoneCircle(), 10));
    	ruinList.add(new RuinEntry(new TFGenWell(), 10));
    	ruinList.add(new RuinEntry(new TFGenWitchHut(), 5));
    	ruinList.add(new RuinEntry(new TFGenOutsideStalagmite(), 12));
    	ruinList.add(new RuinEntry(new TFGenFoundation(), 10));
    	ruinList.add(new RuinEntry(new TFGenMonolith(), 10));
    	ruinList.add(new RuinEntry(new TFGenGroveRuins(), 5));
    	ruinList.add(new RuinEntry(new TFGenHollowStump(), 12));
    	ruinList.add(new RuinEntry(new TFGenFallenHollowLog(), 10));
    	ruinList.add(new RuinEntry(new TFGenFallenSmallLog(), 10));
    }
    
    /**
     * WeightedRandomItem for making the minor features
     */
    static class RuinEntry extends WeightedRandomItem
    {
        public final TFGenerator generator;
        public RuinEntry(TFGenerator generator, int weight)
        {
            super(weight);
            this.generator = generator;
        }
    }


	public TFBiomeDecorator(BiomeGenBase biome) {
		super(biome);

		canopyTreeGen = new TFGenCanopyTree();
		alternateCanopyGen = new TFGenCanopyMushroom();
		mangroveTreeGen = new TFGenMangroveTree();
		myceliumBlobGen = new TFGenMyceliumBlob(5);
		hollowTreeGen = new TFGenHollowTree();
		extraLakeGen = new WorldGenLakes(Block.waterStill.blockID);
		extraLavaPoolGen = new WorldGenLakes(Block.lavaStill.blockID);
		
		plantRootGen = new TFGenPlantRoots();
		woodRootGen = new TFGenWoodRoots();
		torchBerryGen = new TFGenTorchBerries();

		canopyPerChunk = TwilightForestMod.canopyCoverage;
		alternateCanopyChance = 0;
		myceliumPerChunk = 0;
		lakesPerChunk = 0;
		lavaPoolChance = 0;
		mangrovesPerChunk = 0;
	}

    /**
     * Decorates the world (1.7.10 decorateChunk). MITE's version is decorate(World, Random, int, int) and the biome is the field {@link #biome}.
     */
	@Override
    public void decorate(World world, Random rand, int mapX, int mapZ)
    {

    	// check for features
    	TFFeature nearFeature = TFFeature.getNearestFeature(mapX >> 4, mapZ >> 4, world);

    	if (!nearFeature.areChunkDecorationsEnabled) {
        	// no normal decorations here, these parts supply their own decorations.
            decorateUnderground(world, rand, mapX, mapZ);
            decorateOnlyOres(world, rand, mapX, mapZ);
    	} else {
	    	// regular decorations
    		// decorating can generate neighbouring chunks, which re-enters this shared decorator: keep the outer state intact
    		World outerWorld = this.currentWorld;
    		Random outerRandom = this.randomGenerator;
    		int outerX = this.chunk_X;
    		int outerZ = this.chunk_Z;
    		this.currentWorld = null; // suppress "Already decorating" error.
    		try {
	    		super.decorate(world, rand, mapX, mapZ);
    		} finally {
    			this.currentWorld = outerWorld;
    			this.randomGenerator = outerRandom;
    			this.chunk_X = outerX;
    			this.chunk_Z = outerZ;
    		}
	    	
    	}

    }
    
    /**
     * The method that does the work of actually decorating chunks (1.7.10 genDecorations(BiomeGenBase))
     */
    @Override
    protected void decorate()
    {
        
    	// random features!
        if(randomGenerator.nextInt(6) == 0)
        {
            int rx = chunk_X + randomGenerator.nextInt(16) + 8;
            int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
            int ry =  currentWorld.getHeightValue(rx, rz);
            TFGenerator rf = randomFeature(randomGenerator);
            rf.generate(currentWorld, randomGenerator, rx, ry, rz);
        }

		// add canopy trees
    	int nc = (int)canopyPerChunk + ((randomGenerator.nextFloat() < (canopyPerChunk - (int)canopyPerChunk)) ? 1 : 0);
    	for (int i = 0; i < nc; i++) {
    		int rx = chunk_X + randomGenerator.nextInt(16) + 8;
    		int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
    		int ry = currentWorld.getHeightValue(rx,  rz);
    		if (this.alternateCanopyChance > 0 && randomGenerator.nextFloat() <= alternateCanopyChance) {
    			alternateCanopyGen.generate(currentWorld, randomGenerator, rx, ry, rz);
    		} else {
    			canopyTreeGen.generate(currentWorld, randomGenerator, rx, ry, rz);
	        }
    	}

    	// mangrove trees
    	for (int i = 0; i < mangrovesPerChunk; i++) {
	        int rx = chunk_X + randomGenerator.nextInt(16) + 8;
	        int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
	        int ry = currentWorld.getHeightValue(rx, rz);
	        mangroveTreeGen.generate(currentWorld, randomGenerator, rx, ry, rz);
    	}
    	// add extra lakes for swamps
    	for (int i = 0; i < lakesPerChunk; i++) {
	        int rx = chunk_X + randomGenerator.nextInt(16) + 8;
	        int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
	        int ry = currentWorld.getHeightValue(rx, rz);
	        extraLakeGen.generate(currentWorld, randomGenerator, rx, ry, rz);
    	}
    	
    	// add extra lava for fire swamps
    	if (randomGenerator.nextFloat() <= lavaPoolChance) {
	        int rx = chunk_X + randomGenerator.nextInt(16) + 8;
	        int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
	        int ry = currentWorld.getHeightValue(rx, rz);
	        extraLavaPoolGen.generate(currentWorld, randomGenerator, rx, ry, rz);
    	}
    	
    	// mycelium blobs
    	for (int i = 0; i < myceliumPerChunk; i++) {
	        int rx = chunk_X + randomGenerator.nextInt(16) + 8;
	        int rz = chunk_Z + randomGenerator.nextInt(16) + 8;
	        int ry = currentWorld.getHeightValue(rx, rz);
	        myceliumBlobGen.generate(currentWorld, randomGenerator, rx, ry, rz);
    	}
    	
        super.decorate();
        
        decorateUnderground(currentWorld, randomGenerator, chunk_X, chunk_Z);
        

    }

    /**
     * Generate the Twilight Forest underground decorations
     */
	protected void decorateUnderground(World world, Random rand, int mapX, int mapZ) {
		// generate roots
		for (int i = 0; i < 12; ++i)
		{
		    int rx = mapX + rand.nextInt(16) + 8;
		    byte ry = 64;
		    int rz = mapZ + rand.nextInt(16) + 8;
		    plantRootGen.generate(world, rand, rx, ry, rz);
		}

		// generate roots
		for (int i = 0; i < 20; ++i)
		{
		    int rx = mapX + rand.nextInt(16) + 8;
		    int ry = rand.nextInt(64);
		    int rz = mapZ + rand.nextInt(16) + 8;
		    woodRootGen.generate(world, rand, rx, ry, rz);
		}
		
		// extra underground water sources
		if (this.generateLakes) {
			for (int i = 0; i < 50; ++i)
			{
				int rx = mapX + rand.nextInt(16) + 8;
				int ry = rand.nextInt(24) + 4;
				int rz = mapZ + rand.nextInt(16) + 8;
				// MITE's WorldGenLiquids is a static helper, not a WorldGenerator
				WorldGenLiquids.generate(world, rand, Block.waterMoving.blockID, rx, ry, rz);
			}
		}
		
        // torch berries are almost guaranteed to spawn so we don't need many
        for (int i = 0; i < 3; ++i)
        {
            int rx = mapX + rand.nextInt(16) + 8;
            int ry = 64;
            int rz = mapZ + rand.nextInt(16) + 8;
            torchBerryGen.generate(world, rand, rx, ry, rz);
        }
	}
	
	/**
	 * Generates ores only
	 */
	public void decorateOnlyOres(World world, Random rand, int mapX, int mapZ) {
        World outerWorld = this.currentWorld;
        Random outerRandom = this.randomGenerator;
        int outerX = this.chunk_X;
        int outerZ = this.chunk_Z;
        this.currentWorld = world;
        this.randomGenerator = rand;
        this.chunk_X = mapX;
        this.chunk_Z = mapZ;
        this.generateOres();
        this.currentWorld = outerWorld;
        this.randomGenerator = outerRandom;
        this.chunk_X = outerX;
        this.chunk_Z = outerZ;
	}

	/**
	 * MITE's generateOres only knows the overworld and the underworld and reports an error for any other dimension, so
	 * supply the ore set of MITE_BALANCE 4.4: coal, iron, copper, redstone and lapis at reduced frequency; no silver, gold,
	 * diamond, emerald, mithril, adamantium or quartz (H2). MITE gravel (metal nuggets) is kept as in the overworld.
	 */
	@Override
	protected void generateOres() {
		this.genMinable(200, this.dirtGen);
		this.genMinable(200, this.gravelGen);
		if (!twilightforest.TFConfig.oreTerrain) {
			return;
		}
		this.genScaled(50, twilightforest.TFConfig.oreScaleCoal, this.coalGen, false);
		this.genScaled(60, twilightforest.TFConfig.oreScaleIron, this.ironGen, true);
		this.genScaled(40, twilightforest.TFConfig.oreScaleCopper, this.copperGen, true);
		this.genScaled(10, twilightforest.TFConfig.oreScaleRedstone, this.redstoneGen, false);
		this.genScaled(5, twilightforest.TFConfig.oreScaleLapis, this.lapisGen, false);
	}

	/** MITE genMinable with the frequency scaled and the vein height compressed by ore.heightScale. */
	private void genScaled(int mite, double scale, net.minecraft.WorldGenMinable gen, boolean deeperIsBigger) {
		int frequency = (int) (mite * scale);
		while (frequency-- > 0) {
			if (this.randomGenerator.nextInt(10) == 0) {
				int x = this.chunk_X + this.randomGenerator.nextInt(16);
				int y = gen.getRandomVeinHeight(this.currentWorld, this.randomGenerator);
				int z = this.chunk_Z + this.randomGenerator.nextInt(16);
				if (y >= 0) {
					gen.generate(this.currentWorld, this.randomGenerator, x, (int) (y * twilightforest.TFConfig.oreHeightScale), z, deeperIsBigger);
				}
			}
		}
	}

    /**
     * Gets a random feature suitible to the current biome.
     */
    public TFGenerator randomFeature(Random rand)
    {
    	return ((RuinEntry)WeightedRandom.getRandomItem(rand, ruinList)).generator;
    }
    
    public void setTreesPerChunk(int treesPerChunk) {
    	this.treesPerChunk = treesPerChunk;
    }
    
    public void setBigMushroomsPerChunk(int bigMushroomsPerChunk) {
    	this.bigMushroomsPerChunk = bigMushroomsPerChunk;
    }
    
    public void setClayPerChunk(int clayPerChunk) {
    	this.clayPerChunk = clayPerChunk;
    }
    
    public void setDeadBushPerChunk(int deadBushPerChunk) {
    	this.deadBushPerChunk = deadBushPerChunk;
    }
    
    /**
     * 1.7.10 mushroomsPerChunk; MITE's closest knob is surface_mushrooms_per_chunk.
     */
    public void setMushroomsPerChunk(int mushroomsPerChunk) {
    	this.surface_mushrooms_per_chunk = mushroomsPerChunk;
    }
    
    public int getMushroomsPerChunk() {
    	return this.surface_mushrooms_per_chunk;
    }
    
    public void setFlowersPerChunk(int flowersPerChunk) {
    	this.flowersPerChunk = flowersPerChunk;
    }
    
    public void setReedsPerChunk(int reedsPerChunk) {
    	this.reedsPerChunk = reedsPerChunk;
    }
    
    public void setWaterlilyPerChunk(int waterlilyPerChunk) {
    	this.waterlilyPerChunk = waterlilyPerChunk;
    }
    
    public void setGrassPerChunk(int grassPerChunk) {
    	this.grassPerChunk = grassPerChunk;
    }
    
    public void setCactiPerChunk(int cactiPerChunk) {
    	this.cactiPerChunk = cactiPerChunk;
    }
    
    public int getTreesPerChunk() {
    	return this.treesPerChunk;
    }
    
    public int getFlowersPerChunk() {
    	return this.flowersPerChunk;
    }
    
    public int getGrassPerChunk() {
    	return this.grassPerChunk;
    }
    
}
