package twilightforest.command;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemStack;
import twilightforest.TwilightForestMod;

/**
 * Name/id lookup for the developer commands. TF items and blocks are discovered by reflection over TFItems / TFBlocks
 * (so this class compiles and works whichever of those fields exist) plus a scan of Item.itemsList for TF classes.
 */
public final class TFItemLookup {
    private TFItemLookup() {}

    /** Item -> TFItems/TFBlocks field name (blocks map to their ItemBlock). */
    public static Map<Item, String> fieldNames() {
        Map<Item, String> map = new IdentityHashMap<>();
        collect("twilightforest.item.TFItems", map);
        collect("twilightforest.block.TFBlocks", map);
        return map;
    }

    private static void collect(String className, Map<Item, String> out) {
        Class<?> cls;
        try {
            cls = Class.forName(className);
        } catch (Throwable t) {
            return;
        }
        for (Field f : cls.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object o = f.get(null);
                Item item = null;
                if (o instanceof Item i) {
                    item = i;
                } else if (o instanceof Block b && b.blockID > 0 && b.blockID < Item.itemsList.length) {
                    item = Item.itemsList[b.blockID];
                }
                if (item != null && !out.containsKey(item)) {
                    out.put(item, f.getName());
                }
            } catch (Throwable ignored) {
                // field not readable yet
            }
        }
    }

    public static boolean isTFItem(Item item, Map<Item, String> fields) {
        if (item == null) {
            return false;
        }
        if (fields.containsKey(item)) {
            return true;
        }
        if (item.getClass().getName().startsWith("twilightforest.")) {
            return true;
        }
        if (item instanceof ItemBlock && item.itemID < Block.blocksList.length) {
            Block b = Block.blocksList[item.itemID];
            return b != null && b.getClass().getName().startsWith("twilightforest.");
        }
        return false;
    }

    /** All TF items sorted by numeric id. */
    public static List<Item> allTFItems() {
        Map<Item, String> fields = fieldNames();
        List<Item> list = new ArrayList<>();
        for (Item item : Item.itemsList) {
            if (isTFItem(item, fields)) {
                list.add(item);
            }
        }
        return list;
    }

    /** Subtypes the item advertises through getSubItems (always at least subtype 0). */
    public static List<ItemStack> subStacks(Item item) {
        List<ItemStack> result = new ArrayList<>();
        try {
            List raw = new ArrayList();
            item.getSubItems(item.itemID, (CreativeTabs) null, raw);
            TreeSet<Integer> seen = new TreeSet<>();
            for (Object o : raw) {
                if (o instanceof ItemStack s && s.getItem() != null && seen.add(s.getItemSubtype())) {
                    result.add(s);
                }
            }
        } catch (Throwable t) {
            TwilightForestMod.LOGGER.warn("getSubItems failed for item {}: {}", item.itemID, t.toString());
        }
        if (result.isEmpty()) {
            result.add(new ItemStack(item, 1, 0));
        }
        return result;
    }

    private static String norm(String s) {
        String n = s.toLowerCase(Locale.ROOT).trim();
        for (String p : new String[] {"minecraft:", "twilightforest:", "item.", "tile."}) {
            if (n.startsWith(p)) {
                n = n.substring(p.length());
            }
        }
        return n.replace("_", "").replace("-", "").replace(" ", "");
    }

    /** Resolves a numeric id, a TFItems/TFBlocks field name, an unlocalized name or a vanilla unlocalized name. */
    public static Item find(String name) {
        try {
            int id = Integer.parseInt(name.trim());
            return id > 0 && id < Item.itemsList.length ? Item.itemsList[id] : null;
        } catch (NumberFormatException ignored) {
            // fall through to names
        }
        String wanted = norm(name);
        Map<Item, String> fields = fieldNames();
        for (Map.Entry<Item, String> e : fields.entrySet()) {
            if (norm(e.getValue()).equals(wanted)) {
                return e.getKey();
            }
        }
        for (Item item : Item.itemsList) {
            if (item == null) {
                continue;
            }
            try {
                String un = item.getUnlocalizedName();
                if (un != null && norm(un).equals(wanted)) {
                    return item;
                }
            } catch (Throwable ignored) {
                // some items cannot name themselves without a stack
            }
        }
        try {
            return twilightforest.compat.TFRegistry.findItem(name.trim());
        } catch (Throwable t) {
            return null;
        }
    }

    /** Names for tab completion: TF field names first, then TF-agnostic unlocalized names. */
    public static List<String> completionNames() {
        TreeSet<String> names = new TreeSet<>();
        for (String n : fieldNames().values()) {
            names.add(n);
        }
        return new ArrayList<>(names);
    }

    public static Map<Integer, String> idToField() {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (Map.Entry<Item, String> e : fieldNames().entrySet()) {
            m.put(e.getKey().itemID, e.getValue());
        }
        return m;
    }
}
