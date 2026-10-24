package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAITFChargeAttack;
import twilightforest.item.TFItems;


// TODO(mite-port): Boggard is not registered in TF 1.7.10 either (registration commented out); stats below are a T1 guess in line with MITE_BALANCE 9.3 Redcap/Kobold.
public class EntityTFBoggard extends EntityMob {

	private boolean shy;

	public EntityTFBoggard(World world)
	{
		super(world);
		setSize(0.8F, 1.1F);

		shy = true;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAITFChargeAttack(this, 2.0F));
		this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false));
	}

	public EntityTFBoggard(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
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
	 * Set monster attributes
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(9.0D); // attack damage
	}

	@Override
	public int getExperienceValue()
	{
		return 6;
	}

	@Override
	protected String getLivingSound()
	{
		return TwilightForestMod.ID + ":mob.redcap.redcap";
	}

	// 1.7.10 used the non-existent sounds mob.redcap.redcaphurt / redcapdie; mapped to the real redcap sounds
	@Override
	protected String getHurtSound()
	{
		return TwilightForestMod.ID + ":mob.redcap.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return TwilightForestMod.ID + ":mob.redcap.die";
	}

	@Override
	protected int getDropItemId()
	{
		return Item.bootsRustedIron.itemID;
	}

	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
		if (rand.nextInt(100) == 0)
		{
			this.dropItem(TFItems.mazeMapFocus, 1);
		}
		if (rand.nextInt(6) == 0)
		{
			this.dropItem(Item.bootsRustedIron.itemID, 1);
		}
		if (rand.nextInt(9) == 0)
		{
			this.dropItem(Item.pickaxeRustedIron.itemID, 1);
		}
	}

	public boolean isShy() {
		return shy && this.recentlyHit <= 0;
	}

	/**
	 * Fairly straightforward.  Returns true in a 120 degree arc in front of the player's view.
	 */
	public boolean isTargetLookingAtMe() {
		EntityLivingBase target = this.getAttackTarget();
		if (target == null) {
			return false;
		}

		// find angle of approach
		double dx = posX - target.posX;
		double dz = posZ - target.posZ;
		float angle = (float)((Math.atan2(dz, dx) * 180D) / 3.1415927410125732D) - 90F;

		float difference = MathHelper.abs((target.rotationYaw - angle) % 360);

		return difference < 60 || difference > 300;
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
			// are we in a level 1 hill?
			int chunkX = MathHelper.floor_double(posX) >> 4;
			int chunkZ = MathHelper.floor_double(posZ) >> 4;
			if (TFFeature.getNearestFeature(chunkX, chunkZ, worldObj) == TFFeature.hill1) {
				// award level 1 hill cheevo
				((EntityPlayer)killer).triggerAchievement(TFAchievementPage.twilightHill1);
			}
		}
	}

}
