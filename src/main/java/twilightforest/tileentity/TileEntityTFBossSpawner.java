package twilightforest.tileentity;

import net.minecraft.Entity;
import net.minecraft.EntityCreature;
import net.minecraft.EntityLiving;
import net.minecraft.TileEntity;
import net.minecraft.EntityPlayer;
import net.minecraft.World;
import twilightforest.TFConfig;
import twilightforest.TFTestMode;
import twilightforest.block.BlockTFBossSpawner;
import twilightforest.compat.TFChat;
import twilightforest.compat.TFParticles;

public abstract class TileEntityTFBossSpawner extends TileEntity {

	/** The boss this spawner summons: constructed by class, never by an EntityList name (a name mismatch returned null and crashed). */
	protected final Class<? extends EntityLiving> bossClass;
	protected int counter;

	protected Entity displayCreature = null;

	protected TileEntityTFBossSpawner(Class<? extends EntityLiving> bossClass) {
		this.bossClass = bossClass;
	}

	/**
	 * Is there a player in our detection range?
	 */
	public boolean anyPlayerInRange()
	{
		return worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, getRange(), true) != null;
	}

	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
	 * ticks and creates a new spawn inside its implementation.
	 */
	@Override
	public void updateEntity()
	{
		if (!BlockTFBossSpawner.verifyTileEntity(this))
		{
			return;
		}

		this.counter++;

		if (anyPlayerInRange())
		{
			if (worldObj.isRemote)
			{
				// particles
				double rx = xCoord + worldObj.rand.nextFloat();
				double ry = yCoord + worldObj.rand.nextFloat();
				double rz = zCoord + worldObj.rand.nextFloat();
				TFParticles.spawn(worldObj, "smoke", rx, ry, rz, 0.0D, 0.0D, 0.0D);
				TFParticles.spawn(worldObj, "flame", rx, ry, rz, 0.0D, 0.0D, 0.0D);
			}
			else
			{
				// difficultySetting 0 == peaceful
				if (worldObj.difficultySetting != 0 && meetsLevelGate())
				{
					spawnMyBoss();

					// destroy block
					worldObj.setBlockToAir(xCoord, yCoord, zCoord, 2);
				}
			}
		}
	}

	/**
	 * Lowest experience level a nearby player needs for this boss to appear (MITE_BALANCE 5.3).
	 */
	protected int getMinLevel() {
		return 10;
	}

	/**
	 * Boss level gate: some survival player within the spawn range must have the level (creative players always pass).
	 * Otherwise the block stays and the players nearby are told, at most every 5 seconds.
	 */
	protected boolean meetsLevelGate() {
		if (!TFConfig.bossMinLevelGate || TFTestMode.isOn(worldObj)) {
			return true;
		}
		double range = TFConfig.bossSpawnRange;
		int needed = getMinLevel() + TFConfig.bossMinLevelOffset;
		boolean anyone = false;
		for (Object o : worldObj.playerEntities) {
			EntityPlayer player = (EntityPlayer) o;
			if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) > range * range) {
				continue;
			}
			if (player.capabilities.isCreativeMode || player.getExperienceLevel() >= needed) {
				return true;
			}
			anyone = true;
		}
		if (anyone && counter % 100 == 0) {
			for (Object o : worldObj.playerEntities) {
				EntityPlayer player = (EntityPlayer) o;
				if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= range * range) {
					TFChat.sendTranslated(player, "tf.boss.too_heavy", needed);
				}
			}
		}
		return false;
	}

	/**
	 * Spawn the boss
	 */
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

	/**
	 * Get a temporary copy of the creature we're going to summon for display purposes
	 */
	public Entity getDisplayEntity()
	{
		if (this.displayCreature == null)
		{
			this.displayCreature = makeMyCreature();
		}

		return this.displayCreature;
	}

	/**
	 * Any post-creation initialization goes here
	 */
	protected void initializeCreature(EntityLiving myCreature) {
		if (myCreature instanceof EntityCreature)
		{
			((EntityCreature) myCreature).setHomeArea(xCoord, yCoord, zCoord, 46);
		}
	}

	/**
	 * Range?
	 */
	protected int getRange() {
		return 50;
	}

	/**
	 * Create a copy of what we spawn
	 */
	protected EntityLiving makeMyCreature() {
		return createEntity(bossClass, worldObj);
	}

	/** Startup self check: can this spawner construct its boss (and its display entity)? */
	public boolean canCreateBoss(World world) {
		try {
			return createEntity(bossClass, world) != null && createEntity(bossClass, world) instanceof EntityLiving;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/** Builds an entity by class through its (World) constructor; fails loudly instead of returning null. */
	protected static <T extends Entity> T createEntity(Class<T> entityClass, World world) {
		try {
			return entityClass.getConstructor(World.class).newInstance(world);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot construct " + entityClass.getName() + " for a boss spawner", e);
		}
	}
}
