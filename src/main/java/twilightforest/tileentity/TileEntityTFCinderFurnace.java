package twilightforest.tileentity;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.FurnaceRecipes;
import net.minecraft.ISidedInventory;
import net.minecraft.Item;
import net.minecraft.ItemBucket;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.TileEntity;
import twilightforest.block.BlockTFCinderFurnace;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFTags;
import twilightforest.compat.TFWorldUtil;

/**
 * Cinder furnace: a furnace that is accelerated (and multiplies logs, ores and so on) by the cinder logs around it, and
 * that turns nearby logs into cinder logs while it burns.
 * <p>
 * MITE smelting depends on a heat level (see {@code TileEntityFurnace}); the cinder furnace has a maximum heat of 2
 * ({@link #MAX_HEAT_LEVEL}, MITE_BALANCE 8.2), so it cannot smelt mithril or adamantium.
 */
public class TileEntityTFCinderFurnace extends TileEntity implements ISidedInventory {

	public static final int MAX_HEAT_LEVEL = 2;

	private static final int SMELT_LOG_FACTOR = 10;
	private static final int SLOT_INPUT = 0;
	private static final int SLOT_FUEL = 1;
	private static final int SLOT_OUTPUT = 2;

	private static final int[] slotsTop = new int[] { SLOT_INPUT };
	private static final int[] slotsBottom = new int[] { SLOT_OUTPUT, SLOT_FUEL };
	private static final int[] slotsSides = new int[] { SLOT_FUEL };

	/** The ItemStacks that hold the items currently being used in the furnace */
	private ItemStack[] furnaceItemStacks = new ItemStack[3];
	/** The number of ticks that the furnace will keep burning */
	public int furnaceBurnTime;
	/** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
	public int currentItemBurnTime;
	/** The number of ticks that the current item has been cooking for */
	public int furnaceCookTime;
	/** MITE heat level of the fuel that is currently burning (0 when not burning) */
	public int heat_level;

	@Override
	public int getSizeInventory() {
		return this.furnaceItemStacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.furnaceItemStacks[slot];
	}

	/**
	 * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
	 * new stack.
	 */
	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (this.furnaceItemStacks[slot] != null) {
			ItemStack itemstack;

			if (this.furnaceItemStacks[slot].stackSize <= amount) {
				itemstack = this.furnaceItemStacks[slot];
				this.furnaceItemStacks[slot] = null;
				return itemstack;
			} else {
				itemstack = this.furnaceItemStacks[slot].splitStack(amount);

				if (this.furnaceItemStacks[slot].stackSize == 0) {
					this.furnaceItemStacks[slot] = null;
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
	 * like when you close a workbench GUI.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.furnaceItemStacks[slot] != null) {
			ItemStack itemstack = this.furnaceItemStacks[slot];
			this.furnaceItemStacks[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		this.furnaceItemStacks[slot] = itemStack;

		if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
			itemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	/**
	 * Returns the unlocalized name of the inventory (the custom name is handled by TileEntity)
	 */
	@Override
	public String getUnlocalizedInvName() {
		return "twilightforest.container.furnace";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags) {
		super.readFromNBT(nbtTags);
		NBTTagList nbttaglist = nbtTags.getTagList("Items");
		this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			byte slot = nbttagcompound1.getByte("Slot");

			if (slot >= 0 && slot < this.furnaceItemStacks.length) {
				this.furnaceItemStacks[slot] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		this.furnaceBurnTime = nbtTags.getShort("BurnTime");
		this.furnaceCookTime = nbtTags.getShort("CookTime");
		this.currentItemBurnTime = nbtTags.hasKey("CurrentItemBurnTime") ? nbtTags.getShort("CurrentItemBurnTime") : getItemBurnTime(this.furnaceItemStacks[SLOT_FUEL]);
		this.heat_level = nbtTags.getByte("heat_level");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags) {
		super.writeToNBT(nbtTags);
		nbtTags.setShort("BurnTime", (short) this.furnaceBurnTime);
		nbtTags.setShort("CookTime", (short) this.furnaceCookTime);
		nbtTags.setShort("CurrentItemBurnTime", (short) this.currentItemBurnTime);
		nbtTags.setByte("heat_level", (byte) this.heat_level);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.furnaceItemStacks.length; ++i) {
			if (this.furnaceItemStacks[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.furnaceItemStacks[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbtTags.setTag("Items", nbttaglist);
	}

	/**
	 * Returns an integer between 0 and the passed value representing how close the current item is to being completely
	 * cooked
	 */
	public int getCookProgressScaled(int scale) {
		return this.furnaceCookTime * scale / 200;
	}

	/**
	 * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
	 * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
	 */
	public int getBurnTimeRemainingScaled(int scale) {
		if (this.currentItemBurnTime == 0) {
			this.currentItemBurnTime = 200;
		}

		return this.furnaceBurnTime * scale / this.currentItemBurnTime;
	}

	/**
	 * Furnace isBurning
	 */
	public boolean isBurning() {
		return this.furnaceBurnTime > 0;
	}

	@Override
	public void updateEntity() {
		boolean wasBurning = this.furnaceBurnTime > 0;
		boolean changed = false;

		if (this.furnaceBurnTime > 0) {
			--this.furnaceBurnTime;
		} else {
			this.heat_level = 0;
		}

		if (!this.worldObj.isRemote) {
			if (this.furnaceBurnTime != 0 || this.furnaceItemStacks[SLOT_FUEL] != null && this.furnaceItemStacks[SLOT_INPUT] != null) {
				if (this.furnaceBurnTime == 0 && this.canSmelt(this.getFuelHeatLevel())) {
					this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[SLOT_FUEL]);

					if (this.furnaceBurnTime > 0) {
						this.heat_level = getItemHeatLevel(this.furnaceItemStacks[SLOT_FUEL]);
						changed = true;

						if (this.furnaceItemStacks[SLOT_FUEL] != null) {
							--this.furnaceItemStacks[SLOT_FUEL].stackSize;

							if (this.furnaceItemStacks[SLOT_FUEL].stackSize == 0) {
								Item container = this.furnaceItemStacks[SLOT_FUEL].getItem().getContainerItem();
								this.furnaceItemStacks[SLOT_FUEL] = container != null ? new ItemStack(container) : null;
							}
						}
					}
				}

				if (this.isBurning() && this.canSmelt(this.heat_level)) {
					this.furnaceCookTime += this.getCurrentSpeedMultiplier();

					if (this.furnaceCookTime >= 200) {
						this.furnaceCookTime = 0;
						this.smeltItem();
						changed = true;
					}
				} else {
					this.furnaceCookTime = 0;
				}
			}

			// update block if needed
			if (wasBurning != this.furnaceBurnTime > 0) {
				changed = true;
				BlockTFCinderFurnace.updateFurnaceBlockState(this.furnaceBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}

			// occasionally cinderize nearby logs
			if (this.isBurning() && this.furnaceBurnTime % 5 == 0) {
				this.cinderizeNearbyLog();
			}
		}

		if (changed) {
			this.onInventoryChanged();
		}
	}

	/**
	 * Turn a nearby log into a cinder log
	 */
	private void cinderizeNearbyLog() {
		Random rand = this.worldObj.rand;

		int dx = rand.nextInt(2) - rand.nextInt(2);
		int dy = rand.nextInt(2) - rand.nextInt(2);
		int dz = rand.nextInt(2) - rand.nextInt(2);

		int x = this.xCoord + dx;
		int y = this.yCoord + dy;
		int z = this.zCoord + dz;

		if (this.worldObj.blockExists(x, y, z)) {
			Block nearbyBlock = this.worldObj.getBlock(x, y, z);

			if (nearbyBlock != null && nearbyBlock != TFBlocks.cinderLog && this.isLog(nearbyBlock)) {
				TFWorldUtil.setBlock(this.worldObj, x, y, z, TFBlocks.cinderLog, getCinderMetaFor(dx, dy, dz), 2);
				// special effect?
				this.worldObj.playAuxSFX(2004, x, y, z, 0);
				this.worldObj.playAuxSFX(2004, x, y, z, 0);
				this.worldObj.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, "fire.fire", 1.0F, 1.0F);
			}
		}
	}

	/**
	 * What meta should we set the log block with the specified offset to?
	 */
	private int getCinderMetaFor(int dx, int dy, int dz) {
		if (dz == 0 && dx != 0) {
			return dy == 0 ? 4 : 8;
		} else if (dx == 0 && dz != 0) {
			return dy == 0 ? 8 : 4;
		} else if (dx == 0 && dz == 0) {
			return 0;
		} else {
			return dy == 0 ? 0 : 12;
		}
	}

	/**
	 * Is a nearby block a wood log block (the "logWood" ore dictionary tag in 1.7.10)
	 */
	private boolean isLog(Block nearbyBlock) {
		return TFTags.isLogWood(nearbyBlock);
	}

	/**
	 * What is the current speed multiplier, as an int.
	 */
	private int getCurrentSpeedMultiplier() {
		return getCurrentMultiplier(2);
	}

	/**
	 * Returns a number that is based on the number of nearby logs divided by the factor given.
	 */
	private int getCurrentMultiplier(int factor) {
		int logs = this.countNearbyLogs();

		if (logs < factor) {
			return 1;
		} else {
			return (logs / factor) + (this.worldObj.rand.nextInt(factor) >= (logs % factor) ? 0 : 1);
		}
	}

	/**
	 * Search around the block and return how many (out of a possible 26) of the blocks nearby are cinder log blocks
	 */
	private int countNearbyLogs() {
		int count = 0;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					int x = this.xCoord + dx;
					int y = this.yCoord + dy;
					int z = this.zCoord + dz;
					if (this.worldObj.blockExists(x, y, z) && this.worldObj.getBlock(x, y, z) == TFBlocks.cinderLog) {
						count++;
					}
				}
			}
		}

		return count;
	}

	/**
	 * The smelting result at the given heat level, or null.
	 */
	private ItemStack getResult(int heat) {
		return FurnaceRecipes.smelting().getSmeltingResult(this.furnaceItemStacks[SLOT_INPUT], heat);
	}

	/**
	 * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
	 */
	private boolean canSmelt(int heat) {
		if (this.furnaceItemStacks[SLOT_INPUT] == null) {
			return false;
		}
		// cap at what the cinder furnace can reach (the fuel slot only accepts fuel up to MAX_HEAT_LEVEL anyway)
		ItemStack outputStack = this.getResult(Math.min(heat, MAX_HEAT_LEVEL));
		if (outputStack == null) {
			return false;
		}
		if (this.furnaceItemStacks[SLOT_OUTPUT] == null) {
			return true;
		}
		if (!ItemStack.areItemStacksEqual(this.furnaceItemStacks[SLOT_OUTPUT], outputStack, true, false, true, true)) {
			return false;
		}

		int resultStackSize = this.furnaceItemStacks[SLOT_OUTPUT].stackSize + this.getMaxOutputStacks(this.furnaceItemStacks[SLOT_INPUT], outputStack);

		return resultStackSize <= this.getInventoryStackLimit() && resultStackSize <= this.furnaceItemStacks[SLOT_OUTPUT].getMaxStackSize();
	}

	/**
	 * Return the max number of items in the output stack, given our current multiplier
	 */
	public int getMaxOutputStacks(ItemStack input, ItemStack output) {
		if (this.canMultiply(input, output)) {
			return output.stackSize * this.getCurrentMaxSmeltMultiplier();
		} else {
			return output.stackSize;
		}
	}

	/**
	 * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
	 */
	public void smeltItem() {
		if (this.canSmelt(this.heat_level)) {
			ItemStack input = this.furnaceItemStacks[SLOT_INPUT];
			ItemStack outputStack = this.getResult(Math.min(this.heat_level, MAX_HEAT_LEVEL));

			if (this.canMultiply(input, outputStack)) {
				// multiply number of results by our current smelt factor
				int smeltMultiplier = this.getCurrentSmeltMultiplier();

				if (smeltMultiplier > 1) {
					// copy output stack
					outputStack = outputStack.copy();

					// we shouldn't run into max stack size problems, since we've already checked in canSmelt()
					outputStack.stackSize *= smeltMultiplier;
				}
			}

			if (this.furnaceItemStacks[SLOT_OUTPUT] == null) {
				this.furnaceItemStacks[SLOT_OUTPUT] = outputStack.copy();
			} else if (this.furnaceItemStacks[SLOT_OUTPUT].itemID == outputStack.itemID) {
				this.furnaceItemStacks[SLOT_OUTPUT].stackSize += outputStack.stackSize;
			}

			// MITE: sand needs 4 per sandstone / glass
			int consumption = input.itemID == Block.sand.blockID ? 4 : 1;
			input.stackSize -= consumption;

			if (this.furnaceItemStacks[SLOT_INPUT].stackSize <= 0) {
				this.furnaceItemStacks[SLOT_INPUT] = null;
			}
		}
	}

	/**
	 * Can we multiply the output?  We currently multiply logs->charcoal and ore->ingots
	 */
	public boolean canMultiply(ItemStack input, ItemStack output) {
		return TFTags.isOre(input) || TFTags.isLogWood(input);
	}

	/**
	 * What is the current smelt multiplier, as an int.
	 */
	private int getCurrentSmeltMultiplier() {
		return getCurrentMultiplier(SMELT_LOG_FACTOR);
	}

	/**
	 * What is the maximum smelt multiplier, as an int.
	 */
	private int getCurrentMaxSmeltMultiplier() {
		return (int) Math.ceil((float) this.countNearbyLogs() / (float) SMELT_LOG_FACTOR);
	}

	/**
	 * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
	 * fuel
	 */
	public static int getItemBurnTime(ItemStack stack) {
		return stack == null ? 0 : stack.getItem().getBurnTime(stack);
	}

	/**
	 * MITE heat level of the fuel, 0 if it is no fuel
	 */
	public static int getItemHeatLevel(ItemStack stack) {
		return stack == null ? 0 : stack.getItem().getHeatLevel(stack);
	}

	public int getFuelHeatLevel() {
		return getItemHeatLevel(this.furnaceItemStacks[SLOT_FUEL]);
	}

	/**
	 * Fuel that this furnace can use: any fuel up to the maximum heat level
	 */
	public static boolean isItemFuel(ItemStack stack) {
		int heat = getItemHeatLevel(stack);
		return heat > 0 && heat <= MAX_HEAT_LEVEL;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false
				: player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public void destroyInventory() {
		for (int i = 0; i < this.furnaceItemStacks.length; i++) {
			this.furnaceItemStacks[i] = null;
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		if (slot == SLOT_OUTPUT) {
			return false;
		}
		if (slot == SLOT_FUEL) {
			return isItemFuel(itemStack);
		}
		return FurnaceRecipes.smelting().doesSmeltingRecipeExistFor(itemStack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return side == 0 ? slotsBottom : (side == 1 ? slotsTop : slotsSides);
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
	 * side
	 */
	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
	 * side
	 */
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		if (side != 0 || slot != SLOT_FUEL) {
			return true;
		}
		return itemStack.getItem() instanceof ItemBucket bucket && bucket.isEmpty();
	}
}
