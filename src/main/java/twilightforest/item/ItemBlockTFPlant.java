package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.TFBlocks;

public class ItemBlockTFPlant extends ItemBlock {

	public ItemBlockTFPlant(Block block) {
		super(block);
	}

	@Override
	public Icon getIconFromSubtype(int par1) {
		return TFBlocks.plant.getIcon(2, par1);
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		return TFBlocks.plant.getRenderColor(par1ItemStack.getItemSubtype());
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return super.getUnlocalizedName();
		}
		return super.getUnlocalizedName() + "." + itemstack.getItemSubtype();
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	/**
	 * Hanging plants (root strand, torchberry) may only be placed with a diggable/rooted block above them.
	 * The 1.7.10 onItemUseRoot / func_150936_a special cases are covered by MITE's generic placement plus this check.
	 */
	@Override
	public boolean tryPlaceAsBlock(RaycastCollision rc, Block block, EntityPlayer player, ItemStack item_stack) {
		int meta = item_stack.getItemSubtype();

		if (rc.isBlock() && (meta == BlockTFPlant.META_ROOT_STRAND || meta == BlockTFPlant.META_TORCHBERRY)) {
			int x;
			int y;
			int z;
			int placementMeta = block.getMetadataForPlacement(player.worldObj, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, item_stack, player, rc.face_hit, 0.0F, 0.0F, 0.0F);
			if (block.canReplaceBlock(placementMeta, rc.getBlockHit(), rc.block_hit_metadata)) {
				x = rc.block_hit_x;
				y = rc.block_hit_y;
				z = rc.block_hit_z;
			} else {
				x = rc.neighbor_block_x;
				y = rc.neighbor_block_y;
				z = rc.neighbor_block_z;
			}

			if (!BlockTFPlant.canPlaceRootBelow(player.worldObj, x, y + 1, z)) {
				return false;
			}
		}

		return super.tryPlaceAsBlock(rc, block, player, item_stack);
	}

}
