package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.Icon;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import twilightforest.block.TFBlocks;

public class ItemBlockTFLeaves extends ItemBlock {

	public ItemBlockTFLeaves(Block par1) {
		super(par1);
	}

	@Override
	public Icon getIconFromSubtype(int par1) {
		return TFBlocks.leaves.getIcon(2, par1);
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
