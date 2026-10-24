package twilightforest.entity;

import net.minecraft.Block;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityThrowable;
import net.minecraft.EnumFace;
import net.minecraft.Entity;
import net.minecraft.Item;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import twilightforest.block.BlockTFCritter;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;
import twilightforest.item.TFItems;


public class EntityTFMoonwormShot extends EntityThrowable {

	public EntityTFMoonwormShot(World par1World, double par2, double par4, double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityTFMoonwormShot(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
	}

	public EntityTFMoonwormShot(World par1World) {
		super(par1World);
	}

	/**
	 * MITE requires every thrown entity to name its model item (damage source item, fire checks)
	 */
	@Override
	public Item getModelItem() {
		return TFItems.moonwormQueen != null ? TFItems.moonwormQueen : Item.snowball;
	}

	// 1.7.10 overrode func_40077_c(0.5F) here, which is not the velocity hook (func_70182_d), so the shot keeps the default
	// EntityThrowable velocity; MITE's default (1.5F) is kept on purpose.

	@Override
	public void onUpdate() {
		super.onUpdate();

		makeTrail();
	}

	/**
	 * Gets how bright this entity is.
	 */
	@Override
	public float getBrightness(float par1)
	{
		return 1.0F;
	}

	@Override
	public int getBrightnessForRender(float par1)
	{
		return 15728880;
	}



	public void makeTrail() {
		// the 1.7.10 trail was commented out
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
	public float getCollisionBorderSize(Entity forRaycastFromThisEntity)
	{
		return 1.0F;
	}

	/**
	 * How much this entity falls each tick
	 */
	@Override
	protected float getGravityVelocity()
	{
		return 0.03F;
	}

	@Override
	protected void onImpact(RaycastCollision rc) {

		// did we hit a block?  Make a worm there!

		if (rc.isBlock() && !worldObj.isRemote)
		{
			this.placeMoonworm(rc);
		}

		if (rc.isEntity())
		{
			TFDamage.hurtByThrown(rc.getEntityHit(), this, this.getThrower(), 0);
		}

		for (int var3 = 0; var3 < 8; ++var3)
		{
			TFParticles.spawnBlockCrack(this.worldObj, TFBlocks.moonworm.blockID, 0, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
		}

		if (!this.worldObj.isRemote)
		{
			this.setDead();
		}
	}

	/**
	 * What ItemTFMoonwormQueen.onItemUse did in 1.7.10: place a moonworm on the block we hit (no held item involved).
	 */
	private void placeMoonworm(RaycastCollision rc)
	{
		BlockTFCritter moonworm = (BlockTFCritter) TFBlocks.moonworm;
		Block hit = rc.getBlockHit();
		if (hit == moonworm) {
			return;
		}

		EnumFace face = rc.face_hit;
		int x = rc.neighbor_block_x;
		int y = rc.neighbor_block_y;
		int z = rc.neighbor_block_z;

		// snow, vines, grass etc. are replaced instead of built on
		int meta = moonworm.getDefaultMetadataForFaceMountedTo(face);
		if (moonworm.canReplaceBlock(meta, hit, rc.block_hit_metadata)) {
			x = rc.block_hit_x;
			y = rc.block_hit_y;
			z = rc.block_hit_z;
			face = EnumFace.TOP;
			meta = moonworm.getDefaultMetadataForFaceMountedTo(face);
		}

		moonworm.is_being_placed = true;
		try {
			moonworm.tryPlaceBlock(this.worldObj, x, y, z, face, meta, this.getThrower(), true, true);
		} finally {
			moonworm.is_being_placed = false;
		}
	}


}
