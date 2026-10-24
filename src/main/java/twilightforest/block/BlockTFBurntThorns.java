package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.EntityLivingBase;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.IBlockAccess;
import net.minecraft.World;

/**
 * Burnt thorns, they dissolve when touched. Balance (MITE_BALANCE B0): hardness 0.1, no tool needed.
 */
public class BlockTFBurntThorns extends BlockTFThorns {

	public BlockTFBurntThorns(int id) {
		super(id);
		this.setHardness(0.1F);
		this.setMinHarvestLevel(0);
		this.setStepSound(Block.soundSandFootstep);

		this.setNames(new String[] {"burnt"});
	}

	/** Burnt thorns only have one texture, whatever the low metadata bits say. */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	/**
	 * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
	 */
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		// dissolve
		if (!world.isRemote && entity instanceof EntityLivingBase) {
			int metadata = world.getBlockMetadata(x, y, z);
			world.playAuxSFX(2001, x, y, z, this.blockID + (metadata << 12));
			world.setBlockToAir(x, y, z);
		}
	}

	/**
	 * Break normally (the thorns' grow-back behaviour is skipped, MITE's own harvesting removes the block).
	 */
	@Override
	public boolean tf$removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		return false;
	}

	@Override
	public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	/**
	 * no need for leaf decay, I think
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int metadata) {
	}
}
