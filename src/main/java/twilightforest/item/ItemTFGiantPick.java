package twilightforest.item;

import java.util.List;

import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;
import twilightforest.block.BlockTFGiantBlock;

/**
 * Giant Pickaxe. MITE port: stats come from the TF giantstone Material (MITE_BALANCE 6); the 64x giant block speed
 * becomes x8 with x0.1 tool wear. The n*n*n chain mining (giant.chain, default 2) is handled by the event code (TFEvents), not by the item.
 */
public class ItemTFGiantPick extends ItemPickaxe {

	private GiantItemIcon giantIcon;

	public ItemTFGiantPick(int id, Material material) {
		super(id, material);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.rare;
	}

	/** Giant tools are only repaired by combining two of them (MITE_BALANCE 6.7). */
	@Override
	public Item getRepairItem() {
		return null;
	}

	/** Giant icon source: the stone pickaxe sprite. */
	@Override
	public void registerIcons(IconRegister register) {
		// stone-tool sprite shipped as twilightforest:giantPick (TF 1.7.10 used the stone icon too)
		Icon base = register.registerIcon("twilightforest:giantPick");
		this.itemIcon = base;
		if (base != null) {
			this.giantIcon = new GiantItemIcon(base, 0.0625F * 7F, 0.0625F * 2F);
		}
	}

	/** The enlarged icon (old render pass -1) used by the giant item renderer. */
	public Icon getGiantIcon() {
		return this.giantIcon;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
		super.addInformation(stack, player, info, extended, slot);
		info.add(StatCollector.translateToLocal(getUnlocalizedName() + ".tooltip"));
	}

	/**
	 * Giant blocks ignore the harvest level (giantstone is tool level 2, giant obsidian needs 3), so the giant pickaxe can
	 * still open the troll vault as in 1.7.10; the material check stays, so it is no better than before against giant logs.
	 */
	@Override
	public boolean isEffectiveAgainstBlock(Block block, int metadata) {
		if (isGiantBlock(block)) {
			return this.materials_effective_against.contains(block.blockMaterial) || this.blocks_effective_against.contains(block);
		}
		return super.isEffectiveAgainstBlock(block, metadata);
	}

	/** x8 speed against giant blocks (original: x64, x4096 against giant obsidian). */
	@Override
	public float getBaseHarvestEfficiency(Block block) {
		float eff = super.getBaseHarvestEfficiency(block);
		return isGiantBlock(block) ? eff * 8.0F : eff;
	}

	/** x0.1 tool wear against giant blocks, otherwise 9.6 hardness would cost 960 durability per block. */
	@Override
	public float getBaseDecayRateForBreakingBlock(Block block) {
		float decay = super.getBaseDecayRateForBreakingBlock(block);
		return isGiantBlock(block) ? decay * 0.1F : decay;
	}

	private boolean isGiantBlock(Block block) {
		return block instanceof BlockTFGiantBlock;
	}
}
