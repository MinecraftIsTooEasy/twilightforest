package twilightforest.structures;

import java.util.List;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import net.minecraft.StructureComponent;
import twilightforest.TFConfig;
import twilightforest.TFTreasure;
import twilightforest.entity.TFCreatures;
import twilightforest.world.TFGenCaveStalactite;
import twilightforest.entity.EntityTFFireBeetle;
import twilightforest.entity.EntityTFPinchBeetle;
import twilightforest.entity.EntityTFRedcap;
import twilightforest.entity.EntityTFSlimeBeetle;
import twilightforest.entity.EntityTFSwarmSpider;
import twilightforest.entity.EntityTFWraith;



public class ComponentTFHollowHill extends StructureTFComponent {
	
	int hillSize;
	/** Ore stalactites placed in this hill so far (not persisted; counts what this session generated). */
	private int oreStalactitesPlaced;
	private final java.util.Map<TFGenCaveStalactite, Integer> oreStalactiteKindCounts = new java.util.HashMap<TFGenCaveStalactite, Integer>();
	int radius;

	public ComponentTFHollowHill() {
		super();
		// TODO Auto-generated constructor stub
	}



	public ComponentTFHollowHill(World world, Random rand, int i, int size, int x, int y, int z) {
		super(i);

		this.setCoordBaseMode(0);
		
		// get the size of this hill?
		this.hillSize = size;
		radius = ((hillSize * 2 + 1) * 8) - 6;

		// can we determine the size here?
		this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -radius, -3, -radius, radius * 2, 10, radius * 2, 0);
	}
	
	
	/**
	 * Save to NBT
	 */
	@Override
	protected void func_143012_a(NBTTagCompound par1NBTTagCompound) {
		super.func_143012_a(par1NBTTagCompound);
		
        par1NBTTagCompound.setInteger("hillSize", this.hillSize);
	}

	/**
	 * Load from NBT
	 */
	@Override
	protected void func_143011_b(NBTTagCompound par1NBTTagCompound) {
		super.func_143011_b(par1NBTTagCompound);
        this.hillSize = par1NBTTagCompound.getInteger("hillSize");
        this.radius = ((hillSize * 2 + 1) * 8) - 6;

	}

	/**
	 * Add on any other components we need.  In this case we add the maze below the hill
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
		
//		// add a maze
//		ComponentTFHillMaze maze = new ComponentTFHillMaze(1, boundingBox.minX + ((boundingBox.maxX - boundingBox.minX) / 2), boundingBox.minY - 20, boundingBox.minZ + ((boundingBox.maxZ - boundingBox.minZ) / 2), hillSize);
//		list.add(maze);
//		maze.buildComponent(this, list, random);
		
		
	}

	/**
	 * Add in all the blocks we're adding.
	 */
	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
//		int area = (int)(Math.PI * radius * radius);
//		int sn = area / 16; // number of stalactites (there will actually be around twice this number)
		int[] sna = {0, 128, 256, 512};
		int sn = sna[hillSize]; // number of stalactites mga = {0, 3, 9, 18}
		int[] mga = {0, 1, 4, 9};
		int mg = mga[hillSize]; // number of monster generators mga = {0, 3, 9, 18} (reduced due to "natural" spawning)
		int[] tca = {0, 2, 6, 12};
		int tc = tca[hillSize];  // number of treasure chests tca = {0, 2, 6, 12};

		// fill in features
		
		// monster generators!
		for (int i = 0; i < mg; i++)
		{
			int[] dest = getCoordsInHill2D(rand);
			String mobID = getMobID(rand);
			
			placeSpawnerAtCurrentPosition(world, rand, dest[0],rand.nextInt(4), dest[1], mobID, sbb);
//			placeMobSpawner(dest[0], hy + rand.nextInt(4), dest[1]);
		}
		// treasure chests!!
		for (int i = 0; i < tc; i++)
		{
			int[] dest = getCoordsInHill2D(rand);
			generateTreasureChest(world, dest[0], 0, dest[1], sbb);
		}

		// ore or glowing stalactites! (smaller, less plentiful)
		for (int i = 0; i < sn; i++)
		{
			int[] dest = getCoordsInHill2D(rand);
			generateOreStalactite(world, dest[0], 1, dest[1], sbb);
		}
		// stone stalactites!
		for (int i = 0; i < sn; i++)
		{
			int[] dest = getCoordsInHill2D(rand);
			generateBlockStalactite(world, Block.stone, 1.0F, true, dest[0], 1, dest[1], sbb);
		}
		// stone stalagmites!
		for (int i = 0; i < sn; i++)
		{
			int[] dest = getCoordsInHill2D(rand);
			generateBlockStalactite(world, Block.stone, 0.9F, false, dest[0], 1, dest[1], sbb);
		}
		
		// level 3 hills get 2 mid-air wraith spawners
		if (hillSize == 3)
		{
//			int[] dest = getEmptyCoordsInHill(hy + 10, 20);
//			placeWraithSpawner(dest[0], hy + 10, dest[1]);
//			dest = getEmptyCoordsInHill(hy + 10, 20);
//			placeWraithSpawner(dest[0], hy + 10, dest[1]);
		}


		
		return true;
	}
	
	/**
	 * Make an RNG and attempt to use it to place a treasure chest
	 */
	protected void generateTreasureChest(World world, int x, int y, int z, StructureBoundingBox sbb) {
		// generate an RNG for this chest
    	//TODO: MOAR RANDOM!
    	Random chestRNG = new Random(world.getSeed() + x * z);
    	
    	// try placing it
    	placeTreasureAtCurrentPosition(world, chestRNG, x, y, z, this.hillSize == 3 ? TFTreasure.hill3 : (this.hillSize == 2 ? TFTreasure.hill2 : TFTreasure.hill1), sbb);
    	
    	// make something for it to stand on, if necessary
        fillCurrentPositionBlocksDownwards(world, Block.cobblestone, 0, x, y - 1, z, sbb);

	}

	/**
	 * Generate a random ore stalactite
	 */
	protected void generateOreStalactite(World world, int x, int y, int z, StructureBoundingBox sbb) {
		// are the coordinates in our bounding box?
        int dx = getXWithOffset(x, z);
        int dy = getYWithOffset(y);
        int dz = getZWithOffset(x, z);
        if(sbb.isVecInside(dx, dy, dz) && world.getBlock(dx, dy, dz) != Block.mobSpawner)
        {
        	// generate an RNG for this stalactite
        	//TODO: MOAR RANDOM!
        	Random stalRNG = new Random(world.getSeed() + dx * dz);
        	
        	// pick a kind that is still below the per-hill caps (MITE_BALANCE 12, hill.oreCap.*); re-pick a few times, then skip
        	int totalCap = hillSize >= 3 ? TFConfig.hillOreCapHill3 : (hillSize == 2 ? TFConfig.hillOreCapHill2 : TFConfig.hillOreCapHill1);
        	if (oreStalactitesPlaced >= totalCap) {
        		return;
        	}
        	// a level 3 hill spans ~50 chunks; thin the attempts so the caps are not used up by the chunks generated first
        	if (hillSize >= 3 && stalRNG.nextFloat() >= Math.min(1.0F, totalCap * 2.0F / 512)) {
        		return;
        	}
        	TFGenCaveStalactite stalag = null;
        	for (int attempt = 0; attempt < 6; attempt++) {
        		TFGenCaveStalactite candidate = TFGenCaveStalactite.makeRandomOreStalactite(stalRNG, hillSize);
        		if (oreStalactiteKindCount(candidate) < oreStalactiteKindCap(candidate)) {
        			stalag = candidate;
        			break;
        		}
        	}
        	if (stalag == null) {
        		return;
        	}

        	// make the actual stalactite
			if (stalag.generate(world, stalRNG, dx, dy, dz)) {
				oreStalactitesPlaced++;
				oreStalactiteKindCounts.merge(stalag, 1, Integer::sum);
			}
        }
	}

	private int oreStalactiteKindCount(TFGenCaveStalactite kind) {
		Integer n = oreStalactiteKindCounts.get(kind);
		return n == null ? 0 : n;
	}

	/**
	 * Per-kind cap of ore stalactites of one hill (MITE_BALANCE 12). Order: coal, iron, copper, glowstone, redstone, gold,
	 * lapis, diamond, emerald; the hill size picks the row.
	 */
	private int oreStalactiteKindCap(TFGenCaveStalactite kind) {
		int[][] caps = {
			/* hill1 */ {10, 8, 6, 4, 2, 0, 0, 0, 0},
			/* hill2 */ {8, 8, 0, 4, 8, 4, 2, 1, 0},
			/* hill3 */ {16, 16, 4, 10, 20, 12, 8, 5, 2}
		};
		int[] row = caps[Math.max(1, Math.min(3, hillSize)) - 1];
		if (kind == TFGenCaveStalactite.coal) return row[0];
		if (kind == TFGenCaveStalactite.iron) return row[1];
		if (kind == TFGenCaveStalactite.copper) return row[2];
		if (kind == TFGenCaveStalactite.glowstone) return row[3];
		if (kind == TFGenCaveStalactite.redstone) return row[4];
		if (kind == TFGenCaveStalactite.gold) return row[5];
		if (kind == TFGenCaveStalactite.lapis) return row[6];
		if (kind == TFGenCaveStalactite.diamond) return row[7];
		if (kind == TFGenCaveStalactite.emerald) return row[8];
		return 0;
	}

	/**
	 * Make a random stone stalactite
	 */
	protected void generateBlockStalactite(World world, Block blockToGenerate, float length, boolean up, int x, int y, int z, StructureBoundingBox sbb) {
		// are the coordinates in our bounding box?
        int dx = getXWithOffset(x, z);
        int dy = getYWithOffset(y);
        int dz = getZWithOffset(x, z);
        if(sbb.isVecInside(dx, dy, dz) && world.getBlock(dx, dy, dz) != Block.mobSpawner)
        {
        	// generate an RNG for this stalactite
        	//TODO: MOAR RANDOM!
        	Random stalRNG = new Random(world.getSeed() + dx * dz);
        	
        	if (hillSize == 1) {
        		length *= 1.9F;
        	}
        	
        	// make the actual stalactite
        	(new TFGenCaveStalactite(blockToGenerate, length, up)).generate(world, stalRNG, dx, dy, dz);
        }
	}


	/**
	 * 
	 * @param cx
	 * @param cz
	 * @return true if the coordinates would be inside the hill on the "floor" of the hill
	 */
	boolean isInHill(int cx, int cz)
	{
		int dx = radius - cx;
		int dz = radius - cz;
		int dist = (int) Math.sqrt(dx * dx + dz * dz);
		
		return dist < radius;
	}
	
	/**
	 * @return true if the coordinates are inside the hill in 3D
	 */
	boolean isInHill(int mapX, int mapY, int mapZ)
	{
		int dx = boundingBox.minX + radius - mapX;
		int dy = (boundingBox.minY - mapY) * 2; // hill is half as high as it is wide, thus we just double y distance from center.  I *think* that math works!
		int dz = boundingBox.minZ + radius - mapZ;
		int dist = dx * dx + dy * dy + dz * dz;
		return dist < radius * radius;
	}
	
	int[] getCoordsInHill2D(Random rand)
	{
		return getCoordsInHill2D(rand, radius);
	}
	
	/**
	 * 
	 * @return a two element array containing some coordinates in the hill
	 */
	int[] getCoordsInHill2D(Random rand, int rad)
	{
		int rx, rz;
		do {
			rx = rand.nextInt(2 * rad);
			rz = rand.nextInt(2 * rad);
		} while (!isInHill(rx, rz));
		
		int[] coords = {rx, rz};
		return coords;
	}
	
	/**
	 * Gets the id of a mob appropriate to the current hill size.
	 */
	protected String getMobID(Random rand)
	{
		return getMobID(rand, this.hillSize);
	}

	
	/**
	 * Gets the id of a mob appropriate to the specified hill size.
	 * 
	 * @param level
	 * @return
	 */
	protected String getMobID(Random rand, int level)
	{
		if (level == 1)
		{
			return getLevel1Mob(rand);
		}
		if (level == 2)
		{
			return getLevel2Mob(rand);
		}
		if (level == 3)
		{
			return getLevel3Mob(rand);
		}
		
		/// aaah, default: spider!
		return "Spider";
	}
	
	/**
	 * Returns a mob string appropriate for a level 1 hill
	 */
	public String getLevel1Mob(Random rand)
	{
		switch (rand.nextInt(10))
		{
		case 0:
		case 1:
		case 2:
			return TFCreatures.getSpawnerNameFor(EntityTFSwarmSpider.class);
		case 3:
		case 4:
		case 5:
			return "Spider";
		case 6:
		case 7:
			return "Zombie";
		case 8:
			return "Silverfish";
		case 9:
			return TFCreatures.getSpawnerNameFor(EntityTFRedcap.class);
		default:
			return TFCreatures.getSpawnerNameFor(EntityTFSwarmSpider.class);
		}
	}
	
	/**
	 * Returns a mob string appropriate for a level 2 hill
	 */
	public String getLevel2Mob(Random rand)
	{
		switch (rand.nextInt(10))
		{
		case 0:
		case 1:
		case 2:
			return TFCreatures.getSpawnerNameFor(EntityTFRedcap.class);
		case 3:
		case 4:
		case 5:
			return "Zombie";
		case 6:
		case 7:
			return "Skeleton";
		case 8:
			return TFCreatures.getSpawnerNameFor(EntityTFSwarmSpider.class);
		case 9:
			return "CaveSpider";
		default:
			return TFCreatures.getSpawnerNameFor(EntityTFRedcap.class);
		}
	}
	
	/**
	 * Returns a mob string appropriate for a level 3 hill.  The level 3 also has 2 mid-air wraith spawners.
	 */
	public String getLevel3Mob(Random rand)
	{
		switch (rand.nextInt(11))
		{
		case 0:
			return TFCreatures.getSpawnerNameFor(EntityTFSlimeBeetle.class);
		case 1:
			return TFCreatures.getSpawnerNameFor(EntityTFFireBeetle.class);
		case 2:
			return TFCreatures.getSpawnerNameFor(EntityTFPinchBeetle.class);
		case 3:
		case 4:
		case 5:
			return "Skeleton";
		case 6:
		case 7:
		case 8:
			return "CaveSpider";
		case 9:
			return "Creeper";
		case 10:
		default:
			return TFCreatures.getSpawnerNameFor(EntityTFWraith.class);
		}
	}

}
