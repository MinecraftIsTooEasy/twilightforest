package twilightforest.block;

import net.minecraft.BlockPane;
import net.minecraft.Entity;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/**
 * Force field pane. MITE_BALANCE B5: unbreakable (hardness -1, level 100).
 * Extends MITE's BlockPane so the vanilla pane renderer (render type 18) and connection logic are reused:
 * {@code BlockPane.canThisPaneConnectToThisBlockID} (opaque cubes, own id, glass) replaces Forge's canPaneConnectTo.
 */
public class BlockTFForceField extends BlockPane {

	public static String[] names = new String[] { "purple", "pink", "orange", "green", "blue" };

	public static Icon[] sides;
	public static Icon top;

	/**
	 * BlockPane's constructor fixes the constants to "no alpha blending", and Block.<init> validates getRenderBlockPass()==0
	 * against them. The force field must render in the alpha pass (1), so the pass is 0 until construction has finished.
	 */
	private boolean constructed;

	protected BlockTFForceField(int id) {
		super(id, "glass", "glass_pane_top", Material.glass, false);
		this.setLightValue(2F / 15F);
		this.setBlockUnbreakable();
		this.constructed = true;
	}

	// ---- subtypes (pure functions of metadata, called from Block.<init>) ----

	@Override
	public boolean isValidMetadata(int meta) {
		return meta >= 0 && meta < names.length;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return sides[meta % names.length];
	}

	/** 1.7.10 func_150097_e(): the icon of the pane's top edge. */
	@Override
	public Icon getSideTextureIndex() {
		return top;
	}

	@Override
	public void registerIcons(IconRegister register) {
		super.registerIcons(register);

		sides = new Icon[names.length];

		for (int i = 0; i < names.length; i++) {
			sides[i] = register.registerIcon(TwilightForestMod.ID + ":forcefield_" + names[i]);
		}

		top = register.registerIcon(TwilightForestMod.ID + ":forcefield_top");
	}

	/**
	 * How bright to render this block based on the light its receiving. Args: iBlockAccess, x, y, z
	 */
	@Override
	public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z) {
		return 15 << 20 | 15 << 4;
	}

	/**
	 * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
	 */
	@Override
	public int getRenderBlockPass() {
		return this.constructed ? 1 : 0;
	}

	/**
	 * Fill in the whole block when we connect on all sides (1.7.10 addCollisionBoxesToList override).
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		boolean north = this.canThisPaneConnectToThisBlockID(world.getBlockId(x, y, z - 1));
		boolean south = this.canThisPaneConnectToThisBlockID(world.getBlockId(x, y, z + 1));
		boolean west = this.canThisPaneConnectToThisBlockID(world.getBlockId(x - 1, y, z));
		boolean east = this.canThisPaneConnectToThisBlockID(world.getBlockId(x + 1, y, z));

		if (north && south && east && west) {
			return getStandardFormBoundingBoxFromPool(x, y, z);
		}

		return super.getCollisionBounds(world, x, y, z, entity);
	}

}
