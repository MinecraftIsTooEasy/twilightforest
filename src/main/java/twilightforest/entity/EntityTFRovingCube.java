package twilightforest.entity;

import net.minecraft.EntityAIAttackOnCollide;
import net.minecraft.EntityAIHurtByTarget;
import net.minecraft.EntityAINearestAttackableTarget;
import net.minecraft.EntityMob;
import net.minecraft.EntityPlayer;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.ai.EntityAICubeCenterOnSymbol;
import twilightforest.entity.ai.EntityAICubeMoveToRedstoneSymbols;

public class EntityTFRovingCube extends EntityMob {

	// data needed for cube AI

	// last circle visited
	public boolean hasFoundSymbol = false;
	public int symbolX = 0;
	public int symbolY = 0;
	public int symbolZ = 0;

	public EntityTFRovingCube(World world) {
		super(world);
		setSize(1.2F, 2.1F);

		this.tasks.addTask(0, new EntityAICubeMoveToRedstoneSymbols(this, 1.0D));
		this.tasks.addTask(1, new EntityAICubeCenterOnSymbol(this, 1.0D));
		// MITE_BALANCE 9.3: the cube hits for 12 while chasing redstone symbols; the 1.7.10 original had no attack task
		this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(24.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(12.0D);
	}

	/** MITE_BALANCE 9.3: 10 XP, no natural defense. */
	@Override
	public int getExperienceValue() {
		return 10;
	}

	@Override
	public float getReach() {
		return super.getReach() + this.width * 0.5F;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required.
	 */
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// make annihilation particles
		for (int i = 0; i < 3; i++) {
			float px = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.75F;
			float py = this.getEyeHeight() - 0.25F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.75F;
			float pz = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.75F;

			TwilightForestMod.proxy.spawnParticle(this.worldObj, "annihilate", this.lastTickPosX + px, this.lastTickPosY + py, this.lastTickPosZ + pz, 0, 0, 0);
		}
	}

}
