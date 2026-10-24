package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFCastleUnlock extends Block {

	protected BlockTFCastleUnlock(int id) {
		super(id, Material.stone, new BlockConstants());
		// empty shell in TF 1.7.10; MITE_BALANCE B5 lists the castle rune blocks as unbreakable
		this.setBlockUnbreakable();
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setTextureName(TwilightForestMod.ID + ":castleblock_brick");
	}

}
