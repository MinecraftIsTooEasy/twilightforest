package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.EnumParticle;
import net.minecraft.Icon;
import net.minecraft.IBlockAccess;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class BlockTFTrollSteinn extends Block {

	private static final int LIGHT_THRESHHOLD = 7;
	private Icon blockIconLight;

	protected BlockTFTrollSteinn(int id) {
		super(id, Material.stone, new BlockConstants());

		// MITE_BALANCE B3: soft structure stone (hardness 2.4, harvest level 2 from Material.stone)
		this.setHardness(2.4F);
		this.setStepSound(soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setTextureName(TwilightForestMod.ID + ":trollsteinn");
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if (rand.nextInt(2) == 0) {
			this.sparkle(world, x, y, z, rand);
		}
	}

	private static boolean isOpaque(IBlockAccess world, int x, int y, int z) {
		return Block.isBlockSolidOpaqueStandardFormCube(world, x, y, z);
	}

	private void sparkle(World world, int x, int y, int z, Random rand) {
		Random random = rand;
		double pixel = 0.0625D;
		int threshhold = LIGHT_THRESHHOLD;

		for (int side = 0; side < 6; ++side) {
			double rx = (double) ((float) x + random.nextFloat());
			double ry = (double) ((float) y + random.nextFloat());
			double rz = (double) ((float) z + random.nextFloat());

			if (side == 0 && !isOpaque(world, x, y - 1, z) && world.getBlockLightValue(x, y - 1, z) <= threshhold) {
				ry = (double) (y + 0) - pixel;
			}

			if (side == 1 && !isOpaque(world, x, y + 1, z) && world.getBlockLightValue(x, y + 1, z) <= threshhold) {
				ry = (double) (y + 1) + pixel;
			}

			if (side == 2 && !isOpaque(world, x, y, z + 1) && world.getBlockLightValue(x, y, z + 1) <= threshhold) {
				rz = (double) (z + 1) + pixel;
			}

			if (side == 3 && !isOpaque(world, x, y, z - 1) && world.getBlockLightValue(x, y, z - 1) <= threshhold) {
				rz = (double) (z + 0) - pixel;
			}

			if (side == 4 && !isOpaque(world, x + 1, y, z) && world.getBlockLightValue(x + 1, y, z) <= threshhold) {
				rx = (double) (x + 1) + pixel;
			}

			if (side == 5 && !isOpaque(world, x - 1, y, z) && world.getBlockLightValue(x - 1, y, z) <= threshhold) {
				rx = (double) (x + 0) - pixel;
			}

			if (rx < (double) x || rx > (double) (x + 1) || ry < 0.0D || ry > (double) (y + 1) || rz < (double) z || rz > (double) (z + 1)) {
				world.spawnParticle(EnumParticle.reddust, rx, ry, rz, 0.25D, -1.0D, 0.5D);
			}
		}
	}

	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {

		// check brightness for the side
		if (side == 0 && isBlockLit(world, x, y - 1, z)) {
			return this.blockIconLight;
		}

		if (side == 1 && isBlockLit(world, x, y + 1, z)) {
			return this.blockIconLight;
		}

		if (side == 2 && isBlockLit(world, x, y, z - 1)) {
			return this.blockIconLight;
		}

		if (side == 3 && isBlockLit(world, x, y, z + 1)) {
			return this.blockIconLight;
		}

		if (side == 4 && isBlockLit(world, x - 1, y, z)) {
			return this.blockIconLight;
		}

		if (side == 5 && isBlockLit(world, x + 1, y, z)) {
			return this.blockIconLight;
		}

		return this.getIcon(side, world.getBlockMetadata(x, y, z));
	}

	private boolean isBlockLit(IBlockAccess world, int x, int y, int z) {
		int threshhold = LIGHT_THRESHHOLD << 4;

		if (isOpaque(world, x, y, z)) {
			return false;
		} else {
			int light = world.getLightBrightnessForSkyBlocks(x, y, z, 0);
			int sky = light % 65536;
			int block = light / 65536;

			return sky > threshhold || block > threshhold;
		}
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon(this.getTextureName());
		this.blockIconLight = par1IconRegister.registerIcon(this.getTextureName() + "_light");
	}
}
