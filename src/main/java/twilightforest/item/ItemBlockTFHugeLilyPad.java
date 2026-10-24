package twilightforest.item;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockFluid;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemLilyPad;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.block.TFBlocks;

public class ItemBlockTFHugeLilyPad extends ItemLilyPad {

	Random rand = new Random();

	public ItemBlockTFHugeLilyPad(Block block) {
		super(block);
	}

	/**
	 * Places a 2x2 huge lily pad on a grid of still water blocks (4 blocks, metadata 0-3 plus orientation in bits 2-3).
	 */
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
		RaycastCollision rc = player.getSelectedObject(partial_tick, true);
		if (rc == null || !rc.isBlock()) {
			return false;
		}

		World world = player.worldObj;
		ItemStack itemStack = player.getHeldItemStack();
		int x = rc.block_hit_x;
		int y = rc.block_hit_y;
		int z = rc.block_hit_z;

		// 2x2 lily pads go in a larger grid
		int bx = (x >> 1) << 1;
		int by = y;
		int bz = (z >> 1) << 1;

		if (!(this.canPlacePadOn(itemStack, world, player, bx, by, bz) && this.canPlacePadOn(itemStack, world, player, bx + 1, by, bz)
				&& this.canPlacePadOn(itemStack, world, player, bx, by, bz + 1) && this.canPlacePadOn(itemStack, world, player, bx + 1, by, bz + 1))) {
			return false;
		}

		if (player.onServer()) {
			// this seems like a difficult way to generate 2 pseudorandom bits
			rand.setSeed(8890919293L);
			rand.setSeed((bx * rand.nextLong()) ^ (bz * rand.nextLong()) ^ 8890919293L);
			int orient = rand.nextInt(4) << 2;

			int id = TFBlocks.hugeLilyPad.blockID;
			world.setBlock(bx, by + 1, bz, id, 0 | orient, 2);
			world.setBlock(bx + 1, by + 1, bz, id, 1 | orient, 2);
			world.setBlock(bx + 1, by + 1, bz + 1, id, 2 | orient, 2);
			world.setBlock(bx, by + 1, bz + 1, id, 3 | orient, 2);

			if (!player.inCreativeMode()) {
				player.convertOneOfHeldItem(null);
			}
		} else {
			player.swingArm();
		}

		return true;
	}

	public boolean canPlacePadOn(ItemStack itemStack, World world, EntityPlayer player, int x, int y, int z) {
		if (!world.canMineBlock(player, x, y, z)) {
			return false;
		}

		if (!player.canPlayerEdit(x, y, z, itemStack)) {
			return false;
		}

		if (world.isWithinTournamentSafeZone(x, y, z)) {
			return false;
		}

		return BlockFluid.isFullWaterBlock(world, x, y, z, false) && world.isAirBlock(x, y + 1, z);
	}

}
