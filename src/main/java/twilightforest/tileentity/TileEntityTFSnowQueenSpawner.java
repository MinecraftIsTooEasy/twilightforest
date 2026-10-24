package twilightforest.tileentity;

import net.minecraft.EntityLiving;
import net.minecraft.EntityPlayer;
import twilightforest.entity.boss.EntityTFSnowQueen;

public class TileEntityTFSnowQueenSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFSnowQueenSpawner() {
		super(EntityTFSnowQueen.class);
	}

	@Override
	public boolean anyPlayerInRange()
	{
		EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 9D, true);

		return closestPlayer != null && closestPlayer.posY > yCoord - 4;
	}

	@Override
	protected void spawnMyBoss() {
		// spawn creature
		EntityLiving myCreature = makeMyCreature();

		double rx = xCoord + 0.5D;
		double ry = yCoord + 0.5D;
		double rz = zCoord + 0.5D;
		myCreature.setLocationAndAngles(rx, ry, rz, worldObj.rand.nextFloat() * 360F, 0.0F);

		// set creature's home to this
		initializeCreature(myCreature);

		// spawn it
		worldObj.spawnEntityInWorld(myCreature);
	}

	@Override
	protected int getMinLevel() {
		return 30;
	}
}
