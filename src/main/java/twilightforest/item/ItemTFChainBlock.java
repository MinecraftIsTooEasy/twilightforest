package twilightforest.item;

import java.util.WeakHashMap;

import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.Icon;
import net.minecraft.IDamageableItem;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import twilightforest.entity.EntityTFChainBlock;

/**
 * Knightmetal chain block (TF 1.7.10 ItemTFChainBlock, was an ItemTool). Throws an EntityTFChainBlock that returns to the player.
 * MITE_BALANCE 7.2: 32 uses (was 99), 1 durability per throw, 40 tick cooldown; the block restrictions live in the entity.
 * TODO(mite-port): onEntitySwing / getHarvestLevel (pickaxe 2) have no MITE hook and were removed (MITE_BLOCK_ITEM 16.4).
 */
public class ItemTFChainBlock extends ItemTF implements IDamageableItem {

    /** Ticks between two throws (MITE_BALANCE 7.2). */
    public static final int COOLDOWN_TICKS = 40;

    // which items have launched which blocks?
    private final WeakHashMap<ItemStack, Entity> launchedBlocksMap = new WeakHashMap<ItemStack, Entity>();
    // world time of the last throw per stack (server)
    private final WeakHashMap<ItemStack, Long> lastThrowTime = new WeakHashMap<ItemStack, Long>();

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.CHAIN_BLOCK) */
    public ItemTFChainBlock(int id) {
        super(id, Material.iron);
        this.setMaxStackSize(1);
        this.setMaxDamage(32);
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    @Override
    public int getNumComponentsForDurability() {
        return 4;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }

    /** Repair with knightmetal nuggets (MITE_BALANCE 6.7). */
    @Override
    public Item getRepairItem() {
        return TFItems.knightMetalNugget;
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) {
            return false;
        }
        World world = player.worldObj;

        if (player.onClient()) {
            player.setHeldItemInUse();
            return true;
        }

        player.setHeldItemInUse();

        if (!this.hasLaunchedBlock(stack) && this.isCooledDown(world, stack)) {

            world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F));

            EntityTFChainBlock launchedBlock = new EntityTFChainBlock(world, player);
            world.spawnEntityInWorld(launchedBlock);
            this.setLaunchedBlock(stack, launchedBlock);
            this.lastThrowTime.put(stack, world.getTotalWorldTime());

            setChainAsThrown(stack);

            player.tryDamageHeldItem(DamageSource.generic, 1);
        }
        return true;
    }

    private boolean isCooledDown(World world, ItemStack stack) {
        Long last = this.lastThrowTime.get(stack);
        return last == null || world.getTotalWorldTime() - last >= COOLDOWN_TICKS;
    }

    /**
     * Set this item as having been thrown
     */
    public static void setChainAsThrown(ItemStack stack) {
        // set NBT tag for stack
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean("thrown", true);
    }

    /**
     * Set the spike block for this item as returned to the player
     */
    public static void setChainAsReturned(ItemStack stack) {
        // set NBT tag for stack
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean("thrown", false);
    }

    /**
     * Method for the client to determine if the block has been thrown or not.  Not as accurate as server method due to lag, etc.
     */
    public static boolean doesChainHaveBlock(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return true;
        } else {
            return !stack.getTagCompound().getBoolean("thrown");
        }
    }

    /**
     * Set the spike block belonging to the player as returned
     */
    public static void setChainAsReturned(EntityPlayer player) {
        if (player != null && player.getHeldItemStack() != null && player.getHeldItemStack().getItem() == TFItems.chainBlock) {
            setChainAsReturned(player.getHeldItemStack());
        }
    }

    /**
     * Method on the server that determines definitively if the item has been thrown or not
     */
    public boolean hasLaunchedBlock(ItemStack stack) {
        Entity cube = this.launchedBlocksMap.get(stack);

        return cube != null && !cube.isDead;
    }

    public void setLaunchedBlock(ItemStack stack, EntityTFChainBlock launchedCube) {
        this.launchedBlocksMap.put(stack, launchedCube);
    }

    /**
     * Player, item usage sensitive icon: the knightmetal ring once the block is thrown.
     * TODO(mite-port): MITE has no Item.getIcon(stack, pass, player, using, remaining) hook; wire into EntityPlayer.getItemIcon
     * via the client icon mixin (TF_FORGE_HOOKS M23) or the item renderer.
     */
    public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (doesChainHaveBlock(stack)) {
            return this.itemIcon;
        } else {
            return TFItems.knightmetalRing.getIconIndex(stack);
        }
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BLOCK;
    }
}
