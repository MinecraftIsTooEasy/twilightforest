package twilightforest.structures.trollcave;

import java.util.List;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentData;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntityChest;
import net.minecraft.World;
import twilightforest.item.TFItems;
import net.minecraft.StructureBoundingBox;
import net.minecraft.StructureComponent;
import twilightforest.block.TFBlocks;
import twilightforest.entity.EntityTFArmoredGiant;
import twilightforest.entity.EntityTFGiantMiner;
import twilightforest.structures.StructureTFComponent;

public class ComponentTFCloudCastle extends StructureTFComponent {

	private boolean minerPlaced = false;
	private boolean warriorPlaced = false;

	public ComponentTFCloudCastle() { }
	
	public ComponentTFCloudCastle(int index, int x, int y, int z) {
		super(index);
		this.setCoordBaseMode(0);

		
		// adjust x, y, z
    	x = (x >> 2) << 2;
    	y = (y >> 2) << 2;
    	z = (z >> 2) << 2;
    	
		// spawn list!
		this.spawnListIndex = 1;
    	
		this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -8, 0, -8, 16, 16, 16, 0);
	}
	
	/**
	 * Save to NBT
	 */
	@Override
	protected void func_143012_a(NBTTagCompound par1NBTTagCompound) {
		super.func_143012_a(par1NBTTagCompound);
		
        par1NBTTagCompound.setBoolean("minerPlaced", this.minerPlaced);
        par1NBTTagCompound.setBoolean("warriorPlaced", this.warriorPlaced);
	}

	/**
	 * Load from NBT
	 */
	@Override
	protected void func_143011_b(NBTTagCompound par1NBTTagCompound) {
		super.func_143011_b(par1NBTTagCompound);
        this.minerPlaced = par1NBTTagCompound.getBoolean("minerPlaced");
        this.warriorPlaced = par1NBTTagCompound.getBoolean("warriorPlaced");

	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildComponent(StructureComponent parent, List list, Random rand) {
		// up to two trees
		// tree in x direction
		boolean plus = rand.nextBoolean();
		int offset = rand.nextInt(5) - rand.nextInt(5);
		ComponentTFCloudTree treeX = new ComponentTFCloudTree(this.getComponentType() + 1, boundingBox.minX + (plus ? 16 : -16), 168, boundingBox.minZ - 8 + (offset * 4));
		list.add(treeX);
		treeX.buildComponent(this, list, rand);

		// tree in z direction
		plus = rand.nextBoolean();
		offset = rand.nextInt(5) - rand.nextInt(5);
		ComponentTFCloudTree treeZ = new ComponentTFCloudTree(this.getComponentType() + 1, boundingBox.minX - 8 + (offset * 4), 168, boundingBox.minZ + (plus ? 16 : -16));
		list.add(treeZ);
		treeZ.buildComponent(this, list, rand);

	}
	
	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
		// make haus
		this.fillWithMetadataBlocks(world, sbb, 0, -4, 0, 15, -1, 15, TFBlocks.fluffyCloud, 0,TFBlocks.fluffyCloud, 0, false); 
		this.fillWithMetadataBlocks(world, sbb, 0, 0, 0, 15, 11, 15, TFBlocks.giantCobble, 0, TFBlocks.giantCobble, 0, false); 
		this.fillWithMetadataBlocks(world, sbb, 0, 12, 0, 15, 15, 15, TFBlocks.giantLog, 0, TFBlocks.giantLog, 0, false); 
		
		
		// clear inside
		this.fillWithAir(world, sbb, 4, 0, 4, 11, 11, 11);

		// clear door
		this.fillWithAir(world, sbb, 0, 0, 4, 4, 7, 7);
		
		
		// add giants
		if (!this.minerPlaced) {
			int bx = this.getXWithOffset(6, 6);
			int by = this.getYWithOffset(0);
			int bz = this.getZWithOffset(6, 6);
			
			if (sbb.isVecInside(bx, by, bz)) {
				this.minerPlaced = true;
				
				EntityTFGiantMiner miner = new EntityTFGiantMiner(world);
				miner.setPosition(bx, by, bz);
				miner.makeNonDespawning();
				
				world.spawnEntityInWorld(miner);
			}
		}
		if (!this.warriorPlaced) {
			int bx = this.getXWithOffset(9, 9);
			int by = this.getYWithOffset(0);
			int bz = this.getZWithOffset(9, 9);
			
			if (sbb.isVecInside(bx, by, bz)) {
				this.warriorPlaced = true;
				
				EntityTFArmoredGiant warrior = new EntityTFArmoredGiant(world);
				warrior.setPosition(bx, by, bz);
				warrior.makeNonDespawning();
				
				world.spawnEntityInWorld(warrior);
			}
		}
		
		// reward chest on the very top of the roof: a fixed one-off haul that makes the climb worth it (exception to the
		// no-mithril chest rule of MITE_BALANCE L4: there is exactly one per cloud castle and the giants guard it)
		int cx = this.getXWithOffset(7, 7);
		int cy = this.getYWithOffset(16);
		int cz = this.getZWithOffset(7, 7);
		if (sbb.isVecInside(cx, cy, cz) && world.getBlockId(cx, cy, cz) != Block.chest.blockID) {
			world.setBlock(cx, cy, cz, Block.chest.blockID, 2, 2);
			TileEntityChest chest = (TileEntityChest) world.getBlockTileEntity(cx, cy, cz);
			if (chest != null) {
				fillRewardChest(chest);
			}
		}

		return true;
	}

	private static void fillRewardChest(TileEntityChest chest) {
		// metals: one block of each early metal, then a taste of the next tiers (not enough mithril for a full tool)
		chest.setInventorySlotContents(0, new ItemStack(Block.blockCopper));
		chest.setInventorySlotContents(1, new ItemStack(Block.blockSilver));
		chest.setInventorySlotContents(2, new ItemStack(Block.blockGold));
		chest.setInventorySlotContents(3, new ItemStack(Block.blockIron));
		chest.setInventorySlotContents(4, new ItemStack(Item.ingotAncientMetal, 4));
		chest.setInventorySlotContents(5, new ItemStack(Item.ingotMithril, 2));
		chest.setInventorySlotContents(6, new ItemStack(Item.diamond, 2));
		chest.setInventorySlotContents(7, new ItemStack(Item.emerald, 4));
		Item tungsten = findModItem("item.ingots/tungsten");
		if (tungsten != null) {
			// ITF-Reborn present: its mithril-tier metal
			chest.setInventorySlotContents(8, new ItemStack(tungsten, 2));
		}
		Item vibranium = findModItem("item.vibranium");
		if (vibranium != null) {
			// MITE-ITE present: one vibranium ingot (beyond adamantium), the bait players asked for; one alone makes no tool
			chest.setInventorySlotContents(16, new ItemStack(vibranium, 1));
		}
		// consumables and books
		chest.setInventorySlotContents(9, new ItemStack(Item.appleGold, 2, 0));
		chest.setInventorySlotContents(10, new ItemStack(Item.enderPearl, 4));
		chest.setInventorySlotContents(11, new ItemStack(Item.expBottle, 16));
		chest.setInventorySlotContents(12, Item.enchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantment.efficiency, 3)));
		chest.setInventorySlotContents(13, Item.enchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantment.unbreaking, 3)));
		chest.setInventorySlotContents(14, Item.enchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantment.protection, 3)));
		// Twilight specials
		chest.setInventorySlotContents(18, new ItemStack(TFItems.giantPick));
		chest.setInventorySlotContents(19, new ItemStack(TFItems.charmOfKeeping1));
		chest.setInventorySlotContents(20, new ItemStack(TFItems.charmOfLife1));
		chest.setInventorySlotContents(21, new ItemStack(TFItems.magicBeans));
	}

	/** Another mod's item by unlocalized name (no compile dependency), or null when that mod is not loaded. */
	private static Item findModItem(String unlocalizedName) {
		for (Item item : Item.itemsList) {
			if (item != null && unlocalizedName.equals(item.getUnlocalizedName())) {
				return item;
			}
		}
		return null;
	}
}
