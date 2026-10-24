package twilightforest.entity.ai;

import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityLookHelper;
import net.minecraft.MathHelper;


public class EntityTFRavenLookHelper extends EntityLookHelper
{
    private EntityLiving entity;
    // field_46149_b / field_46150_c / field_46147_d in 1.7.10: yaw speed, pitch speed, "is looking" flag
    private float deltaLookYaw;
    private float deltaLookPitch;
    private boolean isLooking = false;
    private double posX;
    private double posY;
    private double posZ;

    public EntityTFRavenLookHelper(EntityLiving par1EntityLiving)
    {
    	super(par1EntityLiving);
        this.entity = par1EntityLiving;
    }

    /**
     * Sets position to look at using entity
     */
    @Override
    public void setLookPositionWithEntity(Entity par1Entity, float par2, float par3)
    {
        this.posX = par1Entity.posX;

        if (par1Entity instanceof EntityLivingBase)
        {
            this.posY = par1Entity.posY + (double)par1Entity.getEyeHeight();
        }
        else
        {
            this.posY = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0D;
        }

        this.posZ = par1Entity.posZ;
        this.deltaLookYaw = par2;
        this.deltaLookPitch = par3;
        this.isLooking = true;
    }

    /**
     * Sets position to look at
     */
    @Override
	public void setLookPosition(double par1, double par3, double par5, float par7, float par8)
    {
        this.posX = par1;
        this.posY = par3;
        this.posZ = par5;
        this.deltaLookYaw = par7;
        this.deltaLookPitch = par8;
        this.isLooking = true;
    }

    /**
     * Updates look
     */
    @Override
	public void onUpdateLook()
    {
        this.entity.rotationPitch = 0.0F;

        if (this.isLooking)
        {
            this.isLooking = false;
            double var1 = this.posX - this.entity.posX;
            double var3 = this.posY - (this.entity.posY + this.entity.getEyeHeight());
            double var5 = this.posZ - this.entity.posZ;
            double var7 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
            // the raven's model faces 60 degrees off the vanilla forward direction (TF used -30 here)
            float var9 = (float)(Math.atan2(var5, var1) * 180.0D / Math.PI) - 30.0F;
            float var10 = (float)(-(Math.atan2(var3, var7) * 180.0D / Math.PI));
            this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, var10, this.deltaLookPitch);
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, var9, this.deltaLookYaw);
        }
        else
        {
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, this.entity.renderYawOffset, 10.0F);
        }

        float var11 = MathHelper.wrapAngleTo180_float(this.entity.rotationYawHead - this.entity.renderYawOffset);

        if (!this.entity.getNavigator().noPath())
        {
            if (var11 < -75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset - 75.0F;
            }

            if (var11 > 75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset + 75.0F;
            }
        }
    }

    private float updateRotation(float par1, float par2, float par3)
    {
        float var4 = MathHelper.wrapAngleTo180_float(par2 - par1);

        if (var4 > par3)
        {
            var4 = par3;
        }

        if (var4 < -par3)
        {
            var4 = -par3;
        }

        return par1 + var4;
    }
}
