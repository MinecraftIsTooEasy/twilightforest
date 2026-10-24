package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.ItemPickaxe;
import net.minecraft.Material;
import twilightforest.block.BlockTFMazestone;

/**
 * Mazebreaker pick (iron material, mithril harvest level). MITE_BALANCE: x4 speed and x0.25 wear against mazestone; the
 * preset enchantments of the 1.7.10 creative stack are removed.
 */
public class ItemTFMazebreakerPick extends ItemPickaxe {

	/** Material.mithril setMinHarvestLevel(4); the field is protected and vanilla MITE has no getter. */
	private static final int MITHRIL_HARVEST_LEVEL = 4;

	public ItemTFMazebreakerPick(int id, Material material) {
		super(id, material);
		this.setTextureName("twilightforest:mazebreakerPick");
	}

	/** Mines like a mithril pickaxe (level 4); damage, durability and iron nugget repairs stay those of the iron material. */
	@Override
	public int getMaterialHarvestLevel() {
		return MITHRIL_HARVEST_LEVEL;
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
