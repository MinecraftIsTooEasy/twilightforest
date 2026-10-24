package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * 
 * Castle block makes a castle
 * 
 * @author Ben
 *
 */
public class BlockTFCastleMagic extends Block {

	public static Icon[] magicIcons = new Icon[8];
	public static int[] magicColors = new int[] { 0x00FFFF, 0xFFFF00, 0xFF00FF, 0x4B0082 };

	/**
	 * MITE_BALANCE B5: castle rune / magic blocks are unbreakable (hardness -1, level 100, resistance 6,000,000).
	 */
	public BlockTFCastleMagic(int id) {
		super(id, Material.stone, new BlockConstants());
		this.setBlockUnbreakable();
		this.setStepSound(Block.soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta < 4;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta;
	}

	@Override
	public void registerIcons(IconRegister register) {
		this.blockIcon = register.registerIcon(TwilightForestMod.ID + ":castleblock_brick");
		for (int i = 0; i < 8; i++) {
			magicIcons[i] = register.registerIcon(TwilightForestMod.ID + ":castleblock_magic_" + i);
		}
	}

	public static Icon getMagicIconFor(int x, int y, int z) {

		long seed = x * 3129871 ^ y * 116129781L ^ z;
		seed = seed * seed * 42317861L + seed * 11L;

		int index = (int) (seed >> 12 & 7L);

		return magicIcons[index];
	}

	public static int getMagicColorFor(int meta) {
		int color = magicColors[meta & 3];

		if ((meta & 8) != 0) {
			color = 0xFFFFFF ^ color;
		}

		return color;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getCastleMagicBlockRenderID();
	}
}
