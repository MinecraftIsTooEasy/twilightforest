package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.compat.ForgeDirection;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFDamage;

/**
 * Thorn rose. Balance (MITE_BALANCE B2): wood, hardness 6.0, harvest level 3, contact damage 4.
 */
public class BlockTFThornRose extends Block {

	private static final float ROSE_DAMAGE = 4.0F;

	public BlockTFThornRose(int id) {
		// NotAlwaysLegal: the rose has to be supported by something that sustains leaves (isLegalAt)
		super(id, Material.wood, new BlockConstants().setNotAlwaysLegal().setNeverHidesAdjacentFaces());

		this.setHardness(6.0F);
		this.setMinHarvestLevel(3);
		this.setStepSound(Block.soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);

		float radius = 0.4F;
		this.setBlockBoundsForAllThreads(0.5F - radius, 0.5F - radius, 0.5F - radius, 0.5F + radius, .5F + radius, 0.5F + radius);
	}

	/**
	 * The type of render function that is called for this block (1 = crossed squares)
	 */
	@Override
	public int getRenderType() {
		return 1;
	}

	@Override
	public boolean isSolid(boolean[] isSolid, int metadata) {
		return false;
	}

	@Override
	public boolean isStandardFormCube(boolean[] isStandardFormCube, int metadata) {
		return false;
	}

	/**
	 * Can this block stay at this position (also checked on placement and neighbour changes).
	 */
	@Override
	public boolean isLegalAt(World world, int x, int y, int z, int metadata) {
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (TFBlockUtil.canSustainLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * No collision box, entities walk through (and get pricked).
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		return null;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (!world.isRemote) {
			TFDamage.hurt(entity, DamageSource.cactus, ROSE_DAMAGE);
		}
	}
}
