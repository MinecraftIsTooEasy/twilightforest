package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockDoubleSlab;
import net.minecraft.BlockHardness;
import net.minecraft.BlockSlab;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Aurora half slab. MITE splits the 1.7.10 isDouble flag into two classes: this class is the single (half) slab and
 * {@link DoubleSlab} is the double one.
 * <p>
 * Registration order matters (MITE BlockDoubleSlab looks its single slab up as blocksList[doubleId + 1]): the double
 * slab id must be exactly (single slab id - 1) and the double slab must be constructed after the single slab.
 */
public class BlockTFAuroraSlab extends BlockSlab {

	private static final String[] TYPES = new String[] { "aurora" };

	private Icon sideIcon;

	public BlockTFAuroraSlab(int id) {
		super(id, Material.stone);
		// MITE_BALANCE B3: aurora block hardness 2.4 (copper pick, resistance 10); a half slab is half of that,
		// the double slab (BlockDoubleSlab doubles it) is again 2.4.
		this.setResistance(10.0F);
	}

	// ---- BlockSlab contract (all called from constructors: constants only) ----

	@Override
	public int getGroup() {
		return 4; // 1-3 are MITE's own slab groups; only used for the default name, overridden by setUnlocalizedName
	}

	@Override
	public String[] getTypes() {
		return TYPES;
	}

	@Override
	public boolean isValidMetadata(int meta) {
		return meta == 0 || meta == 8;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	@Override
	public Block getModelBlock(int meta) {
		return TFBlocks.auroraBlock;
	}

	@Override
	public float getBlockHardness(int meta) {
		return BlockHardness.stone * 0.5F;
	}

	/** 1.7.10 func_150002_b: the item name does not get a subtype suffix. */
	@Override
	public String getFullSlabName(int meta) {
		return this.getUnlocalizedName();
	}

	/**
	 * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
	 * when first determining what to render.
	 */
	@Override
	public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z) {
		return TFBlocks.auroraPillar.colorMultiplier(par1IBlockAccess, -x, y, -z);
	}

	@Override
	public int getBlockColor() {
		return this.colorMultiplier(null, 0, 0, 16);
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		return this.getBlockColor();
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (side == 0 || side == 1) {
			return this.blockIcon;
		} else {
			return this.sideIcon;
		}
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		// same sprite the pillar's end face uses (TextureMap returns the existing sprite for a repeated name)
		this.blockIcon = iconRegister.registerIcon(TwilightForestMod.ID + ":aurora_pillar_top");
		this.sideIcon = iconRegister.registerIcon(TwilightForestMod.ID + ":aurora_slab_side");
	}

	// TODO(mite-port): 1.7.10 shouldSideBeRendered override dropped; MITE BlockSlab.hidesAdjacentSide handles slab culling.
	// createStackedBlock/getItemDropped overrides dropped: BlockSlab/BlockDoubleSlab.createStackedBlock already give
	// 1 slab (single) / 2 slabs (double).

	/**
	 * The double aurora slab: identical tinting to the single slab (BlockDoubleSlab itself is untinted).
	 * Construct with {@code new BlockTFAuroraSlab.DoubleSlab(TFIds.AURORA_DOUBLE_SLAB, TFBlocks.auroraSlab)}
	 * after the single slab (single id must be double id + 1).
	 */
	public static class DoubleSlab extends BlockDoubleSlab {

		public DoubleSlab(int id, BlockTFAuroraSlab single) {
			super(id, single);
			this.setResistance(10.0F);
		}

		@Override
		public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
			return TFBlocks.auroraPillar.colorMultiplier(world, -x, y, -z);
		}

		@Override
		public int getBlockColor() {
			return this.colorMultiplier(null, 0, 0, 16);
		}

		@Override
		public int getRenderColor(int meta) {
			return this.getBlockColor();
		}
	}
}
