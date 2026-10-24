package twilightforest.tileentity;

import net.minecraft.EntityPlayer;
import twilightforest.entity.boss.EntityTFLich;

public class TileEntityTFLichSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFLichSpawner() {
		super(EntityTFLich.class);
	}

	@Override
	public boolean anyPlayerInRange()
	{
		EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 9D, true);

		return closestPlayer != null && closestPlayer.posY > yCoord - 4;
	}

	@Override
	protected int getMinLevel() {
		return 12;
	}
}
