package twilightforest.compat;

import net.minecraft.Block;
import net.minecraft.BlockAnvil;
import net.minecraft.BlockButton;
import net.minecraft.BlockChest;
import net.minecraft.BlockFurnace;
import net.minecraft.BlockHopper;
import net.minecraft.BlockLadder;
import net.minecraft.BlockMushroomCap;
import net.minecraft.BlockSign;
import net.minecraft.BlockSkull;
import net.minecraft.BlockTorch;
import net.minecraft.Minecraft;
import twilightforest.TwilightForestMod;
import twilightforest.block.BlockTFCritter;
import twilightforest.block.BlockTFLeaves;
import twilightforest.block.BlockTFLeaves3;
import twilightforest.block.BlockTFMagicLeaves;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.IBlockAccess;
import net.minecraft.ItemStack;
import net.minecraft.World;

/**
 * 1.7.10 World idioms on MITE. {@code world.getBlock(x,y,z)} exists in MITE (IBlockAccess) but returns null for air,
 * where 1.7.10 returned Blocks.air: replace {@code block == Blocks.air} by {@code block == null}.
 */
public final class TFWorldUtil {
    /** setBlock flags: notify neighbours + send to clients. */
    public static final int FLAGS_DEFAULT = 3;

    private TFWorldUtil() {
    }

    public static int id(Block b) {
        return b == null ? 0 : b.blockID;
    }

    /** True if the block at x,y,z is air (block is null). */
    public static boolean isAir(IBlockAccess w, int x, int y, int z) {
        return w.isAirBlock(x, y, z);
    }

    /** Is the position inside a loaded chunk (World.blockExists). */
    public static boolean isLoaded(World w, int x, int y, int z) {
        return w.blockExists(x, y, z);
    }

    private static final java.util.Set<Long> warnedMeta = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** world.setBlock(x,y,z, Block, meta, flags) with null meaning air. Metadata the block does not accept is normalised (and logged in dev mode). */
    public static boolean setBlock(World w, int x, int y, int z, Block b, int meta, int flags) {
        if (b == null) {
            return w.setBlock(x, y, z, 0, 0, flags);
        }
        return w.setBlock(x, y, z, b.blockID, checkedMeta(b, meta), flags);
    }

    /**
     * Dev-mode check: logs block, metadata and caller when the block rejects the metadata, then returns the normalised value.
     * Every TF set-block helper (TFWorldUtil.setBlock, TFGenerator, structure components) goes through this.
     */
    public static int checkedMeta(Block b, int meta) {
        if (b == null || b.isValidMetadata(meta)) {
            return meta;
        }
        int fixed = sanitizeMeta(b, meta);
        if (Minecraft.inDevMode() && warnedMeta.add(((long) b.blockID << 32) | (meta & 0xFFFFFFFFL))) {
            String caller = StackWalker.getInstance().walk(frames -> frames
                    .filter(f -> !f.getClassName().equals(TFWorldUtil.class.getName()))
                    .findFirst().map(f -> f.getClassName() + "." + f.getMethodName() + ":" + f.getLineNumber()).orElse("?"));
            TwilightForestMod.LOGGER.warn("[TF meta check] block {} (id {}) placed with invalid metadata {} (normalised to {}) by {}",
                    b.getClass().getSimpleName(), b.blockID, meta, fixed, caller);
        }
        return fixed;
    }

    /** The metadata the block accepts that is closest to {@code meta} (lower value wins a tie); {@code meta} itself if there is none. */
    public static int nearestValidMeta(Block block, int meta) {
        int best = meta;
        int bestDistance = Integer.MAX_VALUE;
        for (int m = 0; m < 16; m++) {
            if (block.isValidMetadata(m) && Math.abs(m - meta) < bestDistance) {
                best = m;
                bestDistance = Math.abs(m - meta);
            }
        }
        return best;
    }

    /**
     * Structure-placed leaves must never decay: sets bit 4 (placed by a player, ignored by decay) and clears the decay-check bit 8 on
     * every decaying leaf block (TF oak/canopy/mangrove/rainbow leaves, thorn/beanstalk leaves, magic tree leaves, vanilla leaves).
     * Dark leaves and giant leaves never decay and are left alone.
     */
    public static int structureMeta(Block block, int meta) {
        if (block instanceof BlockTFLeaves || block instanceof BlockTFLeaves3 || block instanceof BlockTFMagicLeaves || block == Block.leaves) {
            return (meta | 4) & ~8;
        }
        return meta;
    }

    /**
     * Maps metadata that a block rejects to the nearest legal value (1.7.10 code used 0 or a rotation index freely): chests, furnaces,
     * ladders, signs and skulls take the facings 2-5, torches become standing (5), buttons 1-4, anvils 0-11, hoppers 0, TF critters 0-6.
     * Returns the input unchanged if the block is unknown or no legal replacement exists.
     */
    public static int sanitizeMeta(Block block, int meta) {
        if (block == null || block.isValidMetadata(meta)) {
            return meta;
        }
        int fixed;
        if (block instanceof BlockChest || block instanceof BlockFurnace || block instanceof BlockLadder
                || block instanceof BlockSkull || block instanceof BlockSign) {
            fixed = meta < 2 || meta > 5 ? 2 + (meta & 3) : meta;
        } else if (block instanceof BlockTorch) {
            fixed = 5;
        } else if (block instanceof BlockAnvil) {
            fixed = ((meta % 12) + 12) % 12;
        } else if (block instanceof BlockHopper) {
            fixed = 0;
        } else if (block instanceof BlockButton) {
            fixed = meta == 0 ? 1 : 1 + (meta & 3);
        } else if (block instanceof BlockTFCritter) {
            int m = meta & 7;
            fixed = m > 6 ? 0 : m;
        } else if (block instanceof BlockMushroomCap) {
            // 1.7.10 meta 15 (stem texture on every side) does not exist in MITE; 10 is the stem on the four sides
            fixed = meta == 15 ? 10 : 14;
        } else {
            // any other block (including every BlockMounted whose face lookup would return null): nearest metadata it accepts
            fixed = nearestValidMeta(block, meta);
        }
        return block.isValidMetadata(fixed) ? fixed : meta;
    }

    /** world.setBlock(x,y,z, Block): metadata 0, flags 3. */
    public static boolean setBlock(World w, int x, int y, int z, Block b) {
        return setBlock(w, x, y, z, b, 0, FLAGS_DEFAULT);
    }

    /** world.setBlock(x,y,z, Block, meta, 3). */
    public static boolean setBlock(World w, int x, int y, int z, Block b, int meta) {
        return setBlock(w, x, y, z, b, meta, FLAGS_DEFAULT);
    }

    /** World.setBlockToAir (no drops). */
    public static boolean setAir(World w, int x, int y, int z) {
        return w.setBlockToAir(x, y, z);
    }

    /** Block at x,y,z is this block (null matches air). */
    public static boolean is(IBlockAccess w, int x, int y, int z, Block b) {
        return w.getBlock(x, y, z) == b;
    }

    /** Drops an item entity at the position (server only, null/empty stacks ignored); returns it or null. */
    public static EntityItem spawnItem(World w, double x, double y, double z, ItemStack stack) {
        if (w.isRemote || stack == null || stack.stackSize <= 0) {
            return null;
        }
        EntityItem item = new EntityItem(w, x, y, z, stack);
        item.delayBeforeCanPickup = 10;
        w.spawnEntityInWorld(item);
        return item;
    }

    /** Spawns an entity on the server only (1.7.10 idiom {@code if (!world.isRemote) world.spawnEntityInWorld(e)}). */
    public static boolean spawn(World w, Entity e) {
        return !w.isRemote && w.spawnEntityInWorld(e);
    }

    public static void playSound(World w, double x, double y, double z, String sound, float volume, float pitch) {
        w.playSoundEffect(x, y, z, sound, volume, pitch);
    }
}
