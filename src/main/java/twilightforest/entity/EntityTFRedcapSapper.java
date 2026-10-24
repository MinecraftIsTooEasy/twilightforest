package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.entity.ai.EntityAITFRedcapPlantTNT;
import twilightforest.item.TFItems;

public class EntityTFRedcapSapper extends EntityTFRedcap {

	public EntityTFRedcapSapper(World world) {
		super(world);

		this.tasks.addTask(4, new EntityAITFRedcapPlantTNT(this)); // plant TNT

		this.setTntLeft(3);

		// cosmetic only (MITE_BALANCE 9.3: copper pick)
		this.setCurrentItemOrArmor(1, new ItemStack(TFItems.ironwoodBoots));
		this.setCurrentItemOrArmor(0, new ItemStack(Item.pickaxeCopper, 1));
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3: HP 30, melee 11, defense 1, XP 9)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(30.0D); // max health
	}

	@Override
	public int getExperienceValue()
	{
		return 9;
	}

	/**
	 * Natural defense 1 (replaces the original +2 armour points), not applied against magic.
	 */
	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return super.getNaturalDefense(damage_source) + (damage_source.bypassesMundaneArmor() ? 0.0F : 1.0F);
	}

	@Override
	public ItemStack getPick()
	{
		return new ItemStack(Item.pickaxeCopper);
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		Entity killer = par1DamageSource.getResponsibleEntity();
		if (killer instanceof EntityPlayer) {
			((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHunter);
			// are we in a level 2 hill?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hill2) {
				// award level 2 hill cheevo
				((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHill2);
			}
		}
	}
}
