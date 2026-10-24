package twilightforest.entity.passive;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityPig;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.World;
import twilightforest.TFAchievementPage;


public class EntityTFBoar extends EntityPig {
	
    public EntityTFBoar(World world)
    {
        super(world);
        //texture = TwilightForestMod.MODEL_DIR + "wildboar.png";
        setSize(0.9F, 0.9F);
    }
    
    public EntityTFBoar(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

    /**
     * What is our baby?!
     */
    @Override
	public EntityPig createChild(EntityAgeable entityanimal)
    {
        return new EntityTFBoar(worldObj);
    }

    /**
     * Pork 1-2 (+butchering), only when the boar is well (MITE_BALANCE 9.3).
     */
    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
    {
        if (this.getSaddled())
        {
            this.dropItem(Item.saddle);
        }

        if (this.isWell())
        {
            int num_drops = this.rand.nextInt(2) + 1 + this.rand.nextInt(1 + damage_source.getButcheringModifier());

            for (int i = 0; i < num_drops; i++)
            {
                this.dropItem(this.isBurning() ? Item.porkCooked : Item.porkRaw);
            }
        }
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
		}
	}

}
