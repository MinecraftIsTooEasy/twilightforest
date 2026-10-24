package twilightforest.compat;

import net.minecraft.Block;
import net.minecraft.BlockOre;
import net.minecraft.BlockRedstoneOre;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import twilightforest.block.TFBlocks;
import twilightforest.item.TFItems;

/** Replaces the OreDictionary tags TF used internally (logWood, ore*, treeLeaves, treeSapling, plankWood). */
public final class TFTags {
    private TFTags() {
    }

    /** Tag "logWood". */
    public static boolean isLogWood(Block b) {
        return TFBlockUtil.isWood(b);
    }

    public static boolean isLogWood(ItemStack s) {
        return isLogWood(blockOf(s));
    }

    /** Any tag starting with "ore": MITE ore blocks (incl. redstone) plus oreIronwood / oreKnightmetal. */
    public static boolean isOre(ItemStack s) {
        if (s == null) {
            return false;
        }
        Item item = s.getItem();
        if (item == TFItems.ironwoodRaw || item == TFItems.shardCluster) {
            return true;
        }
        Block b = blockOf(s);
        return b instanceof BlockOre || b instanceof BlockRedstoneOre;
    }

    /** Tag "treeLeaves". */
    public static boolean isTreeLeaves(Block b) {
        return TFBlockUtil.isLeaves(b);
    }

    /** Tag "treeSapling" (MITE saplings plus the TF sapling block). */
    public static boolean isTreeSapling(Block b) {
        return b == Block.sapling || b == TFBlocks.sapling;
    }

    /** Tag "plankWood" (MITE planks plus TF tower wood). */
    public static boolean isPlankWood(Block b) {
        return b == Block.planks || b == TFBlocks.towerWood;
    }

    /** The block of an ItemBlock stack, or null. */
    public static Block blockOf(ItemStack s) {
        return s != null && s.getItem() instanceof ItemBlock ib ? Block.getBlock(ib.getBlockID()) : null;
    }
}
