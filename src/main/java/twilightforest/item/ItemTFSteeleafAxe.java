package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.ItemAxe;
import net.minecraft.Material;

/**
 * Steeleaf Axe. MITE port: the 1.7.10 ToolMaterial is replaced by a MITE Material
 * (TFMaterials.STEELEAF); the built-in creative-tab enchantment stack is dropped (MITE_BALANCE 7.1).
 */
public class ItemTFSteeleafAxe extends ItemAxe {

	public ItemTFSteeleafAxe(int id, Material material) {
		super(id, material);
	}

	/**
	 * Repair with steeleaf nuggets (MITE_BALANCE 6.7); replaces getIsRepairable.
	 */
	@Override
	public Item getRepairItem() {
		return TFItems.steeleafNugget;
	}
}
