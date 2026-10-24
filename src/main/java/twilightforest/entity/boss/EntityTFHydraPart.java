package twilightforest.entity.boss;

import net.minecraft.Damage;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLiving;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;

public class EntityTFHydraPart extends EntityLiving {

    public EntityTFHydra hydraObj;

    public EntityTFHydraPart(World world)
    {
    	super(world);
    }

    public EntityTFHydraPart(EntityTFHydra hydra, String s, float f, float f1)
    {
        super(hydra.worldObj);
        setSize(f, f1);
        hydraObj = hydra;
        setPartName(s);
    }

	/**
	 * The parts never use the MITE AI machinery (avoids getTarget/getAttackTarget error messages).
	 */
	@Override
	protected boolean isAIEnabled()
	{
		return true;
	}

	@Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(17, "");
    }

    public String getPartName()
    {
        return dataWatcher.getWatchableObjectString(17);
    }

    public void setPartName(String name)
    {
        dataWatcher.updateObject(17, name);
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setString("PartName", getPartName());
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        setPartName(nbttagcompound.getString("PartName"));
    }

	/**
	 * We need this to display bounding boxes
	 */
    @Override
    public void onUpdate() {

    	if (this.hydraObj != null && this.hydraObj.deathTime > 190)
    	{
    		setDead();
    	}

    	//  just die if we've been alive 60 seconds and there's still no body
    	if (this.hydraObj == null && this.ticksExisted > 1200)
    	{
    		setDead();
    	}

    	super.onEntityUpdate();

    	lastTickPosX = posX;
    	lastTickPosY = posY;
    	lastTickPosZ = posZ;

        if (this.newPosRotationIncrements > 0)
        {
            double var1 = this.posX + (this.newPosX - this.posX) / this.newPosRotationIncrements;
            double var3 = this.posY + (this.newPosY - this.posY) / this.newPosRotationIncrements;
            double var5 = this.posZ + (this.newPosZ - this.posZ) / this.newPosRotationIncrements;
            double var7 = MathHelper.wrapAngleTo180_double(this.newRotationYaw - this.rotationYaw);
            this.rotationYaw = (float)(this.rotationYaw + var7 / this.newPosRotationIncrements);
            this.rotationPitch = (float)(this.rotationPitch + (this.newRotationPitch - this.rotationPitch) / this.newPosRotationIncrements);
            --this.newPosRotationIncrements;
            this.setPosition(var1, var3, var5);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }

    	this.rotationYawHead = this.rotationYaw;
    	this.prevRotationYawHead = this.prevRotationYaw;

    	for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
    	for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }

    	for (; renderYawOffset - prevRenderYawOffset < -180F; prevRenderYawOffset -= 360F) { }
    	for (; renderYawOffset - prevRenderYawOffset >= 180F; prevRenderYawOffset += 360F) { }

    	for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
    	for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }

    	for (; rotationYawHead - prevRotationYawHead < -180F; prevRotationYawHead -= 360F) { }
    	for (; rotationYawHead - prevRotationYawHead >= 180F; prevRotationYawHead += 360F) { }
    }

	/**
	 * Set monster attributes
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(1000D); // max health
    }

	/**
	 * All damage is forwarded to the hydra (MITE: attackEntityFrom(Damage), null = no effect)
	 */
	@Override
	public EntityDamageResult attackEntityFrom(Damage damage)
    {
		if (hydraObj != null)
		{
			return hydraObj.attackEntityFromPart(this, damage);
		}
		else
		{
			return null;
		}
    }

    @Override
    public boolean isEntityEqual(Entity entity)
    {
        return this == entity || hydraObj == entity;
    }

    /**
     * The parts are immune to fire like the hydra itself
     */
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
    public boolean canCatchFire()
    {
        return false;
    }

    /**
     * Sets the rotation of the entity
     */
    @Override
	protected void setRotation(float par1, float par2)
    {
        this.rotationYaw = par1 % 360.0F;
        this.rotationPitch = par2 % 360.0F;
    }

}
