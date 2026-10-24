package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFDeadrock extends Block {

	public static final String[] names = new String[] {"surface", "cracked", "solid"};
	private Icon[] icons;

	public BlockTFDeadrock(int id) {
		// 1.7.10 Material.rock -> Material.stone (MITE tools only know vanilla materials)
		super(id, Material.stone, new BlockConstants());
		// balance (MITE_BALANCE B5): unbreakable, opened by mechanics only (hardness -1, harvest level 100)
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
		this.setStepSound(soundStoneFootstep);
		this.disableStats();
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.icons = new Icon[names.length];

		for (int i = 0; i < names.length; i++) {
			this.icons[i] = iconRegister.registerIcon(TwilightForestMod.ID + ":deadrock_" + names[i]);
		}
	}

	@Override
	public Icon getIcon(int side, int meta) {
		if (meta < 0 || meta >= names.length) {
			meta = 0;
		}

		return this.icons[meta];
	}

	// metadata 0..2 are the three variants; the item subtype equals the metadata (1.7.10 damageDropped)
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 3;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}
}
