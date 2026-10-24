package twilightforest.inventory;

import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import twilightforest.TwilightForestMod;

public class InventoryTFGoblinUncrafting implements IInventory {

	private ItemStack[] contents = new ItemStack[9];
	public int numberOfInputItems;
	/** Cost of taking the uncrafted items, in experience POINTS (MITE has no level cost, see ContainerTFUncrafting). */
	public int uncraftingCost;
	public int recraftingCost;

	public InventoryTFGoblinUncrafting(ContainerTFUncrafting containerTFGoblinCrafting) {
	}

	@Override
	public int getSizeInventory() {
		return 9;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return contents[var1];
	}

	@Override
	public ItemStack decrStackSize(int slotNum, int amount) {
		if (this.contents[slotNum] != null) {
			ItemStack takenStack;

			if (this.contents[slotNum].stackSize <= amount) {
				takenStack = this.contents[slotNum];
				this.contents[slotNum] = null;
				return takenStack;
			} else {
				takenStack = this.contents[slotNum].splitStack(amount);

				if (this.contents[slotNum].stackSize == 0) {
					this.contents[slotNum] = null;
				}

				return takenStack;
			}
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		if (this.contents[par1] != null) {
			ItemStack var2 = this.contents[par1];
			this.contents[par1] = null;
			return var2;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		this.contents[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit()) {
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}

		this.onInventoryChanged();
	}

	@Override
	public String getCustomNameOrUnlocalized() {
		return "twilightforest.goblincrafting";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return !ContainerTFUncrafting.isDisabled();
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
		return false;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public void destroyInventory() {
		for (int i = 0; i < this.contents.length; i++) {
			this.contents[i] = null;
		}
	}

}
