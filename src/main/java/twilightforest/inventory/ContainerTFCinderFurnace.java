package twilightforest.inventory;

import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.FurnaceRecipes;
import net.minecraft.ICrafting;
import net.minecraft.InventoryPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import net.minecraft.SlotFurnace;
import net.minecraft.World;
import twilightforest.tileentity.TileEntityTFCinderFurnace;

public class ContainerTFCinderFurnace extends Container {
	private TileEntityTFCinderFurnace tileFurnace;
	private int lastCookTime;
	private int lastBurnTime;
	private int lastItemBurnTime;

	public ContainerTFCinderFurnace(InventoryPlayer inventory, World world, int x, int y, int z) {
		this(inventory, (TileEntityTFCinderFurnace) world.getBlockTileEntity(x, y, z));
	}

	public ContainerTFCinderFurnace(InventoryPlayer inventory, TileEntityTFCinderFurnace furnace) {
		super(inventory.player);
		this.tileFurnace = furnace;
		this.addSlotToContainer(new Slot(furnace, 0, 56, 17, true));
		this.addSlotToContainer(new Slot(furnace, 1, 56, 53, true) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return TileEntityTFCinderFurnace.isItemFuel(stack) && super.isItemValid(stack);
			}
		});
		this.addSlotToContainer(new SlotFurnace(inventory.player, furnace, 2, 116, 35));
		int i;

		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting crafter) {
		super.addCraftingToCrafters(crafter);
		crafter.sendProgressBarUpdate(this, 0, this.tileFurnace.furnaceCookTime);
		crafter.sendProgressBarUpdate(this, 1, this.tileFurnace.furnaceBurnTime);
		crafter.sendProgressBarUpdate(this, 2, this.tileFurnace.currentItemBurnTime);
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting) this.crafters.get(i);

			if (this.lastCookTime != this.tileFurnace.furnaceCookTime) {
				icrafting.sendProgressBarUpdate(this, 0, this.tileFurnace.furnaceCookTime);
			}

			if (this.lastBurnTime != this.tileFurnace.furnaceBurnTime) {
				icrafting.sendProgressBarUpdate(this, 1, this.tileFurnace.furnaceBurnTime);
			}

			if (this.lastItemBurnTime != this.tileFurnace.currentItemBurnTime) {
				icrafting.sendProgressBarUpdate(this, 2, this.tileFurnace.currentItemBurnTime);
			}
		}

		this.lastCookTime = this.tileFurnace.furnaceCookTime;
		this.lastBurnTime = this.tileFurnace.furnaceBurnTime;
		this.lastItemBurnTime = this.tileFurnace.currentItemBurnTime;
	}

	@Override
	public void updateProgressBar(int id, int value) {
		if (id == 0) {
			this.tileFurnace.furnaceCookTime = value;
		}

		if (id == 1) {
			this.tileFurnace.furnaceBurnTime = value;
		}

		if (id == 2) {
			this.tileFurnace.currentItemBurnTime = value;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tileFurnace.isUseableByPlayer(player);
	}

	/**
	 * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotIndex == 2) {
				if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (slotIndex != 1 && slotIndex != 0) {
				if (FurnaceRecipes.smelting().doesSmeltingRecipeExistFor(itemstack1)) {
					if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
						return null;
					}
				} else if (TileEntityTFCinderFurnace.isItemFuel(itemstack1)) {
					if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
						return null;
					}
				} else if (slotIndex >= 3 && slotIndex < 30) {
					if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
						return null;
					}
				} else if (slotIndex >= 30 && slotIndex < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
					return null;
				}
			} else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}
}
