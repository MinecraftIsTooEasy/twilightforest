package twilightforest.entity.boss;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityZombie;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.World;
import twilightforest.TFAchievementPage;



public class EntityTFLichMinion extends EntityZombie {

	EntityTFLich master;

	public EntityTFLichMinion(World par1World) {
		super(par1World);
		this.master = null;
	}

	public EntityTFLichMinion(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	public EntityTFLichMinion(World par1World, EntityTFLich entityTFLich) {
		super(par1World);
		this.master = entityTFLich;
	}


	/**
	 * Called when we get attacked.
	 *
	 * If we are hit by a lich, still take the damage, but attack stronger and move faster.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage) {
		EntityLivingBase prevTarget = getAttackTarget();

		EntityDamageResult result = super.attackEntityFrom(damage);

		if (result != null && result.entityWasNegativelyAffected()) {
			if (damage.getSource().getResponsibleEntity() instanceof EntityTFLich) {
				// return to previous target
				setAttackTarget(prevTarget);
				setRevengeTarget(prevTarget);
				// but speed up
				addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 200, 4));
				addPotionEffect(new PotionEffect(Potion.damageBoost.id, 200, 1));
			}
		}
		return result;
	}

	/**
	 * Check and see if our master is dead.  If so, die
	 */
	@Override
	public void onLivingUpdate() {
		if (master == null) {
			findNewMaster();
		}
		// if we still don't have a master, or ours is dead, die.
		if (master == null || master.isDead) {
			this.setHealth(0);
		}
		super.onLivingUpdate();
	}

	@SuppressWarnings("unchecked")
	private void findNewMaster() {
		List<EntityTFLich> nearbyLiches = worldObj.getEntitiesWithinAABB(EntityTFLich.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(32.0D, 16.0D, 32.0D));

		for (EntityTFLich nearbyLich : nearbyLiches) {
			if (!nearbyLich.isShadowClone() && nearbyLich.wantsNewMinion(this)) {
				this.master = nearbyLich;

				// animate our new linkage!
				master.makeBlackMagicTrail(posX, posY + this.getEyeHeight(), posZ, master.posX, master.posY + master.getEyeHeight(), master.posZ);

				// become angry at our masters target (the Lich uses the old AI, getTarget() is the safe accessor)
				setAttackTarget(master.getTarget());

				// quit looking
				break;
			}
		}
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			((EntityPlayer) par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
		}
	}

	/**
	 * Boss minions never despawn on their own (MITE_BALANCE 9.4); they die with their master
	 */
	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	/**
	 * MITE_BALANCE: Lich minions give no XP
	 */
	@Override
	public int getExperienceValue()
	{
		return 0;
	}

	/**
	 * MITE_BALANCE: Lich minions give no drops
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
	}

	/**
	 * MITE_BALANCE: ... and do not drop their armor either
	 */
	@Override
	protected void dropEquipment(boolean recently_hit_by_player, int looting)
	{
	}


	@Override
	protected void addRandomArmor()
	{
		float[] equipChances = new float[] {0.0F, 0.25F, 0.75F, 1F};

		if (this.rand.nextFloat() < equipChances[2]) //this.worldObj.difficultySetting])
		{
			int var1 = this.rand.nextInt(2);
			float var2 = this.worldObj.difficultySetting == 3 ? 0.1F : 0.25F; // 3 = hard

			if (this.rand.nextFloat() < 0.07F)
			{
				++var1;
			}

			if (this.rand.nextFloat() < 0.07F)
			{
				++var1;
			}

			if (this.rand.nextFloat() < 0.07F)
			{
				++var1;
			}

			for (int var3 = 3; var3 >= 0; --var3)
			{
				ItemStack var4 = this.func_130225_q(var3);

				if (var3 < 3 && this.rand.nextFloat() < var2)
				{
					break;
				}

				if (var4 == null)
				{
					Item var5 = getArmorItemForSlot(var3 + 1, var1);

					if (var5 != null)
					{
						this.setCurrentItemOrArmor(var3 + 1, new ItemStack(var5));
					}
				}
			}
		}
	}

}
