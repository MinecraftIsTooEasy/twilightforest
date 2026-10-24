package twilightforest;

import com.google.common.eventbus.Subscribe;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockDispenser;
import net.minecraft.BlockButton;
import net.minecraft.BlockChest;
import net.minecraft.BlockMobSpawner;
import net.minecraft.BlockOre;
import net.minecraft.BlockRedstoneOre;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EntityXPOrb;
import net.minecraft.FurnaceRecipes;
import net.minecraft.GameRules;
import net.minecraft.ICommandSender;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ChatMessageComponent;
import net.minecraft.IInventory;
import net.minecraft.IMob;
import net.minecraft.InventoryPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.Minecraft;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.ServerPlayer;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import net.minecraft.server.MinecraftServer;
import net.xiaoyu233.fml.reload.event.CommandRegisterEvent;
import net.xiaoyu233.fml.reload.event.PlayerLoggedInEvent;
import twilightforest.block.BlockTFGiantBlock;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFChat;
import twilightforest.dimension.TFDimension;
import twilightforest.enchantment.TFEnchantment;
import twilightforest.entity.EntityTFCharmEffect;
import twilightforest.entity.EntityTFPinchBeetle;
import twilightforest.entity.EntityTFYeti;
import twilightforest.item.BehaviorTFMobEggDispense;
import twilightforest.item.TFItems;
import twilightforest.network.TFNetwork;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.TFWorldChunkManager;
import twilightforest.world.WorldProviderTwilightForest;

/**
 * Static event handlers ported from TFEventListener. The Forge event bus does not exist in MITE, so the mixins in
 * twilightforest.mixin.event call these methods directly; only the FML login event goes through the event bus
 * (register an instance of this class on MITEEvents.MITE_EVENT_BUS).
 */
public class TFEvents {
    // Tunables kept in one place so the balance pass can adjust them.
    public static int giantLogPlanksCrafted = 64;
    public static int giantLogBonusStacks = 0;
    public static float charmOfLife1Health = 8.0F;
    public static int charmOfLife1RegenTicks = 100;
    public static int charmOfLife2RegenTicks = 600;
    public static int charmOfLife2RegenAmplifier = 3;
    public static int iceBowChillTicks = 20 * 10;
    public static int iceBowChillAmplifier = 2;
    /** Furnace heat level the fiery pickaxe smelts at; 2 covers iron/gold/copper/silver but not mithril (3) or adamantium (4). */
    public static int fieryPickHeatLevel = 2;
    /** Players that died inside the forest, told where their gear is after they respawn. */
    private static final Map<String, String> forestDeaths = new HashMap<>();

    private static final Map<String, Long> lastProtectHint = new HashMap<>();

    private static final Map<String, InventoryPlayer> playerKeepsMap = new HashMap<>();
    private static boolean isBreakingWithGiantPick = false;

    // ---------------------------------------------------------------- FML events

    /** FML PlayerLoggedInEvent: portal item / difficulty hint, and tell the client whether progression is enforced. */
    @Subscribe
    public void onLogin(PlayerLoggedInEvent event) {
        ServerPlayer player = event.getPlayer();
        // fixed, user specified welcome text: sent literally, never as a lang key
        player.sendChatToPlayer(ChatMessageComponent.createFromText(TwilightForestMod.LOAD_MESSAGE).setColor(EnumChatFormatting.GREEN));
        TFTestMode.onLogin(player);
        TFTickHandler.sendPortalHint(player);
        sendEnforcedProgressionStatus(player, TwilightForestMod.isProgressionEnforced(player.worldObj));
    }

    /** Called from the CommandRegisterEvent handler (TFRegistryHandler). */
    public static void registerCommands(CommandRegisterEvent event) {
        event.register(new CommandTFProgress());
        event.register(new CommandTFFeature());
        event.register(new CommandTFRule());
        event.register(new twilightforest.command.CommandTwilightForest());
    }

    // ---------------------------------------------------------------- E01 pickup

    /**
     * Picking items up grants no progression (H4): progress comes from kills, see {@link TFProgress}. Only the
     * possession achievements without gameplay effect remain.
     */
    public static void onItemPickedUp(EntityPlayer player, Item item, int subtype) {
        if (item == null) {
            return;
        }
        if (item == TFItems.scepterTwilight || item == TFItems.scepterLifeDrain || item == TFItems.scepterZombie) {
            checkPlayerForScepterMastery(player);
        }
        if (item == TFItems.mazebreakerPick) {
            player.triggerAchievement(TFAchievementPage.twilightMazebreaker);
        }
    }

    private static void checkPlayerForScepterMastery(EntityPlayer player) {
        boolean twilight = false;
        boolean lifeDrain = false;
        boolean zombie = false;

        IInventory inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            Item item = stack.getItem();
            twilight |= item == TFItems.scepterTwilight;
            lifeDrain |= item == TFItems.scepterLifeDrain;
            zombie |= item == TFItems.scepterZombie;
        }

        if (twilight && lifeDrain && zombie) {
            player.triggerAchievement(TFAchievementPage.twilightLichScepters);
        }
    }

    // ---------------------------------------------------------------- E02 crafting

    public static void onItemCrafted(EntityPlayer player, ItemStack crafted, IInventory craftMatrix) {
        if (crafted == null || player.worldObj.isRemote) {
            return;
        }
        Item item = crafted.getItem();

        if (item == TFItems.plateNaga || item == TFItems.legsNaga) {
            checkPlayerForNagaArmorer(player);
        }
        if (item == TFItems.magicMapFocus) {
            player.triggerAchievement(TFAchievementPage.twilightMagicMapFocus);
        }
        if (item == TFItems.emptyMagicMap) {
            player.triggerAchievement(TFAchievementPage.twilightMagicMap);
        }
        if (item == TFItems.emptyMazeMap) {
            player.triggerAchievement(TFAchievementPage.twilightMazeMap);
        }
        if (item == TFItems.emptyOreMap) {
            player.triggerAchievement(TFAchievementPage.twilightOreMap);
        }

        // a giant log yields a full stack of planks; sneak the remaining stacks into the player's inventory
        if (crafted.itemID == Block.planks.blockID && crafted.stackSize == giantLogPlanksCrafted && doesCraftMatrixHaveGiantLog(craftMatrix)) {
            for (int i = 0; i < giantLogBonusStacks; i++) {
                player.inventory.addItemStackToInventoryOrDropIt(new ItemStack(Block.planks.blockID, giantLogPlanksCrafted, crafted.getItemSubtype()));
            }
        }
    }

    private static boolean doesCraftMatrixHaveGiantLog(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && stack.itemID == TFBlocks.giantLog.blockID) {
                return true;
            }
        }
        return false;
    }

    private static void checkPlayerForNagaArmorer(EntityPlayer player) {
        boolean nagaScale = false;
        boolean legsNaga = false;

        IInventory inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            nagaScale |= stack.getItem() == TFItems.nagaScale;
            legsNaga |= stack.getItem() == TFItems.legsNaga;
        }

        if (nagaScale && legsNaga) {
            player.triggerAchievement(TFAchievementPage.twilightNagaArmors);
        }
    }

    // ---------------------------------------------------------------- E03 block drops

    /**
     * Fiery pickaxe smelts what it harvests. Every drop reaching here is a single item (Block.dropBlockAsEntityItem
     * loops per unit), so the replacement stack is also a single item. Returns the stack to actually drop.
     */
    public static ItemStack onBlockDrop(BlockBreakInfo info, ItemStack stack) {
        if (stack == null || info == null || !info.wasHarvestedByPlayer()) {
            return stack;
        }
        EntityPlayer player = info.getResponsiblePlayer();
        ItemStack tool = info.responsible_item_stack;
        if (player == null || tool == null || tool.getItem() != TFItems.fieryPick || info.block == null) {
            return stack;
        }
        // economy: smelting while mining is gated by furnace heat (fieryPickSmelt: off, heatN or vanilla)
        int heat = fieryPickHeat(TFConfig.fieryPickSmelt);
        if (heat == -2) {
            return stack;
        }
        if (!tool.getItem().isEffectiveAgainstBlock(info.block, info.getMetadata())) {
            return stack;
        }

        ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack, heat);
        if (result == null) {
            return stack;
        }

        ItemStack smelted = result.copy();
        smelted.stackSize = 1;
        // the furnace would pay XP for this; mining with the pick pays it here
        if (info.world != null && !info.world.isRemote) {
            spawnSmeltXP(smelted, info.world, info.x, info.y, info.z);
        }
        return smelted;
    }

    /** Heat level for a fieryPickSmelt value: -2 = off, -1 = no heat gate, otherwise the furnace heat level. */
    private static int fieryPickHeat(String mode) {
        if (mode == null || "off".equalsIgnoreCase(mode)) {
            return -2;
        }
        if ("vanilla".equalsIgnoreCase(mode)) {
            return -1;
        }
        if (mode.toLowerCase().startsWith("heat")) {
            try {
                return Math.max(1, Integer.parseInt(mode.substring(4).trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return fieryPickHeatLevel;
    }

    private static void spawnSmeltXP(ItemStack smelted, World world, int x, int y, int z) {
        int xp = smelted.getItem().getExperienceReward(smelted.getItemSubtype());
        while (xp > 0) {
            int split = EntityXPOrb.getXPSplit(xp);
            xp -= split;
            world.spawnEntityInWorld(new EntityXPOrb(world, x + 0.5D, y + 0.5D, z + 0.5D, split));
        }
    }

    // ---------------------------------------------------------------- E04 hurt

    /**
     * Called with the damage that is about to be applied (after armor / defense modifiers). Returns the damage to
     * apply; returning 0 cancels it.
     */
    public static float onLivingHurt(EntityLivingBase target, Damage damage, float amount) {
        if (target.worldObj.isRemote) {
            return amount;
        }
        DamageSource source = damage.getSource();
        String type = source.damageType;
        Entity attacker = source.getResponsibleEntity();

        // mob.hpMult / mob.damageMult / boss.hpMult / boss.singleHitCapPct / mob.dayScaling.max
        amount = TFBalance.tuneDamage(target, attacker, amount);

        if (target instanceof EntityPlayer player && "mob".equals(type) && attacker != null) {
            // fire aura
            int fireLevel = TFEnchantment.getFieryAuraLevel(player.inventory, source);
            if (fireLevel > 0 && player.getRNG().nextInt(25) < fireLevel) {
                attacker.setFire(fireLevel / 2);
            }

            // chill aura
            if (attacker instanceof EntityLivingBase livingAttacker) {
                int chillLevel = TFEnchantment.getChillAuraLevel(player.inventory, source);
                if (chillLevel > 0) {
                    livingAttacker.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, chillLevel * 5 + 5, chillLevel));
                }
            }
        }

        if ("arrow".equals(type) && attacker instanceof EntityPlayer shooter) {
            ItemStack held = shooter.getHeldItemStack();
            Item heldItem = held == null ? null : held.getItem();

            if (heldItem == TFItems.iceBow) {
                target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, iceBowChillTicks, iceBowChillAmplifier, true));
            }

            if (heldItem == TFItems.enderBow) {
                double sourceX = shooter.posX;
                double sourceY = shooter.posY;
                double sourceZ = shooter.posZ;
                float sourceYaw = shooter.rotationYaw;
                float sourcePitch = shooter.rotationPitch;

                // setPositionAndUpdate is the only call that moves a player properly
                shooter.rotationYaw = target.rotationYaw;
                shooter.rotationPitch = target.rotationPitch;
                shooter.setPositionAndUpdate(target.posX, target.posY, target.posZ);
                shooter.playSound("mob.endermen.portal", 1.0F, 1.0F);

                target.setPositionAndRotation(sourceX, sourceY, sourceZ, sourceYaw, sourcePitch);
                target.playSound("mob.endermen.portal", 1.0F, 1.0F);
            }
        }

        if (target instanceof EntityPlayer player && willEntityDie(player, amount) && tryCharmOfLife(player)) {
            return 0.0F;
        }
        return amount;
    }

    /** The amount passed in is already reduced by armor, so only absorption (applied right after) is left to consider. */
    private static boolean willEntityDie(EntityLivingBase living, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        float dealt = Math.max(amount - living.getAbsorptionAmount(), 0.0F);
        if (dealt > 0.0F && dealt < 1.0F) {
            dealt = 1.0F;
        }
        return dealt > 0.0F && dealt >= living.getHealth();
    }

    private static boolean tryCharmOfLife(EntityPlayer player) {
        boolean charm1 = false;
        boolean charm2 = player.inventory.consumeInventoryItem(TFItems.charmOfLife2.itemID);
        if (!charm2) {
            charm1 = player.inventory.consumeInventoryItem(TFItems.charmOfLife1.itemID);
        }
        if (!charm1 && !charm2) {
            return false;
        }

        if (charm1) {
            player.setHealth(charmOfLife1Health);
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, charmOfLife1RegenTicks, 0));
        }
        if (charm2) {
            player.setHealth(player.getMaxHealth());
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, charmOfLife2RegenTicks, charmOfLife2RegenAmplifier));
            player.addPotionEffect(new PotionEffect(Potion.resistance.id, charmOfLife2RegenTicks, 0));
            player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, charmOfLife2RegenTicks, 0));
        }

        spawnCharmEffects(player, charm1 ? TFItems.charmOfLife1 : TFItems.charmOfLife2);
        player.worldObj.playSoundEffect(player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, "mob.zombie.unfect", 1.5F, 1.0F);
        return true;
    }

    private static void spawnCharmEffects(EntityPlayer player, Item charm) {
        EntityTFCharmEffect effect = new EntityTFCharmEffect(player.worldObj, player, charm);
        player.worldObj.spawnEntityInWorld(effect);

        EntityTFCharmEffect effect2 = new EntityTFCharmEffect(player.worldObj, player, charm);
        effect2.offset = (float) Math.PI;
        player.worldObj.spawnEntityInWorld(effect2);
    }

    // ---------------------------------------------------------------- E05 bonemeal

    /** Returns true when the bonemeal was used up on a Twilight Forest sapling. */
    public static boolean onBonemeal(World world, int x, int y, int z, Block block) {
        if (block == null || block != TFBlocks.sapling) {
            return false;
        }
        if (!world.isRemote) {
            ((twilightforest.block.BlockTFSapling) TFBlocks.sapling).func_149878_d(world, x, y, z, world.rand);
        }
        return true;
    }

    // ---------------------------------------------------------------- E06-E08 death / respawn / logout

    /** Called at the head of ServerPlayer.onDeath, before the inventory is dropped. */
    public static void onPlayerDeath(ServerPlayer player, DamageSource source) {
        noteForestDeathSite(player);
        if (!player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
            InventoryPlayer inv = player.inventory;

            if (inv.consumeInventoryItem(TFItems.charmOfKeeping3.itemID)) {
                TwilightForestMod.LOGGER.info("Player died with charm of keeping III, keeping everything");
                InventoryPlayer keep = new InventoryPlayer(null);
                keepAllArmor(player, keep);
                for (int i = 0; i < inv.mainInventory.length; i++) {
                    keep.mainInventory[i] = ItemStack.copyItemStack(inv.mainInventory[i]);
                    inv.mainInventory[i] = null;
                }
                keep.setItemStack(new ItemStack(TFItems.charmOfKeeping3));
                playerKeepsMap.put(player.getCommandSenderName(), keep);
            } else if (inv.consumeInventoryItem(TFItems.charmOfKeeping2.itemID)) {
                TwilightForestMod.LOGGER.info("Player died with charm of keeping II, keeping armor and hotbar");
                InventoryPlayer keep = new InventoryPlayer(null);
                keepAllArmor(player, keep);
                for (int i = 0; i < 9; i++) {
                    keep.mainInventory[i] = ItemStack.copyItemStack(inv.mainInventory[i]);
                    inv.mainInventory[i] = null;
                }
                keep.setItemStack(new ItemStack(TFItems.charmOfKeeping2));
                playerKeepsMap.put(player.getCommandSenderName(), keep);
            } else if (inv.consumeInventoryItem(TFItems.charmOfKeeping1.itemID)) {
                TwilightForestMod.LOGGER.info("Player died with charm of keeping I, keeping armor and current item");
                InventoryPlayer keep = new InventoryPlayer(null);
                keepAllArmor(player, keep);
                if (player.getHeldItemStack() != null) {
                    keep.mainInventory[inv.currentItem] = ItemStack.copyItemStack(inv.mainInventory[inv.currentItem]);
                    inv.mainInventory[inv.currentItem] = null;
                }
                keep.setItemStack(new ItemStack(TFItems.charmOfKeeping1));
                playerKeepsMap.put(player.getCommandSenderName(), keep);
            }

            // tower keys are always kept
            if (inv.hasItem(TFItems.towerKey.itemID)) {
                InventoryPlayer keep = retrieveOrMakeKeepInventory(player);
                for (int i = 0; i < inv.mainInventory.length; i++) {
                    if (inv.mainInventory[i] != null && inv.mainInventory[i].getItem() == TFItems.towerKey) {
                        keep.mainInventory[i] = ItemStack.copyItemStack(inv.mainInventory[i]);
                        inv.mainInventory[i] = null;
                    }
                }
                playerKeepsMap.put(player.getCommandSenderName(), keep);
            }
        }

        if (playerKeepsMap.size() > 1) {
            TwilightForestMod.LOGGER.warn("Twilight Forest is keeping track of {} dead player inventories", playerKeepsMap.size());
        }
    }

    private static InventoryPlayer retrieveOrMakeKeepInventory(EntityPlayer player) {
        InventoryPlayer existing = playerKeepsMap.get(player.getCommandSenderName());
        return existing != null ? existing : new InventoryPlayer(null);
    }

    private static void keepAllArmor(EntityPlayer player, InventoryPlayer keep) {
        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            keep.armorInventory[i] = ItemStack.copyItemStack(player.inventory.armorInventory[i]);
            player.inventory.armorInventory[i] = null;
        }
    }

    /** Remembers where a player left the forest without their gear (death or /tf stuck). */
    public static void noteForestDeathSite(EntityPlayer player) {
        if (TFDimension.is(player.worldObj)) {
            forestDeaths.put(player.getCommandSenderName(), MathHelper.floor_double(player.posX) + ", "
                    + MathHelper.floor_double(player.posY) + ", " + MathHelper.floor_double(player.posZ));
        }
    }

    /** Called with the new player right after ServerConfigurationManager.respawnPlayer built it. */
    public static void onPlayerRespawn(ServerPlayer player) {
        TFTestMode.syncTo(player);
        String site = forestDeaths.remove(player.getCommandSenderName());
        if (site != null) {
            player.sendChatToPlayer(TFChat.translation("tf.death.location", site).setColor(EnumChatFormatting.YELLOW));
        }
        InventoryPlayer keep = playerKeepsMap.remove(player.getCommandSenderName());
        if (keep == null) {
            return;
        }
        TwilightForestMod.LOGGER.info("Player {} respawned and received items held in storage", player.getCommandSenderName());

        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            if (keep.armorInventory[i] != null) {
                player.inventory.armorInventory[i] = keep.armorInventory[i];
            }
        }
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            if (keep.mainInventory[i] != null) {
                player.inventory.mainInventory[i] = keep.mainInventory[i];
            }
        }
        player.inventory.onInventoryChanged();

        if (keep.getItemStack() != null) {
            spawnCharmEffects(player, keep.getItemStack().getItem());
            player.worldObj.playSoundEffect(player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, "mob.zombie.unfect", 1.5F, 1.0F);
        }
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        InventoryPlayer keep = playerKeepsMap.remove(player.getCommandSenderName());
        if (keep == null) {
            return;
        }
        TwilightForestMod.LOGGER.warn("Keeping inventory items in reserve for player {} but they logged out, dropping them", player.getCommandSenderName());
        keep.player = player;
        keep.dropAllItems();
    }

    // ---------------------------------------------------------------- E09/E10 unfriendly mounts

    public static boolean shouldHideMountHealth(Entity riding) {
        return riding instanceof EntityTFPinchBeetle || riding instanceof EntityTFYeti;
    }

    public static boolean isRidingUnfriendly(EntityLivingBase entity) {
        return entity.isRiding() && shouldHideMountHealth(entity.ridingEntity);
    }

    // ---------------------------------------------------------------- E11 block breaking

    /** True when the break must be refused because the block is in a structure the player has not unlocked. */
    public static boolean shouldCancelBreak(ServerPlayer player, World world, int x, int y, int z) {
        return !player.capabilities.isCreativeMode && isAreaProtected(world, player, x, y, z);
    }

    /**
     * Giant pickaxe: also harvest the matching blocks of the aligned n*n*n cell (n = giant.chain, default 2). Called before the clicked block is
     * broken. Returns true when the break was handled completely (n=4 only: 64 cobblestone merged into one giant cobblestone),
     * in which case tryHarvestBlock must report success without running its own logic.
     */
    public static boolean onGiantPickBreak(ServerPlayer player, World world, int x, int y, int z) {
        ItemStack held = player.getHeldItemStack();
        if (isBreakingWithGiantPick || held == null || held.getItem() != TFItems.giantPick) {
            return false;
        }
        Block block = world.getBlock(x, y, z);
        // a giant block already breaks as a whole 4x4x4 cube, chaining into its other cells would harvest it out of order
        if (block == null || block instanceof BlockTFGiantBlock) {
            return false;
        }
        int meta = world.getBlockMetadata(x, y, z);
        if (!held.getItem().isEffectiveAgainstBlock(block, meta) || !isGiantChainable(world, block, x, y, z)) {
            return false;
        }

        int n = TFConfig.giantChain;
        int bx = Math.floorDiv(x, n) * n;
        int by = Math.floorDiv(y, n) * n;
        int bz = Math.floorDiv(z, n) * n;

        // only a full 4x4x4 cell may merge into a giant cobblestone (smaller cells would turn into a 16 cobblestone dupe)
        if (n == 4 && !player.capabilities.isCreativeMode && player.getCurrentPlayerStrVsBlock(x, y, z, true) > 0.0F && isAllCobble(world, bx, by, bz, n)) {
            BlockBreakInfo info = new BlockBreakInfo(world, x, y, z).setHarvestedBy(player);
            for (int dx = 0; dx < n; dx++) {
                for (int dy = 0; dy < n; dy++) {
                    for (int dz = 0; dz < n; dz++) {
                        world.setBlockToAir(bx + dx, by + dy, bz + dz);
                    }
                }
            }
            if (held.getItem().onBlockDestroyed(info)) {
                player.addStat(net.minecraft.StatList.objectUseStats[held.itemID], 1);
            }
            world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(TFBlocks.giantCobble)));
            return true;
        }

        isBreakingWithGiantPick = true;
        try {
            for (int dx = 0; dx < n; dx++) {
                for (int dy = 0; dy < n; dy++) {
                    for (int dz = 0; dz < n; dz++) {
                        int tx = bx + dx;
                        int ty = by + dy;
                        int tz = bz + dz;
                        if ((tx != x || ty != y || tz != z) && world.getBlock(tx, ty, tz) == block && world.getBlockMetadata(tx, ty, tz) == meta) {
                            player.theItemInWorldManager.tryHarvestBlock(tx, ty, tz);
                        }
                    }
                }
            }
        } finally {
            isBreakingWithGiantPick = false;
        }
        return false;
    }

    /**
     * Balance 7.1: the giant pickaxe only chains through bulk terrain (stone/dirt/sand/gravel/clay material, hardness at most 3.0),
     * never ores or spawners. Protected structures need no check here: every chained cell goes through tryHarvestBlock and
     * so through shouldCancelBreak.
     */
    private static boolean isGiantChainable(World world, Block block, int x, int y, int z) {
        Material material = block.blockMaterial;
        if (material != Material.stone && material != Material.dirt && material != Material.sand && material != Material.clay) {
            return false;
        }
        if (block instanceof BlockOre || block instanceof BlockRedstoneOre || block instanceof BlockMobSpawner) {
            return false;
        }
        return world.getBlockHardness(x, y, z) <= 3.0F;
    }

    /** Stone drops cobblestone in MITE too, so both count; any other block in the cell disqualifies it. */
    private static boolean isAllCobble(World world, int bx, int by, int bz, int n) {
        for (int dx = 0; dx < n; dx++) {
            for (int dy = 0; dy < n; dy++) {
                for (int dz = 0; dz < n; dz++) {
                    int id = world.getBlockId(bx + dx, by + dy, bz + dz);
                    if (id != Block.stone.blockID && id != Block.cobblestone.blockID) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- E12 interaction

    public static boolean shouldDenyBlockUse(EntityPlayer player, World world, int x, int y, int z) {
        if (world.isRemote || !(world.provider instanceof WorldProviderTwilightForest) || player.capabilities.isCreativeMode) {
            return false;
        }
        return isBlockProtectedFromInteraction(world, x, y, z) && isAreaProtected(world, player, x, y, z);
    }

    /** Fiery set achievement: fiery sword or pickaxe in hand and any piece of fiery armor worn. */
    public static void onFierySetCheck(EntityPlayer player) {
        ItemStack held = player.getHeldItemStack();
        if (held != null && (held.getItem() == TFItems.fierySword || held.getItem() == TFItems.fieryPick) && checkPlayerForFieryArmor(player)) {
            player.triggerAchievement(TFAchievementPage.twilightFierySet);
        }
    }

    private static boolean isBlockProtectedFromInteraction(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block != null && (block == TFBlocks.towerDevice || block instanceof BlockChest || block instanceof BlockButton || block == Block.lever);
    }

    private static boolean checkPlayerForFieryArmor(EntityPlayer player) {
        ItemStack[] armor = player.inventory.armorInventory;
        return armor[0] != null && armor[0].getItem() == TFItems.fieryBoots
                || armor[1] != null && armor[1].getItem() == TFItems.fieryLegs
                || armor[2] != null && armor[2].getItem() == TFItems.fieryPlate
                || armor[3] != null && armor[3].getItem() == TFItems.fieryHelm;
    }

    /**
     * Quiet protection query for WorldServer.canMineBlock (X13): no packet, no hint monster, so it may be asked often.
     * Covers placing and breaking blocks only.
     */
    public static boolean isBlockLocked(World world, EntityPlayer player, int x, int y, int z) {
        if (world.isRemote || player.capabilities.isCreativeMode || !TwilightForestMod.isProgressionEnforced(world)
                || !(world.provider instanceof WorldProviderTwilightForest provider) || TFTestMode.isOn(world)) {
            return false;
        }
        ChunkProviderTwilightForest chunkProvider = provider.getChunkProvider();
        if (chunkProvider != null && chunkProvider.isBlockInStructureBB(x, y, z)) {
            TFFeature feature = ((TFWorldChunkManager) world.provider.worldChunkMgr).getFeatureAt(x, z, world);
            return !feature.doesPlayerHaveRequiredAchievement(player) && chunkProvider.isBlockProtected(x, y, z);
        }
        return false;
    }

    /** Returns true if the position is protected for that player; also sends the protection packet and may spawn a hint monster. */
    public static boolean isAreaProtected(World world, EntityPlayer player, int x, int y, int z) {
        if (world.isRemote || !TwilightForestMod.isProgressionEnforced(world) || !(world.provider instanceof WorldProviderTwilightForest provider)
                || TFTestMode.isOn(world)) {
            return false;
        }
        ChunkProviderTwilightForest chunkProvider = provider.getChunkProvider();
        if (chunkProvider != null && chunkProvider.isBlockInStructureBB(x, y, z)) {
            TFFeature nearbyFeature = ((TFWorldChunkManager) world.provider.worldChunkMgr).getFeatureAt(x, z, world);

            if (!nearbyFeature.doesPlayerHaveRequiredAchievement(player) && chunkProvider.isBlockProtected(x, y, z)) {
                StructureBoundingBox sbb = chunkProvider.getSBBAt(x, y, z);
                TFNetwork.sendAreaProtection(world, sbb, x, y, z);
                sendProtectionDenied(world, player, nearbyFeature);

                nearbyFeature.trySpawnHintMonster(world, player, x, y, z);
                return true;
            }
        }
        return false;
    }

    /** Chat tf.protect.denied.[structure], at most once per 5 seconds per player (MITE_BALANCE 5.5). */
    private static void sendProtectionDenied(World world, EntityPlayer player, TFFeature feature) {
        long now = world.getTotalWorldTime();
        String who = player.getCommandSenderName().toLowerCase();
        Long last = lastProtectHint.get(who);
        if (last != null && now >= last && now - last < 100L) {
            return;
        }
        lastProtectHint.put(who, now);
        String key = feature.name.toLowerCase().replace(' ', '_');
        player.sendChatToPlayer(TFChat.translation("tf.protect.denied." + key).setColor(EnumChatFormatting.YELLOW));
    }

    // ---------------------------------------------------------------- E13 attack

    /**
     * Structure protection never blocks a player's attacks (MITE_BALANCE 5.1): standing in a locked structure and not
     * being able to hit back at the monsters would be a death trap. Kept for the event mixin.
     */
    public static boolean shouldCancelAttack(EntityLivingBase target, Damage damage) {
        return false;
    }

    // ---------------------------------------------------------------- E15 dimension change

    public static void onPlayerChangedDimension(ServerPlayer player, int toDim) {
        TFTestMode.syncTo(player);
        if (toDim == TwilightForestMod.dimensionID) {
            sendEnforcedProgressionStatus(player, TwilightForestMod.isProgressionEnforced(player.worldObj));
        }
    }

    // ---------------------------------------------------------------- E16 world load

    /** Called before the start region is generated: define the progression rule and the dispenser behaviors. */
    public static void onServerWorldsLoaded(MinecraftServer server) {
        // the game rules object is shared by every world, so one pass is enough
        // clients learn the server's progress.mode through this rule (see TwilightForestMod.isProgressionEnforced)
        GameRules rules = server.worldServers[0].getGameRules();
        rules.setOrCreateGameRule(TwilightForestMod.ENFORCED_PROGRESSION_RULE, String.valueOf(TFConfig.progressEnforced()));
        twilightforest.dimension.TFPortalSanctuary.reset();
        TFSelfCheck.run(server);
        TwilightForestMod.LOGGER.info("Twilight Forest portal item(s): {} (portal.items = {})", TFTickHandler.getPortalItems(), TFConfig.portalItems);

        if (TFItems.spawnEgg != null) {
            BlockDispenser.dispenseBehaviorRegistry.putObject(TFItems.spawnEgg, new BehaviorTFMobEggDispense());
        }
    }

    // ---------------------------------------------------------------- E18 player tick

    public static void onPlayerTick(EntityPlayer player) {
        // Forge Item.onUsingTick replacement (MITE has none): the item in use gets one call per tick on both sides
        ItemStack inUse = player.getItemInUse();
        if (inUse != null && inUse.getItem() instanceof twilightforest.compat.ITFUsingTickItem usingTick) {
            usingTick.onUsingTick(inUse, player, player.getItemInUseCount());
        }
        if (player.worldObj.isRemote) {
            return;
        }
        TFTickHandler.playerTick(player);
        if (player.worldObj.getTotalWorldTime() % 20 == 0) {
            onFierySetCheck(player);
        }
    }

    // ---------------------------------------------------------------- helpers

    public static void sendEnforcedProgressionStatus(EntityPlayer player, boolean enforced) {
        TFNetwork.sendEnforcedProgressionStatus(player, enforced);
    }

    /**
     * MITE has no working op list (isPlayerOpped is hardcoded false). Admins are the console, the owner of a
     * single-player / LAN world, anyone in MITE dev mode, and ops should the list ever be restored.
     */
    public static boolean isAdmin(ICommandSender sender) {
        if (!(sender instanceof EntityPlayer player)) {
            return true;
        }
        if (Minecraft.inDevMode() || player.worldObj != null && TFTestMode.isOn(player.worldObj)) {
            return true;
        }
        MinecraftServer server = MinecraftServer.getServer();
        String name = player.getCommandSenderName();
        return server != null && (server.isSinglePlayer() && name.equalsIgnoreCase(server.getServerOwner())
                || server.getConfigurationManager().isPlayerOpped(name));
    }

}
