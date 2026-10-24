package twilightforest.block;

import java.util.Random;

import net.minecraft.BlockBreakInfo;
import net.minecraft.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.item.TFItems;

public class BlockTFRipeTorchCluster extends BlockTFTrollRoot {

	protected BlockTFRipeTorchCluster(int id) {
		super(id);

		this.setTextureName(TwilightForestMod.ID + ":ripe_torch_cluster");
		this.setLightValue(1.0F);
	}

	/**
	 * Drops 4-8 torchberries (plus a fortune bonus); does not drop berries when sheared, the cluster itself drops instead.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		if (info.wasSilkHarvested() || info.wasSelfDropped()) {
			return super.dropBlockAsEntityItem(info);
		}

		// do not call normal harvest if the player is shearing
		if (info.wasHarvestedByPlayer()) {
			ItemStack held = info.getHarvesterItemStack();
			if (held != null && held.getItem() == net.minecraft.Item.shears) {
				return 0;
			}
		}

		int fortune = info.wasHarvestedByPlayer() ? info.getHarvesterFortune() : 0;
		return this.dropBlockAsEntityItem(info, TFItems.torchberries.itemID, 0, quantityDroppedWithBonus(fortune, info.world.rand), 1.0F);
	}

	/**
	 * Returns the usual quantity dropped by the block (4-8).
	 */
	public int quantityDropped(Random rand) {
		return 4 + rand.nextInt(5);
	}

	/**
	 * Returns the usual quantity dropped by the block plus a bonus of 1 to 'bonus' (inclusive).
	 */
	public int quantityDroppedWithBonus(int bonus, Random rand) {
		if (bonus > 0) {
			int j = rand.nextInt(bonus + 2) - 1;

			if (j < 0) {
				j = 0;
			}

			return this.quantityDropped(rand) * (j + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}
}
