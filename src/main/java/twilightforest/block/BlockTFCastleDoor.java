package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;
import twilightforest.network.TFNetwork;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.WorldProviderTwilightForest;

public class BlockTFCastleDoor extends Block {

	private Icon activeIcon;
	private boolean isVanished;

	/**
	 * Metadata bits 0-1: lock index, bit 3 (8): active (about to vanish / reappear).
	 * MITE_BALANCE B5: unbreakable; the player opens them with the rune puzzle only.
	 */
	public BlockTFCastleDoor(int id, boolean isVanished) {
		// Material.glass doubles as the "vanished" marker for the constructor-time overrides below,
		// because instance fields are not assigned yet while Block.<init> queries isSolid/isStandardFormCube.
		super(id, isVanished ? Material.glass : Material.stone, isVanished ? new BlockConstants().setNeverHidesAdjacentFaces() : new BlockConstants());

		this.isVanished = isVanished;

		this.setBlockUnbreakable();
		this.setStepSound(Block.soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	/** Valid before the constructor body has run (Block.<init> calls the metadata overrides). */
	private boolean isVanishedMaterial() {
		return this.blockMaterial == Material.glass;
	}

	// ---- shape / subtypes (pure functions of metadata, called from Block.<init>) ----

	/** Vanished doors do not block movement (1.7.10 getBlocksMovement / null collision box). */
	@Override
	public boolean isSolid(boolean[] is_solid, int metadata) {
		return !this.isVanishedMaterial();
	}

	/** 1.7.10 isOpaqueCube() == !isVanished. */
	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return !this.isVanishedMaterial();
	}

	@Override
	public boolean isValidMetadata(int meta) {
		return (meta & 4) == 0;
	}

	/** TF 1.7.10 never overrode damageDropped/getSubBlocks: only subtype 0 exists as an item. */
	@Override
	public int getBlockSubtypeUnchecked(int meta) {
		return 0;
	}

	@Override
	public void registerIcons(IconRegister register) {
		if (this.isVanished) {
			this.blockIcon = register.registerIcon(TwilightForestMod.ID + ":castle_door_vanished");
			this.activeIcon = register.registerIcon(TwilightForestMod.ID + ":castle_door_vanished_active");
		} else {
			this.blockIcon = register.registerIcon(TwilightForestMod.ID + ":castle_door");
			this.activeIcon = register.registerIcon(TwilightForestMod.ID + ":castle_door_active");
		}
	}

	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
		int meta = world.getBlockMetadata(x, y, z);

		if (isMetaActive(meta)) {
			return this.activeIcon;
		} else {
			return this.blockIcon;
		}
	}

	@Override
	public Icon getIcon(int side, int meta) {
		return isMetaActive(meta) ? this.activeIcon : this.blockIcon;
	}

	/**
	 * Called upon block activation (right click on the block.)
	 * The state change is done on the server only; the client just reports the click as used.
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float offsetX, float offsetY, float offsetZ) {
		int meta = world.getBlockMetadata(x, y, z);

		if (!isMetaActive(meta)) {
			if (!world.isRemote) {
				if (isBlockLocked(world, x, y, z)) {
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.click", 1.0F, 0.3F);
				} else {
					changeToActiveBlock(world, x, y, z, meta);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Change this block to be active
	 */
	public static void changeToActiveBlock(World world, int x, int y, int z, int meta) {
		changeToBlockMeta(world, x, y, z, meta | 8);
		playVanishSound(world, x, y, z);

		Block blockAt = world.getBlock(x, y, z);
		if (blockAt != null) {
			world.scheduleBlockUpdate(x, y, z, blockAt.blockID, 2 + world.rand.nextInt(5));
		}
	}

	/**
	 * Change this block into an different device block
	 */
	private static void changeToBlockMeta(World world, int x, int y, int z, int meta) {
		Block blockAt = world.getBlock(x, y, z);

		if (blockAt != null && (blockAt == TFBlocks.castleDoor || blockAt == TFBlocks.castleDoorVanished)) {
			world.setBlock(x, y, z, blockAt.blockID, meta, 3);
			world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
			world.notifyBlocksOfNeighborChange(x, y, z, blockAt.blockID);
		}
	}

	public static boolean isBlockLocked(World world, int x, int y, int z) {
		// check if we are in a structure, and if that structure says that we are locked

		int meta = world.getBlockMetadata(x, y, z);

		if (!world.isRemote && world.provider instanceof WorldProviderTwilightForest) {
			ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) world.provider).getChunkProvider();

			return chunkProvider.isStructureLocked(x, y, z, meta);
		} else {
			return false;
		}
	}

	public static boolean isMetaActive(int meta) {
		return (meta & 8) != 0;
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);

			if (this.isVanished) {
				if (isMetaActive(meta)) {
					TFWorldUtil.setBlock(world, x, y, z, TFBlocks.castleDoor, meta & 7, 3);
					world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
					playVanishSound(world, x, y, z);
				} else {
					changeToActiveBlock(world, x, y, z, meta);
				}
			} else {

				// if we have an active castle door, turn it into a vanished door block
				if (isMetaActive(meta)) {
					Block other = getOtherBlock(this);
					TFWorldUtil.setBlock(world, x, y, z, other, meta & 7, 3);
					world.scheduleBlockUpdate(x, y, z, other.blockID, 80);

					world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
					playReappearSound(world, x, y, z);
					world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);

					this.sendAnnihilateBlockPacket(world, x, y, z);

					// activate all adjacent inactive doors
					checkAndActivateCastleDoor(world, x - 1, y, z);
					checkAndActivateCastleDoor(world, x + 1, y, z);
					checkAndActivateCastleDoor(world, x, y + 1, z);
					checkAndActivateCastleDoor(world, x, y - 1, z);
					checkAndActivateCastleDoor(world, x, y, z + 1);
					checkAndActivateCastleDoor(world, x, y, z - 1);

				}

				// inactive solid door blocks we don't care about updates
			}

		}
		return false;
	}

	private void sendAnnihilateBlockPacket(World world, int x, int y, int z) {
		// TF packet 8 (ANNIHILATE_BLOCK), range 64
		TFNetwork.sendAnnihilateBlock(world, x, y, z);
	}

	private static void playVanishSound(World world, int x, int y, int z) {
		world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.fizz", 0.125f, world.rand.nextFloat() * 0.25F + 1.75F);
	}

	private static void playReappearSound(World world, int x, int y, int z) {
		world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.fizz", 0.125f, world.rand.nextFloat() * 0.25F + 1.25F);
	}

	private static Block getOtherBlock(Block block) {
		return block == TFBlocks.castleDoor ? TFBlocks.castleDoorVanished : TFBlocks.castleDoor;
	}

	/**
	 * If the targeted block is a vanishing block, activate it
	 */
	public static void checkAndActivateCastleDoor(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		if (block != null && block == TFBlocks.castleDoor && !isMetaActive(meta) && !isBlockLocked(world, x, y, z)) {
			changeToActiveBlock(world, x, y, z, meta);
		}
	}

	/**
	 * Shine bright like a DIAMOND! (or actually, sparkle like redstone ore)
	 * (unused in TF 1.7.10 as well: its randomDisplayTick only had an empty loop)
	 */
	public void sparkle(World world, int x, int y, int z, Random rand) {
		double offset = 0.0625D;

		for (int side = 0; side < 6; ++side) {
			double rx = x + rand.nextFloat();
			double ry = y + rand.nextFloat();
			double rz = z + rand.nextFloat();

			if (side == 0 && !Block.isBlockOpaqueStandardFormCube(world, x, y + 1, z)) {
				ry = y + 1 + offset;
			}

			if (side == 1 && !Block.isBlockOpaqueStandardFormCube(world, x, y - 1, z)) {
				ry = y + 0 - offset;
			}

			if (side == 2 && !Block.isBlockOpaqueStandardFormCube(world, x, y, z + 1)) {
				rz = z + 1 + offset;
			}

			if (side == 3 && !Block.isBlockOpaqueStandardFormCube(world, x, y, z - 1)) {
				rz = z + 0 - offset;
			}

			if (side == 4 && !Block.isBlockOpaqueStandardFormCube(world, x + 1, y, z)) {
				rx = x + 1 + offset;
			}

			if (side == 5 && !Block.isBlockOpaqueStandardFormCube(world, x - 1, y, z)) {
				rx = x + 0 - offset;
			}

			if (rx < x || rx > x + 1 || ry < 0.0D || ry > y + 1 || rz < z || rz > z + 1) {
				TFParticles.spawn(world, "reddust", rx, ry, rz, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getCastleMagicBlockRenderID();
	}

}
