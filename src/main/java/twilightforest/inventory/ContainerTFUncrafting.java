package twilightforest.inventory;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.Block;
import net.minecraft.Container;
import net.minecraft.CraftingManager;
import net.minecraft.CraftingResult;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.IRecipe;
import net.minecraft.InventoryCraftResult;
import net.minecraft.InventoryCrafting;
import net.minecraft.InventoryPlayer;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemAxe;
import net.minecraft.ItemBlock;
import net.minecraft.ItemBow;
import net.minecraft.ItemCoin;
import net.minecraft.ItemHoe;
import net.minecraft.ItemIngot;
import net.minecraft.ItemNugget;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemShovel;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.Material;
import net.minecraft.ShapedRecipes;
import net.minecraft.Slot;
import net.minecraft.World;
import twilightforest.TFConfig;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFRegistry;

/**
 * The uncrafting table container.
 *
 * MITE adaptations (MITE_BALANCE, uncrafting=whitelist):
 * <ul>
 * <li>only shaped recipes exist (no ShapedOreRecipe in MITE);</li>
 * <li>only TF items and non-metal MITE items can be uncrafted (no MITE metals, no coins);</li>
 * <li>ingots come back as 3 nuggets, scales/furs/liveroot at half rate, sticks are lost, all scaled by the wear of the input;</li>
 * <li>the cost is experience POINTS, round(recipe difficulty / 5), instead of levels;</li>
 * <li>recrafting / repairing (the "anvil" function with the combine matrix) is disabled;</li>
 * <li>Container.slotClick is final in MITE: the 1.7.10 slot redirection lives in {@link #tf$remapSlot} and is called by a mixin.</li>
 * </ul>
 */
public class ContainerTFUncrafting extends Container {

	/** XP points = round(difficulty / divisor), divisor = config uncrafting.costDivisor (MITE_BALANCE 12). */
	public static float costDivisor() {
		return Math.max(1, TFConfig.uncraftingCostDivisor);
	}

	/** uncrafting=off (or the legacy disableUncrafting flag): the table cannot take anything apart. */
	public static boolean isDisabled() {
		return TwilightForestMod.disableUncrafting || "off".equalsIgnoreCase(TFConfig.uncrafting);
	}

	/** uncrafting=vanilla: the original Twilight Forest behaviour, no whitelist and no Minoshroom gate. */
	public static boolean isVanillaMode() {
		return "vanilla".equalsIgnoreCase(TFConfig.uncrafting);
	}
	/** Nuggets returned for one ingot ingredient (1/3 of its value, MITE_BALANCE). */
	public static final int NUGGETS_PER_INGOT = 3;

	/** The crafting matrix inventory (3x3). */
	public InventoryTFGoblinUncrafting uncraftingMatrix = new InventoryTFGoblinUncrafting(this);
	public InventoryCrafting assemblyMatrix = new InventoryCrafting(this, 3, 3);
	public IInventory tinkerInput = new InventoryTFGoblinInput(this);
	public IInventory tinkerResult = new InventoryCraftResult();

	private final int posX;
	private final int posY;
	private final int posZ;

	/** Uncrafting cost of the current input, only charged while the assembly grid is empty. */
	private int baseUncraftingCost;
	private ItemStack lastInput;

	private static Field recipeWidthField;
	private static Field recipeHeightField;
	private static boolean reflectionTried;

	public ContainerTFUncrafting(InventoryPlayer inventory, World world, int x, int y, int z) {
		super(inventory.player);
		this.posX = x;
		this.posY = y;
		this.posZ = z;

		this.addSlotToContainer(new Slot(this.tinkerInput, 0, 13, 35));
		this.addSlotToContainer(new SlotTFGoblinCraftResult(inventory.player, this.tinkerInput, this.uncraftingMatrix, this.assemblyMatrix, this.tinkerResult, 0, 147, 35));

		int invX;
		int invY;

		for (invX = 0; invX < 3; ++invX) {
			for (invY = 0; invY < 3; ++invY) {
				this.addSlotToContainer(new SlotTFGoblinUncrafting(inventory.player, this.tinkerInput, this.uncraftingMatrix, this.assemblyMatrix, invY + invX * 3, 300000 + invY * 18, 17 + invX * 18));
			}
		}
		for (invX = 0; invX < 3; ++invX) {
			for (invY = 0; invY < 3; ++invY) {
				this.addSlotToContainer(new SlotTFGoblinAssembly(inventory.player, this.tinkerInput, this.assemblyMatrix, this.uncraftingMatrix, invY + invX * 3, 62 + invY * 18, 17 + invX * 18));
			}
		}

		for (invX = 0; invX < 3; ++invX) {
			for (invY = 0; invY < 9; ++invY) {
				this.addSlotToContainer(new Slot(inventory, invY + invX * 9 + 9, 8 + invY * 18, 84 + invX * 18));
			}
		}

		for (invX = 0; invX < 9; ++invX) {
			this.addSlotToContainer(new Slot(inventory, invX, 8 + invX * 18, 142));
		}

		this.onCraftMatrixChanged(this.assemblyMatrix);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Container.slotClick is final in MITE. A mixin on Container.slotClick calls these two hooks (see mixin request).
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Replaces the start of the 1.7.10 slotClick override: if the player is trying to take an item out of an empty
	 * assembly grid, take the item from the matching uncrafting slot instead. Returns the slot to really click.
	 */
	public int tf$remapSlot(int slotNum, int button, int mode, EntityPlayer player) {
		if (slotNum > 0 && slotNum < this.inventorySlots.size() && player.inventory.getItemStack() == null) {
			Slot clicked = (Slot) this.inventorySlots.get(slotNum);
			if (clicked.inventory == this.assemblyMatrix && !clicked.getHasStack() && isMatrixEmpty(this.assemblyMatrix)) {
				return slotNum - 9;
			}
		}
		return slotNum;
	}

	/** Replaces the end of the 1.7.10 slotClick override: an input slot click always refreshes the grids. */
	public void tf$afterClick(int slotNum, int button, int mode, EntityPlayer player) {
		if (slotNum > 0 && slotNum < this.inventorySlots.size() && ((Slot) this.inventorySlots.get(slotNum)).inventory instanceof InventoryTFGoblinInput) {
			this.onCraftMatrixChanged(this.tinkerInput);
		}
	}

	/**
	 * Without the slotClick mixin the input stack can still grow in place (merging with the cursor stack) without any
	 * inventory callback; notice that here so numberOfInputItems / the grid stay right.
	 */
	@Override
	public void detectAndSendChanges() {
		ItemStack input = this.tinkerInput.getStackInSlot(0);
		if (!ItemStack.areItemStacksEqual(this.lastInput, input)) {
			this.onCraftMatrixChanged(this.tinkerInput);
		}
		super.detectAndSendChanges();
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory par1IInventory) {
		// we need to see what inventory is calling this, and update appropriately
		if (par1IInventory == this.tinkerInput) {
			// see if there is a recipe for the input
			ItemStack inputStack = tinkerInput.getStackInSlot(0);
			this.lastInput = ItemStack.copyItemStack(inputStack);
			IRecipe recipe = getRecipeFor(inputStack);

			if (recipe != null) {
				// empty whole grid to start with
				// let's not get leftovers if something changes like the recipe size
				for (int i = 0; i < this.uncraftingMatrix.getSizeInventory(); i++) {
					this.uncraftingMatrix.setInventorySlotContents(i, null);
				}

				this.baseUncraftingCost = fillUncraftingGrid(inputStack, recipe);

				// store number of items this recipe produces (and thus how many input items are required for uncrafting)
				this.uncraftingMatrix.numberOfInputItems = recipe.getRecipeOutput().stackSize;
				this.uncraftingMatrix.uncraftingCost = calculateUncraftingCost();
				this.uncraftingMatrix.recraftingCost = 0;
			} else {
				for (int i = 0; i < 9; i++) {
					this.uncraftingMatrix.setInventorySlotContents(i, null);
				}
				this.baseUncraftingCost = 0;
				this.uncraftingMatrix.numberOfInputItems = 0;
				this.uncraftingMatrix.uncraftingCost = 0;
			}
		}

		if (par1IInventory == this.assemblyMatrix || par1IInventory == this.tinkerInput) {
			if (isMatrixEmpty(this.tinkerInput)) {
				// display the output
				this.tinkerResult.setInventorySlotContents(0, findCraftingResult(this.assemblyMatrix));
				this.uncraftingMatrix.recraftingCost = 0;
			} else {
				// TODO(mite-port): the 1.7.10 "combine matrix" (repair / recraft the input item with items in both grids,
				// inheriting enchantments) is disabled by MITE_BALANCE; with an input item there is no crafting result.
				this.tinkerResult.setInventorySlotContents(0, null);
				this.uncraftingMatrix.uncraftingCost = calculateUncraftingCost();
				this.uncraftingMatrix.recraftingCost = 0;
			}
		}
	}

	/**
	 * Crafting result of the assembly grid. Does not use CraftingManager.findMatchingRecipe: that one contains MITE's
	 * "merge two damaged items" repair branches, which would turn the table into a free repair bench.
	 */
	@SuppressWarnings("rawtypes")
	protected ItemStack findCraftingResult(InventoryCrafting matrix) {
		boolean empty = true;
		for (int i = 0; i < matrix.getSizeInventory(); i++) {
			if (matrix.getStackInSlot(i) != null) {
				empty = false;
				break;
			}
		}
		if (empty) {
			return null;
		}

		for (Object o : (List) CraftingManager.getInstance().getRecipeList()) {
			IRecipe recipe = (IRecipe) o;
			if (recipe.matches(matrix, this.world)) {
				CraftingResult result = recipe.getCraftingResult(matrix);
				if (result == null || result.item_stack == null || result.item_stack.getItem() == null) {
					return null;
				}
				Item item = result.item_stack.getItem();
				// this table has no crafting time / bench tier / skill checks, so it must not hand out gated items
				if (item instanceof ItemCoin || isMetalForbidden(item)) {
					return null;
				}
				return result.item_stack.copy();
			}
		}
		return null;
	}

	/**
	 * Problematic ingredients are not allowed to be uncrafted in the uncrafting table.
	 */
	protected boolean isIngredientProblematic(ItemStack ingredient) {
		return ingredient != null && (ingredient.getItem().hasContainerItem() || ingredient.getUnlocalizedName().contains("itemMatter"));
	}

	/**
	 * Get the first valid shaped recipe for the item in the input
	 */
	@SuppressWarnings("rawtypes")
	public IRecipe getRecipeFor(ItemStack inputStack) {
		if (inputStack != null && inputStack.getItem() != null && isUncraftable(inputStack.getItem())) {
			for (Object o : (List) CraftingManager.getInstance().getRecipeList()) {
				IRecipe recipe = (IRecipe) o;
				ItemStack output = recipe.getRecipeOutput();
				if (recipe instanceof ShapedRecipes && output != null && output.getItem() == inputStack.getItem()
						&& inputStack.stackSize >= output.stackSize
						&& (!output.getHasSubtypes() || output.getItemSubtype() == inputStack.getItemSubtype())) {
					return recipe;
				}
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Whitelist (MITE_BALANCE: TF items and non-metal MITE items only)
	// ------------------------------------------------------------------------------------------------------------

	/** Is this item allowed to be taken apart? (uncrafting=whitelist; "off" see {@link #isDisabled()}, "vanilla" lifts the whitelist.) */
	public static boolean isUncraftable(Item item) {
		// /tf test mode lifts the whitelist, so every craftable item can be taken apart
		if (item != null && (twilightforest.TFTestMode.isOn() || isVanillaMode())) {
			return true;
		}
		return item != null && !(item instanceof ItemCoin) && !isMetalForbidden(item);
	}

	/** Items made of a MITE metal (copper ... adamantium) are forbidden unless they are TF items / TF materials. */
	public static boolean isMetalForbidden(Item item) {
		Material hardest = item.getHardestMetalMaterial();
		if (hardest == null && !item.containsMetal()) {
			return false;
		}
		if (isTFItem(item)) {
			return false;
		}
		return hardest != null && isTFClass(hardest.getClass()) ? false : true;
	}

	private static boolean isTFClass(Class<?> clazz) {
		return clazz.getName().startsWith("twilightforest.");
	}

	private static boolean isTFItem(Item item) {
		if (isTFClass(item.getClass())) {
			return true;
		}
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			return block != null && isTFClass(block.getClass());
		}
		return false;
	}

	/** Scale / fur / liveroot style ingredients come back at half rate. */
	private static boolean isHalfReturnItem(Item item) {
		return item != null && (item == TFRegistry.findItem("nagaScale") || item == TFRegistry.findItem("alphaYetiFur")
				|| item == TFRegistry.findItem("arcticFur") || item == TFRegistry.findItem("liveRoot"));
	}

	/** The nugget that stands for 1/9 of this ingot, or null when the item is not an ingot / has no known nugget. */
	private static Item getNuggetFor(Item item) {
		if (!(item instanceof ItemIngot) && !isTFIngotName(item)) {
			return null;
		}
		if (item instanceof ItemNugget) {
			return null;
		}

		String name = item.getUnlocalizedName();
		if (name != null) {
			if (name.startsWith("item.")) {
				name = name.substring(5);
			}
			String lower = name.toLowerCase();
			if (lower.endsWith("ingot")) {
				Item nugget = TFRegistry.findItem(name.substring(0, name.length() - 5) + "Nugget");
				if (nugget != null) {
					return nugget;
				}
			} else if (lower.equals("knightmetal")) {
				Item nugget = TFRegistry.findItem(name + "Nugget");
				if (nugget != null) {
					return nugget;
				}
			}
		}

		// MITE's own ingots
		Material metal = item.getHardestMetalMaterial();
		if (metal != null && Item.ironNugget != null) {
			return Item.ironNugget.getForMaterial(metal);
		}
		// TODO(mite-port): TF nugget item names are owned by the TFItems unit; if no nugget is found the ingot yields nothing
		return null;
	}

	private static boolean isTFIngotName(Item item) {
		if (item == null || !isTFClass(item.getClass())) {
			return false;
		}
		String name = item.getUnlocalizedName();
		return name != null && (name.toLowerCase().endsWith("ingot") || name.equalsIgnoreCase("item.knightmetal"));
	}

	/**
	 * Fills the uncrafting grid for the input, applying the MITE return rules. Returns the XP point cost (0 when nothing
	 * can be taken).
	 */
	protected int fillUncraftingGrid(ItemStack inputStack, IRecipe recipe) {
		int recipeWidth = getRecipeWidth(recipe);
		int recipeHeight = getRecipeHeight(recipe);
		ItemStack[] recipeItems = getRecipeItems(recipe);
		if (recipeItems == null || recipeWidth <= 0 || recipeHeight <= 0) {
			return 0;
		}

		// the share of the input that is still intact
		float wear = 0.0F;
		if (inputStack.isItemDamaged() && inputStack.getMaxDamage() > 0) {
			wear = Math.min(1.0F, Math.max(0.0F, (float) inputStack.getItemDamage() / (float) inputStack.getMaxDamage()));
		}

		ItemStack[] grid = new ItemStack[9];
		for (int invY = 0; invY < recipeHeight; invY++) {
			for (int invX = 0; invX < recipeWidth; invX++) {
				int index = invX + invY * recipeWidth;
				if (index >= recipeItems.length || invX + invY * 3 >= 9) {
					continue;
				}
				ItemStack ingredient = ItemStack.copyItemStack(recipeItems[index]);
				if (ingredient == null) {
					continue;
				}
				// fix weird recipe for diamond/ingot blocks
				if (ingredient.stackSize > 1) {
					ingredient.stackSize = 1;
				}
				// wildcard subtype
				if (ingredient.getItemSubtype() == Short.MAX_VALUE) {
					ingredient.setItemSubtype(0);
				}
				grid[invX + invY * 3] = ingredient;
			}
		}

		// count the ordinary (non-nugget, non-half) damageable parts to spread the wear over
		int plainParts = 0;
		for (ItemStack ingredient : grid) {
			if (ingredient != null && isDamageableComponent(ingredient) && getNuggetFor(ingredient.getItem()) == null && !isHalfReturnItem(ingredient.getItem())) {
				plainParts++;
			}
		}
		int damagedParts = wear > 0.0F ? (int) Math.ceil(Math.max(4, plainParts) * wear) : 0;

		// totals of the half-return items, they are paid out in the first slot that holds them
		int[] halfIndexToCount = new int[9];
		for (int i = 0; i < 9; i++) {
			if (grid[i] != null && isHalfReturnItem(grid[i].getItem())) {
				int total = 0;
				int firstIndex = -1;
				for (int j = 0; j < 9; j++) {
					if (grid[j] != null && grid[j].getItem() == grid[i].getItem()) {
						total++;
						if (firstIndex < 0) {
							firstIndex = j;
						}
					}
				}
				halfIndexToCount[firstIndex] = total;
			}
		}

		boolean anyObtainable = false;
		for (int i = 0; i < 9; i++) {
			ItemStack ingredient = grid[i];
			if (ingredient == null) {
				continue;
			}
			Item item = ingredient.getItem();

			if (!isDamageableComponent(ingredient)) {
				// sticks are not returned
				grid[i] = null;
				continue;
			}

			if (isIngredientProblematic(ingredient)) {
				// banned items are marked damaged
				ingredient.stackSize = 0;
				continue;
			}

			Item nugget = getNuggetFor(item);
			if (nugget != null) {
				int amount = (int) Math.floor(NUGGETS_PER_INGOT * (1.0F - wear));
				if (amount > 0) {
					grid[i] = new ItemStack(nugget, amount);
					anyObtainable = true;
				} else {
					ingredient.stackSize = 0;
				}
				continue;
			}

			if (isHalfReturnItem(item)) {
				if (halfIndexToCount[i] > 0) {
					int amount = (int) Math.floor(halfIndexToCount[i] * 0.5F * (1.0F - wear));
					if (amount > 0) {
						ingredient.stackSize = amount;
						anyObtainable = true;
					} else {
						ingredient.stackSize = 0;
					}
				} else {
					// merged into the first slot holding this item
					grid[i] = null;
				}
				continue;
			}

			// ordinary component: mark the appropriate number of components damaged
			if (damagedParts > 0) {
				ingredient.stackSize = 0;
				damagedParts--;
			} else {
				anyObtainable = true;
			}
		}

		for (int i = 0; i < 9; i++) {
			this.uncraftingMatrix.setInventorySlotContents(i, grid[i]);
		}

		if (!anyObtainable) {
			return 0;
		}
		return Math.max(1, Math.round(recipe.getUnmodifiedDifficulty() / costDivisor()));
	}

	/**
	 * Checks if the result is a valid match for the input.  Currently only accepts armor or tools that are the same type as the input
	 */
	public boolean isValidMatchForInput(ItemStack inputStack, ItemStack resultStack) {
		if (inputStack.getItem() instanceof ItemPickaxe && resultStack.getItem() instanceof ItemPickaxe) {
			return true;
		}
		if (inputStack.getItem() instanceof ItemAxe && resultStack.getItem() instanceof ItemAxe) {
			return true;
		}
		if (inputStack.getItem() instanceof ItemShovel && resultStack.getItem() instanceof ItemShovel) {
			return true;
		}
		if (inputStack.getItem() instanceof ItemHoe && resultStack.getItem() instanceof ItemHoe) {
			return true;
		}
		if (inputStack.getItem() instanceof ItemSword && resultStack.getItem() instanceof ItemSword) {
			return true;
		}
		if (inputStack.getItem() instanceof ItemBow && resultStack.getItem() instanceof ItemBow) {
			return true;
		}

		if (inputStack.getItem() instanceof ItemArmor && resultStack.getItem() instanceof ItemArmor) {
			ItemArmor inputArmor = (ItemArmor) inputStack.getItem();
			ItemArmor resultArmor = (ItemArmor) resultStack.getItem();

			return inputArmor.armorType == resultArmor.armorType;
		}

		// nope!
		return false;
	}

	public int getUncraftingCost() {
		return this.uncraftingMatrix.uncraftingCost;
	}

	public int getRecraftingCost() {
		return this.uncraftingMatrix.recraftingCost;
	}

	/**
	 * Calculate the cost of uncrafting (experience points), if any.  Return 0 if uncrafting is not available at this time
	 */
	public int calculateUncraftingCost() {
		// we don't want to display anything if there is anything in the assembly grid
		if (!isMatrixEmpty(this.assemblyMatrix)) {
			return 0;
		} else {
			return this.baseUncraftingCost;
		}
	}

	/**
	 * Return the cost of recrafting, if any.
	 */
	public int calculateRecraftingCost() {
		// TODO(mite-port): recrafting is disabled by MITE_BALANCE; the 1.7.10 formula (repair cost + enchantment weights
		// + damaged parts + enchantability difference) depended on ItemStack.setRepairCost, which MITE does not have.
		return 0;
	}

	public int countHighestEnchantmentCost(ItemStack itemStack) {
		int count = 0;

		// go through all enchantment IDs, and see if the item has that enchantment, and at what level
		for (Enchantment ench : Enchantment.enchantmentsList) {
			if (ench != null) {
				int level = EnchantmentHelper.getEnchantmentLevel(ench.effectId, itemStack);
				if (level > count) {
					count += getWeightModifier(ench) * level;
				}
			}
		}

		return count;
	}

	public int countTotalEnchantmentCost(ItemStack itemStack) {
		int count = 0;

		for (Enchantment ench : Enchantment.enchantmentsList) {
			if (ench != null) {
				int level = EnchantmentHelper.getEnchantmentLevel(ench.effectId, itemStack);
				if (level > 0) {
					count += getWeightModifier(ench) * level;
					count += 1;
				}
			}
		}

		return count;
	}

	public int getWeightModifier(Enchantment ench) {
		switch (ench.getWeight()) {
		case 1:
			return 8;
		case 2:
			return 4;
		case 3:
		case 4:
		case 5:
			return 2;
		case 6:
		case 7:
		case 8:
		case 9:
		default:
		case 10:
			return 1;
		}
	}

	/**
	 * Checks if an inventory has any items in it
	 */
	private boolean isMatrixEmpty(IInventory matrix) {
		boolean matrixEmpty = true;
		for (int i = 0; i < matrix.getSizeInventory(); i++) {
			if (matrix.getStackInSlot(i) != null) {
				matrixEmpty = false;
			}
		}
		return matrixEmpty;
	}

	/**
	 * Should the specified item count for taking damage?
	 */
	public boolean isDamageableComponent(ItemStack itemStack) {
		return itemStack != null && itemStack.getItem() != Item.stick;
	}

	/**
	 * Count how many items in an inventory can take damage
	 */
	public int countDamageableParts(IInventory matrix) {
		int count = 0;
		for (int i = 0; i < matrix.getSizeInventory(); i++) {
			if (isDamageableComponent(matrix.getStackInSlot(i))) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Determine, based on the item damage, how many parts are damaged.  We're already
	 * assuming that the item is loaded into the uncrafing matrix.
	 */
	public int countDamagedParts(ItemStack input) {
		int totalMax4 = Math.max(4, countDamageableParts(this.uncraftingMatrix));
		float damage = (float) input.getItemDamage() / (float) input.getMaxDamage();
		int damagedParts = (int) Math.ceil(totalMax4 * damage);
		return damagedParts;
	}

	/**
	 * Called to transfer a stack from one inventory to the other eg. when shift clicking.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotNum) {
		ItemStack copyItem = null;
		Slot transferSlot = (Slot) this.inventorySlots.get(slotNum);

		if (transferSlot != null && transferSlot.getHasStack()) {
			ItemStack transferStack = transferSlot.getStack();
			copyItem = transferStack.copy();

			if (slotNum == 0 || slotNum == 1) {
				// result or input goes to inventory or hotbar
				if (!this.mergeItemStack(transferStack, 20, 56, true)) {
					return null;
				}

				transferSlot.onSlotChange(transferStack, copyItem);
			} else if (slotNum >= 20 && slotNum < 47) {
				// inventory goes to hotbar
				if (!this.mergeItemStack(transferStack, 47, 56, false)) {
					return null;
				}
			} else if (slotNum >= 47 && slotNum < 56) {
				// hotbar goes to inventory
				if (!this.mergeItemStack(transferStack, 20, 47, false)) {
					return null;
				}
			} else if (!this.mergeItemStack(transferStack, 20, 56, false)) {
				// crafting area goes to inventory or hotbar
				return null;
			}

			if (transferStack.stackSize == 0) {
				transferSlot.putStack((ItemStack) null);
			} else {
				transferSlot.onSlotChanged();
			}

			if (transferStack.stackSize == copyItem.stackSize) {
				return null;
			}

			transferSlot.onPickupFromSlot(player, transferStack);
		}

		return copyItem;
	}

	/**
	 * Callback for when the crafting gui is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);

		if (!this.world.isRemote) {
			// drop items in assembly grid
			for (int i = 0; i < 9; ++i) {
				ItemStack assemblyStack = this.assemblyMatrix.getStackInSlotOnClosing(i);

				if (assemblyStack != null) {
					par1EntityPlayer.dropPlayerItem(assemblyStack);
				}
			}

			// drop input
			ItemStack inputStack = this.tinkerInput.getStackInSlotOnClosing(0);
			if (inputStack != null) {
				par1EntityPlayer.dropPlayerItem(inputStack);
			}
		}

		this.tinkerResult.setInventorySlotContents(0, null);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Recipe introspection. MITE has no ShapedOreRecipe; ShapedRecipes keeps width/height private (items are public
	// through getComponents()).
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * If we can determine this recipe's items, return it.
	 */
	public ItemStack[] getRecipeItems(IRecipe recipe) {
		if (recipe instanceof ShapedRecipes) {
			return getRecipeItemsShaped((ShapedRecipes) recipe);
		}
		return null;
	}

	public ItemStack[] getRecipeItemsShaped(ShapedRecipes shaped) {
		return shaped.getComponents();
	}

	/**
	 * If we can determine this recipe's width, return it.
	 */
	public int getRecipeWidth(IRecipe recipe) {
		if (recipe instanceof ShapedRecipes) {
			return getRecipeWidthShaped((ShapedRecipes) recipe);
		}
		return -1;
	}

	public int getRecipeWidthShaped(ShapedRecipes shaped) {
		int value = readShapedField(true, shaped);
		if (value > 0) {
			return value;
		}
		// TODO(mite-port): replace reflection with an access widener (ShapedRecipes.recipeWidth/recipeHeight)
		switch (shaped.getRecipeSize()) {
		case 1:
			return 1;
		case 2:
			return 1;
		case 3:
			return 1;
		case 4:
			return 2;
		case 6:
			return 2;
		default:
			return 3;
		}
	}

	/**
	 * If we can determine this recipe's height, return it.
	 */
	public int getRecipeHeight(IRecipe recipe) {
		if (recipe instanceof ShapedRecipes) {
			return getRecipeHeightShaped((ShapedRecipes) recipe);
		}
		return -1;
	}

	public int getRecipeHeightShaped(ShapedRecipes shaped) {
		int value = readShapedField(false, shaped);
		if (value > 0) {
			return value;
		}
		int size = shaped.getRecipeSize();
		int width = getRecipeWidthShaped(shaped);
		return width > 0 ? size / width : 1;
	}

	private static int readShapedField(boolean width, ShapedRecipes shaped) {
		if (!reflectionTried) {
			reflectionTried = true;
			try {
				recipeWidthField = ShapedRecipes.class.getDeclaredField("recipeWidth");
				recipeWidthField.setAccessible(true);
				recipeHeightField = ShapedRecipes.class.getDeclaredField("recipeHeight");
				recipeHeightField.setAccessible(true);
			} catch (ReflectiveOperationException | RuntimeException e) {
				TwilightForestMod.LOGGER.warn("Could not reflect ShapedRecipes size, uncrafting falls back to guessing: {}", e.toString());
				recipeWidthField = null;
				recipeHeightField = null;
			}
		}
		Field field = width ? recipeWidthField : recipeHeightField;
		if (field == null) {
			return -1;
		}
		try {
			return field.getInt(shaped);
		} catch (IllegalAccessException | RuntimeException e) {
			return -1;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		// the block check also covers the table being broken while the GUI is open
		if (this.world.getBlockId(this.posX, this.posY, this.posZ) != TFIds.UNCRAFTING_TABLE) {
			return false;
		}
		return player.getDistanceSq(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D) <= 64.0D;
	}
}
