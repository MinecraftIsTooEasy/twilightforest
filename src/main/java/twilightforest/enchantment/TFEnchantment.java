package twilightforest.enchantment;

import net.minecraft.CreativeTabs;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Enchantment;
import net.minecraft.EnumRarity;
import net.minecraft.InventoryPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import twilightforest.item.ItemTFFieryArmor;
import twilightforest.item.ItemTFYetiArmor;

/**
 * Static aura helpers. The 1.7.10 class was an Enchantment subclass only to hold these statics; it is never registered.
 */
public class TFEnchantment extends Enchantment {

	protected TFEnchantment(int id, EnumRarity rarity, int difficulty) {
		super(id, rarity, difficulty);
	}

	@Override
	public String getNameSuffix() {
		return "tfEnchantment";
	}

	@Override
	public boolean canEnchantItem(Item item) {
		return false;
	}

	@Override
	public boolean isOnCreativeTab(CreativeTabs tab) {
		return false;
	}

	/**
	 * Add up the number of fiery armor pieces the player is wearing, multiplied by 5
	 */
	public static int getFieryAuraLevel(InventoryPlayer inventory, DamageSource source) {
		int modifier = 0;

		for (int i = 0; i < inventory.armorInventory.length; i++) {
			ItemStack armor = inventory.armorInventory[i];

			if (armor != null && ItemTFFieryArmor.isFieryArmor(armor.getItem())) {
				modifier += 5;
			}
		}

		return modifier;
	}

	/** Fiery armor: each worn piece removes 20% of non-lava fire damage (balance 7, X8). */
	public static float reduceFireDamage(EntityLivingBase target, Damage damage, float amount) {
		if (amount <= 0.0F || !(target instanceof EntityPlayer player) || !damage.isFireDamage() || damage.isLavaDamage()) {
			return amount;
		}
		int pieces = getFieryAuraLevel(player.inventory, damage.getSource()) / 5;
		return pieces <= 0 ? amount : amount * Math.max(0.0F, 1.0F - 0.2F * pieces);
	}

	/**
	 * Add up the number of yeti armor pieces the player is wearing, 0-4
	 */
	public static int getChillAuraLevel(InventoryPlayer inventory, DamageSource source) {
		int modifier = 0;

		for (int i = 0; i < inventory.armorInventory.length; i++) {
			ItemStack armor = inventory.armorInventory[i];

			if (armor != null && ItemTFYetiArmor.isYetiArmor(armor.getItem())) {
				modifier++;
			}
		}

		return modifier;
	}
}
