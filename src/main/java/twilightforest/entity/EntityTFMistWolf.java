package twilightforest.entity;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;

public class EntityTFMistWolf extends EntityTFHostileWolf {

	public EntityTFMistWolf(World world) {
		super(world);
        this.setSize(1.4F, 1.9F);
        
        //this.texture = TwilightForestMod.MODEL_DIR + "mistwolf.png";
	}

	/**
	 * Set monster attributes (MITE_BALANCE: T3 mob, 36 HP, 13 damage, 1 natural defense)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.setEntityAttribute(SharedMonsterAttributes.maxHealth, 36.0D); // max health
        this.setEntityAttribute(SharedMonsterAttributes.attackDamage, 13.0D); // attack damage
    }

	@Override
	public int getExperienceValue()
	{
		return 13;
	}

	@Override
	public float getNaturalDefense(DamageSource damage_source)
	{
		return damage_source != null && damage_source.bypassesMundaneArmor() ? 0.0F : 1.0F;
	}

    /**
     * Returns the amount of damage a mob should deal.
     */
    public int getAttackStrength(Entity par1Entity)
    {
        return (int) this.getEntityAttributeValue(SharedMonsterAttributes.attackDamage);
    }
    
    /**
     * Bite, and blind the target if we are standing in the dark (3 s, not on easy/peaceful per MITE_BALANCE).
     */
    @Override
    public EntityDamageResult attackEntityAsMob(Entity par1Entity)
    {
    	EntityDamageResult result = super.attackEntityAsMob(par1Entity);
        if (result != null && result.entityWasNegativelyAffected())
        {
            float myBrightness = this.getBrightness(1.0F);

            if (par1Entity instanceof EntityLivingBase && myBrightness < 0.10F && this.worldObj.difficultySetting > 1)
            {
                ((EntityLivingBase)par1Entity).addPotionEffect(new PotionEffect(Potion.blindness.id, 3 * 20, 0));
            }
        }
        return result;
    }
    
    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
    protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.6F;
    }
}
