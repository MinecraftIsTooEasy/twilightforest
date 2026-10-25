package twilightforest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.Block;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.TileEntityChest;
import net.minecraft.World;

/**
 * Boss loot goes into a chest where the boss died instead of onto the ground (lava, void, water, melting ice items and
 * despawn timers ate it before).
 * <p>
 * A boss overrides dropItemStack to hand its drops to {@link #capture}, between {@link #begin} and {@link #finish} around
 * super.onDeath. Whatever the normal drop path produced (dropFewItems, dropEquipment, ...) is therefore the chest content,
 * so MITE's own rules (doMobLoot, massive fall damage, looting, repeat kills) still decide what appears. Server side only;
 * when no safe spot exists the items are dropped as before, so nothing is ever lost.
 */
public final class TFBossLoot {

	/** How far from the death position a free chest spot is searched (blocks). */
	private static final int SEARCH_RADIUS = 6;
	/** How far below / above the death height the floor is searched. */
	private static final int SEARCH_DOWN = 24;
	private static final int SEARCH_UP = 3;

	private TFBossLoot() {
	}

	/** Start collecting the drops of a dying boss (null on the client: nothing is collected there). */
	public static List<ItemStack> begin(Entity boss) {
		return boss.worldObj.isRemote ? null : new ArrayList<ItemStack>();
	}

	/** Called from the dropItemStack override: true when the stack was taken into the chest list. */
	public static boolean capture(List<ItemStack> collected, ItemStack stack) {
		if (collected == null) {
			return false;
		}
		if (stack != null && stack.stackSize > 0) {
			collected.add(stack);
		}
		return true;
	}

	/** Put the collected drops into a chest (or two) at the boss; call right after super.onDeath. */
	public static void finish(Entity boss, List<ItemStack> collected) {
		if (collected == null || collected.isEmpty()) {
			return;
		}
		World world = boss.worldObj;
		List<ItemStack> stacks = merge(collected);
		boolean needDouble = stacks.size() > 27;

		int[] spot = findSpot(world, boss, needDouble);
		if (spot == null && needDouble) {
			// no room for two chests side by side: fill one chest and let the rest fall as items
			spot = findSpot(world, boss, false);
		}
		if (spot == null) {
			dropOnGround(boss, stacks, 0);
			return;
		}

		int chests = needDouble && isSpot(world, spot[0] + 1, spot[1], spot[2]) ? 2 : 1;
		for (int c = 0; c < chests; c++) {
			int x = spot[0] + c;
			world.setBlock(x, spot[1], spot[2], Block.chest.blockID, 2, 3);
		}
		int placed = 0;
		for (int c = 0; c < chests; c++) {
			TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(spot[0] + c, spot[1], spot[2]);
			if (chest == null) {
				break;
			}
			for (int slot = 0; slot < 27 && placed < stacks.size(); slot++) {
				chest.setInventorySlotContents(slot, stacks.get(placed++));
			}
		}
		// anything that did not fit (or a chest without tile entity) still drops
		dropOnGround(boss, stacks, placed);
	}

	private static void dropOnGround(Entity boss, List<ItemStack> stacks, int from) {
		for (int i = from; i < stacks.size(); i++) {
			EntityItem item = new EntityItem(boss.worldObj, boss.posX, boss.posY + boss.height * 0.5F, boss.posZ, stacks.get(i));
			item.delayBeforeCanPickup = 10;
			boss.worldObj.spawnEntityInWorld(item);
		}
	}

	/** Merge equal stacks up to their max stack size so ice / snowball / chop piles take few slots. */
	private static List<ItemStack> merge(List<ItemStack> collected) {
		List<ItemStack> out = new ArrayList<ItemStack>();
		for (ItemStack stack : collected) {
			ItemStack rest = stack.copy();
			for (ItemStack have : out) {
				if (rest.stackSize <= 0) {
					break;
				}
				if (have.isStackable() && ItemStack.areItemStacksEqual(have, rest, true) && have.stackSize < have.getMaxStackSize()) {
					int move = Math.min(rest.stackSize, have.getMaxStackSize() - have.stackSize);
					have.stackSize += move;
					rest.stackSize -= move;
				}
			}
			if (rest.stackSize > 0) {
				out.add(rest);
			}
		}
		return out;
	}

	/** Nearest column around the boss with a dry replaceable block on a solid floor and room to open the lid. */
	private static int[] findSpot(World world, Entity boss, boolean needPair) {
		int bx = (int) Math.floor(boss.posX);
		int by = (int) Math.floor(boss.posY);
		int bz = (int) Math.floor(boss.posZ);
		for (int r = 0; r <= SEARCH_RADIUS; r++) {
			for (int dx = -r; dx <= r; dx++) {
				for (int dz = -r; dz <= r; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
						continue;
					}
					int x = bx + dx;
					int z = bz + dz;
					for (int y = by + SEARCH_UP; y >= by - SEARCH_DOWN; y--) {
						if (y < 2 || y >= world.getHeight() - 2) {
							continue;
						}
						if (isSpot(world, x, y, z) && (!needPair || isSpot(world, x + 1, y, z))) {
							return new int[] { x, y, z };
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isSpot(World world, int x, int y, int z) {
		if (!isFree(world, x, y, z) || !isFree(world, x, y + 1, z)) {
			return false;
		}
		int below = world.getBlockId(x, y - 1, z);
		return below != 0 && !world.getBlockMaterial(x, y - 1, z).isLiquid() && world.isBlockNormalCube(x, y - 1, z);
	}

	private static boolean isFree(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z)) {
			return true;
		}
		Material material = world.getBlockMaterial(x, y, z);
		return !material.isLiquid() && material.isReplaceable();
	}
}
