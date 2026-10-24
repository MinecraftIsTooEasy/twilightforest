package twilightforest.item;

import java.util.ArrayList;

import net.minecraft.Block;
import net.minecraft.ChunkCoordinates;
import net.minecraft.DamageSource;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.world.TFGenerator;

public class ItemTFOreMagnet extends ItemTF implements IDamageableItem {

	private static final float WIGGLE = 10F;

	/** MITE_BALANCE 7.7 defaults: range 16 (TF 32), vein max 12 (TF 24), durability 6 (TF 12); config magnet.range / veinMax / durability. */
	private static String whitelistSource;
	private static java.util.Set<Block> whitelist = java.util.Collections.emptySet();

	private Icon[] icons;
	private final String[] iconNames = new String[] {"oreMagnet", "oreMagnet1", "oreMagnet2"};

	/** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
	protected ItemTFOreMagnet(int id) {
		super(id, Material.iron);
		this.setMaxStackSize(1);
		this.setMaxDamage(TFConfig.magnetDurability);
	}

	@Override
	public int getNumComponentsForDurability() {
		return 1;
	}

	@Override
	public int getRepairCost() {
		return 1;
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
		return player.setHeldItemInUse();
	}

	/**
	 * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int useRemaining) {
		int useTime = this.getMaxItemUseDuration(stack) - useRemaining;

		if (!world.isRemote && useTime > 10) {
			int moved = doMagnet(world, player, 0, 0);

			if (moved == 0) {
				moved = doMagnet(world, player, WIGGLE, 0);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, WIGGLE, WIGGLE);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, 0, WIGGLE);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, -WIGGLE, WIGGLE);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, -WIGGLE, 0);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, -WIGGLE, -WIGGLE);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, 0, -WIGGLE);
			}
			if (moved == 0) {
				moved = doMagnet(world, player, WIGGLE, -WIGGLE);
			}

			if (moved > 0) {
				// 1 durability per moved block; MITE destroys the stack itself when it breaks
				player.tryDamageHeldItem(DamageSource.generic, moved);

				world.playSoundAtEntity(player, "mob.endermen.portal", 1.0F, 1.0F);
			}
		}
	}

	/**
	 * Icon for the given use time (ticks held). Used by the client icon hook.
	 * TODO(mite-port): MITE only swaps in-use icons for ItemBow in EntityPlayer.getItemIcon; needs a client mixin
	 * (ItemRendererMixin / EntityPlayer.getItemIcon) that calls this while the player uses an ItemTFOreMagnet.
	 */
	public Icon getIconForUseTime(int useTime) {
		if (this.icons == null) {
			return this.itemIcon;
		}
		if (useTime >= 20) {
			return (useTime >> 2) % 2 == 0 ? this.icons[2] : this.icons[1];
		}
		if (useTime > 10) {
			return this.icons[1];
		}
		return this.icons[0];
	}

	@Override
	public void registerIcons(IconRegister register) {
		super.registerIcons(register);
		this.icons = new Icon[iconNames.length];

		for (int i = 0; i < this.iconNames.length; ++i) {
			this.icons[i] = register.registerIcon(TwilightForestMod.ID + ":" + iconNames[i]);
		}
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	@Override
	public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
		return EnumItemInUseAction.BOW;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	/**
	 * Magnet from the player's position and facing to the specified offset
	 */
	protected int doMagnet(World world, EntityPlayer player, float yawOffset, float pitchOffset) {
		// find vector from look
		double range = TFConfig.magnetRange;
		Vec3 srcVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 lookVec = getOffsetLook(player, yawOffset, pitchOffset);
		Vec3 destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

		int useX = MathHelper.floor_double(srcVec.xCoord);
		int useY = MathHelper.floor_double(srcVec.yCoord);
		int useZ = MathHelper.floor_double(srcVec.zCoord);

		int destX = MathHelper.floor_double(destVec.xCoord);
		int destY = MathHelper.floor_double(destVec.yCoord);
		int destZ = MathHelper.floor_double(destVec.zCoord);

		return doMagnet(world, useX, useY, useZ, destX, destY, destZ);
	}

	/**
	 * This function makes the magnet work
	 */
	public static int doMagnet(World world, int useX, int useY, int useZ, int destX, int destY, int destZ) {
		int blocksMoved = 0;

		// MITE_BALANCE 7.7: no effect in the Underworld / Nether / End
		if (world.isUnderworld() || world.isTheNether() || world.isTheEnd()) {
			return 0;
		}

		// get blocks in line from src to dest
		ChunkCoordinates[] lineArray = TFGenerator.getBresehnamArrayCoords(useX, useY, useZ, destX, destY, destZ);

		// find some ore?
		Block foundID = null;
		int foundMeta = -1;
		int foundX = -1;
		int foundY = -1;
		int foundZ = -1;
		int baseX = -1;
		int baseY = -1;
		int baseZ = -1;

		for (ChunkCoordinates coord : lineArray) {
			Block searchID = world.getBlock(coord.posX, coord.posY, coord.posZ);
			int searchMeta = world.getBlockMetadata(coord.posX, coord.posY, coord.posZ);

			// keep track of where the dirt/stone we first find is.
			if (baseY == -1 && isReplaceable(searchID)) {
				baseX = coord.posX;
				baseY = coord.posY;
				baseZ = coord.posZ;
			}

			if (searchID != null && isOre(searchID, searchMeta)) {
				foundID = searchID;
				foundMeta = searchMeta;
				foundX = coord.posX;
				foundY = coord.posY;
				foundZ = coord.posZ;

				break;
			}
		}

		if (baseY != -1 && foundID != null) {
			// find the whole vein
			ArrayList<ChunkCoordinates> veinBlocks = new ArrayList<ChunkCoordinates>();
			findVein(world, foundX, foundY, foundZ, foundID, foundMeta, veinBlocks);

			// move it up into minable blocks or dirt
			int offX = baseX - foundX;
			int offY = baseY - foundY;
			int offZ = baseZ - foundZ;

			for (ChunkCoordinates coord : veinBlocks) {
				int replaceX = coord.posX + offX;
				int replaceY = coord.posY + offY;
				int replaceZ = coord.posZ + offZ;

				Block replaceID = world.getBlock(replaceX, replaceY, replaceZ);

				if (replaceID == null || isReplaceable(replaceID)) {
					// set vein to stone
					world.setBlock(coord.posX, coord.posY, coord.posZ, Block.stone.blockID, 0, 2);

					// set close to ore material
					world.setBlock(replaceX, replaceY, replaceZ, foundID.blockID, foundMeta, 2);
					blocksMoved++;
				}
			}
		}
		return blocksMoved;
	}

	/**
	 * Get the player look vector, but offset by the specified parameters.  We use to scan the area around where the player is looking
	 * in the likely case there's no ore in the exact look direction.
	 */
	private Vec3 getOffsetLook(EntityPlayer player, float yawOffset, float pitchOffset) {
		float var2 = MathHelper.cos(-(player.rotationYaw + yawOffset) * 0.017453292F - (float) Math.PI);
		float var3 = MathHelper.sin(-(player.rotationYaw + yawOffset) * 0.017453292F - (float) Math.PI);
		float var4 = -MathHelper.cos(-(player.rotationPitch + pitchOffset) * 0.017453292F);
		float var5 = MathHelper.sin(-(player.rotationPitch + pitchOffset) * 0.017453292F);
		return Vec3.createVectorHelper(var3 * var4, var5, var2 * var4);
	}

	/**
	 * Blocks an ore can be moved into: dirt, grass, gravel, stone
	 * (replaces Block.isReplaceableOreGen(stone) of Forge; the Nether branch is removed per MITE_BALANCE 7.7).
	 */
	private static boolean isReplaceable(Block block) {
		return block != null && (block == Block.dirt || block == Block.grass || block == Block.gravel || block == Block.stone);
	}

	private static boolean findVein(World world, int x, int y, int z, Block oreID, int oreMeta, ArrayList<ChunkCoordinates> veinBlocks) {
		ChunkCoordinates here = new ChunkCoordinates(x, y, z);

		// is this already on the list?
		if (veinBlocks.contains(here)) {
			return false;
		}

		// limit the vein size
		if (veinBlocks.size() >= TFConfig.magnetVeinMax) {
			return false;
		}

		// otherwise, check if we're still in the vein
		if (world.getBlock(x, y, z) == oreID && world.getBlockMetadata(x, y, z) == oreMeta) {
			veinBlocks.add(here);

			// recurse in 6 directions
			findVein(world, x + 1, y, z, oreID, oreMeta, veinBlocks);
			findVein(world, x - 1, y, z, oreID, oreMeta, veinBlocks);
			findVein(world, x, y + 1, z, oreID, oreMeta, veinBlocks);
			findVein(world, x, y - 1, z, oreID, oreMeta, veinBlocks);
			findVein(world, x, y, z + 1, oreID, oreMeta, veinBlocks);
			findVein(world, x, y, z - 1, oreID, oreMeta, veinBlocks);

			return true;
		} else {
			return false;
		}
	}

	/**
	 * MITE_BALANCE 7.7 whitelist (by block id), config magnet.whitelist (default coal, iron, copper, redstone, lapis).
	 * Silver / gold / mithril / adamantium / diamond / emerald / quartz and ore roots are NOT attracted unless the server adds them.
	 * Also used by the ore map.
	 */
	public static boolean isOre(Block blockID, int meta) {
		if (blockID == null) {
			return false;
		}
		return getWhitelist().contains(blockID);
	}

	private static synchronized java.util.Set<Block> getWhitelist() {
		String source = TFConfig.magnetWhitelist;
		if (source == null) {
			source = "";
		}
		if (!source.equals(whitelistSource)) {
			java.util.Set<Block> set = new java.util.HashSet<Block>();
			for (String token : source.split("[,;\\s]+")) {
				switch (token.trim().toLowerCase(java.util.Locale.ROOT)) {
					case "coal": set.add(Block.oreCoal); break;
					case "iron": set.add(Block.oreIron); break;
					case "copper": set.add(Block.oreCopper); break;
					case "silver": set.add(Block.oreSilver); break;
					case "gold": set.add(Block.oreGold); break;
					case "mithril": set.add(Block.oreMithril); break;
					case "adamantium": set.add(Block.oreAdamantium); break;
					case "diamond": set.add(Block.oreDiamond); break;
					case "emerald": set.add(Block.oreEmerald); break;
					case "quartz": set.add(Block.oreNetherQuartz); break;
					case "lapis": set.add(Block.oreLapis); break;
					case "redstone":
						set.add(Block.oreRedstone);
						set.add(Block.oreRedstoneGlowing);
						break;
					case "":
						break;
					default:
						TwilightForestMod.LOGGER.warn("Unknown ore '{}' in magnet.whitelist", token);
				}
			}
			whitelist = set;
			whitelistSource = source;
		}
		return whitelist;
	}

}
