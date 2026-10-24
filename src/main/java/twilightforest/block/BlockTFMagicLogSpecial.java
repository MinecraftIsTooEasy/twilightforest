package twilightforest.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.BiomeGenBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.IInventory;
import net.minecraft.Icon;
import net.minecraft.InventoryLargeChest;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.BlockBreakInfo;
import net.minecraft.World;
import net.minecraft.WorldServer;
import twilightforest.TFConfig;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.compat.TFItemStacks;
import twilightforest.item.ItemTFOreMagnet;
import twilightforest.network.TFNetwork;

/**
 * The core blocks of the four magic trees (time, transformation, mining, sorting). They tick once a second and apply
 * their effect; right clicking toggles them (metadata orientation bits 12 = switched off).
 */
public class BlockTFMagicLogSpecial extends BlockTFMagicLog {

	protected BlockTFMagicLogSpecial(int id) {
		super(id);
		// 1.7.10 getLightValue(IBlockAccess,..) was 15 for every metadata
		this.setLightValue(1.0F);
	}

	/**
	 * How many world ticks before ticking
	 */
	@Override
	public int tickRate(World world) {
		return 20;
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickDelay(world.getBlockMetadata(x, y, z)));
	}

	/**
	 * Ticks between two effects of the given tree type: trees.transformInterval / trees.sortInterval, one second otherwise.
	 */
	private int tickDelay(int meta) {
		switch (meta & 3) {
		case META_TRANS:
			return TFConfig.treesTransformInterval;
		case META_SORT:
			return TFConfig.treesSortInterval;
		default:
			return 20;
		}
	}

	/**
	 * Drops the normal magic log (the special effect is lost when the block is broken).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return this.dropBlockAsEntityItem(info, TFBlocks.magicLog.blockID, info.getMetadata() & 3, 1, 1.0F);
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		int orient = meta & 12;
		int woodType = meta & 3;

		if (orient == 12) {
			// off blocks
			switch (woodType) {
			default:
			case META_TIME:
				return (side == 1 || side == 0) ? SPR_TIMETOP : SPR_TIMECLOCKOFF;
			case META_TRANS:
				return (side == 1 || side == 0) ? SPR_TRANSTOP : SPR_TRANSHEARTOFF;
			case META_MINE:
				return (side == 1 || side == 0) ? SPR_MINETOP : SPR_MINEGEMOFF;
			case META_SORT:
				return (side == 1 || side == 0) ? SPR_SORTTOP : SPR_SORTEYEOFF;
			}
		} else {
			boolean end = orient == 0 && (side == 1 || side == 0) || orient == 4 && (side == 5 || side == 4) || orient == 8 && (side == 2 || side == 3);
			switch (woodType) {
			default:
			case META_TIME:
				return end ? SPR_TIMETOP : SPR_TIMECLOCK;
			case META_TRANS:
				return end ? SPR_TRANSTOP : SPR_TRANSHEART;
			case META_MINE:
				return end ? SPR_MINETOP : SPR_MINEGEM;
			case META_SORT:
				return end ? SPR_SORTTOP : SPR_SORTEYE;
			}
		}
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random rand) {
		int meta = world.getBlockMetadata(x, y, z);

		if ((meta & 12) == 12) {
			// block is off, do not tick
			return false;
		}

		if (!world.isRemote) {
			switch (meta & 3) {
			case META_TIME:
				// tree of time effect (trees.time)
				if (TFConfig.treesTime) {
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 0.1F, 0.5F);
					doTreeOfTimeEffect(world, x, y, z, rand);
				}
				break;
			case META_TRANS:
				// tree of transformation effect
				doTreeOfTransformationEffect(world, x, y, z, rand);
				break;
			case META_MINE:
				// miner's tree effect (trees.mining)
				if (TFConfig.treesMining) {
					doMinersTreeEffect(world, x, y, z, rand);
				}
				break;
			case META_SORT:
				// sorting tree effect
				doSortingTreeEffect(world, x, y, z, rand);
				break;
			}
		}

		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickDelay(meta));
		return false;
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float dx, float dy, float dz) {
		int meta = world.getBlockMetadata(x, y, z);

		int orient = meta & 12;
		int woodType = meta & 3;

		if (orient == 0) {
			// turn off
			world.setBlockMetadataWithNotify(x, y, z, woodType | 12, 3);
			return true;
		}
		if (orient == 12) {
			// turn on
			world.setBlockMetadataWithNotify(x, y, z, woodType | 0, 3);
			world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickDelay(woodType));
			return true;
		}

		return false;
	}

	/**
	 * The tree of time adds extra ticks to blocks, so that they have twice the normal chance to get a random tick
	 */
	private void doTreeOfTimeEffect(World world, int x, int y, int z, Random rand) {
		int numticks = 8 * 3 * this.tickRate(world);

		for (int i = 0; i < numticks; i++) {
			// find a nearby block
			int dx = rand.nextInt(32) - 16;
			int dy = rand.nextInt(32) - 16;
			int dz = rand.nextInt(32) - 16;

			Block there = world.getBlock(x + dx, y + dy, z + dz);

			if (there != null && there.getTickRandomly()) {
				there.updateTick(world, x + dx, y + dy, z + dz, rand);
			}
		}
	}

	/**
	 * The tree of transformation transforms the biome in the area near it into the enchanted forest biome.
	 *
	 * TODO: also change entities
	 */
	private void doTreeOfTransformationEffect(World world, int x, int y, int z, Random rand) {
		for (int i = 0; i < 1; i++) {
			int dx = rand.nextInt(32) - 16;
			int dz = rand.nextInt(32) - 16;

			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "note.harp", 0.1F, rand.nextFloat() * 2F);

			if (Math.sqrt(dx * dx + dz * dz) < 16) {
				BiomeGenBase biomeAt = world.getBiomeGenForCoords(x + dx, z + dz);

				if (biomeAt != TFBiomeBase.enchantedForest) {
					Chunk chunkAt = world.getChunkFromBlockCoords(x + dx, z + dz);

					chunkAt.getBiomeArray()[((z + dz) & 15) << 4 | ((x + dx) & 15)] = (byte) TFBiomeBase.enchantedForest.biomeID;
					chunkAt.isModified = true;

					world.markBlockForUpdate((x + dx), y, (z + dz));

					if (world instanceof WorldServer) {
						sendChangedBiome(world, x + dx, z + dz, chunkAt);
					}
				}
			}
		}
	}

	/**
	 * Send a tiny update packet to the client to inform it of the changed biome
	 */
	private void sendChangedBiome(World world, int x, int z, Chunk chunkAt) {
		TFNetwork.sendBiomeChange(world, x, z, (byte) TFBiomeBase.enchantedForest.biomeID);
	}

	/**
	 * The miner's tree generates the ore magnet effect randomly every second
	 */
	private void doMinersTreeEffect(World world, int x, int y, int z, Random rand) {
		int dx = rand.nextInt(64) - 32;
		int dy = rand.nextInt(64) - 32;
		int dz = rand.nextInt(64) - 32;

		int moved = ItemTFOreMagnet.doMagnet(world, x, y, z, x + dx, y + dy, z + dz);

		if (moved > 0) {
			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "mob.endermen.portal", 0.1F, 1.0F);
		}
	}

	/**
	 * The sorting tree finds two chests nearby and then attempts to sort a random item.
	 */
	private void doSortingTreeEffect(World world, int x, int y, int z, Random rand) {
		// find all the chests nearby
		int XSEARCH = TFConfig.treesSortRadius;
		int YSEARCH = TFConfig.treesSortRadius;
		int ZSEARCH = TFConfig.treesSortRadius;

		ArrayList<IInventory> chests = new ArrayList<IInventory>();
		int itemCount = 0;

		for (int sx = x - XSEARCH; sx < x + XSEARCH; sx++) {
			for (int sy = y - YSEARCH; sy < y + YSEARCH; sy++) {
				for (int sz = z - ZSEARCH; sz < z + ZSEARCH; sz++) {
					if (world.getBlock(sx, sy, sz) == Block.chest) {
						IInventory thisChest = Block.chest.getInventory(world, sx, sy, sz);

						// make sure we haven't counted this chest
						if (thisChest != null && !checkIfChestsContains(chests, (IInventory) world.getBlockTileEntity(sx, sy, sz))) {
							int itemsInChest = 0;

							// count items
							for (int i = 0; i < thisChest.getSizeInventory(); i++) {
								if (thisChest.getStackInSlot(i) != null) {
									itemsInChest++;
									itemCount++;
								}
							}

							// only add non-empty chests
							if (itemsInChest > 0) {
								chests.add(thisChest);
							}
						}
					}
				}
			}
		}

		// find a random item in one of the chests
		ItemStack beingSorted = null;
		int sortedChestNum = -1;
		int sortedSlotNum = -1;

		if (itemCount > 0) {
			int itemNumber = rand.nextInt(itemCount);
			int currentNumber = 0;

			for (int i = 0; i < chests.size(); i++) {
				IInventory chest = chests.get(i);
				for (int slotNum = 0; slotNum < chest.getSizeInventory(); slotNum++) {
					ItemStack currentItem = chest.getStackInSlot(slotNum);

					if (currentItem != null) {
						if (currentNumber++ == itemNumber) {
							beingSorted = currentItem;
							sortedChestNum = i;
							sortedSlotNum = slotNum;
						}
					}
				}
			}
		}

		if (beingSorted != null) {
			int matchChestNum = -1;
			int matchCount = 0;

			// decide where to put it, if anywhere
			for (int chestNum = 0; chestNum < chests.size(); chestNum++) {
				IInventory chest = chests.get(chestNum);
				int currentChestMatches = 0;

				for (int slotNum = 0; slotNum < chest.getSizeInventory(); slotNum++) {
					ItemStack currentItem = chest.getStackInSlot(slotNum);
					if (currentItem != null && isSortingMatch(beingSorted, currentItem)) {
						currentChestMatches += currentItem.stackSize;
					}
				}

				if (currentChestMatches > matchCount) {
					matchCount = currentChestMatches;
					matchChestNum = chestNum;
				}
			}

			// soooo, did we find a better match?
			if (matchChestNum >= 0 && matchChestNum != sortedChestNum) {
				IInventory moveChest = chests.get(matchChestNum);
				IInventory oldChest = chests.get(sortedChestNum);

				// is there an empty inventory slot in the new chest?
				int moveSlot = getEmptySlotIn(moveChest);

				if (moveSlot >= 0) {
					// remove old item
					oldChest.setInventorySlotContents(sortedSlotNum, null);

					// add new item
					moveChest.setInventorySlotContents(moveSlot, beingSorted);
				}
			}

			// if the stack is not full, combine items from other stacks
			if (beingSorted.stackSize < beingSorted.getMaxStackSize()) {
				for (IInventory chest : chests) {
					for (int slotNum = 0; slotNum < chest.getSizeInventory(); slotNum++) {
						ItemStack currentItem = chest.getStackInSlot(slotNum);

						if (currentItem != null && currentItem != beingSorted && TFItemStacks.sameItemAndSubtype(beingSorted, currentItem)) {
							if (currentItem.stackSize <= (beingSorted.getMaxStackSize() - beingSorted.stackSize)) {
								chest.setInventorySlotContents(slotNum, null);
								beingSorted.stackSize += currentItem.stackSize;
								currentItem.stackSize = 0;
							}
						}
					}
				}
			}
		}
	}

	private boolean isSortingMatch(ItemStack beingSorted, ItemStack currentItem) {
		// 1.7.10 read Item.tabToDisplayOn by reflection; MITE exposes it
		return currentItem.getItem().getCreativeTab() == beingSorted.getItem().getCreativeTab();
	}

	/**
	 * Is the chest we're testing part of our chest list already?
	 */
	private boolean checkIfChestsContains(ArrayList<IInventory> chests, IInventory testChest) {
		for (IInventory chest : chests) {
			if (chest == testChest) {
				return true;
			}

			if (chest instanceof InventoryLargeChest && ((InventoryLargeChest) chest).isPartOfLargeChest(testChest)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return an empty slot number in the chest, or -1 if the chest is full
	 */
	private int getEmptySlotIn(IInventory chest) {
		for (int i = 0; i < chest.getSizeInventory(); i++) {
			if (chest.getStackInSlot(i) == null) {
				return i;
			}
		}

		return -1;
	}
}
