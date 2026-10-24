package twilightforest.item;

import java.util.List;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.EnumRarity;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.RaycastCollision;
import net.minecraft.TileEntity;
import net.minecraft.TileEntitySkull;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFBlocks;

public class ItemTFTrophy extends ItemTF {
    private static final String[] trophyTypes = new String[] {"hydra", "naga", "lich", "ur-ghast", "snowQueen"};
    public static final String[] trophyTextures = new String[] {"hydraTrophy", "nagaTrophy", "lichTrophy", "urGhastTrophy", "snowQueenTrophy"};
    public Icon[] trophyIcons;

    /** id is the Item(int) constructor argument, i.e. TFIds.item(finalId). */
    public ItemTFTrophy(int id) {
        super(id);
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    /**
     * returns a list of items with the same ID, but different subtype (called from the Item constructor, so it must
     * only use static data)
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(int id, CreativeTabs tab, List list) {
        for (int j = 0; j < trophyTypes.length; ++j) {
            list.add(new ItemStack(id, 1, j));
        }
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    @Override
    public int getMetadata(int subtype) {
        return subtype;
    }

    /**
     * Places the trophy block (1.7.10 onItemUse). The placement pipeline (solid-face check, edit rights, collision,
     * item consumption) is MITE's tryPlaceHeldItemAsBlock; BlockTFTrophy decides the metadata (1 = floor, 2-5 = wall).
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        RaycastCollision rc = player.getSelectedObject(partial_tick, false);
        if (rc == null || !rc.isBlock() || rc.face_hit == EnumFace.BOTTOM) {
            return false;
        }

        // the held stack is consumed by the placement, so remember what we need first
        int trophySubtype = player.getHeldItemStack().getItemSubtype();
        EnumFace face = rc.face_hit;
        if (!player.tryPlaceHeldItemAsBlock(rc, TFBlocks.trophy)) {
            return false;
        }

        if (player.onServer()) {
            int skullRotate = 0;
            if (face == EnumFace.TOP) {
                skullRotate = MathHelper.floor_double((double) (player.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
            }
            World world = player.worldObj;
            // the block lands either on the neighbour of the clicked block or replaces the clicked block
            if (!setSkull(world, rc.neighbor_block_x, rc.neighbor_block_y, rc.neighbor_block_z, trophySubtype, skullRotate)) {
                setSkull(world, rc.block_hit_x, rc.block_hit_y, rc.block_hit_z, trophySubtype, skullRotate);
            }
        }
        return true;
    }

    private static boolean setSkull(World world, int x, int y, int z, int subtype, int rotation) {
        if (world.getBlock(x, y, z) != TFBlocks.trophy) {
            return false;
        }
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull skull = (TileEntitySkull) tileEntity;
            skull.setSkullType(subtype & 255, "");
            skull.setSkullRotation(rotation);
            world.markBlockForUpdate(x, y, z);
            return true;
        }
        return false;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their subtype.
     */
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (stack == null) {
            return super.getUnlocalizedName();
        }
        int i = stack.getItemSubtype();
        if (i < 0 || i >= trophyTypes.length) {
            i = 0;
        }
        return super.getUnlocalizedName() + "." + trophyTypes[i];
    }

    /**
     * Properly register icon source
     */
    @Override
    public void registerIcons(IconRegister register) {
        this.trophyIcons = new Icon[trophyTextures.length];
        for (int i = 0; i < trophyTextures.length; ++i) {
            this.trophyIcons[i] = register.registerIcon(TwilightForestMod.ID + ":" + trophyTextures[i]);
        }
    }

    /**
     * Gets an icon index based on an item's subtype
     */
    @Override
    public Icon getIconFromSubtype(int subtype) {
        if (subtype < 0 || subtype >= trophyTypes.length) {
            subtype = 0;
        }
        return this.trophyIcons[subtype];
    }
}
