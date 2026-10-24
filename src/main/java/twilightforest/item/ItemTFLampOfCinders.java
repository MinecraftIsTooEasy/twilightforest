package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;

/**
 * Lamp of Cinders. MITE_BALANCE 7: 256 uses (the 1.7.10 original never wore out); every single burn and every
 * released area burn costs 1 use. Burning thorns grants the "troll" achievement.
 */
public class ItemTFLampOfCinders extends ItemTF implements IDamageableItem {

    private static final int FIRING_TIME = 12;

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.LAMP_OF_CINDERS) */
    public ItemTFLampOfCinders(int id) {
        super(id, Material.bone);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
        this.setMaxDamage(256);
    }

    @Override
    public int getNumComponentsForDurability() {
        return 0;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }

    /**
     * 1.7.10 onItemUse (burn the clicked thorns) + onItemRightClick (start charging the area burn).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        RaycastCollision rc = player.getSelectedObject(partialTick, false);
        if (rc != null && rc.isBlock() && rc.getBlockHit() == TFBlocks.thorns) {
            World world = player.worldObj;
            int x = rc.block_hit_x;
            int y = rc.block_hit_y;
            int z = rc.block_hit_z;

            if (player.onServer()) {
                if (burnBlock(world, x, y, z)) {
                    world.playSoundAtEntity(player, this.getSound(), 0.5F, 1.5F);
                    player.tryDamageHeldItem(DamageSource.generic, 1);
                    player.triggerAchievement(TFAchievementPage.twilightProgressTroll);
                }
            } else {
                // spawn flame particles
                for (int i = 0; i < 10; i++) {
                    double dx = x + 0.5F + (itemRand.nextFloat() - itemRand.nextFloat()) * 0.75F;
                    double dy = y + 0.5F + (itemRand.nextFloat() - itemRand.nextFloat()) * 0.75F;
                    double dz = z + 0.5F + (itemRand.nextFloat() - itemRand.nextFloat()) * 0.75F;

                    TFParticles.spawn(world, "smoke", dx, dy, dz, 0.0D, 0.0D, 0.0D);
                    TFParticles.spawn(world, "flame", dx, dy, dz, 0.0D, 0.0D, 0.0D);
                }
                player.swingArm();
            }
            return true;
        }

        return player.setHeldItemInUse();
    }

    private boolean burnBlock(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block != null && block == TFBlocks.thorns) {
            return TFWorldUtil.setBlock(world, x, y, z, TFBlocks.burntThorns, world.getBlockMetadata(x, y, z) & 12, 2);
        }
        return false;
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int useRemaining) {
        int useTime = this.getMaxItemUseDuration(stack) - useRemaining;

        if (useTime > FIRING_TIME) {
            doBurnEffect(world, player);
        }
    }

    private void doBurnEffect(World world, EntityPlayer player) {
        int px = MathHelper.floor_double(player.lastTickPosX);
        int py = MathHelper.floor_double(player.lastTickPosY + player.getEyeHeight());
        int pz = MathHelper.floor_double(player.lastTickPosZ);

        int range = 4;

        if (!world.isRemote) {
            world.playSoundAtEntity(player, this.getSound(), 1.5F, 0.8F);

            // set nearby thorns to burnt
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    for (int dz = -range; dz <= range; dz++) {
                        this.burnBlock(world, px + dx, py + dy, pz + dz);
                    }
                }
            }

            player.tryDamageHeldItem(DamageSource.generic, 1);

            // trigger achievement
            player.triggerAchievement(TFAchievementPage.twilightProgressTroll);
        }

        for (int i = 0; i < 6; i++) {
            int rx = px + itemRand.nextInt(range) - itemRand.nextInt(range);
            int ry = py + itemRand.nextInt(2);
            int rz = pz + itemRand.nextInt(range) - itemRand.nextInt(range);

            world.playAuxSFXAtEntity(player, 2004, rx, ry, rz, 0);
        }
    }

    public String getSound() {
        return "mob.ghast.fireball";
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
}
