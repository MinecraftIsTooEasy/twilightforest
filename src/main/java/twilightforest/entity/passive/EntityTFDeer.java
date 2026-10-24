package twilightforest.entity.passive;

import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityCow;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.item.TFItems;


/**
 * Deer are like quiet, non-milkable cows!
 * 
 * Also they look like deer
 * 
 * @author Ben
 *
 */
public class EntityTFDeer extends EntityCow
{

    public EntityTFDeer(World world)
    {
        super(world);
        //texture = TwilightForestMod.MODEL_DIR + "wilddeer.png";
        setSize(0.7F, 2.3F);
    }
    
    public EntityTFDeer(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }


    /**
     * No sounds when idle
     */
    @Override
    protected String getLivingSound()
    {
    	return null;
    }

    /**
     * Plays step sound at given x, y, z for the entity (deer are silent walkers)
     */
    @Override
	protected void playStepSound(int par1, int par2, int par3, int par4)
    {
    }
    
    /**
     * Not milkable: MITE's bucket logic (ItemVessel) asks the cow for its milk, so a deer never has any.
     */
    @Override
    protected int getMilk()
    {
        return 0;
    }

    /**
     * Not milkable: do not store produced goods as milk.
     */
    @Override
    public void produceGoods()
    {
        this.production_counter = 0;
    }

    /**
     * Venison 1-2 (+butchering) when well, leather 0-1 (MITE_BALANCE 9.3).
     */
    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
    {
        int num_drops = this.rand.nextInt(2);

        for (int i = 0; i < num_drops; ++i)
        {
            this.dropItem(Item.leather.itemID, 1);
        }

        if (this.isWell())
        {
            num_drops = this.rand.nextInt(2) + 1 + this.rand.nextInt(1 + damage_source.getButcheringModifier());

            for (int i = 0; i < num_drops; ++i)
            {
                this.dropItem(this.isBurning() ? TFItems.venisonCooked : TFItems.venisonRaw, 1);
            }
        }
    }

    
    /**
     * What is our baby going to be?  Another deer?!
     */
    @Override
	public EntityCow spawnBabyAnimal(EntityAgeable entityanimal)
    {
        return new EntityTFDeer(worldObj);
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
