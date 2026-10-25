package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.Item;
import twilightforest.TFConfig;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;

/**
 * Picks the block id range before TFBlocks creates anything. FML gives mods without fixed ids block ids counting down from
 * 4095, so a large pack (FutureMITE and friends) can already sit on 3652-3779; Block's constructor then threw for the first
 * TF block, the event bus swallowed it and every TF block and item stayed null (portal, biome and recipe crashes later).
 */
public final class TFBlockIds {
    private static final int SPAN = TFIds.BLOCK_LAST - TFIds.BLOCK_FIRST + 1;
    /** Lowest id the automatic search uses: far above vanilla and far below the dynamic ids that count down from 4095. */
    private static final int SEARCH_FROM = 1024;

    private TFBlockIds() {
    }

    public static void resolve() {
        int configured = TFConfig.blockIdBase;
        int base;
        if (configured > 0) {
            base = configured;
            String occupant = firstOccupant(base);
            if (occupant != null) {
                // a plain exception is swallowed by the event bus and leaves TF half registered; an Error stops the start
                throw new Error("Twilight Forest blockIdBase " + base + " is already used (" + occupant + "). Set dimension.blockIdBase"
                        + " in config/twilightforest.json to 0 (automatic) or to a free range of " + SPAN + " ids.");
            }
        } else {
            base = TFIds.BLOCK_FIRST;
            String occupant = firstOccupant(base);
            if (occupant != null) {
                base = findFreeRange();
                if (base < 0) {
                    throw new Error("Twilight Forest finds no free range of " + SPAN + " block ids (3652 is used by " + occupant + ").");
                }
                TwilightForestMod.LOGGER.warn("Twilight Forest block ids {}-{} are taken ({}); using {}-{} instead",
                        TFIds.BLOCK_FIRST, TFIds.BLOCK_LAST, occupant, base, base + SPAN - 1);
            }
            // keep the choice: a later mod change must not move the blocks of an existing world
            TFConfig.BLOCK_ID_BASE.set(base);
            TFConfig.blockIdBase = base;
            TFConfig.save();
        }
        TFIds.relocateBlocks(base);
        TwilightForestMod.LOGGER.info("Twilight Forest block ids {}-{}", base, base + SPAN - 1);
    }

    private static int findFreeRange() {
        for (int base = SEARCH_FROM; base + SPAN <= Block.blocksList.length; base++) {
            if (firstOccupant(base) == null) {
                return base;
            }
        }
        return -1;
    }

    /** Description of the first block or item in [base, base + SPAN), or null when the whole range is free. */
    private static String firstOccupant(int base) {
        if (base < 256 || base + SPAN > Block.blocksList.length) {
            return "outside 256-" + (Block.blocksList.length - 1);
        }
        for (int id = base; id < base + SPAN; id++) {
            if (Block.blocksList[id] != null) {
                return "block " + id + " " + Block.blocksList[id].getClass().getName();
            }
            if (id < Item.itemsList.length && Item.itemsList[id] != null) {
                return "item " + id + " " + Item.itemsList[id].getClass().getName();
            }
        }
        return null;
    }
}
