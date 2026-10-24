package twilightforest.entity.passive;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityLivingData;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySheep;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.item.TFItems;



public class EntityTFBighorn extends EntitySheep
{

    public EntityTFBighorn(World world)
    {
        super(world);
        //texture = TwilightForestMod.MODEL_DIR + "bighorn.png";
        setSize(0.9F, 1.3F);
    }
    
    public EntityTFBighorn(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

    /**
     * 50% brown, 50% any other color
     * 
     * @param random
     * @return
     */
    public static int getRandomFleeceColor(Random random)
    {
    	if (random.nextInt(2) == 0)
    	{
    		return 12;
    	}
    	else
    	{
        	return random.nextInt(15);
    	}
    }
    

    /**
     * Entity init, set our fleece color
     */
    @Override
    public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData)
    {
        par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);
        this.setFleeceColor(getRandomFleeceColor(this.worldObj.rand));
        return par1EntityLivingData;
    }
    
    /**
     * What is our baby?!
     */
    @Override
	public EntitySheep createChild(EntityAgeable entityanimal)
    {
    	EntityTFBighorn otherParent = (EntityTFBighorn)entityanimal;
    	EntityTFBighorn babySheep = new EntityTFBighorn(worldObj);
        if(rand.nextBoolean())
        {
            babySheep.setFleeceColor(getFleeceColor());
        } else
        {
            babySheep.setFleeceColor(otherParent.getFleeceColor());
        }
        return babySheep;
    }

    /**
     * Wool (as the sheep), 1-2 meef (+butchering) when well, leather 50% (MITE_BALANCE 9.3).
     */
    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source)
    {
        if (!this.getSheared() && !this.isBurning() && this.rand.nextInt(2) == 0)
        {
            this.dropItemStack(new ItemStack(Block.cloth.blockID, 1, this.getFleeceColor()));
        }

        if (this.isWell())
        {
            int num_drops = this.rand.nextInt(2) + 1 + this.rand.nextInt(1 + damage_source.getButcheringModifier());

            for (int i = 0; i < num_drops; i++)
            {
                this.dropItem(this.isBurning() ? TFItems.meefSteak : TFItems.meefRaw, 1);
            }
        }

        if (this.rand.nextInt(2) == 0)
        {
            this.dropItem(Item.leather.itemID, 1);
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
