package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.BlockHardness;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.IBlockAccess;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.ITFLeafSupport;
import twilightforest.compat.TFBlockUtil;

/** Trunk block of the magic bean stalk. Holds leaves up (ITFLeafSupport) and starts leaf decay when broken. */
public class BlockTFHugeStalk extends Block implements ITFLeafSupport {

	private Icon topIcon;

	public BlockTFHugeStalk(int id) {
		super(id, Material.wood, new BlockConstants());

		// MITE_BALANCE B1: huge stalk = MITE log hardness (setResistance is a no-op in MITE, hardness is the explosion resistance)
		this.setHardness(BlockHardness.log);

		this.setTextureName(TwilightForestMod.ID + ":huge_stalk");

		this.setStepSound(soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		super.registerIcons(par1IconRegister);

		this.topIcon = par1IconRegister.registerIcon(this.getTextureName() + "_top");
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (side == 0 || side == 1) {
			return this.topIcon;
		} else {
			return this.blockIcon;
		}
	}

	/**
	 * Can prevent leaves connected to it from decaying (Forge canSustainLeaves).
	 */
	@Override
	public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
		return true;
	}

	/**
	 * Begin leaf decay when this block is broken
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		super.breakBlock(world, x, y, z, blockId, meta);

		byte radius = 4;
		int rad1 = radius + 1;

		if (world.checkChunksExist(x - rad1, y - rad1, z - rad1, x + rad1, y + rad1, z + rad1)) {
			for (int dx = -radius; dx <= radius; ++dx) {
				for (int dy = -radius; dy <= radius; ++dy) {
					for (int dz = -radius; dz <= radius; ++dz) {
						if (TFBlockUtil.isLeaves(world, x + dx, y + dy, z + dz)) {
							TFBlockUtil.beginLeavesDecay(world, x + dx, y + dy, z + dz);
						}
					}
				}
			}
		}
	}

}
