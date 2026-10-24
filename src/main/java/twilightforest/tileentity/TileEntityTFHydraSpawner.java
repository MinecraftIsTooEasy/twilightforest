package twilightforest.tileentity;

import net.minecraft.Entity;
import twilightforest.entity.boss.EntityTFHydra;
import twilightforest.entity.boss.EntityTFHydraHead;

public class TileEntityTFHydraSpawner extends TileEntityTFBossSpawner {

	public TileEntityTFHydraSpawner() {
		super(EntityTFHydra.class);
	}

	/**
	 * Get a temporary copy of the creature we're going to summon for display purposes
	 */
	@Override
	public Entity getDisplayEntity()
	{
		if (this.displayCreature == null)
		{
			this.displayCreature = createEntity(EntityTFHydraHead.class, worldObj);
		}

		return this.displayCreature;
	}

	@Override
	protected int getMinLevel() {
		return 20;
	}
}
