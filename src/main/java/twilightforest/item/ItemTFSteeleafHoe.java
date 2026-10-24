package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.ItemHoe;
import net.minecraft.Material;

/**
 * Steeleaf Hoe. MITE port: the 1.7.10 ToolMaterial is replaced by a MITE Material
 * (TFMaterials.STEELEAF); the built-in creative-tab enchantment stack is dropped (MITE_BALANCE 7.1).
 */
public class ItemTFSteeleafHoe extends ItemHoe {

	public ItemTFSteeleafHoe(int id, Material material) {
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
