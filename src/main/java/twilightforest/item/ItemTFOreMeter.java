package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.block.BlockTFRoots;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFChat;

/** Creative-only (MITE_BALANCE 7.7): no recipe, no loot. */
public class ItemTFOreMeter extends ItemTF {

	/** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
	protected ItemTFOreMeter(int id) {
		super(id, Material.iron);
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
		int useX = MathHelper.floor_double(player.posX);
		int useZ = MathHelper.floor_double(player.posZ);

		if (player.onServer()) {
			countOreInArea(player, player.worldObj, useX, useZ, 3);
		} else {
			player.swingArm();
		}

		return true;
	}

	private void countOreInArea(EntityPlayer player, World world, int useX, int useZ, int radius) {
		int chunkX = useX >> 4;
		int chunkZ = useZ >> 4;

		int countStone = 0;
		int countDirt = 0;
		int countGravel = 0;

		int countCoal = 0;
		int countIron = 0;
		int countGold = 0;
		int countDiamond = 0;
		int countLapis = 0;
		int countRedstone = 0;
		int countExposedDiamond = 0;

		int countRoots = 0;
		int countOreRoots = 0;

		for (int cx = chunkX - radius; cx <= chunkX + radius; cx++) {
			for (int cz = chunkZ - radius; cz <= chunkZ + radius; cz++) {

				countStone += countBlockInChunk(world, Block.stone, cx, cz);
				countDirt += countBlockInChunk(world, Block.dirt, cx, cz);
				countGravel += countBlockInChunk(world, Block.gravel, cx, cz);

				countCoal += countBlockInChunk(world, Block.oreCoal, cx, cz);
				countIron += countBlockInChunk(world, Block.oreIron, cx, cz);
				countGold += countBlockInChunk(world, Block.oreGold, cx, cz);
				countDiamond += countBlockInChunk(world, Block.oreDiamond, cx, cz);
				countLapis += countBlockInChunk(world, Block.oreLapis, cx, cz);
				countRedstone += countBlockInChunk(world, Block.oreRedstone, cx, cz);
				countExposedDiamond += countExposedBlockInChunk(world, Block.oreDiamond, cx, cz);

				countRoots += countBlockInChunk(world, TFBlocks.root, BlockTFRoots.ROOT_META, cx, cz);
				countOreRoots += countBlockInChunk(world, TFBlocks.root, BlockTFRoots.OREROOT_META, cx, cz);
			}
		}

		int total = countStone + countDirt + countGravel + countCoal + countIron + countGold + countDiamond + countLapis + countRedstone + countRoots + countOreRoots;

		// The 1.7.10 original computed these and only had the (commented out) chat output; we print a compact summary.
		TFChat.sendTranslated(player, "tf.oremeter.header", radius, chunkX, chunkZ);
		TFChat.sendTranslated(player, "tf.oremeter.common", percentString(countCoal, total), percentString(countIron, total),
			percentString(countGold, total), percentString(countLapis, total), percentString(countRedstone, total));
		TFChat.sendTranslated(player, "tf.oremeter.rare", percentString(countDiamond, total), countExposedDiamond,
			percentString(countRoots, total), percentString(countOreRoots, total));
	}

	private static String percentString(int count, int total) {
		return String.format("%.3f%%", total == 0 ? 0F : (double) percent(count, total));
	}

	public static float percent(int count, int total) {
		return (float) count / (float) total * 100F;
	}

	public int countBlockInChunk(World world, Block block, int cx, int cz) {
		Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
		int id = block.blockID;
		int count = 0;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 256; y++) {
					if (chunk.getBlockID(x, y, z) == id) {
						count++;
					}
				}
			}
		}

		return count;
	}

	public int countBlockInChunk(World world, Block block, int meta, int cx, int cz) {
		Chunk chunk = world.getChunkFromChunkCoords(cx, cz);
		int id = block.blockID;
		int count = 0;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 256; y++) {
					if (chunk.getBlockID(x, y, z) == id && chunk.getBlockMetadata(x, y, z) == meta) {
						count++;
					}
				}
			}
		}

		return count;
	}

	private int countExposedBlockInChunk(World world, Block block, int cx, int cz) {
		int count = 0;

		for (int x = cx << 4; x < (cx << 4) + 16; x++) {
			for (int z = cz << 4; z < (cz << 4) + 16; z++) {
				for (int y = 0; y < 256; y++) {
					if (world.getBlock(x, y, z) == block) {
						// check if exposed
						if (world.isAirBlock(x + 1, y, z) || world.isAirBlock(x - 1, y, z)
							|| world.isAirBlock(x, y + 1, z) || world.isAirBlock(x, y - 1, z)
							|| world.isAirBlock(x, y, z + 1) || world.isAirBlock(x, y, z - 1)) {
							count++;
						}
					}
				}
			}
		}

		return count;
	}
}
