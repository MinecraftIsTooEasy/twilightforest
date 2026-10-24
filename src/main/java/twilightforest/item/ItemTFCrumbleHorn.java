package twilightforest.item;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.block.TFBlocks;
import twilightforest.compat.ITFUsingTickItem;
import twilightforest.compat.TFBlockUtil;

/**
 * Crumble horn. MITE_BALANCE 7 / 17.18: 256 uses (was 1024); only affects TF structure blocks (mazestone, stone bricks)
 * and gravel; the "stone -> cobblestone -> gravel" steps and the dirt harvesting were removed.
 */
public class ItemTFCrumbleHorn extends ItemTF implements IDamageableItem, ITFUsingTickItem {

    private static final int CHANCE_HARVEST = 20;
    private static final int CHANCE_CRUMBLE = 5;

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.CRUMBLE_HORN) */
    public ItemTFCrumbleHorn(int id) {
        super(id, Material.bone);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
        this.setMaxDamage(256);
    }

    @Override
    public int getNumComponentsForDurability() {
        return 2;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        player.setHeldItemInUse();

        player.worldObj.playSoundAtEntity(player, "mob.sheep.say", 1.0F, 0.8F);

        return true;
    }

    /**
     * Called each tick while the horn is in use, from TFEvents.onPlayerTick (MITE has no Forge onUsingTick hook); server side only.
     * @param count the remaining use ticks, already decremented for this tick
     */
    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (player.worldObj.isRemote) {
            return;
        }
        // count is already decremented for this tick (MITE decrements before the tick hook): count + 1 is what 1.7.10 passed
        int forgeCount = count + 1;
        if (forgeCount > 10 && forgeCount % 5 == 0) {
            int crumbled = doCrumble(player.worldObj, player);

            if (crumbled > 0) {
                player.tryDamageHeldItem(DamageSource.generic, crumbled);
            }

            player.worldObj.playSoundAtEntity(player, "mob.sheep.say", 1.0F, 0.8F);
        }
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BOW;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * Bleeeat!
     */
    private int doCrumble(World world, EntityPlayer player) {
        double range = 3.0D;
        double radius = 2.0D;
        Vec3 srcVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 lookVec = player.getLookVec();
        Vec3 destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

        AxisAlignedBB crumbleBox = AxisAlignedBB.getBoundingBox(destVec.xCoord - radius, destVec.yCoord - radius, destVec.zCoord - radius, destVec.xCoord + radius, destVec.yCoord + radius, destVec.zCoord + radius);

        return crumbleBlocksInAABB(world, player, crumbleBox);
    }

    /**
     * Crumble block in the box
     */
    private int crumbleBlocksInAABB(World world, EntityPlayer player, AxisAlignedBB box) {
        int minX = MathHelper.floor_double(box.minX);
        int minY = MathHelper.floor_double(box.minY);
        int minZ = MathHelper.floor_double(box.minZ);
        int maxX = MathHelper.floor_double(box.maxX);
        int maxY = MathHelper.floor_double(box.maxY);
        int maxZ = MathHelper.floor_double(box.maxZ);

        int crumbled = 0;

        for (int dx = minX; dx <= maxX; ++dx) {
            for (int dy = minY; dy <= maxY; ++dy) {
                for (int dz = minZ; dz <= maxZ; ++dz) {
                    crumbled += crumbleBlock(world, player, dx, dy, dz);
                }
            }
        }

        return crumbled;
    }

    /**
     * Crumble a specific block.
     */
    private int crumbleBlock(World world, EntityPlayer player, int dx, int dy, int dz) {
        int cost = 0;

        Block current = world.getBlock(dx, dy, dz); // null for air

        if (current != null) {
            int currentMeta = world.getBlockMetadata(dx, dy, dz);
            int sfxData = current.blockID + (currentMeta << 12);

            // MITE_BALANCE: no stone -> cobblestone, no cobblestone -> gravel

            if (current == Block.stoneBrick && currentMeta == 0 && world.rand.nextInt(CHANCE_CRUMBLE) == 0) {
                world.setBlock(dx, dy, dz, Block.stoneBrick.blockID, 2, 3);
                world.playAuxSFX(2001, dx, dy, dz, sfxData);
                cost++;
            }

            if (current == TFBlocks.mazestone && currentMeta == 1 && world.rand.nextInt(CHANCE_CRUMBLE) == 0) {
                world.setBlock(dx, dy, dz, TFBlocks.mazestone.blockID, 4, 3);
                world.playAuxSFX(2001, dx, dy, dz, sfxData);
                cost++;
            }

            // MITE_BALANCE: dirt is no longer harvested; gravel only when the player could harvest it
            if (current == Block.gravel) {
                if (TFBlockUtil.canPlayerHarvest(player, world, dx, dy, dz) && world.rand.nextInt(CHANCE_HARVEST) == 0) {
                    // destroyBlock plays the 2001 break effect itself
                    TFBlockUtil.harvestBlockAs(player, world, dx, dy, dz);
                    cost++;
                }
            }
        }

        return cost;
    }
}
