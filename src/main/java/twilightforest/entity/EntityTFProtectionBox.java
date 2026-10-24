package twilightforest.entity;

import net.minecraft.Entity;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;

public class EntityTFProtectionBox extends Entity {

	public int lifeTime;
	public int sizeX;
	public int sizeY;
	public int sizeZ;

	public EntityTFProtectionBox(World worldObj, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		super(worldObj);

		this.setLocationAndAngles(minX, minY, minZ, 0.0F, 0.0F);

		sizeX = Math.abs(maxX - minX) + 1;
		sizeY = Math.abs(maxY - minY) + 1;
		sizeZ = Math.abs(maxZ - minZ) + 1;

		this.setSize(Math.max(sizeX, sizeZ), sizeY);

		this.lifeTime = 60;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (lifeTime <= 1) {
			setDead();
		} else {
			lifeTime--;
		}
	}

	/**
	 * Gets how bright this entity is.
	 */
	@Override
	public float getBrightness(float par1) {
		return 1.0F;
	}

	@Override
	public int getBrightnessForRender(float par1) {
		return 15728880;
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	@Override
	public boolean canRenderOnFire() {
		return false;
	}

}
