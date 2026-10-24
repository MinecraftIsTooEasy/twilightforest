package twilightforest.entity.boss;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EnchantmentDamage;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityFlying;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.IMob;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagDouble;
import net.minecraft.NBTTagList;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFProgress;
import twilightforest.TFFeature;
import twilightforest.TFTreasure;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.item.TFItems;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

public class EntityTFKnightPhantom extends EntityFlying implements IMob 
{
	
	private static final float CIRCLE_SMALL_RADIUS = 2.5F;
	private static final float CIRCLE_LARGE_RADIUS = 8.5F;
	private static final int FLAG_CHARGING = 17;
	int number;
	int ticksProgress;
	Formation currentFormation;
	
    private ChunkCoordinates homePosition = new ChunkCoordinates(0, 0, 0);
    /** If -1 there is no maximum distance */
    private float maximumHomeDistance = -1.0F;
    
    private int chargePosX;
    private int chargePosY;
    private int chargePosZ;
	
	public enum Formation { HOVER, LARGE_CLOCKWISE, SMALL_CLOCKWISE, LARGE_ANTICLOCKWISE, SMALL_ANTICLOCKWISE, CHARGE_PLUSX, CHARGE_MINUSX, CHARGE_PLUSZ, CHARGE_MINUSZ, WAITING_FOR_LEADER, ATTACK_PLAYER_START,  ATTACK_PLAYER_ATTACK};

	public EntityTFKnightPhantom(World par1World) {
		super(par1World);

		this.setSize(1.5F, 3.0F);

		this.noClip = true;
        // immune to fire: see isHarmedByFire()/isHarmedByLava()
        
		this.currentFormation = Formation.HOVER;
		
		// experience: see getExperienceValue()
		
        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlySword));
        this.setCurrentItemOrArmor(3, new ItemStack(TFItems.phantomPlate));
        this.setCurrentItemOrArmor(4, new ItemStack(TFItems.phantomHelm));

		
	}
 
	
	@Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(FLAG_CHARGING, Byte.valueOf((byte)0));
    }

	/**
	 * Set monster attributes
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(50.0D); // max health (MITE_BALANCE 9.3)
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.5D); // movement speed

        // need to initialize damage since we're not an EntityMob (MITE_BALANCE 9.3: melee 12, charge 14)
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 12.0D); // attack damage
    }

    /**
     * MITE_BALANCE E2: AI mode is enabled so the movement speed attribute and getTarget() behave; the formation
     * movement is driven from updateAITasks().
     */
    @Override
    protected boolean isAIEnabled()
    {
        return true;
    }

    /**
     * Experience (MITE_BALANCE 9.3: 30 each, x0.25 on repeat kills): handed out to the credited players in onDeath, so no orbs.
     */
    @Override
    public int getExperienceValue()
    {
        return 0;
    }

    @Override
    public boolean isHarmedByFire()
    {
        return false;
    }

    @Override
    public boolean isHarmedByLava()
    {
        return false;
    }

    @Override
    public boolean canSpawnInShallowWater()
    {
        return true;
    }

    @Override
    public boolean fleesWhenBadlyWounded()
    {
        return false;
    }

    /**
     * Natural defense (MITE_BALANCE 9.3: 3)
     */
    @Override
    public float getNaturalDefense(DamageSource damage_source)
    {
        return 3.0F;
    }
	
    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
	protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public EntityDamageResult attackEntityFrom(Damage damage)
    {
        if (this.isEntityInvulnerable())
        {
            return null;
        }
        else if (damage.getSource() == DamageSource.inWall)
        {
            return null;
        }
        else
        {
            return super.attackEntityFrom(damage);
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate()
    {
    	super.onLivingUpdate();
    	
    	if (this.isChargingAtPlayer())
    	{
	    	// make particles
	        for (int i = 0; i < 4; ++i)
	        {
	        	net.minecraft.Item particleItem = this.rand.nextBoolean() ? TFItems.phantomHelm : TFItems.knightlySword;

		    TFParticles.spawnItemCrack(worldObj, particleItem.itemID, 0, this.posX + (this.rand.nextFloat() * this.rand.nextFloat() - 0.5D) * this.width, this.posY + this.rand.nextFloat() * (this.height - 0.75D) + 0.5D, this.posZ + (this.rand.nextFloat() * this.rand.nextFloat() - 0.5D) * this.width, 0, -0.1, 0);
		    TFParticles.spawn(worldObj, "smoke", this.posX + (this.rand.nextFloat() * this.rand.nextFloat() - 0.5D) * this.width, this.posY + this.rand.nextFloat() * (this.height - 0.75D) + 0.5D, this.posZ + (this.rand.nextFloat() * this.rand.nextFloat() - 0.5D) * this.width, 0, 0.1, 0);
	        }
    	}
    }
    
    /**
     * handles entity death timer, experience orb and particle creation
     */
	@Override
    protected void onDeathUpdate()
    {
		super.onDeathUpdate();
		
        for (int i = 0; i < 20; ++i)
        {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            TFParticles.spawn(this.worldObj, "explode", this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
        }


    }
	
    /**
     * Trigger achievement when killed
     */
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);

		// progress credit: the flag is granted by the last phantom, every phantom pays its own 30 xp
		TFProgress.KillResult killResult = null;
		if (!worldObj.isRemote) {
			boolean last = getNearbyKnights().size() <= 1;
			if (last) {
				killResult = TFProgress.onBossKilled(this, TFProgress.Flag.KNIGHTS, TFAchievementPage.twilightHunter);
			} else {
				List<EntityPlayer> credited = TFProgress.creditedPlayers(this);
				killResult = new TFProgress.KillResult(credited, TFProgress.isRepeat(credited, TFProgress.Flag.KNIGHTS));
			}
			killResult.awardXp(30);
		}
		
		// mark the stronghold as defeated
		if (!worldObj.isRemote && worldObj.provider instanceof WorldProviderTwilightForest) {
			int dx = getHomePosition().posX;
			int dy = getHomePosition().posY;
			int dz = getHomePosition().posZ;
			
			ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest)worldObj.provider).getChunkProvider();
			TFFeature nearbyFeature = ((TFWorldChunkManager)worldObj.provider.worldChunkMgr).getFeatureAt(dx, dz, worldObj);
			
			if (nearbyFeature == TFFeature.tfStronghold) {
				chunkProvider.setStructureConquered(dx, dy, dz, true);
			}
		}
		
        
        // make treasure for killing the last knight
        if (!this.worldObj.isRemote) {
        	// am I the last one?!?!
    		List<EntityTFKnightPhantom> nearbyKnights = getNearbyKnights();
        	if (nearbyKnights.size() <= 1)
        	{
        		// 	make a treasure!'
        		//System.out.println("I think I'm the last one!");
        		this.makeATreasure(killResult != null && killResult.repeat());
        	}
        }

	}

	/**
	 * Make a treasure for when we're dead
	 */
	private void makeATreasure(boolean repeat) {
		int cx;
		int cy;
		int cz;
		if (this.getHomePosition().posX != 0) {
			// if we have a proper home position, generate the treasure there
			cx = getHomePosition().posX;
			cy = getHomePosition().posY - 1;
			cz = getHomePosition().posZ;
		} else {
			// if not, spawn it right where we are
			cx = MathHelper.floor_double(this.lastTickPosX);
			cy = MathHelper.floor_double(this.lastTickPosY);
			cz = MathHelper.floor_double(this.lastTickPosZ);
		}
		TFTreasure.stronghold_boss.generate(worldObj, null, cx, cy, cz);
		if (repeat) {
			// MITE_BALANCE 9.5: the chest of a repeat kill holds half the items
			TFProgress.halveChest(worldObj, cx, cy, cz);
		}
	}


	/**
     * Formation-based AI
     */
    @Override
    protected void updateAITasks()
    {
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting == 0)
        {
            this.setDead();
        }

        this.tryDespawnEntity();
        
        this.noClip = this.ticksProgress % 20 != 0;
        
        ticksProgress++;
        
        if (ticksProgress >= getMaxTicksForFormation())
        {
        	switchToNextFormation();
        }

        float seekRange = this.isChargingAtPlayer() ? 24 : 9;

        EntityPlayer target = this.getClosestVulnerablePlayer(seekRange);
        //EntityPlayer target = this.worldObj.getClosestPlayerToEntity(this, seekRange);

        if (target != null && this.currentFormation == Formation.ATTACK_PLAYER_START)
        {
        	int targetX = MathHelper.floor_double(target.lastTickPosX);
        	int targetY = MathHelper.floor_double(target.lastTickPosY);
        	int targetZ = MathHelper.floor_double(target.lastTickPosZ);

        	if (this.isWithinHomeArea(targetX, targetY, targetZ))
        	{
        		this.chargePosX = targetX;
        		this.chargePosY = targetY;
        		this.chargePosZ = targetZ;
        	}
        	else
        	{
        		this.chargePosX = this.getHomePosition().posX;
        		this.chargePosY = this.getHomePosition().posY;
        		this.chargePosZ = this.getHomePosition().posZ;
        	}
        }

        Vec3 dest = this.getDestination();

//        if (this.getNumber() == 0)
//        {
//        	System.out.printf("Knight Phantom %d moving towards %f, %f, %f.  Is in formation %s, progress %d.\n", this.getNumber(), dest.xCoord, dest.yCoord, dest.zCoord, this.currentFormation, ticksProgress);
//        	System.out.printf("Knight Phantom %d at position %f, %f, %f.\n", this.getNumber(), this.posX, this.posY, this.posZ);
//        }
        
        double moveX = dest.xCoord - this.posX;
        double moveY = dest.yCoord - this.posY;
        double moveZ = dest.zCoord - this.posZ;
        
        double factor = moveX * moveX + moveY * moveY + moveZ * moveZ;
        
        factor = (double)MathHelper.sqrt_double(factor);

        double speed = 0.1D;//this.isChargingAtPlayer() ? 0.1D : 0.05D;
        
		this.motionX += moveX / factor * speed;
        this.motionY += moveY / factor * speed;
        this.motionZ += moveZ / factor * speed;
        
        if (target != null)
        {
        	this.faceEntity(target, 10.0F, 500.0F);
        	
            if (target.isEntityAlive())
            {
                float f1 = target.getDistanceToEntity(this);

                if (this.canSeeEntity(target))
                {
                    this.attackEntity(target, f1);
                }
            }
            
            // launch axe at the appropriate time in our routine
            if (isAxeKnight() && this.currentFormation == Formation.ATTACK_PLAYER_ATTACK && (this.ticksProgress % 4) == 0)
            {
            	this.launchAxeAt(target);
            }
            
            // same for picks
            if (isPickKnight() && this.currentFormation == Formation.ATTACK_PLAYER_ATTACK && (this.ticksProgress % 4) == 0)
            {
            	this.launchPicks();
            }

        }
        
        
        //this.setPosition(dest.xCoord, dest.yCoord, dest.zCoord);
    }
    
    
    /**
     * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
     */
    protected void attackEntity(Entity par1Entity, float par2)
    {
    	
        if (this.attackTime <= 0 && par2 < 2.0F && par1Entity.boundingBox.maxY > this.boundingBox.minY && par1Entity.boundingBox.minY < this.boundingBox.maxY)
        {
            this.attackTime = 20;
            this.attackEntityAsMob(par1Entity);

        }
    }

    /**
     * Copy of EntityMob attack
     */
    @Override
    public EntityDamageResult attackEntityAsMob(Entity par1Entity)
    {
        Damage damage = new Damage(DamageSource.causeMobDamage(this), getAttackDamage());
        int i = 0;

        if (par1Entity instanceof EntityLivingBase)
        {
            damage.addAmount(EnchantmentDamage.getDamageModifiers(this.getHeldItemStack(), (EntityLivingBase)par1Entity));
            i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)par1Entity);
        }

        int j = EnchantmentHelper.getFireAspectModifier(this);

        EntityDamageResult result = par1Entity.attackEntityFrom(damage.setFireAspect(j > 0));

        if (result != null && result.entityWasNegativelyAffected())
        {
            if (i > 0)
            {
                par1Entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            if (j > 0)
            {
                par1Entity.setFire(j * 4);
            }
        }

        return result;
    }


    /**
     * How much damage do we deal, per attack?
     * @return
     */
	private float getAttackDamage() {
		float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();

		if (this.isChargingAtPlayer())
		{
			damage += 2; // MITE_BALANCE 9.3: charge hit 14 (melee 12)
		}
		
		return damage;
	}

	/**
     * Fires an axe at the target
     * @param targetedEntity
     */
	protected void launchAxeAt(Entity targetedEntity) {
		float bodyFacingAngle = ((renderYawOffset * 3.141593F) / 180F);
		double sx = posX + (MathHelper.cos(bodyFacingAngle) * 1);
		double sy = posY + (height * 0.82);
		double sz = posZ + (MathHelper.sin(bodyFacingAngle) * 1);
		
		double tx = targetedEntity.posX - sx;
		double ty = (targetedEntity.boundingBox.minY + (double)(targetedEntity.height / 2.0F)) - (posY + height / 2.0F);
		double tz = targetedEntity.posZ - sz;
		
		worldObj.playSoundAtEntity(this, "random.bow", getSoundVolume("random.bow"), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 0.4F);
		EntityTFThrownAxe projectile = new EntityTFThrownAxe(worldObj, this);
		
		float speed = 0.75F;

		projectile.setThrowableHeading(tx, ty, tz, speed, 1.0F);
		
		projectile.setLocationAndAngles(sx, sy, sz, rotationYaw, rotationPitch);

		worldObj.spawnEntityInWorld(projectile);
	}
	
	protected void launchPicks()
	{
		worldObj.playSoundAtEntity(this, "random.bow", getSoundVolume("random.bow"), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 0.4F);

		for (int i = 0; i < 8; i++)
		{
			float throwAngle = i * 3.14159165F / 4F; 

			
			double sx = posX + (MathHelper.cos(throwAngle) * 1);
			double sy = posY + (height * 0.82);
			double sz = posZ + (MathHelper.sin(throwAngle) * 1);

			double vx = MathHelper.cos(throwAngle);
			double vy = 0;
			double vz = MathHelper.sin(throwAngle);

			
			EntityTFThrownPick projectile = new EntityTFThrownPick(worldObj, this);
			

			projectile.setLocationAndAngles(sx, sy, sz, i * 45F, rotationPitch);
			
			float speed = 0.5F;

			projectile.setThrowableHeading(vx, vy, vz, speed, 1.0F);

			worldObj.spawnEntityInWorld(projectile);
		}
	}
    
    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return true;
    }
    
    /**
     * knocks back this entity
     */
    @Override
    public void knockBack(Entity par1Entity, float damage, double par3, double par5)
    {
        this.isAirBorne = true;
        float f = MathHelper.sqrt_double(par3 * par3 + par5 * par5);
        float distance = 0.2F;
        this.motionX /= 2.0D;
        this.motionY /= 2.0D;
        this.motionZ /= 2.0D;
        this.motionX -= par3 / (double)f * (double)distance;
        this.motionY += (double)distance;
        this.motionZ -= par5 / (double)f * (double)distance;

        if (this.motionY > 0.4000000059604645D)
        {
            this.motionY = 0.4000000059604645D;
        }
    }

    /**
     * Called each time the current formation ends.
     * 
     * If the current knight is the leader knight, it will pick and broadcast a new formation.
     */
    public void switchToNextFormation() {
		List<EntityTFKnightPhantom> nearbyKnights = getNearbyKnights();

    	if (this.currentFormation == Formation.ATTACK_PLAYER_START)
    	{
    		this.switchToFormation(Formation.ATTACK_PLAYER_ATTACK);
    	}
    	else if (this.currentFormation == Formation.ATTACK_PLAYER_ATTACK)
    	{
    		if (nearbyKnights.size() > 1)
    		{
    			this.switchToFormation(Formation.WAITING_FOR_LEADER);
    		}
    		else
    		{
    			// random weapon switch!
    			switch (rand.nextInt(3))
    			{
    			case 0:
    		        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlySword));
    		        break;
    			case 1:
    		        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlyAxe));
    		        break;
    			case 2:
    		        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlyPick));
    		        break;
    			}
    			
    			this.switchToFormation(Formation.ATTACK_PLAYER_START);
    		}
    	}
    	else if (this.currentFormation == Formation.WAITING_FOR_LEADER)
    	{
			//System.out.println("I am done waiting");

    		// try to find a nearby knight and do what they're doing
    		if (nearbyKnights.size() > 1)
    		{
    			this.switchToFormation(nearbyKnights.get(1).currentFormation);
    			this.ticksProgress = nearbyKnights.get(1).ticksProgress;
    		}
    		else
    		{
    			this.switchToFormation(Formation.ATTACK_PLAYER_START);
    			//System.out.println("Can't find nearby knight, charging");
    		}
    	}
    	else
    	{

    		if (isThisTheLeader(nearbyKnights))
    		{
    			// pick a random formation
    			pickRandomFormation();

    			// broadcast it
    			broadcastMyFormation(nearbyKnights);

    			// if no one is charging
    			if (isNobodyCharging(nearbyKnights))
    			{
    				makeARandomKnightCharge(nearbyKnights);
    			}
    		}
    	}
    }


	@SuppressWarnings("unchecked")
	private List<EntityTFKnightPhantom> getNearbyKnights() {
		List<EntityTFKnightPhantom> nearbyKnights = worldObj.getEntitiesWithinAABB(EntityTFKnightPhantom.class, AxisAlignedBB.getAABBPool().getAABB(this.posX, this.posY, this.posZ, this.posX + 1, this.posY + 1, this.posZ + 1).expand(32.0D, 8.0D, 32.0D));
		return nearbyKnights;
	}

    /**
     * Pick a random formation.  Called by the leader when his current formation duration ends
     */
    protected void pickRandomFormation() {
    	switch (rand.nextInt(8))
    	{
    	case 0:
    		currentFormation = Formation.SMALL_CLOCKWISE;
    		break;
    	case 1:
    		currentFormation = Formation.SMALL_ANTICLOCKWISE;
    		//currentFormation = Formation.LARGE_ANTICLOCKWISE;
    		break;
    	case 2:
    		currentFormation = Formation.SMALL_ANTICLOCKWISE;
    		break;
    	case 3:
    		currentFormation = Formation.CHARGE_PLUSX;
    		break;
    	case 4:
    		currentFormation = Formation.CHARGE_MINUSX;
    		break;
    	case 5:
    		currentFormation = Formation.CHARGE_PLUSZ;
    		break;
    	case 6:
    		currentFormation = Formation.CHARGE_MINUSZ;
    		break;
    	case 7:
    		currentFormation = Formation.SMALL_CLOCKWISE;
    		//currentFormation = Formation.LARGE_CLOCKWISE;
    		break;
    	}

    	this.switchToFormation(currentFormation);
    }

    /**
     * Check within 20ish squares.  If this phantom is the lowest numbered one, return true
     * @param nearbyKnights 
     */
    private boolean isThisTheLeader(List<EntityTFKnightPhantom> nearbyKnights) {

    	boolean iAmTheLowest = true;

    	//System.out.println("Checking " + nearbyKnights.size() + " knights to see if I'm the leader");


    	// find more knights

    	for (EntityTFKnightPhantom knight : nearbyKnights)
    	{
    		if (knight.getNumber() < this.getNumber())
    		{
    			iAmTheLowest = false;
    			break; // don't bother checking more
    		}
    	}

    	return iAmTheLowest;
    }

    private boolean isNobodyCharging(List<EntityTFKnightPhantom> nearbyKnights) {
    	boolean noCharge = true;
    	for (EntityTFKnightPhantom knight : nearbyKnights)
    	{
    		if (knight.isChargingAtPlayer())
    		{
    			noCharge = false;
    			break; // don't bother checking more
    		}
    	}

    	return noCharge;
    }

    /**
     * Tell a random knight from the list to charge
     */
    private void makeARandomKnightCharge(List<EntityTFKnightPhantom> nearbyKnights) {
    	int randomNum = rand.nextInt(nearbyKnights.size());

    	nearbyKnights.get(randomNum).switchToFormation(Formation.ATTACK_PLAYER_START);

    	//System.out.println("Telling knight " + randomNum + " to charge");
    }


    /**
     * Tell all the knights on the list to do something
     */
	private void broadcastMyFormation(List<EntityTFKnightPhantom> nearbyKnights) {
    	// find more knights

    	//System.out.println("Broadcasting to " + nearbyKnights.size() + " knights");

    	for (EntityTFKnightPhantom knight : nearbyKnights)
    	{
    		if (!knight.isChargingAtPlayer())
    		{
    			//System.out.println("Telling knight " + knight + " to switch");
    			knight.switchToFormation(this.currentFormation);
    		}
    	}

    	//System.out.println("knight phantom broadcast switch to formation " + this.currentFormation);
    }

    public boolean isChargingAtPlayer()
    {
    	return dataWatcher.getWatchableObjectByte(FLAG_CHARGING) != 0;
    }

    public void setChargingAtPlayer(boolean flag)
    {
    	if (flag)
    	{
    		dataWatcher.updateObject(FLAG_CHARGING, Byte.valueOf((byte)127));
    	}
    	else
    	{
    		dataWatcher.updateObject(FLAG_CHARGING, Byte.valueOf((byte)0));
    	}
    }
    
    @Override
	protected String getLivingSound()
    {
        return TwilightForestMod.ID + ":mob.wraith.wraith";
    }

    @Override
	protected String getHurtSound()
    {
        return TwilightForestMod.ID + ":mob.wraith.wraith";
    }

    @Override
	protected String getDeathSound()
    {
        return TwilightForestMod.ID + ":mob.wraith.wraith";
    }


    private void switchToFormationByNumber(int formationNumber) {

    	currentFormation = Formation.values()[formationNumber];

    	this.ticksProgress = 0;
    }

    public void switchToFormation(Formation formation)
    {
    	//System.out.println("Knight " + this.getNumber() + " now switching to formation " + formation);

    	this.currentFormation = formation;
    	this.ticksProgress = 0;

    	this.setChargingAtPlayer(this.currentFormation == Formation.ATTACK_PLAYER_START || this.currentFormation == Formation.ATTACK_PLAYER_ATTACK);

    }

    public int getFormationAsNumber()
    {
    	return this.currentFormation.ordinal();	
    }


    public int getTicksProgress() {
    	return ticksProgress;
    }

	public void setTicksProgress(int ticksProgress) {
		this.ticksProgress = ticksProgress;
	}

	public int getMaxTicksForFormation()
	{
		switch (currentFormation)
		{
		default:
		case HOVER:
			return 90;
		case LARGE_CLOCKWISE:
			return 180;
		case SMALL_CLOCKWISE:
			return 90;
		case LARGE_ANTICLOCKWISE:
			return 180;
		case SMALL_ANTICLOCKWISE:
			return 90;
		case CHARGE_PLUSX:
			return 180;
		case CHARGE_MINUSX:
			return 180;
		case CHARGE_PLUSZ:
			return 180;
		case CHARGE_MINUSZ:
			return 180;
		case ATTACK_PLAYER_START:
			return 50;
		case ATTACK_PLAYER_ATTACK:
			return 50;
		case WAITING_FOR_LEADER:
			return 10;
		}
	}

	private Vec3 getDestination() {
		
		if (!this.hasHome())
		{
			// hmmm
		}

		switch (currentFormation)
		{
		case LARGE_CLOCKWISE:
			return getCirclePosition(CIRCLE_LARGE_RADIUS, true);
		case SMALL_CLOCKWISE:
			return getCirclePosition(CIRCLE_SMALL_RADIUS, true);
		case LARGE_ANTICLOCKWISE:
			return getCirclePosition(CIRCLE_LARGE_RADIUS, false);
		case SMALL_ANTICLOCKWISE:
			return getCirclePosition(CIRCLE_SMALL_RADIUS, false);
		case CHARGE_PLUSX:
			return getMoveAcrossPosition(true, true);
		case CHARGE_MINUSX:
			return getMoveAcrossPosition(false, true);
		case CHARGE_PLUSZ:
			return getMoveAcrossPosition(true, false);
		case ATTACK_PLAYER_START:
		case HOVER:
			return getHoverPosition(CIRCLE_LARGE_RADIUS);
		case CHARGE_MINUSZ:
			return getMoveAcrossPosition(false, false);
		case WAITING_FOR_LEADER:
			return getLoiterPosition();
		case ATTACK_PLAYER_ATTACK:
			return getAttackPlayerPosition();
		default:
			return getLoiterPosition();
		}
	}

	private Vec3 getMoveAcrossPosition(boolean plus, boolean alongX) {
		
		float offset0 = (this.getNumber() * 3F) - 7.5F;
		
		float offset1;
		
		if (this.ticksProgress < 60)
		{
			offset1 = -7F;
		}
		else
		{
			offset1 = -7F + (((this.ticksProgress - 60) / 120F) * 14F);
		}
		
		if (!plus)
		{
			offset1 *= -1;
		}
		
		
		double dx = this.getHomePosition().posX + (alongX ? offset0 : offset1);
		double dy = this.getHomePosition().posY + Math.cos(this.ticksProgress / 7F + this.getNumber());
		double dz = this.getHomePosition().posZ + (alongX ? offset1 : offset0);
		return Vec3.createVectorHelper(dx, dy, dz);	
	}

	protected Vec3 getCirclePosition(float distance, boolean clockwise) {
		float angle = (this.ticksProgress * 2.0F);
		
		if (!clockwise)
		{
			angle *= -1;
		}
		
		angle += (60F * this.getNumber());
		
		double dx = this.getHomePosition().posX + Math.cos((angle) * Math.PI / 180.0D) * distance;
		double dy = this.getHomePosition().posY + Math.cos(this.ticksProgress / 7F + this.getNumber());
		double dz = this.getHomePosition().posZ + Math.sin((angle) * Math.PI / 180.0D) * distance;
		return Vec3.createVectorHelper(dx, dy, dz);
	}

	private Vec3 getHoverPosition(float distance) {
		// bound this by distance so we don't hover in walls if we get knocked into them
		
		double dx = this.lastTickPosX;
		double dy = this.getHomePosition().posY + Math.cos(this.ticksProgress / 7F + this.getNumber());
		double dz = this.lastTickPosZ;
		
		// let's just bound this by 2D distance
		double ox = (this.getHomePosition().posX - dx);
		double oz = (this.getHomePosition().posZ - dz);
		double dDist = Math.sqrt(ox * ox + oz * oz);
		
		if (dDist > distance)
		{
//			System.out.println("Phantom hovering beyond normal bounds");
			
			// normalize back to boundaries
			
			dx = this.getHomePosition().posX + (ox / dDist * distance);
			dz = this.getHomePosition().posZ + (oz / dDist * distance);
		}
		
		return Vec3.createVectorHelper(dx, dy, dz);
	}

	private Vec3 getLoiterPosition() {
		double dx = this.getHomePosition().posX;
		double dy = this.getHomePosition().posY + Math.cos(this.ticksProgress / 7F + this.getNumber());
		double dz = this.getHomePosition().posZ;
		return Vec3.createVectorHelper(dx, dy, dz);
	}


	private Vec3 getAttackPlayerPosition() {
		if (isSwordKnight())
		{
			return Vec3.createVectorHelper(this.chargePosX, this.chargePosY, this.chargePosZ);
		}
		else
		{
			return getHoverPosition(CIRCLE_LARGE_RADIUS);
		}

	}


	public boolean isSwordKnight() {
		return this.getCurrentItemOrArmor(0) != null && this.getCurrentItemOrArmor(0).getItem() == TFItems.knightlySword;
	}
	public boolean isAxeKnight() {
		return this.getCurrentItemOrArmor(0) != null && this.getCurrentItemOrArmor(0).getItem() == TFItems.knightlyAxe;
	}
	public boolean isPickKnight() {
		return this.getCurrentItemOrArmor(0) != null && this.getCurrentItemOrArmor(0).getItem() == TFItems.knightlyPick;
	}


	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
		
		// set weapon per number
		switch (number % 3)
		{
		case 0:
	        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlySword));
	        break;
		case 1:
	        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlyAxe));
	        break;
		case 2:
	        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.knightlyPick));
	        break;
		}
	}
	
    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
    	ChunkCoordinates home = this.getHomePosition();
        nbttagcompound.setTag("Home", newDoubleNBTList((double)home.posX, (double)home.posY, (double)home.posZ));
        nbttagcompound.setBoolean("HasHome", this.hasHome());
        nbttagcompound.setInteger("MyNumber", this.getNumber());
        nbttagcompound.setInteger("Formation", this.getFormationAsNumber());
        nbttagcompound.setInteger("TicksProgress", this.getTicksProgress());
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        
        if (nbttagcompound.hasKey("Home"))
        {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Home");
            int hx = (int) ((NBTTagDouble)nbttaglist.tagAt(0)).data;
            int hy = (int) ((NBTTagDouble)nbttaglist.tagAt(1)).data;
            int hz = (int) ((NBTTagDouble)nbttaglist.tagAt(2)).data;
            this.setHomeArea(hx, hy, hz, 20);
        }
        if (!nbttagcompound.getBoolean("HasHome"))
        {
        	this.detachHome();
        }
        this.setNumber(nbttagcompound.getInteger("MyNumber"));
        this.switchToFormationByNumber(nbttagcompound.getInteger("Formation"));
        this.setTicksProgress(nbttagcompound.getInteger("TicksProgress"));
    }

//    public boolean func_110173_bK()
//    {
//        return this.func_110176_b(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
//    }

    public boolean isWithinHomeArea(int par1, int par2, int par3)
    {
        return this.maximumHomeDistance == -1.0F ? true : this.homePosition.getDistanceSquared(par1, par2, par3) < this.maximumHomeDistance * this.maximumHomeDistance;
    }

    public void setHomeArea(int par1, int par2, int par3, int par4)
    {
    	
        this.homePosition.set(par1, par2, par3);
        this.maximumHomeDistance = (float)par4;

    }

    public ChunkCoordinates getHomePosition()
    {
        return this.homePosition;
    }

    public float getMaximumHomeDistance()
    {
        return this.maximumHomeDistance;
    }

    public void detachHome()
    {
        this.maximumHomeDistance = -1.0F;
    }

    public boolean hasHome()
    {
        return this.maximumHomeDistance != -1.0F;
    }
    
    
}
