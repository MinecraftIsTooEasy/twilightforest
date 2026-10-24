package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockLeavesBase;
import net.minecraft.ColorizerFoliage;
import net.minecraft.CreativeTabs;
import net.minecraft.IBlockAccess;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Dark forest canopy leaves: never decay, nearly fireproof, block all light. Extends BlockLeavesBase (not Block) so that
 * Block.isTreeLeaves() / TFBlockUtil.isLeaves() hold, as Forge's isLeaves did for Material.leaves.
 */
public class BlockTFDarkLeaves extends BlockLeavesBase {

	public BlockTFDarkLeaves(int id) {
		super(id, Material.tree_leaves, true);
		this.setHardness(1.2F); // balance: 1.2 (was 2.0)
		this.setStepSound(Block.soundGrassFootstep);
		this.setLightOpacity(255); // the original is an opaque cube: no light through the dark canopy
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setCushioning(0.8F);

		this.setTextureName(TwilightForestMod.ID + ":darkwood_leaves2");
	}

	@Override
	public int getBlockColor() {
		double d0 = 0.5D;
		double d1 = 1.0D;
		return ColorizerFoliage.getFoliageColor(d0, d1);
	}

	/**
	 * Returns the color this block should be rendered. Used by leaves.
	 */
	@Override
	public int getRenderColor(int meta) {
		return ColorizerFoliage.getFoliageColorBasic();
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return BlockTFLeaves.averageFoliageColor(world, x, z);
	}

	/**
	 * The original dark leaves are an opaque cube and the texture has no transparent pixels, so hide every face behind them.
	 * BlockLeavesBase draws all faces between leaves in fancy mode; across the thick dark canopy that cost the frame rate
	 * whenever the forest was on screen.
	 */
	@Override
	public boolean hidesAdjacentSide(IBlockAccess block_access, int x, int y, int z, Block neighbor, int side) {
		return true;
	}

	/**
	 * Drops a darkwood sapling (damage 3) like the other leaves: 1%, nothing if placed by a player. Flammability of the
	 * original (getFlammability 1, getFireSpreadSpeed 0) is applied through the fire table in TFBlocks.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if ((info.getMetadata() & 4) != 0 || TFBlocks.sapling == null) {
			return 0;
		}
		return this.dropBlockAsEntityItem(info, TFBlocks.sapling.blockID, 3, 1, 0.01F);
	}

	@Override
	public String getMetadataNotes() {
		return "Metadata unused (bit 4 = placed by player, bit 8 may be set by leaf-decay flagging and is ignored)";
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
