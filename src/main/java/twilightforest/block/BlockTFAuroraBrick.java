package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.BlockHardness;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EnumFace;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class BlockTFAuroraBrick extends Block {

	private static Icon[] icons;

	public BlockTFAuroraBrick(int id) {
		super(id, Material.stone, new BlockConstants());

		this.setCreativeTab(CreativeTabs.tabBlock);
		// MITE_BALANCE B3: hardness 2.4 (= MITE stone), copper pick (stone material min harvest level 2), resistance 10
		this.setHardness(BlockHardness.stone);
		this.setResistance(10.0F);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	/** Metadata 0-15 is the colour/animation position; all of it drops as a single item subtype. */
	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	@Override
	public String getMetadataNotes() {
		return "0-15 = animation offset, derived from (x + z) % 16 when placed";
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFAuroraBrick.icons = new Icon[8];
		for (int i = 0; i < 8; i++) {
			BlockTFAuroraBrick.icons[i] = par1IconRegister.registerIcon(TwilightForestMod.ID + ":aurorabrick" + i);
		}
		this.blockIcon = BlockTFAuroraBrick.icons[0];
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (meta < 8) {
			return icons[meta];
		} else {
			return icons[15 - meta];
		}
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z) {
		int red = 0;
		int green = 0;
		int blue = 0;

		// aurora fade
		red = 16;

		blue = x * 12 + z * 6;
		if ((blue & 256) != 0) {
			blue = 255 - (blue & 255);
		}
		blue ^= 255;
		blue &= 255;

		green = x * 4 + z * 8;
		if ((green & 256) != 0) {
			green = 255 - (green & 255);
		}
		green &= 255;

		// don't let things get black
		if (green + blue < 128) {
			green = 128 - blue;
		}

		return red << 16 | blue << 8 | green;
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

	/**
	 * Metadata for a block placed from its item (replaces 1.7.10 onBlockPlaced).
	 */
	@Override
	public int getMetadataForPlacement(World world, int x, int y, int z, ItemStack itemStack, Entity entity, EnumFace face, float offsetX, float offsetY, float offsetZ) {
		return Math.abs(x + z) % 16;
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.setBlockMetadataWithNotify(x, y, z, Math.abs(x + z) % 16, 2);
	}
}
