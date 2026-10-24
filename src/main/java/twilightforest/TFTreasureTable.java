package twilightforest;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentData;
import net.minecraft.Item;
import net.minecraft.ItemStack;


/**
 * This is not much more than a list of TFTreasureEntries.
 * 
 * @author Ben
 *
 */
public class TFTreasureTable {
	
	private static final int DEFAULT_RARITY = 10;
	/** Books never carry more than this level (MITE_BALANCE L5). */
	private static final int MAX_BOOK_LEVEL = 2;
	protected ArrayList<TFTreasureItem> list;
	/** Upper bound for random enchantment levels of this table (MITE_BALANCE L6, per structure tier). */
	protected int enchantCap = Integer.MAX_VALUE;
	
	public TFTreasureTable() {
		this.list = new ArrayList<TFTreasureItem>();
	}
	
	/**
	 * Sets the maximum random-enchantment level for entries added from now on.
	 */
	public void setEnchantCap(int cap) {
		this.enchantCap = cap;
	}
	

	/**
	 * Adds the item onto this list to be generated at normal rarity (10), in the specified quantity
	 * 
	 * @param item
	 * @param quantity
	 */
	public void add(Item item, int quantity) {
		this.add(item, quantity, DEFAULT_RARITY);
	}
	
	/**
	 * Adds the item onto the list, with the values specified
	 * 
	 * @param item
	 * @param quantity
	 * @param rarity
	 */
	public void add(Item item, int quantity, int rarity) {
		if (item == null) {
			missing();
			return;
		}
		list.add(new TFTreasureItem(item, quantity, rarity));
	}
	
	
	/**
	 * Adds the ItemStack onto this list to be generated at normal rarity (10), in the quantity the itemstack contains
	 * 
	 * @param itemstack
	 */
	public void add(ItemStack itemstack) {
		if (itemstack == null || itemstack.getItem() == null) {
			missing();
			return;
		}
		list.add(new TFTreasureItem(itemstack, DEFAULT_RARITY));
	}
	
	/**
	 * Adds the item at the specified enchantment level
	 */
	public void addEnchanted(ItemStack itemstack, Enchantment ench1, int enchLevel1) {
		if (itemstack == null || itemstack.getItem() == null || ench1 == null) {
			missing();
			return;
		}
		itemstack.addEnchantment(ench1, enchLevel1);
		list.add(new TFTreasureItem(itemstack, DEFAULT_RARITY));
	}
	
	/**
	 * Adds an enchanted book at the specified enchantment level
	 */
	public void addEnchantedBook(Enchantment ench1, int enchLevel1) {
		if (ench1 == null) {
			missing();
			return;
		}
		// MITE_BALANCE L5: books are at most level II, and never silk touch / tree felling / loot bonus above II
		if (ench1 == Enchantment.silkTouch || ench1 == Enchantment.tree_felling) {
			return;
		}
		ItemStack itemstack = Item.enchantedBook.getEnchantedItemStack(new EnchantmentData(ench1, Math.min(enchLevel1, MAX_BOOK_LEVEL)));
		list.add(new TFTreasureItem(itemstack, DEFAULT_RARITY));
	}
	
	/**
	 * Adds the item at the specified enchantment level, 2 enchantments
	 */
	public void addEnchanted(ItemStack itemstack, Enchantment ench1, int enchLevel1, Enchantment ench2, int enchLevel2) {
		if (itemstack == null || itemstack.getItem() == null || ench1 == null || ench2 == null) {
			missing();
			return;
		}
		itemstack.addEnchantment(ench1, enchLevel1);
		itemstack.addEnchantment(ench2, enchLevel2);
		list.add(new TFTreasureItem(itemstack, DEFAULT_RARITY));
	}
	
	/**
	 * Adds the item at the specified enchantment level, 3 enchantments
	 */
	public void addEnchanted(ItemStack itemstack, Enchantment ench1, int enchLevel1, Enchantment ench2, int enchLevel2, Enchantment ench3, int enchLevel3) {
		if (itemstack == null || itemstack.getItem() == null || ench1 == null || ench2 == null || ench3 == null) {
			missing();
			return;
		}
		itemstack.addEnchantment(ench1, enchLevel1);
		itemstack.addEnchantment(ench2, enchLevel2);
		itemstack.addEnchantment(ench3, enchLevel3);
		list.add(new TFTreasureItem(itemstack, DEFAULT_RARITY));
	}
	
	/**
	 * Adds the item with a random enchantment level
	 */
	public void addRandomEnchanted(ItemStack itemstack, int randomLevel)
	{
		if (itemstack == null || itemstack.getItem() == null) {
			missing();
			return;
		}
		TFTreasureItem treasure = new TFTreasureItem(itemstack, DEFAULT_RARITY);
		treasure.setRandomEnchantmentLevel(Math.min(randomLevel, enchantCap));
		list.add(treasure);
	}
	
	/**
	 * Adds the item with a random enchantment level
	 */
	public void addRandomEnchanted(Item item, int randomLevel)
	{
		if (item == null) {
			missing();
			return;
		}
		TFTreasureItem treasure = new TFTreasureItem(item, 1, DEFAULT_RARITY);
		treasure.setRandomEnchantmentLevel(Math.min(randomLevel, enchantCap));
		list.add(treasure);
	}
	
	/**
	 * Adds the block onto this list to be generated at normal rarity (10), in the specified quantity
	 * 
	 * @param block
	 * @param quantity
	 */
	public void add(Block block, int quantity) {
		this.add(block, quantity, DEFAULT_RARITY);
	}
	

	/**
	 * Adds the block onto the list, with the values specified
	 * 
	 * @param block
	 * @param quantity
	 * @param rarity
	 */
	public void add(Block block, int quantity, int rarity) {
		if (block == null) {
			missing();
			return;
		}
		list.add(new TFTreasureItem(block, quantity, rarity));
	}
	
	/**
	 * An entry referenced an item/block/enchantment that is not registered (yet): skip it instead of crashing.
	 */
	private static void missing() {
		TwilightForestMod.LOGGER.warn("TFTreasureTable: skipped a loot entry whose item/block/enchantment is not registered");
	}
	
	
	/**
	 * What is the total of all the rarity entries in our table?
	 * 
	 * @return
	 */
	protected int total() {
		int value = 0;
		
		for (TFTreasureItem item : list) {
			value += item.getRarity();
		}
		
		return value;
	}
	
	public ItemStack getRandomItem(Random rand) {
		
		int total = total();
		if (total <= 0) {
			return null;
		}
		
		int value = rand.nextInt(total);
		
		for (TFTreasureItem item : list) {
			if (item.getRarity() > value) {
				return item.getItemStack(rand);
			}
			else
			{
				value -= item.getRarity();
			}
		}
		
		// we should not actually get here
		return null;
	}
	
	/**
	 * Is this table empty 
	 */
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	
	/**
	 * Clear this list
	 */
	public void clear()
	{
		list.clear();
	}
}
