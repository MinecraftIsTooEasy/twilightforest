package twilightforest.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.AABBIntercept;
import net.minecraft.AxisAlignedBB;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.EnumRarity;
import net.minecraft.IBossDisplayData;
import net.minecraft.IDamageableItem;
import net.minecraft.IMob;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.Slot;
import net.minecraft.Vec3;
import net.minecraft.World;
import twilightforest.compat.ITFUsingTickItem;
import twilightforest.compat.TFDamage;
import twilightforest.compat.TFParticles;

public class ItemTFScepterLifeDrain extends ItemTF implements IDamageableItem, ITFUsingTickItem {

    /** Ticks between two drain pulses (MITE_BALANCE 7.5: 10). */
    private static final int PULSE_INTERVAL = 10;
    /** Drain range in blocks (MITE_BALANCE 7.5: 16). */
    private static final double RANGE = 16.0D;
    /** Magic damage per pulse (MITE_BALANCE 7.5: 2). */
    private static final float DRAIN_DAMAGE = 2.0F;
    /** HP the caster regains per pulse (MITE_BALANCE 7.5: 0.5). */
    private static final float HEAL_PER_PULSE = 0.5F;
    /** Hunger spent per pulse that heals (2 hunger per HP). */
    private static final float HUNGER_PER_PULSE = 1.0F;
    /** At most 3 HP healed per 60 seconds. */
    private static final float MAX_HEAL_PER_WINDOW = 3.0F;
    private static final long HEAL_WINDOW_TICKS = 1200L;

    /** player name -> {window start world time, hp healed in window} (server only) */
    private static final Map<String, float[]> healWindows = new HashMap<String, float[]>();

    protected ItemTFScepterLifeDrain(int id) {
        super(id);
        this.setMaxStackSize(1);
        // MITE_BALANCE 7.5: 48 charges (original 99)
        this.setMaxDamage(48);
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
        if (stack.getItemDamage() < stack.getMaxDamage()) {
            player.setHeldItemInUse();
            return true;
        }
        player.stopUsingItem();
        return false;
    }

    /**
     * Animates the target falling apart into a rain of shatter particles
     */
    public static void animateTargetShatter(World worldObj, EntityLivingBase target) {
        for (int var1 = 0; var1 < 50; ++var1) {
            double gaussX = itemRand.nextGaussian() * 0.02D;
            double gaussY = itemRand.nextGaussian() * 0.02D;
            double gaussZ = itemRand.nextGaussian() * 0.02D;
            double gaussFactor = 10.0D;

            Item popItem = getTargetDropItemId(target) != null ? getTargetDropItemId(target) : Item.rottenFlesh;

            TFParticles.spawnItemCrack(worldObj, popItem.itemID, 0,
                    target.posX + itemRand.nextFloat() * target.width * 2.0F - target.width - gaussX * gaussFactor,
                    target.posY + itemRand.nextFloat() * target.height - gaussY * gaussFactor,
                    target.posZ + itemRand.nextFloat() * target.width * 2.0F - target.width - gaussZ * gaussFactor,
                    gaussX, gaussY, gaussZ);
        }
    }

    public static Item getTargetDropItemId(EntityLivingBase target) {
        //TODO: make this actually work
        return Item.rottenFlesh;
    }

    /**
     * What, if anything, is the player currently looking at?
     */
    @SuppressWarnings("rawtypes")
    private Entity getPlayerLookTarget(World worldObj, EntityPlayer player) {
        Entity pointedEntity = null;
        double range = RANGE;
        Vec3 srcVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 lookVec = player.getLook(1.0F);
        Vec3 destVec = srcVec.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
        float var9 = 1.0F;
        List possibleList = worldObj.getEntitiesWithinAABBExcludingEntity(player,
                player.boundingBox.addCoord(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range).expand(var9, var9, var9));
        double hitDist = 0;

        for (Object o : possibleList) {
            Entity possibleEntity = (Entity) o;

            if (possibleEntity.canBeCollidedWith()) {
                float borderSize = possibleEntity.getCollisionBorderSize(player);
                AxisAlignedBB collisionBB = possibleEntity.boundingBox.expand((double) borderSize, (double) borderSize, (double) borderSize);
                AABBIntercept interceptPos = collisionBB.calculateIntercept(worldObj, srcVec, destVec);

                if (collisionBB.isVecInside(srcVec)) {
                    if (0.0D < hitDist || hitDist == 0.0D) {
                        pointedEntity = possibleEntity;
                        hitDist = 0.0D;
                    }
                } else if (interceptPos != null) {
                    double possibleDist = srcVec.distanceTo(interceptPos.position_hit);

                    if (possibleDist < hitDist || hitDist == 0.0D) {
                        pointedEntity = possibleEntity;
                        hitDist = possibleDist;
                    }
                }
            }
        }
        return pointedEntity;
    }

    /** Called each tick while the scepter is in use, from TFEvents.onPlayerTick (MITE has no Forge onUsingTick hook). */
    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        World worldObj = player.worldObj;

        if (stack.getItemDamage() >= stack.getMaxDamage()) {
            // do not use
            player.stopUsingItem();
            return;
        }

        // count is already decremented for this tick (MITE order: EntityPlayer.onUpdate, then Item.onUpdate): count + 1 is what 1.7.10 passed
        if ((count + 1) % PULSE_INTERVAL == 0) {

            // is the player looking at an entity
            Entity pointedEntity = getPlayerLookTarget(worldObj, player);

            // MITE_BALANCE 7.5: only hostile mobs, never bosses, players or passive animals
            if (pointedEntity instanceof EntityLivingBase && pointedEntity instanceof IMob
                    && !(pointedEntity instanceof IBossDisplayData) && !(pointedEntity instanceof EntityPlayer)) {
                EntityLivingBase target = (EntityLivingBase) pointedEntity;

                boolean continuing = target.getActivePotionEffect(Potion.moveSlowdown) != null;

                makeRedMagicTrail(worldObj, player.posX, player.posY + player.getEyeHeight(), player.posZ,
                        target.posX, target.posY + target.getEyeHeight(), target.posZ);
                if (!continuing) {
                    // this is a new creature to start draining
                    worldObj.playSoundAtEntity(player, "fire.ignite", 1.0F, (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F + 1.0F);
                }

                if (!worldObj.isRemote) {
                    // MITE_BALANCE 7.5: 2 magic damage per pulse, no "kill at 3 HP" shatter
                    boolean hurt = TFDamage.hurtByMagic(target, player, player, DRAIN_DAMAGE);

                    // only do lifting effect on creatures weaker than the player
                    if (target.getMaxHealth() <= player.getMaxHealth()) {
                        target.motionX = 0;
                        target.motionY = 0.2;
                        target.motionZ = 0;
                    }

                    target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 2));

                    if (hurt) {
                        healCaster(worldObj, player);
                    }

                    if (!player.inCreativeMode()) {
                        // not tryDamageItem: the scepter must stay in the inventory when empty so that it can be recharged by crafting
                        stack.setItemDamage(stack.getItemDamage() + 1);
                    }
                }
            }
        }
    }

    /**
     * Heals the caster by 0.5 HP for 1 hunger, capped at 3 HP per 60 seconds, and only when the player is not starving.
     */
    private static void healCaster(World worldObj, EntityPlayer player) {
        if (player.getHealth() >= player.getMaxHealth()) {
            return;
        }
        if (player.getFoodStats().getSatiation() + player.getFoodStats().getNutrition() <= 0) {
            return;
        }

        String name = player.getCommandSenderName();
        long now = worldObj.getTotalWorldTime();
        float[] window = healWindows.get(name);
        if (window == null || now < (long) window[0] || now - (long) window[0] >= HEAL_WINDOW_TICKS) {
            window = new float[] { now, 0.0F };
            healWindows.put(name, window);
        }
        if (window[1] + HEAL_PER_PULSE > MAX_HEAL_PER_WINDOW + 0.001F) {
            return;
        }

        window[1] += HEAL_PER_PULSE;
        player.heal(HEAL_PER_PULSE);
        player.addHungerServerSide(HUNGER_PER_PULSE);
    }

    /**
     * Make a trail of particles from one point to another
     */
    protected void makeRedMagicTrail(World worldObj, double srcX, double srcY, double srcZ, double destX, double destY, double destZ) {
        // make particle trail
        int particles = 32;
        for (int i = 0; i < particles; i++) {
            double trailFactor = i / (particles - 1.0D);
            float f = 1.0F;
            float f1 = 0.5F;
            float f2 = 0.5F;
            double tx = srcX + (destX - srcX) * trailFactor + worldObj.rand.nextGaussian() * 0.005;
            double ty = srcY + (destY - srcY) * trailFactor + worldObj.rand.nextGaussian() * 0.005;
            double tz = srcZ + (destZ - srcZ) * trailFactor + worldObj.rand.nextGaussian() * 0.005;
            TFParticles.spawn(worldObj, "mobSpell", tx, ty, tz, f, f1, f2);
        }
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
        super.addInformation(stack, player, info, extended_info, slot);
        info.add(net.minecraft.StatCollector.translateToLocalFormatted("tf.tooltip.charges", stack.getMaxDamage() - stack.getItemDamage()));
    }
}
