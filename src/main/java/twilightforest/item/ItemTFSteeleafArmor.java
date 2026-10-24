package twilightforest.item;

import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemBoots;
import net.minecraft.ItemCuirass;
import net.minecraft.ItemHelmet;
import net.minecraft.ItemLeggings;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

/**
 * Steeleaf armor. MITE port: the single 1.7.10 class (material, renderIndex, armorType) is split into four
 * part classes extending ItemHelmet/ItemCuirass/ItemLeggings/ItemBoots (MITE_BLOCK_ITEM P-14); shared logic is static here.
 * Use: new ItemTFSteeleafArmor.Helm(id, TFMaterials.STEELEAF), .Plate, .Legs, .Boots.
 */
public final class ItemTFSteeleafArmor {

	/** Whole-set protection from MITE_BALANCE 6.2 (steeleaf = 8, never above iron/ancient metal). */
	public static final int SET_PROTECTION = 8;

	private ItemTFSteeleafArmor() {
	}

	/**
	 * Replaces Forge getArmorTexture: path of the worn-armor texture for an armor slot (0 head, 1 chest, 2 legs, 3 boots).
	 * Consumed by the client armor-texture mixin (TFArmorRender).
	 */
	public static String getArmorTexture(int slot) {
		return slot == 2 ? TwilightForestMod.ARMOR_DIR + "steeleaf_2.png" : TwilightForestMod.ARMOR_DIR + "steeleaf_1.png";
	}

	static EnumRarity rarity() {
		return EnumRarity.uncommon;
	}

	public static class Helm extends ItemHelmet {
		public Helm(int id, Material material) {
			super(id, material, false);
		}

		@Override
		public EnumRarity getRarity(ItemStack stack) {
			return rarity();
		}

		@Override
		public int getMaterialProtection() {
			return SET_PROTECTION;
		}

		/** Repair with steeleaf nuggets (MITE_BALANCE 6.7); replaces getIsRepairable. */
		@Override
		public Item getRepairItem() {
			return TFItems.steeleafNugget;
		}
	}

	public static class Plate extends ItemCuirass {
		public Plate(int id, Material material) {
			super(id, material, false);
		}

		@Override
		public EnumRarity getRarity(ItemStack stack) {
			return rarity();
		}

		@Override
		public int getMaterialProtection() {
			return SET_PROTECTION;
		}

		@Override
		public Item getRepairItem() {
			return TFItems.steeleafNugget;
		}
	}

	public static class Legs extends ItemLeggings {
		public Legs(int id, Material material) {
			super(id, material, false);
		}

		@Override
		public EnumRarity getRarity(ItemStack stack) {
			return rarity();
		}

		@Override
		public int getMaterialProtection() {
			return SET_PROTECTION;
		}

		@Override
		public Item getRepairItem() {
			return TFItems.steeleafNugget;
		}
	}

	public static class Boots extends ItemBoots {
		public Boots(int id, Material material) {
			super(id, material, false);
		}

		@Override
		public EnumRarity getRarity(ItemStack stack) {
			return rarity();
		}

		@Override
		public int getMaterialProtection() {
			return SET_PROTECTION;
		}

		@Override
		public Item getRepairItem() {
			return TFItems.steeleafNugget;
		}
	}
}
