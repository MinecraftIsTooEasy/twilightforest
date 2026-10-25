package twilightforest.item;

import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.BlockFlower;
import net.minecraft.CreativeTabs;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.IDamageableItem;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.Material;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFParticles;

/**
 * Peacock fan. MITE_BALANCE 7: 256 uses (was 1024), 1 use per successful fan.
 */
public class ItemTFPeacockFan extends ItemTF implements IDamageableItem {

    /** @param id the Item constructor argument, i.e. TFIds.item(TFIds.PEACOCK_FAN) */
    protected ItemTFPeacockFan(int id) {
        super(id, Material.bone);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setMaxStackSize(1);
        this.setMaxDamage(256);
        this.setFull3D();
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
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        World world = player.worldObj;

        if (player.onServer()) {
            if (!player.onGround) {
                player.addPotionEffect(new PotionEffect(Potion.jump.id, 45, 0));
            } else {
                int fanned = doFan(world, player);

                if (fanned > 0) {
                    player.tryDamageHeldItem(DamageSource.generic, fanned);
                }
            }
        } else {
            // jump if the player is in the air, once per time off the ground. 1.7.10 gated this on the jump boost the server
            // grants for 45 ticks, which also blocked it while any other jump boost was active or right after an earlier fan jump
            if (!player.onGround && !airJumpUsed) {
                airJumpUsed = true;
                player.motionX *= 3F;
                player.motionY = 1.5F;
                player.motionZ *= 3F;
                player.fallDistance = 0.0F;
            } else {
                AxisAlignedBB fanBox = getEffectAABB(world, player);
                Vec3 lookVec = player.getLookVec();

                // particle effect
                for (int i = 0; i < 30; i++) {
                    TFParticles.spawn(world, "cloud", fanBox.minX + world.rand.nextFloat() * (fanBox.maxX - fanBox.minX),
                        fanBox.minY + world.rand.nextFloat() * (fanBox.maxY - fanBox.minY),
                        fanBox.minZ + world.rand.nextFloat() * (fanBox.maxZ - fanBox.minZ),
                        lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
                }
            }

            world.playSound(player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, "random.breath",
                1.0F + itemRand.nextFloat(), itemRand.nextFloat() * 0.7F + 0.3F, false);
        }

        player.setHeldItemInUse();

        return true;
    }

    /** Client: the local player has used the air jump since last standing on the ground. */
    private static boolean airJumpUsed;

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
        if (world.isRemote && entity.onGround) {
            airJumpUsed = false;
        }
    }

    /**
     * The use is only the 20 tick fan animation: the default MITE use-finish would consume the item.
     */
    @Override
    public void onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BLOCK;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }

    /**
     * Fannnn
     */
    private int doFan(World world, EntityPlayer player) {
        AxisAlignedBB fanBox = getEffectAABB(world, player);

        fanBlocksInAABB(world, player, fanBox);

        fanEntitiesInAABB(world, player, fanBox);

        return 1;
    }

    /**
     * Move entities in the box
     */
    private void fanEntitiesInAABB(World world, EntityPlayer player, AxisAlignedBB fanBox) {
        Vec3 moveVec = player.getLookVec();

        List<?> inBox = world.getEntitiesWithinAABB(Entity.class, fanBox);

        float force = 2.0F;

        for (Object o : inBox) {
            Entity entity = (Entity) o;
            if (entity.canBePushed() || entity instanceof EntityItem) {
                entity.motionX = moveVec.xCoord * force;
                entity.motionY = moveVec.yCoord * force;
                entity.motionZ = moveVec.zCoord * force;
            }
        }
    }

    private AxisAlignedBB getEffectAABB(World world, EntityPlayer player) {
        double range = 3.0D;
        double radius = 2.0D;
        Vec3 srcVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 lookVec = player.getLookVec();
        Vec3 destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

        return AxisAlignedBB.getBoundingBox(destVec.xCoord - radius, destVec.yCoord - radius, destVec.zCoord - radius,
            destVec.xCoord + radius, destVec.yCoord + radius, destVec.zCoord + radius);
    }

    /**
     * Do fan effects on blocks in the bounding box
     */
    private int fanBlocksInAABB(World world, EntityPlayer player, AxisAlignedBB box) {
        int minX = MathHelper.floor_double(box.minX);
        int minY = MathHelper.floor_double(box.minY);
        int minZ = MathHelper.floor_double(box.minZ);
        int maxX = MathHelper.floor_double(box.maxX);
        int maxY = MathHelper.floor_double(box.maxY);
        int maxZ = MathHelper.floor_double(box.maxZ);

        int fan = 0;

        for (int dx = minX; dx <= maxX; ++dx) {
            for (int dy = minY; dy <= maxY; ++dy) {
                for (int dz = minZ; dz <= maxZ; ++dz) {
                    fan += fanBlock(world, player, dx, dy, dz);
                }
            }
        }

        return fan;
    }

    private int fanBlock(World world, EntityPlayer player, int dx, int dy, int dz) {
        int cost = 0;

        Block current = world.getBlock(dx, dy, dz);

        if (current instanceof BlockFlower) {
            if (TFBlockUtil.canPlayerHarvest(player, world, dx, dy, dz) && itemRand.nextInt(3) == 0) {
                // destroyBlock plays the break effect and drops the flower
                TFBlockUtil.harvestBlockAs(player, world, dx, dy, dz);
            }
        }

        return cost;
    }
}
