package twilightforest.tileentity;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntity;
import twilightforest.TFIds;
import twilightforest.TwilightForestMod;
import twilightforest.block.BlockTFFireJet;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;

public class TileEntityTFFlameJet extends TileEntity {

	int counter = 0;
	private int nextMeta;

	public TileEntityTFFlameJet() {
		this(BlockTFFireJet.META_JET_IDLE);
	}

	/** @param parNextMeta the state to return to; negative = derive it from the block's current state. */
	public TileEntityTFFlameJet(int parNextMeta) {
		this.nextMeta = parNextMeta;
	}

	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
	 * ticks and creates a new spawn inside its implementation.
	 */
	@Override
	public void updateEntity() {
		if (!BlockTFFireJet.verifyTileEntity(this)) {
			return;
		}
		if (this.nextMeta < 0) {
			this.nextMeta = BlockTFFireJet.nextMetaFor(worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord));
		}

		if (++counter > 60) {
			counter = 0;
			// idle again
			if (!worldObj.isRemote && worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord) == TFIds.FIRE_JET) {
				worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, TFIds.FIRE_JET, this.nextMeta, 3);
			}
			this.invalidate();
		} else if (counter % 2 == 0) {
			TFParticles.spawn(worldObj, "largesmoke", this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5, 0.0D, 0.0D, 0.0D);
			TwilightForestMod.proxy.spawnParticle(this.worldObj, "largeflame", this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5, 0.0D, 0.5D, 0.0D);

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "largeflame", this.xCoord - 0.5, this.yCoord + 1.0, this.zCoord + 0.5, 0.05D, 0.5D, 0.0D);
			TwilightForestMod.proxy.spawnParticle(this.worldObj, "largeflame", this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord - 0.5, 0.0D, 0.5D, 0.05D);
			TwilightForestMod.proxy.spawnParticle(this.worldObj, "largeflame", this.xCoord + 1.5, this.yCoord + 1.0, this.zCoord + 0.5, -0.05D, 0.5D, 0.0D);
			TwilightForestMod.proxy.spawnParticle(this.worldObj, "largeflame", this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 1.5, 0.0D, 0.5D, -0.05D);
		}

		// sounds
		if (counter % 4 == 0) {
			worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "mob.ghast.fireball", 1.0F + worldObj.rand.nextFloat(), worldObj.rand.nextFloat() * 0.7F + 0.3F);
		} else if (counter == 1) {
			worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, "fire.ignite", 1.0F + worldObj.rand.nextFloat(), worldObj.rand.nextFloat() * 0.7F + 0.3F);
		}

		// actual fire effects
		if (!worldObj.isRemote) {
			if (counter % 5 == 0) {
				// find entities in the area of effect
				List<Entity> entitiesInRange = worldObj.getEntitiesWithinAABB(Entity.class,
						AxisAlignedBB.getBoundingBox(this.xCoord - 2, this.yCoord, this.zCoord - 2,
								this.xCoord + 2, this.yCoord + 4, this.zCoord + 2));
				// fire!
				for (Entity entity : entitiesInRange) {
					if (entity.isHarmedByFire()) {
						TFDamage.hurt(entity, DamageSource.inFire, 2);
						entity.setFire(15);
					}
				}
			}
		}
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
		this.nextMeta = par1NBTTagCompound.getInteger("NextMeta");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setInteger("NextMeta", this.nextMeta);
	}
}
