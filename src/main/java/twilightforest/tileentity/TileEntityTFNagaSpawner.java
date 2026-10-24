package twilightforest.tileentity;

import net.minecraft.EntityCreature;
import net.minecraft.EntityLiving;
import twilightforest.entity.boss.EntityTFNaga;

public class TileEntityTFNagaSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFNagaSpawner() {
		super(EntityTFNaga.class);
	}

	@Override
	public boolean anyPlayerInRange()
	{
		return worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 50D, true) != null;
	}

	/**
	 * Any post-creation initialization goes here
	 */
	@Override
	protected void initializeCreature(EntityLiving myCreature) {

		if (myCreature instanceof EntityCreature)
		{
			((EntityCreature) myCreature).setHomeArea(xCoord, yCoord, zCoord, 46);
		}
	}

	/**
	 * Range?
	 */
	@Override
	protected int getRange() {
		return 50;
	}

	@Override
	protected int getMinLevel() {
		return 10;
	}
}
