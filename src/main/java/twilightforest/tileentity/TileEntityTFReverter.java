package twilightforest.tileentity;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.TileEntity;
import twilightforest.block.BlockTFTowerTranslucent;
import twilightforest.block.TFBlocks;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.compat.TFWorldUtil;

public class TileEntityTFReverter extends TileEntity 
{
	private static final int REVERT_CHANCE = 10;
	
	public int radius = 4;
	public int diameter = 2 * radius + 1;
	public double requiredPlayerRange = 16;
	public Random rand = new Random();
	private int tickCount;
	
	private boolean slowScan;
	private int ticksSinceChange;
	
	private Block[] blockData;
	private byte[] metaData;
			
    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
	@Override
	public void updateEntity() 
	{
		if (this.anyPlayerInRange())
		{
			this.tickCount++;
			
			if (this.worldObj.isRemote)
			{
				double var1 = (double)((float)this.xCoord + this.worldObj.rand.nextFloat());
				double var3 = (double)((float)this.yCoord + this.worldObj.rand.nextFloat());
				double var5 = (double)((float)this.zCoord + this.worldObj.rand.nextFloat());
//				this.worldObj.spawnParticle("smoke", var1, var3, var5, 0.0D, 0.0D, 0.0D);
				TFParticles.spawn(this.worldObj, "reddust", var1, var3, var5, 0.0D, 0.0D, 0.0D);
				
				
				// occasionally make a little red dust line to outline our radius
				if (this.rand.nextInt(10) == 0)
				{
					makeRandomOutline();
					makeRandomOutline();
					makeRandomOutline();
				}
			}
			else
			{
				
				// new plan, take a snapshot of the world when we are first activated, and then rapidly revert changes
				if (blockData == null || metaData == null)
				{
					captureBlockData();
					this.slowScan = true;
				}
				
				if (!this.slowScan || this.tickCount % 20 == 0)
				{
					if (scanAndRevertChanges())
					{
						this.slowScan = false;
						this.ticksSinceChange = 0;
					}
					else
					{
						ticksSinceChange++;

						if (ticksSinceChange > 20)
						{
							this.slowScan = true;
						}
					}
				}
			}
		}
		else
		{
			// remove data
			this.blockData = null;
			this.metaData = null;
			
			this.tickCount = 0;
		}
	}

	
	/**
	 * Display a random one of the 12 possible outlines
	 */
    private void makeRandomOutline() 
    {
		makeOutline(this.rand.nextInt(12));
	}

    /**
     * Display a specific outline
     */
	private void makeOutline(int outline) 
	{
		// src
		double sx = this.xCoord;
		double sy = this.yCoord;
		double sz = this.zCoord;
		// dest
		double dx = this.xCoord;
		double dy = this.yCoord;
		double dz = this.zCoord;
		
		switch (outline)
		{
		case 0:
		case 8:
			sx -= radius;
			dx += radius + 1;
			sz -= radius;
			dz -= radius;
			break;
		case 1:
		case 9:
			sx -= radius;
			dx -= radius;
			sz -= radius;
			dz += radius + 1;
			break;
		case 2:
		case 10:
			sx -= radius;
			dx += radius + 1;
			sz += radius + 1;
			dz += radius + 1;
			break;
		case 3:
		case 11:
			sx += radius + 1;
			dx += radius + 1;
			sz -= radius;
			dz += radius + 1;
			break;
		case 4:
			sx -= radius;
			dx -= radius;
			sz -= radius;
			dz -= radius;
			break;
		case 5:
			sx += radius + 1;
			dx += radius + 1;
			sz -= radius;
			dz -= radius;
			break;
		case 6:
			sx += radius + 1;
			dx += radius + 1;
			sz += radius + 1;
			dz += radius + 1;
			break;
		case 7:
			sx -= radius;
			dx -= radius;
			sz += radius + 1;
			dz += radius + 1;
			break;
		}
		
		switch (outline)
		{
		case 0:
		case 1:
		case 2:
		case 3:
			sy += radius + 1;
			dy += radius + 1;
			break;
		case 4:
		case 5:
		case 6:
		case 7:
			sy -= radius;
			dy += radius + 1;
			break;
		case 8:
		case 9:
		case 10:
		case 11:
			sy -= radius;
			dy -= radius;
			break;
		}
		
		if(rand.nextBoolean())
		{
			drawParticleLine(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5, dx, dy, dz);
		}
		else
		{
			drawParticleLine(sx, sy, sz, this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5);
		}
		drawParticleLine(sx, sy, sz, dx, dy, dz);
	}

    /**
     * Make a trail of particles from one point to another
     */
    protected void drawParticleLine(double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
		// make particle trail
    	int particles = 16;
    	for (int i = 0; i < particles; i++)
    	{
    		double trailFactor = i / (particles - 1.0D);

    		double tx = srcX + (destX - srcX) * trailFactor + rand.nextFloat() * 0.005;
    		double ty = srcY + (destY - srcY) * trailFactor + rand.nextFloat() * 0.005;
    		double tz = srcZ + (destZ - srcZ) * trailFactor + rand.nextFloat() * 0.005;
    		TFParticles.spawn(worldObj, "reddust", tx, ty, tz, 0, 0, 0);
    	}
	}

	private boolean scanAndRevertChanges() {
		int index = 0;
		boolean reverted = false;
		
		for (int x = - radius; x <= radius; x++)
		{
			for (int y = - radius; y <= radius; y++)
			{
				for (int z = - radius; z <= radius; z++)
				{
					Block blockID = worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z);
					byte meta = (byte) worldObj.getBlockMetadata(this.xCoord + x, this.yCoord + y, this.zCoord + z);
					
					if (blockData[index] != blockID)
					{
						if (revertBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z, blockID, meta, blockData[index], metaData[index]))
						{
							reverted = true;
						}
						else
						{
							blockData[index] = blockID;
							metaData[index] = meta;
						}
					}
					
					index++;
				}
			}
		}
		
		return reverted;
	}

	private boolean revertBlock(int x, int y, int z, Block thereBlockID, byte thereMeta, Block replaceBlockID, byte replaceMeta) 
	{
		if (thereBlockID == null && replaceBlockID != null && !replaceBlockID.blockMaterial.isSolid())
		{
			// do not revert
			
			TwilightForestMod.LOGGER.debug("Not replacing block {} because it doesn't block movement", replaceBlockID);
			
			return false;
		}
		if (isUnrevertable(thereBlockID, thereMeta, replaceBlockID, replaceMeta))
		{
			// do not revert
			return false;
		}
		else if (this.rand.nextInt(REVERT_CHANCE) == 0)
		{
			// don't revert everything instantly
			if (replaceBlockID != null)
			{
				replaceBlockID = TFBlocks.towerTranslucent;
				replaceMeta = BlockTFTowerTranslucent.META_REVERTER_REPLACEMENT;
			}
			
			// capture the old block for drops before it is replaced
			BlockBreakInfo dropInfo = (thereBlockID != null && replaceBlockID == null) ? new BlockBreakInfo(worldObj, x, y, z).setWasNotLegal() : null;
			
			TFWorldUtil.setBlock(worldObj, x, y, z, replaceBlockID, replaceMeta, 2);
			
			// play a little animation
			if (thereBlockID == null)
			{
				worldObj.playAuxSFX(2001, x, y, z, TFWorldUtil.id(replaceBlockID) + (replaceMeta << 12));
			}
			else if (replaceBlockID == null)
			{
				worldObj.playAuxSFX(2001, x, y, z, thereBlockID.blockID + (thereMeta << 12));
				thereBlockID.dropBlockAsEntityItem(dropInfo);

			}
		}

		return true;
	}

	private boolean isUnrevertable(Block thereBlockID, byte thereMeta, Block replaceBlockID, byte replaceMeta) 
	{
		if (thereBlockID == TFBlocks.towerDevice || replaceBlockID == TFBlocks.towerDevice)
		{
			return true;
		}
		if ((thereBlockID == TFBlocks.towerTranslucent && thereMeta != BlockTFTowerTranslucent.META_REVERTER_REPLACEMENT)
				|| (replaceBlockID == TFBlocks.towerTranslucent && replaceMeta != BlockTFTowerTranslucent.META_REVERTER_REPLACEMENT))
		{
			return true;
		}
		if (thereBlockID == Block.redstoneLampIdle && replaceBlockID == Block.redstoneLampActive)
		{
			return true;
		}
		if (thereBlockID == Block.redstoneLampActive && replaceBlockID == Block.redstoneLampIdle)
		{
			return true;
		}
		if (thereBlockID == Block.waterStill || replaceBlockID == Block.waterMoving)
		{
			return true;
		}
		if (thereBlockID == Block.waterMoving || replaceBlockID == Block.waterStill)
		{
			return true;
		}
		if (replaceBlockID == Block.tnt)
		{
			return true;
		}

		return false;
	}

	private void captureBlockData() 
    {
		blockData = new Block[diameter * diameter * diameter];
		metaData = new byte[diameter * diameter * diameter];
		
		int index = 0;
		
		for (int x = - radius; x <= radius; x++)
		{
			for (int y = - radius; y <= radius; y++)
			{
				for (int z = - radius; z <= radius; z++)
				{
					Block blockID = worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z);
					int meta = worldObj.getBlockMetadata(this.xCoord + x, this.yCoord + y, this.zCoord + z);
					
					blockData[index] = blockID;
					metaData[index] = (byte) meta;
					
					index++;
				}
			}
		}
		
		//System.out.println("Captured data for " + index + " blocks");
	}

	/**
     * Returns true if there is a player in range (using World.getClosestPlayer)
     */
    public boolean anyPlayerInRange()
    {
        return this.worldObj.getClosestPlayer((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D, (double)this.requiredPlayerRange, true) != null;
    }
}
