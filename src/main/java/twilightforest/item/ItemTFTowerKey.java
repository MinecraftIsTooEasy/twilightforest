package twilightforest.item;

import net.minecraft.EntityPlayer;
import net.minecraft.Material;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.block.BlockTFTowerDevice;
import twilightforest.block.TFBlocks;

public class ItemTFTowerKey extends ItemTF {

    protected ItemTFTowerKey(int id) {
        super(id, Material.gold);
    }

    /**
     * Right click on a locked vanishing tower block unlocks it (1.7.10 onItemUse).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        RaycastCollision rc = player.getSelectedObject(partial_tick, false);
        if (rc == null || !rc.isBlock()) {
            return false;
        }
        if (rc.getBlockHit() != TFBlocks.towerDevice || rc.block_hit_metadata != BlockTFTowerDevice.META_VANISH_LOCKED) {
            return false;
        }
        if (player.onServer()) {
            World world = player.worldObj;
            BlockTFTowerDevice.unlockBlock(world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z);
            player.convertOneOfHeldItem(null);
        } else {
            player.swingArm();
        }
        return true;
    }
}
