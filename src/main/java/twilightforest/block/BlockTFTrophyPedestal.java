package twilightforest.block;

import java.util.List;
import java.util.Random;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Explosion;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.PlayerStatsHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFChat;

public class BlockTFTrophyPedestal extends Block {

	private Icon sprTopActive;
	private Icon sprTop;
	private Icon sprBottom;
	private Icon sprNagaActive;
	private Icon sprNaga;
	private Icon sprLichActive;
	private Icon sprLich;
	private Icon sprHydraActive;
	private Icon sprHydra;
	private Icon sprUrghastActive;
	private Icon sprUrghast;

	public BlockTFTrophyPedestal(int id) {
		super(id, Material.stone, new BlockConstants().setNeverHidesAdjacentFaces());
		// MITE_BALANCE B3: stone hardness (2.4), min harvest level 2 (copper pick); resistance 2000 is handled in getExplosionResistance
		this.setHardness(Material.stone.getFullBlockHardness());
		this.setMinHarvestLevel(2);
		this.setStepSound(Block.soundStoneFootstep);
		this.setBlockBoundsForAllThreads(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);

		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	// ---- constructor-time virtuals (pure functions of metadata) ----

	/** Low 2 bits = rotation, bit 8 = latent (shield still up). */
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	/** Only 0 and 15 exist as items (see getItemStacks). */
	@Override
	public int getItemSubtype(int metadata) {
		return metadata == 15 ? 15 : 0;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}

	@Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
		return false;
	}

	@Override
	public String getMetadataNotes() {
		return "bits 0-1 rotation, bit 8 latent (shield blocks still present)";
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {

		if (side == 1) {
			return meta > 7 ? sprTopActive : sprTop;
		} else if (side >= 2 && side <= 5) {
			// determine rotated side to show
			int rotate = meta & 3;
			int rotatedSide = ((side - 2) + rotate) % 4;

			switch (rotatedSide) {
			case 0:
				return meta > 7 ? sprNagaActive : sprNaga;
			case 1:
				return meta > 7 ? sprLichActive : sprLich;
			case 2:
				return meta > 7 ? sprHydraActive : sprHydra;
			case 3:
				return meta > 7 ? sprUrghastActive : sprUrghast;
			}

		}

		return sprTop;
	}

	/**
	 * Properly register icon source
	 */
	@Override
	public void registerIcons(IconRegister register) {
		this.blockIcon = register.registerIcon(TwilightForestMod.ID + ":pedestal_top");
		this.sprTopActive = register.registerIcon(TwilightForestMod.ID + ":pedestal_top_active");
		this.sprTop = register.registerIcon(TwilightForestMod.ID + ":pedestal_top");
		this.sprBottom = register.registerIcon(TwilightForestMod.ID + ":pedestal_top");
		this.sprNagaActive = register.registerIcon(TwilightForestMod.ID + ":pedestal_naga_active");
		this.sprNaga = register.registerIcon(TwilightForestMod.ID + ":pedestal_naga");
		this.sprLichActive = register.registerIcon(TwilightForestMod.ID + ":pedestal_lich_active");
		this.sprLich = register.registerIcon(TwilightForestMod.ID + ":pedestal_lich");
		this.sprHydraActive = register.registerIcon(TwilightForestMod.ID + ":pedestal_hydra_active");
		this.sprHydra = register.registerIcon(TwilightForestMod.ID + ":pedestal_hydra");
		this.sprUrghastActive = register.registerIcon(TwilightForestMod.ID + ":pedestal_urghast_active");
		this.sprUrghast = register.registerIcon(TwilightForestMod.ID + ":pedestal_urghast");
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (only the plain and the latent pedestal)
	 */
	@Override
	public void getItemStacks(int id, CreativeTabs tabs, List list) {
		list.add(new ItemStack(id, 1, 0));
		list.add(new ItemStack(id, 1, 15));
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		this.setBlockBoundsForCurrentThread(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getPedestalBlockRenderID();
	}

	/**
	 * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
	 * coordinates.  Args: blockAccess, x, y, z, side
	 */
	@Override
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		return true;
	}

	/**
	 * Lets the block know when one of its neighbor changes. Args: x, y, z, neighbor blockID
	 */
	@Override
	public boolean onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
		int meta = world.getBlockMetadata(x, y, z);

		if (!world.isRemote && meta > 0) {
			// something has changed, is there a trophy on top of us?
			if (isTrophyOnTop(world, x, y, z)) {
				world.scheduleBlockUpdate(x, y, z, this.blockID, 1); // original delay is 1 tick, not tickRate
			}
		}

		return super.onNeighborBlockChange(world, x, y, z, neighborBlockId);
	}

	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, Entity placer, boolean test_only) {
		if (!test_only && placer != null) {
			int facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

			// the placement metadata is the item subtype (0 or 15)
			int latent = metadata & 8;

			int rotation;
			switch (facing) {
			case 1:
				rotation = 1;
				break;
			case 2:
				rotation = 3;
				break;
			case 3:
				rotation = 2;
				break;
			default:
				rotation = 0;
			}

			world.setBlockMetadataWithNotify(x, y, z, rotation | latent, 2);
		}

		return super.onBlockPlacedMITE(world, x, y, z, metadata, placer, test_only);
	}

	/**
	 * Just checks if there is a trophy above the position indicated.  Perhaps in the future we can accept different kinds of trophies.
	 */
	private boolean isTrophyOnTop(World world, int x, int y, int z) {
		return world.getBlockId(x, y + 1, z) == TFBlocks.trophy.blockID;
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote) {
			int meta = world.getBlockMetadata(x, y, z);

			if (this.isTrophyOnTop(world, x, y, z)) {
				// is the pedestal still "latent"?
				if (meta > 7) {
					// check enforced progression
					if (TwilightForestMod.isProgressionEnforced(world) && !twilightforest.TFTestMode.isOn(world)) {
						if (this.areNearbyPlayersEligible(world, x, y, z)) {
							doPedestalEffect(world, x, y, z, meta);
						}
						// warn players whether it works or not
						warnIneligiblePlayers(world, x, y, z);
					} else {
						// just do it if enforced progression is turned off
						doPedestalEffect(world, x, y, z, meta);
					}
				}

				rewardNearbyPlayers(world, x, y, z);
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private List<EntityPlayer> getNearbyPlayers(World world, int x, int y, int z) {
		return world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(16.0D, 16.0D, 16.0D));
	}

	private void warnIneligiblePlayers(World world, int x, int y, int z) {
		for (EntityPlayer player : getNearbyPlayers(world, x, y, z)) {
			if (!isPlayerEligible(player)) {
				TFChat.sendTranslated(player, "tf.pedestal.unworthy");
			}
		}
	}

	private boolean areNearbyPlayersEligible(World world, int x, int y, int z) {
		boolean isEligible = false;

		for (EntityPlayer player : getNearbyPlayers(world, x, y, z)) {
			isEligible |= isPlayerEligible(player);
		}

		return isEligible;
	}

	private boolean isPlayerEligible(EntityPlayer player) {
		// same stat lookup as TFFeature.doesPlayerHaveRequiredAchievement
		if (player instanceof ServerPlayer && player.stats != null) {
			return PlayerStatsHelper.getValue(TFAchievementPage.twilightProgressTrophyPedestal.parentAchievement.statId, player.stats) == 1L;
		}
		// uh, not a player?
		return false;
	}

	private void doPedestalEffect(World world, int x, int y, int z, int meta) {
		// if we're a latent pedestal, change to a non-latent!
		// remove shield blocks
		removeNearbyShields(world, x, y, z);

		// change this block meta to be non-latent
		world.setBlockMetadataWithNotify(x, y, z, meta & 7, 2);

		// sound
		world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "mob.zombie.infect", 4.0F, 0.1F);
	}

	private void rewardNearbyPlayers(World world, int x, int y, int z) {
		// scan for players nearby to give the achievement
		for (EntityPlayer player : getNearbyPlayers(world, x, y, z)) {
			player.triggerAchievement(TFAchievementPage.twilightProgressTrophyPedestal);
		}
	}

	/**
	 * Remove shield blocks near the specified coordinates
	 */
	protected void removeNearbyShields(World world, int x, int y, int z) {
		for (int sx = -5; sx <= 5; sx++) {
			for (int sy = -5; sy <= 5; sy++) {
				for (int sz = -5; sz <= 5; sz++) {
					int idAt = world.getBlockId(x + sx, y + sy, z + sz);
					int metaAt = world.getBlockMetadata(x + sx, y + sy, z + sz);

					if (idAt == TFBlocks.shield.blockID && metaAt == 15) {
						world.setBlock(x + sx, y + sy, z + sz, 0, 0, 2);

						// effect
						world.playAuxSFX(2001, x + sx, y + sy, z + sz, idAt + (metaAt << 12));
					}
				}
			}
		}
	}

	/**
	 * How many world ticks before ticking
	 */
	@Override
	public int tickRate(World world) {
		return 10;
	}

	/**
	 * Not breakable if meta > 0 (replaces getPlayerRelativeBlockHardness: negative hardness = unbreakable in MITE).
	 */
	@Override
	public float getBlockHardness(int metadata) {
		if (metadata > 0) {
			return -1.0F;
		}
		return super.getBlockHardness(metadata);
	}

	/**
	 * setResistance is a no-op in MITE (resistance == hardness), so the original 2000 resistance is returned here.
	 */
	@Override
	public float getExplosionResistance(Explosion explosion) {
		return 2000.0F;
	}

}
