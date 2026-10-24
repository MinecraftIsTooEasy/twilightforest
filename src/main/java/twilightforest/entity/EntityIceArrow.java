package twilightforest.entity;

import net.minecraft.EntityArrow;
import net.minecraft.EntityLivingBase;
import net.minecraft.Item;
import net.minecraft.ItemArrow;
import net.minecraft.World;

public class EntityIceArrow extends EntityArrow {

	public EntityIceArrow(World par1World) {
		super(par1World);
		// MITE: EntityList/client spawn path has no arrow item, which the model item / fire checks need
		this.item_arrow = Item.arrowFlint;
	}

	/**
	 * MITE: arrows are created with the arrow item that was nocked (1.7.10 had (World, EntityPlayer, float)).
	 */
	public EntityIceArrow(World world, EntityLivingBase shooter, float velocity, ItemArrow itemArrow, boolean launcherWasEnchanted) {
		super(world, shooter, velocity, itemArrow, launcherWasEnchanted);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();
	}
}
