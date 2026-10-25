package twilightforest.tileentity;

import net.minecraft.EntityCreature;
import net.minecraft.EntityLiving;
import net.minecraft.EntityPlayer;
import twilightforest.entity.boss.EntityTFMinoshroom;

/**
 * Labyrinth boss room spawner (TF 1.12 places a boss spawner here too). The room used to spawn the Minoshroom straight
 * from worldgen, which left some rooms empty; a spawner only spawns it with a player in the room.
 */
public class TileEntityTFMinoshroomSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFMinoshroomSpawner() {
		super(EntityTFMinoshroom.class);
	}

	/**
	 * The player has to be inside the 16x16 room, on the room's level (the maze above is 10 blocks higher)
	 */
	@Override
	public boolean anyPlayerInRange()
	{
		EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, getRange(), true);

		return closestPlayer != null && Math.abs(closestPlayer.posY - yCoord) < 4.0D;
	}

	@Override
	protected int getRange() {
		return 12;
	}

	/**
	 * On the floor (not at block center height): the ceiling mushrooms above the room center would suffocate it
	 */
	@Override
	protected void initializeCreature(EntityLiving myCreature) {
		myCreature.setLocationAndAngles(xCoord + 0.5D, yCoord, zCoord + 0.5D, myCreature.rotationYaw, 0.0F);

		if (myCreature instanceof EntityCreature)
		{
			((EntityCreature) myCreature).setHomeArea(xCoord, yCoord, zCoord, 7);
		}
	}

	@Override
	protected int getMinLevel() {
		return EntityTFMinoshroom.MIN_LEVEL;
	}
}
