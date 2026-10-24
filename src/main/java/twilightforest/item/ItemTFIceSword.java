package twilightforest.item;

import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.IBossDisplayData;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.Material;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import twilightforest.TwilightForestMod;
import twilightforest.compat.ITFMeleeItem;

/** Ice Sword: chills the target. MITE_BALANCE: Slowness II for 4 s (was III / 10 s), bosses are immune. */
public class ItemTFIceSword extends ItemSword implements ITFMeleeItem {

	public ItemTFIceSword(int id, Material material) {
		super(id, material);
		this.setTextureName("twilightforest:iceSword");
	}

	/** Only repaired by combining two swords. */
	@Override
	public Item getRepairItem() {
		// TODO(mite-port): original repaired with packed ice; MITE has no packed ice and the repair item must be a material item
		return null;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		boolean result = super.hitEntity(stack, target, attacker);
		if (result && !(target instanceof IBossDisplayData)) {
			int chillLevel = 1; // amplifier 1 = Slowness II
			// per-target cooldown of 60 ticks: only re-apply once the previous 80 tick effect has run down to <= 20
			PotionEffect active = target.getActivePotionEffect(Potion.moveSlowdown);
			if (active != null && active.getAmplifier() >= chillLevel && active.getDuration() > 20) {
				return result;
			}
			target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20 * 4, chillLevel, true));
		}
		return result;
	}

	/** Client side: snow animation. Never cancels the attack. */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (player.worldObj.isRemote) {
			for (int i = 0; i < 20; ++i) {
				double px = entity.posX + itemRand.nextFloat() * entity.width * 2.0F - entity.width;
				double py = entity.posY + itemRand.nextFloat() * entity.height;
				double pz = entity.posZ + itemRand.nextFloat() * entity.width * 2.0F - entity.width;
				TwilightForestMod.proxy.spawnParticle(entity.worldObj, "snowstuff", px, py, pz, 0, 0, 0);
			}
		}
		return false;
	}
}
