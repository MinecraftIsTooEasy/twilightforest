package twilightforest.structures.minotaurmaze;

import java.util.Random;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import twilightforest.TFProgress;
import twilightforest.TFTreasure;
import twilightforest.block.BlockTFBossSpawner;
import twilightforest.block.TFBlocks;
import twilightforest.entity.boss.EntityTFMinoshroom;

public class ComponentTFMazeRoomBoss extends ComponentTFMazeRoom {


	public ComponentTFMazeRoomBoss() {
		super();
		// TODO Auto-generated constructor stub
	}


	public ComponentTFMazeRoomBoss(int i, Random rand, int x, int y, int z) {
		super(i, rand, x, y, z);
	}


	/**
	 * Rooms generated before the spawner existed may have no Minoshroom. With a player inside who has not beaten it yet,
	 * no Minoshroom around and no spawner left, put the spawner back. Returns true when it placed one.
	 */
	public boolean restoreBossSpawner(World world, EntityPlayer player) {
		if (TFProgress.has(player, TFProgress.Flag.MINO)) {
			return false;
		}
		int bx = this.getXWithOffset(7, 7);
		int by = this.getYWithOffset(1);
		int bz = this.getZWithOffset(7, 7);
		if (!world.blockExists(bx, by, bz) || world.getBlockId(bx, by, bz) == TFBlocks.bossSpawner.blockID) {
			return false;
		}
		AxisAlignedBB room = AxisAlignedBB.getBoundingBox(boundingBox.minX, boundingBox.minY, boundingBox.minZ,
				boundingBox.maxX + 1, boundingBox.maxY + 1, boundingBox.maxZ + 1).expand(16.0D, 4.0D, 16.0D);
		if (!world.getEntitiesWithinAABB(EntityTFMinoshroom.class, room).isEmpty()) {
			return false;
		}
		world.setBlock(bx, by, bz, TFBlocks.bossSpawner.blockID, BlockTFBossSpawner.META_MINOSHROOM, 2);
		return true;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
		// doorways
		if (this.getBlockIdAtCurrentPosition(world, 7, 1, 0, sbb) == 0)
		{
			fillWithBlocks(world, sbb, 6, 1, 0, 9, 4, 0, Block.fence, null, false);
		}

		if (this.getBlockIdAtCurrentPosition(world, 7, 1, 15, sbb) == 0)
		{
			fillWithBlocks(world, sbb, 6, 1, 15, 9, 4, 15, Block.fence, null, false);
		}

		if (this.getBlockIdAtCurrentPosition(world, 0, 1, 7, sbb) == 0)
		{
			fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 9, Block.fence, null, false);
		}

		if (this.getBlockIdAtCurrentPosition(world, 15, 1, 7, sbb) == 0)
		{
			fillWithBlocks(world, sbb, 15, 1, 6, 15, 4, 9, Block.fence, null, false);
		}

		// mycelium / small mushrooms on floor
		for (int x = 1; x < 14; x++)
		{
			for (int z = 1; z < 14; z++)
			{
				// calculate distance from middle
				int dist = (int) Math.round(7 / Math.sqrt((7.5 - x) * (7.5 - x) + (7.5 - z) * (7.5 - z)));
				boolean mycelium = rand.nextInt(dist + 1) > 0;
				boolean mushroom = rand.nextInt(dist) > 0;
				boolean mushRed = rand.nextBoolean();

				// make part of the floor mycelium
				if (mycelium)
				{
					this.placeBlockAtCurrentPosition(world, Block.mycelium, 0, x, 0, z, sbb);
				}
				// add small mushrooms all over
				if (mushroom)
				{
					this.placeBlockAtCurrentPosition(world, mushRed ? Block.mushroomRed : Block.mushroomBrown, 0, x, 1, z, sbb);
				}
			}
		}
		
		// mushroom chest shelves in corner
		fillWithMetadataBlocks(world, sbb, 1, 1, 1, 3, 1, 3, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 1, 2, 1, 1, 3, 4, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 2, 2, 1, 4, 3, 1, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 1, 4, 1, 3, 4, 3, Block.mushroomCapRed, 14, null, 0, false);
		placeTreasureAtCurrentPosition(world, rand, 3, 2, 3, TFTreasure.labyrinth_room, sbb);

		fillWithMetadataBlocks(world, sbb, 12, 1, 12, 14, 1, 14, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 14, 2, 11, 14, 3, 14, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 11, 2, 14, 14, 3, 14, Block.mushroomCapRed, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 12, 4, 12, 14, 4, 14, Block.mushroomCapRed, 14, null, 0, false);
		placeTreasureAtCurrentPosition(world, rand, 12, 2, 12, TFTreasure.labyrinth_room, sbb);

		fillWithMetadataBlocks(world, sbb, 1, 1, 12, 3, 1, 14, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 1, 2, 11, 1, 3, 14, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 2, 2, 14, 4, 3, 14, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 1, 4, 12, 3, 4, 14, Block.mushroomCapBrown, 14, null, 0, false);
		placeTreasureAtCurrentPosition(world, rand, 3, 2, 12, TFTreasure.labyrinth_room, sbb);

		fillWithMetadataBlocks(world, sbb, 12, 1, 1, 14, 1, 3, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 11, 2, 1, 14, 3, 1, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 14, 2, 2, 14, 3, 4, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 12, 4, 1, 14, 4, 3, Block.mushroomCapBrown, 14, null, 0, false);
		placeTreasureAtCurrentPosition(world, rand, 12, 2, 3, TFTreasure.labyrinth_room, sbb);

		// a few more ceilingshrooms
		fillWithMetadataBlocks(world, sbb, 5, 4, 5, 7, 5, 7, Block.mushroomCapBrown, 14, null, 0, false);
		fillWithMetadataBlocks(world, sbb, 8, 4, 8, 10, 5, 10, Block.mushroomCapRed, 14, null, 0, false);

		
		// the moo-cen-mino-shrom-taur! a boss spawner: entities spawned during worldgen could go missing
		this.placeBlockAtCurrentPosition(world, TFBlocks.bossSpawner, BlockTFBossSpawner.META_MINOSHROOM, 7, 1, 7, sbb);
		
		
		return true;
	}
}
