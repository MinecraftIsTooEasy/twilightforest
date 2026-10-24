package twilightforest;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Enchantment;
import net.minecraft.Item;
import net.minecraft.ItemAxe;
import net.minecraft.ItemCoin;
import net.minecraft.ItemHoe;
import net.minecraft.ItemIngot;
import net.minecraft.ItemMattock;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemStack;
import net.minecraft.TileEntityChest;
import net.minecraft.World;
import net.minecraft.server.MinecraftServer;
import twilightforest.block.TFBlocks;
import twilightforest.item.TFItems;


/**
 * Chest loot of the Twilight Forest structures.
 * <p>
 * The tables follow MITE_BALANCE.md section 11 (rules L1-L10): the TF drawing scheme is kept (4 common, 2 uncommon,
 * 1 rare per chest, 1/4 of the common draws come from "useless", 1/4 of the rare draws from "ultrarare"), items are
 * mapped to MITE items, and every chest additionally obeys the MITE day gates, a potion / enchanted gear / XP-bottle limit
 * and an XP value budget.
 */
public class TFTreasure {

	/** loot.profile = vanilla switches the MITE rules (day gates, budgets, enchant caps) off. */
	private static boolean miteProfile() {
		return !"vanilla".equalsIgnoreCase(TFConfig.lootProfile);
	}
	/** MITE_BALANCE L5: at most one potion / one XP bottle per chest. */
	private static final int MAX_POTIONS_PER_CHEST = 1;
	private static final int MAX_XP_BOTTLES_PER_CHEST = 1;
	/** MITE_BALANCE L6: at most one enchanted piece of gear per chest (the stronghold boss chest is exempt). */
	private static final int MAX_ENCHANTED_GEAR_PER_CHEST = 1;
	/** MITE re-rolls a loot entry at most 4 times when it is not allowed. */
	private static final int MAX_REROLLS = 4;

	int type;
	
	Random treasureRNG;

	protected TFTreasureTable useless; // one quarter of common treasures are
										// one of these useless items
	protected TFTreasureTable common;
	protected TFTreasureTable uncommon;
	protected TFTreasureTable rare;
	protected TFTreasureTable ultrarare; // one quarter of rare treasures are
											// one of these ultrarare items. The
											// list must be at least as large as
											// the large list for this to make
											// sense.

	/** The tables are filled lazily: the TF/MITE items they reference are only registered after this class could load. */
	private boolean filled = false;

	public static TFTreasure hill1 = new TFTreasure(1);
	public static TFTreasure hill2 = new TFTreasure(2);
	public static TFTreasure hill3 = new TFTreasure(3);
	public static TFTreasure hedgemaze = new TFTreasure(4);
	public static TFTreasure labyrinth_room = new TFTreasure(5);
	public static TFTreasure labyrinth_deadend = new TFTreasure(6);
	public static TFTreasure tower_room = new TFTreasure(7);
	public static TFTreasure tower_library = new TFTreasure(8);
	public static TFTreasure basement = new TFTreasure(9);
	public static TFTreasure labyrinth_vault = new TFTreasure(10);
	public static TFTreasure darktower_cache = new TFTreasure(11);
	public static TFTreasure darktower_key = new TFTreasure(12);
	public static TFTreasure darktower_boss = new TFTreasure(13);
	public static TFTreasure tree_cache = new TFTreasure(14);
	public static TFTreasure stronghold_cache = new TFTreasure(15);
	public static TFTreasure stronghold_room = new TFTreasure(16);
	public static TFTreasure stronghold_boss = new TFTreasure(17);
	public static TFTreasure aurora_cache = new TFTreasure(18);
	public static TFTreasure aurora_room = new TFTreasure(19);
	public static TFTreasure aurora_boss = new TFTreasure(20);
	public static TFTreasure troll_garden = new TFTreasure(21);
	public static TFTreasure troll_vault = new TFTreasure(22);

	
	/**
	 * Construct a new treasure of type i
	 * 
	 * @param i
	 */
	public TFTreasure(int i) {
		this.type = i;

		useless = new TFTreasureTable();
		common = new TFTreasureTable();
		uncommon = new TFTreasureTable();
		rare = new TFTreasureTable();
		ultrarare = new TFTreasureTable();
		
		treasureRNG = new Random();

		// the tables are filled on first use, see ensureFilled()
	}

	private synchronized void ensureFilled() {
		if (!filled) {
			filled = true;
			int cap = miteProfile() ? getEnchantCap(type) : Integer.MAX_VALUE;
			useless.setEnchantCap(cap);
			common.setEnchantCap(cap);
			uncommon.setEnchantCap(cap);
			rare.setEnchantCap(cap);
			ultrarare.setEnchantCap(cap);
			// TODO: separate these into plain ol' different classes
			fill(type);
		}
	}

	/**
	 * MITE_BALANCE L6: loot.enchantCap.T1..T5 = 4/8/12/16/16 (random enchantment level of a table entry).
	 */
	private static int getEnchantCap(int type) {
		switch (type) {
		case 1: case 2: case 4: case 7: case 8: case 9: case 14:
			return TFConfig.lootEnchantCapT1;
		case 3: case 5: case 6: case 10:
			return TFConfig.lootEnchantCapT2;
		case 15: case 16: case 17:
			return TFConfig.lootEnchantCapT3;
		case 11: case 12: case 13: case 18: case 19: case 20:
			return TFConfig.lootEnchantCapT4;
		default:
			return TFConfig.lootEnchantCapT5;
		}
	}

	/**
	 * MITE chests only accept the facings 2-5 (north, south, west, east), metadata 0 is rejected. Faces the first open side.
	 */
	private static int chestFacing(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z - 1)) {
			return 2;
		}
		if (world.isAirBlock(x, y, z + 1)) {
			return 3;
		}
		if (world.isAirBlock(x - 1, y, z)) {
			return 4;
		}
		if (world.isAirBlock(x + 1, y, z)) {
			return 5;
		}
		return 2;
	}

	/**
	 * Generates a treasure chest in the world at the specified coordinates.
	 */
	public boolean generate(World world, Random rand, int cx, int cy, int cz) {

		return this.generate(world, rand, cx, cy, cz, Block.chest);
	}

	/**
	 * Generates a treasure chest in the world at the specified coordinates.
	 */
	public boolean generate(World world, Random rand, int cx, int cy, int cz, Block chestBlock) {
		ensureFilled();
		
		boolean flag = true;
		
		// the same chest position always holds the same loot
		treasureRNG.setSeed(world.getSeed() * cx + cy ^ cz);

		// make a chest
		world.setBlock(cx, cy, cz, chestBlock.blockID, chestFacing(world, cx, cy, cz), 2);

		ChestBudget budget = new ChestBudget();
		int day = (miteProfile() && TFConfig.lootDayGates) ? getOverworldDay(world) : Integer.MAX_VALUE;

		// add 4 common items
		for (int i = 0; i < 4; i++) {
			flag &= addItemToChest(world, treasureRNG, cx, cy, cz, drawChecked(0, day, budget));
		}

		// add 2 uncommon items
		for (int i = 0; i < 2; i++) {
			flag &= addItemToChest(world, treasureRNG, cx, cy, cz, drawChecked(1, day, budget));
		}

		// add 1 rare item
		for (int i = 0; i < 1; i++) {
			flag &= addItemToChest(world, treasureRNG, cx, cy, cz, drawChecked(2, day, budget));
		}

		return flag;
	}

	/**
	 * Gets a common item. If the useless list is not empty, 
	 *   there is a 1/4 chance we get a useless item instead.
	 */
	public ItemStack getCommonItem(Random rand) {
		ensureFilled();
		if (!useless.isEmpty() && rand.nextInt(4) == 0) {
			return useless.getRandomItem(rand);
		} else {
			return common.getRandomItem(rand);
		}
	}

	/**
	 * Gets an uncommon item.
	 */
	public ItemStack getUncommonItem(Random rand) {
		ensureFilled();
		return uncommon.getRandomItem(rand);
	}

	/**
	 * Gets a rare item. If there are items on the ultrarare list, 
	 *   there is a 1/4 chance we get a ultrarare item instead.
	 */
	public ItemStack getRareItem(Random rand) 
	{
		ensureFilled();
		if (!ultrarare.isEmpty() && rand.nextInt(4) == 0) 
		{
			return ultrarare.getRandomItem(rand);
		}
		else 
		{
			return rare.getRandomItem(rand);
		}
	}

	/**
	 * Draws an item of the given kind (0 common, 1 uncommon, 2 rare) that is allowed by the MITE rules of this chest.
	 * Like MITE's own chest generator, a disallowed draw is re-rolled up to 4 times and then dropped.
	 */
	private ItemStack drawChecked(int kind, int overworldDay, ChestBudget budget) {
		for (int attempt = 0; attempt < MAX_REROLLS; attempt++) {
			ItemStack stack;
			if (kind == 0) {
				stack = getCommonItem(treasureRNG);
			} else if (kind == 1) {
				stack = getUncommonItem(treasureRNG);
			} else {
				stack = getRareItem(treasureRNG);
			}

			if (stack == null) {
				continue;
			}
			if (isAllowed(stack, overworldDay, budget)) {
				budget.add(stack);
				return stack;
			}
		}
		return null;
	}

	/**
	 * MITE_BALANCE L2 (day gates), L5/L6 (potion, XP bottle and enchanted gear limits) and L7 (XP value budget).
	 */
	private boolean isAllowed(ItemStack stack, int overworldDay, ChestBudget budget) {
		Item item = stack.getItem();
		if (item == null) {
			return false;
		}

		// L2: same thresholds as MITE's WeightedRandomChestContent
		int minDay = 0;
		if (item instanceof ItemAxe || item instanceof ItemHoe || item instanceof ItemMattock) {
			minDay = 10;
		} else if (item instanceof ItemPickaxe || item instanceof ItemIngot || item instanceof ItemCoin) {
			minDay = 20;
		}
		if (overworldDay < minDay) {
			return false;
		}

		if (!miteProfile()) {
			return true;
		}

		// L5: potions and experience bottles
		if (item == Item.potion && budget.potions >= MAX_POTIONS_PER_CHEST) {
			return false;
		}
		if (item == Item.expBottle && budget.xpBottles >= MAX_XP_BOTTLES_PER_CHEST) {
			return false;
		}

		// L6: enchanted gear (the stronghold boss chest hands out a fixed set of enchanted gear)
		if (type != 17 && isEnchantedGear(stack) && budget.enchantedGear >= MAX_ENCHANTED_GEAR_PER_CHEST) {
			return false;
		}

		// L7: hard XP budget of coins and gem shards (loot.budget.xpHard)
		if (budget.xpValue + getXPValue(stack) > TFConfig.lootBudgetXpHard) {
			return false;
		}

		// L7: hard metal budget (loot.budget.iieHard), 1 IIE = crafting difficulty 800
		if (budget.iie + getIIE(stack) > TFConfig.lootBudgetIieHard) {
			return false;
		}
		return true;
	}

	private static boolean isEnchantedGear(ItemStack stack) {
		return stack.getItem() != Item.enchantedBook && stack.isItemEnchanted();
	}

	/**
	 * Metal content of a stack in IIE (MITE_BALANCE L7: crafting difficulty / 800, metal items only).
	 */
	private static float getIIE(ItemStack stack) {
		Item item = stack.getItem();
		if (item == null || item.getHardestMetalMaterial() == null) {
			return 0.0F;
		}
		return stack.getCraftingDifficultyAsComponent() / 800.0F * stack.stackSize;
	}

	/**
	 * XP value (MITE_BALANCE L7): copper coin 5, silver coin 25, gold coin 100, emerald shard 27.8, diamond shard 55.6.
	 */
	private static float getXPValue(ItemStack stack) {
		Item item = stack.getItem();
		float each = 0.0F;
		if (item == Item.coinCopper) {
			each = 5.0F;
		} else if (item == Item.coinSilver) {
			each = 25.0F;
		} else if (item == Item.coinGold) {
			each = 100.0F;
		} else if (item == Item.shardEmerald) {
			each = 27.8F;
		} else if (item == Item.shardDiamond) {
			each = 55.6F;
		}
		return each * stack.stackSize;
	}

	/**
	 * The MITE overworld day (TF chests use the overworld clock, not the day of the Twilight Forest slot).
	 */
	private static int getOverworldDay(World world) {
		if (world.isOverworld()) {
			return world.getDayOfWorld();
		}
		MinecraftServer server = MinecraftServer.getServer();
		if (server != null && server.worldServers != null && server.worldServers[0] != null) {
			return server.worldServers[0].getDayOfWorld();
		}
		return world.getDayOfWorld();
	}

	/**
	 * Running totals of one chest while it is being filled.
	 */
	private static class ChestBudget {
		int potions;
		int xpBottles;
		int enchantedGear;
		float xpValue;
		float iie;

		void add(ItemStack stack) {
			Item item = stack.getItem();
			if (item == Item.potion) {
				potions++;
			}
			if (item == Item.expBottle) {
				xpBottles++;
			}
			if (isEnchantedGear(stack)) {
				enchantedGear++;
			}
			xpValue += getXPValue(stack);
			iie += getIIE(stack);
		}
	}

	/**
	 * Used internally to add items into chests
	 */
	protected boolean addItemToChest(World world, Random rand, int cx, int cy,
			int cz, ItemStack itemStack) {

		if (itemStack == null) {
			// the draw was dropped (not allowed / not registered)
			return true;
		}

		TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(cx, cy, cz);

		// make sure the chest is not full
		if (chest != null) {
			int slot = findRandomInventorySlot(chest, rand);

			if (slot != -1) {
				chest.setInventorySlotContents(slot, itemStack);
				return true;
			}

		}
		return false;
	}

	/**
	 * Finds an open slot in the chest, or returns -1 if the chest is full.
	 */
	protected int findRandomInventorySlot(TileEntityChest chest, Random rand) {
		// TODO: this is lazy

		// try 100 times to find an empty slot
		for (int i = 0; i < 100; i++) {
			int slot = rand.nextInt(chest.getSizeInventory());
			if (chest.getStackInSlot(slot) == null) {
				return slot;
			}
		}

		// didn't find one
		return -1;
	}

	private static ItemStack stack(Item item, int size, int subtype) {
		return item == null ? null : new ItemStack(item, size, subtype);
	}

	private static ItemStack stack(Block block, int size, int subtype) {
		return block == null ? null : new ItemStack(block, size, subtype);
	}

	private static ItemStack potion(int subtype) {
		return new ItemStack(Item.potion, 1, subtype);
	}

	/**
	 * Fills the table with items for the specified type. This should be
	 * refactored into seperate functions someday.
	 * <p>
	 * Contents follow MITE_BALANCE.md section 11.2; amounts are the maximum stack (a draw yields 1..amount).
	 */
	protected void fill(int i) {
		// 1.7.10 flowers: MITE plantRed / plantYellow
		useless.add(Block.plantRed, 4);
		useless.add(Block.plantYellow, 4);
		useless.add(Item.feather, 3);
		useless.add(Item.seeds, 2);
		useless.add(Item.flint, 2);
		useless.add(Block.cactus, 2);
		useless.add(Item.reed, 4);
		useless.add(Block.sand, 4);
		useless.add(Item.flowerPot, 1);
		useless.add(new ItemStack(Item.dyePowder, 1, 0));

		if (i == 1) {
			// hill type 1 chest
			common.add(Item.ironNugget, 9);
			common.add(Item.ingotCopper, 2);
			common.add(Item.silk, 4);
			common.add(Item.wheat, 4);
			common.add(Item.copperNugget, 9);
			common.add(Item.bucketCopperEmpty, 1);

			uncommon.add(Item.bread, 3);
			uncommon.add(TFItems.oreMagnet, 1);
			uncommon.add(Item.gunpowder, 4);
			uncommon.add(Item.arrowFlint, 6);
			uncommon.add(Block.torchWood, 8);
			uncommon.add(TFItems.liveRoot, 3);

			rare.add(Item.ingotIron, 2);
			rare.add(TFItems.liveRoot, 3);
			rare.add(Item.goldNugget, 4);

			ultrarare.add(Item.shardDiamond, 2);
			ultrarare.add(TFItems.steeleafIngot, 1);
			ultrarare.add(TFItems.transformPowder, 4);
		}
		
		if (i == 2) {
			// hill type 2 chest
			common.add(Item.ingotIron, 2);
			common.add(Item.carrot, 4);
			common.add(Block.ladder, 6);
			common.add(Item.bucketCopperEmpty, 1);

			uncommon.add(Item.bakedPotato, 2);
			uncommon.add(TFItems.oreMagnet, 1);
			uncommon.add(TFItems.ironwoodIngot, 3);
			uncommon.add(Item.arrowFlint, 6);
			uncommon.add(Block.torchWood, 8);

			rare.add(TFItems.nagaScale, 1);
			rare.add(Item.ingotSilver, 1);
			rare.add(TFItems.transformPowder, 4);
			// the Uncrafting Table is no longer a chest item (MITE_BALANCE section 12 #1)

			ultrarare.add(TFItems.peacockFan, 1);
			ultrarare.add(Item.shardEmerald, 4);
			ultrarare.add(Item.shardDiamond, 2);
			ultrarare.add(TFItems.charmOfLife1, 1);
		}
		
		if (i == 3) {
			// hill type 3 chest
			common.add(Item.goldNugget, 9);
			common.add(Item.potato, 4);
			common.add(Item.fishRaw, 4);
			common.add(TFItems.torchberries, 5);

			uncommon.add(Item.pumpkinPie, 1);
			uncommon.add(TFItems.oreMagnet, 1);
			uncommon.add(Item.gunpowder, 4);
			uncommon.add(Item.arrowFlint, 6);
			uncommon.add(Block.torchWood, 8);
			uncommon.add(TFItems.steeleafIngot, 2);

			rare.add(TFItems.nagaScale, 1);
			rare.add(TFItems.ironwoodPick, 1); // plain: the Efficiency/Fortune enchantments are gone
			rare.add(TFItems.transformPowder, 4);

			ultrarare.add(TFItems.moonwormQueen, 1);
			ultrarare.add(TFBlocks.sapling, 1, 4); // TF oak sapling at rarity 4 (as in 1.7.10)
			ultrarare.add(Item.shardDiamond, 3);
			ultrarare.add(TFItems.charmOfLife1, 1);
			ultrarare.add(TFItems.charmOfKeeping1, 1);
		}

		if (i == 4) {
			// hedge maze chest
			common.add(Block.planks, 4);
			common.add(Block.mushroomBrown, 2);
			common.add(Block.mushroomRed, 2);
			common.add(Item.wheat, 4);
			common.add(Item.silk, 4);
			common.add(Item.stick, 6);

			uncommon.add(Item.melon, 4);
			uncommon.add(Item.melonSeeds, 4);
			uncommon.add(Item.pumpkinSeeds, 4);
			uncommon.add(Item.arrowFlint, 6);
			uncommon.add(TFBlocks.firefly, 4);

			rare.add(Block.web, 3);
			rare.add(Item.shears, 1);
			rare.add(Item.saddle, 1);
			rare.add(Item.bow, 1);
			rare.add(Item.appleRed, 2);

			ultrarare.add(Item.hoeIron, 1); // was the diamond hoe
			ultrarare.add(Item.shardDiamond, 1);
			ultrarare.add(Item.bowlMushroomStew, 1);
			ultrarare.add(Item.appleGold, 1);
		}

		if (i == 5) {
			// labyrinth room chest
			useless.clear();
			
			common.add(Item.ingotIron, 3);
			common.add(TFItems.mazeWafer, 12);
			common.add(Item.gunpowder, 4);
			common.add(TFItems.ironwoodIngot, 2);
			common.add(TFBlocks.firefly, 5);
			common.add(Item.bucketIronMilk, 1);

			uncommon.add(TFItems.steeleafIngot, 3);
			uncommon.add(TFItems.steeleafPick, 1);
			uncommon.add(TFItems.steeleafHelm, 1);
			uncommon.add(TFItems.steeleafBoots, 1);
			uncommon.add(TFItems.steeleafSword, 1);
			uncommon.add(TFItems.ironwoodSword, 1);
			uncommon.add(TFItems.charmOfKeeping1, 1);
			// no steeleaf / ironwood chestplates (MITE_BALANCE table T2)
			
			rare.add(TFItems.mazeMapFocus, 1);
			rare.add(Block.tnt, 1);
			rare.add(potion(16373)); // health ii
		}

		if (i == 6) {
			// labyrinth dead end chest
			common.add(Item.stick, 12);
			common.add(Item.coal, 4);
			common.add(Item.arrowFlint, 12);
			common.add(TFItems.mazeWafer, 9);
			common.add(Item.paper, 12);
			common.add(Item.leather, 4);

			uncommon.add(Item.bucketIronMilk, 1);
			uncommon.add(Item.paper, 5);
			uncommon.add(Item.ingotIron, 2);
			uncommon.add(TFItems.ironwoodIngot, 3);
			uncommon.add(TFBlocks.firefly, 5);
			uncommon.add(TFItems.charmOfKeeping1, 1);

			rare.add(TFItems.steeleafIngot, 3);
			rare.add(Item.appleGold, 1);
			rare.add(Item.gunpowder, 2);
			// no blaze rods unless loot.allowBlazeRod (MITE_BALANCE L4)
			if (TFConfig.lootAllowBlazeRod) {
				rare.add(Item.blazeRod, 2);
			}
		}
		
		if (i == 10) {
			// labyrinth vault chest
			useless.clear();
			
			common.add(Item.ingotIron, 3);
			common.add(Item.shardEmerald, 3);
			common.add(TFItems.mazeWafer, 12);
			common.add(TFItems.ironwoodIngot, 3);
			common.add(potion(16373)); // health ii

			uncommon.addRandomEnchanted(Item.bow, 8);
			uncommon.addRandomEnchanted(Item.bow, 8);
			uncommon.add(TFItems.steeleafShovel, 1);
			uncommon.add(TFItems.steeleafAxe, 1);
			uncommon.add(TFItems.steeleafIngot, 4);
			uncommon.add(TFItems.steeleafLegs, 1);
			uncommon.add(TFItems.steeleafBoots, 1);
			uncommon.add(TFItems.steeleafHelm, 1);
			
			rare.add(Item.emerald, 1); // was the emerald block
			rare.add(TFItems.steeleafPick, 1);
			rare.add(TFItems.steeleafSword, 1);
			rare.add(TFItems.mazebreakerPick, 1); // plain; at most one per maze is up to the structure
			rare.add(Item.shardEmerald, 2);
			// no ender chest (MITE_BALANCE L4)
		}

		if (i == 7) {
			// wizards tower treasure room chest
			common.add(Item.glassBottle, 6);
			common.add(potion(0));
			common.add(Item.sugar, 5);
			common.add(Item.spiderEye, 3);
			common.add(Item.ghastTear, 1);
			common.add(Item.fermentedSpiderEye, 1);
			common.add(Item.paper, 6);

			uncommon.addRandomEnchanted(Item.swordGold, 4);
			uncommon.addRandomEnchanted(Item.bootsGold, 4);
			uncommon.add(potion(16341)); // healing
			
			rare.addRandomEnchanted(Item.helmetGold, 4);
			rare.add(potion(16306)); // swiftness ii
			rare.add(TFItems.transformPowder, 4);
			rare.add(TFItems.charmOfLife1, 1);
			rare.add(TFItems.charmOfKeeping1, 1);

			ultrarare.addRandomEnchanted(Item.axeGold, 4);
			ultrarare.add(Item.enderPearl, 1);
			ultrarare.add(Block.obsidian, 2);
			ultrarare.add(Item.shardDiamond, 1);
			ultrarare.add(TFItems.moonwormQueen, 1);
			ultrarare.add(TFItems.peacockFan, 1);
		}

		if (i == 8) {
			// wizards tower library chest
			common.add(Item.glassBottle, 6);
			common.add(potion(0));
			common.add(Block.ladder, 6);
			common.add(Item.paper, 6);
			common.add(Item.bone, 6);
			common.add(Item.goldNugget, 6);
			common.add(Item.clay, 12);

			uncommon.addRandomEnchanted(Item.legsIron, 4);
			uncommon.add(Item.book, 5);
			uncommon.add(Item.emptyMap, 1);
			uncommon.add(potion(16276)); // poison
			
			rare.addRandomEnchanted(Item.bow, 4);
			rare.add(TFItems.transformPowder, 4);
			rare.add(TFItems.charmOfKeeping1, 1);
			rare.add(potion(16373)); // health ii
			
			ultrarare.addRandomEnchanted(Item.pickaxeGold, 4);
			ultrarare.addRandomEnchanted(Item.swordIron, 4);
			ultrarare.add(Item.enderPearl, 1);
			ultrarare.add(Block.bookShelf, 5);
			ultrarare.add(Item.expBottle, 1);
		}
		
		if (i == 9) {
			// basement treasure cache, mostly full of food!
			common.add(potion(0));
			common.add(Item.rottenFlesh, 6);
			common.add(Item.wheat, 6);
			common.add(Item.potato, 6);
			common.add(Item.carrot, 6);
			common.add(Item.melon, 6);
			common.add(Block.torchWood, 12);
			
			uncommon.add(Item.bread, 8);
			uncommon.add(Item.beefCooked, 3);
			uncommon.add(Item.porkCooked, 3);
			uncommon.add(Item.bakedPotato, 3);
			uncommon.add(Item.chickenCooked, 3);
			uncommon.add(Item.fishCooked, 3);
			
			rare.add(Item.speckledMelon, 4);
			rare.add(Item.appleRed, 4);
			rare.add(Item.emptyMap, 1);
			rare.add(TFItems.charmOfKeeping1, 1);
			
			ultrarare.add(Item.appleGold, 1, 2); // about 2% per chest
			ultrarare.add(Item.goldenCarrot, 1);
			ultrarare.add(Item.cake, 1);
			ultrarare.add(new ItemStack(TFBlocks.sapling, 1, 4));
		}

		if (i == 11) {
			// dark tower regular treasure cache, contains supplies
			common.add(Item.stick, 12);
			common.add(new ItemStack(Item.coal, 12, 1));
			common.add(Item.arrowFlint, 12);
			common.add(TFItems.experiment115, 9);
			common.add(new ItemStack(Block.cloth, 1, 14));
			common.add(Item.redstone, 6);

			uncommon.add(Block.redstoneLampIdle, 3);
			uncommon.add(Item.ingotIron, 3);
			uncommon.add(TFItems.ironwoodIngot, 3);
			uncommon.add(TFBlocks.firefly, 5);
			uncommon.add(TFItems.charmOfKeeping1, 1);

			rare.add(TFItems.steeleafIngot, 3);
			rare.add(Item.shardDiamond, 2);

		}

		if (i == 12) {
			// dark tower key treasure chest, contains higher quality treasure
			// key will be added seperately
			useless.clear();
			
			common.add(Item.ingotIron, 2);
			common.add(TFItems.experiment115, 12);
			common.add(Item.gunpowder, 4);
			common.add(TFItems.ironwoodIngot, 3);
			common.add(TFBlocks.firefly, 5);
			common.add(Item.redstone, 12);
			common.add(Item.glowstone, 12);

			uncommon.add(TFItems.steeleafIngot, 3);
			uncommon.add(TFItems.steeleafHelm, 1);
			uncommon.add(TFItems.steeleafBoots, 1);
			uncommon.add(TFItems.ironwoodSword, 1);
			
			rare.add(TFItems.charmOfLife1, 1);
			rare.addEnchantedBook(Enchantment.featherFalling, 2);
			rare.addEnchantedBook(Enchantment.knockback, 2);
			rare.addEnchantedBook(Enchantment.efficiency, 2);
		}

		if (i == 13) {
			// dark tower boss treasure
			useless.clear();
			
			common.add(TFItems.carminite, 3);
			uncommon.add(TFItems.fieryTears, 5);
			rare.add(new ItemStack(TFItems.trophy, 1, 3));
		}

		if (i == 14) {
			// tree branch treasure cache, food and saplings
			common.add(Item.poisonousPotato, 2);
			common.add(Item.wheat, 6);
			common.add(Item.potato, 6);
			common.add(Item.carrot, 6);
			common.add(Item.melon, 6);
			
			uncommon.add(new ItemStack(TFBlocks.firefly, 12));
			uncommon.add(new ItemStack(TFBlocks.sapling, 4, 0));
			uncommon.add(new ItemStack(TFBlocks.sapling, 4, 1));
			uncommon.add(new ItemStack(TFBlocks.sapling, 4, 2));
			uncommon.add(new ItemStack(TFBlocks.sapling, 4, 3));
			
			rare.add(Item.pumpkinPie, 4);
			rare.add(Item.appleRed, 4);
			rare.add(TFItems.charmOfLife1, 1);
			rare.add(TFItems.charmOfKeeping1, 1);

			// no time tree (meta 5) and no mining tree (meta 7): MITE_BALANCE section 12 #2, #3
			ultrarare.add(new ItemStack(TFBlocks.sapling, 1, 4));
			ultrarare.add(new ItemStack(TFBlocks.sapling, 1, 6));
			ultrarare.add(new ItemStack(TFBlocks.sapling, 1, 8));
		}
		
		if (i == 15) {
			// stronghold regular treasure cache, contains supplies
			common.add(Item.stick, 12);
			common.add(new ItemStack(Item.coal, 4));
			common.add(Item.arrowFlint, 12);
			common.add(TFItems.mazeWafer, 9);
			common.add(new ItemStack(Block.cloth, 1, 11));
			common.add(Item.ingotIron, 2);

			uncommon.add(Item.bucketIronEmpty, 1);
			uncommon.add(Item.ingotIron, 3);
			uncommon.add(TFItems.ironwoodIngot, 3);
			uncommon.add(TFBlocks.firefly, 5);
			uncommon.add(TFItems.charmOfKeeping1, 1);
			uncommon.add(TFItems.armorShard, 3);

			rare.add(TFItems.knightMetal, 2);
			rare.addRandomEnchanted(Item.bow, 12);
			rare.addRandomEnchanted(Item.swordIron, 12);
			rare.addRandomEnchanted(TFItems.ironwoodSword, 12);
			rare.addRandomEnchanted(TFItems.steeleafSword, 12);
			
			ultrarare.addEnchantedBook(Enchantment.baneOfArthropods, 2);
			ultrarare.addEnchantedBook(Enchantment.sharpness, 2);
			ultrarare.addEnchantedBook(Enchantment.smite, 2);
			ultrarare.addEnchantedBook(Enchantment.unbreaking, 2);

		}
		
		if (i == 16) {
			// stronghold treasure room chest - kinda rare
			useless.clear();
			
			common.add(Item.ingotIron, 3);
			common.add(TFItems.mazeWafer, 12);
			common.add(Item.gunpowder, 4);
			common.add(TFItems.ironwoodIngot, 3);
			common.add(TFBlocks.firefly, 5);
			common.add(Item.bucketIronMilk, 1);

			uncommon.add(TFItems.steeleafIngot, 3);
			uncommon.add(TFItems.steeleafHelm, 1);
			uncommon.add(TFItems.steeleafPick, 1);
			uncommon.add(TFItems.ironwoodSword, 1);
			uncommon.add(TFItems.charmOfLife1, 1);
			
			rare.add(TFItems.mazeMapFocus, 1);
			rare.addRandomEnchanted(Item.bow, 12);
			rare.addRandomEnchanted(Item.swordIron, 12);
			rare.addRandomEnchanted(TFItems.ironwoodSword, 12);
			rare.addRandomEnchanted(TFItems.steeleafSword, 12);
			rare.addRandomEnchanted(TFItems.knightlySword, 10); // was the diamond sword

		}
		
		if (i == 17) {
			// stronghold boss treasure
			useless.clear();
			
			common.addRandomEnchanted(TFItems.knightlySword, 8);
			common.addRandomEnchanted(TFItems.knightlyPick, 8);
			common.addRandomEnchanted(TFItems.knightlyAxe, 8);
			uncommon.addRandomEnchanted(TFItems.phantomHelm, 8);
			uncommon.addRandomEnchanted(TFItems.phantomPlate, 8);
			rare.addRandomEnchanted(TFItems.phantomHelm, 12);
			rare.addRandomEnchanted(TFItems.phantomPlate, 12);
		}

		if (i == 18) {
			// aurora cache
			common.add(Item.stick, 12);
			common.add(new ItemStack(Item.coal, 4));
			common.add(Item.arrowFlint, 12);
			common.add(TFItems.mazeWafer, 9);
			common.add(Block.ice, 4);
			// TODO(mite-port): MITE has no packed ice block, a second ice entry stands in for it
			common.add(Block.ice, 4);
			common.add(TFItems.ironwoodIngot, 2);

			uncommon.add(TFBlocks.auroraBlock, 12);
			uncommon.add(TFItems.ironwoodIngot, 3);
			uncommon.add(TFBlocks.firefly, 5);
			uncommon.add(TFItems.charmOfKeeping1, 1);
			uncommon.add(TFItems.arcticFur, 3);

			rare.add(TFItems.arcticFur, 8);
			rare.addRandomEnchanted(TFItems.iceBow, 12);
			rare.add(TFItems.enderBow, 1);
			rare.add(TFItems.iceSword, 1);
			
			ultrarare.addEnchantedBook(Enchantment.sharpness, 2);
			ultrarare.addEnchantedBook(Enchantment.power, 2);
			ultrarare.addEnchantedBook(Enchantment.punch, 2);
			ultrarare.addEnchantedBook(Enchantment.unbreaking, 2);
			ultrarare.addEnchantedBook(Enchantment.unbreaking, 2);
			// TODO(mite-port): MITE has no Infinity; arrow recovery replaces it (MITE_MISC 5.1)
			ultrarare.addEnchantedBook(Enchantment.arrow_recovery, 1);

		}

		if (i == 19) {
			// aurora treasure
			useless.clear();
			
			common.add(Block.ice, 4);
			// TODO(mite-port): MITE has no packed ice block, a second ice entry stands in for it
			common.add(Block.ice, 4);
			common.add(TFItems.mazeWafer, 12);
			common.add(TFItems.iceBomb, 4);
			common.add(TFBlocks.firefly, 5);

			uncommon.add(TFItems.arcticFur, 6);
			uncommon.add(TFItems.arcticLegs, 1);
			uncommon.add(TFItems.arcticPlate, 1);
			uncommon.add(TFItems.arcticHelm, 1);
			uncommon.add(TFItems.arcticBoots, 1);
			uncommon.add(TFItems.knightlySword, 1);
			uncommon.add(TFItems.knightlySword, 1);
			uncommon.add(TFItems.charmOfLife1, 1);
			
			rare.addRandomEnchanted(TFItems.iceBow, 12);
			rare.add(TFItems.enderBow, 1);
			rare.add(TFItems.iceSword, 1);
			rare.add(TFItems.glassSword, 1); // plain; at most one per structure is up to the structure

		}

		if (i == 21) {
			// troll garden
			useless.clear();
			
			common.add(Block.mushroomRed, 4);
			common.add(Block.mushroomBrown, 4);
			common.add(Item.seeds, 6);
			common.add(Item.carrot, 6);
			common.add(Item.potato, 6);
			common.add(Item.melonSeeds, 6);
			common.add(new ItemStack(Item.dyePowder, 12, 15));

			uncommon.add(TFBlocks.uberousSoil, 6);
			
			rare.add(TFItems.magicBeans, 1);

		}

		if (i == 22) {
			// troll vault
			useless.clear();
			
			common.add(Item.coal, 8);
			common.add(TFItems.torchberries, 8);
			common.add(Item.shardEmerald, 6);

			uncommon.add(TFBlocks.trollSteinn, 6);
			uncommon.add(Block.obsidian, 3);
			
			rare.add(TFItems.lampOfCinders, 1);

		}

	}
}
