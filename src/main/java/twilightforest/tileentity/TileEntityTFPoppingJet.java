package twilightforest.tileentity;

import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntity;
import twilightforest.TFIds;
import twilightforest.block.BlockTFFireJet;
import twilightforest.compat.TFParticles;

public class TileEntityTFPoppingJet extends TileEntity {

	int counter = 0;
	int nextMeta;

	public TileEntityTFPoppingJet() {
		this(BlockTFFireJet.META_JET_FLAME);
	}

	public TileEntityTFPoppingJet(int parNextMeta) {
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

		if (++counter >= 80) {
			counter = 0;
			// turn to flame
			if (!worldObj.isRemote && worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord) == TFIds.FIRE_JET) {
				worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, TFIds.FIRE_JET, this.nextMeta, 3);
			}
			this.invalidate();
		} else {
			if (counter % 20 == 0) {
				TFParticles.spawn(worldObj, "lava", this.xCoord + 0.5, this.yCoord + 1.5, this.zCoord + 0.5, 0.0D, 0.0D, 0.0D);
				worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "liquid.lavapop", 0.2F + worldObj.rand.nextFloat() * 0.2F, 0.9F + worldObj.rand.nextFloat() * 0.15F);
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
