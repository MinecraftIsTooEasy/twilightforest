package twilightforest.item;

import java.util.List;

import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.EnumRarity;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.Slot;
import net.minecraft.World;
import twilightforest.compat.ITFUsingTickItem;
import twilightforest.entity.EntityTFTwilightWandBolt;

public class ItemTFTwilightWand extends ItemTF implements IDamageableItem, ITFUsingTickItem {

    /** Ticks between two bolts while the wand is charged (MITE_BALANCE 7.5: 10). */
    private static final int BOLT_INTERVAL = 10;

    protected ItemTFTwilightWand(int id) {
        super(id);
        this.setMaxStackSize(1);
        // MITE_BALANCE 7.5: 48 charges (original 99)
        this.setMaxDamage(48);
    }

    @Override
    public int getNumComponentsForDurability() {
        return 0;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) {
            return false;
        }
        if (stack.getItemDamage() < stack.getMaxDamage()) {
            player.setHeldItemInUse();
            // first bolt right away on the server; the following ones come from onUsingTick every BOLT_INTERVAL ticks
            if (player.onServer()) {
                this.fireBolt(stack, player);
            }
            return true;
        }
        player.stopUsingItem();
        return false;
    }

    /**
     * Called each tick while the wand is in use, from TFEvents.onPlayerTick (MITE has no Forge onUsingTick hook).
     * @param count Remaining use ticks (already decremented for this tick)
     */
    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (stack.getItemDamage() >= stack.getMaxDamage()) {
            // do not use
            player.stopUsingItem();
            return;
        }

        // ticks since the use started: the first bolt was fired by onItemRightClick, then one every BOLT_INTERVAL ticks
        int elapsed = this.getMaxItemUseDuration(stack) - count;
        if (elapsed > 0 && elapsed % BOLT_INTERVAL == 0 && player.onServer()) {
            this.fireBolt(stack, player);
        }
    }

    /** Server side: one bolt, one charge (creative mode keeps the charges). */
    private void fireBolt(ItemStack stack, EntityPlayer player) {
        World worldObj = player.worldObj;

        worldObj.playSoundAtEntity(player, "mob.ghast.fireball", 1.0F, (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F + 1.0F);
        worldObj.spawnEntityInWorld(new EntityTFTwilightWandBolt(worldObj, player));

        // not tryDamageItem: the wand must stay in the inventory when empty so that it can be recharged by crafting
        if (!player.inCreativeMode()) {
            stack.setItemDamage(stack.getItemDamage() + 1);
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
        return EnumItemInUseAction.BOW;
    }

    /**
     * Return an item rarity from EnumRarity
     */
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    /**
     * Display charges left in tooltip
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended_info, Slot slot) {
        super.addInformation(stack, player, info, extended_info, slot);
        info.add(net.minecraft.StatCollector.translateToLocalFormatted("tf.tooltip.charges", stack.getMaxDamage() - stack.getItemDamage()));
    }
}
