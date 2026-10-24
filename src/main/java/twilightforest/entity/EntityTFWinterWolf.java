package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.entity.ai.EntityAITFBreathAttack;
import twilightforest.item.TFItems;

public class EntityTFWinterWolf extends EntityTFHostileWolf  implements IBreathAttacker {

	/** DataWatcher slot of the breath flag; allocated in entityInit via getNextAvailableId (MITE's EntityWolf already uses 21 and 22). */
	private int data_object_id_breath; // no initializer on purpose: entityInit runs inside the super constructor
    public static final int BREATH_DURATION = 10;
    public static final int BREATH_DAMAGE = 4;
    /** Ticks between two breath damage applications (the AI calls doBreathAttack every tick). */
    private static final int BREATH_HIT_INTERVAL = 10;

	public EntityTFWinterWolf(World world) {
		super(world);
        this.setSize(1.4F, 1.9F);
        
        this.tasks.getTaskEntries().clear();
        this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAITFBreathAttack(this, 1.0F, 5F, 30, 0.1F));
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
 
        this.targetTasks.getTaskEntries().clear();
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	/**
	 * Set monster attributes (MITE_BALANCE: T4 mob, 40 HP, 14 damage)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 40.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 14.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 15;
	}
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        this.data_object_id_breath = this.dataWatcher.addObject(this.dataWatcher.getNextAvailableId(), new Byte((byte)0));
    }

    /**
     * Returns the amount of damage a mob should deal.
     */
    public int getAttackStrength(Entity par1Entity)
    {
        return (int) this.getEntityAttributeValue(SharedMonsterAttributes.attackDamage);
    }
    
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
	public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    	
    	// when breathing fire, spew particles
    	if (isBreathing())
    	{
    		Vec3 look = this.getLookVec();

    		double dist = 0.5;
    		double px = this.posX + look.xCoord * dist;
    		double py = this.posY + 1.25 + look.yCoord * dist;
    		double pz = this.posZ + look.zCoord * dist;

    		for (int i = 0; i < 10; i++)
    		{
    			double dx = look.xCoord;
    			double dy = look.yCoord;
    			double dz = look.zCoord;

    			double spread = 5 + this.getRNG().nextDouble() * 2.5;
    			double velocity = 3.0 + this.getRNG().nextDouble() * 0.15;

    			// spread flame
    			dx += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dy += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dz += this.getRNG().nextGaussian() * 0.007499999832361937D * spread;
    			dx *= velocity;
    			dy *= velocity;
    			dz *= velocity;

    			TFParticles.spawn(this.worldObj, "snowstuff", px, py, pz, dx, dy, dz);
    		}
    		
			playBreathSound();
    	}

    }
    
	public void playBreathSound() {
		worldObj.playSoundEffect(this.posX + 0.5, this.posY + 0.5, this.posZ + 0.5, "mob.ghast.fireball", rand.nextFloat() * 0.5F, rand.nextFloat() * 0.5F);
	}

	/**
     * Gets the pitch of living sounds in living entities.
     */
	@Override
    protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.6F;
    }

	@Override
	public boolean isBreathing() {
        return this.getDataWatcher().getWatchableObjectByte(data_object_id_breath) == 1;

	}

	@Override
	public void setBreathing(boolean flag) {
        this.getDataWatcher().updateObject(data_object_id_breath, Byte.valueOf((byte)(flag ? 1 : 0)));
	}

	/**
	 * MITE_BALANCE: ice breath, 4 magic damage + Slowness I for 2 s.
	 * The original 1.7.10 body was commented out (fire copy-paste), so this is new behaviour.
	 */
	@Override
	public void doBreathAttack(Entity target) {
		if (this.worldObj.isRemote || this.ticksExisted % BREATH_HIT_INTERVAL != 0) {
			return;
		}
		if (TFDamage.hurtByMagic(target, this, this, BREATH_DAMAGE) && target instanceof EntityLivingBase) {
			((EntityLivingBase) target).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 2 * 20, 0));
		}
	}
	
    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
	@Override
	protected boolean isValidLightLevel() {
        int x = MathHelper.floor_double(this.posX);
        int z = MathHelper.floor_double(this.posZ);
        
		if (worldObj.getBiomeGenForCoords(x, z) == TFBiomeBase.tfSnow) {
			return true;
		} else {
			return super.isValidLightLevel();
		}
	}
	
	@Override
	protected int getDropItemId()
    {
        return TFItems.arcticFur.itemID;
    }

	/**
	 * Drop 0-2 arctic fur (+ looting); no leather like the plain MITE wolf.
	 */
	@Override
	protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
	{
		if (!recently_hit_by_player)
		{
			return;
		}
		int looting = damage_source == null ? 0 : damage_source.getLootingModifier();
		int count = this.rand.nextInt(3) + (looting > 0 ? this.rand.nextInt(1 + looting) : 0);
		for (int i = 0; i < count; ++i)
		{
			this.dropItem(TFItems.arcticFur.itemID, 1);
		}
	}

}
