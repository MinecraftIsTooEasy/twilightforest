package twilightforest.item;

import net.minecraft.DamageSource;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.EntityArrow;
import net.minecraft.EntityPlayer;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemArrow;
import net.minecraft.ItemBow;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.World;

public abstract class ItemTFBowBase extends ItemBow {

    private static final String[] PULL_ICON_NAMES = { "pulling_0", "pulling_1", "pulling_2" };

    private Icon[] tfIconArray;

    /** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
    protected ItemTFBowBase(int id) {
        super(id, Material.wood);
    }

    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(this.getIconString() + "_standby");
        this.tfIconArray = new Icon[PULL_ICON_NAMES.length];

        for (int i = 0; i < this.tfIconArray.length; ++i) {
            this.tfIconArray[i] = register.registerIcon(this.getIconString() + "_" + PULL_ICON_NAMES[i]);
        }
    }

    /**
     * Used to cycle through icons based on pull stage (0..2); MITE picks the stage in EntityPlayer.getItemIcon.
     */
    @Override
    public Icon getItemIconForUseDuration(int stage, EntityPlayer player) {
        return this.tfIconArray[stage];
    }

    /**
     * Called when the player releases the use item button.
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int itemInUseCount) {
        if (world.isRemote) {
            return;
        }

        ItemArrow arrow = player.inventory.getReadiedArrow();
        if (arrow == null) {
            if (!player.inCreativeMode()) {
                return;
            }
            arrow = player.nocked_arrow;
            if (arrow == null) {
                arrow = Item.arrowFlint;
            }
        }

        float velocity = getFractionPulled(itemstack, itemInUseCount);
        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;

        if (velocity < 0.1F) {
            return;
        }

        if (velocity > 1.0F) {
            velocity = 1.0F;
        }

        EntityArrow entityarrow = getArrow(world, player, velocity * 2.0F, arrow, itemstack.isItemEnchanted());
        player.nocked_arrow = null;

        if (velocity == 1.0F) {
            entityarrow.setIsCritical(true);
        }

        int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);
        if (powerLevel > 0) {
            entityarrow.setDamage(entityarrow.getDamage() + (double) powerLevel * 0.5D + 0.5D);
        }

        int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack);
        if (punchLevel > 0) {
            entityarrow.setKnockbackStrength(punchLevel);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0) {
            entityarrow.setFire(100);
        }

        player.tryDamageHeldItem(DamageSource.generic, getDurabilityPerShot());
        world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);

        if (player.inCreativeMode()) {
            entityarrow.canBePickedUp = 2;
        } else {
            player.inventory.consumeArrow();
        }

        spawnArrows(world, player, entityarrow, velocity * 2.0F, arrow, itemstack);
    }

    /** Spawns the fully configured arrow(s); Triple Bow adds two side arrows. */
    protected void spawnArrows(World world, EntityPlayer player, EntityArrow primary, float launchVelocity, ItemArrow arrow, ItemStack bow) {
        world.spawnEntityInWorld(primary);
    }

    /** Durability lost per shot (Triple Bow costs 2, MITE_BALANCE 7.4). */
    protected int getDurabilityPerShot() {
        return 1;
    }

    protected EntityArrow getArrow(World world, EntityPlayer player, float velocity, ItemArrow arrow, boolean launcherEnchanted) {
        return new EntityArrow(world, player, velocity, arrow, launcherEnchanted);
    }

}
