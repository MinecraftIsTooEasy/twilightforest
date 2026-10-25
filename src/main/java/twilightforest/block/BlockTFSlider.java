package twilightforest.block;

import java.util.Random;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockRotatedPillar;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.ForgeDirection;
import twilightforest.compat.TFDamage;
import twilightforest.entity.EntityTFSlideBlock;

public class BlockTFSlider extends BlockRotatedPillar {

	private static final int TICK_TIME = 80;
	private static final int OFFSET_TIME = 20;
	private static final int PLAYER_RANGE = 32;
	private static final float BLOCK_DAMAGE = 5;
	private Icon horiIcon;
	private Icon vertIcon;
	private Icon topIcon;

	public BlockTFSlider(int id) {
		super(id, Material.iron);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	// ---- subtypes / shape (pure functions of metadata, called from Block.<init>) ----

	/** Bits 0-1 are the subtype (update offset), bits 2-3 the axis (12 is unused). */
	@Override
	public boolean isValidMetadata(int meta) {
		return (meta & 12) != 12;
	}

	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return meta & 3;
	}

	@Override
	public boolean isStandardFormCube(boolean[] isStandardFormCube, int metadata) {
		return false;
	}

	/**
	 * Returns a bounding box (world coordinates) for collisions
	 */
	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		int meta = world.getBlockMetadata(x, y, z);

		int rotation = meta & 12;
		float pixel = 0.0625F;
		float inset = 5F;

		switch (rotation) {
		case 0:
		default:
			return AxisAlignedBB.getBoundingBox(x + pixel * inset, y, z + pixel * inset, x + 1F - pixel * inset, y + 1F, z + 1F - pixel * inset);
		case 4:
			return AxisAlignedBB.getBoundingBox(x, y + pixel * inset, z + pixel * inset, x + 1F, y + 1F - pixel * inset, z + 1F - pixel * inset);
		case 8:
			return AxisAlignedBB.getBoundingBox(x + pixel * inset, y + pixel * inset, z, x + 1F - pixel * inset, y + 1F - pixel * inset, z + 1F);
		}
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);

		setBlockBoundsBasedOnMeta(meta);
	}

	public void setBlockBoundsBasedOnMeta(int meta) {
		int rotation = meta & 12;
		float pixel = 0.0625F;
		float inset = 5F;

		switch (rotation) {
		case 0:
		default:
			this.setBlockBoundsForCurrentThread(pixel * inset, 0, pixel * inset, 1F - pixel * inset, 1F, 1F - pixel * inset);
			break;
		case 4:
			this.setBlockBoundsForCurrentThread(0, pixel * inset, pixel * inset, 1F, 1F - pixel * inset, 1F - pixel * inset);
			break;
		case 8:
			this.setBlockBoundsForCurrentThread(pixel * inset, pixel * inset, 0, 1F - pixel * inset, 1F - pixel * inset, 1F);
			break;
		}
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return 0;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		int rotation = meta & 12;

		if (rotation == 0) {
			switch (side) {
			case 0:
			case 1:
				return this.topIcon;
			default:
				return this.vertIcon;
			}
		} else if (rotation == 4) {
			switch (side) {
			case 4:
			case 5:
				return this.topIcon;
			default:
				return this.horiIcon;
			}
		} else { // rotation == 8
			switch (side) {
			case 2:
			case 3:
				return this.topIcon;
			case 0:
			case 1:
				return this.vertIcon;
			default:
				return this.horiIcon;
			}
		}
	}

	@Override
	protected Icon getSideIcon(int meta) {
		if ((meta & 12) == 0) {
			return this.horiIcon;
		} else if ((meta & 12) == 8) {
			return this.horiIcon;
		}
		return this.vertIcon;
	}

	@Override
	protected Icon getEndIcon(int meta) {
		return this.topIcon;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.horiIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":slider_h");
		this.vertIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":slider_v");
		this.topIcon = par1IconRegister.registerIcon(TwilightForestMod.ID + ":slider_top");
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random par5Random) {
		if (!world.isRemote && this.isConnectedInRange(world, x, y, z)) {
			EntityTFSlideBlock slideBlock = new EntityTFSlideBlock(world, x + 0.5, y, z + 0.5, this, world.getBlockMetadata(x, y, z));
			world.spawnEntityInWorld(slideBlock);
		}

		scheduleBlockUpdate(world, x, y, z);
		return false;
	}

	/**
	 * Check if there is any players in range, and also recursively check connected blocks
	 */
	public boolean isConnectedInRange(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);

		if ((meta & 12) == 0) {
			return this.anyPlayerInRange(world, x, y, z) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.UP) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.DOWN);
		} else if ((meta & 12) == 4) {
			return this.anyPlayerInRange(world, x, y, z) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.WEST) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.EAST);
		} else if ((meta & 12) == 8) {
			return this.anyPlayerInRange(world, x, y, z) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.NORTH) || this.isConnectedInRangeRecursive(world, x, y, z, ForgeDirection.SOUTH);
		} else {
			// why are we here?
			return this.anyPlayerInRange(world, x, y, z);
		}
	}

	private boolean isConnectedInRangeRecursive(World world, int x, int y, int z, ForgeDirection dir) {
		// where is the coords we're talking about
		int dx = x + dir.offsetX;
		int dy = y + dir.offsetY;
		int dz = z + dir.offsetZ;

		// are the blocks connected?  (block and meta are the same
		if (world.getBlockId(x, y, z) == world.getBlockId(dx, dy, dz) && world.getBlockMetadata(x, y, z) == world.getBlockMetadata(dx, dy, dz)) {
			return this.anyPlayerInRange(world, dx, dy, dz) || this.isConnectedInRangeRecursive(world, dx, dy, dz, dir);
		} else {
			return false;
		}
	}

	/**
	 * Returns true if there is a player in range (using World.getClosestPlayer)
	 */
	public boolean anyPlayerInRange(World world, int x, int y, int z) {
		return world.getClosestPlayer((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, PLAYER_RANGE, true) != null;
	}

	public void scheduleBlockUpdate(World world, int x, int y, int z) {
		int offset = world.getBlockMetadata(x, y, z) & 3;
		int update = TICK_TIME - ((int) (world.getTotalWorldTime() - (offset * OFFSET_TIME)) % TICK_TIME);
		world.scheduleBlockUpdate(x, y, z, this.blockID, update);
	}

	/**
	 * Schedule an update to try to get lighting right
	 */
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		scheduleBlockUpdate(world, x, y, z);
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	@Override
	public void setBlockBoundsForItemRender(int itemDamage) {
		this.setBlockBoundsBasedOnMeta(0);
	}

	/**
	 * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
	 */
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		TFDamage.hurtGeneric(entity, BLOCK_DAMAGE);
		if (entity instanceof EntityLivingBase) {
			double kx = (x + 0.5 - entity.posX) * 2.0;
			double kz = (z + 0.5 - entity.posZ) * 2.0;

			// strength 1: MITE multiplies by it, the 1.7.10 5 was ignored
			((EntityLivingBase) entity).knockBack(null, 1.0F, kx, kz);
		}
	}
}
