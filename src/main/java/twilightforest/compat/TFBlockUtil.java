package twilightforest.compat;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.IBlockAccess;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.block.BlockTFCinderLog;
import twilightforest.block.BlockTFLog;
import twilightforest.block.BlockTFMagicLog;

/**
 * Block query shims for Forge Block methods that MITE lacks. Air is {@code null} in MITE ({@code Block.getBlock(0)}),
 * so every method here accepts a null block.
 */
public final class TFBlockUtil {
    private TFBlockUtil() {
    }

    /** Forge Block.isLeaves. */
    public static boolean isLeaves(Block b) {
        return b != null && b.isTreeLeaves();
    }

    public static boolean isLeaves(IBlockAccess w, int x, int y, int z) {
        return isLeaves(w.getBlock(x, y, z));
    }

    /** Forge Block.isWood (BlockLog is final in MITE, so TF logs are matched by class). */
    public static boolean isWood(Block b) {
        return b == Block.wood || b instanceof BlockTFLog || b instanceof BlockTFMagicLog || b instanceof BlockTFCinderLog;
    }

    public static boolean isWood(IBlockAccess w, int x, int y, int z) {
        return isWood(w.getBlock(x, y, z));
    }

    /** Forge Block.canBeReplacedByLeaves: air, any non-opaque block, or a block marked {@link ITFReplaceableByLeaves}. */
    public static boolean canBeReplacedByLeaves(IBlockAccess w, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        // do not use Block.isOpaqueStandardFormCube(int): it is final, always false and logs an error
        return b == null || b instanceof ITFReplaceableByLeaves || !Block.isBlockOpaqueStandardFormCube(w, x, y, z);
    }

    /** Forge Block.canSustainLeaves: logs, plus blocks implementing {@link ITFLeafSupport}. */
    public static boolean canSustainLeaves(IBlockAccess w, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        if (b instanceof ITFLeafSupport s) {
            return s.canSustainLeaves(w, x, y, z);
        }
        return isWood(b);
    }

    /** Forge Block.isReplaceable (Blocks.air included). */
    public static boolean isReplaceable(IBlockAccess w, int x, int y, int z) {
        Block b = w.getBlock(x, y, z);
        return b == null || b.blockMaterial.isReplaceable() || b.isAlwaysReplaceable();
    }

    /** Forge Block.isNormalCube. */
    public static boolean isNormalCube(IBlockAccess w, int x, int y, int z) {
        return w.isBlockNormalCube(x, y, z);
    }

    /** Forge Block.isSideSolid: is the face {@code side} of the block at x,y,z flat and solid. */
    public static boolean isSideSolid(IBlockAccess w, int x, int y, int z, ForgeDirection side) {
        Block b = w.getBlock(x, y, z);
        EnumFace face = side.toEnumFace();
        return b != null && face != null && b.isFaceFlatAndSolid(w.getBlockMetadata(x, y, z), face);
    }

    /** Forge Block.canSustainPlant for the block at x,y,z (the soil), including the Beach water check. */
    public static boolean canSustainPlant(IBlockAccess w, int x, int y, int z, EnumPlantType type) {
        Block below = w.getBlock(x, y, z);
        int meta = w.getBlockMetadata(x, y, z);
        if (type == EnumPlantType.Beach) {
            boolean ground = below == Block.grass || below == Block.dirt || below == Block.sand;
            if (!ground) {
                return false;
            }
            return w.getBlockMaterial(x - 1, y, z) == Material.water || w.getBlockMaterial(x + 1, y, z) == Material.water
                || w.getBlockMaterial(x, y, z - 1) == Material.water || w.getBlockMaterial(x, y, z + 1) == Material.water;
        }
        return canSustainPlant(below, meta, type);
    }

    /** Forge Block.canSustainPlant without world context (Beach is never sustained here). */
    public static boolean canSustainPlant(Block below, int belowMeta, EnumPlantType type) {
        if (below == null) {
            return false;
        }
        if (below instanceof ITFPlantSoil soil) {
            return soil.canSustainPlant(type);
        }
        return switch (type) {
            case Desert -> below == Block.sand;
            case Nether -> below == Block.slowSand;
            case Crop -> below == Block.tilledField;
            case Cave -> below.isFaceFlatAndSolid(belowMeta, EnumFace.TOP);
            case Plains -> below == Block.grass || below == Block.dirt || below == Block.tilledField;
            case Water -> below.blockMaterial == Material.water && belowMeta == 0;
            case Beach -> false;
        };
    }

    /**
     * Stand-in for Forge Block.getExplosionResistance. MITE has no separate resistance, it is the hardness, which is on
     * a different scale than 1.7.10 resistance: callers must re-tune their thresholds.
     */
    public static float getResistance(Block b, int meta) {
        return b == null ? 0.0F : b.getBlockHardness(meta);
    }

    /** True if the player's current tool makes progress against the block (replaces canHarvestBlock/getBlockHardness checks). */
    public static boolean canPlayerHarvest(EntityPlayer p, World w, int x, int y, int z) {
        return p.getCurrentPlayerStrVsBlock(x, y, z, true) > 0.0F;
    }

    /** Breaks the block as if {@code p} harvested it (drops included). */
    public static void harvestBlockAs(EntityPlayer p, World w, int x, int y, int z) {
        w.destroyBlock(new BlockBreakInfo(w, x, y, z).setHarvestedBy(p), true);
    }

    /** Forge Block.beginLeavesDecay: flags the leaf block for a decay check (metadata bit 8). */
    public static void beginLeavesDecay(World w, int x, int y, int z) {
        if (isLeaves(w, x, y, z)) {
            int meta = w.getBlockMetadata(x, y, z);
            if ((meta & 8) == 0) {
                w.setBlockMetadataWithNotify(x, y, z, meta | 8, 4);
            }
        }
    }
}
