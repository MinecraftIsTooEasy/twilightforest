package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Mazestone mimics other types of stone in appearance, but is much harder to mine
 *
 * @author Ben
 */
public class BlockTFMazestone extends Block {

	private static Icon TEX_PLAIN;
	private static Icon TEX_BRICK;
	private static Icon TEX_PILLAR;
	private static Icon TEX_DECO;
	private static Icon TEX_CRACKED;
	private static Icon TEX_MOSSY;
	private static Icon TEX_MOSAIC;
	private static Icon TEX_BORDER;

	public BlockTFMazestone(int id) {
		super(id, Material.stone, new BlockConstants());
		// balance B4a: hardness 4.0 (1.7.10: 100), harvest level 2 (from Material.stone, copper pick and up);
		// the Mazebreaker pick mines it x4 faster with x0.25 wear (see ItemTFMazebreakerPick).
		// The 1.7.10 harvestBlock "extra 16 pick damage" is dropped: MITE tool decay (block hardness based) replaces it.
		this.setHardness(4.0F);
		this.setResistance(15.0F);
		this.setStepSound(soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public Icon getIcon(int side, int meta) {
		switch (meta) {
		case 0:
		default:
			return TEX_PLAIN;
		case 1:
			return TEX_BRICK;
		case 2:
			return side > 1 ? TEX_PILLAR : TEX_PLAIN;
		case 3:
			return side > 1 ? TEX_DECO : TEX_BRICK;
		case 4:
			return TEX_CRACKED;
		case 5:
			return TEX_MOSSY;
		case 6:
			return side > 1 ? TEX_BRICK : TEX_MOSAIC;
		case 7:
			return side > 1 ? TEX_BRICK : TEX_BORDER;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFMazestone.TEX_PLAIN = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_plain");
		BlockTFMazestone.TEX_BRICK = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_brick");
		BlockTFMazestone.TEX_PILLAR = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_pillar");
		BlockTFMazestone.TEX_DECO = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_decorative");
		BlockTFMazestone.TEX_CRACKED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_cracked");
		BlockTFMazestone.TEX_MOSSY = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_mossy");
		BlockTFMazestone.TEX_MOSAIC = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_mosaic");
		BlockTFMazestone.TEX_BORDER = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mazestone_border");
	}

	// metadata 0..7 are the eight variants; the item subtype equals the metadata (1.7.10 damageDropped)
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 8;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}
}
