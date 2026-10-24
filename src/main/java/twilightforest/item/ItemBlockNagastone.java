package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.Icon;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import twilightforest.block.TFBlocks;

public class ItemBlockNagastone extends ItemBlock {

	public ItemBlockNagastone(Block block) {
		super(block);
		// 1.7.10 setHasSubtypes/setMaxDamage removed: MITE derives subtypes from Block.isValidMetadata/getBlockSubtypeUnchecked
	}

	@Override
	public Icon getIconFromSubtype(int i) {
		int j = MathHelper.clamp_int(i, 0, 15);
		return TFBlocks.nagastone.getIcon(2, j);
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
}
