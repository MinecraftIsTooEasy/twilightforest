package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemBlock;
import net.minecraft.RaycastCollision;

/**
 * Placed on top of a still water block, like MITE's ItemLilyPad (the Forge BlockSnapshot/place event is gone).
 */
public class ItemBlockTFHugeWaterLily extends ItemBlock {

	public ItemBlockTFHugeWaterLily(Block block) {
		super(block);
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
		RaycastCollision rc = player.getSelectedObject(partial_tick, true);
		if (rc == null || !rc.isBlock()) {
			return false;
		}
		return rc.face_hit.isTop() && rc.isBlockHitFullWaterBlock(false) && player.tryPlaceHeldItemAsBlock(rc, this.getBlock());
	}

}
