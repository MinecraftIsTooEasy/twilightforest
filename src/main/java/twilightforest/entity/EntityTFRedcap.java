package twilightforest.entity;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIAvoidEntity;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAIWatchClosest;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityTNTPrimed;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFFeature;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAITFRedcapLightTNT;
import twilightforest.entity.ai.EntityAITFRedcapShy;


public class EntityTFRedcap extends EntityMob {

	/** MITE_BALANCE 9.3 / E12: lit TNT fuse in ticks. */
	public static final int TNT_FUSE = 60;
	/** MITE_BALANCE E12: blast radius against living entities (~11.7 damage at the centre). */
	public static final float TNT_SIZE_VS_LIVING = 1.1F;
	/** MITE_BALANCE E12 / F20: 0 radius against blocks (default, explosion.breakBlocks=false), redcap TNT does not destroy terrain. */
	public static final float TNT_SIZE_VS_BLOCKS = 0.0F;

	/** Block radius of redcap TNT: 0 unless explosion.breakBlocks is on, then the same as the creature radius (like the other TF explosions). */
	public static float tntSizeVsBlocks() {
		return twilightforest.TFConfig.explosionBreakBlocks ? TNT_SIZE_VS_LIVING : TNT_SIZE_VS_BLOCKS;
	}

	// equipment is purely cosmetic in MITE (MITE_BALANCE E11)
	public static ItemStack heldPick = new ItemStack(Item.pickaxeRustedIron, 1);
	public static ItemStack heldTNT = new ItemStack(Block.tnt, 1);
	public static ItemStack heldFlint = new ItemStack(Item.flintAndSteel, 1);

	private boolean shy;

	private int tntLeft = 0;

	public EntityTFRedcap(World world)
	{
		super(world);
		setSize(0.9F, 1.4F);

		shy = true;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityTNTPrimed.class, 2.0F, 1.0D, 2.0D));
		this.tasks.addTask(2, new EntityAITFRedcapShy(this, 1.0F));
		this.tasks.addTask(3, new EntityAITFRedcapLightTNT(this, 1.0F)); // light TNT
		this.tasks.addTask(5, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

		this.setCurrentItemOrArmor(0, heldPick);
		this.setCurrentItemOrArmor(1, new ItemStack(Item.bootsRustedIron));

		// cosmetic only: never drops free MITE gear
		this.equipmentDropChances[0] = 0.0F;
		this.equipmentDropChances[1] = 0.0F;
	}

	public EntityTFRedcap(World world, double x, double y, double z)
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
	 * Set monster attributes (MITE_BALANCE 9.3: HP 24, melee 11, XP 7)
	 */
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(24.0D); // max health
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.28D); // movement speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(11.0D); // attack damage
	}

	@Override
	public int getExperienceValue()
	{
		return 7;
	}

	@Override
	protected String getLivingSound()
	{
		return TwilightForestMod.ID + ":mob.redcap.redcap";
	}

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
		return Item.coal.itemID;
	}

	/**
	 * Coal 30% x1 (MITE_BALANCE 9.3).
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
		if (rand.nextInt(10) < 3)
		{
			this.dropItem(Item.coal.itemID, 1);
		}
	}

	public boolean isShy()
	{
		return shy && this.recentlyHit <= 0;
	}

	public int getTntLeft() {
		return tntLeft;
	}

	public void setTntLeft(int tntLeft) {
		this.tntLeft = tntLeft;
	}

	public ItemStack getPick()
	{
		return heldPick;
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("TNTLeft", this.getTntLeft());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		this.setTntLeft(par1NBTTagCompound.getInteger("TNTLeft"));
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
