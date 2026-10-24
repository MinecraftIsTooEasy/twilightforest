package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockHardness;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/** Cinder log (burnt wood, cinder furnace component). Same orientation metadata as BlockTFLog; 12 = corner texture. */
public class BlockTFCinderLog extends BlockRotatedPillar {

	private Icon topIcon;
	private Icon cornerIcon;

	public BlockTFCinderLog(int id) {
		super(id, Material.wood);
		this.modifyMinHarvestLevel(1);
		this.setHardness(BlockHardness.log);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		int orient = meta & 12;

		return orient == 12 ? this.cornerIcon : (orient == 0 && (side == 1 || side == 0) ? this.topIcon : (orient == 4 && (side == 5 || side == 4) ? this.topIcon : (orient == 8 && (side == 2 || side == 3) ? this.topIcon : this.blockIcon)));
	}

	@Override
	protected Icon getSideIcon(int subtype) {
		return this.blockIcon;
	}

	@Override
	protected Icon getEndIcon(int subtype) {
		return this.topIcon;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":cinder_side");
		this.topIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":cinder_top");
		this.cornerIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":cinder_corner");
	}

	@Override
	public String getMetadataNotes() {
		return "Bit 4 set if aligned WE, bit 8 set if aligned NS, 12 = corner texture";
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return 0;
	}
}
