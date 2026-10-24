package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFUnderBrick extends Block {

	private static Icon[] iconSide = new Icon[4];

	public BlockTFUnderBrick(int id) {
		super(id, Material.stone, new BlockConstants());
		// balance B3: soft structure stone, hardness = MITE stone
		this.setHardness(2.4F);
		this.setResistance(10.0F);
		this.setStepSound(soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFUnderBrick.iconSide[0] = par1IconRegister.registerIcon(TwilightForestMod.ID + ":knightbrick");
		BlockTFUnderBrick.iconSide[1] = par1IconRegister.registerIcon(TwilightForestMod.ID + ":knightbrick_mossy");
		BlockTFUnderBrick.iconSide[2] = par1IconRegister.registerIcon(TwilightForestMod.ID + ":knightbrick_cracked");
	}

	@Override
	public Icon getIcon(int side, int meta) {
		if (meta >= 0 && meta < BlockTFUnderBrick.iconSide.length) {
			return BlockTFUnderBrick.iconSide[meta];
		} else {
			return BlockTFUnderBrick.iconSide[0];
		}
	}

	// metadata 0..2 = normal / mossy / cracked; the item subtype equals the metadata
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 3;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}
}
