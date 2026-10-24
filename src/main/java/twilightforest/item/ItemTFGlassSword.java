package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ItemSword;
import net.minecraft.Material;
import twilightforest.TFConfig;
import twilightforest.compat.ITFMeleeItem;
import twilightforest.compat.TFParticles;

/** Glass Sword: shatters on the first hit (damage items.glassSwordDamage, durability 1). */
public class ItemTFGlassSword extends ItemSword implements ITFMeleeItem {

	public ItemTFGlassSword(int id, Material material) {
		super(id, material);
		this.setTextureName("twilightforest:glassSword");
	}

	/**
	 * The configured damage replaces sword base + material bonus; ItemTool caches it in its constructor (config is read before
	 * the items are built). Overriding also keeps the compat.profile weapon factor off this sword.
	 */
	@Override
	public float getCombinedDamageVsEntity() {
		return (float) TFConfig.itemsGlassSwordDamage;
	}

	/** Cannot be repaired. */
	@Override
	public Item getRepairItem() {
		return null;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		boolean result = super.hitEntity(stack, target, attacker);
		if (result) {
			stack.tryDamageItem(DamageSource.generic, 1000, attacker);
		}
		return result;
	}

	/** Client side: glass shatter animation. Never cancels the attack. */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (player.worldObj.isRemote) {
			for (int i = 0; i < 20; ++i) {
				double px = entity.posX + itemRand.nextFloat() * entity.width * 2.0F - entity.width;
				double py = entity.posY + itemRand.nextFloat() * entity.height;
				double pz = entity.posZ + itemRand.nextFloat() * entity.width * 2.0F - entity.width;
				TFParticles.spawnBlockCrack(entity.worldObj, Block.glass.blockID, 0, px, py, pz, 0, 0, 0);
			}
			player.playSound(Block.glass.stepSound.getBreakSound(), 1F, 0.5F);
		}
		return false;
	}
}
