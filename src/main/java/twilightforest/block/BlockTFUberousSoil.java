package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.BlockCrops;
import net.minecraft.BlockFarmland;
import net.minecraft.BlockLilyPad;
import net.minecraft.BlockMushroom;
import net.minecraft.BlockNetherStalk;
import net.minecraft.BlockPlant;
import net.minecraft.BlockStem;
import net.minecraft.IBlockAccess;
import net.minecraft.Item;
import net.minecraft.ItemDye;
import net.minecraft.ItemStack;
import net.minecraft.CreativeTabs;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.EnumPlantType;
import twilightforest.compat.IPlantable;
import twilightforest.compat.ITFPlantSoil;

/**
 * Uberous soil: sustains Crop/Plains/Cave plants and bone-meal-spreads (1.7.10 IGrowable).
 */
public class BlockTFUberousSoil extends Block implements ITFPlantSoil {

	protected BlockTFUberousSoil(int id) {
		super(id, Material.dirt, new BlockConstants());
		this.setBlockBoundsForAllThreads(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
		this.setLightOpacity(255);
		this.setHardness(0.6F);
		this.setStepSound(soundGravelFootstep);
		this.setTickRandomly(true);

		this.setTextureName(TwilightForestMod.ID + ":uberous_soil");

		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/** Not a full cube (0.9375 high): neither opaque nor a standard form cube. */
	@Override
	public boolean isStandardFormCube(boolean[] isStandardFormCube, int metadata) {
		return false;
	}

	/** Drops plain dirt (1.7.10 getItemDropped = dirt); never itself, also not for silk harvests. */
	@Override
	public boolean canSilkHarvest(int metadata) {
		return false;
	}

	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return this.dropBlockAsEntityItem(info, Block.dirt);
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random rand) {
		if (isSolidAbove(world, x, y, z)) {
			world.setBlock(x, y, z, Block.dirt.blockID);
			return true;
		}
		return false;
	}

	private static boolean isSolidAbove(World world, int x, int y, int z) {
		Block above = world.getBlock(x, y + 1, z);
		return above != null && above.blockMaterial.isSolid();
	}

	/**
	 * Forge canSustainPlant: Crop, Plains and Cave plants (queried via TFBlockUtil.canSustainPlant).
	 */
	@Override
	public boolean canSustainPlant(EnumPlantType type) {
		return type == EnumPlantType.Crop || type == EnumPlantType.Plains || type == EnumPlantType.Cave;
	}

	/**
	 * What Forge would call IPlantable.getPlantType for any plant block: TF plants say so themselves, vanilla MITE plants are
	 * classified by class. Null = not a plant.
	 */
	public static EnumPlantType plantTypeOf(Block block, IBlockAccess world, int x, int y, int z) {
		if (block instanceof IPlantable plantable) {
			return plantable.getPlantType(world, x, y, z);
		} else if (block instanceof BlockCrops || block instanceof BlockStem) {
			return EnumPlantType.Crop;
		} else if (block instanceof BlockNetherStalk) {
			return EnumPlantType.Nether;
		} else if (block instanceof BlockMushroom) {
			return EnumPlantType.Cave;
		} else if (block instanceof BlockLilyPad) {
			return EnumPlantType.Water;
		} else if (block instanceof BlockPlant) {
			return EnumPlantType.Plains;
		}
		return null;
	}

	/**
	 * Used by the plant legality mixins: true when a vanilla MITE plant may grow on / be placed on the given soil block.
	 * TF plants (IPlantable) keep their own rules.
	 */
	public static boolean canVanillaPlantUse(Block plant, Block below) {
		if (plant == null || plant instanceof IPlantable || !(below instanceof ITFPlantSoil soil)) {
			return false;
		}
		EnumPlantType type = plantTypeOf(plant, null, 0, 0, 0);
		return type != null && soil.canSustainPlant(type);
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: x, y, z, neighbor block id
	 */
	@Override
	public boolean onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
		if (isSolidAbove(world, x, y, z)) {
			world.setBlock(x, y, z, Block.dirt.blockID);
			return true;
		}

		Block above = world.getBlock(x, y + 1, z);
		EnumPlantType type = plantTypeOf(above, world, x, y + 1, z);
		if (type != null) {
			// revert to farmland or grass
			if (type == EnumPlantType.Crop) {
				world.setBlock(x, y, z, Block.tilledField.blockID, 2, 2);
			} else if (type == EnumPlantType.Plains) {
				world.setBlock(x, y, z, Block.grass.blockID);
			} else {
				world.setBlock(x, y, z, Block.dirt.blockID);
			}
			// apply bonemeal
			// TODO(mite-port): MITE bone meal only unblights crops / feeds stems (BlockFarmland.fertilize); it has no growth
			// stages, so the 4 applications of 1.7.10 ItemDye.applyBonemeal do far less than before.
			ItemStack dye = new ItemStack(Item.dyePowder, 1, ItemDye.WHITE);
			for (int i = 0; i < 4; i++) {
				BlockFarmland.fertilize(world, x, y, z, dye, null);
			}
			// green sparkles
			if (!world.isRemote) {
				world.playAuxSFX(2005, x, y + 1, z, 0);
			}
			return true;
		}
		return false;
	}

	/*
	 * IGrowable replacement (func_149851_a / func_149852_a / func_149853_b).
	 * TODO(mite-port): MITE ItemDye.tryFertilize has no IGrowable hook; a mixin on it must call tryBoneMeal for this block.
	 */

	/**
	 * Is is possible for us to grow? (func_149851_a)
	 */
	public boolean canGrow(World world, int x, int y, int z) {
		return true;
	}

	/**
	 * Have we randomly decided it's okay to grow? (func_149852_a)
	 */
	public boolean shouldGrow(World world, Random rand, int x, int y, int z) {
		return true;
	}

	/**
	 * Do the growth! (func_149853_b)
	 */
	public void grow(World world, Random rand, int x, int y, int z) {
		int gx = x;
		int gy = y;
		int gz = z;

		if (rand.nextBoolean()) {
			gx += (rand.nextBoolean() ? 1 : -1);
		} else {
			gz += (rand.nextBoolean() ? 1 : -1);
		}

		Block blockAt = world.getBlock(gx, gy, gz);
		if (world.isAirBlock(gx, gy + 1, gz) && (blockAt == Block.dirt || blockAt == Block.grass || blockAt == Block.tilledField)) {
			world.setBlock(gx, gy, gz, this.blockID);
		}
	}

	/**
	 * Entry point for the bone meal hook: applies one bone meal to the soil at x,y,z. Returns true if the item should be used up.
	 */
	public static boolean tryBoneMeal(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (!(block instanceof BlockTFUberousSoil soil) || !soil.canGrow(world, x, y, z)) {
			return false;
		}
		if (!world.isRemote && soil.shouldGrow(world, world.rand, x, y, z)) {
			soil.grow(world, world.rand, x, y, z);
		}
		return true;
	}
}
