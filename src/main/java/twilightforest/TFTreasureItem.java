package twilightforest;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Item;
import net.minecraft.ItemStack;


public class TFTreasureItem {

	ItemStack itemStack;
	int rarity;
	
	int randomEnchantmentLevel = 0;
	
	public TFTreasureItem(Item item) {
		this(item, 1, 10);
	}
	
	public TFTreasureItem(Item item, int quantity) {
		this(item, quantity, 10);
	}
	
	public TFTreasureItem(Item item, int quantity, int rarity) {
		this.itemStack = new ItemStack(item, quantity);
		this.rarity = rarity;
	}
	
	public TFTreasureItem(Block block, int quantity, int rarity) {
		this.itemStack = new ItemStack(block, quantity);
		this.rarity = rarity;
	}
	
	public TFTreasureItem(ItemStack itemStack, int rarity) {
		this.itemStack = itemStack.copy();
		this.rarity = rarity;
	}
	
	
	/**
	 * Get a new ItemStack ready for use!
	 * 
	 * @param rand
	 * @return
	 */
	public ItemStack getItemStack(Random rand) {
		ItemStack result = this.itemStack.copy();
		// quantity is 1..q, but never more than one stack can hold
		int max = Math.max(1, Math.min(result.stackSize, result.getMaxStackSize()));
		result.stackSize = rand.nextInt(max) + 1;
		
		// randomly enchant this item if needed
		if (result.isEnchantable() && randomEnchantmentLevel > 0)
		{
			EnchantmentHelper.addRandomEnchantment(rand, result, randomEnchantmentLevel);
		}
		
		// MITE_BALANCE L3: damageable loot is worn like MITE's own chest loot (0-60%, rusted iron 30-90%)
		if (result.isItemStackDamageable())
		{
			result.applyRandomItemStackDamageForChest();
		}
		
		return result;
	}
	
	/**
	 * Returns this items rarity value
	 * 
	 * @param total
	 * @return
	 */
	public int getRarity() {
		return rarity;
	}

	public int getRandomEnchantmentLevel() {
		return randomEnchantmentLevel;
	}

	public void setRandomEnchantmentLevel(int randomEnchantmentLevel) {
		this.randomEnchantmentLevel = randomEnchantmentLevel;
	}
}
