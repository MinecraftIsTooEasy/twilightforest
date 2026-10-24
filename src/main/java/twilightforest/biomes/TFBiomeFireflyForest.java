package twilightforest.biomes;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.World;
import net.minecraft.WorldGenPumpkin;
import net.minecraft.WorldGenTallGrass;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;
import twilightforest.world.TFGenHangingLamps;
import twilightforest.world.TFGenLampposts;
import twilightforest.world.TFWorld;

public class TFBiomeFireflyForest extends TFBiomeTwilightForest {

    private static final int LAMPPOST_CHANCE = 4;
    TFGenHangingLamps tfGenHangingLamps;
    TFGenLampposts tfGenLampposts;
    WorldGenTallGrass worldGenMushgloom;

    public TFBiomeFireflyForest(int i) {
        super(i);

        this.worldGenMushgloom = new WorldGenTallGrass(TFBlocks.plant.blockID, BlockTFPlant.META_MUSHGLOOM);
        this.tfGenHangingLamps = new TFGenHangingLamps();
        this.tfGenLampposts = new TFGenLampposts();

        this.temperature = 0.5F;
        this.rainfall = 1.0F;

        getTFBiomeDecorator().setFlowersPerChunk(4);
        getTFBiomeDecorator().setGrassPerChunk(1);

        this.getTFBiomeDecorator().setTreesPerChunk(2);

    }

    /**
     * Decorate this biome.
     */
    @Override
    public void decorate(World world, Random rand, int mapX, int mapZ) {
        // TODO(mite-port): flower-forest style tall flowers (WorldGenDoublePlant / genTallFlowers) do not exist in 1.6.4

        super.decorate(world, rand, mapX, mapZ);

        if (rand.nextInt(24) == 0) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = getGroundLevel(world, rx, rz);
            // mushglooms
            this.worldGenMushgloom.generate(world, rand, rx, ry, rz);
        }

        // hanging lamps
        for (int i = 0; i < 30; i++) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = TFWorld.SEALEVEL + rand.nextInt(TFWorld.CHUNKHEIGHT - TFWorld.SEALEVEL);

            this.tfGenHangingLamps.generate(world, rand, rx, ry, rz);
        }

        if (rand.nextInt(LAMPPOST_CHANCE) == 0) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = getGroundLevel(world, rx, rz);

            this.tfGenLampposts.generate(world, rand, rx, ry, rz);
        }

        // extra pumpkins (should they be lit?)
        if (rand.nextInt(32) == 0) {
            int rx = mapX + rand.nextInt(16) + 8;
            int rz = mapZ + rand.nextInt(16) + 8;
            int ry = getGroundLevel(world, rx, rz);
            (new WorldGenPumpkin()).generate(world, rand, rx, ry, rz);
        }

    }

    public int getGroundLevel(World world, int x, int z) {
        // go from sea level up.  If we get grass, return that, otherwise return the last dirt, stone or gravel we got
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        int lastDirt = TFWorld.SEALEVEL;
        for (int y = TFWorld.SEALEVEL; y < TFWorld.CHUNKHEIGHT - 1; y++) {
            int blockID = chunk.getBlockID(x & 15, y, z & 15);
            // grass = return immediately
            if (blockID == Block.grass.blockID) {
                return y + 1;
            } else if (blockID == Block.dirt.blockID || blockID == Block.stone.blockID || blockID == Block.gravel.blockID) {
                lastDirt = y + 1;
            }
        }

        return lastDirt;
    }

    // TODO(mite-port): func_150572_a ("every colour flower") has no MITE hook (no BlockFlower subtype name tables)
}
