package twilightforest.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.EnumRarity;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.RaycastCollision;
import net.minecraft.Slot;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFChat;
import twilightforest.entity.EntityTFLoyalZombie;

public class ItemTFZombieWand extends ItemTF implements IDamageableItem {

    /** MITE_BALANCE 7.5: at most 2 loyal zombies per player at the same time. */
    private static final int MAX_ZOMBIES_PER_PLAYER = 2;
    /** MITE_BALANCE 7.5: cooldown between two summons, in ticks. */
    private static final long COOLDOWN_TICKS = 100L;
    private static final double RANGE = 20.0D;

    /** player name -> world time of the last summon (server only) */
    private static final Map<String, Long> lastSummon = new HashMap<String, Long>();

    protected ItemTFZombieWand(int id) {
        super(id);
        this.setMaxStackSize(1);
        // MITE_BALANCE 7.5: 6 charges (original 9)
        this.setMaxDamage(6);
    }

    @Override
    public int getNumComponentsForDurability() {
        return 0;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) {
            return false;
        }
        if (stack.getItemDamage() >= stack.getMaxDamage()) {
            return false;
        }

        // note: the item is deliberately not put "in use": in MITE a finished use would consume the held item
        if (player.onServer()) {
            World worldObj = player.worldObj;
            String owner = player.getCommandSenderName();
            long now = worldObj.getTotalWorldTime();

            Long last = lastSummon.get(owner);
            if (last != null && now >= last && now - last < COOLDOWN_TICKS) {
                return true;
            }

            // what block is the player pointing at?
            RaycastCollision rc = getPlayerPointVec(worldObj, player, RANGE);

            if (rc != null && rc.isBlock()) {
                if (countLoyalZombies(worldObj, owner) >= MAX_ZOMBIES_PER_PLAYER) {
                    TFChat.sendTranslated(player, "tf.zombiewand.limit", MAX_ZOMBIES_PER_PLAYER);
                    return true;
                }

                // make a zombie in the open block in front of the pointed face
                EntityTFLoyalZombie zombie = new EntityTFLoyalZombie(worldObj);
                zombie.setPositionAndRotation(rc.neighbor_block_x + 0.5D, rc.neighbor_block_y, rc.neighbor_block_z + 0.5D, player.rotationYaw, 0.0F);
                zombie.setTamed(true);
                zombie.setOwner(owner);
                // TODO(mite-port): original gave Strength II (1200 ticks); dropped, MITE_BALANCE 7.5 fixes the zombie attack at 6
                worldObj.spawnEntityInWorld(zombie);

                lastSummon.put(owner, now);
                if (!player.inCreativeMode()) {
                    // not tryDamageItem: the wand must stay in the inventory when empty so that it can be recharged by crafting
                    stack.setItemDamage(stack.getItemDamage() + 1);
                }
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private static int countLoyalZombies(World worldObj, String owner) {
        int count = 0;
        List entities = worldObj.loadedEntityList;
        for (Object o : entities) {
            if (o instanceof EntityTFLoyalZombie) {
                EntityTFLoyalZombie zombie = (EntityTFLoyalZombie) o;
                if (!zombie.isDead && owner.equalsIgnoreCase(zombie.getOwnerName())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * What block is the player pointing the wand at?
     *
     * This very similar to player.rayTrace, but that method is not available on the server.
     */
    private RaycastCollision getPlayerPointVec(World worldObj, EntityPlayer player, double range) {
        Vec3 position = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLook(1.0F);
        Vec3 dest = position.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        return worldObj.getBlockCollisionForSelection(position, dest, false);
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 30;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BOW;
    }

    /**
     * Return an item rarity from EnumRarity
     */
    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.rare;
    }

    /**
     * Display charges left in tooltip
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extended_info, Slot slot) {
        info.add(net.minecraft.StatCollector.translateToLocalFormatted("tf.tooltip.charges", stack.getMaxDamage() - stack.getItemDamage()));
    }
}
