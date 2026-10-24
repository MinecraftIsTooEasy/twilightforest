package twilightforest.item;

import java.util.HashMap;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Entity;
import net.minecraft.EntityBat;
import net.minecraft.EntityCaveSpider;
import net.minecraft.EntityChicken;
import net.minecraft.EntityCow;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPig;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySheep;
import net.minecraft.EntitySpider;
import net.minecraft.EntityWolf;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFParticles;
import twilightforest.entity.EntityTFHedgeSpider;
import twilightforest.entity.EntityTFHostileWolf;
import twilightforest.entity.EntityTFSwarmSpider;
import twilightforest.entity.passive.EntityTFBighorn;
import twilightforest.entity.passive.EntityTFBoar;
import twilightforest.entity.passive.EntityTFDeer;
import twilightforest.entity.passive.EntityTFPenguin;
import twilightforest.entity.passive.EntityTFRaven;

public class ItemTFTransformPowder extends ItemTF {

	HashMap<Class<? extends Entity>, Class<? extends Entity>> transformMap;

	/** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
	protected ItemTFTransformPowder(int id) {
		super(id, Material.bone);
		this.setMaxStackSize(64);

		transformMap = new HashMap<Class<? extends Entity>, Class<? extends Entity>>();

		// MITE_BALANCE 7.7: only the 8 safe pairs. Disabled: Minotaur<->PigZombie (gold nuggets),
		// Wraith<->Blaze (blaze rods), Redcap<->Villager, SkeletonDruid<->Witch (curse).
		addTwoWayTransformation(EntityTFDeer.class, EntityCow.class);
		addTwoWayTransformation(EntityTFBighorn.class, EntitySheep.class);
		addTwoWayTransformation(EntityTFBoar.class, EntityPig.class);
		addTwoWayTransformation(EntityTFRaven.class, EntityBat.class);
		addTwoWayTransformation(EntityTFHostileWolf.class, EntityWolf.class);
		addTwoWayTransformation(EntityTFPenguin.class, EntityChicken.class);
		addTwoWayTransformation(EntityTFHedgeSpider.class, EntitySpider.class);
		addTwoWayTransformation(EntityTFSwarmSpider.class, EntityCaveSpider.class);
	}

	public void addTwoWayTransformation(Class<? extends Entity> class1, Class<? extends Entity> class2) {
		transformMap.put(class1, class2);
		transformMap.put(class2, class1);
	}

	/**
	 * dye sheep, place saddles, etc ...
	 */
	@Override
	public boolean tryEntityInteraction(Entity target, EntityPlayer player, ItemStack stack) {
		// this is where we transform normal entities into twilight entities and vice versa

		Class<? extends Entity> transformClass = getMonsterTransform(target.getClass());

		if (transformClass == null) {
			return false;
		}

		if (target.worldObj.isRemote) {
			// particles, sound
			if (target instanceof EntityLiving) {
				((EntityLiving) target).spawnExplosionParticle();
				((EntityLiving) target).spawnExplosionParticle();
			}

			target.worldObj.playSound(target.posX + 0.5D, target.posY + 0.5D, target.posZ + 0.5D, "mob.zombie.remedy",
				1.0F + itemRand.nextFloat(), itemRand.nextFloat() * 0.7F + 0.3F, false);
		} else {
			// add new monster with old monster's position
			Entity newMonster = null;
			try {
				newMonster = transformClass.getConstructor(World.class).newInstance(target.worldObj);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (newMonster == null) {
				return false;
			}

			newMonster.setPositionAndRotation(target.posX, target.posY, target.posZ, target.rotationYaw, target.rotationPitch);

			target.worldObj.spawnEntityInWorld(newMonster);

			// remove original monster
			target.setDead();

			if (!player.inCreativeMode()) {
				player.convertOneOfHeldItem(null);
			}
		}

		return true;
	}

	/**
	 * Callback for item usage. If the item does something special on right clicking, he will have one of those.
	 */
	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
		if (player.onClient()) {
			World world = player.worldObj;
			AxisAlignedBB fanBox = getEffectAABB(world, player);

			// particle effect
			for (int i = 0; i < 30; i++) {
				TFParticles.spawn(world, "magicCrit",
					fanBox.minX + world.rand.nextFloat() * (fanBox.maxX - fanBox.minX),
					fanBox.minY + world.rand.nextFloat() * (fanBox.maxY - fanBox.minY),
					fanBox.minZ + world.rand.nextFloat() * (fanBox.maxZ - fanBox.minZ),
					0, 0, 0);
			}
			player.swingArm();
		}

		return true;
	}

	private AxisAlignedBB getEffectAABB(World world, EntityPlayer player) {
		double range = 2.0D;
		double radius = 1.0D;
		Vec3 srcVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3 lookVec = player.getLookVec();
		Vec3 destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

		return AxisAlignedBB.getBoundingBox(destVec.xCoord - radius, destVec.yCoord - radius, destVec.zCoord - radius,
			destVec.xCoord + radius, destVec.yCoord + radius, destVec.zCoord + radius);
	}

	/**
	 * Which class should the specified monster class transform into?
	 */
	public Class<? extends Entity> getMonsterTransform(Class<? extends Entity> originalMonster) {
		return transformMap.get(originalMonster);
	}
}
