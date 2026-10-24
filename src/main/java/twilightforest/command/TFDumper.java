package twilightforest.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.CraftingManager;
import net.minecraft.FurnaceRecipes;
import net.minecraft.IRecipe;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.ShapedRecipes;
import net.minecraft.TileEntityFurnace;
import twilightforest.TwilightForestMod;

/** Writes twilightforest-item-ids.md and twilightforest-recipes.md into the game (server working) directory. */
public final class TFDumper {
    public static final String ITEMS_FILE = "twilightforest-item-ids.md";
    public static final String RECIPES_FILE = "twilightforest-recipes.md";
    private static final int WILDCARD = 32767;

    private TFDumper() {}

    /** Startup hook: never throws. */
    public static void dumpAllQuietly() {
        try {
            dumpItems();
            dumpRecipes();
        } catch (Throwable t) {
            TwilightForestMod.LOGGER.warn("Twilight Forest dump on startup failed", t);
        }
    }

    // ------------------------------------------------------------------ lang

    static Map<String, String> loadLang(String code) {
        Map<String, String> map = new HashMap<>();
        try (InputStream in = TFDumper.class.getResourceAsStream("/assets/twilightforest/lang/" + code + ".lang")) {
            if (in == null) {
                return map;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq > 0) {
                    map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }
        } catch (IOException e) {
            TwilightForestMod.LOGGER.warn("Could not read lang file {}: {}", code, e.toString());
        }
        return map;
    }

    private static String unlocalized(Item item, ItemStack stack) {
        try {
            String s = item.getUnlocalizedName(stack);
            return s == null ? "" : s;
        } catch (Throwable t) {
            try {
                return item.getUnlocalizedName();
            } catch (Throwable t2) {
                return "";
            }
        }
    }

    private static String md(String s) {
        return s == null ? "" : s.replace("|", "\\|");
    }

    // ------------------------------------------------------------------ items

    /** @return the number of rows written. */
    public static int dumpItems() throws IOException {
        Map<String, String> en = loadLang("en_US");
        Map<String, String> zh = loadLang("zh_CN");
        Map<Item, String> fields = TFItemLookup.fieldNames();
        StringBuilder sb = new StringBuilder();
        sb.append("# Twilight Forest item ids\n\n");
        sb.append("Generated automatically. Block items have id = block id. `/give @p <id> <count> <subtype>` is the vanilla command;")
            .append(" `/twilightforest give <id> <count> <subtype>` (alias `/tf give`) is the built-in fallback.\n\n");
        sb.append("| id | subtype | field | unlocalized name | English | Chinese | /give | /twilightforest give |\n");
        sb.append("|---:|---:|---|---|---|---|---|---|\n");
        int rows = 0;
        for (Item item : TFItemLookup.allTFItems()) {
            String field = fields.getOrDefault(item, "-");
            for (ItemStack stack : TFItemLookup.subStacks(item)) {
                int sub = stack.getItemSubtype();
                String un = unlocalized(item, stack);
                String key = un + ".name";
                String e = en.getOrDefault(key, "?");
                String z = zh.getOrDefault(key, "?");
                sb.append("| ").append(item.itemID).append(" | ").append(sub).append(" | ").append(md(field))
                    .append(" | ").append(md(un)).append(" | ").append(md(e)).append(" | ").append(md(z))
                    .append(" | `/give @p ").append(item.itemID).append(" 1 ").append(sub).append("`")
                    .append(" | `/twilightforest give ").append(item.itemID).append(" 1 ").append(sub).append("` |\n");
                rows++;
            }
        }
        write(ITEMS_FILE, sb.toString());
        TwilightForestMod.LOGGER.info("Wrote {} ({} rows)", ITEMS_FILE, rows);
        return rows;
    }

    // ------------------------------------------------------------------ recipes

    private static String label(ItemStack s, Map<Item, String> fields) {
        if (s == null) {
            return "_";
        }
        Item item = s.getItem();
        StringBuilder b = new StringBuilder();
        if (item == null) {
            b.append("id").append(s.itemID);
        } else {
            String f = fields.get(item);
            b.append(f != null ? f : strip(unlocalized(item, s)));
            b.append('(').append(item.itemID).append(')');
        }
        int sub = s.getItemSubtype();
        if (sub == WILDCARD) {
            b.append("#*");
        } else if (sub != 0) {
            b.append('#').append(sub);
        }
        if (s.stackSize > 1) {
            b.append(" x").append(s.stackSize);
        }
        return b.toString();
    }

    private static String strip(String un) {
        return un.startsWith("item.") ? un.substring(5) : un.startsWith("tile.") ? un.substring(5) : un;
    }

    private static boolean involves(ItemStack s, Set<Integer> tfIds) {
        return s != null && tfIds.contains(s.itemID);
    }

    private static int shapedWidth(ShapedRecipes r, int len) {
        try {
            Field f = ShapedRecipes.class.getDeclaredField("recipeWidth");
            f.setAccessible(true);
            int w = f.getInt(r);
            if (w > 0) {
                return w;
            }
        } catch (Throwable ignored) {
            // fall back to a guess
        }
        return len == 9 ? 3 : len == 4 ? 2 : Math.max(1, len);
    }

    public static int dumpRecipes() throws IOException {
        Map<Item, String> fields = TFItemLookup.fieldNames();
        Set<Integer> tfIds = new HashSet<>();
        for (Item i : TFItemLookup.allTFItems()) {
            tfIds.add(i.itemID);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Twilight Forest recipes\n\n");
        sb.append("Crafting recipes whose output or any input is a Twilight Forest item. Format: `name(id)#subtype xcount`, `#*` = any subtype, `_` = empty.\n");
        sb.append("Difficulty is the MITE crafting difficulty (IRecipe.getUnmodifiedDifficulty).\n\n## Crafting\n\n");

        int count = 0;
        List recipes = new ArrayList(CraftingManager.getInstance().getRecipeList());
        for (Object o : recipes) {
            if (!(o instanceof IRecipe recipe)) {
                continue;
            }
            ItemStack out = null;
            ItemStack[] comps = null;
            try {
                out = recipe.getRecipeOutput();
                comps = recipe.getComponents();
            } catch (Throwable t) {
                continue;
            }
            boolean tf = involves(out, tfIds);
            if (!tf && comps != null) {
                for (ItemStack c : comps) {
                    if (involves(c, tfIds)) {
                        tf = true;
                        break;
                    }
                }
            }
            if (!tf) {
                continue;
            }
            count++;
            boolean shaped = recipe instanceof ShapedRecipes;
            sb.append("### ").append(count).append(". ").append(label(out, fields)).append("\n\n");
            sb.append("- type: ").append(shaped ? "shaped" : recipe instanceof net.minecraft.ShapelessRecipes ? "shapeless" : recipe.getClass().getSimpleName()).append('\n');
            sb.append("- difficulty: ").append(recipe.getUnmodifiedDifficulty()).append('\n');
            sb.append("```text\n");
            if (comps != null) {
                if (shaped) {
                    int w = shapedWidth((ShapedRecipes) recipe, comps.length);
                    for (int i = 0; i < comps.length; i += w) {
                        StringBuilder row = new StringBuilder();
                        for (int j = i; j < Math.min(comps.length, i + w); j++) {
                            if (j > i) {
                                row.append(" | ");
                            }
                            row.append(label(comps[j], fields));
                        }
                        sb.append(row).append('\n');
                    }
                } else {
                    for (ItemStack c : comps) {
                        sb.append("+ ").append(label(c, fields)).append('\n');
                    }
                }
            }
            sb.append("=> ").append(label(out, fields)).append("\n```\n\n");
        }

        sb.append("## Furnace / smelting\n\n| input | output | heat level |\n|---|---|---|\n");
        int smelt = 0;
        Map<?, ?> list = FurnaceRecipes.smelting().getSmeltingList();
        for (Map.Entry<?, ?> e : list.entrySet()) {
            if (!(e.getKey() instanceof Integer id) || !(e.getValue() instanceof ItemStack result)) {
                continue;
            }
            if (!tfIds.contains(id) && !involves(result, tfIds)) {
                continue;
            }
            ItemStack in = Item.itemsList[id] != null ? new ItemStack(id, 1, 0) : null;
            String heat = "?";
            try {
                heat = String.valueOf(TileEntityFurnace.getHeatLevelRequired(id));
            } catch (Throwable ignored) {
                // leave unknown
            }
            sb.append("| ").append(md(in == null ? "id" + id : label(in, fields))).append(" | ")
                .append(md(label(result, fields))).append(" | ").append(heat).append(" |\n");
            smelt++;
        }

        write(RECIPES_FILE, sb.toString());
        TwilightForestMod.LOGGER.info("Wrote {} ({} crafting, {} smelting)", RECIPES_FILE, count, smelt);
        return count + smelt;
    }

    private static void write(String name, String content) throws IOException {
        File f = new File(name);
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
