package twilightforest.entity.passive;

import net.minecraft.EntityAgeable;
import net.minecraft.EntityAnimal;
import net.minecraft.Item;
import net.minecraft.World;


public abstract class EntityTFBird extends EntityAnimal {

	public float flapLength = 0.0F;
	public float flapIntensity = 0.0F;
	public float lastFlapIntensity;
	public float lastFlapLength;
	public float flapSpeed = 1.0F;

	public EntityTFBird(World par1World) {
		super(par1World);
	}

	// MITE: EntityAnimal.isAIEnabled() is public final (true), so no override here.

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate() {
	    super.onLivingUpdate();
	    this.lastFlapLength = this.flapLength;
	    this.lastFlapIntensity = this.flapIntensity;
	    this.flapIntensity = (float)(this.flapIntensity + (this.onGround ? -1 : 4) * 0.3D);
	
	    if (this.flapIntensity < 0.0F)
	    {
	        this.flapIntensity = 0.0F;
	    }
	
	    if (this.flapIntensity > 1.0F)
	    {
	        this.flapIntensity = 1.0F;
	    }
	
	    if (!this.onGround && this.flapSpeed < 1.0F)
	    {
	        this.flapSpeed = 1.0F;
	    }
	
	    this.flapSpeed = (float)(this.flapSpeed * 0.9D);
	
	    // don't fall as fast
	    if (!this.onGround && this.motionY < 0.0D)
	    {
	        this.motionY *= 0.6D;
	    }
	
	    this.flapLength += this.flapSpeed * 2.0F;
	}

	/**
	 * Called when the mob is falling. Calculates and applies fall damage.
	 */
	@Override
	protected void fall(float par1) {}

    /**
     * returns if this entity triggers Blocks.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
	protected boolean canTriggerWalking()
    {
        return false;
    }
	
	/**
	 * Returns the item ID for the item the mob drops on death.
	 */
	@Override
	protected int getDropItemId() {
	    return Item.feather.itemID;
	}

	/**
	 * This function is used when two same-species animals in 'love mode' breed to generate the new baby animal.
	 */
	@Override
	public EntityAgeable createChild(EntityAgeable entityanimal)
	{
		return null;
	}

	/**
	 * Overridden by flying birds
	 */
    public boolean isBirdLanded()
    {
    	return true;
    }

}
