package twilightforest.item;

import net.minecraft.BehaviorDefaultDispenseItem;
import net.minecraft.BlockDispenser;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EnumFacing;
import net.minecraft.IBlockSource;
import net.minecraft.ItemStack;

public class BehaviorTFMobEggDispense extends BehaviorDefaultDispenseItem {

    // 1.7.10 took a MinecraftServer reference that it never used; dropped.
    public BehaviorTFMobEggDispense() {
    }

    /**
     * Dispense the specified stack, play the dispense sound and spawn particles.
     */
    @Override
    public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
        EnumFacing facing = BlockDispenser.getFacing(source.getBlockMetadata());
        double x = source.getX() + (double) facing.getFrontOffsetX();
        double y = (double) ((float) source.getYInt() + 0.2F);
        double z = source.getZ() + (double) facing.getFrontOffsetZ();
        Entity entity = ItemTFSpawnEgg.spawnCreature(source.getWorld(), stack.getItemSubtype(), x, y, z);
        if (entity instanceof EntityLiving && stack.hasDisplayName()) {
            ((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
        }
        stack.splitStack(1);
        return stack;
    }
}
