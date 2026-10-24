package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockHardness;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFBlockUtil;

/**
 * Magic tree logs (time, transformation, mine, sort). Metadata: bits 1+2 = tree type, bit 4 = aligned east-west,
 * bit 8 = aligned north-south, both (12) = bark on all sides (used as the "switched off" state by the special logs).
 */
public class BlockTFMagicLog extends BlockRotatedPillar {

	public static final int META_TIME = 0;
	public static final int META_TRANS = 1;
	public static final int META_MINE = 2;
	public static final int META_SORT = 3;

	public static Icon SPR_TIMESIDE;
	public static Icon SPR_TIMETOP;
	public static Icon SPR_TIMECLOCK;
	public static Icon SPR_TIMECLOCKOFF;
	public static Icon SPR_TRANSSIDE;
	public static Icon SPR_TRANSTOP;
	public static Icon SPR_TRANSHEART;
	public static Icon SPR_TRANSHEARTOFF;
	public static Icon SPR_MINESIDE;
	public static Icon SPR_MINETOP;
	public static Icon SPR_MINEGEM;
	public static Icon SPR_MINEGEMOFF;
	public static Icon SPR_SORTSIDE;
	public static Icon SPR_SORTTOP;
	public static Icon SPR_SORTEYE;
	public static Icon SPR_SORTEYEOFF;

	protected BlockTFMagicLog(int id) {
		super(id, Material.wood);
		this.modifyMinHarvestLevel(1); // same as MITE's BlockLog: no chopping with bare hands
		this.setHardness(BlockHardness.log);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/** Unlike MITE's BlockLog, metadata 12 (bark on every side) is valid here. */
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata & 3;
	}

	@Override
	public String getMetadataNotes() {
		return "Bits 1 and 2 used for subtype, bit 4 set if aligned WE, bit 8 set if aligned NS, both set = all bark / switched off";
	}

	@Override
	protected Icon getSideIcon(int woodType) {
		switch (woodType & 3) {
		default:
		case META_TIME:
			return SPR_TIMESIDE;
		case META_TRANS:
			return SPR_TRANSSIDE;
		case META_MINE:
			return SPR_MINESIDE;
		case META_SORT:
			return SPR_SORTSIDE;
		}
	}

	@Override
	protected Icon getEndIcon(int woodType) {
		switch (woodType & 3) {
		default:
		case META_TIME:
			return SPR_TIMETOP;
		case META_TRANS:
			return SPR_TRANSTOP;
		case META_MINE:
			return SPR_MINETOP;
		case META_SORT:
			return SPR_SORTTOP;
		}
	}

	@Override
	public void registerIcons(IconRegister reg) {
		BlockTFMagicLog.SPR_TIMESIDE = reg.registerIcon(TwilightForestMod.ID + ":time_side");
		BlockTFMagicLog.SPR_TIMETOP = reg.registerIcon(TwilightForestMod.ID + ":time_section");
		BlockTFMagicLog.SPR_TIMECLOCK = reg.registerIcon(TwilightForestMod.ID + ":time_clock");
		BlockTFMagicLog.SPR_TIMECLOCKOFF = reg.registerIcon(TwilightForestMod.ID + ":time_clock_off");
		BlockTFMagicLog.SPR_TRANSSIDE = reg.registerIcon(TwilightForestMod.ID + ":trans_side");
		BlockTFMagicLog.SPR_TRANSTOP = reg.registerIcon(TwilightForestMod.ID + ":trans_section");
		BlockTFMagicLog.SPR_TRANSHEART = reg.registerIcon(TwilightForestMod.ID + ":trans_heart");
		BlockTFMagicLog.SPR_TRANSHEARTOFF = reg.registerIcon(TwilightForestMod.ID + ":trans_heart_off");
		BlockTFMagicLog.SPR_MINESIDE = reg.registerIcon(TwilightForestMod.ID + ":mine_side");
		BlockTFMagicLog.SPR_MINETOP = reg.registerIcon(TwilightForestMod.ID + ":mine_section");
		BlockTFMagicLog.SPR_MINEGEM = reg.registerIcon(TwilightForestMod.ID + ":mine_gem");
		BlockTFMagicLog.SPR_MINEGEMOFF = reg.registerIcon(TwilightForestMod.ID + ":mine_gem_off");
		BlockTFMagicLog.SPR_SORTSIDE = reg.registerIcon(TwilightForestMod.ID + ":sort_side");
		BlockTFMagicLog.SPR_SORTTOP = reg.registerIcon(TwilightForestMod.ID + ":sort_section");
		BlockTFMagicLog.SPR_SORTEYE = reg.registerIcon(TwilightForestMod.ID + ":sort_eye");
		BlockTFMagicLog.SPR_SORTEYEOFF = reg.registerIcon(TwilightForestMod.ID + ":sort_eye_off");
	}

	/** Like BlockLog.breakBlock: flag leaves within 4 blocks so they re-check their support. */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int metadata) {
		if (world.decorating) {
			return;
		}
		byte range = 4;
		int check = range + 1;
		if (world.checkChunksExist(x - check, y - check, z - check, x + check, y + check, z + check)) {
			for (int dx = -range; dx <= range; dx++) {
				for (int dy = -range; dy <= range; dy++) {
					for (int dz = -range; dz <= range; dz++) {
						TFBlockUtil.beginLeavesDecay(world, x + dx, y + dy, z + dz);
					}
				}
			}
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		// intentionally empty, as in 1.7.10 (no particles)
	}
}
