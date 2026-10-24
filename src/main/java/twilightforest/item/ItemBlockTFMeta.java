package twilightforest.item;

import java.util.List;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.Slot;

public class ItemBlockTFMeta extends ItemBlock {

	private final Block myBlock;

	public ItemBlockTFMeta(Block block) {
		super(block);
		this.myBlock = block;
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return super.getUnlocalizedName();
		}
		return super.getUnlocalizedName() + "." + itemstack.getItemSubtype();
	}

	@Override
	public Icon getIconFromSubtype(int par1) {
		return this.myBlock.getIcon(2, par1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
		super.addInformation(stack, player, info, extended, slot);

		if (stack == null || !stack.hasDisplayName()) {
			return;
		}
		String name = stack.getDisplayName();

		// add warning for [WIP] tag
		if (name.contains("[WIP]")) {
			info.add("This block is a work in progress");
			info.add("and may have bugs or unintended");
			info.add("effects that may damage your world.");
			info.add("Use with caution.");
		}
		// add warning for [NYI] tag
		if (name.contains("[NYI]")) {
			info.add("This block has effects");
			info.add("that are not yet implemented.");
		}
	}
}
