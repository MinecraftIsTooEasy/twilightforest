package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.Icon;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import twilightforest.block.TFBlocks;

public class ItemBlockTFLog extends ItemBlock {

	public static final String woodNames[] = {
		"oak", "canopy", "mangrove", "darkwood", "x", "root", "oreroot", "rotten"
	};

	public ItemBlockTFLog(Block log) {
		super(log);
	}

	@Override
	public Icon getIconFromSubtype(int par1) {
		return TFBlocks.log.getIcon(2, par1);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return super.getUnlocalizedName();
		}
		int meta = itemstack.getItemSubtype();
		if ((meta & 8) == 0) {
			// wood
			int i = MathHelper.clamp_int(meta, 0, 7);
			return super.getUnlocalizedName() + "." + woodNames[i];
		} else {
			// log
			meta &= 7;
			int i = MathHelper.clamp_int(meta, 0, 7);
			return super.getUnlocalizedName() + "." + woodNames[i] + ".log";
		}
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

}
