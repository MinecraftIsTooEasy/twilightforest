package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFWorldUtil;

public class ItemTFMagicBeans extends ItemTF {

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.MAGIC_BEANS) */
    public ItemTFMagicBeans(int id) {
        super(id, Material.bone);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.makeRare();
    }

    /**
     * 1.7.10 onItemUse: right click on uberous soil grows a huge beanstalk up to the clouds (the beans are not consumed).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        RaycastCollision rc = player.getSelectedObject(partialTick, false);
        if (rc == null || !rc.isBlock() || rc.getBlockHit() != TFBlocks.uberousSoil) {
            return false;
        }

        World world = player.worldObj;
        int x = rc.block_hit_x;
        int y = rc.block_hit_y;
        int z = rc.block_hit_z;

        int minY = y + 1;
        int maxY = Math.max(y + 100, (int) (getCloudHeight(world) + 25));
        if (y < maxY) {
            if (player.onServer()) {
                // TODO(mite-port): MITE_BALANCE 7 wants the stalk trunk to be climbable (ladder/vine along the trunk or a climbable BlockTFHugeStalk)
                makeHugeStalk(world, x, z, minY, maxY);
            } else {
                player.swingArm();
            }
            return true;
        }
        return false;
    }

    /**
     * Try to find the given world's cloud height. WorldProvider.getCloudHeight exists in MITE on both sides and
     * WorldProviderTwilightForest overrides it.
     */
    private float getCloudHeight(World world) {
        return world.provider.getCloudHeight();
    }

    private void makeHugeStalk(World world, int x, int z, int minY, int maxY) {
        int yOffset = world.rand.nextInt(100);

        float cScale = world.rand.nextFloat() * 0.25F + 0.125F; // spiral tightness scaling
        float rScale = world.rand.nextFloat() * 0.25F + 0.125F; // radius change scaling

        // offset x and z to make stalk start at origin
        float radius = 4F + MathHelper.sin((minY + yOffset) * rScale) * 3F; // make radius a little wavy
        x -= MathHelper.sin((minY + yOffset) * cScale) * radius;
        z -= MathHelper.cos((minY + yOffset) * cScale) * radius;

        // leaves!
        int nextLeafY = minY + 10 + world.rand.nextInt(20);

        // make stalk
        boolean isClear = true;
        for (int dy = minY; dy < maxY && isClear; dy++) {
            // make radius a little wavy
            radius = 5F + MathHelper.sin((dy + yOffset) * rScale) * 2.5F;

            // find center of stalk
            float cx = x + MathHelper.sin((dy + yOffset) * cScale) * radius;
            float cz = z + MathHelper.cos((dy + yOffset) * cScale) * radius;

            float stalkThickness = 2.5F;

            // reduce thickness near top
            if (maxY - dy < 5) {
                stalkThickness *= (maxY - dy) / 5F;
            }

            int minX = MathHelper.floor_float(x - radius - stalkThickness);
            int maxX = MathHelper.ceiling_float_int(x + radius + stalkThickness);
            int minZ = MathHelper.floor_float(z - radius - stalkThickness);
            int maxZ = MathHelper.ceiling_float_int(z + radius + stalkThickness);

            // generate stalk
            for (int dx = minX; dx < maxX; dx++) {
                for (int dz = minZ; dz < maxZ; dz++) {
                    if ((dx - cx) * (dx - cx) + (dz - cz) * (dz - cz) < stalkThickness * stalkThickness) {
                        isClear &= this.tryToPlaceStalk(world, dx, dy, dz);
                    }
                }
            }

            // leaves?
            if (dy == nextLeafY) {
                // make leaf blob

                int lx = (int) (x + MathHelper.sin((dy + yOffset) * cScale) * (radius + stalkThickness));
                int lz = (int) (z + MathHelper.cos((dy + yOffset) * cScale) * (radius + stalkThickness));

                this.placeLeaves(world, lx, dy, lz);

                nextLeafY = dy + 5 + world.rand.nextInt(10);
            }
        }
    }

    private void placeLeaves(World world, int x, int y, int z) {
        // stalk at center
        TFWorldUtil.setBlock(world, x, y, z, TFBlocks.hugeStalk);

        // small squares
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                this.tryToPlaceLeaves(world, x + dx, y - 1, z + dz);
                this.tryToPlaceLeaves(world, x + dx, y + 1, z + dz);
            }
        }
        // larger square
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (!((dx == 2 || dx == -2) && (dz == 2 || dz == -2))) {
                    this.tryToPlaceLeaves(world, x + dx, y + 0, z + dz);
                }
            }
        }
    }

    /**
     * Place the stalk block only if the destination is clear. Return false if blocked.
     */
    private boolean tryToPlaceStalk(World world, int x, int y, int z) {
        Block blockThere = world.getBlock(x, y, z);
        if (blockThere == null || TFBlockUtil.isReplaceable(world, x, y, z) || TFBlockUtil.canBeReplacedByLeaves(world, x, y, z)
            || TFBlockUtil.isLeaves(world, x, y, z) || TFBlockUtil.canSustainLeaves(world, x, y, z)) {
            TFWorldUtil.setBlock(world, x, y, z, TFBlocks.hugeStalk);
            return true;
        } else {
            return false;
        }
    }

    private void tryToPlaceLeaves(World world, int x, int y, int z) {
        Block blockThere = world.getBlock(x, y, z);
        if (blockThere == null || TFBlockUtil.canBeReplacedByLeaves(world, x, y, z)) {
            TFWorldUtil.setBlock(world, x, y, z, TFBlocks.leaves3, 1, 2);
        }
    }
}
