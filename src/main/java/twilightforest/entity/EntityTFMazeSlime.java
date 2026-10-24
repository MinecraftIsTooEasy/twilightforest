package twilightforest.entity;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityCubic;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityGelatinousCube;
import net.minecraft.EnumParticle;
import net.minecraft.EnumSkyBlock;
import net.minecraft.MathHelper;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.item.TFItems;

// TODO(mite-port): MITE's EntitySlime is final, so the maze slime extends EntityGelatinousCube (the slime family base) instead.
public class EntityTFMazeSlime extends EntityGelatinousCube
{

	public EntityTFMazeSlime(World par1World) {
		super(par1World);
        //texture = TwilightForestMod.MODEL_DIR + "mazeslime.png";
        this.setSize(1 << (1 + this.rand.nextInt(2)));
	}

	/**
	 * Make more of meeee
	 */
    @Override
	public EntityCubic createInstance()
    {
        return new EntityTFMazeSlime(this.worldObj);
    }

    /**
     * Sets the slime size; maze slimes have 2 * size^2 health (balance: 12 for the small one, 32 for the big one)
     */
    @Override
	protected void setSize(int par1)
    {
        super.setSize(par1);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(par1 == 2 ? 12.0D : 2.0D * par1 * par1);
        this.setHealth(this.getMaxHealth());
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    @Override
	public boolean getCanSpawnHere(boolean perform_light_check)
    {
    	return this.worldObj.difficultySetting > 0 && this.worldObj.checkNoEntityCollision(this.boundingBox)
        		&& this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
        		&& !this.worldObj.isAnyLiquid(this.boundingBox) && (!perform_light_check || this.isValidLightLevel());
    }

    /**
     * Damage dealt to the target (balance: 8 for the small slime, 11 for the big one). The size based
     * getAttackStrength() of MITE is final, so the melee attack is rewritten here.
     */
    @Override
	public EntityDamageResult attackEntityAsMob(Entity target)
    {
    	if (!this.triesToDamageOnContact(target)) {
    		return null;
    	}
    	float amount = this.getSize() <= 2 ? 8.0F : 11.0F;
    	return target.attackEntityFrom(new Damage(DamageSource.causeMobDamage(this), amount));
    }

    @Override
	public int getAttackStrengthMultiplierForType()
    {
        return 4;
    }

    /**
     * Particle thrown when landing.
     */
    @Override
	public EnumParticle getSquishParticle()
    {
    	// TODO(mite-port): TF used blockcrack_<mazestone>_1; MITE's EntityCubic spawns the particle without block data
        return EnumParticle.slime;
    }

    @Override
	public DamageSource getDamageTypeVsItems()
    {
    	// TODO(mite-port): MITE slimes corrode held items hit by them; maze slimes keep the vanilla slime behaviour
        return DamageSource.pepsin;
    }

    /**
     * Experience: 4 / 8 (balance)
     */
    @Override
	public int getExperienceValue()
    {
        return this.getSize() * 2;
    }

    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    @Override
	protected boolean isValidLightLevel()
    {
        int var1 = MathHelper.floor_double(this.posX);
        int var2 = MathHelper.floor_double(this.boundingBox.minY);
        int var3 = MathHelper.floor_double(this.posZ);

        if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, var1, var2, var3) > this.rand.nextInt(32))
        {
            return false;
        }
        else
        {
            int var4 = this.worldObj.getBlockLightValue(var1, var2, var3);

            if (this.worldObj.isThundering(true))
            {
                int var5 = this.worldObj.skylightSubtracted;
                this.worldObj.skylightSubtracted = 10;
                var4 = this.worldObj.getBlockLightValue(var1, var2, var3);
                this.worldObj.skylightSubtracted = var5;
            }

            return var4 <= this.rand.nextInt(8);
        }
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
	protected float getSoundVolume(String sound)
    {
    	// OH MY GOD, SHUT UP
        return 0.1F * this.getSize();
    }

    /**
     * Drops: Keeping I at 0.5 % (balance), plus MITE's slimeball for the smallest size.
     */
    @Override
	protected void dropFewItems(boolean recentlyHit, DamageSource source)
    {
    	super.dropFewItems(recentlyHit, source);
    	if (recentlyHit && this.rand.nextFloat() < 0.005F + 0.002F * source.getLootingModifier())
    	{
    		this.dropItem(TFItems.charmOfKeeping1, 1);
    	}
    }

}
