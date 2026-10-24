package twilightforest.item;

import java.util.WeakHashMap;

import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EntityTFCubeOfAnnihilation;

/**
 * Cube of Annihilation. Creative-only in MITE (MITE_BALANCE 7.2 / 17): no recipe, no loot.
 * TODO(mite-port): the empty onUsingTick and onItemUse (always false) of 1.7.10 are dropped, MITE has neither hook.
 */
public class ItemTFCubeOfAnnihilation extends ItemTF {

    private Icon annihilateIcon;

    private final WeakHashMap<ItemStack, Entity> launchedCubesMap = new WeakHashMap<ItemStack, Entity>();

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.CUBE_OF_ANNIHILATION) */
    public ItemTFCubeOfAnnihilation(int id) {
        super(id, Material.iron);
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) {
            return false;
        }
        World world = player.worldObj;

        player.setHeldItemInUse();

        if (!world.isRemote && !this.hasLaunchedCube(stack)) {
            EntityTFCubeOfAnnihilation launchedCube = new EntityTFCubeOfAnnihilation(world, player);

            world.spawnEntityInWorld(launchedCube);

            this.setLaunchedCube(stack, launchedCube);
            setCubeAsThrown(stack);
        }

        return true;
    }

    /**
     * Set this item as having been thrown
     */
    public static void setCubeAsThrown(ItemStack stack) {
        // set NBT tag for stack
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean("thrown", true);
    }

    /**
     * Set the cube for this item as returned to the player
     */
    public static void setCubeAsReturned(ItemStack stack) {
        // set NBT tag for stack
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean("thrown", false);
    }

    /**
     * Method for the client to determine if the cube has been thrown or not.  Not as accurate as server method due to lag, etc.
     */
    public static boolean doesTalismanHaveCube(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return true;
        } else {
            return !stack.getTagCompound().getBoolean("thrown");
        }
    }

    /**
     * Set the cube belonging to the player as returned
     */
    public static void setCubeAsReturned(EntityPlayer player) {
        if (player != null && player.getHeldItemStack() != null && player.getHeldItemStack().getItem() == TFItems.cubeOfAnnihilation) {
            setCubeAsReturned(player.getHeldItemStack());
        }
    }

    /**
     * Player, item usage sensitive icon: the talisman once the cube is thrown.
     * TODO(mite-port): MITE has no Item.getIcon(stack, pass, player, using, remaining) hook; wire into EntityPlayer.getItemIcon
     * via the client icon mixin (TF_FORGE_HOOKS M23) or the item renderer.
     */
    public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (doesTalismanHaveCube(stack)) {
            return this.itemIcon;
        } else {
            return TFItems.cubeTalisman.getIconIndex(stack);
        }
    }

    public boolean hasLaunchedCube(ItemStack stack) {
        Entity cube = this.launchedCubesMap.get(stack);

        return cube != null && !cube.isDead;
    }

    public void setLaunchedCube(ItemStack stack, EntityTFCubeOfAnnihilation launchedCube) {
        this.launchedCubesMap.put(stack, launchedCube);
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BLOCK;
    }

    /**
     * Properly register icon source
     */
    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(TwilightForestMod.ID + ":" + this.getUnlocalizedName().substring(5));
        this.annihilateIcon = register.registerIcon(TwilightForestMod.ID + ":annihilate_particle");
    }

    public Icon getAnnihilateIcon() {
        return this.annihilateIcon;
    }
}
