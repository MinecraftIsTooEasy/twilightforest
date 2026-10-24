package twilightforest.item;

import net.minecraft.EnchantmentHelper;
import net.minecraft.Enchantment;
import net.minecraft.EntityArrow;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemArrow;
import net.minecraft.ItemStack;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class ItemTFTripleBow extends ItemTFBowBase {

    /** Side arrow deviation (MITE_BALANCE 7.4: +-6 degrees). */
    private static final double SPREAD_RADIANS = Math.toRadians(6.0D);
    /** Damage multiplier per arrow (MITE_BALANCE 7.4). */
    private static final double DAMAGE_MULTIPLIER = 0.6D;

    public ItemTFTripleBow(int id) {
        super(id);
        this.setTextureName(TwilightForestMod.ID + ":triplebow");
        this.setMaxDamage(64); // MITE_BALANCE 7.4
    }

    /** MITE_BALANCE 7.4: -2 durability per shot. */
    @Override
    protected int getDurabilityPerShot() {
        return 2;
    }

    /**
     * Fires the centre arrow plus two deviated side arrows. Only one arrow of ammo is consumed (by the base class);
     * every arrow does 0.6x damage and side arrows cannot be picked up.
     */
    @Override
    protected void spawnArrows(World world, EntityPlayer player, EntityArrow primary, float launchVelocity, ItemArrow arrow, ItemStack bow) {
        primary.setDamage(primary.getDamage() * DAMAGE_MULTIPLIER);

        int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        boolean flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0;

        world.spawnEntityInWorld(primary);

        for (int sign = -1; sign <= 1; sign += 2) {
            // recreate with the same launch parameters, then deviate
            EntityArrow side = getArrow(world, player, launchVelocity, arrow, bow.isItemEnchanted());
            side.setDamage(primary.getDamage());
            side.setIsCritical(primary.getIsCritical());
            if (punchLevel > 0) {
                side.setKnockbackStrength(punchLevel);
            }
            if (flame) {
                side.setFire(100);
            }
            rotateHorizontally(side, sign * SPREAD_RADIANS);
            side.canBePickedUp = 0;
            // the side arrows hit on the same tick as the centre arrow, so they must ignore the invulnerability frames
            ((twilightforest.compat.ITFNoIFrames) side).tf$setNoIFrames(true);
            world.spawnEntityInWorld(side);
        }
    }

    private static void rotateHorizontally(EntityArrow arrow, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = arrow.motionX;
        double z = arrow.motionZ;
        arrow.motionX = x * cos - z * sin;
        arrow.motionZ = x * sin + z * cos;
        arrow.rotationYaw = (float) (Math.atan2(arrow.motionX, arrow.motionZ) * 180.0D / Math.PI);
        arrow.prevRotationYaw = arrow.rotationYaw;
    }

}
