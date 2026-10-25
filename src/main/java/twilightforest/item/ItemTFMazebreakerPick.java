package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.ItemPickaxe;
import net.minecraft.Material;
import twilightforest.block.BlockTFMazestone;

/**
 * Mazebreaker pick (mithril material: stats, harvest level 4, enchantability). MITE_BALANCE: x4 speed and x0.25 wear against mazestone; the
 * preset enchantments of the 1.7.10 creative stack are removed.
 */
public class ItemTFMazebreakerPick extends ItemPickaxe {

	public ItemTFMazebreakerPick(int id, Material material) {
		super(id, material);
		this.setTextureName("twilightforest:mazebreakerPick");
	}

	@Override
	public float getBaseHarvestEfficiency(Block block) {
		float eff = super.getBaseHarvestEfficiency(block);
		return isMazeBlock(block) ? eff * 4.0F : eff;
	}

	@Override
	public float getBaseDecayRateForBreakingBlock(Block block) {
		float decay = super.getBaseDecayRateForBreakingBlock(block);
		return isMazeBlock(block) ? decay * 0.25F : decay;
	}

	private boolean isMazeBlock(Block block) {
		// TODO(mite-port): also cover the castle blocks once BlockTFCastleBlock is ported (u017 depends on this pick)
		return block instanceof BlockTFMazestone;
	}
}
