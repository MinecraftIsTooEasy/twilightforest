package twilightforest.testbot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.Block;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityList;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPig;
import net.minecraft.EnumGameType;
import net.minecraft.GuiInventory;
import net.minecraft.GuiMainMenu;
import net.minecraft.Icon;
import net.minecraft.Item;
import net.minecraft.ItemArmor;
import net.minecraft.ItemBlock;
import net.minecraft.ItemBow;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Minecraft;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.Render;
import net.minecraft.RenderManager;
import net.minecraft.ServerPlayer;
import net.minecraft.TextureAtlasSprite;
import net.minecraft.World;
import net.minecraft.WorldServer;
import net.minecraft.WorldSettings;
import net.minecraft.WorldType;
import net.minecraft.server.MinecraftServer;
import twilightforest.TFFeature;
import twilightforest.TFIds;
import twilightforest.TFTestMode;
import twilightforest.block.TFBlocks;
import twilightforest.command.TFStructureLocator;
import twilightforest.dimension.TFDimension;
import twilightforest.entity.TFCreatures;

/**
 * The linear test script. Every step is wrapped (exceptions are recorded, the next step still runs). Runs on the bot thread; see
 * {@link TestBot} for how work is handed to the client/server/render threads.
 */
final class TestBotScript {
    static final String WORLD = "TFTestBot";
    /** Ticks every item stays visible in the first person hand (20 ticks = 1 s; -Dtwilightforest.testbot.hold=N, -Dtwilightforest.testbot.nopace=true for none). */
    static final int HAND_HOLD_TICKS = Integer.getInteger("twilightforest.testbot.hold", 14);
    static final int GROUND_Y = 119;
    static final int FEET_Y = 120;

    final TestBot bot;
    final Minecraft mc;
    final Report rep;
    final Set<String> skip = new HashSet<>();
    /** -Dtwilightforest.testbot.only=04_weapons,struct_hedge_maze : run only steps whose id starts with one of these (00/01/99 always run); empty = all */
    final Set<String> only = new HashSet<>();
    /** -Dtwilightforest.testbot.weapons=ItemTFTwilightWand,ItemTFSpawnEgg : step 04 only handles items of these simple class names; empty = all */
    final Set<String> weaponFilter = new HashSet<>();
    Report.Section sec;
    private int cameraWarns;
    private int cameraWarnsSuppressed;

    private Set<String> langEn;
    private Set<String> langZh;

    TestBotScript(TestBot bot) {
        this.bot = bot;
        this.mc = bot.mc;
        this.rep = bot.report;
        for (String s : System.getProperty("twilightforest.testbot.skip", "").split(",")) {
            if (!s.isBlank()) {
                skip.add(s.trim().toLowerCase());
            }
        }
        for (String s : System.getProperty("twilightforest.testbot.only", "").split(",")) {
            if (!s.isBlank()) {
                only.add(s.trim().toLowerCase());
            }
        }
        for (String s : System.getProperty("twilightforest.testbot.weapons", "").split(",")) {
            if (!s.isBlank()) {
                weaponFilter.add(s.trim().toLowerCase());
            }
        }
    }

    private boolean selected(String id) {
        if (only.isEmpty() || id.startsWith("00_") || id.startsWith("01_") || id.startsWith("99_")) {
            return true;
        }
        String l = id.toLowerCase();
        for (String o : only) {
            if (l.startsWith(o)) {
                return true;
            }
        }
        return false;
    }

    private interface Body {
        void run(Report.Section s) throws Exception;
    }

    private boolean step(String id, String title, Body body) {
        if (bot.isAborted()) {
            rep.skipped(id, title + " (run aborted)");
            return false;
        }
        if (skip.contains(id.toLowerCase()) || !selected(id)) {
            rep.skipped(id, title);
            return true;
        }
        sec = rep.begin(id, title);
        cameraWarns = 0;
        cameraWarnsSuppressed = 0;
        TestBot.log("step " + id + ": " + title);
        bot.status(title);
        try {
            body.run(sec);
        } catch (Throwable t) {
            sec.exception(title, t);
            t.printStackTrace();
        } finally {
            try {
                bot.afterFrame(() -> null);
            } catch (Exception ignored) {
            }
            if (cameraWarnsSuppressed > 0) {
                sec.info("camera", cameraWarnsSuppressed + " more 'client player did not reach the camera position' warnings suppressed (first 3 shown)");
            }
            rep.end(sec);
        }
        return true;
    }

    void run() throws Exception {
        step("00_startup", "00 Startup log scan", s -> {
            for (String l : TestBot.startupLog()) {
                s.warn("startup log", l);
            }
            s.info("mod classes", "TFBlocks.portal=" + TFBlocks.portal + " TFBlocks.hedge=" + TFBlocks.hedge);
            s.check(TFBlocks.portal != null && TFBlocks.hedge != null, "TF blocks registered", "TFBlocks fields are null");
            nullWorldScan(s);
        });
        step("01_world", "01 Create / enter creative test world", this::createWorld);
        if (bot.isAborted() || mc.thePlayer == null) {
            return;
        }
        step("02_items", "02 Items and blocks: hand, inventory icon, dropped item", this::stepItems);
        step("03_armor", "03 Armor: third person + inventory", this::stepArmor);
        step("04_weapons", "04 Weapons, tools, bows, scepters, throwables", this::stepWeapons);
        step("05_entities", "05 Entities: every TF living entity, boss and projectile", this::stepEntities);
        step("06_blocks", "06 Placed block subtype grids (all render types)", this::stepBlockGrid);
        step("07_portal", "07 Portal block purple icon", this::stepPortal);
        // acquisition: every intended way to obtain every TF item/block subtype is exercised server side (they all run in the overworld arena)
        TestBotAcquisition acq = new TestBotAcquisition(this);
        step("acq_craft", "07a Acquisition: crafting (real workbench containers, required bench tier)", acq::stepCraft);
        step("acq_smelt", "07b Acquisition: smelting (real furnace tile entity)", acq::stepSmelt);
        step("acq_harvest", "07c Acquisition: block harvest in survival with MITE tools", acq::stepHarvest);
        step("acq_mobs", "07d Acquisition: mob and boss drops (first kill vs repeat kill)", acq::stepMobs);
        step("acq_loot", "07e Acquisition: structure chest loot (every TFTreasure table)", acq::stepLoot);
        step("acq_special", "07f Acquisition: special interactions (powder, trees, ram, beans, queen, uncrafting)", acq::stepSpecial);
        step("acq_matrix", "07g Acquisition matrix and red list", acq::stepMatrix);
        for (String name : STRUCTURES) {
            step("struct_" + name, "08 Structure " + name, s -> stepStructure(s, name));
        }
        step("09_maze_trap", "09 Labyrinth trapped dead end: tripwire -> hook -> TNT", this::stepMazeTrap);
        step("11_spawner_light", "11 Spawner mobs in bright light (MITE: spawners ignore light)", this::stepSpawnerLight);
        step("11_minoshroom", "11 Labyrinth boss room: Minoshroom spawner and restore", this::stepMinoshroom);
        step("99_final", "99 Final log scan", s -> s.info("done", "all steps finished"));
    }

    // ================================================================== helpers

    /**
     * MITE's ReferenceFileWriter.writeMobExperienceFile (runs at every client start) builds every registered EntityLiving with
     * {@code new X((World) null)} and calls getExperienceValue(); the first exception aborts all reference files ("Writing reference files... [failed]").
     * Same construction here for every TF creature, so a constructor that dereferences the world is reported with its stack trace.
     */
    void nullWorldScan(Report.Section s) {
        int ok = 0;
        for (Map.Entry<Class<?>, String> e : TFCreatures.registeredNames().entrySet()) {
            Class<?> c = e.getKey();
            if (!net.minecraft.EntityLiving.class.isAssignableFrom(c) || java.lang.reflect.Modifier.isAbstract(c.getModifiers())) {
                continue;
            }
            try {
                net.minecraft.EntityLiving el = (net.minecraft.EntityLiving) c.getConstructor(World.class).newInstance((Object) null);
                el.getExperienceValue();
                ok++;
            } catch (java.lang.reflect.InvocationTargetException ite) {
                s.exception("construct " + e.getValue() + " with a null world (MITE reference file writer does this)", ite.getCause() == null ? ite : ite.getCause());
            } catch (Throwable t) {
                s.exception("construct " + e.getValue() + " with a null world", t);
            }
        }
        s.info("null world construction", ok + " TF living entities constructed with (World) null without an exception");
    }

    static final String[] STRUCTURES = {
        "naga_courtyard", "lich_tower", "hedge_maze", "labyrinth", "hydra_lair", "dark_tower", "aurora_palace", "knight_stronghold",
        "yeti_cave", "troll_cave", "final_castle", "hollow_hill1", "hollow_hill2", "hollow_hill3", "mushroom_tower", "quest_grove"
    };

    MinecraftServer srv() {
        return MinecraftServer.getServer();
    }

    ServerPlayer sp() {
        return (ServerPlayer) srv().getConfigurationManager().playerEntityList.get(0);
    }

    WorldServer swOf(ServerPlayer p) {
        return srv().worldServerForDimension(p.dimension);
    }

    static Object field(Object o, Class<?> c, String name) throws Exception {
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    static void setField(Object o, Class<?> c, String name, Object v) throws Exception {
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        f.set(o, v);
    }

    /** Server side: flying, invulnerable, night vision (dark dungeons), no fall damage. */
    void prepPlayer() throws Exception {
        bot.server(() -> {
            ServerPlayer p = sp();
            p.capabilities.allowFlying = true;
            p.capabilities.isFlying = true;
            p.capabilities.disableDamage = true;
            p.sendPlayerAbilities();
            p.fallDistance = 0.0F;
            p.addPotionEffect(new PotionEffect(Potion.nightVision.id, 20 * 3600, 0));
            p.setHealth(p.getMaxHealth());
            for (WorldServer w : srv().worldServers) {
                if (w != null) {
                    if (w.provider.dimensionId == 0) {
                        int tod = w.getTimeOfDay();
                        if (tod < 1000 || tod > 11000) {
                            w.setTotalWorldTime(w.getTotalWorldTimeAtStartOfToday() + 6000L);
                        }
                    }
                    try {
                        w.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
                    } catch (Throwable ignored) {
                    }
                }
            }
        });
    }

    /** Puts the (server) player at feet position x,y,z looking at the target, and waits until the client agrees. */
    void camera(double x, double eyeY, double z, double tx, double ty, double tz) throws Exception {
        double dx = tx - x;
        double dz = tz - z;
        double dy = ty - eyeY;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        place(x, eyeY - 1.62D, z, yaw, pitch);
    }

    void place(double x, double feetY, double z, float yaw, float pitch) throws Exception {
        bot.server(() -> {
            ServerPlayer p = sp();
            p.fallDistance = 0.0F;
            p.playerNetServerHandler.setPlayerLocation(x, feetY, z, yaw, pitch);
        });
        boolean ok = bot.waitFor(() -> {
            if (mc.thePlayer == null) {
                return false;
            }
            // client posY is the eye position (feet + 1.62): compare the feet (bounding box bottom)
            return Math.abs(mc.thePlayer.posX - x) < 0.6 && Math.abs(mc.thePlayer.posZ - z) < 0.6 && Math.abs(mc.thePlayer.boundingBox.minY - feetY) < 1.2;
        }, 60);
        if (!ok && sec != null && cameraWarns++ >= 3) {
            cameraWarnsSuppressed++;
        } else if (!ok && sec != null) {
            sec.warn("camera", "client player did not reach " + x + "," + feetY + "," + z + " (is at " + (mc.thePlayer == null ? "null" : mc.thePlayer.posX + "," + mc.thePlayer.boundingBox.minY + "," + mc.thePlayer.posZ) + ")");
        }
        // keep the pose exact (client may have drifted)
        bot.client(() -> {
            mc.thePlayer.setPositionAndRotation(x, feetY, z, yaw, pitch);
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
        });
    }

    /** Holds a stack in a hotbar slot on server and client and fast-forwards the first person equip animation. */
    void hold(ItemStack stack, int slot) throws Exception {
        bot.server(() -> {
            ServerPlayer p = sp();
            p.inventory.mainInventory[slot] = stack == null ? null : stack.copy();
            p.inventory.currentItem = slot;
        });
        bot.client(() -> {
            mc.thePlayer.inventory.mainInventory[slot] = stack == null ? null : stack.copy();
            mc.thePlayer.inventory.currentItem = slot;
            try {
                Object ir = mc.entityRenderer.itemRenderer;
                Class<?> c = ir.getClass();
                setField(ir, c, "itemToRender", mc.thePlayer.inventory.getCurrentItemStack());
                setField(ir, c, "equippedItemSlot", slot);
                setField(ir, c, "equippedProgress", 1.0F);
                setField(ir, c, "prevEquippedProgress", 1.0F);
            } catch (Throwable ignored) {
            }
        });
    }

    void fillInventory(List<ItemStack> stacks) throws Exception {
        ItemStack[] copy = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            copy[i] = i < stacks.size() ? stacks.get(i).copy() : null;
        }
        bot.server(() -> {
            ServerPlayer p = sp();
            for (int i = 0; i < 36; i++) {
                p.inventory.mainInventory[i] = copy[i] == null ? null : copy[i].copy();
            }
        });
        bot.client(() -> {
            for (int i = 0; i < 36; i++) {
                mc.thePlayer.inventory.mainInventory[i] = copy[i] == null ? null : copy[i].copy();
            }
        });
    }

    /**
     * Opens the player's own inventory screen (36 slots + armor slots + paper doll). In MITE dev mode the client controller is "creative", where
     * GuiInventory redirects to GuiContainerCreative (which shows the last used creative tab, i.e. a block list). Its static tab index is switched to
     * the survival inventory tab first so the screenshot really shows the inventory contents and the worn armor.
     */
    void openInventory() throws Exception {
        bot.client(() -> {
            try {
                Field f = net.minecraft.GuiContainerCreative.class.getDeclaredField("selectedTabIndex");
                f.setAccessible(true);
                f.setInt(null, net.minecraft.CreativeTabs.tabInventory.getTabIndex());
            } catch (Throwable ignored) {
                // survival inventory screen needs no tab switch
            }
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        });
    }

    void clearInventory() throws Exception {
        fillInventory(new ArrayList<>());
        bot.server(() -> {
            ServerPlayer p = sp();
            for (int i = 0; i < 4; i++) {
                p.inventory.armorInventory[i] = null;
            }
        });
        bot.client(() -> {
            for (int i = 0; i < 4; i++) {
                mc.thePlayer.inventory.armorInventory[i] = null;
            }
        });
    }

    // ------------------------------------------------------------------ item / block enumeration

    boolean isTfItem(Item it) {
        if (it == null) {
            return false;
        }
        return it.itemID >= TFIds.ITEM_FIRST || it.getClass().getName().startsWith("twilightforest.")
            || (it instanceof ItemBlock && it.itemID >= TFIds.BLOCK_FIRST && it.itemID <= TFIds.BLOCK_LAST);
    }

    /** Every TF item and every subtype (blocks included): one ItemStack each. */
    List<ItemStack> allStacks(Report.Section s) {
        List<ItemStack> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int id = 0; id < Item.itemsList.length; id++) {
            Item it = Item.itemsList[id];
            if (!isTfItem(it)) {
                continue;
            }
            List<ItemStack> subs = new ArrayList<>();
            try {
                List raw = it.getSubItems();
                if (raw != null) {
                    for (Object o : raw) {
                        if (o instanceof ItemStack st) {
                            subs.add(st);
                        }
                    }
                }
            } catch (Throwable t) {
                s.exception("getSubItems of " + it.getClass().getSimpleName() + " #" + id, t);
            }
            if (subs.isEmpty()) {
                subs.add(new ItemStack(it, 1, 0));
            }
            for (ItemStack st : subs) {
                if (seen.add(id + ":" + st.getItemSubtype())) {
                    out.add(st);
                }
            }
        }
        return out;
    }

    String label(ItemStack st) {
        return st.getItem().getClass().getSimpleName() + "#" + st.itemID + ":" + st.getItemSubtype();
    }

    Set<String> loadLang(String lang) {
        Set<String> keys = new HashSet<>();
        try (InputStream in = TestBotScript.class.getResourceAsStream("/assets/twilightforest/lang/" + lang + ".lang")) {
            if (in == null) {
                return null;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq > 0 && !line.startsWith("#")) {
                    keys.add(line.substring(0, eq).trim());
                }
            }
        } catch (Exception e) {
            return null;
        }
        return keys;
    }

    static boolean badIcon(Icon i) {
        return i == null || "missingno".equals(i.getIconName());
    }

    /** Null / missingno icon checks for the item sprite and (blocks) all six faces. */
    void checkIcons(Report.Section s, ItemStack st) {
        String lab = label(st);
        Item it = st.getItem();
        boolean bad = false;
        try {
            Icon icon = it.getIconIndex(st);
            if (badIcon(icon)) {
                s.fail("icon " + lab, "item icon is " + (icon == null ? "null" : icon.getIconName()));
                bad = true;
            }
        } catch (Throwable t) {
            s.exception("item icon " + lab, t);
            bad = true;
        }
        if (it instanceof ItemBlock && st.itemID < Block.blocksList.length && Block.blocksList[st.itemID] != null) {
            Block b = Block.blocksList[st.itemID];
            int meta = st.getHasSubtypes() ? st.getItemSubtype() : 0; // MITE: the placement metadata of a subtype block item is the stack subtype
            for (int side = 0; side < 6; side++) {
                try {
                    Icon ic = b.getIcon(side, meta);
                    if (badIcon(ic)) {
                        s.fail("block icon " + lab, "side " + side + " meta " + meta + " -> " + (ic == null ? "null" : ic.getIconName()));
                        bad = true;
                        break;
                    }
                } catch (Throwable t) {
                    s.exception("block icon " + lab + " side " + side, t);
                    bad = true;
                    break;
                }
            }
        }
        // what the player really sees: the display name resolved on the client (a missing key is shown raw, e.g. "item.fooNugget.name")
        try {
            String shown = bot.client(() -> st.getDisplayName());
            String key = st.getUnlocalizedName() + ".name";
            boolean raw = shown == null || shown.isEmpty() || shown.equals(key) || shown.startsWith("tile.") || shown.startsWith("item.");
            if (raw) {
                boolean technical = st.isBlock() && !st.getItemAsBlock().getBlock().canBeCarried();
                if (technical) {
                    s.warn("display name " + lab, "technical (not carriable) block shows the raw key '" + shown + "'");
                } else {
                    s.fail("display name " + lab, "the player sees the raw translation key '" + shown + "'");
                    bad = true;
                }
            }
        } catch (Throwable t) {
            s.exception("display name " + lab, t);
        }
        if (langEn == null) {
            langEn = loadLang("en_US");
            langZh = loadLang("zh_CN");
        }
        try {
            String key = st.getUnlocalizedName() + ".name";
            if (langEn != null && !langEn.contains(key) && (isOnlyOurs(it))) {
                s.warn("lang " + lab, "en_US.lang has no key " + key);
            }
            if (langZh != null && !langZh.contains(key) && isOnlyOurs(it)) {
                s.warn("lang " + lab, "zh_CN.lang has no key " + key);
            }
        } catch (Throwable ignored) {
        }
        if (!bad) {
            s.ok("icon " + lab);
        }
    }

    boolean isOnlyOurs(Item it) {
        return it.getClass().getName().startsWith("twilightforest.") || it.itemID >= TFIds.ITEM_FIRST
            || (it.itemID >= TFIds.BLOCK_FIRST && it.itemID <= TFIds.BLOCK_LAST);
    }

    // ================================================================== 01 world

    void createWorld(Report.Section s) throws Exception {
        boolean menu = bot.waitFor(() -> mc.theWorld == null && mc.currentScreen instanceof GuiMainMenu, 3000);
        s.check(menu, "main menu reached", "main menu did not appear within 150 s; trying to create the world anyway");
        bot.client(() -> {
            mc.gameSettings.pauseOnLostFocus = false;
            mc.gameSettings.showDebugInfo = false;
        });
        bot.clientCall(() -> {
            mc.getSaveLoader().deleteWorldDirectory(WORLD);
            WorldSettings ws = new WorldSettings(424242L, EnumGameType.CREATIVE, false, false, WorldType.FLAT, false).enableCommands();
            mc.launchIntegratedServer(WORLD, WORLD, ws);
            return null;
        }, 600_000);
        boolean in = bot.waitFor(() -> mc.theWorld != null && mc.thePlayer != null && mc.currentScreen == null, 1200);
        s.check(in, "world created and entered (flat, seed 424242, creative requested, TF dimension available)", "player never entered the world");
        if (!in) {
            bot.abort("could not create/enter the test world");
            return;
        }
        bot.ticks(40);
        boolean on = bot.server(() -> TFTestMode.set(true));
        s.check(on, "TF test mode switched on (all gates open)", "TFTestMode.set(true) returned false");
        s.info("creative", "EnumGameType.CREATIVE only counts in MITE dev mode; the bot sets flying + invulnerable on the player abilities itself");
        // platform
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (int x = -48; x < 48; x++) {
                for (int z = -48; z < 48; z++) {
                    w.setBlock(x, GROUND_Y, z, Block.stone.blockID, 0, 2);
                }
            }
        });
        prepPlayer();
        place(0.5D, FEET_Y, 0.5D, 0.0F, 15.0F);
        bot.ticks(40);
        bot.frames(10);
        s.ok("arena platform built at y=" + GROUND_Y);
        rep.setWorldNote("flat world '" + WORLD + "', seed 424242, test mode on, arena platform 96x96 at y=" + GROUND_Y);
        bot.screenshot(s, "world_start.png");
    }

    // ================================================================== 02 items

    void stepItems(Report.Section s) throws Exception {
        List<ItemStack> all = allStacks(s);
        s.info("count", all.size() + " TF item/block subtypes");
        place(0.5D, FEET_Y, 0.5D, 0.0F, 20.0F);
        bot.client(() -> mc.gameSettings.thirdPersonView = 0);
        for (ItemStack st : all) {
            checkIcons(s, st);
        }
        int batchNo = 0;
        for (int from = 0; from < all.size(); from += 36) {
            List<ItemStack> batch = all.subList(from, Math.min(all.size(), from + 36));
            batchNo++;
            String bn = String.format("%02d", batchNo);

            // 1. first person hand, one item at a time
            for (ItemStack st : batch) {
                try {
                    bot.status("02 items: hand " + label(st));
                    hold(st, 0);
                    bot.ticks(1);
                    bot.frames(2);
                    bot.client(() -> mc.thePlayer.swingArm());
                    bot.frames(3);
                    bot.pace(HAND_HOLD_TICKS); // the user watches: every item stays in the hand for ~1 s
                    s.ok("hand " + label(st));
                } catch (Throwable t) {
                    s.exception("hand render " + label(st), t);
                }
            }
            bot.screenshot(s, "items/hand_batch" + bn + "_last.png");

            // 2. inventory icons (36 slots at once)
            try {
                fillInventory(batch);
                openInventory();
                bot.frames(4);
                bot.screenshot(s, "items/inventory_batch" + bn + ".png");
                bot.client(() -> mc.displayGuiScreen(null));
                bot.frames(2);
                s.ok("inventory batch " + bn + " (" + batch.size() + " icons)");
            } catch (Throwable t) {
                s.exception("inventory render batch " + bn, t);
                try {
                    bot.client(() -> mc.displayGuiScreen(null));
                } catch (Exception ignored) {
                }
            }

            // 3. dropped EntityItem, 6x6 grid in front of the player
            try {
                List<Integer> ids = new ArrayList<>();
                bot.server(() -> {
                    ServerPlayer p = sp();
                    WorldServer w = swOf(p);
                    for (int i = 0; i < batch.size(); i++) {
                        double x = -1.5 + (i % 6) * 1.0;
                        double z = 3.5 + (i / 6) * 1.0;
                        ItemStack drop = batch.get(i);
                        if (drop.isBlock() && !drop.getItemAsBlock().getBlock().canBeCarried()) {
                            continue; // technical block (portal, spawner, double slab...): MITE itself logs an error when such a stack is dropped as an item
                        }
                        EntityItem e = new EntityItem(w, x, FEET_Y + 0.3D, z, drop.copy());
                        e.motionX = 0;
                        e.motionY = 0;
                        e.motionZ = 0;
                        e.delayBeforeCanPickup = 32767;
                        w.spawnEntityInWorld(e);
                        ids.add(e.entityId);
                    }
                });
                camera(0.5D, FEET_Y + 4.5D, 0.5D, 1.0D, FEET_Y, 6.0D);
                bot.ticks(8);
                bot.frames(4);
                bot.screenshot(s, "items/drops_batch" + bn + ".png");
                int synced = bot.client(() -> {
                    int n = 0;
                    for (int id : ids) {
                        if (mc.theWorld.getEntityByID(id) != null) {
                            n++;
                        }
                    }
                    return n;
                });
                s.check(synced == ids.size(), "dropped EntityItem batch " + bn + " synced to client", synced + "/" + ids.size() + " EntityItems reached the client");
                bot.server(() -> {
                    WorldServer w = swOf(sp());
                    for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
                        if (o instanceof EntityItem) {
                            ((EntityItem) o).setDead();
                        }
                    }
                });
                place(0.5D, FEET_Y, 0.5D, 0.0F, 20.0F);
            } catch (Throwable t) {
                s.exception("dropped item render batch " + bn, t);
            }
        }
        clearInventory();
    }

    // ================================================================== 03 armor

    void stepArmor(Report.Section s) throws Exception {
        Map<String, List<ItemArmor>> sets = new LinkedHashMap<>();
        for (int id = 0; id < Item.itemsList.length; id++) {
            Item it = Item.itemsList[id];
            if (it instanceof ItemArmor a && isTfItem(it)) {
                Material m = a.getArmorMaterial();
                String key = (m == null ? "null" : String.valueOf(m));
                sets.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
            }
        }
        int pieces = 0;
        for (List<ItemArmor> l : sets.values()) {
            pieces += l.size();
        }
        s.info("count", pieces + " TF armor pieces in " + sets.size() + " material sets: " + sets.keySet());
        place(0.5D, FEET_Y, 0.5D, 0.0F, 0.0F);
        for (Map.Entry<String, List<ItemArmor>> e : sets.entrySet()) {
            String set = e.getKey().replaceAll("[^A-Za-z0-9_]", "_");
            bot.status("03 armor set " + set);
            try {
                ItemStack[] worn = new ItemStack[4];
                for (ItemArmor a : e.getValue()) {
                    int slot = 3 - a.armorType;
                    if (slot >= 0 && slot < 4) {
                        worn[slot] = new ItemStack(a, 1, 0);
                    }
                }
                bot.server(() -> {
                    ServerPlayer p = sp();
                    for (int i = 0; i < 4; i++) {
                        p.inventory.armorInventory[i] = worn[i] == null ? null : worn[i].copy();
                    }
                    p.inventory.mainInventory[0] = null;
                });
                bot.client(() -> {
                    for (int i = 0; i < 4; i++) {
                        mc.thePlayer.inventory.armorInventory[i] = worn[i] == null ? null : worn[i].copy();
                    }
                    mc.thePlayer.inventory.mainInventory[0] = null;
                });
                // third person, back then front (toggle view)
                bot.client(() -> mc.gameSettings.thirdPersonView = 1);
                bot.ticks(3);
                bot.frames(4);
                bot.client(() -> mc.gameSettings.thirdPersonView = 2);
                bot.ticks(3);
                bot.frames(4);
                bot.screenshot(s, "armor/" + set + "_thirdperson_front.png");
                bot.client(() -> mc.gameSettings.thirdPersonView = 1);
                bot.ticks(2);
                bot.frames(3);
                bot.screenshot(s, "armor/" + set + "_thirdperson_back.png");
                bot.client(() -> mc.gameSettings.thirdPersonView = 0);
                // inventory (paper doll + armor slots)
                openInventory();
                bot.frames(4);
                bot.screenshot(s, "armor/" + set + "_inventory.png");
                bot.client(() -> mc.displayGuiScreen(null));
                bot.frames(2);
                for (ItemArmor a : e.getValue()) {
                    checkIcons(s, new ItemStack(a, 1, 0));
                }
                s.ok("armor set " + e.getKey() + " (" + e.getValue().size() + " pieces) rendered");
            } catch (Throwable t) {
                s.exception("armor set " + e.getKey(), t);
                try {
                    bot.client(() -> {
                        mc.gameSettings.thirdPersonView = 0;
                        mc.displayGuiScreen(null);
                    });
                } catch (Exception ignored) {
                }
            }
        }
        bot.client(() -> mc.gameSettings.thirdPersonView = 0);
        clearInventory();
    }

    // ================================================================== 04 weapons

    static final Map<String, String> EXPECT_SPAWN = new LinkedHashMap<>();

    static {
        EXPECT_SPAWN.put("ItemTFIceBomb", "EntityTFIceBomb");
        EXPECT_SPAWN.put("ItemTFChainBlock", "EntityTFChainBlock");
        EXPECT_SPAWN.put("ItemTFCubeOfAnnihilation", "EntityTFCubeOfAnnihilation");
        EXPECT_SPAWN.put("ItemTFTwilightWand", "EntityTFTwilightWandBolt");
        EXPECT_SPAWN.put("ItemTFZombieWand", "EntityTFLoyalZombie");
    }

    boolean isMelee(Item it) {
        return it instanceof net.minecraft.ItemSword || it instanceof net.minecraft.ItemTool || it instanceof twilightforest.compat.ITFMeleeItem;
    }

    void clearEntities() throws Exception {
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
                if (!(o instanceof net.minecraft.EntityPlayer)) {
                    ((Entity) o).setDead();
                }
            }
        });
    }

    int spawnDummy() throws Exception {
        return bot.server(() -> {
            WorldServer w = swOf(sp());
            EntityPig pig = new EntityPig(w);
            pig.setLocationAndAngles(0.5D, FEET_Y, 2.9D, 180.0F, 0.0F);
            w.spawnEntityInWorld(pig);
            return pig.entityId;
        });
    }

    void recentreDummy(int id) throws Exception {
        bot.server(() -> {
            Entity e = swOf(sp()).getEntityByID(id);
            if (e != null && !e.isDead) {
                e.setLocationAndAngles(0.5D, FEET_Y, 2.9D, 180.0F, 0.0F);
                e.motionX = 0;
                e.motionZ = 0;
                if (e instanceof EntityLivingBase l) {
                    l.setHealth(l.getMaxHealth());
                }
            }
        });
    }

    float dummyHealth(int id) throws Exception {
        return bot.server(() -> {
            Entity e = swOf(sp()).getEntityByID(id);
            if (e == null || e.isDead) {
                return -1.0F;
            }
            return e instanceof EntityLivingBase l ? l.getHealth() : 0.0F;
        });
    }

    Map<Integer, String> entitySnapshot() throws Exception {
        return bot.server(() -> {
            Map<Integer, String> m = new LinkedHashMap<>();
            for (Object o : new ArrayList<Object>(swOf(sp()).loadedEntityList)) {
                Entity e = (Entity) o;
                if (!(e instanceof net.minecraft.EntityPlayer)) {
                    m.put(e.entityId, e.getClass().getSimpleName());
                }
            }
            return m;
        });
    }

    /**
     * Spawn egg (MITE ItemMonsterPlacer, also the TF egg): the egg spawns its creature on the block the player is looking at, so the aim goes to the
     * floor (no dummy pig in the way: an entity under the crosshair would turn the click into an entity interaction instead of a spawn).
     */
    void testSpawnEgg(Report.Section s, ItemStack st, String lab) throws Exception {
        clearEntities();
        hold(st, 0);
        camera(0.5D, FEET_Y + 1.62D, 0.5D, 0.5D, FEET_Y, 3.5D);
        bot.ticks(2);
        bot.startSpawnRecording();
        bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = true);
        bot.ticks(3);
        bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = false);
        bot.ticks(6);
        bot.frames(3);
        List<String> rec = bot.stopSpawnRecording();
        Class<?> want = EntityList.getClassFromID(st.getItemSubtype());
        boolean got = false;
        for (String r : rec) {
            got |= want != null && r.endsWith(":" + want.getSimpleName());
        }
        s.check(got, "spawn egg " + lab + " spawns " + (want == null ? "null" : want.getSimpleName()),
            "right click on the floor spawned " + rec + " (expected " + (want == null ? "an entity class for id " + st.getItemSubtype() : want.getSimpleName()) + ")");
        if (!got) {
            bot.screenshot(s, "weapons/egg_" + st.getItemSubtype() + "_failed.png");
        }
        clearEntities();
    }

    void leftClick() throws Exception {
        bot.client(() -> {
            try {
                setField(mc, Minecraft.class, "leftClickCounter", 0); // BotMinecraftMixin sets it to 10000 (about 8 minutes of blocked clicks)
                Method m = Minecraft.class.getDeclaredMethod("clickMouse", int.class);
                m.setAccessible(true);
                m.invoke(mc, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    void stepWeapons(Report.Section s) throws Exception {
        List<ItemStack> cands = new ArrayList<>();
        Map<Integer, Integer> perItem = new LinkedHashMap<>();
        for (ItemStack st : allStacks(s)) {
            Item it = st.getItem();
            if (it instanceof ItemBlock || it instanceof ItemArmor) {
                continue;
            }
            if (!weaponFilter.isEmpty() && !weaponFilter.contains(it.getClass().getSimpleName().toLowerCase())) {
                continue;
            }
            int n = perItem.merge(st.itemID, 1, Integer::sum);
            // spawn eggs: every creature subtype is tested (one cheap use each); everything else at most 3 subtypes per item
            if (n <= 3 || it instanceof net.minecraft.ItemMonsterPlacer || !weaponFilter.isEmpty()) {
                cands.add(st);
            }
        }
        s.info("count", cands.size() + " non-block, non-armor item stacks (max 3 subtypes per item, all spawn egg subtypes)");
        ItemStack[] ammo = {new ItemStack(Item.arrowFlint, 64), new ItemStack(Item.arrowIron, 64), new ItemStack(Item.arrowCopper, 64)};
        bot.server(() -> {
            ServerPlayer p = sp();
            for (int i = 0; i < ammo.length; i++) {
                p.inventory.mainInventory[9 + i] = ammo[i].copy();
            }
        });
        bot.client(() -> {
            for (int i = 0; i < ammo.length; i++) {
                mc.thePlayer.inventory.mainInventory[9 + i] = ammo[i].copy();
            }
            mc.gameSettings.thirdPersonView = 0;
        });
        for (ItemStack st : cands) {
            String lab = label(st);
            Item it = st.getItem();
            bot.status("04 weapon/tool " + lab);
            if (it instanceof net.minecraft.ItemMonsterPlacer) {
                try {
                    testSpawnEgg(s, st, lab);
                } catch (Throwable t) {
                    s.exception("spawn egg test " + lab, t);
                    try {
                        bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = false);
                    } catch (Exception ignored) {
                    }
                }
                continue;
            }
            try {
                clearEntities();
                int dummy = spawnDummy();
                hold(st, 0);
                camera(0.5D, FEET_Y + 1.62D, 0.5D, 0.5D, FEET_Y + 0.5D, 2.9D);
                bot.ticks(3);
                // melee swings
                boolean melee = isMelee(it);
                boolean damaged = false;
                for (int i = 0; i < 2; i++) {
                    recentreDummy(dummy);
                    bot.ticks(2);
                    float before = dummyHealth(dummy);
                    leftClick();
                    bot.ticks(3);
                    bot.frames(2);
                    float after = dummyHealth(dummy);
                    if (after < before) {
                        damaged = true;
                    }
                    bot.ticks(5);
                }
                if (melee) {
                    if (damaged) {
                        s.ok("melee " + lab);
                    } else {
                        s.warn("melee " + lab, "swing at the dummy pig did not reduce its health");
                    }
                }
                // use / draw / throw
                recentreDummy(dummy);
                camera(0.5D, FEET_Y + 1.62D, 0.5D, 0.5D, FEET_Y + 0.5D, 2.9D);
                hold(st, 0);
                // projectiles (scepter bolts, ice bombs, arrows) hit the dummy and die within a tick or two: BotWorldMixin records every server side
                // spawn while the window is open, polling the entity list would miss them
                bot.startSpawnRecording();
                boolean bow = it instanceof ItemBow;
                bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = true);
                bot.ticks(bow ? 28 : 10);
                bot.frames(2);
                if (bow) {
                    bot.screenshot(s, "weapons/" + lab.replace('#', '_').replace(':', '_') + "_drawn.png");
                }
                bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = false);
                bot.ticks(6);
                bot.frames(3);
                if (EXPECT_SPAWN.containsKey(it.getClass().getSimpleName())) {
                    bot.screenshot(s, "weapons/" + lab.replace('#', '_').replace(':', '_') + "_effect.png");
                }
                bot.ticks(2);
                Map<String, Integer> spawned = new LinkedHashMap<>();
                for (String rec : bot.stopSpawnRecording()) {
                    spawned.merge(rec.substring(rec.indexOf(':') + 1), 1, Integer::sum);
                }
                String simple = it.getClass().getSimpleName();
                String expect = EXPECT_SPAWN.get(simple);
                if (expect != null) {
                    s.check(spawned.containsKey(expect), "use " + lab + " spawns " + expect, "spawned only " + spawned);
                } else if (bow) {
                    boolean arrow = false;
                    for (String n : spawned.keySet()) {
                        arrow |= n.contains("Arrow");
                    }
                    s.check(arrow, "bow " + lab + " drew and released an arrow", "no arrow entity after full draw; spawned " + spawned);
                } else {
                    s.ok("use " + lab + (spawned.isEmpty() ? "" : " -> " + spawned));
                }
                if (!spawned.isEmpty() && !(bow) && expect == null) {
                    s.info("effects " + lab, spawned.toString());
                }
                bot.ticks(2);
            } catch (Throwable t) {
                s.exception("weapon test " + lab, t);
                try {
                    bot.client(() -> mc.gameSettings.keyBindUseItem.pressed = false);
                } catch (Exception ignored) {
                }
            }
        }
        // one showcase screenshot of the effect/projectile area with a bomb flying
        clearEntities();
        clearInventory();
    }

    // ================================================================== 05 entities

    void stepEntities(Report.Section s) throws Exception {
        clearInventory();
        List<Map.Entry<Class<?>, String>> living = new ArrayList<>(TFCreatures.registeredNames().entrySet());
        s.info("count", living.size() + " registered TF creatures, " + TFCreatures.nonLivingNames().size() + " named non-living (projectiles, parts)");
        for (Map.Entry<Class<?>, String> e : living) {
            String name = e.getValue();
            if (skip.contains(name.toLowerCase())) {
                s.info("skipped " + name, "-Dtwilightforest.testbot.skip");
                continue;
            }
            testEntity(s, name, e.getKey(), false);
        }
        for (Map.Entry<Class<?>, String> e : TFCreatures.nonLivingNames().entrySet()) {
            String name = e.getValue();
            if (skip.contains(name.toLowerCase())) {
                s.info("skipped " + name, "-Dtwilightforest.testbot.skip");
                continue;
            }
            testEntity(s, name, e.getKey(), true);
        }
        clearEntities();
    }

    Entity construct(WorldServer w, Class<?> c, String fullName, boolean nonLiving, Report.Section s) throws Exception {
        if (!nonLiving) {
            return EntityList.createEntityByName(twilightforest.compat.TFRegistry.NAMESPACE + ":" + fullName, w);
        }
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == World.class) {
                return (Entity) k.newInstance(w);
            }
        }
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 4 && p[0] == World.class && p[1] == double.class && p[2] == double.class && p[3] == double.class) {
                return (Entity) k.newInstance(w, 0.5D, FEET_Y + 1.0D, 6.5D);
            }
        }
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == World.class && EntityLivingBase.class.isAssignableFrom(p[1])) {
                return (Entity) k.newInstance(w, sp());
            }
        }
        return null;
    }

    void testEntity(Report.Section s, String name, Class<?> cls, boolean nonLiving) {
        bot.status("05 entity " + name);
        try {
            clearEntities();
            // [id, width*100, height*100] or null when it cannot be built
            int[] info = bot.server(() -> {
                ServerPlayer p = sp();
                WorldServer w = swOf(p);
                Entity e = construct(w, cls, name, nonLiving, s);
                if (e == null) {
                    return null;
                }
                double dist = Math.max(4.0D, 2.0D * Math.max(e.width, e.height) + 3.0D);
                e.setLocationAndAngles(0.5D, FEET_Y + (nonLiving ? 1.0D : 0.0D), 0.5D + dist, 180.0F, 0.0F);
                boolean ok = w.spawnEntityInWorld(e);
                return new int[] {e.entityId, (int) (e.width * 100), (int) (e.height * 100), (int) (dist * 100), ok ? 1 : 0};
            });
            if (info == null) {
                s.info("entity " + name, "no (World) / (World,x,y,z) / (World,EntityLivingBase) constructor - spawned by its owner only (weapons step)");
                return;
            }
            if (info[4] == 0) {
                s.warn("entity " + name, "spawnEntityInWorld returned false");
            }
            double dist = info[3] / 100.0D;
            double h = info[2] / 100.0D;
            camera(0.5D, FEET_Y + 2.0D, 0.5D, 0.5D, FEET_Y + h / 2.0D + (nonLiving ? 1.0D : 0.0D), 0.5D + dist);
            bot.ticks(10);
            bot.frames(3);
            int id = info[0];
            boolean synced = bot.client(() -> mc.theWorld.getEntityByID(id) != null);
            boolean aliveServer = bot.server(() -> {
                Entity e = swOf(sp()).getEntityByID(id);
                return e != null && !e.isDead;
            });
            if (!synced) {
                if (aliveServer || !nonLiving) {
                    s.fail("entity " + name, "not present on the client (spawn packet / entity type registration problem); server entity alive=" + aliveServer);
                } else {
                    s.warn("entity " + name, "died/vanished before it could be rendered (projectile?)");
                }
            }
            String rend = bot.client(() -> {
                Render r = RenderManager.instance.getEntityClassRenderObject(cls);
                return r == null ? "null" : r.getClass().getName();
            });
            boolean generic = rend.equals("null") || rend.equals("net.minecraft.RenderEntity") || rend.equals("net.minecraft.Render");
            if (generic) {
                if (nonLiving) {
                    s.warn("renderer " + name, "only the generic renderer " + rend + " (invisible/placeholder?)");
                } else {
                    s.fail("renderer " + name, "living entity has no dedicated renderer (" + rend + ")");
                }
            }
            if (synced && !generic) {
                s.ok("entity " + name + " rendered by " + rend.substring(rend.lastIndexOf('.') + 1));
            }
            bot.screenshot(s, "entities/" + name + ".png");
            bot.frames(2);
        } catch (Throwable t) {
            s.exception("entity " + name, t);
        }
    }

    // ================================================================== 06 placed block grids

    void stepBlockGrid(Report.Section s) throws Exception {
        // every TF block id with every subtype (ItemBlock subitems when present, else metadata 0)
        List<int[]> blocks = new ArrayList<>();
        Map<Integer, Integer> renderTypes = new LinkedHashMap<>();
        for (int id = TFIds.BLOCK_FIRST; id <= TFIds.BLOCK_LAST && id < Block.blocksList.length; id++) {
            Block b = Block.blocksList[id];
            if (b == null) {
                continue;
            }
            Set<Integer> metas = new java.util.TreeSet<>();
            Item item = Item.itemsList[id];
            if (item instanceof ItemBlock) {
                try {
                    List raw = item.getSubItems();
                    if (raw != null) {
                        for (Object o : raw) {
                            if (o instanceof ItemStack st) {
                                metas.add(st.getHasSubtypes() ? st.getItemSubtype() : 0);
                            }
                        }
                    }
                } catch (Throwable t) {
                    s.exception("subtypes of block " + b.getClass().getSimpleName() + " #" + id, t);
                }
            }
            if (metas.isEmpty()) {
                metas.add(0);
            }
            renderTypes.merge(b.getRenderType(), 1, Integer::sum);
            for (int m : metas) {
                blocks.add(new int[] {id, m});
                if (!(item instanceof ItemBlock)) {
                    // blocks without an item (portal, ...) were not icon-checked in step 02
                    for (int side = 0; side < 6; side++) {
                        try {
                            Icon ic = b.getIcon(side, m);
                            if (badIcon(ic)) {
                                s.fail("block icon " + b.getClass().getSimpleName() + "#" + id, "side " + side + " meta " + m + " -> " + (ic == null ? "null" : ic.getIconName()));
                                break;
                            }
                        } catch (Throwable t) {
                            s.exception("block icon " + b.getClass().getSimpleName() + "#" + id, t);
                            break;
                        }
                    }
                }
            }
        }
        s.info("count", blocks.size() + " block subtypes; render types (type=count of block ids): " + renderTypes);
        final int per = 64;
        final double cx = 0.5D;
        final double cz = 17.5D;
        int page = 0;
        for (int from = 0; from < blocks.size(); from += per) {
            page++;
            List<int[]> chunk = blocks.subList(from, Math.min(blocks.size(), from + per));
            String pn = String.format("%02d", page);
            bot.status("06 block page " + pn);
            try {
                List<String> mismatches = new ArrayList<>();
                bot.server(() -> {
                    WorldServer w = swOf(sp());
                    for (int i = 0; i < chunk.size(); i++) {
                        int x = -7 + (i % 8) * 2;
                        int z = 10 + (i / 8) * 2;
                        int[] bm = chunk.get(i);
                        w.setBlock(x, FEET_Y, z, bm[0], bm[1], 2);
                    }
                    for (int i = 0; i < chunk.size(); i++) {
                        int x = -7 + (i % 8) * 2;
                        int z = 10 + (i / 8) * 2;
                        int[] bm = chunk.get(i);
                        int gotId = w.getBlockId(x, FEET_Y, z);
                        if (gotId != bm[0]) {
                            mismatches.add("#" + bm[0] + ":" + bm[1] + " at " + x + "," + z + " became block id " + gotId);
                        }
                    }
                });
                for (String m : mismatches) {
                    s.warn("placement page " + pn, m);
                }
                bot.ticks(12);
                // client has the blocks?
                List<String> clientMissing = bot.client(() -> {
                    List<String> miss = new ArrayList<>();
                    for (int i = 0; i < chunk.size(); i++) {
                        int x = -7 + (i % 8) * 2;
                        int z = 10 + (i / 8) * 2;
                        int[] bm = chunk.get(i);
                        int got = mc.theWorld.getBlockId(x, FEET_Y, z);
                        if (got != bm[0]) {
                            miss.add("#" + bm[0] + ":" + bm[1] + " client has " + got);
                        }
                    }
                    return miss;
                });
                for (String m : clientMissing) {
                    if (!mismatches.toString().contains(m.substring(0, m.indexOf(' ')))) {
                        s.warn("client sync page " + pn, m);
                    }
                }
                int pagesBlocks = chunk.size();
                // three camera angles
                double ty = FEET_Y + 0.5D;
                camera(cx, FEET_Y + 11.0D, 0.5D, cx, ty, cz);
                bot.ticks(6);
                bot.frames(4);
                bot.screenshot(s, "blocks/page" + pn + "_front.png");
                camera(-16.0D, FEET_Y + 7.0D, cz, 0.5D, ty, cz);
                bot.ticks(4);
                bot.frames(4);
                bot.screenshot(s, "blocks/page" + pn + "_side.png");
                camera(cx, FEET_Y + 24.0D, cz - 0.01D, cx, ty, cz);
                bot.ticks(4);
                bot.frames(4);
                bot.screenshot(s, "blocks/page" + pn + "_top.png");
                s.ok("block page " + pn + " (" + pagesBlocks + " subtypes, 3 angles)");
                bot.server(() -> {
                    WorldServer w = swOf(sp());
                    for (int i = 0; i < chunk.size(); i++) {
                        w.setBlock(-7 + (i % 8) * 2, FEET_Y, 10 + (i / 8) * 2, 0, 0, 2);
                    }
                });
                bot.ticks(4);
            } catch (Throwable t) {
                s.exception("block page " + pn, t);
            }
        }
        place(0.5D, FEET_Y, 0.5D, 0.0F, 0.0F);
    }

    // ================================================================== 07 portal

    void stepPortal(Report.Section s) throws Exception {
        Block portal = TFBlocks.portal;
        s.check(portal != null, "TFBlocks.portal exists", "null");
        if (portal == null) {
            return;
        }
        int purple = 0;
        for (int side = 0; side < 6; side++) {
            for (int meta = 0; meta < 4; meta++) {
                Icon ic = portal.getIcon(side, meta);
                if (badIcon(ic)) {
                    s.fail("portal icon side " + side + " meta " + meta, ic == null ? "null" : ic.getIconName());
                } else if (!ic.getIconName().contains("TFPortal")) {
                    s.fail("portal icon side " + side + " meta " + meta, "icon is " + ic.getIconName() + " (expected twilightforest:TFPortal, the purple sprite)");
                } else {
                    purple++;
                }
            }
        }
        if (purple == 24) {
            s.ok("portal getIcon() is twilightforest:TFPortal for every side/meta");
        }
        Icon nether = Block.portal.getIcon(0, 2);
        Icon ours = portal.getIcon(0, 0);
        s.check(ours != null && nether != null && ours != nether, "TF portal icon differs from the red MITE nether portal icon", "same Icon object as Block.portal");
        // pixel colour of the sprite: purple means blue and red clearly above green
        if (ours instanceof TextureAtlasSprite sprite && sprite.getFrameCount() > 0) {
            long r = 0;
            long g = 0;
            long b = 0;
            long n = 0;
            for (int f = 0; f < sprite.getFrameCount(); f++) {
                int[] data = sprite.getFrameTextureData(f);
                if (data == null) {
                    continue;
                }
                for (int px : data) {
                    int a = px >>> 24;
                    if (a < 32) {
                        continue;
                    }
                    r += (px >> 16) & 0xFF;
                    g += (px >> 8) & 0xFF;
                    b += px & 0xFF;
                    n++;
                }
            }
            if (n > 0) {
                s.check(b > g + 10 * n && r > g + 5 * n, "portal sprite average colour is purple (r=" + r / n + " g=" + g / n + " b=" + b / n + ")",
                    "average colour r=" + r / n + " g=" + g / n + " b=" + b / n + " is not purple");
            } else {
                s.warn("portal sprite", "no opaque pixels");
            }
        } else {
            s.info("portal sprite", "icon is not a TextureAtlasSprite with frame data, colour not sampled");
        }
        // place a portal in an obsidian frame and look at it (client world icon query + screenshot)
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (int dx = -1; dx <= 2; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    boolean edge = dx == -1 || dx == 2 || dy == 0 || dy == 4;
                    w.setBlock(dx, FEET_Y + dy, 12, edge ? Block.obsidian.blockID : portal.blockID, 0, 2);
                }
            }
        });
        bot.ticks(12);
        camera(0.5D, FEET_Y + 3.0D, 6.0D, 0.5D, FEET_Y + 2.5D, 12.0D);
        bot.ticks(6);
        bot.frames(5);
        String clientIcon = bot.client(() -> {
            Icon ic = portal.getBlockTexture(mc.theWorld, 0, FEET_Y + 2, 12, 2);
            return ic == null ? "null" : ic.getIconName();
        });
        s.check(clientIcon.contains("TFPortal"), "portal getBlockTexture(world,x,y,z,side) -> " + clientIcon, "icon is " + clientIcon);
        s.check(bot.client(() -> mc.theWorld.getBlockId(0, FEET_Y + 2, 12) == portal.blockID), "portal block present on the client", "client world has a different block");
        bot.screenshot(s, "portal.png");
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (int dx = -1; dx <= 2; dx++) {
                for (int dy = 0; dy <= 4; dy++) {
                    w.setBlock(dx, FEET_Y + dy, 12, 0, 0, 2);
                }
            }
        });
        place(0.5D, FEET_Y, 0.5D, 0.0F, 0.0F);
    }

    // ================================================================== 08 structures

    void stepStructure(Report.Section s, String name) throws Exception {
        TFFeature expected = TFStructureLocator.byName(name);
        s.check(expected != null, "locator knows '" + name + "'", "TFStructureLocator.byName returned null");
        if (expected == null) {
            return;
        }
        bot.status("08 structure " + name);
        double[] before = bot.server(() -> new double[] {sp().posX, sp().posY, sp().posZ, sp().dimension});
        String tpResult = bot.server(() -> {
            try {
                int r = srv().getCommandManager().executeCommand(sp(), "tf tp " + name, true);
                return "executeCommand -> " + r;
            } catch (Throwable t) {
                return "command threw " + t;
            }
        });
        bot.serverTicks(2);
        double[] after = bot.server(() -> new double[] {sp().posX, sp().posY, sp().posZ, sp().dimension});
        s.info("teleport", tpResult + "; from " + fmt(before) + " to " + fmt(after));
        if ((int) after[3] != TFDimension.id) {
            s.fail("teleport " + name, "player is not in the Twilight Forest dimension after /tf tp (dimension " + (int) after[3] + ")");
            return;
        }
        if (Math.abs(after[0] - before[0]) + Math.abs(after[2] - before[2]) < 1.0D && (int) before[3] == TFDimension.id) {
            s.fail("teleport " + name, "player did not move (structure not found or no safe spot)");
            return;
        }
        s.ok("teleported to " + name + " at " + fmt(after));
        prepPlayer();
        boolean there = bot.waitFor(() -> mc.theWorld != null && mc.thePlayer != null && mc.theWorld.provider.dimensionId == TFDimension.id
            && Math.abs(mc.thePlayer.posX - after[0]) < 3.0D && Math.abs(mc.thePlayer.posZ - after[2]) < 3.0D, 400);
        s.check(there, "client arrived in the TF dimension at the structure", "client world dimension/position did not follow");
        // chunk streaming + chunk mesh builds
        bot.ticks(30);
        bot.frames(40);
        final int px = (int) Math.floor(after[0]);
        final int py = (int) Math.floor(after[1]);
        final int pz = (int) Math.floor(after[2]);
        int nonAir = bot.client(() -> {
            int n = 0;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int dy = -3; dy <= 4; dy++) {
                        if (mc.theWorld.getBlockId(px + dx, py + dy, pz + dz) != 0) {
                            n++;
                        }
                    }
                }
            }
            return n;
        });
        s.check(nonAir > 0, "client chunks around the player loaded (" + nonAir + " non-air blocks in 9x8x9)", "client world around the player is empty");
        TFFeature here = bot.server(() -> {
            WorldServer w = srv().worldServerForDimension(TFDimension.id);
            return TFFeature.getNearestFeature(px >> 4, pz >> 4, w);
        });
        s.check(here == expected, "nearest TF feature at the arrival point is " + expected.name, "nearest feature is " + (here == null ? "null" : here.name));

        if (name.equals("hedge_maze")) {
            checkHedgeMaze(s, px, py, pz);
        }

        // rotate through
        for (int yaw : new int[] {0, 90, 180, 270}) {
            place(after[0], after[1], after[2], yaw, 10.0F);
            bot.ticks(4);
            bot.frames(6);
            bot.screenshot(s, "structures/" + name + "_yaw" + yaw + ".png");
        }
        // walk forward (real key press, flying)
        place(after[0], after[1], after[2], 0.0F, 5.0F);
        bot.client(() -> mc.gameSettings.keyBindForward.pressed = true);
        bot.ticks(25);
        bot.frames(2);
        bot.screenshot(s, "structures/" + name + "_walk.png");
        bot.client(() -> mc.gameSettings.keyBindForward.pressed = false);
        bot.ticks(4);
        double[] moved = bot.client(() -> new double[] {mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ});
        s.check(Math.abs(moved[0] - after[0]) + Math.abs(moved[2] - after[2]) > 2.0D, "walked forward in " + name + " (now " + fmt(moved) + ")", "player could not move forward (stuck inside blocks?)");
    }

    static String fmt(double[] p) {
        return String.format("%.1f,%.1f,%.1f", p[0], p[1], p[2]);
    }

    /** Hedge maze: hedge blocks must exist at the maze, match on the client, and have non-null icons for every metadata and side. */
    void checkHedgeMaze(Report.Section s, int px, int py, int pz) throws Exception {
        Block hedge = TFBlocks.hedge;
        s.check(hedge != null, "TFBlocks.hedge exists", "null");
        if (hedge == null) {
            return;
        }
        for (int meta = 0; meta < 4; meta++) {
            for (int side = 0; side < 6; side++) {
                Icon ic = hedge.getIcon(side, meta);
                if (badIcon(ic)) {
                    s.fail("hedge icon side " + side + " meta " + meta, ic == null ? "null" : ic.getIconName());
                }
            }
        }
        s.ok("hedge getIcon(side 0-5, meta 0-3) never null/missingno (meta 0 -> " + hedge.getIcon(2, 0).getIconName() + ", meta 1 -> " + hedge.getIcon(2, 1).getIconName() + ")");
        final int range = 56;
        List<int[]> found = bot.server(() -> {
            WorldServer w = srv().worldServerForDimension(TFDimension.id);
            List<int[]> out = new ArrayList<>();
            for (int x = px - range; x <= px + range; x++) {
                for (int z = pz - range; z <= pz + range; z++) {
                    for (int y = py - 8; y <= py + 10; y++) {
                        if (w.getBlockId(x, y, z) == hedge.blockID) {
                            out.add(new int[] {x, y, z, w.getBlockMetadata(x, y, z)});
                        }
                    }
                }
            }
            return out;
        });
        s.check(found.size() > 0, "hedge blocks exist in the generated maze (" + found.size() + " within " + range + " blocks)", "no TFBlocks.hedge block within " + range + " blocks of the maze arrival point");
        if (found.isEmpty()) {
            return;
        }
        bot.ticks(20);
        int[] clientBad = bot.client(() -> {
            int ok = 0;
            int air = 0;
            int other = 0;
            for (int[] p : found) {
                if (!mc.theWorld.getChunkProvider().chunkExists(p[0] >> 4, p[2] >> 4)) {
                    continue;
                }
                int id = mc.theWorld.getBlockId(p[0], p[1], p[2]);
                if (id == hedge.blockID) {
                    ok++;
                } else if (id == 0) {
                    air++;
                } else {
                    other++;
                }
            }
            return new int[] {ok, air, other};
        });
        s.check(clientBad[1] == 0 && clientBad[2] == 0 && clientBad[0] > 0,
            "client world shows the same hedge blocks (" + clientBad[0] + " ok, " + clientBad[1] + " air, " + clientBad[2] + " other, among loaded chunks)",
            "client mismatch: " + clientBad[0] + " ok, " + clientBad[1] + " air, " + clientBad[2] + " other");
        Map<Integer, Integer> metas = new LinkedHashMap<>();
        for (int[] p : found) {
            metas.merge(p[3], 1, Integer::sum);
        }
        s.info("hedge metadata histogram", metas.toString());
        // close-up: nearest hedge block, camera on a free cell 3 blocks away
        int[] nearest = null;
        double best = 1e18;
        for (int[] p : found) {
            double d = (p[0] - px) * (p[0] - px) + (p[2] - pz) * (p[2] - pz) + (p[1] - py) * (p[1] - py) * 4.0D;
            if (d < best) {
                best = d;
                nearest = p;
            }
        }
        final int[] hp = nearest;
        double[] cam = bot.server(() -> {
            WorldServer w = srv().worldServerForDimension(TFDimension.id);
            int[][] dirs = {{-3, 0}, {3, 0}, {0, -3}, {0, 3}, {-2, 0}, {2, 0}, {0, -2}, {0, 2}};
            for (int[] d : dirs) {
                int x = hp[0] + d[0];
                int z = hp[2] + d[1];
                if (w.getBlockId(x, hp[1], z) == 0 && w.getBlockId(x, hp[1] + 1, z) == 0) {
                    return new double[] {x + 0.5D, hp[1], z + 0.5D};
                }
            }
            return null;
        });
        if (cam != null) {
            camera(cam[0], cam[1] + 1.62D, cam[2], hp[0] + 0.5D, hp[1] + 0.5D, hp[2] + 0.5D);
            bot.ticks(6);
            bot.frames(5);
            bot.screenshot(s, "structures/hedge_maze_hedge_closeup.png");
        } else {
            s.warn("hedge close-up", "no free air cell next to the nearest hedge block");
        }
    }

    // ================================================================== 09 labyrinth trap

    /** Block ids/metas of the trap parts (hooks, strings, TNT) inside the 6x6 cell at ox,y,oz (y = cell floor). */
    static String trapState(World w, int ox, int y, int oz) {
        StringBuilder b = new StringBuilder();
        for (int x = ox; x <= ox + 5; x++) {
            for (int z = oz; z <= oz + 5; z++) {
                for (int yy = y - 1; yy <= y + 1; yy++) {
                    int id = w.getBlockId(x, yy, z);
                    String n = id == Block.tripWireSource.blockID ? "hook" : id == Block.tripWire.blockID ? "string" : id == Block.tnt.blockID ? "tnt" : null;
                    if (n != null) {
                        b.append(n).append('(').append(x - ox).append(',').append(yy - y).append(',').append(z - oz).append(" m").append(w.getBlockMetadata(x, yy, z)).append(") ");
                    }
                }
            }
        }
        return b.toString().trim();
    }

    static int countTnt(World w, int ox, int y, int oz) {
        int n = 0;
        for (int x = ox - 1; x <= ox + 6; x++) {
            for (int z = oz - 1; z <= oz + 6; z++) {
                for (int yy = y - 2; yy <= y + 2; yy++) {
                    if (w.getBlockId(x, yy, z) == Block.tnt.blockID) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    /**
     * Builds a walled 6x6 maze cell in the air for each rotation, generates ComponentTFMazeDeadEndTrappedChest into it, then stands the
     * player on a string. Records the trap blocks before / after; if the walk-in did not fire, calls the string's collision handler directly
     * to tell "no collision" from "the hook -> TNT chain does not work".
     */
    void stepMazeTrap(Report.Section s) throws Exception {
        prepPlayer();
        for (int rot = 0; rot < 4; rot++) {
            final int r = rot;
            final int ox = 40 + rot * 40;
            final int y = GROUND_Y + 30;
            final int oz = 40;
            int[] str = bot.server(() -> {
                WorldServer w = swOf(sp());
                for (int x = ox - 2; x <= ox + 7; x++) {
                    for (int z = oz - 2; z <= oz + 7; z++) {
                        for (int yy = y - 3; yy <= y + 6; yy++) {
                            boolean edge = x == ox || x == ox + 5 || z == oz || z == oz + 5;
                            boolean inCell = x >= ox && x <= ox + 5 && z >= oz && z <= oz + 5;
                            int id = 0;
                            if (inCell && (yy == y || yy == y - 1 || (edge && yy >= y + 1 && yy <= y + 4) || yy == y + 5)) {
                                id = TFBlocks.mazestone.blockID;
                            }
                            w.setBlock(x, yy, z, id, 0, 2);
                        }
                    }
                }
                twilightforest.structures.minotaurmaze.ComponentTFMazeDeadEndTrappedChest c =
                    new twilightforest.structures.minotaurmaze.ComponentTFMazeDeadEndTrappedChest(0, ox, y, oz, r);
                c.addComponentParts(w, new java.util.Random(1), new net.minecraft.StructureBoundingBox(ox - 10, y - 10, oz - 10, ox + 20, y + 20, oz + 20));
                for (int x = ox; x <= ox + 5; x++) {
                    for (int z = oz; z <= oz + 5; z++) {
                        if (w.getBlockId(x, y + 1, z) == Block.tripWire.blockID) {
                            return new int[] {x, y + 1, z};
                        }
                    }
                }
                return null;
            });
            String gen = bot.server(() -> trapState(swOf(sp()), ox, y + 1, oz));
            s.info("rot " + r + " generated", gen);
            if (str == null) {
                s.fail("rot " + r, "no string placed");
                continue;
            }
            int tntBefore = bot.server(() -> countTnt(swOf(sp()), ox, y, oz));
            place(str[0] + 0.5D, str[1], str[2] + 0.5D, 0.0F, 30.0F);
            bot.serverTicks(10);
            String walked = bot.server(() -> trapState(swOf(sp()), ox, y + 1, oz));
            int tntWalk = bot.server(() -> countTnt(swOf(sp()), ox, y, oz));
            int primed = bot.server(() -> swOf(sp()).getEntitiesWithinAABB(net.minecraft.EntityTNTPrimed.class,
                net.minecraft.AxisAlignedBB.getBoundingBox(ox - 4, y - 4, oz - 4, ox + 10, y + 8, oz + 10)).size());
            double[] pp = bot.server(() -> new double[] {sp().posX, sp().boundingBox.minY, sp().posZ});
            s.info("rot " + r + " after walking onto string at " + str[0] + "," + str[1] + "," + str[2] + " (player " + fmt(pp) + ")",
                walked + " | tnt blocks " + tntBefore + " -> " + tntWalk + ", primed " + primed);
            if (tntWalk < tntBefore || primed > 0) {
                s.ok("rot " + r + ": walking onto the string ignites the TNT");
            } else {
                s.fail("rot " + r + " walk", "TNT not ignited by walking onto the string");
                bot.server(() -> {
                    Block.tripWire.onEntityCollidedWithBlock(swOf(sp()), str[0], str[1], str[2], sp());
                    return null;
                });
                bot.serverTicks(10);
                String direct = bot.server(() -> trapState(swOf(sp()), ox, y + 1, oz));
                int tntDirect = bot.server(() -> countTnt(swOf(sp()), ox, y, oz));
                s.info("rot " + r + " after direct onEntityCollidedWithBlock", direct + " | tnt blocks " + tntDirect);
                s.check(tntDirect < tntBefore, "rot " + r + ": direct collision call ignites the TNT (so the walk-in collision is the problem)",
                    "rot " + r + ": even the direct collision call does not ignite the TNT (hook/TNT chain broken)");
            }
            // let the fuse burn out before the next cell
            place(0.5D, FEET_Y, 0.5D, 0.0F, 20.0F);
            bot.serverTicks(100);
        }
    }

    // ================================================================== 11 minoshroom

    /** The labyrinth boss room nearest to x,z in the TF dimension (searches the loaded structure starts), or null. */
    static twilightforest.structures.minotaurmaze.ComponentTFMazeRoomBoss findBossRoom(WorldServer w, int x, int z) throws Exception {
        Object gen = field(((twilightforest.world.WorldProviderTwilightForest) w.provider).getChunkProvider(),
            twilightforest.world.ChunkProviderTwilightForest.class, "majorFeatureGenerator");
        Map<?, ?> starts = (Map<?, ?>) field(gen, net.minecraft.MapGenStructure.class, "structureMap");
        twilightforest.structures.minotaurmaze.ComponentTFMazeRoomBoss best = null;
        double bestD = Double.MAX_VALUE;
        for (Object o : starts.values()) {
            for (Object c : ((net.minecraft.StructureStart) o).getComponents()) {
                if (c instanceof twilightforest.structures.minotaurmaze.ComponentTFMazeRoomBoss room) {
                    double dx = room.getBoundingBox().getCenterX() - x;
                    double dz = room.getBoundingBox().getCenterZ() - z;
                    if (dx * dx + dz * dz < bestD) {
                        bestD = dx * dx + dz * dz;
                        best = room;
                    }
                }
            }
        }
        return best;
    }

    static int countMinoshrooms(WorldServer w, net.minecraft.StructureBoundingBox bb) {
        return w.getEntitiesWithinAABB(twilightforest.entity.boss.EntityTFMinoshroom.class, net.minecraft.AxisAlignedBB.getBoundingBox(
            bb.minX - 8, bb.minY - 4, bb.minZ - 8, bb.maxX + 9, bb.maxY + 5, bb.maxZ + 9)).size();
    }

    /**
     * /tf tp labyrinth, then into the boss room: the room's boss spawner (center, y+1) must turn into one Minoshroom. Then the
     * Minoshroom is removed (an old room without boss): standing in the room must put the spawner back and spawn a new one.
     */
    void stepMinoshroom(Report.Section s) throws Exception {
        String tp = bot.server(() -> "executeCommand -> " + srv().getCommandManager().executeCommand(sp(), "tf tp labyrinth", true));
        bot.serverTicks(2);
        prepPlayer();
        s.info("teleport", tp);
        int[] room = bot.server(() -> {
            WorldServer w = swOf(sp());
            if (w.provider.dimensionId != TFDimension.id) {
                return null;
            }
            twilightforest.structures.minotaurmaze.ComponentTFMazeRoomBoss r = findBossRoom(w, (int) sp().posX, (int) sp().posZ);
            if (r == null) {
                return null;
            }
            net.minecraft.StructureBoundingBox bb = r.getBoundingBox();
            return new int[] {bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ};
        });
        if (room == null) {
            s.fail("boss room", "no ComponentTFMazeRoomBoss in the structure starts near the labyrinth");
            return;
        }
        // room center x/z (the spawner sits at local 7,1,7 of a 16x16 room); stand 4 blocks off center
        final int cx = (room[0] + room[3]) / 2;
        final int cz = (room[2] + room[5]) / 2;
        final int fy = room[1] + 1;
        s.info("boss room", "bounding box " + java.util.Arrays.toString(room));
        place(cx + 4.5D, fy, cz + 0.5D, 90.0F, 10.0F);
        bot.serverTicks(60);
        int[] first = bot.server(() -> {
            WorldServer w = swOf(sp());
            net.minecraft.StructureBoundingBox bb = new net.minecraft.StructureBoundingBox(room[0], room[1], room[2], room[3], room[4], room[5]);
            int spawners = 0;
            for (int x = room[0]; x <= room[3]; x++) {
                for (int z = room[2]; z <= room[5]; z++) {
                    if (w.getBlockId(x, fy, z) == TFBlocks.bossSpawner.blockID) {
                        spawners++;
                    }
                }
            }
            return new int[] {countMinoshrooms(w, bb), spawners};
        });
        s.check(first[0] == 1, "boss room has exactly one Minoshroom with the player inside (" + first[0] + ", spawners left " + first[1] + ")",
            first[0] + " Minoshrooms in the room, " + first[1] + " spawner blocks left");
        bot.screenshot(s, "minoshroom_room.png");
        // simulate an old room whose boss went missing: remove the boss and any spawner, the restore check runs every 40 ticks
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (Object o : w.loadedEntityList) {
                if (o instanceof twilightforest.entity.boss.EntityTFMinoshroom m) {
                    m.setDead();
                }
            }
            for (int x = room[0]; x <= room[3]; x++) {
                for (int z = room[2]; z <= room[5]; z++) {
                    if (w.getBlockId(x, fy, z) == TFBlocks.bossSpawner.blockID) {
                        w.setBlockToAir(x, fy, z);
                    }
                }
            }
        });
        // the restore only happens for players without the MINO flag; creative players count as having every flag
        bot.server(() -> sp().setGameType(EnumGameType.SURVIVAL));
        prepPlayer();
        bot.serverTicks(5);
        s.info("player", bot.server(() -> "creative=" + sp().capabilities.isCreativeMode + ", has MINO="
            + twilightforest.TFProgress.has(sp(), twilightforest.TFProgress.Flag.MINO) + ", room at feet="
            + ((twilightforest.world.WorldProviderTwilightForest) swOf(sp()).provider).getChunkProvider().getComponentAt((int) Math.floor(sp().posX),
                (int) Math.floor(sp().posY), (int) Math.floor(sp().posZ), twilightforest.structures.minotaurmaze.ComponentTFMazeRoomBoss.class)
            + ", feet " + fmt(new double[] {sp().posX, sp().posY, sp().posZ})));
        int gone = bot.server(() -> countMinoshrooms(swOf(sp()), new net.minecraft.StructureBoundingBox(room[0], room[1], room[2], room[3], room[4], room[5])));
        s.check(gone == 0, "Minoshroom removed for the restore test", gone + " Minoshrooms still there");
        bot.serverTicks(100);
        int again = bot.server(() -> countMinoshrooms(swOf(sp()), new net.minecraft.StructureBoundingBox(room[0], room[1], room[2], room[3], room[4], room[5])));
        s.check(again == 1, "empty boss room restored its spawner and spawned one Minoshroom", again + " Minoshrooms after the restore window");
        bot.server(() -> sp().setGameType(EnumGameType.CREATIVE));
        prepPlayer();
        place(0.5D, FEET_Y, 0.5D, 0.0F, 20.0F);
    }

    // ================================================================== 11 spawner light

    /**
     * MITE lets spawner mobs ignore light. TF mobs that override isValidLightLevel must keep that: getCanSpawnHere(true) is sampled
     * 200 times at block light 15 (glowstone ring) with came_from_spawner on and off, then a real skeleton druid spawner is run there.
     */
    void stepSpawnerLight(Report.Section s) throws Exception {
        prepPlayer();
        final int bx = -40;
        final int by = GROUND_Y + 1;
        final int bz = 40;
        bot.server(() -> {
            WorldServer w = swOf(sp());
            for (int x = bx - 4; x <= bx + 4; x++) {
                for (int z = bz - 4; z <= bz + 4; z++) {
                    w.setBlock(x, GROUND_Y, z, Block.stone.blockID, 0, 2);
                    for (int y = by; y <= by + 4; y++) {
                        boolean ring = Math.abs(x - bx) == 4 || Math.abs(z - bz) == 4;
                        w.setBlock(x, y, z, ring ? Block.glowStone.blockID : 0, 0, 2);
                    }
                    w.setBlock(x, by + 5, z, Block.glowStone.blockID, 0, 2);
                }
            }
            return null;
        });
        bot.serverTicks(5);
        int light = bot.server(() -> swOf(sp()).getBlockLightValue(bx + 1, by, bz + 1));
        s.info("block light at the test spot", String.valueOf(light));
        Class<?>[] mobs = {twilightforest.entity.EntityTFSkeletonDruid.class, twilightforest.entity.EntityTFWraith.class, twilightforest.entity.EntityTFMiniGhast.class,
            twilightforest.entity.EntityTFSwarmSpider.class, twilightforest.entity.EntityTFHedgeSpider.class};
        for (Class<?> c : mobs) {
            int[] ok = bot.server(() -> {
                WorldServer w = swOf(sp());
                int[] r = new int[2];
                for (int flag = 0; flag < 2; flag++) {
                    for (int i = 0; i < 200; i++) {
                        net.minecraft.EntityLiving e = (net.minecraft.EntityLiving) c.getConstructor(World.class).newInstance(w);
                        e.setLocationAndAngles(bx + 1.5D, by, bz + 1.5D, 0.0F, 0.0F);
                        setField(e, net.minecraft.EntityLiving.class, "came_from_spawner", flag == 1);
                        if (e.getCanSpawnHere(true)) {
                            r[flag]++;
                        }
                    }
                }
                return r;
            });
            s.info(c.getSimpleName(), "getCanSpawnHere(true) at light " + light + ": natural " + ok[0] + "/200, from spawner " + ok[1] + "/200");
            s.check(ok[1] > 100, c.getSimpleName() + " from a spawner may spawn in bright light (" + ok[1] + "/200)",
                c.getSimpleName() + " from a spawner is refused in bright light (" + ok[1] + "/200)");
        }
        String druidName = twilightforest.entity.TFCreatures.getSpawnerNameFor(twilightforest.entity.EntityTFSkeletonDruid.class);
        bot.server(() -> {
            WorldServer w = swOf(sp());
            w.setBlock(bx, by, bz, Block.mobSpawner.blockID, 0, 2);
            net.minecraft.TileEntityMobSpawner te = (net.minecraft.TileEntityMobSpawner) w.getBlockTileEntity(bx, by, bz);
            te.getSpawnerLogic().setMobID(druidName);
            te.getSpawnerLogic().spawnDelay = 0;
            return null;
        });
        place(bx + 2.5D, by, bz + 2.5D, 0.0F, 30.0F);
        bot.serverTicks(60);
        int druids = bot.server(() -> swOf(sp()).getEntitiesWithinAABB(twilightforest.entity.EntityTFSkeletonDruid.class,
            net.minecraft.AxisAlignedBB.getBoundingBox(bx - 8, by - 4, bz - 8, bx + 8, by + 8, bz + 8)).size());
        s.check(druids > 0, "real skeleton druid spawner at light " + light + " spawned " + druids + " druid(s) within 60 ticks",
            "real skeleton druid spawner at light " + light + " spawned nothing within 60 ticks");
    }
}
