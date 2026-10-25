package twilightforest.item;

import java.util.List;

import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemBattleAxe;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Slot;
import net.minecraft.StatCollector;
import twilightforest.TFConfig;
import twilightforest.compat.ITFMeleeItem;

/**
 * Minotaur Axe (mithril material battle axe, MITE_BALANCE 6). The sprint charge bonus was dead code in 1.7.10 and is
 * disabled by default (config minotaurAxeSprintBonus).
 */
public class ItemTFMinotaurAxe extends ItemBattleAxe implements ITFMeleeItem {

	/** Balance: +4 instead of the original +7. */
	public static final int BONUS_CHARGING_DAMAGE = 4;
	private Entity bonusDamageEntity;
	private EntityPlayer bonusDamagePlayer;

	public ItemTFMinotaurAxe(int id, Material material) {
		super(id, material);
		this.setTextureName("twilightforest:minotaurAxe");
	}

	/** Sprinting hits remember the target so the next damage calculation adds the bonus. */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (TFConfig.minotaurAxeSprintBonus && player.isSprinting()) {
			this.bonusDamageEntity = entity;
			this.bonusDamagePlayer = player;
		}
		return false;
	}

	@Override
	public float modifyMeleeDamage(EntityPlayer player, Entity target, float rawDamage) {
		if (this.bonusDamagePlayer != null && this.bonusDamageEntity != null && target == this.bonusDamageEntity) {
			this.bonusDamagePlayer.onEnchantmentCritical(target);
			this.bonusDamagePlayer = null;
			this.bonusDamageEntity = null;
			return rawDamage + BONUS_CHARGING_DAMAGE;
		}
		return rawDamage;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended, Slot slot) {
		super.addInformation(stack, player, info, extended, slot);
		info.add(StatCollector.translateToLocal(getUnlocalizedName() + ".tooltip"));
	}
}
