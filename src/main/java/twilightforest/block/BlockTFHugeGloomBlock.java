package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/** Giant gloom (mushroom) block grown by magic beans. Metadata selects cap/stem/inside faces like vanilla huge mushrooms. */
public class BlockTFHugeGloomBlock extends Block {

	private Icon capTex;
	private Icon stemTex;
	private Icon insideTex;

	public BlockTFHugeGloomBlock(int id) {
		super(id, Material.wood, new BlockConstants());
		this.setHardness(0.2F);
		this.setStepSound(soundWoodFootstep);
		this.setTextureName(TwilightForestMod.ID + ":huge_gloom");
		this.setLightValue(5F / 16F);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/** Cap/stem/inside piece (1-10) as in a vanilla huge mushroom, 14 = all cap, 15 = all stem, 0 = inside. */
	@Override
	public boolean isValidMetadata(int metadata) {
		return (metadata >= 0 && metadata <= 10) || metadata == 14 || metadata == 15;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return meta == 10 && side > 1 ? this.stemTex : (meta >= 1 && meta <= 9 && side == 1 ? this.capTex : (meta >= 1 && meta <= 3 && side == 2 ? this.capTex : (meta >= 7 && meta <= 9 && side == 3 ? this.capTex : ((meta == 1 || meta == 4 || meta == 7) && side == 4 ? this.capTex : ((meta == 3 || meta == 6 || meta == 9) && side == 5 ? this.capTex : (meta == 14 ? this.capTex : (meta == 15 ? this.stemTex : this.insideTex)))))));
	}

	/**
	 * Drops 0-2 TF plant items (1.7.10 quantityDropped: nextInt(10) - 7, clamped to 0). Silk harvests are routed to
	 * dropBlockAsItself by Block.dropBlockAsEntityItem(Block, info).
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		int quantity = info.world.rand.nextInt(10) - 7;

		if (quantity < 0) {
			quantity = 0;
		}

		return this.dropBlockAsEntityItem(info, TFBlocks.plant.blockID, 0, quantity, 1.0F);
	}

	/**
	 * Middle-click pick result (1.7.10 getItem): the plant block.
	 */
	@Override
	public int idPicked(World world, int x, int y, int z) {
		return TFBlocks.plant.blockID;
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.capTex = iconRegister.registerIcon(this.getTextureName() + "_cap");
		this.insideTex = iconRegister.registerIcon(this.getTextureName() + "_inside");
		this.stemTex = iconRegister.registerIcon(this.getTextureName() + "_stem");
	}
}
