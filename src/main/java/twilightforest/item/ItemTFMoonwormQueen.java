package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.IDamageableItem;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;
import twilightforest.entity.EntityTFMoonwormShot;

/**
 * Moonworm Queen. MITE_BALANCE 7: 128 uses (was 256); recharged with torchberries (recipe in TFRecipes).
 */
public class ItemTFMoonwormQueen extends ItemTF implements IDamageableItem {

    private static final int FIRING_TIME = 12;

    private Icon[] icons;
    private final String[] iconNames = new String[] { "moonwormQueen", "moonwormQueenAlt" };

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.MOONWORM_QUEEN) */
    protected ItemTFMoonwormQueen(int id) {
        super(id, Material.bone);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
        this.setMaxDamage(128);
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
     * 1.7.10 onItemUse (place a moonworm on the clicked block) + onItemRightClick (start charging a shot).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) {
            return false;
        }

        // don't wear item out, leave it at 0 uses left so that it can be recharged
        boolean empty = stack.getItemDamage() + 1 >= stack.getMaxDamage();

        RaycastCollision rc = player.getSelectedObject(partialTick, false);
        if (!empty && rc != null && rc.isBlock() && rc.getBlockHit() != TFBlocks.moonworm) {
            Block moonworm = TFBlocks.moonworm;
            // MITE block placement (does not consume the held item, unlike EntityPlayer.tryPlaceHeldItemAsBlock)
            moonworm.is_being_placed = true;
            boolean placed = this.tryPlaceAsBlock(rc, moonworm, player, stack);
            moonworm.is_being_placed = false;
            if (placed) {
                if (player.onServer()) {
                    if (!player.inCreativeMode()) {
                        stack.tryDamageItem(player.worldObj, 1, true);
                    }
                    player.stopUsingItem();
                } else {
                    player.swingArm();
                }
                return true;
            }
        }

        if (!empty) {
            return player.setHeldItemInUse();
        }
        player.stopUsingItem();
        return false;
    }

    public String getSound() {
        return "mob.slime.big";
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int useRemaining) {
        int useTime = this.getMaxItemUseDuration(stack) - useRemaining;

        if (!world.isRemote && useTime > FIRING_TIME && (stack.getItemDamage() + 1) < stack.getMaxDamage()) {
            boolean fired = world.spawnEntityInWorld(new EntityTFMoonwormShot(world, player));

            if (fired) {
                if (!player.inCreativeMode()) {
                    stack.tryDamageItem(world, 2, true);
                }

                world.playSoundAtEntity(player, this.getSound(), 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void registerIcons(IconRegister register) {
        super.registerIcons(register);
        this.icons = new Icon[iconNames.length];

        for (int i = 0; i < this.iconNames.length; ++i) {
            this.icons[i] = register.registerIcon(TwilightForestMod.ID + ":" + iconNames[i]);
        }
        this.itemIcon = this.icons[0];
    }

    // TODO(mite-port): the 1.7.10 charged-state icon swap (moonwormQueenAlt while charging, Item.getIcon(stack, pass, player, usingItem, useRemaining)) has no MITE hook; Item.getIconIndex is final and EntityPlayer.getItemIcon only special-cases bows. Needs a client mixin.

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
