package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Explosion;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFTowerTermite;

/**
 * Tower wood is a type of plank block that forms the walls of Dark Towers.
 * <p>
 * Balance (MITE_BALANCE B4b): wood material, hardness 4.0, minimum harvest level 2 (copper axe), explosion resistance 10.
 * The infested variant is soft and releases a tower termite when broken.
 * <p>
 * Fire info (TF getFlammability = 1, getFireSpreadSpeed = 0) is applied by {@code TFBlocks} with
 * {@code Block.fire.setBurnRate(TFIds.TOWER_WOOD, 0, 1)}.
 */
public class BlockTFTowerWood extends Block {

	private static Icon TEX_PLAIN;
	private static Icon TEX_ENCASED;
	private static Icon TEX_CRACKED;
	private static Icon TEX_MOSSY;
	private static Icon TEX_INFESTED;

	public static final int META_INFESTED = 4;

	public BlockTFTowerWood(int id) {
		super(id, Material.wood, new BlockConstants());
		this.setHardness(4.0F);
		this.setMinHarvestLevel(2);
		this.setResistance(10F);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta <= META_INFESTED;
	}

	/** TF damageDropped: the item subtype is the metadata. */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta;
	}

	// ---- appearance ----

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z) {
		int meta = par1IBlockAccess.getBlockMetadata(x, y, z);

		if (meta == 0 || meta == 2 || meta == 3 || meta == META_INFESTED) {
			// stripes!
			int value = x * 31 + y * 15 + z * 33;
			if ((value & 256) != 0) {
				value = 255 - (value & 255);
			}
			value &= 255;
			value = value >> 1;
			value |= 128;

			return value << 16 | value << 8 | value;
		} else {
			return 16777215;
		}
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		switch (meta) {
		case 0:
		default:
			return TEX_PLAIN;
		case 1:
			return TEX_ENCASED;
		case 2:
			return TEX_CRACKED;
		case 3:
			return TEX_MOSSY;
		case META_INFESTED:
			return TEX_INFESTED;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFTowerWood.TEX_PLAIN = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerwood_planks");
		BlockTFTowerWood.TEX_ENCASED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerwood_encased");
		BlockTFTowerWood.TEX_CRACKED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerwood_cracked");
		BlockTFTowerWood.TEX_MOSSY = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerwood_mossy");
		BlockTFTowerWood.TEX_INFESTED = par1IconRegister.registerIcon(TwilightForestMod.ID + ":towerwood_infested");
	}

	// ---- hardness / harvesting ----

	/**
	 * Returns the block hardness for a metadata. The infested block is not very hard.
	 */
	@Override
	public float getBlockHardness(int meta) {
		if (meta == META_INFESTED) {
			return 0.75F;
		} else {
			return super.getBlockHardness(meta);
		}
	}

	/** The infested block can be broken by anything (it is full of termites, not worth protecting). */
	@Override
	public int getMinHarvestLevel(int meta) {
		if (meta == META_INFESTED) {
			return 0;
		} else {
			return super.getMinHarvestLevel(meta);
		}
	}

	/** Explosion resistance is static in MITE (balance B4b: 10). */
	@Override
	public float getExplosionResistance(Explosion explosion) {
		return 10F;
	}

	/** Silk touch would let the player carry a termite nest around safely. */
	@Override
	public boolean canSilkHarvest(int meta) {
		return meta != META_INFESTED;
	}

	// ---- drops ----

	/**
	 * Replaces TF dropBlockAsItemWithChance / quantityDropped / damageDropped: the infested block drops a tower termite
	 * and no item, every other variant drops itself (balance: tower blocks drop themselves, also when exploded).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		int meta = info.getMetadata();
		World world = info.world;

		if (meta == META_INFESTED) {
			if (world != null && !world.isRemote) {
				EntityTFTowerTermite termite = new EntityTFTowerTermite(world);
				termite.setLocationAndAngles(info.x + 0.5D, info.y, info.z + 0.5D, 0.0F, 0.0F);
				world.spawnEntityInWorld(termite);
				termite.spawnExplosionParticle();
			}
			return 0;
		}

		if (info.wasCrushed()) {
			return 0;
		}

		return this.dropBlockAsEntityItem(info, this.createStackedBlock(meta));
	}

}
