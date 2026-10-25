package twilightforest.entity.boss;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityLivingBase;
import net.minecraft.ItemStack;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFProgress;
import twilightforest.entity.EntityTFMinotaur;
import twilightforest.item.TFItems;

public class EntityTFMinoshroom extends EntityTFMinotaur {

	public EntityTFMinoshroom(World par1World) {
		super(par1World);
		//this.texture = TwilightForestMod.MODEL_DIR + "minoshroomtaur.png";
		this.setSize(1.49F, 2.9F);

        this.setCurrentItemOrArmor(0, new ItemStack(TFItems.minotaurAxe));
        this.equipmentDropChances[0] = 0.0F;
	}

	/**
	 * Set monster attributes (balance: 180 HP, melee 12; the charge hit is 15 like the Minotaur's)
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(180.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(12.0D); // attack damage
    }

    /**
     * Natural defense (balance: 3)
     */
    @Override
	public float getNaturalDefense(DamageSource damage_source)
    {
        return 3.0F;
    }

    /**
     * Bosses never flee
     */
    @Override
	public boolean fleesWhenBadlyWounded()
    {
        return false;
    }

    /**
     * Balance: the 150 xp (x0.25 on repeat kills) are handed out to the credited players in onDeath, so no orbs.
     */
    @Override
	public int getExperienceValue()
    {
        return 0;
    }

    /** MITE_BALANCE 5.3: Minoshroom stays dormant until a player of level 14 is near (the maze room spawns it at generation). */
    public static final int MIN_LEVEL = 14;
    private boolean gateOpen;
    /** Set once in onDeath (server side), read by the drop methods. */
    private TFProgress.KillResult killResult;

    private boolean isLevelGated()
    {
        if (this.gateOpen)
        {
            return false;
        }
        if (TFProgress.levelGated(this, MIN_LEVEL))
        {
            return true;
        }
        this.gateOpen = true;
        return false;
    }

    @Override
    public void setAttackTarget(EntityLivingBase target)
    {
        if (target != null && isLevelGated())
        {
            return;
        }
        super.setAttackTarget(target);
    }

    @Override
    public EntityDamageResult attackEntityFrom(Damage damage)
    {
        if (!this.worldObj.isRemote && isLevelGated())
        {
            return null;
        }
        return super.attackEntityFrom(damage);
    }

    @Override
    public void onLivingUpdate()
    {
        if (!this.worldObj.isRemote && this.ticksExisted % 20 == 0 && isLevelGated())
        {
            TFProgress.hintGated(this, MIN_LEVEL);
            if (this.getAttackTarget() != null)
            {
                this.setAttackTarget(null);
            }
        }
        super.onLivingUpdate();
    }

    /** Progress credit first: the drops are decided inside super.onDeath. */
    @Override
    public void onDeath(DamageSource source)
    {
        if (!this.worldObj.isRemote && this.killResult == null)
        {
            this.killResult = TFProgress.onBossKilled(this, TFProgress.Flag.MINO);
        }
        super.onDeath(source);
        if (!this.worldObj.isRemote && this.killResult != null)
        {
            this.killResult.awardXp(150);
        }
    }

    /**
     * Returns the item ID for the item the mob drops on death.
     */
    @Override
	protected int getDropItemId()
    {
        return TFItems.meefStroganoff.itemID;
    }

    /**
     * Drop 2-4 stroganoff (balance), + looting
     */
    @Override
	protected void dropFewItems(boolean par1, DamageSource source)
    {
        int numDrops = this.rand.nextInt(3) + 2 + this.rand.nextInt(1 + source.getLootingModifier());
        if (this.killResult != null)
        {
            numDrops = this.killResult.material(numDrops);
        }

        for (int i = 0; i < numDrops; ++i)
        {
            this.dropItem(TFItems.meefStroganoff, 1);
        }
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
     * Drop the equipment for this entity.
     */
    @Override
	protected void dropEquipment(boolean par1, int par2)
    {
        super.dropEquipment(par1, par2);
        // the Minotaur Axe: always on the first kill, 60% on a repeat kill
        if (this.killResult == null || this.killResult.dropsUniques() || this.rand.nextFloat() < 0.6F)
        {
            this.dropItemStack(new ItemStack(TFItems.minotaurAxe), 0.0F);
        }
    }

    // Minoshroom never drops the rare maze map focus
    @Override
	protected void dropRareDrop()
    {
    }

}
