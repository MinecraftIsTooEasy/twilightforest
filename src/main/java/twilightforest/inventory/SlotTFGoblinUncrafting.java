package twilightforest.inventory;

import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import twilightforest.TwilightForestMod;

public class SlotTFGoblinUncrafting extends Slot {

	protected EntityPlayer thePlayer;
	protected IInventory inputSlot;
	protected InventoryTFGoblinUncrafting uncraftingMatrix;
	protected IInventory assemblyMatrix;

	public SlotTFGoblinUncrafting(EntityPlayer par1EntityPlayer, IInventory inputSlot, InventoryTFGoblinUncrafting uncraftingMatrix, IInventory assemblyMatrix, int slotNum, int x, int y) {
		super(uncraftingMatrix, slotNum, x, y);
		this.thePlayer = par1EntityPlayer;
		this.inputSlot = inputSlot;
		this.uncraftingMatrix = uncraftingMatrix;
		this.assemblyMatrix = assemblyMatrix;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always false: nothing is put into this matrix.
	 */
	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		return false;
	}

	/**
	 * Return whether this slot's stack can be taken from this slot.
	 */
	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
		// MITE slot lock protocol (client locks a slot until the server confirms)
		if (!super.canTakeStack(par1EntityPlayer)) {
			return false;
		}

		// broken / missing components cannot be taken (1.7.10 did this check in Container.slotClick)
		ItemStack stack = this.getStack();
		if (stack == null || stack.stackSize <= 0) {
			return false;
		}

		// if there is anything in the assembly matrix, then you cannot have these items
		for (int i = 0; i < this.assemblyMatrix.getSizeInventory(); i++) {
			if (this.assemblyMatrix.getStackInSlot(i) != null) {
				return false;
			}
		}

		// if uncrafting is disabled, no!
		if (ContainerTFUncrafting.isDisabled()) {
			return false;
		}

		// if you don't have enough XP, no (MITE_BALANCE: cost is experience points, not levels)
		if (this.uncraftingMatrix.uncraftingCost > par1EntityPlayer.experience && !par1EntityPlayer.inCreativeMode()) {
			return false;
		}

		return true;
	}

	/**
	 * Called when the player picks up an item from an inventory slot
	 */
	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par1ItemStack) {
		super.onPickupFromSlot(par1EntityPlayer, par1ItemStack);

		// charge the player for this (the server owns the experience value, the client gets it synchronised)
		if (this.uncraftingMatrix.uncraftingCost > 0 && !par1EntityPlayer.inCreativeMode() && !this.thePlayer.worldObj.isRemote) {
			this.thePlayer.addExperience(-this.uncraftingMatrix.uncraftingCost, true, true);
		}

		// move all remaining items from the uncrafting grid to the assembly grid
		// the assembly grid should be empty for this to even happen, so just plop the items right in
		for (int i = 0; i < 9; i++) {
			ItemStack transferStack = this.uncraftingMatrix.getStackInSlot(i);
			if (transferStack != null && transferStack.stackSize > 0) {
				this.assemblyMatrix.setInventorySlotContents(i, transferStack.copy());
			}
		}

		// decrement the inputslot by the recipe output size
		// do this second so that it doesn't change the contents of the uncrafting grid
		ItemStack inputStack = this.inputSlot.getStackInSlot(0);
		if (inputStack != null) {
			this.inputSlot.decrStackSize(0, uncraftingMatrix.numberOfInputItems);
		}
	}

}
