package twilightforest.item;

import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.Material;

/** Giant Sword: mostly cosmetic, drawn with a 2x scaled icon. */
public class ItemTFGiantSword extends ItemSword {

	private GiantItemIcon giantIcon;

	public ItemTFGiantSword(int id, Material material) {
		super(id, material);
	}

	/** Giant tools are only repaired by combining two of them (MITE_BALANCE 6.7). */
	@Override
	public Item getRepairItem() {
		return null;
	}

	/** Giant icon source: the stone sword sprite. */
	@Override
	public void registerIcons(IconRegister register) {
		// stone-tool sprite shipped as twilightforest:giantSword (TF 1.7.10 used the stone icon too)
		Icon base = register.registerIcon("twilightforest:giantSword");
		this.itemIcon = base;
		if (base != null) {
			this.giantIcon = new GiantItemIcon(base, 0.0625F * 3F, 0.0625F * 5F);
		}
	}

	/** The enlarged icon (old render pass -1) used by the giant item renderer. */
	public Icon getGiantIcon() {
		return this.giantIcon;
	}
}
