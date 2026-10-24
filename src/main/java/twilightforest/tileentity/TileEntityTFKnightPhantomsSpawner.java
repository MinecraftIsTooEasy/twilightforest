package twilightforest.tileentity;

import net.minecraft.EntityLiving;
import net.minecraft.EntityPlayer;
import twilightforest.entity.boss.EntityTFKnightPhantom;

public class TileEntityTFKnightPhantomsSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFKnightPhantomsSpawner() {
		super(EntityTFKnightPhantom.class);
	}

	@Override
	public boolean anyPlayerInRange()
	{
		EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 9D, true);

		return closestPlayer != null && closestPlayer.posY > yCoord - 2;
	}

	/**
	 * Spawn the boss
	 */
	@Override
	protected void spawnMyBoss() {

		for (int i = 0; i < 6; i++)
		{

			// spawn creature
			EntityLiving myCreature = makeMyCreature();

			float angle = 60F * i;
			float distance = 4F;

			double rx = xCoord + 0.5D + Math.cos((angle) * Math.PI / 180.0D) * distance;
			double ry = yCoord + 0.5D;
			double rz = zCoord + 0.5D + Math.sin((angle) * Math.PI / 180.0D) * distance;

			myCreature.setLocationAndAngles(rx, ry, rz, worldObj.rand.nextFloat() * 360F, 0.0F);

			// set creature's home to this
			initializeCreature(myCreature);

			((EntityTFKnightPhantom) myCreature).setNumber(i);

			// spawn it
			worldObj.spawnEntityInWorld(myCreature);
		}
	}

	/**
	 * Any post-creation initialization goes here
	 */
	@Override
	protected void initializeCreature(EntityLiving myCreature) {
		if (myCreature instanceof EntityTFKnightPhantom)
		{
			((EntityTFKnightPhantom) myCreature).setHomeArea(xCoord, yCoord, zCoord, 46);
		}
	}

	@Override
	protected int getMinLevel() {
		return 20;
	}
}
