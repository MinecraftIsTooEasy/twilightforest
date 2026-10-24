package twilightforest.entity;

import net.minecraft.Block;
import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.ItemDye;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;


public class EntityTFNatureBolt extends EntityThrowable {

	private EntityPlayer playerTarget;

	public EntityTFNatureBolt(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityTFNatureBolt(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	public EntityTFNatureBolt(World par1World) {
		super(par1World);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

        makeTrail();
	}
	
	/**
	 * How much this entity falls each tick
	 */
	@Override
    protected float getGravityVelocity()
    {
		return 0.003F;
    }
	
	/**
	 * Make sparkly trail
	 */
	public void makeTrail() {
		for (int i = 0; i < 5; i++) {
			double dx = posX + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dy = posY + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			double dz = posZ + 0.5 * (rand.nextDouble() - rand.nextDouble()); 
			TFParticles.spawn(worldObj, "happyVillager", dx, dy, dz, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * What happens when we hit something?
	 */
	@Override
	protected void onImpact(RaycastCollision rc) {
		// only damage living things
		if (rc.isEntity() && rc.getEntityHit() instanceof EntityLivingBase)
		{
			if (TFDamage.hurt(rc.getEntityHit(), DamageSource.causeIndirectMagicDamage(this, this.getThrower()), 2))
			{
				// similar to EntityCaveSpider (difficultySetting: 0 peaceful, 1 easy, 2 normal, 3 hard)
				byte poisonStrength = (byte) (worldObj.difficultySetting == 0 ? 0 : worldObj.difficultySetting == 2 ? 3 : 7);
				if(poisonStrength > 0)
				{
					((EntityLivingBase)rc.getEntityHit()).addPotionEffect(new PotionEffect(Potion.poison.id, poisonStrength * 20, 0));
				}

			}
		}

		for (int i = 0; i < 8; ++i)
		{
			TFParticles.spawnBlockCrack(this.worldObj, Block.leaves.blockID, 0, this.posX, this.posY, this.posZ, rand.nextGaussian() * 0.05D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.05D);
		}

		if (!this.worldObj.isRemote)
		{
			this.setDead();
			
			if (rc.isBlock()) {
				// if we hit a solid block, maybe do our nature burst effect.
				int dx = rc.block_hit_x;
				int dy = rc.block_hit_y;
				int dz = rc.block_hit_z;

				Material materialHit = rc.getBlockHitMaterial();
			
				if (materialHit == Material.grass && this.playerTarget != null) 
				{
					// bonemeal the grass (the stack is a throwaway, nothing is consumed)
					ItemDye.tryFertilize(new ItemStack(Item.dyePowder, 1, 15), worldObj, dx, dy, dz, rc.face_hit);
				}			
				else if (materialHit.isSolid() && canReplaceBlock(worldObj, dx, dy, dz)) 
				{
					TFWorldUtil.setBlock(worldObj, dx, dy, dz, Block.leaves, 2, 3);
				}			
			}
		}

	}

	/**
	 * This is surprisingly difficult to determine
	 */
	private boolean canReplaceBlock(World worldObj, int dx, int dy, int dz) 
	{
		Block blockObj = worldObj.getBlock(dx, dy, dz);
		float hardness = blockObj == null ? -1 : blockObj.getBlockHardness(worldObj.getBlockMetadata(dx, dy, dz));
		
		return hardness >= 0 && hardness < 50F;
	}

	public void setTarget(EntityLivingBase attackTarget) 
	{
		if (attackTarget instanceof EntityPlayer)
		{
			this.playerTarget = (EntityPlayer)attackTarget;
		}
	}

}
