package twilightforest.testbot;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.Block;
import net.minecraft.BlockWorkbench;
import net.minecraft.ContainerWorkbench;
import net.minecraft.CraftingManager;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityItem;
import net.minecraft.EntityLivingBase;
import net.minecraft.EnumGameType;
import net.minecraft.FurnaceRecipes;
import net.minecraft.IRecipe;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.ServerPlayer;
import net.minecraft.ShapedRecipes;
import net.minecraft.SlotCrafting;
import net.minecraft.TileEntityChest;
import net.minecraft.TileEntityFurnace;
import net.minecraft.WorldServer;
import twilightforest.TFConfig;
import twilightforest.TFIds;
import twilightforest.TFProgress;
import twilightforest.TFProgressData;
import twilightforest.TFTreasure;
import twilightforest.block.TFBlocks;
import twilightforest.entity.TFCreatures;
import twilightforest.inventory.ContainerTFUncrafting;
import twilightforest.item.TFItems;

/**
 * Acquisition tests: for every TF item / block subtype, every intended way to get it is exercised server side through the integrated server
 * (real crafting container on the right bench tier, real furnace tile entity, real survival block harvest, real kills with player damage, real
 * TFTreasure chest generation, special interactions) and recorded in an {@link AcqBook}. {@link #stepMatrix} turns the book into the per item
 * report and the red list (items without any working source).
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class TestBotAcquisition {
    private static final int GROUND = TestBotScript.GROUND_Y;
    private static final int FEET = TestBotScript.FEET_Y;

    final TestBotScript t;
    final TestBot bot;
    final AcqBook book = new AcqBook();

    /** every TF item/block subtype (the universe the matrix is judged against), filled by stepCraft */
    List<ItemStack> universe;
    /** registry field names, id -> "TFItems.x" / "TFBlocks.x" */
    final Map<Integer, String> fieldNames = new LinkedHashMap<>();
    private final Map<String, String> sourceNotes = new LinkedHashMap<>();
    private boolean wasCreative;

    TestBotAcquisition(TestBotScript t) {
        this.t = t;
        this.bot = t.bot;
        collectFieldNames();
    }

    // ================================================================== helpers

    private void collectFieldNames() {
        for (Class<?> c : new Class<?>[] {TFItems.class, TFBlocks.class}) {
            for (Field f : c.getFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                try {
                    Object v = f.get(null);
                    if (v instanceof Item it) {
                        fieldNames.putIfAbsent(it.itemID, "TFItems." + f.getName());
                    } else if (v instanceof Block b) {
                        fieldNames.put(b.blockID, "TFBlocks." + f.getName());
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    String nameOf(int id) {
        String n = fieldNames.get(id);
        if (n != null) {
            return n;
        }
        Item it = id >= 0 && id < Item.itemsList.length ? Item.itemsList[id] : null;
        return it == null ? "#" + id : it.getClass().getSimpleName() + "#" + id;
    }

    String label(ItemStack st) {
        int sub = st.getHasSubtypes() ? st.getItemSubtype() : 0;
        return nameOf(st.itemID) + (st.getHasSubtypes() ? "/" + sub : "");
    }

    boolean isTfStack(ItemStack st) {
        return st != null && st.getItem() != null && t.isTfItem(st.getItem());
    }

    /** Survival is required for real harvest/credit logic. (Outside MITE dev mode the "creative" world is survival anyway.) */
    void enterSurvival() throws Exception {
        bot.server(() -> {
            ServerPlayer p = t.sp();
            wasCreative = p.capabilities.isCreativeMode;
            if (wasCreative) {
                p.theItemInWorldManager.setGameType(EnumGameType.SURVIVAL);
            }
            p.capabilities.disableDamage = true; // keep the bot alive while it stands among bosses
            p.setHealth(p.getMaxHealth());
            p.sendPlayerAbilities();
        });
    }

    void leaveSurvival() throws Exception {
        bot.server(() -> {
            ServerPlayer p = t.sp();
            if (wasCreative) {
                p.theItemInWorldManager.setGameType(EnumGameType.CREATIVE);
            }
            clearPlayerItems(p);
        });
        t.prepPlayer();
    }

    void clearPlayerItems(ServerPlayer p) {
        for (int i = 0; i < p.inventory.mainInventory.length; i++) {
            p.inventory.mainInventory[i] = null;
        }
        for (int i = 0; i < p.inventory.armorInventory.length; i++) {
            p.inventory.armorInventory[i] = null;
        }
    }

    Set<Integer> itemEntityIds(WorldServer w) {
        Set<Integer> ids = new HashSet<>();
        for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
            if (o instanceof EntityItem e) {
                ids.add(e.entityId);
            }
        }
        return ids;
    }

    /** Collects (and removes) the item entities that appeared since {@code before}. */
    List<ItemStack> sweepItems(WorldServer w, Set<Integer> before) {
        List<ItemStack> out = new ArrayList<>();
        for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
            if (o instanceof EntityItem e && !e.isDead && !before.contains(e.entityId)) {
                ItemStack st = e.getEntityItem();
                if (st != null && st.getItem() != null) {
                    out.add(st.copy());
                }
                e.setDead();
            }
        }
        return out;
    }

    void killNonPlayerEntities(WorldServer w) {
        for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
            if (!(o instanceof net.minecraft.EntityPlayer)) {
                ((Entity) o).setDead();
            }
        }
    }

    void clearBox(WorldServer w, int x0, int y0, int z0, int x1, int y1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (w.getBlockId(x, y, z) != 0) {
                        w.setBlockToAir(x, y, z, 2);
                    }
                }
            }
        }
    }

    static String list(List<ItemStack> stacks, TestBotAcquisition self) {
        Map<String, Integer> counts = new TreeMap<>();
        for (ItemStack st : stacks) {
            counts.merge(self.label(st), st.stackSize, Integer::sum);
        }
        return counts.toString();
    }

    static float materialDurability(Material m) {
        try {
            Field f = Material.class.getDeclaredField("durability");
            f.setAccessible(true);
            return f.getFloat(m);
        } catch (Throwable e) {
            return 0.0F;
        }
    }

    // ================================================================== a) crafting

    void stepCraft(Report.Section s) throws Exception {
        universe = t.allStacks(s);
        s.info("universe", universe.size() + " TF item/block subtypes (item stacks; MITE subtype is the identity)");
        List<IRecipe> recipes = bot.server(() -> new ArrayList<IRecipe>((List<IRecipe>) CraftingManager.getInstance().getRecipeList()));
        int late = 0;
        for (IRecipe r : recipes) {
            if (r instanceof twilightforest.item.TFRecipes.TFRechargeRecipe || r instanceof twilightforest.item.TFMapCloningRecipe) {
                late++;
            }
        }
        s.check(late >= 7, "late recipes registered (3 map cloning + 4 recharge)", "only " + late + " of 7 custom TF recipes are in the CraftingManager list");
        enterSurvival();
        int tested = 0;
        try {
            for (IRecipe r : recipes) {
                ItemStack out;
                try {
                    out = r.getRecipeOutput();
                } catch (Throwable e) {
                    continue;
                }
                if (!isTfStack(out)) {
                    continue;
                }
                tested++;
                String name = label(out) + " x" + out.stackSize + " (" + r.getClass().getSimpleName() + ")";
                try {
                    String[] res = bot.server(() -> craftOne(r, out));
                    // res = {error or null, outputKey, detail}
                    if (res[0] == null) {
                        s.ok("craft " + name);
                        book.add(res[1], "craft", res[2], true);
                    } else {
                        s.fail("craft " + name, res[0] + " [" + res[2] + "]");
                        book.add(AcqBook.key(out), "craft", res[2] + " : " + res[0], false);
                    }
                } catch (Throwable e) {
                    s.exception("craft " + name, e);
                    book.add(AcqBook.key(out), "craft", "exception " + e, false);
                }
            }
        } finally {
            leaveSurvival();
        }
        s.info("recipes", tested + " recipes with a TF output exercised through ContainerWorkbench");
    }

    /** {metadata, sufficient (1/0), required durability x100}: the lowest bench tier whose tool material is at least as durable as the recipe's hardest metal (MITE's own rule). */
    private int[] benchMeta(IRecipe r, ItemStack out) {
        Material need = null;
        try {
            need = r.getMaterialToCheckToolBenchHardnessAgainst();
        } catch (Throwable ignored) {
        }
        if (need == null) {
            try {
                need = out.getItem().getHardestMetalMaterial();
            } catch (Throwable ignored) {
            }
        }
        int idx = 0;
        int sufficient = 1;
        float d = 0.0F;
        if (need != null) {
            idx = -1;
            d = materialDurability(need);
            for (int i = 0; i < 8; i++) {
                if (materialDurability(BlockWorkbench.tool_materials[i]) >= d) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0) {
                idx = 7;
                sufficient = 0;
            }
        }
        return new int[] {idx == 0 ? 0 : idx + 3, sufficient, Math.round(d * 100.0F)};
    }

    private int[] shapedSize(ShapedRecipes r) {
        try {
            Field w = ShapedRecipes.class.getDeclaredField("recipeWidth");
            Field h = ShapedRecipes.class.getDeclaredField("recipeHeight");
            w.setAccessible(true);
            h.setAccessible(true);
            return new int[] {w.getInt(r), h.getInt(r)};
        } catch (Throwable e) {
            int size = r.getRecipeSize();
            int width = size >= 9 ? 3 : size == 6 || size == 4 ? 2 : 1;
            return new int[] {width, Math.max(1, size / width)};
        }
    }

    /** Runs on the server thread. Returns {error|null, key of the crafted stack, detail}. */
    private String[] craftOne(IRecipe r, ItemStack out) throws Exception {
        ServerPlayer p = t.sp();
        WorldServer w = t.swOf(p);
        int bx = -10;
        int by = FEET;
        int bz = -10;
        int[] bench = benchMeta(r, out);
        int meta = bench[0];
        w.setBlock(bx, by, bz, Block.workbench.blockID, meta, 2);
        ContainerWorkbench c = new ContainerWorkbench(p, bx, by, bz);
        ItemStack[] comps = r.getComponents();
        StringBuilder detail = new StringBuilder("bench meta " + meta + " (" + BlockWorkbench.getToolMaterial(meta).getCapitalizedName() + " tools"
            + (bench[2] > 0 ? ", recipe metal durability " + bench[2] / 100.0F : "") + "): ");
        try {
            if (bench[1] == 0) {
                return new String[] {"no workbench tier is durable enough for this recipe (MITE shows 'needs better tools'): unobtainable by crafting", AcqBook.key(out), detail.toString()};
            }
            if (comps == null) {
                return new String[] {"recipe exposes no components, cannot build a grid", AcqBook.key(out), detail.toString()};
            }
            int[] wh = r instanceof ShapedRecipes sr ? shapedSize(sr) : null;
            int seq = 0;
            for (int i = 0; i < comps.length; i++) {
                ItemStack comp = comps[i];
                if (comp == null) {
                    continue;
                }
                ItemStack st = comp.copy();
                st.stackSize = 1;
                if (st.getItemSubtype() == 32767) {
                    st.setItemSubtype(0);
                }
                // used-up tools of the recharge recipes
                if (r instanceof twilightforest.item.TFRecipes.TFRechargeRecipe && i == 0 && st.isItemStackDamageable()) {
                    st.setItemDamage(st.getMaxDamage());
                }
                int slot;
                if (wh != null) {
                    slot = (i % wh[0]) + (i / wh[0]) * 3;
                } else {
                    slot = seq++;
                }
                if (slot > 8) {
                    return new String[] {"component " + i + " does not fit the 3x3 grid", AcqBook.key(out), detail.toString()};
                }
                c.craft_matrix.setInventorySlotContents(slot, st);
                detail.append(label(st)).append(' ');
            }
            c.onCraftMatrixChanged(c.craft_matrix);
            ItemStack res = c.getSlot(0).getStack();
            if (res == null) {
                return new String[] {"the grid built from the recipe components crafts nothing (no match / forbidden / shadowed)", AcqBook.key(out), detail.toString()};
            }
            String key = AcqBook.key(res);
            int expectSub = out.getHasSubtypes() ? out.getItemSubtype() : 0;
            int gotSub = res.getHasSubtypes() ? res.getItemSubtype() : 0;
            if (res.itemID != out.itemID || gotSub != expectSub) {
                return new String[] {"crafted " + label(res) + " instead of " + label(out) + " (an earlier recipe shadows this one)", AcqBook.key(out), detail.toString()};
            }
            if (res.stackSize != out.stackSize) {
                return new String[] {"stack size " + res.stackSize + " instead of " + out.stackSize, key, detail.toString()};
            }
            SlotCrafting slot0 = (SlotCrafting) c.getSlot(0);
            boolean can = slot0.canPlayerCraftItem(p);
            // really take the result: consumes the grid and puts the item into the inventory
            slot0.onPickupFromSlot(p, res);
            boolean got = false;
            for (ItemStack inv : p.inventory.mainInventory) {
                if (inv != null && inv.itemID == out.itemID) {
                    got = true;
                    break;
                }
            }
            if (!got) {
                return new String[] {"result was not delivered to the inventory by SlotCrafting.onPickupFromSlot", key, detail.toString()};
            }
            if (!can) {
                detail.append("(canPlayerCraftItem=false: skills/quality gate in this world; item delivered by the slot anyway) ");
            }
            return new String[] {null, key, detail.toString()};
        } finally {
            for (int i = 0; i < 9; i++) {
                c.craft_matrix.setInventorySlotContents(i, null);
            }
            c.craft_result.setInventorySlotContents(0, null);
            clearPlayerItems(p);
            w.setBlockToAir(bx, by, bz, 2);
        }
    }

    // ================================================================== b) smelting

    void stepSmelt(Report.Section s) throws Exception {
        Map<Integer, ItemStack> entries = bot.server(() -> new LinkedHashMap<Integer, ItemStack>((Map<Integer, ItemStack>) FurnaceRecipes.smelting().getSmeltingList()));
        int tested = 0;
        for (Map.Entry<Integer, ItemStack> e : entries.entrySet()) {
            int inId = e.getKey();
            ItemStack out = e.getValue();
            Item in = inId >= 0 && inId < Item.itemsList.length ? Item.itemsList[inId] : null;
            if (in == null || out == null || out.getItem() == null) {
                continue;
            }
            if (!t.isTfItem(in) && !isTfStack(out)) {
                continue;
            }
            tested++;
            String name = nameOf(inId) + " -> " + label(out) + " x" + out.stackSize;
            try {
                String[] res = bot.server(() -> smeltOne(inId, out));
                if (res[0] == null) {
                    s.ok("smelt " + name);
                    book.add(AcqBook.key(out), "smelt", res[1], true);
                } else {
                    s.fail("smelt " + name, res[0]);
                    book.add(AcqBook.key(out), "smelt", name + " : " + res[0], false);
                }
            } catch (Throwable ex) {
                s.exception("smelt " + name, ex);
                book.add(AcqBook.key(out), "smelt", name + " exception " + ex, false);
            }
        }
        s.info("recipes", tested + " furnace recipes with a TF input or output exercised in a netherrack furnace (max heat 4)");
    }

    private String[] smeltOne(int inId, ItemStack expected) {
        ServerPlayer p = t.sp();
        WorldServer w = t.swOf(p);
        int x = -10;
        int y = FEET;
        int z = -20;
        w.setBlock(x, y, z, Block.furnaceNetherrackIdle.blockID, 2, 2);
        TileEntityFurnace te = (TileEntityFurnace) w.getBlockTileEntity(x, y, z);
        if (te == null) {
            return new String[] {"no furnace tile entity", null};
        }
        ItemStack[] fuels = {new ItemStack(Item.coal, 8, 1), new ItemStack(Item.coal, 8, 0), new ItemStack(Item.bucketIronLava, 1, 0), new ItemStack(Item.blazeRod, 8, 0)};
        String[] fuelNames = {"charcoal (heat 1)", "coal (heat 2)", "lava bucket (heat 3)", "blaze rod (heat 4)"};
        try {
            for (int f = 0; f < fuels.length; f++) {
                te.destroyInventory();
                te.furnaceBurnTime = 0;
                te.furnaceCookTime = 0;
                te.currentItemBurnTime = 0;
                te.heat_level = 0;
                te.setInventorySlotContents(0, new ItemStack(inId, 1, 0));
                te.setInventorySlotContents(1, fuels[f].copy());
                for (int i = 0; i < 700 && te.getOutputItemStack() == null; i++) {
                    te.updateEntity();
                }
                ItemStack outStack = te.getOutputItemStack();
                if (outStack != null) {
                    boolean same = outStack.itemID == expected.itemID && (!expected.getHasSubtypes() || outStack.getItemSubtype() == expected.getItemSubtype());
                    if (!same) {
                        return new String[] {"furnace produced " + label(outStack) + " instead of " + label(expected), null};
                    }
                    return new String[] {null, "furnace: " + nameOf(inId) + " with " + fuelNames[f] + " (needs a furnace of max heat >= " + (f + 1) + ")"};
                }
            }
            return new String[] {"no output with any fuel up to blaze rod (heat 4) in a netherrack furnace", null};
        } finally {
            te.destroyInventory();
            w.setBlockToAir(x, y, z, 2);
        }
    }

    // ================================================================== c) block harvest

    private List<ItemStack> harvestTools() {
        List<ItemStack> tools = new ArrayList<>();
        tools.add(null); // bare hand
        for (Item it : new Item[] {Item.axeIron, Item.shovelIron, Item.hoeIron, Item.shears, Item.pickaxeIron, Item.pickaxeAdamantium, TFItems.giantPick,
            TFItems.mazebreakerPick}) {
            if (it != null) {
                tools.add(new ItemStack(it));
            }
        }
        return tools;
    }

    static String toolName(ItemStack tool) {
        return tool == null ? "bare hand" : tool.getItem().getClass().getSimpleName() + "#" + tool.itemID;
    }

    void stepHarvest(Report.Section s) throws Exception {
        enterSurvival();
        List<ItemStack> tools = harvestTools();
        final int x = -30;
        final int y = FEET + 1;
        final int z = 30;
        bot.server(() -> {
            WorldServer w = t.swOf(t.sp());
            // a 9x9 grass patch (plants need soil) with air above
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    w.setBlock(x + dx, y - 1, z + dz, Block.grass.blockID, 0, 2);
                    for (int dy = 0; dy < 5; dy++) {
                        w.setBlockToAir(x + dx, y + dy, z + dz, 2);
                    }
                }
            }
        });
        int cases = 0;
        try {
            for (int id = TFIds.BLOCK_FIRST; id <= TFIds.BLOCK_LAST && id < Block.blocksList.length; id++) {
                Block b = Block.blocksList[id];
                if (b == null) {
                    continue;
                }
                final int bid = id;
                List<String[]> results;
                try {
                    results = bot.server(() -> harvestBlock(Block.blocksList[bid], tools, x, y, z));
                } catch (Throwable ex) {
                    s.exception("harvest " + nameOf(id), ex);
                    continue;
                }
                for (String[] r : results) {
                    // r = {state, metaLabel, detail}
                    cases++;
                    if (r[0].equals("ok")) {
                        s.ok("harvest " + nameOf(id) + "/" + r[1]);
                    } else if (r[0].equals("info")) {
                        s.info("harvest " + nameOf(id) + "/" + r[1], r[2]);
                    } else if (r[0].equals("warn")) {
                        s.warn("harvest " + nameOf(id) + "/" + r[1], r[2]);
                    } else {
                        s.fail("harvest " + nameOf(id) + "/" + r[1], r[2]);
                    }
                }
            }
        } finally {
            bot.server(() -> {
                WorldServer w = t.swOf(t.sp());
                clearBox(w, x - 4, y - 1, z - 4, x + 4, y + 5, z + 4);
                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        w.setBlock(x + dx, y - 1, z + dz, Block.stone.blockID, 0, 2);
                    }
                }
                killNonPlayerEntities(w);
            });
            leaveSurvival();
        }
        s.info("cases", cases + " (block, metadata) cases harvested in survival with up to " + tools.size() + " tools each (hand, axe, shovel, hoe, shears, iron/adamantium pickaxe, giant and mazebreaker pick)");
        // special case: giant cobblestone from cobblestone with the giant pick
        giantCobbleFromCobble(s);
    }

    /** Server thread. One block id, every valid metadata. */
    private List<String[]> harvestBlock(Block b, List<ItemStack> tools, int x, int y, int z) {
        List<String[]> out = new ArrayList<>();
        ServerPlayer p = t.sp();
        WorldServer w = t.swOf(p);
        Item blockItem = Item.itemsList[b.blockID];
        for (int meta = 0; meta < 16; meta++) {
            try {
                if (!b.isValidMetadata(meta)) {
                    continue;
                }
            } catch (Throwable ex) {
                // validity check itself broken: fall through and try the metadata
            }
            ItemStack own = null;
            try {
                if (blockItem instanceof ItemBlock) {
                    own = new ItemStack(b, 1, b.getItemSubtype(meta));
                }
            } catch (Throwable ignored) {
            }
            String metaLabel = String.valueOf(meta);
            try {
                List<ItemStack> firstDrops = null;
                String firstTool = null;
                ItemStack usedTool = null;
                String problem = null;
                Set<String> triedTools = new java.util.LinkedHashSet<>();
                for (ItemStack tool : tools) {
                    clearBox(w, x - 2, y, z - 2, x + 2, y + 3, z + 2);
                    w.setBlock(x, y, z, b.blockID, meta, 2);
                    if (w.getBlockId(x, y, z) != b.blockID) {
                        problem = "block could not be placed (got id " + w.getBlockId(x, y, z) + ")";
                        break;
                    }
                    p.inventory.mainInventory[0] = tool == null ? null : tool.copy();
                    p.inventory.currentItem = 0;
                    Set<Integer> before = itemEntityIds(w);
                    boolean harvested;
                    try {
                        harvested = p.theItemInWorldManager.tryHarvestBlock(x, y, z);
                    } catch (Throwable ex) {
                        problem = "tryHarvestBlock threw " + ex + " with " + toolName(tool);
                        sweepItems(w, before);
                        break;
                    }
                    List<ItemStack> drops = sweepItems(w, before);
                    triedTools.add(toolName(tool) + (harvested ? "" : "(refused)"));
                    if (!drops.isEmpty()) {
                        firstDrops = drops;
                        firstTool = toolName(tool);
                        usedTool = tool;
                        break;
                    }
                }
                if (firstDrops == null) {
                    if (problem != null) {
                        out.add(new String[] {"warn", metaLabel, problem});
                    } else {
                        out.add(new String[] {"info", metaLabel, "no drops with any tool (tried " + triedTools + ")"});
                    }
                    if (own != null) {
                        book.add(AcqBook.key(own), "harvest", "no drops harvesting " + nameOf(b.blockID) + "/" + meta + " with " + triedTools, false);
                    }
                    continue;
                }
                // repeat with the working tool to catch random extra drops (saplings, berries, ...)
                List<ItemStack> all = new ArrayList<>(firstDrops);
                for (int i = 0; i < 14; i++) {
                    clearBox(w, x - 2, y, z - 2, x + 2, y + 3, z + 2);
                    w.setBlock(x, y, z, b.blockID, meta, 2);
                    if (w.getBlockId(x, y, z) != b.blockID) {
                        break;
                    }
                    p.inventory.mainInventory[0] = usedTool == null ? null : new ItemStack(usedTool.getItem());
                    p.inventory.currentItem = 0;
                    Set<Integer> before = itemEntityIds(w);
                    try {
                        p.theItemInWorldManager.tryHarvestBlock(x, y, z);
                    } catch (Throwable ignored) {
                        break;
                    }
                    all.addAll(sweepItems(w, before));
                }
                String src = nameOf(b.blockID) + "/" + meta + " with " + firstTool;
                Set<String> seen = new HashSet<>();
                for (ItemStack d : all) {
                    String k = AcqBook.key(d);
                    if (seen.add(k)) {
                        book.add(k, "harvest", src, true);
                    }
                }
                boolean self = own != null && seen.contains(AcqBook.key(own));
                out.add(new String[] {"ok", metaLabel, src + " -> " + list(all, this) + (self ? "" : " (not the block itself)")});
            } catch (Throwable ex) {
                out.add(new String[] {"fail", metaLabel, "exception " + ex});
            } finally {
                clearBox(w, x - 2, y, z - 2, x + 2, y + 3, z + 2);
                killNonPlayerEntities(w);
                p.inventory.mainInventory[0] = null;
            }
        }
        return out;
    }

    /** Giant pickaxe on a full aligned 4x4x4 cobblestone cell: default config (giant.chain=2) and giant.chain=4. */
    private void giantCobbleFromCobble(Report.Section s) throws Exception {
        if (TFItems.giantPick == null || TFBlocks.giantCobble == null) {
            return;
        }
        enterSurvival();
        int saved = TFConfig.giantChain;
        try {
            for (int chain : new int[] {saved, 4}) {
                if (chain == saved && saved == 4) {
                    continue;
                }
                final int ch = chain;
                String res = bot.server(() -> {
                    ServerPlayer p = t.sp();
                    WorldServer w = t.swOf(p);
                    TFConfig.giantChain = ch;
                    int bx = 32;
                    int by = 124;
                    int bz = -36; // multiples of 4: aligned cell
                    for (int dx = 0; dx < 4; dx++) {
                        for (int dy = 0; dy < 4; dy++) {
                            for (int dz = 0; dz < 4; dz++) {
                                w.setBlock(bx + dx, by + dy, bz + dz, Block.cobblestone.blockID, 0, 2);
                            }
                        }
                    }
                    p.inventory.mainInventory[0] = new ItemStack(TFItems.giantPick);
                    p.inventory.currentItem = 0;
                    Set<Integer> before = itemEntityIds(w);
                    boolean ok = p.theItemInWorldManager.tryHarvestBlock(bx + 1, by + 1, bz + 1);
                    List<ItemStack> drops = sweepItems(w, before);
                    int left = 0;
                    for (int dx = 0; dx < 4; dx++) {
                        for (int dy = 0; dy < 4; dy++) {
                            for (int dz = 0; dz < 4; dz++) {
                                if (w.getBlockId(bx + dx, by + dy, bz + dz) != 0) {
                                    left++;
                                }
                            }
                        }
                    }
                    clearBox(w, bx, by, bz, bx + 3, by + 3, bz + 3);
                    p.inventory.mainInventory[0] = null;
                    killNonPlayerEntities(w);
                    return "harvested=" + ok + " blocksLeftOf64=" + left + " drops=" + list(drops, this);
                });
                boolean giant = res.contains("TFBlocks.giantCobble");
                String detail = "giant pick on a 4x4x4 cobblestone cell with giant.chain=" + ch + " : " + res;
                s.info("giant cobblestone from cobblestone (chain " + ch + ")", res);
                ItemStack gc = new ItemStack(TFBlocks.giantCobble);
                if (ch == 4) {
                    book.add(AcqBook.key(gc), "config", "[non-default config giant.chain=4] " + detail, giant);
                } else {
                    book.add(AcqBook.key(gc), "special", "[default config] " + detail, giant);
                }
            }
        } catch (Throwable ex) {
            s.exception("giant cobblestone from cobblestone", ex);
        } finally {
            TFConfig.giantChain = saved;
            leaveSurvival();
        }
    }

    // ================================================================== d) mob / boss drops

    private static final String[] BOSS_ORDER = {"EntityTFNaga", "EntityTFLich", "EntityTFMinoshroom", "EntityTFHydra", "EntityTFKnightPhantom", "EntityTFUrGhast",
        "EntityTFYetiAlpha", "EntityTFSnowQueen"};
    private static final Set<String> RARE_DROP_MOBS = new HashSet<>(java.util.Arrays.asList("EntityTFTroll", "EntityTFMazeSlime", "EntityTFBoggard", "EntityTFMinotaur"));
    private static final String[] BOSS_FLAG = {"NAGA", "LICH", "MINO", "HYDRA", "KNIGHTS", "URGHAST", "YETI", "QUEEN"};

    void stepMobs(Report.Section s) throws Exception {
        enterSurvival();
        try {
            t.place(0.5D, FEET, 0.5D, 0.0F, 0.0F);
            Map<String, Class<?>> bosses = new LinkedHashMap<>();
            List<Map.Entry<Class<?>, String>> normal = new ArrayList<>();
            for (Map.Entry<Class<?>, String> e : TFCreatures.registeredNames().entrySet()) {
                int bi = java.util.Arrays.asList(BOSS_ORDER).indexOf(e.getKey().getSimpleName());
                if (bi >= 0) {
                    bosses.put(e.getKey().getSimpleName(), e.getKey());
                } else {
                    normal.add(e);
                }
            }
            for (Map.Entry<Class<?>, String> e : normal) {
                String name = e.getValue();
                if (t.skip.contains(name.toLowerCase())) {
                    s.info("skipped " + name, "-Dtwilightforest.testbot.skip");
                    continue;
                }
                try {
                    List<String> drops = new ArrayList<>();
                    // rare drops (0.5% - 1%: troll beans, maze slime, boggard, minotaur) need many kills; batches keep one server task short
                    int total = RARE_DROP_MOBS.contains(e.getKey().getSimpleName()) ? 1200 : 100;
                    String err = null;
                    for (int done = 0; done < total && err == null; done += 100) {
                        err = bot.server(() -> killMany(e.getKey(), name, 100, null, drops));
                    }
                    report(s, "mob", name + " (" + total + " kills)", err, drops, null);
                } catch (Throwable ex) {
                    s.exception("mob " + name, ex);
                }
            }
            for (int i = 0; i < BOSS_ORDER.length; i++) {
                Class<?> cls = bosses.get(BOSS_ORDER[i]);
                if (cls == null) {
                    s.warn("boss " + BOSS_ORDER[i], "not registered");
                    continue;
                }
                String name = TFCreatures.registeredNames().get(cls);
                if (t.skip.contains(name.toLowerCase())) {
                    s.info("skipped " + name, "-Dtwilightforest.testbot.skip");
                    continue;
                }
                TFProgress.Flag flag = TFProgress.Flag.byName(BOSS_FLAG[i]);
                for (int pass = 0; pass < 2; pass++) {
                    boolean first = pass == 0;
                    try {
                        List<String> drops = new ArrayList<>();
                        String err = bot.server(() -> killMany(cls, name, 6, flag, drops, first));
                        report(s, "boss", name + (first ? " [first kill]" : " [repeat kill]"), err, drops, null);
                    } catch (Throwable ex) {
                        s.exception("boss " + name + (first ? " first" : " repeat"), ex);
                    }
                }
            }
        } finally {
            bot.server(() -> killNonPlayerEntities(t.swOf(t.sp())));
            leaveSurvival();
        }
    }

    private void report(Report.Section s, String type, String name, String err, List<String> drops, Object unused) {
        // drops = "key|label|count" lines
        if (err != null) {
            s.fail(type + " " + name, err);
        }
        Map<String, Integer> counts = new TreeMap<>();
        for (String d : drops) {
            String[] p = d.split("\\|");
            counts.merge(p[1], Integer.parseInt(p[2]), Integer::sum);
            book.add(p[0], type, "kill " + name, true);
        }
        if (drops.isEmpty()) {
            if (err == null) {
                s.info(type + " " + name, "no drops over all kills");
            }
        } else {
            s.ok(type + " " + name + " drops " + counts);
            s.info(type + " " + name, "drops " + counts);
        }
    }

    /**
     * User report: a boss burnt to death leaves no reward chest. Each boss stands on a stone floor 40 blocks east of the player and is
     * killed by fire damage only (no player hit, recentlyHit 0), like burning in lava or fire. Reports the chests and the dropped items
     * found around it a few seconds later.
     */
    void stepBossFireDeath(Report.Section s) throws Exception {
        for (int i = 0; i < BOSS_ORDER.length; i++) {
            final String simple = BOSS_ORDER[i];
            Class<?> cls = null;
            for (Map.Entry<Class<?>, String> e : TFCreatures.registeredNames().entrySet()) {
                if (e.getKey().getSimpleName().equals(simple)) {
                    cls = e.getKey();
                }
            }
            if (cls == null) {
                s.warn("boss " + simple, "not registered");
                continue;
            }
            final Class<?> c = cls;
            final String name = TFCreatures.registeredNames().get(cls);
            final int bx = 40, by = FEET + 10, bz = 0;
            String setup = bot.server(() -> {
                WorldServer w = t.swOf(t.sp());
                killNonPlayerEntities(w);
                clearBox(w, bx - 12, by - 2, bz - 12, bx + 12, by + 14, bz + 12);
                for (int x = bx - 12; x <= bx + 12; x++) {
                    for (int z = bz - 12; z <= bz + 12; z++) {
                        w.setBlock(x, by - 1, z, Block.stone.blockID, 0, 2);
                    }
                }
                Entity e = t.construct(w, c, name, false, null);
                if (!(e instanceof EntityLivingBase l)) {
                    return "cannot construct " + name;
                }
                e.setLocationAndAngles(bx + 0.5D, by, bz + 0.5D, 0.0F, 0.0F);
                w.spawnEntityInWorld(e);
                // fire only: the burning entity dies from DamageSource.onFire, nobody hit it
                for (int k = 0; k < 400 && !l.isDead && l.getHealth() > 0.0F; k++) {
                    l.setFire(10);
                    l.attackEntityFrom(new Damage(DamageSource.onFire, 50.0F));
                }
                return "dead=" + l.isDead + " health=" + l.getHealth() + " burning=" + l.isBurning();
            });
            s.info("boss " + simple + " burnt", setup);
            bot.serverTicks(260);
            String found = bot.server(() -> {
                WorldServer w = t.swOf(t.sp());
                int chests = 0;
                int stacks = 0;
                StringBuilder where = new StringBuilder();
                for (int x = bx - 12; x <= bx + 12; x++) {
                    for (int y = by - 30; y <= by + 14; y++) {
                        for (int z = bz - 12; z <= bz + 12; z++) {
                            if (w.getBlockTileEntity(x, y, z) instanceof TileEntityChest chest) {
                                chests++;
                                for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                                    if (chest.getStackInSlot(slot) != null) {
                                        stacks++;
                                    }
                                }
                                where.append(x).append(',').append(y).append(',').append(z).append(' ');
                            }
                        }
                    }
                }
                int items = w.getEntitiesWithinAABB(EntityItem.class, net.minecraft.AxisAlignedBB.getBoundingBox(bx - 16, by - 32, bz - 16, bx + 16, by + 20, bz + 16)).size();
                return chests + "|" + stacks + "|" + items + "|" + where;
            });
            String[] f = found.split("\\|", -1);
            s.info("boss " + simple + " after death", "chests " + f[0] + " (" + f[1] + " stacks) at " + f[3] + ", item entities on the ground " + f[2]);
            s.check(Integer.parseInt(f[0]) > 0, "boss " + simple + ": reward chest after a fire death",
                "boss " + simple + ": no reward chest after a fire death (" + f[2] + " item entities on the ground)");
            bot.server(() -> {
                killNonPlayerEntities(t.swOf(t.sp()));
                return null;
            });
        }
    }

    private String killMany(Class<?> cls, String name, int kills, TFProgress.Flag flag, List<String> dropsOut) throws Exception {
        return killMany(cls, name, kills, flag, dropsOut, false);
    }

    /** Server thread. Spawns, kills with real player damage and collects the item entities. flag != null: boss, firstKill selects the progress state. */
    private String killMany(Class<?> cls, String name, int kills, TFProgress.Flag flag, List<String> dropsOut, boolean first) throws Exception {
        ServerPlayer p = t.sp();
        WorldServer w = t.swOf(p);
        boolean savedByKill = TFConfig.progressByKill;
        String player = p.getCommandSenderName();
        Map<String, int[]> counts = new LinkedHashMap<>();
        String error = null;
        if (flag != null) {
            TFProgressData data = TFProgressData.get();
            if (first) {
                // no flag for this boss: every kill counts as a first kill (the flag is not granted while the series runs)
                TFConfig.progressByKill = false;
                if (data != null && TFProgress.has(p, flag)) {
                    data.setBits(player, data.getBits(player) & ~flag.bit());
                }
                if (TFProgress.has(p, flag)) {
                    error = "player already has the " + flag + " flag (achievement stat), cannot test a first kill";
                }
            } else {
                TFProgress.grantChain(p, flag);
                if (!TFProgress.has(p, flag)) {
                    error = "could not grant the " + flag + " flag, repeat kill not testable";
                }
            }
        }
        try {
            if (error != null) {
                return error;
            }
            p.setPositionAndRotation(0.5D, FEET, 0.5D, 0.0F, 0.0F);
            for (int k = 0; k < kills; k++) {
                Set<Integer> before = itemEntityIds(w);
                Entity e = t.construct(w, cls, name, false, null);
                if (e == null) {
                    return "cannot construct " + name;
                }
                double dist = Math.max(5.0D, 2.0D * Math.max(e.width, e.height) + 3.0D);
                e.setLocationAndAngles(0.5D, FEET, 0.5D + dist, 180.0F, 0.0F);
                w.spawnEntityInWorld(e);
                if (e instanceof EntityLivingBase l) {
                    try {
                        TestBotScript.setField(l, EntityLivingBase.class, "recentlyHit", 100);
                    } catch (Throwable ignored) {
                    }
                    Damage dmg = new Damage(DamageSource.causePlayerDamage(p), 1000.0F);
                    l.attackEntityFrom(dmg);
                    if (!l.isDead && l.getHealth() > 0.0F) {
                        l.setHealth(0.0F);
                        l.onDeath(DamageSource.causePlayerDamage(p));
                    }
                } else {
                    e.setDead();
                }
                for (ItemStack st : sweepItems(w, before)) {
                    counts.computeIfAbsent(AcqBook.key(st) + "|" + label(st), x -> new int[1])[0] += st.stackSize;
                }
                e.setDead();
                killNonPlayerEntities(w);
            }
        } catch (Throwable ex) {
            error = "exception while killing " + name + ": " + ex;
            ex.printStackTrace();
        } finally {
            TFConfig.progressByKill = savedByKill;
            killNonPlayerEntities(w);
        }
        for (Map.Entry<String, int[]> c : counts.entrySet()) {
            dropsOut.add(c.getKey() + "|" + c.getValue()[0]);
        }
        return error;
    }

    // ================================================================== e) structure loot

    void stepLoot(Report.Section s) throws Exception {
        List<Field> tables = new ArrayList<>();
        for (Field f : TFTreasure.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == TFTreasure.class) {
                tables.add(f);
            }
        }
        s.info("tables", tables.size() + " TFTreasure tables, 300 real chests each (generate()) plus 1500 raw common/uncommon/rare draws; day gates off, then default gating checked separately");
        for (Field f : tables) {
            String name = f.getName();
            try {
                Map<String, int[]> found = new TreeMap<>();
                String err = bot.server(() -> lootOne((TFTreasure) f.get(null), name, found));
                if (err != null) {
                    s.fail("loot " + name, err);
                }
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, int[]> e : found.entrySet()) {
                    String[] kl = e.getKey().split("\\|");
                    book.add(kl[0], "loot", "chest loot " + name + " (" + e.getValue()[0] + "/300 chests, " + e.getValue()[1] + "/4500 raw draws)", true);
                    if (sb.length() < 400) {
                        sb.append(kl[1]).append(' ');
                    }
                }
                s.ok("loot " + name + " (" + found.size() + " distinct stacks)");
                s.info("loot " + name, found.size() + " distinct item variants: " + sb);
            } catch (Throwable ex) {
                s.exception("loot " + name, ex);
            }
        }
    }

    private String lootOne(TFTreasure table, String name, Map<String, int[]> found) {
        ServerPlayer p = t.sp();
        WorldServer w = t.swOf(p);
        boolean gates = TFConfig.lootDayGates;
        TFConfig.lootDayGates = false;
        Random rnd = new Random(1234L);
        String err = null;
        try {
            for (int i = 0; i < 300; i++) {
                int cx = -44 + (i % 30) * 3;
                int cz = -44 + (i / 30) * 4 + (i % 2);
                int cy = 135;
                w.setBlockToAir(cx, cy, cz, 2);
                table.generate(w, rnd, cx, cy, cz);
                Object te = w.getBlockTileEntity(cx, cy, cz);
                if (te instanceof TileEntityChest chest) {
                    Set<String> inThisChest = new HashSet<>();
                    for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                        ItemStack st = chest.getStackInSlot(slot);
                        if (st != null && st.getItem() != null && inThisChest.add(AcqBook.key(st))) {
                            found.computeIfAbsent(AcqBook.key(st) + "|" + label(st), k -> new int[2])[0]++;
                        }
                    }
                } else if (err == null) {
                    err = "generate() did not leave a chest tile entity at " + cx + "," + cy + "," + cz;
                }
                w.setBlockToAir(cx, cy, cz, 2);
            }
            for (int i = 0; i < 1500; i++) {
                for (ItemStack st : new ItemStack[] {table.getCommonItem(rnd), table.getUncommonItem(rnd), table.getRareItem(rnd)}) {
                    if (st != null && st.getItem() != null) {
                        found.computeIfAbsent(AcqBook.key(st) + "|" + label(st), k -> new int[2])[1]++;
                    }
                }
            }
        } catch (Throwable ex) {
            err = "exception generating " + name + ": " + ex;
            ex.printStackTrace();
        } finally {
            TFConfig.lootDayGates = gates;
        }
        return err;
    }

    // ================================================================== f) special interactions

    void stepSpecial(Report.Section s) throws Exception {
        enterSurvival();
        try {
            transformPowder(s);
            uncrafting(s);
            magicBeans(s);
            moonwormQueen(s);
            questRam(s);
            magicTrees(s);
        } finally {
            leaveSurvival();
        }
    }

    /** Runs a server-side special test and reports its single result line. */
    private interface Special {
        String run(ServerPlayer p, WorldServer w) throws Exception;
    }

    private void special(Report.Section s, String name, Special body) {
        special(s, name, body, true);
    }

    private void special(Report.Section s, String name, Special body, boolean cleanup) {
        try {
            String r = bot.server(() -> {
                ServerPlayer p = t.sp();
                WorldServer w = t.swOf(p);
                try {
                    return body.run(p, w);
                } finally {
                    if (cleanup) {
                        killNonPlayerEntities(w);
                    }
                }
            });
            // r = "OK text" or "FAIL text"
            if (r.startsWith("OK")) {
                s.ok(name);
                s.info(name, r.substring(2).trim());
            } else {
                s.fail(name, r.substring(4).trim());
            }
        } catch (Throwable ex) {
            s.exception(name, ex);
        }
    }

    private void transformPowder(Report.Section s) throws Exception {
        if (TFItems.transformPowder == null) {
            return;
        }
        Map<Class<?>, Class<?>> map;
        try {
            map = (Map<Class<?>, Class<?>>) TestBotScript.field(TFItems.transformPowder, TFItems.transformPowder.getClass(), "transformMap");
        } catch (Throwable ex) {
            s.exception("transformation powder map", ex);
            return;
        }
        ItemStack powderKey = new ItemStack(TFItems.transformPowder);
        for (Map.Entry<Class<?>, Class<?>> e : new ArrayList<>(map.entrySet())) {
            String name = "transformation powder " + e.getKey().getSimpleName() + " -> " + e.getValue().getSimpleName();
            final String detail = "powder turns " + e.getKey().getSimpleName() + " into " + e.getValue().getSimpleName();
            special(s, name, (p, w) -> {
                Entity target = (Entity) e.getKey().getConstructor(net.minecraft.World.class).newInstance(w);
                target.setLocationAndAngles(0.5D, FEET, 8.5D, 180.0F, 0.0F);
                w.spawnEntityInWorld(target);
                ItemStack powder = new ItemStack(TFItems.transformPowder, 3, 0);
                p.inventory.mainInventory[0] = powder;
                p.inventory.currentItem = 0;
                boolean used = TFItems.transformPowder.tryEntityInteraction(target, p, powder);
                boolean replaced = false;
                for (Object o : new ArrayList<Object>(w.loadedEntityList)) {
                    if (o != target && !((Entity) o).isDead && o.getClass() == e.getValue()) {
                        replaced = true;
                    }
                }
                p.inventory.mainInventory[0] = null;
                boolean ok = used && replaced && target.isDead;
                book.add(AcqBook.key(powderKey), "special", detail, ok);
                return (ok ? "OK " : "FAIL ") + "interaction used=" + used + ", new entity present=" + replaced + ", original removed=" + target.isDead;
            });
        }
    }

    private void uncrafting(Report.Section s) throws Exception {
        if (ContainerTFUncrafting.isDisabled()) {
            s.warn("uncrafting table", "disabled by config (uncrafting=off): no uncrafting acquisition possible");
            return;
        }
        List<IRecipe> recipes = bot.server(() -> new ArrayList<IRecipe>((List<IRecipe>) CraftingManager.getInstance().getRecipeList()));
        Set<String> doneOutputs = new HashSet<>();
        int n = 0;
        for (IRecipe r : recipes) {
            ItemStack out = r.getRecipeOutput();
            if (!(r instanceof ShapedRecipes) || !isTfStack(out) || !doneOutputs.add(AcqBook.key(out))) {
                continue;
            }
            n++;
            String name = "uncraft " + label(out);
            try {
                String res = bot.server(() -> {
                    ServerPlayer p = t.sp();
                    WorldServer w = t.swOf(p);
                    ContainerTFUncrafting c = new ContainerTFUncrafting(p.inventory, w, -20, FEET, -20);
                    ItemStack input = new ItemStack(out.itemID, out.stackSize, out.getItemSubtype());
                    c.tinkerInput.setInventorySlotContents(0, input);
                    c.onCraftMatrixChanged(c.tinkerInput);
                    List<String> got = new ArrayList<>();
                    for (int i = 0; i < c.uncraftingMatrix.getSizeInventory(); i++) {
                        ItemStack st = c.uncraftingMatrix.getStackInSlot(i);
                        if (st != null && st.getItem() != null) {
                            book.add(AcqBook.key(st), "uncraft", "uncrafting table: " + label(out), true);
                            got.add(label(st) + "x" + st.stackSize);
                        }
                    }
                    c.tinkerInput.setInventorySlotContents(0, null);
                    return got.isEmpty() ? null : got.toString();
                });
                if (res == null) {
                    s.info(name, "uncrafting table returns nothing (whitelist / metal rule / cost)");
                } else {
                    s.ok(name + " -> " + res);
                }
            } catch (Throwable ex) {
                s.exception(name, ex);
            }
        }
        s.info("uncrafting", n + " TF shaped recipes put into the uncrafting table");
    }

    private void magicBeans(Report.Section s) {
        if (TFItems.magicBeans == null || TFBlocks.uberousSoil == null || TFBlocks.hugeStalk == null) {
            return;
        }
        final int sx = 20;
        final int sz = 20;
        special(s, "magic beans on uberous soil grow the giant stalk", (p, w) -> {
            clearBox(w, sx - 3, FEET, sz - 6, sx + 3, FEET + 3, sz + 1);
            w.setBlock(sx, GROUND, sz, TFBlocks.uberousSoil.blockID, 0, 2);
            // player stands south of the soil, looking down at it
            p.setPositionAndRotation(sx + 0.5D, FEET + 1.0D, sz + 3.5D, 180.0F, 45.0F);
            p.inventory.mainInventory[0] = new ItemStack(TFItems.magicBeans, 1, 0);
            p.inventory.currentItem = 0;
            // aim exactly at the soil block centre
            double dx = (sx + 0.5D) - p.posX;
            double dy = (GROUND + 0.5D) - (p.posY + p.getEyeHeight());
            double dz = (sz + 0.5D) - p.posZ;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            p.setPositionAndRotation(p.posX, p.posY, p.posZ, yaw, pitch);
            boolean used = TFItems.magicBeans.onItemRightClick(p, 1.0F, false);
            int stalk = 0;
            for (int x = sx - 14; x <= sx + 14; x++) {
                for (int z = sz - 14; z <= sz + 14; z++) {
                    for (int y = GROUND + 1; y < GROUND + 40; y++) {
                        if (w.getBlockId(x, y, z) == TFBlocks.hugeStalk.blockID) {
                            stalk++;
                        }
                    }
                }
            }
            // clean the column away again
            clearBox(w, sx - 14, GROUND + 1, sz - 14, sx + 14, GROUND + 140, sz + 14);
            w.setBlockToAir(sx, GROUND, sz, 2);
            w.setBlock(sx, GROUND, sz, Block.stone.blockID, 0, 2);
            p.inventory.mainInventory[0] = null;
            boolean ok = used && stalk > 20;
            book.add(AcqBook.key(new ItemStack(TFBlocks.hugeStalk)), "special", "magic beans on uberous soil grow the stalk", ok);
            return (ok ? "OK " : "FAIL ") + "onItemRightClick=" + used + ", huge stalk blocks in the first 40 layers above the soil: " + stalk;
        });
    }

    private void moonwormQueen(Report.Section s) {
        if (TFItems.moonwormQueen == null || TFBlocks.moonworm == null) {
            return;
        }
        special(s, "moonworm queen places a moonworm on a wall", (p, w) -> {
            int wx = -20;
            int wz = 20;
            clearBox(w, wx - 2, FEET, wz - 2, wx + 2, FEET + 4, wz + 6);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = 0; dy < 4; dy++) {
                    w.setBlock(wx + dx, FEET + dy, wz, Block.stone.blockID, 0, 2);
                }
            }
            p.setPositionAndRotation(wx + 0.5D, FEET, wz + 4.0D, 180.0F, -10.0F);
            double dx = (wx + 0.5D) - p.posX;
            double dy = (FEET + 1.5D) - (p.posY + p.getEyeHeight());
            double dz = (wz + 0.9D) - p.posZ;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            p.setPositionAndRotation(p.posX, p.posY, p.posZ, yaw, pitch);
            p.inventory.mainInventory[0] = new ItemStack(TFItems.moonwormQueen);
            p.inventory.currentItem = 0;
            boolean used = TFItems.moonwormQueen.onItemRightClick(p, 1.0F, false);
            int worms = 0;
            for (int x = wx - 3; x <= wx + 3; x++) {
                for (int y = FEET - 1; y < FEET + 5; y++) {
                    for (int z = wz - 3; z <= wz + 5; z++) {
                        if (w.getBlockId(x, y, z) == TFBlocks.moonworm.blockID) {
                            worms++;
                        }
                    }
                }
            }
            clearBox(w, wx - 3, FEET, wz - 3, wx + 3, FEET + 5, wz + 6);
            p.inventory.mainInventory[0] = null;
            boolean ok = worms > 0;
            book.add(AcqBook.key(new ItemStack(TFBlocks.moonworm)), "special", "moonworm queen right click on a wall", ok);
            return (ok ? "OK " : "FAIL ") + "onItemRightClick=" + used + ", moonworm blocks placed: " + worms;
        });
    }

    private void questRam(Report.Section s) throws Exception {
        final twilightforest.entity.passive.EntityTFQuestRam[] holder = new twilightforest.entity.passive.EntityTFQuestRam[1];
        special(s, "quest ram: feed 16 wool colours", (p, w) -> {
            twilightforest.entity.passive.EntityTFQuestRam ram = new twilightforest.entity.passive.EntityTFQuestRam(w);
            ram.setLocationAndAngles(0.5D, FEET, 8.5D, 180.0F, 0.0F);
            w.spawnEntityInWorld(ram);
            holder[0] = ram;
            int accepted = 0;
            for (int color = 0; color < 16; color++) {
                ItemStack wool = new ItemStack(Block.cloth, 1, color);
                p.inventory.mainInventory[0] = wool;
                p.inventory.currentItem = 0;
                if (ram.onEntityRightClicked(p, wool) && ram.isColorPresent(color)) {
                    accepted++;
                }
            }
            p.inventory.mainInventory[0] = null;
            boolean ok = accepted == 16;
            return (ok ? "OK " : "FAIL ") + accepted + "/16 colours accepted, colours set=" + ram.countColorsSet();
        }, false);
        // the reward is paid from the AI tick: wait for a few server ticks, then sweep the dropped items
        if (holder[0] == null) {
            return;
        }
        String name = "quest ram: reward after 16 colours";
        try {
            Set<Integer> before = bot.server(() -> itemEntityIds(t.swOf(t.sp())));
            bot.serverTicks(160);
            List<ItemStack> drops = bot.server(() -> sweepItems(t.swOf(t.sp()), before));
            boolean rewarded = bot.server(() -> holder[0].getRewarded());
            for (ItemStack d : drops) {
                book.add(AcqBook.key(d), "special", "quest ram reward", true);
            }
            if (rewarded && !drops.isEmpty()) {
                s.ok(name);
                s.info(name, "reward " + list(drops, this));
            } else {
                s.fail(name, "rewarded=" + rewarded + " drops=" + list(drops, this) + " (the reward needs the ram to tick next to a loaded world; quest grove home area is not required)");
            }
        } catch (Throwable ex) {
            s.exception(name, ex);
        } finally {
            bot.server(() -> killNonPlayerEntities(t.swOf(t.sp())));
        }
    }

    private void magicTrees(Report.Section s) {
        if (TFBlocks.magicLogSpecial == null) {
            return;
        }
        final int cx = 30;
        final int cz = -30;
        final int cy = FEET + 1;
        // time: wheat on farmland next to the core must grow
        special(s, "tree of time core accelerates crop growth", (p, w) -> {
            clearBox(w, cx - 4, FEET, cz - 4, cx + 4, FEET + 6, cz + 4);
            w.setBlock(cx + 2, GROUND, cz, Block.tilledField.blockID, 7, 2);
            w.setBlock(cx + 2, FEET, cz, Block.crops.blockID, 0, 2);
            w.setBlock(cx, FEET, cz, TFBlocks.magicLogSpecial.blockID, 0, 2); // META_TIME
            Random rnd = new Random(1L);
            boolean def = TFConfig.treesTime;
            TFConfig.treesTime = true;
            try {
                for (int i = 0; i < 40; i++) {
                    TFBlocks.magicLogSpecial.updateTick(w, cx, FEET, cz, rnd);
                }
            } finally {
                TFConfig.treesTime = def;
            }
            int grown = w.getBlockMetadata(cx + 2, FEET, cz);
            clearBox(w, cx - 4, FEET, cz - 4, cx + 4, FEET + 6, cz + 4);
            w.setBlock(cx + 2, GROUND, cz, Block.stone.blockID, 0, 2);
            boolean ok = grown > 0;
            return (ok ? "OK " : "FAIL ") + "wheat metadata after 40 time pulses (trees.time forced on for the test; default config trees.time=" + def
                + (def ? "" : ", i.e. the tree of time does nothing by default") + "): " + grown + " (0 = nothing grew; MITE crops also need light/water)";
        });
        // transformation: biome array changes to the enchanted forest
        special(s, "tree of transformation core changes biomes", (p, w) -> {
            w.setBlock(cx, FEET, cz, TFBlocks.magicLogSpecial.blockID, 1, 2);
            Random rnd = new Random(2L);
            int before = w.getBiomeGenForCoords(cx, cz).biomeID;
            for (int i = 0; i < 400; i++) {
                TFBlocks.magicLogSpecial.updateTick(w, cx, FEET, cz, rnd);
            }
            int changed = 0;
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = -16; dz <= 16; dz++) {
                    if (w.getBiomeGenForCoords(cx + dx, cz + dz).biomeID == twilightforest.biomes.TFBiomeBase.enchantedForest.biomeID) {
                        changed++;
                    }
                }
            }
            w.setBlockToAir(cx, FEET, cz, 2);
            boolean ok = changed > 20;
            return (ok ? "OK " : "FAIL ") + "biome before " + before + ", columns turned into the enchanted forest after 400 pulses: " + changed;
        });
        // mining: an ore deep in stone is moved towards the core (ItemTFOreMagnet.doMagnet)
        special(s, "tree of mining core moves ores (ore magnet)", (p, w) -> {
            int x0 = cx + 20;
            int z0 = cz;
            for (int x = x0 - 10; x <= x0 + 10; x++) {
                for (int y = GROUND; y <= GROUND + 14; y++) {
                    for (int z = z0 - 10; z <= z0 + 10; z++) {
                        w.setBlock(x, y, z, Block.stone.blockID, 0, 2);
                    }
                }
            }
            int coreY = GROUND + 2;
            w.setBlock(x0, coreY, z0, TFBlocks.magicLogSpecial.blockID, 2, 2);
            int ox = x0 + 7;
            int oy = GROUND + 10;
            w.setBlock(ox, oy, z0, Block.oreIron.blockID, 0, 2);
            int moved = twilightforest.item.ItemTFOreMagnet.doMagnet(w, x0, coreY, z0, ox, oy, z0);
            boolean still = w.getBlockId(ox, oy, z0) == Block.oreIron.blockID;
            Random rnd = new Random(3L);
            boolean def = TFConfig.treesMining;
            TFConfig.treesMining = true;
            try {
                for (int i = 0; i < 100; i++) {
                    TFBlocks.magicLogSpecial.updateTick(w, x0, coreY, z0, rnd);
                }
            } finally {
                TFConfig.treesMining = def;
            }
            clearBox(w, x0 - 10, GROUND, z0 - 10, x0 + 10, GROUND + 14, z0 + 10);
            for (int x = x0 - 10; x <= x0 + 10; x++) {
                for (int z = z0 - 10; z <= z0 + 10; z++) {
                    w.setBlock(x, GROUND, z, Block.stone.blockID, 0, 2);
                }
            }
            boolean ok = moved > 0 && !still;
            return (ok ? "OK " : "FAIL ") + "doMagnet moved " + moved + " blocks, ore left its start position: " + !still + " (default config trees.mining=" + def
                + (def ? "" : ", i.e. the miner's tree core does nothing by default") + ")";
        });
        // sorting: a chest pair, items of one creative tab are collected into the chest that already holds most of them
        special(s, "tree of sorting core sorts chests", (p, w) -> {
            int x0 = cx;
            int z0 = cz + 10;
            clearBox(w, x0 - 5, FEET, z0 - 5, x0 + 5, FEET + 4, z0 + 5);
            w.setBlock(x0 + 2, FEET, z0, Block.chest.blockID, 2, 2);
            w.setBlock(x0 - 2, FEET, z0, Block.chest.blockID, 2, 2);
            TileEntityChest a = (TileEntityChest) w.getBlockTileEntity(x0 + 2, FEET, z0);
            TileEntityChest b = (TileEntityChest) w.getBlockTileEntity(x0 - 2, FEET, z0);
            // chest a: many blocks + one tool; chest b: many tools + one block
            for (int i = 0; i < 6; i++) {
                a.setInventorySlotContents(i, new ItemStack(Block.cobblestone, 8));
                b.setInventorySlotContents(i, new ItemStack(Item.pickaxeIron));
            }
            a.setInventorySlotContents(10, new ItemStack(Item.pickaxeIron));
            b.setInventorySlotContents(10, new ItemStack(Block.cobblestone, 8));
            w.setBlock(x0, FEET, z0, TFBlocks.magicLogSpecial.blockID, 3, 2);
            Random rnd = new Random(4L);
            for (int i = 0; i < 400; i++) {
                TFBlocks.magicLogSpecial.updateTick(w, x0, FEET, z0, rnd);
            }
            int toolsInA = 0;
            int blocksInB = 0;
            for (int i = 0; i < a.getSizeInventory(); i++) {
                ItemStack st = a.getStackInSlot(i);
                if (st != null && st.itemID == Item.pickaxeIron.itemID) {
                    toolsInA++;
                }
                st = b.getStackInSlot(i);
                if (st != null && st.itemID == Block.cobblestone.blockID) {
                    blocksInB++;
                }
            }
            clearBox(w, x0 - 5, FEET, z0 - 5, x0 + 5, FEET + 4, z0 + 5);
            boolean ok = toolsInA == 0 && blocksInB == 0;
            return (ok ? "OK " : "FAIL ") + "stray tools left in the block chest: " + toolsInA + ", stray blocks left in the tool chest: " + blocksInB;
        });
    }

    // ================================================================== matrix

    void stepMatrix(Report.Section s) throws Exception {
        if (universe == null) {
            universe = t.allStacks(s);
        }
        File dir = bot.dir;
        File balance = new File(dir.getParentFile(), "balance/MITE_BALANCE.md");
        File srcRoot = new File(System.getProperty("twilightforest.testbot.src", "../../mite-twilightforest/src/main/java"));
        Map<String, List<String>> refs = AcqBook.referenceScan(srcRoot);
        s.info("inputs", "balance doc " + balance + (balance.isFile() ? "" : " (missing)") + "; source tree " + srcRoot + " (" + refs.size() + " TFItems/TFBlocks members referenced outside the registries)");

        int green = 0;
        int config = 0;
        int red = 0;
        StringBuilder matrix = new StringBuilder();
        matrix.append("| Item / block subtype | key | craft | smelt | harvest | mob | boss | loot | special | uncraft | config | verdict |\n");
        matrix.append("|---|---|---|---|---|---|---|---|---|---|---|---|\n");
        StringBuilder reds = new StringBuilder();
        reds.append("| Item / block subtype | key | failed sources tried | referenced by (source tree) | MITE_BALANCE.md | note |\n|---|---|---|---|---|---|\n");
        StringBuilder details = new StringBuilder();
        List<String> redKeys = new ArrayList<>();
        for (ItemStack st : universe) {
            String key = AcqBook.key(st);
            List<AcqBook.Src> srcs = book.get(key);
            StringBuilder row = new StringBuilder("| ").append(label(st)).append(" | ").append(key);
            boolean pass = false;
            boolean cfgOnly = false;
            for (String type : AcqBook.TYPES) {
                int p = book.count(key, type, true);
                int f = book.count(key, type, false);
                row.append(" | ").append(p == 0 && f == 0 ? "-" : (p > 0 ? "P" + p : "") + (f > 0 ? (p > 0 ? "/" : "") + "F" + f : ""));
                if (p > 0 && !type.equals("config")) {
                    pass = true;
                }
            }
            if (!pass && book.count(key, "config", true) > 0) {
                cfgOnly = true;
            }
            String verdict = pass ? "OK" : cfgOnly ? "CONFIG-ONLY" : "**RED**";
            row.append(" | ").append(verdict).append(" |\n");
            matrix.append(row);
            if (pass) {
                green++;
            } else if (cfgOnly) {
                config++;
            } else {
                red++;
                redKeys.add(key);
                reds.append(redRow(st, key, srcs, refs, balance));
            }
            details.append("- ").append(label(st)).append(" (").append(key).append(")");
            if (srcs.isEmpty()) {
                details.append(": no source exercised\n");
            } else {
                details.append(":\n");
                for (AcqBook.Src src : srcs) {
                    details.append("  - ").append(src.pass ? "PASS " : "FAIL ").append(src.type).append(": ").append(src.detail.replace("\n", " ")).append("\n");
                }
            }
        }
        s.info("summary", universe.size() + " item/block subtypes: " + green + " with at least one working default source, " + config + " only with a non-default config, " + red
            + " RED (no working source)");
        if (red > 0) {
            s.warn("red list", red + " subtypes have no working source: " + redKeys);
        }
        s.markdown("### Red list: items with NO working source\n\nEvery row failed or never had a tested source. \"referenced by\" lists code outside the registries that mentions the item "
            + "(worldgen, structures, events: not exercised by the bot, so a reference there can be an untested source). \"MITE_BALANCE.md\" shows balance-doc lines mentioning the "
            + "item; a removal there means the missing source is intended.\n\n" + reds);
        s.markdown("### Acquisition matrix (P = passing sources, F = failed sources, - = none tested)\n\n" + matrix);
        s.markdown("<details><summary>Every exercised source per item</summary>\n\n" + details + "\n</details>");
        if (!sourceNotes.isEmpty()) {
            s.markdown("Notes: " + sourceNotes);
        }
    }

    private String redRow(ItemStack st, String key, List<AcqBook.Src> srcs, Map<String, List<String>> refs, File balance) {
        String field = fieldNames.get(st.itemID);
        String bare = field == null ? null : field.substring(field.indexOf('.') + 1);
        StringBuilder tried = new StringBuilder();
        for (AcqBook.Src src : srcs) {
            tried.append(src.type).append(": ").append(src.detail.replace("|", "/")).append("<br>");
        }
        if (tried.length() == 0) {
            tried.append("none (no recipe, drop, loot entry or interaction found)");
        }
        String refCell = "-";
        if (field != null) {
            List<String> files = refs.get(field.startsWith("TFItems.") ? "Items." + bare : "Blocks." + bare);
            if (files != null) {
                refCell = String.join(", ", files.size() > 8 ? files.subList(0, 8) : files) + (files.size() > 8 ? ", ..." : "");
            }
        }
        List<String> hints = AcqBook.balanceHints(balance, bare, null);
        String hintCell = hints.isEmpty() ? "-" : String.join("<br>", hints);
        String note = "";
        Item it = st.getItem();
        if (it instanceof twilightforest.item.ItemTFSpawnEgg) {
            note = "spawn egg: creative only by design";
        } else if (it instanceof ItemBlock && st.itemID >= TFIds.BLOCK_FIRST && st.itemID <= TFIds.BLOCK_LAST) {
            Block b = Block.blocksList[st.itemID];
            String cn = b == null ? "" : b.getClass().getSimpleName();
            if (cn.contains("BossSpawner") || cn.contains("ForceField") || cn.contains("Portal") || cn.contains("Vanished") || cn.contains("CastleUnlock")) {
                note = "technical block (" + cn + "): creative / worldgen only";
            }
        }
        return "| " + label(st) + " | " + key + " | " + tried + " | " + refCell + " | " + hintCell + " | " + note + " |\n";
    }
}
