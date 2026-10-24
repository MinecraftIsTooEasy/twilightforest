package twilightforest.block;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockOreStorage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.IBlockAccess;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFDamage;

/**
 * Block of knightmetal. 1.7.10 extended BlockCompressed; in MITE it is a BlockOreStorage of the custom knightmetal Material
 * (hardness 1.2 * durability, harvest level +1, see MITE_BALANCE B6).
 */
public class BlockTFKnightmetalBlock extends BlockOreStorage {

	private static final float BLOCK_DAMAGE = 4;

	/**
	 * @param material the TF knightmetal material (durability must already be set, otherwise hardness is 0;
	 *                 map color must be non-null)
	 */
	public BlockTFKnightmetalBlock(int id, Material material) {
		super(id, material);
		// balance B6: resistance 30 (hardness 1.2 * durability comes from BlockOreStorage)
		this.setResistance(30.0F);
		this.setStepSound(Block.soundMetalFootstep);
		this.setTextureName(TwilightForestMod.ID + ":knightmetal_block");
	}

	/**
	 * Collision box is inset by 1/16 so entities inside the spikes' reach touch the block.
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		double f = 1D / 16D;
		return AxisAlignedBB.getAABBPool().getAABB(x + f, y + f, z + f, x + 1 - f, y + 1 - f, z + 1 - f);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		TFDamage.hurt(entity, DamageSource.cactus, BLOCK_DAMAGE);
	}

	// 1.7.10 renderAsNormalBlock/isOpaqueCube = false
	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getKnightmetalBlockRenderID();
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
		return true;
	}
}
