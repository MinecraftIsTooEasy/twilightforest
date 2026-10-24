package twilightforest.item;

import java.util.List;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;

/**
 * Fiery Sword. MITE port: 1.7.10 ToolMaterial replaced by a MITE Material (TFMaterials.FIERY). Balance (MITE_BALANCE 7.1):
 * ignites the target for 4 s (was 15) and the built-in Fire Aspect stack is dropped.
 */
public class ItemTFFierySword extends ItemSword {

	/** Seconds the target burns (MITE_BALANCE 7.1). */
	public static final int IGNITE_SECONDS = 4;

	public ItemTFFierySword(int id, Material material) {
		super(id, material);
	}

	/**
	 * Return an item rarity from EnumRarity
	 *
	 * This is automatically rare
	 */
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.rare;
	}

	/**
	 * Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots).
	 */
	@Override
	public Item getRepairItem() {
		return TFItems.fieryNugget;
	}

	/**
	 * Server only in MITE (the 1.7.10 client-side flame animation is dropped; the target burning shows its own fire).
	 */
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		boolean result = super.hitEntity(stack, target, attacker);

		if (result && target.isHarmedByFire()) {
			target.setFire(IGNITE_SECONDS);
		}

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
		super.addInformation(stack, player, info, extended, slot);
		info.add(StatCollector.translateToLocal(getUnlocalizedName() + ".tooltip"));
	}
}
