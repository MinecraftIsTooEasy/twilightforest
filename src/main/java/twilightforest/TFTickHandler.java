package twilightforest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.BiomeGenBase;
import net.minecraft.ChatMessageComponent;
import net.minecraft.EntityItem;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumChatFormatting;
import net.minecraft.EnumParticle;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.Packet71Weather;
import net.minecraft.ServerPlayer;
import net.minecraft.SpatialScaler;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import net.minecraft.server.MinecraftServer;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.block.BlockTFPortal;
import twilightforest.block.TFBlocks;
import twilightforest.compat.TFChat;
import twilightforest.compat.TFModCompat;
import twilightforest.compat.TFRegistry;
import twilightforest.dimension.TFDimension;
import twilightforest.dimension.TFPortalSanctuary;
import twilightforest.network.TFNetwork;
import twilightforest.world.ChunkProviderTwilightForest;
import twilightforest.world.WorldProviderTwilightForest;

/**
 * Per-player tick logic: portal creation by throwing the portal item into a flower-ringed pool (MITE_BALANCE 3.1), and
 * progression enforcement. Called from TFEvents.onPlayerTick (EntityPlayer.onUpdate RETURN, server side only).
 */
public class TFTickHandler {
    /** The vanilla lightning does not burn the item in MITE, so without this the portal would be free to reopen forever. */
    public static boolean consumePortalItem = true;

    private static String portalItemsKey;
    private static List<Item> portalItems = new ArrayList<>();
    /** Overworld time of the last item based portal per creator, and of the last refusal message per creator. */
    private static final Map<String, Long> lastCreation = new HashMap<>();
    private static final Map<String, Long> lastFailure = new HashMap<>();
    private static final Map<String, Long> lastPenaltyHint = new HashMap<>();

    private TFTickHandler() {
    }

    /**
     * The items that open the portal, resolved lazily from portal.items (items do not exist before registration). 'auto' is
     * the copper ingot, or the diamond when a difficulty mod is installed (TFModCompat).
     */
    public static List<Item> getPortalItems() {
        String cfg = TwilightForestMod.portalCreationItemString;
        if (portalItemsKey == null || !portalItemsKey.equals(cfg)) {
            List<Item> found = new ArrayList<>();
            String list = cfg == null || cfg.trim().isEmpty() || "auto".equalsIgnoreCase(cfg.trim()) ? autoPortalItemName() : cfg;
            for (String token : list.split(",")) {
                token = token.trim();
                if (token.isEmpty()) {
                    continue;
                }
                Item item = TFRegistry.findItem(token);
                if (item == null) {
                    TwilightForestMod.LOGGER.warn("portal.items lists '{}', which is not an item", token);
                } else {
                    found.add(item);
                }
            }
            if (found.isEmpty()) {
                found.add(TFModCompat.hardModPresent() ? Item.diamond : Item.ingotCopper);
            }
            portalItems = found;
            portalItemsKey = cfg;
        }
        return portalItems;
    }

    public static Item getPortalItem() {
        return getPortalItems().get(0);
    }

    /** MITE's zh_CN.lang has no copper ingot name, so the usual portal items carry their own TF lang key. */
    private static String portalItemNameKey(Item item) {
        if (item == Item.ingotCopper || item == Item.diamond || item == Item.emerald) {
            return "tf.portal.item." + item.getUnlocalizedName().substring("item.".length());
        }
        return item.getUnlocalizedName() + ".name";
    }

    /** portal.items = auto: diamond with a difficulty mod installed, copper ingot otherwise. */
    private static String autoPortalItemName() {
        return TFModCompat.hardModPresent() ? "diamond" : "ingotCopper";
    }

    /**
     * Login hint: which item opens the portal (names translated by the client) and how many, plus the difficulty profile
     * picked for the installed mods.
     */
    public static void sendPortalHint(EntityPlayer player) {
        if (TFConfig.portalDisabled) {
            player.sendChatToPlayer(TFChat.translation("tf.portal.hint.disabled").setColor(EnumChatFormatting.GOLD));
        } else {
            ChatMessageComponent names = new ChatMessageComponent();
            List<Item> items = getPortalItems();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    names.appendComponent(TFChat.translation("tf.portal.hint.or"));
                }
                names.appendComponent(TFChat.translation(portalItemNameKey(items.get(i))));
            }
            player.sendChatToPlayer(TFChat.translation("tf.portal.hint", names, Math.max(1, TFConfig.portalCost)).setColor(EnumChatFormatting.GOLD));
        }
        TFModCompat.sendProfileHint(player);
    }

    public static void playerTick(EntityPlayer player) {
        World world = player.worldObj;
        if (world.isRemote) {
            return;
        }
        long time = world.getTotalWorldTime();

        if (time % 20 == 0 && TFDimension.is(world)) {
            // blood moon nights (mirrored from the overworld) double the hostile spawn rate
            float rate = (float) TFConfig.spawnHostileFactor;
            if (world.isBloodMoon(true)) {
                rate = Math.min(1.0F, rate * 2.0F);
            }
            TFDimension.hostileSpawnRateModifier = rate;
        }

        if (time % 20 == 0) {
            TFPortalSanctuary.tick(player, time);
        }

        boolean testMode = time % 20 == 0 && TFTestMode.isOn(world);

        if ((testMode || !TFConfig.portalDisabled) && time % 20 == 0) {
            if (testMode) {
                checkForPortalCreation(player, world, TFConfig.portalScanRadius);
            } else if (TFConfig.portalAdminOnly) {
                // admins only, and only close by
                if (TFEvents.isAdmin(player)) {
                    checkForPortalCreation(player, world, 4.0F);
                }
            } else {
                checkForPortalCreation(player, world, TFConfig.portalScanRadius);
            }
        }

        if (time % 20 == 0 && !testMode && TwilightForestMod.isProgressionEnforced(world) && !TFConfig.progressLenient()) {
            if (world.provider instanceof WorldProviderTwilightForest && !player.capabilities.isCreativeMode) {
                checkBiomeForProgression(player, world);
            }
        }

        if (time % 100 == 0 && TwilightForestMod.isProgressionEnforced(world) && world.provider instanceof WorldProviderTwilightForest) {
            if (!player.capabilities.isCreativeMode && !TFTestMode.isOn(world)) {
                checkForLockedStructuresSendPacket(player, world);
            } else {
                sendAllClearPacket(player);
            }
        }
    }

    private static void sendStructureProtectionPacket(EntityPlayer player, StructureBoundingBox sbb) {
        if (player instanceof ServerPlayer) {
            TFNetwork.sendStructureProtection(player, sbb);
        }
    }

    private static void sendAllClearPacket(EntityPlayer player) {
        if (player instanceof ServerPlayer) {
            TFNetwork.sendStructureProtectionClear(player);
        }
    }

    private static boolean checkForLockedStructuresSendPacket(EntityPlayer player, World world) {
        ChunkProviderTwilightForest chunkProvider = ((WorldProviderTwilightForest) world.provider).getChunkProvider();

        int px = MathHelper.floor_double(player.posX);
        int pz = MathHelper.floor_double(player.posZ);

        if (chunkProvider != null && chunkProvider.isBlockNearFullStructure(px, pz, 64)) {
            StructureBoundingBox fullSBB = chunkProvider.getFullSBBNear(px, pz, 64);

            TFFeature nearFeature = TFFeature.getFeatureForRegion(fullSBB.getCenterX() >> 4, fullSBB.getCenterZ() >> 4, world);

            if (!nearFeature.hasProtectionAura || nearFeature.doesPlayerHaveRequiredAchievement(player)) {
                sendAllClearPacket(player);
                return false;
            }
            sendStructureProtectionPacket(player, fullSBB);
            return true;
        }
        return false;
    }

    private static void checkForPortalCreation(EntityPlayer player, World world, float rangeToCheck) {
        boolean testMode = TFTestMode.isOn(world);
        int dimension = world.getDimensionId();
        if (!testMode) {
            if (dimension == TwilightForestMod.dimensionID) {
                if (!TFConfig.portalAllowInTF) {
                    return;
                }
            } else if (dimension != 0 && !TFConfig.portalAllowOtherDimensions) {
                return;
            }
        }

        List<Item> items = getPortalItems();
        @SuppressWarnings("unchecked")
        List<EntityItem> itemList = world.getEntitiesWithinAABB(EntityItem.class, player.boundingBox.expand(rangeToCheck, rangeToCheck, rangeToCheck));
        BlockTFPortal portalBlock = (BlockTFPortal) TFBlocks.portal;

        for (EntityItem entityItem : itemList) {
            ItemStack stack = entityItem.getEntityItem();
            if (stack == null || !items.contains(stack.getItem()) || !world.isMaterialInBB(entityItem.boundingBox, Material.water)) {
                continue;
            }

            for (int k = 0; k < 2; k++) {
                double d = world.rand.nextGaussian() * 0.02D;
                double d1 = world.rand.nextGaussian() * 0.02D;
                double d2 = world.rand.nextGaussian() * 0.02D;
                world.spawnParticle(EnumParticle.spell, entityItem.posX, entityItem.posY + 0.2, entityItem.posZ, d, d1, d2);
            }

            int dx = MathHelper.floor_double(entityItem.posX);
            int dy = MathHelper.floor_double(entityItem.posY);
            int dz = MathHelper.floor_double(entityItem.posZ);

            if (!portalBlock.isGoodPortalPool(world, dx, dy, dz)) {
                continue;
            }

            EntityPlayer creator = checkPortalConditions(world, entityItem, dx, dy, dz, testMode);
            if (creator == null && !testMode) {
                continue;
            }

            // commit, in this order: lightning, water to portal, then pay exactly once. Nothing is paid on a refusal above.
            if (TFConfig.portalCosmeticLightning) {
                sendCosmeticLightning(world, dx, dy, dz);
            }
            portalBlock.transmuteWaterToPortal(world, dx, dy, dz);
            TFPortalSanctuary.register(world, dx, dy, dz);
            if (!testMode && consumePortalItem) {
                consumePortalPayment(entityItem);
            }

            if (creator != null) {
                lastCreation.put(creator.getCommandSenderName().toLowerCase(), TFBalance.overworldTime());
                creator.triggerAchievement(TFAchievementPage.twilightPortal);
                sendFirstPortalAdvice(creator);
            }
            break;
        }
    }

    /**
     * Takes portal.cost items out of the thrown stack. The stack is replaced through setEntityItemStack so the data watcher
     * marks it changed (a bare stackSize edit is never sent to the clients). Everybody pays, creative players included.
     */
    private static void consumePortalPayment(EntityItem entityItem) {
        ItemStack stack = entityItem.getEntityItem();
        int left = stack.stackSize - Math.max(1, TFConfig.portalCost);
        if (left <= 0) {
            entityItem.setDead();
            return;
        }
        ItemStack rest = stack.copy();
        rest.stackSize = left;
        entityItem.setEntityItemStack(rest);
    }

    /**
     * Every precondition of MITE_BALANCE 3.1 step 3. Returns the player who creates the portal, or null after telling the
     * players nearby why nothing happened (nothing is consumed, no lightning, no portal).
     */
    private static EntityPlayer checkPortalConditions(World world, EntityItem entityItem, int dx, int dy, int dz, boolean testMode) {
        double px = entityItem.posX;
        double py = entityItem.posY;
        double pz = entityItem.posZ;

        if (testMode) {
            // test mode: the pool and an accepted item are enough, whoever is near gets the credit
            return nearestPlayer(world, px, py, pz, 16.0D);
        }

        if (!TFSelfCheck.passed()) {
            warnNear(world, px, py, pz, "selfcheck");
            return null;
        }
        if (entityItem.getEntityItem().stackSize < TFConfig.portalCost) {
            warnNear(world, px, py, pz, "cost", TFConfig.portalCost);
            return null;
        }

        EntityPlayer creator = nearestSurvivalPlayer(world, px, py, pz, 16.0D);
        if (creator == null) {
            // creative players can also open it for the console-less single player case
            creator = nearestPlayer(world, px, py, pz, 16.0D);
            if (creator == null || !creator.capabilities.isCreativeMode) {
                warnNear(world, px, py, pz, "nobody");
                return null;
            }
        }
        boolean exempt = creator.capabilities.isCreativeMode;

        if (!exempt && TFConfig.portalRequireObsidianTool) {
            World overworld = TFBalance.overworld();
            World info = overworld != null ? overworld : world;
            if ((info.getWorldInfo().getVillageConditions() & 16) == 0) {
                warnNear(world, px, py, pz, "obsidian");
                return null;
            }
        }
        if (!exempt && TFConfig.portalMinLevel > 0 && creator.getExperienceLevel() < TFConfig.portalMinLevel) {
            warnNear(world, px, py, pz, "level", TFConfig.portalMinLevel);
            return null;
        }
        if (!exempt && TFConfig.portalMinDay > 0 && TFBalance.overworldDay() < TFConfig.portalMinDay) {
            warnNear(world, px, py, pz, "day", TFConfig.portalMinDay);
            return null;
        }
        if (!exempt && TFConfig.portalCooldownTicks > 0) {
            Long last = lastCreation.get(creator.getCommandSenderName().toLowerCase());
            long now = TFBalance.overworldTime();
            if (last != null && now >= last && now - last < TFConfig.portalCooldownTicks) {
                long seconds = (TFConfig.portalCooldownTicks - (now - last)) / 20L + 1L;
                warnNear(world, px, py, pz, "cooldown", seconds);
                return null;
            }
        }
        return creator;
    }

    private static EntityPlayer nearestSurvivalPlayer(World world, double x, double y, double z, double range) {
        EntityPlayer best = null;
        double bestDist = range * range;
        for (Object o : world.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p.capabilities.isCreativeMode || p.isDead) {
                continue;
            }
            double dist = p.getDistanceSq(x, y, z);
            if (dist <= bestDist) {
                bestDist = dist;
                best = p;
            }
        }
        return best;
    }

    private static EntityPlayer nearestPlayer(World world, double x, double y, double z, double range) {
        return world.getClosestPlayer(x, y, z, range, false);
    }

    /** Refusal message tf.portal.fail.[reason] to players within 16 blocks, at most once per 5 seconds per reason. */
    private static void warnNear(World world, double x, double y, double z, String reason, Object... args) {
        long now = world.getTotalWorldTime();
        String key = world.getDimensionId() + ":" + reason + ":" + (int) (x / 8) + ":" + (int) (z / 8);
        Long last = lastFailure.get(key);
        if (last != null && now >= last && now - last < 100L) {
            return;
        }
        lastFailure.put(key, now);
        if (lastFailure.size() > 256) {
            lastFailure.clear();
        }
        for (Object o : world.playerEntities) {
            EntityPlayer p = (EntityPlayer) o;
            if (p.getDistanceSq(x, y, z) <= 256.0D) {
                p.sendChatToPlayer(TFChat.translation("tf.portal.fail." + reason, args).setColor(EnumChatFormatting.YELLOW));
            }
        }
    }

    /** A lightning bolt that only the clients see: no server entity, so no fire and no 5 point shock (F16, F24). */
    private static void sendCosmeticLightning(World world, double x, double y, double z) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        Packet71Weather packet = new Packet71Weather();
        packet.entityID = Integer.MIN_VALUE + world.rand.nextInt(1 << 20);
        packet.isLightningBolt = 1;
        packet.scaled_pos_x = SpatialScaler.getScaledPosX(x);
        packet.scaled_pos_y = SpatialScaler.getScaledPosY(y);
        packet.scaled_pos_z = SpatialScaler.getScaledPosZ(z);
        server.getConfigurationManager().sendToAllNear(x, y, z, 64.0D, world.getDimensionId(), packet);
    }

    /** One time advice for the player who opens a portal for the first time. */
    private static void sendFirstPortalAdvice(EntityPlayer creator) {
        TFProgressData data = TFProgressData.get();
        if (data == null) {
            return;
        }
        String name = creator.getCommandSenderName();
        int bits = data.getBits(name);
        if ((bits & TFProgressData.BIT_PORTAL) != 0) {
            return;
        }
        data.setBits(name, bits | TFProgressData.BIT_PORTAL);
        if (TFConfig.hungerAdvisory) {
            creator.sendChatToPlayer(TFChat.translation("tf.portal.advice").setColor(EnumChatFormatting.AQUA));
        }
    }

    /** Check what biome the player is in and whether current progression allows it; if not, take action. */
    private static void checkBiomeForProgression(EntityPlayer player, World world) {
        if (TFTestMode.isOn(world)) {
            return;
        }
        BiomeGenBase currentBiome = world.getBiomeGenForCoords(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posZ));

        if (currentBiome instanceof TFBiomeBase tfBiome && !tfBiome.doesPlayerHaveRequiredAchievement(player)) {
            sendPenaltyHint(player, world, tfBiome);
            tfBiome.enforceProgession(player, world);
        }
    }

    /** Chat hint tf.penalty.[biome] naming the missing progress, at most once per 5 minutes per player and biome. */
    private static void sendPenaltyHint(EntityPlayer player, World world, TFBiomeBase biome) {
        String key = switch (biome.getClass().getSimpleName()) {
            case "TFBiomeSwamp" -> "swamp";
            case "TFBiomeFireSwamp" -> "fire_swamp";
            case "TFBiomeDarkForest" -> "dark_forest";
            case "TFBiomeSnow" -> "snow";
            case "TFBiomeGlacier" -> "glacier";
            case "TFBiomeHighlands" -> "highlands";
            default -> null;
        };
        if (key == null) {
            return;
        }
        String mapKey = player.getCommandSenderName().toLowerCase() + ":" + key;
        long now = world.getTotalWorldTime();
        Long last = lastPenaltyHint.get(mapKey);
        if (last != null && now >= last && now - last < 6000L) {
            return;
        }
        lastPenaltyHint.put(mapKey, now);
        player.sendChatToPlayer(TFChat.translation("tf.penalty." + key).setColor(EnumChatFormatting.YELLOW));
    }
}
