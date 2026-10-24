package twilightforest.inventory;

import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.Slot;

public class SlotTFGoblinAssembly extends Slot {

	public SlotTFGoblinAssembly(EntityPlayer par1EntityPlayer, IInventory inputSlot, IInventory assemblyMatrix, IInventory uncraftingMatrix, int slotNum, int x, int y) {
		super(assemblyMatrix, slotNum, x, y);
	}

}
