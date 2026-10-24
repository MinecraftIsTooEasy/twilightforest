package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockHardness;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFBlockUtil;

/**
 * Twilight oak / canopy / mangrove / darkwood log. Metadata: bits 1-2 = wood type, bit 4 = aligned west-east,
 * bit 8 = aligned north-south, 12 = bark on all sides. (BlockLog is final in MITE, so this extends BlockRotatedPillar.)
 */
public class BlockTFLog extends BlockRotatedPillar {

	public static Icon sprOakSide;
	public static Icon sprOakTop;
	public static Icon sprCanopySide;
	public static Icon sprCanopyTop;
	public static Icon sprMangroveSide;
	public static Icon sprMangroveTop;
	public static Icon sprDarkwoodSide;
	public static Icon sprDarkwoodTop;

	public BlockTFLog(int id) {
		super(id, Material.wood);
		this.modifyMinHarvestLevel(1);
		this.setHardness(BlockHardness.log);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(net.minecraft.CreativeTabs.tabBlock);
	}

	/** From the specified side/orientation retrieves the texture; BlockRotatedPillar.getIcon picks side vs end. */
	@Override
	protected Icon getSideIcon(int woodType) {
		switch (woodType & 3) {
		default:
		case 0:
			return sprOakSide;
		case 1:
			return sprCanopySide;
		case 2:
			return sprMangroveSide;
		case 3:
			return sprDarkwoodSide;
		}
	}

	@Override
	protected Icon getEndIcon(int woodType) {
		switch (woodType & 3) {
		default:
		case 0:
			return sprOakTop;
		case 1:
			return sprCanopyTop;
		case 2:
			return sprMangroveTop;
		case 3:
			return sprDarkwoodTop;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFLog.sprOakSide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":oak_side");
		BlockTFLog.sprOakTop = par1IconRegister.registerIcon(TwilightForestMod.ID + ":oak_top");
		BlockTFLog.sprCanopySide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":canopy_side");
		BlockTFLog.sprCanopyTop = par1IconRegister.registerIcon(TwilightForestMod.ID + ":canopy_top");
		BlockTFLog.sprMangroveSide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mangrove_side");
		BlockTFLog.sprMangroveTop = par1IconRegister.registerIcon(TwilightForestMod.ID + ":mangrove_top");
		BlockTFLog.sprDarkwoodSide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":darkwood_side");
		BlockTFLog.sprDarkwoodTop = par1IconRegister.registerIcon(TwilightForestMod.ID + ":darkwood_top");
		this.blockIcon = sprOakSide;
	}

	/** Breaking a log makes leaves within 4 blocks check for decay (vanilla BlockLog.breakBlock, Forge beginLeavesDecay). */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		byte range = 4;
		int check = range + 1;
		if (world.checkChunksExist(x - check, y - check, z - check, x + check, y + check, z + check)) {
			for (int dx = -range; dx <= range; ++dx) {
				for (int dy = -range; dy <= range; ++dy) {
					for (int dz = -range; dz <= range; ++dz) {
						TFBlockUtil.beginLeavesDecay(world, x + dx, y + dy, z + dz);
					}
				}
			}
		}
	}

	@Override
	public String getMetadataNotes() {
		return "Bits 1 and 2 used for wood type, bit 4 set if aligned WE, bit 8 set if aligned NS, 12 = bark on all sides";
	}

	/** BlockRotatedPillar only allows 0; TF uses every value 0-15 (12 = full bark). */
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata & 3;
	}
}
