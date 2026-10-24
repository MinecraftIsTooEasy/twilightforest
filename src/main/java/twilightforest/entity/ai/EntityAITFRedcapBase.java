package twilightforest.entity.ai;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockTNT;
import net.minecraft.ChunkCoordinates;
import net.minecraft.Entity;
import net.minecraft.EntityAIBase;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityTNTPrimed;
import net.minecraft.MathHelper;
import net.minecraft.World;
import twilightforest.entity.EntityTFRedcap;

public abstract class EntityAITFRedcapBase extends EntityAIBase
{

	protected EntityTFRedcap entityObj;

	/**
	 * Fairly straightforward.  Returns true in a 120 degree arc in front of the target's view.
	 */
	public boolean isTargetLookingAtMe(EntityLivingBase attackTarget) {
		// find angle of approach
		double dx = entityObj.posX - attackTarget.posX;
		double dz = entityObj.posZ - attackTarget.posZ;
		float angle = (float)((Math.atan2(dz, dx) * 180D) / Math.PI) - 90F;

		float difference = MathHelper.abs((attackTarget.rotationYaw - angle) % 360);

		return difference < 60 || difference > 300;
	}

	/**
	 * Check within the specified range to see if any of the blocks nearby are TNT
	 */
	public ChunkCoordinates findBlockTNTNearby(int range) {
		int entityPosX = MathHelper.floor_double(this.entityObj.posX);
		int entityPosY = MathHelper.floor_double(this.entityObj.posY);
		int entityPosZ = MathHelper.floor_double(this.entityObj.posZ);

		for (int x = -range; x <= range; x++)
		{
			for (int y = -range; y <= range; y++)
			{
				for (int z = -range; z <= range; z++)
				{
					if (entityObj.worldObj.getBlock(entityPosX + x, entityPosY + y, entityPosZ + z) == Block.tnt)
					{
						return new ChunkCoordinates(entityPosX + x, entityPosY + y, entityPosZ + z);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Check within the specified range to see if any primed TNT is nearby
	 */
	@SuppressWarnings("unchecked")
	public boolean isLitTNTNearby(int range)
	{
		AxisAlignedBB expandedBox = entityObj.boundingBox.expand(range, range, range);

		List<Entity> nearbyTNT = entityObj.worldObj.getEntitiesWithinAABB(EntityTNTPrimed.class, expandedBox);

		return nearbyTNT.size() > 0;
	}

	/**
	 * Replaces Blocks.tnt.onBlockDestroyedByPlayer + setBlock(air): primes the TNT block at the given position
	 * with the redcap as igniter and removes the block. Fuse is shortened to EntityTFRedcap.TNT_FUSE.
	 */
	protected void igniteTNT(World world, int x, int y, int z)
	{
		if (world.isRemote)
		{
			return;
		}

		// BlockTNT.primeTnt spawns the entity; find it afterwards to apply the redcap fuse
		BlockTNT.ignite(world, x, y, z, this.entityObj);

		// TODO(mite-port): EntityTNTPrimed.explode() is hard-coded (size 2/2, destroys blocks); a mixin must use
		// EntityTFRedcap.TNT_SIZE_VS_BLOCKS / TNT_SIZE_VS_LIVING when getTntPlacedBy() is an EntityTFRedcap.
		List<Entity> primed = world.getEntitiesWithinAABB(EntityTNTPrimed.class, AxisAlignedBB.getBoundingBox(x - 0.5D, y - 0.5D, z - 0.5D, x + 1.5D, y + 1.5D, z + 1.5D));
		for (Entity e : primed)
		{
			EntityTNTPrimed tnt = (EntityTNTPrimed)e;
			if (tnt.getTntPlacedBy() == this.entityObj && tnt.fuse > EntityTFRedcap.TNT_FUSE)
			{
				tnt.fuse = EntityTFRedcap.TNT_FUSE;
			}
		}
	}

}
