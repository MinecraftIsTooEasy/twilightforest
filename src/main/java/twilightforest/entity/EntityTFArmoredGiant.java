package twilightforest.entity;

import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.World;
import twilightforest.item.TFItems;

public class EntityTFArmoredGiant extends EntityTFGiantMiner {

	public EntityTFArmoredGiant(World par1World) {
		super(par1World);

		// MITE has no stone sword, the copper one is the closest tier
		this.setCurrentItemOrArmor(0, new ItemStack(Item.swordCopper));
		this.setHelmet(new ItemStack(Item.helmetIron));
		this.setCuirass(new ItemStack(Item.plateIron));
		this.setLeggings(new ItemStack(Item.legsIron));
		this.setBoots(new ItemStack(Item.bootsIron));
	}

	/** MITE_BALANCE 9.3 T5: Armored Giant HP 170. */
	@Override
	protected double getBaseMaxHealth() {
		return 170.0D;
	}

	/** MITE_BALANCE 9.3 T5: Armored Giant melee 20. */
	@Override
	protected double getBaseAttackDamage() {
		return 20.0D;
	}

	/** MITE_BALANCE 9.3 T5: Armored Giant defense 5 (total, the iron armour counts towards it). */
	@Override
	protected float getBaseDefense() {
		return 5.0F;
	}

	/** MITE_BALANCE 9.3 T5: Armored Giant 35 XP. */
	@Override
	public int getExperienceValue() {
		return 35;
	}

	@Override
	protected int getDropItemId() {
		return TFItems.giantSword.itemID;
	}
}
