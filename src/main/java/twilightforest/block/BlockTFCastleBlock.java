package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
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
public class BlockTFCastleBlock extends Block {

	private static Icon brickIcon;
	private static Icon crackIcon;
	private static Icon fadedIcon;
	private static Icon roofIcon;

	/**
	 * Metadata: 0 brick, 1 faded, 2 cracked, 3 roof. MITE_BALANCE B5: stone, hardness 20, needs a mithril pickaxe (level 4).
	 */
	public BlockTFCastleBlock(int id) {
		super(id, Material.stone, new BlockConstants());
		this.setHardness(20F);
		this.setMinHarvestLevel(4);
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

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		switch (meta) {
		case 0:
		default:
			return brickIcon;
		case 1:
			return fadedIcon;
		case 2:
			return crackIcon;
		case 3:
			return roofIcon;
		}
	}

	@Override
	public void registerIcons(IconRegister register) {
		BlockTFCastleBlock.brickIcon = register.registerIcon(TwilightForestMod.ID + ":castleblock_brick");
		BlockTFCastleBlock.fadedIcon = register.registerIcon(TwilightForestMod.ID + ":castleblock_faded");
		BlockTFCastleBlock.crackIcon = register.registerIcon(TwilightForestMod.ID + ":castleblock_cracked");
		BlockTFCastleBlock.roofIcon = register.registerIcon(TwilightForestMod.ID + ":castleblock_roof");
	}

	/**
	 * Always drops itself with its subtype (MITE_BALANCE B5: no cobblestone when exploded).
	 * The 1.7.10 harvestBlock() hack that damaged the pickaxe by 16 extra is dropped: MITE already wears tools by hardness
	 * (20) and the Mazebreaker pick is balanced through its own wear rate.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.wasCrushed()) {
			return 0;
		}
		return this.dropBlockAsEntityItem(info, this.createStackedBlock(info.getMetadata()));
	}
}
