package twilightforest.item;

import java.util.List;
import net.minecraft.Block;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.Item;
import net.minecraft.ItemPickaxe;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;

/**
 * Fiery Pickaxe. MITE port: 1.7.10 ToolMaterial replaced by a MITE Material (TFMaterials.FIERY).
 * Balance (MITE_BALANCE 7.1/12): the auto-smelt flame preview in onBlockDestroyed is deleted;
 * auto-smelt itself is TFEvents.onBlockDrop (fieryPick.smelt, default heat2); ignite duration is 4 s; obsidian stays harvestable.
 */
public class ItemTFFieryPick extends ItemPickaxe {

	protected ItemTFFieryPick(int id, Material material) {
		super(id, material);
	}

	/**
	 * Server only in MITE (the 1.7.10 client-side flame animation is dropped).
	 */
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		boolean result = super.hitEntity(stack, target, attacker);

		if (result && target.isHarmedByFire()) {
			target.setFire(ItemTFFierySword.IGNITE_SECONDS);
		}

		return result;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
		super.addInformation(stack, player, info, extended, slot);
		info.add(StatCollector.translateToLocal(getUnlocalizedName() + ".tooltip"));
	}

	/**
	 * Repair with fiery nuggets (MITE_BALANCE 6.7); replaces getIsRepairable (fiery ingots).
	 */
	@Override
	public Item getRepairItem() {
		return TFItems.fieryNugget;
	}

	/**
	 * Replaces func_150897_b (canHarvestBlock): obsidian is always effective, subject to the material harvest level.
	 */
	@Override
	public boolean isEffectiveAgainstBlock(Block block, int metadata) {
		return block == Block.obsidian ? this.getMaterialHarvestLevel() >= block.getMinHarvestLevel(metadata) : super.isEffectiveAgainstBlock(block, metadata);
	}
}
