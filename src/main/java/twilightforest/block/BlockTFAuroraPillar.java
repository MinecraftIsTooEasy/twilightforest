package twilightforest.block;

import java.awt.Color;

import net.minecraft.BlockConstants;
import net.minecraft.BlockHardness;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.CreativeTabs;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFAuroraPillar extends BlockRotatedPillar {

	private Icon sideIcon;
	private Icon topIcon;

	public BlockTFAuroraPillar(int id) {
		super(id, Material.stone);
		this.setCreativeTab(CreativeTabs.tabBlock);
		// MITE_BALANCE B3: hardness 2.4, copper pick, resistance 10
		this.setHardness(BlockHardness.stone);
		this.setResistance(10.0F);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	/** Only the axis bits (0 = up/down, 4 = east/west, 8 = north/south) are used. */
	@Override
	public boolean isValidMetadata(int meta) {
		return meta == 0 || meta == 4 || meta == 8;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int red = 0;
		int green = 0;
		int blue = 0;

		int normalColor = TFBlocks.auroraBlock.colorMultiplier(world, x, y, z);

		red = (normalColor >> 16) & 255;
		blue = normalColor & 255;
		green = (normalColor >> 8) & 255;

		float[] hsb = Color.RGBtoHSB(red, blue, green, null);

		return Color.HSBtoRGB(hsb[0], hsb[1] * 0.5F, Math.min(hsb[2] + 0.4F, 0.9F));
	}

	@Override
	public int getBlockColor() {
		return this.colorMultiplier(null, 16, 0, 16);
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		return this.getBlockColor();
	}

	@Override
	protected Icon getSideIcon(int meta) {
		return this.sideIcon;
	}

	@Override
	protected Icon getEndIcon(int meta) {
		return this.topIcon;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.sideIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":aurora_pillar_side");
		this.topIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":aurora_pillar_top");
		this.blockIcon = this.topIcon;
	}

}
