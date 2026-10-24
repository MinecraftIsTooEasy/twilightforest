package twilightforest.item;

import java.util.List;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemBoots;
import net.minecraft.ItemCuirass;
import net.minecraft.ItemHelmet;
import net.minecraft.ItemLeggings;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;
import twilightforest.TwilightForestMod;

/**
 * Fiery armor. MITE port: the single 1.7.10 class (material, renderIndex, armorType) is split into four
 * part classes extending ItemHelmet/ItemCuirass/ItemLeggings/ItemBoots (MITE_BLOCK_ITEM P-14); shared logic is static here.
 * Use: new ItemTFFieryArmor.Helm(id, TFMaterials.FIERY), .Plate, .Legs, .Boots.
 * The built-in creative-tab enchantment stack was already commented out in 1.7.10; getSubItems is the default.
 */
public final class ItemTFFieryArmor {

	/** Whole-set protection from MITE_BALANCE 6.2 (fiery = 8, never above iron/ancient metal). */
	public static final int SET_PROTECTION = 8;

	private ItemTFFieryArmor() {
	}

	/** True for any of the four fiery armor part classes (used by the Fire Aura / fire-reduction hooks). */
	public static boolean isFieryArmor(Item item) {
		return item instanceof Helm || item instanceof Plate || item instanceof Legs || item instanceof Boots;
	}

	/**
	 * Replaces Forge getArmorTexture: path of the worn-armor texture for an armor slot (0 head, 1 chest, 2 legs, 3 boots).
	 * Consumed by the client armor-texture mixin (TFArmorRender).
	 */
	public static String getArmorTexture(int slot) {
		return slot == 2 ? TwilightForestMod.ARMOR_DIR + "fiery_2.png" : TwilightForestMod.ARMOR_DIR + "fiery_1.png";
	}

	/**
	 * Replaces Forge getArmorModel: custom biped model for an armor slot, as an Object so common code never names a client class.
	 * Consumed by the client armor-model mixin (TFArmorRender).
	 */
	public static Object getArmorModel(int armorSlot) {
		return TwilightForestMod.proxy.getFieryArmorModel(armorSlot);
	}

	static EnumRarity rarity() {
		return EnumRarity.epic;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void addTooltip(Item item, List info) {
		info.add(StatCollector.translateToLocal(item.getUnlocalizedName() + ".tooltip"));
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

		/** Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots). */
		@Override
		public Item getRepairItem() {
			return TFItems.fieryNugget;
		}

		/** Worn texture name; the texture must live at assets/minecraft/textures/models/armor/twilightforest_fiery_layer_N.png. */
		@Override
		public String getTextureFilenamePrefix() {
			return "twilightforest_fiery";
		}

		@Override
		public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
			super.addInformation(stack, player, info, extended, slot);
			addTooltip(this, info);
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

		/** Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots). */
		@Override
		public Item getRepairItem() {
			return TFItems.fieryNugget;
		}

		/** Worn texture name; the texture must live at assets/minecraft/textures/models/armor/twilightforest_fiery_layer_N.png. */
		@Override
		public String getTextureFilenamePrefix() {
			return "twilightforest_fiery";
		}

		@Override
		public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
			super.addInformation(stack, player, info, extended, slot);
			addTooltip(this, info);
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

		/** Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots). */
		@Override
		public Item getRepairItem() {
			return TFItems.fieryNugget;
		}

		/** Worn texture name; the texture must live at assets/minecraft/textures/models/armor/twilightforest_fiery_layer_N.png. */
		@Override
		public String getTextureFilenamePrefix() {
			return "twilightforest_fiery";
		}

		@Override
		public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
			super.addInformation(stack, player, info, extended, slot);
			addTooltip(this, info);
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

		/** Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots). */
		@Override
		public Item getRepairItem() {
			return TFItems.fieryNugget;
		}

		/** Worn texture name; the texture must live at assets/minecraft/textures/models/armor/twilightforest_fiery_layer_N.png. */
		@Override
		public String getTextureFilenamePrefix() {
			return "twilightforest_fiery";
		}

		@Override
		public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
			super.addInformation(stack, player, info, extended, slot);
			addTooltip(this, info);
		}
	}
}
