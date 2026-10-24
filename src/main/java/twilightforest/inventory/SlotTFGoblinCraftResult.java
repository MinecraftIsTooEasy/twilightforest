package twilightforest.inventory;

import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.InventoryCrafting;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SlotCraftingBase;

/**
 * Result slot of the uncrafting table's assembly grid. MITE's SlotCrafting only works inside a
 * MITEContainerCrafting (timed crafting, NPE on crafting_result otherwise), so this is based on SlotCraftingBase
 * and consumes the ingredients like the 1.6.4 slot did.
 */
public class SlotTFGoblinCraftResult extends SlotCraftingBase {

	/** The player that is using the GUI where this slot resides. */
	protected EntityPlayer thePlayer;
	protected IInventory inputSlot;
	protected InventoryTFGoblinUncrafting uncraftingMatrix;
	protected InventoryCrafting assemblyMatrix;
	protected IInventory resultSlot;

	public SlotTFGoblinCraftResult(EntityPlayer player, IInventory input, IInventory uncraftingMatrix, IInventory assemblyMatrix, IInventory result, int slotIndex, int x, int y) {
		super(player, result, slotIndex, x, y);
		this.thePlayer = player;
		this.inputSlot = input;
		this.uncraftingMatrix = (InventoryTFGoblinUncrafting) uncraftingMatrix;
		this.assemblyMatrix = (InventoryCrafting) assemblyMatrix;
		this.resultSlot = result;
	}

	/**
	 * Called when the player picks up an item from an inventory slot. The result stack is already on the cursor
	 * (or in the inventory when shift clicking), so only the ingredients are consumed here.
	 */
	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par1ItemStack) {
		// TODO(mite-port): recrafting/repair (the 1.7.10 "combined" branch charging recraftingCost levels and wiping the
		// uncrafting grid) is disabled by MITE_BALANCE (uncrafting table must not work as an anvil), so every result
		// here is a plain crafting result.
		this.onCrafting(par1ItemStack);

		for (int i = 0; i < this.assemblyMatrix.getSizeInventory(); i++) {
			ItemStack ingredient = this.assemblyMatrix.getStackInSlot(i);

			if (ingredient != null) {
				Item item = ingredient.getItem();
				int itemID = ingredient.itemID;
				int subtype = ingredient.getItemSubtype();
				boolean hasContainer = item.hasContainerItem();
				boolean containerLeaves = hasContainer && item.doesContainerItemLeaveCraftingGrid(ingredient);

				this.assemblyMatrix.decrStackSize(i, 1);

				if (hasContainer) {
					ItemStack containerStack = new ItemStack(item.getContainerItem());

					if (!containerLeaves || !par1EntityPlayer.inventory.addItemStackToInventory(containerStack)) {
						if (this.assemblyMatrix.getStackInSlot(i) == null) {
							this.assemblyMatrix.setInventorySlotContents(i, containerStack);
						} else {
							par1EntityPlayer.dropPlayerItem(containerStack);
						}
					}
				} else if (itemID == net.minecraft.Block.workbench.blockID) {
					// MITE: crafting with a workbench hands its component (e.g. the flint/obsidian wood block) back
					ItemStack component = net.minecraft.BlockWorkbench.getBlockComponent(subtype);
					if (component != null) {
						par1EntityPlayer.inventory.addItemStackToInventoryOrDropIt(component);
					}
				}
			}
		}
	}

}
