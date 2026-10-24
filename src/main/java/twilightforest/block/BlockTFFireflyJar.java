package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.EntityTFTinyFirefly;

public class BlockTFFireflyJar extends Block {

	public static Icon jarTop;
	public static Icon jarSide;
	public static Icon jarCork;

	protected BlockTFFireflyJar(int id) {
		super(id, Material.glass, new BlockConstants().setNeverHidesAdjacentFaces());
		this.setBlockBoundsForAllThreads(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);
		this.setHardness(0.3F);
		this.setStepSound(Block.soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setLightValue(1.0F);
	}

	/**
	 * Not a full cube (1.7.10 renderAsNormalBlock/isOpaqueCube/isBlockNormalCube returned false).
	 */
	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getComplexBlockRenderID();
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return (side == 1 || side == 0) ? jarTop : jarSide;
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		this.setBlockBoundsForCurrentThread(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	@Override
	public void setBlockBoundsForItemRender(int item_damage) {
		this.setBlockBoundsForCurrentThread(0.1875F, 0.0F, 0.1875F, 0.8125F, 1.0F, 0.8125F);
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		double dx = x + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);
		double dy = y - 0.1F + ((rand.nextFloat() - rand.nextFloat()) * 0.4F);
		double dz = z + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);

		EntityTFTinyFirefly tinyfly = new EntityTFTinyFirefly(world, dx, dy, dz);
		world.addWeatherEffect(tinyfly);

		dx = x + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);
		dy = y - 0.1F + ((rand.nextFloat() - rand.nextFloat()) * 0.4F);
		dz = z + ((rand.nextFloat() - rand.nextFloat()) * 0.3F + 0.5F);

		tinyfly = new EntityTFTinyFirefly(world, dx, dy, dz);
		world.addWeatherEffect(tinyfly);
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		BlockTFFireflyJar.jarTop = iconRegister.registerIcon(TwilightForestMod.ID + ":fireflyjar_top");
		BlockTFFireflyJar.jarSide = iconRegister.registerIcon(TwilightForestMod.ID + ":fireflyjar_side");
		BlockTFFireflyJar.jarCork = iconRegister.registerIcon(TwilightForestMod.ID + ":fireflyjar_cork");
	}

}
