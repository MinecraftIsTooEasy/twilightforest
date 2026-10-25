package twilightforest.entity.boss;

import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.item.TFItems;

public class EntityTFThrownAxe extends EntityThrowable  {

	/** MITE_BALANCE 9.3: thrown axe 9 raw */
	private static final float PROJECTILE_DAMAGE = 9.0F;

	public EntityTFThrownAxe(World par1World, EntityLivingBase par2EntityLivingBase) {
		super(par1World, par2EntityLivingBase);
        this.setSize(0.5F, 0.5F);
	}

	public EntityTFThrownAxe(World par1World) {
		super(par1World);
        this.setSize(0.5F, 0.5F);
	}

	@Override
	protected void onImpact(RaycastCollision rc) {
		boolean passThru = false;

		if (rc.isEntity())
		{
			Entity entityHit = rc.getEntityHit();

			if (entityHit instanceof EntityTFKnightPhantom)
			{
				passThru = true;
			}

	    	// if we're not set to pass, damage what we hit
			if (!passThru)
			{
				TFDamage.hurtByThrown(entityHit, this, this.getThrower(), PROJECTILE_DAMAGE);
			}
		}

		for (int i = 0; i < 8; ++i)
		{
			TFParticles.spawn(this.worldObj, "largesmoke", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		if (!passThru && !this.worldObj.isRemote)
		{
			this.setDead();
		}
	}

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
	public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * We need to set this so that the player can attack and reflect the bolt
     */
    @Override
	public float getCollisionBorderSize(Entity for_raycast_from_this_entity)
    {
        return 1.0F;
    }

    /**
     * Projectile speed
     */
    @Override
    protected float func_70182_d()
    {
        return 0.1F;
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

	/**
	 * MITE requires every thrown entity to name its model item
	 */
	@Override
	public Item getModelItem() {
		return TFItems.knightlyAxe;
	}
}
