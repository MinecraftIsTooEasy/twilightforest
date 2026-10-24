package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFMagicAttack;
import twilightforest.item.TFItems;

public class EntityTFDeathTome extends EntityMob {

	public EntityTFDeathTome(World par1World) {
		super(par1World);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAITFMagicAttack(this, 1.0F, 2, 60));
		this.tasks.addTask(5, new EntityAIWander(this, 1.0F));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	public int getAttackStrength(Entity par1Entity)
	{
		return 4;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled()
	{
		return true;
	}

	/**
	 * Set monster attributes (MITE_BALANCE 9.3: HP 24, bolt damage is handled by EntityTFTomeBolt)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(24.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.25D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(4.0D); // not used for melee, the book only shoots
	}

	@Override
	public int getExperienceValue() {
		return 8;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		TFParticles.spawn(this.worldObj, "enchantmenttable", this.posX + (this.rand.nextDouble() - 0.5D) * this.width, this.posY + this.rand.nextDouble() * (this.height - 0.75D) + 0.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
				0, 0.5, 0);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
	{
		if (damage.isFireDamage())
		{
			damage.scaleAmount(2.0F);
		}

		EntityDamageResult result = super.attackEntityFrom(damage);
		if (result != null && result.entityWasNegativelyAffected())
		{
			// we took damage
			if (this.rand.nextInt(2) == 0)
			{
				this.dropItemStack(new ItemStack(Item.paper), 1.0F);
			}
		}
		return result;
	}

	@Override
	protected int getDropItemId()
	{
		return Item.paper.itemID;
	}

	/**
	 * Drop 0-2 paper, a book and rarely a magic map focus
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
		int looting = damage_source.getLootingModifier();
		int num = this.rand.nextInt(3 + looting);

		for (int i = 0; i < num; ++i)
		{
			this.dropItem(Item.paper, 1);
		}

		if (this.rand.nextInt(5) - looting <= 0)
		{
			this.dropItem(Item.writableBook, 1);
		}
		else
		{
			this.dropItem(Item.book, 1);
		}

		if (recently_hit_by_player && !this.has_taken_massive_fall_damage && this.rand.nextInt(this.getBaseChanceOfRareDrop()) < 5 + looting * 2)
		{
			this.dropItem(TFItems.magicMapFocus, 1);
		}
	}

	/**
	 * Trigger achievement when killed
	 */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		if (par1DamageSource.getResponsibleEntity() instanceof EntityPlayer) {
			((EntityPlayer)par1DamageSource.getResponsibleEntity()).triggerAchievement(TFAchievementPage.twilightHunter);
		}
	}

}
