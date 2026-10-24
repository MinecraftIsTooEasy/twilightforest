package twilightforest.creativetab;

import huix.glacier.api.extension.creativetab.GlacierCreativeTabs;
import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.Item;
import twilightforest.block.TFBlocks;
import twilightforest.item.TFItems;

/**
 * The two Twilight Forest creative tabs (RustedIronCore {@link GlacierCreativeTabs}; the vanilla tab array is fixed at 12 entries).
 * Lang keys: {@code itemGroup.twilightforest.blocks} / {@code itemGroup.twilightforest.items}.
 * <p>
 * Loading this class creates the tabs, so it is first touched from {@code TFBlocks.registerBlocks} (ItemRegistryEvent); the icon
 * lookups are lazy (evaluated when the creative GUI draws), so they never force Block/Item static initialisation early.
 */
public class TFCreativeTabs {
    public static final CreativeTabs BLOCKS = new GlacierCreativeTabs("twilightforest.blocks") {
        @Override
        public int getTabIconItemIndex() {
            Block b = TFBlocks.portal;
            return b == null ? 1 : b.blockID;
        }
    };

    public static final CreativeTabs ITEMS = new GlacierCreativeTabs("twilightforest.items") {
        @Override
        public int getTabIconItemIndex() {
            Item i = TFItems.trophy;
            return i == null ? Item.appleRed.itemID : i.itemID;
        }
    };

    private TFCreativeTabs() {
    }
}
