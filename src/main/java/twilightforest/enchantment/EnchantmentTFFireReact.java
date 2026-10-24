package twilightforest.enchantment;

import net.minecraft.CreativeTabs;
import net.minecraft.Enchantment;
import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemArmor;

/**
 * Marker enchantment, never registered or referenced in TF 1.7.10 (dead code). Ported so the class exists;
 * do NOT instantiate it without allocating a free id (the Enchantment ctor puts itself into enchantmentsList).
 */
public class EnchantmentTFFireReact extends Enchantment {

	public EnchantmentTFFireReact(int id) {
		super(id, EnumRarity.rare, 10);
	}

	@Override
	public String getNameSuffix() {
		return "tfFireReact";
	}

	@Override
	public boolean canEnchantItem(Item item) {
		return item instanceof ItemArmor;
	}

	@Override
	public boolean isOnCreativeTab(CreativeTabs tab) {
		return false;
	}
}
