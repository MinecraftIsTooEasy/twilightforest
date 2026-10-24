package twilightforest.testbot;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ItemStack;

/**
 * Per item subtype ledger of every acquisition source the bot exercised (crafting, smelting, harvest, mob and boss drops, chest loot, special
 * interactions). Keys are "itemID:subtype" (MITE keeps subtype and damage apart, so the subtype is the identity of a variant).
 * Thread-safe: the steps run on the script thread but record results from server task callbacks too.
 */
final class AcqBook {
    /** One exercised source. */
    static final class Src {
        final String type;
        final String detail;
        final boolean pass;

        Src(String type, String detail, boolean pass) {
            this.type = type;
            this.detail = detail;
            this.pass = pass;
        }
    }

    static final String[] TYPES = {"craft", "smelt", "harvest", "mob", "boss", "loot", "special", "uncraft", "config"};

    final Map<String, List<Src>> sources = new LinkedHashMap<>();
    /** Free text lines for items that are not in the universe (kept for the report appendix). */
    final List<String> orphanNotes = new ArrayList<>();

    static String key(ItemStack st) {
        return st.itemID + ":" + (st.getHasSubtypes() ? st.getItemSubtype() : 0);
    }

    synchronized void add(String key, String type, String detail, boolean pass) {
        List<Src> l = sources.computeIfAbsent(key, k -> new ArrayList<>());
        // collapse exact duplicates (same type + detail), keep a pass over a fail
        for (int i = 0; i < l.size(); i++) {
            Src s = l.get(i);
            if (s.type.equals(type) && s.detail.equals(detail)) {
                if (!s.pass && pass) {
                    l.set(i, new Src(type, detail, true));
                }
                return;
            }
        }
        l.add(new Src(type, detail, pass));
    }

    synchronized void add(ItemStack st, String type, String detail, boolean pass) {
        if (st != null && st.getItem() != null) {
            add(key(st), type, detail, pass);
        }
    }

    synchronized List<Src> get(String key) {
        List<Src> l = sources.get(key);
        return l == null ? Collections.emptyList() : new ArrayList<>(l);
    }

    synchronized boolean hasPass(String key) {
        for (Src s : get(key)) {
            if (s.pass) {
                return true;
            }
        }
        return false;
    }

    synchronized int count(String key, String type, boolean pass) {
        int n = 0;
        for (Src s : get(key)) {
            if (s.type.equals(type) && s.pass == pass) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ doc hints for the red list

    /** Lines of MITE_BALANCE.md that mention the item and talk about removal / change; best effort, case-insensitive. */
    static List<String> balanceHints(File balanceDoc, String fieldName, String unlocalized) {
        List<String> out = new ArrayList<>();
        if (balanceDoc == null || !balanceDoc.isFile() || fieldName == null) {
            return out;
        }
        try {
            List<String> lines = Files.readAllLines(balanceDoc.toPath(), StandardCharsets.UTF_8);
            // "giantCobble" -> "giant cobble"; "knightMetalNugget" -> "knight metal nugget" (also the joined form)
            String spaced = fieldName.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase(Locale.ROOT);
            String joined = spaced.replace(" ", "");
            String compact = spaced.replace(" ", "[ _-]?");
            Pattern p = Pattern.compile(compact, Pattern.CASE_INSENSITIVE);
            Pattern kw = Pattern.compile("删除|不再|移除|取消|不可|不掉|不能|不合成|来自|removed|no longer|disabled|not craftable|only|仅", Pattern.CASE_INSENSITIVE);
            int shown = 0;
            for (int i = 0; i < lines.size() && shown < 4; i++) {
                String l = lines.get(i);
                Matcher m = p.matcher(l);
                boolean found = m.find();
                if (found || l.toLowerCase(Locale.ROOT).contains(joined)) {
                    if (kw.matcher(l).find() || shown < 1) {
                        String ex = l.trim();
                        if (ex.length() > 220) {
                            int at = Math.max(0, (found ? m.start() : 0) - 60);
                            ex = ex.substring(Math.min(at, ex.length() - 1), Math.min(ex.length(), at + 220));
                        }
                        out.add("L" + (i + 1) + ": " + ex.replace("|", "/"));
                        shown++;
                    }
                }
            }
        } catch (Exception e) {
            out.add("(balance doc unreadable: " + e + ")");
        }
        return out;
    }

    /** simple name -> files in the source tree that reference TFItems.<field> / TFBlocks.<field> (worldgen / structure / event references). */
    static Map<String, List<String>> referenceScan(File srcRoot) {
        Map<String, List<String>> refs = new TreeMap<>();
        if (srcRoot == null || !srcRoot.isDirectory()) {
            return refs;
        }
        Pattern p = Pattern.compile("\\bTF(Items|Blocks)\\.([A-Za-z0-9_]+)");
        List<File> files = new ArrayList<>();
        collect(srcRoot, files);
        for (File f : files) {
            String name = f.getName();
            if (name.equals("TFItems.java") || name.equals("TFBlocks.java") || name.equals("TFIds.java")) {
                continue;
            }
            try {
                String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                Matcher m = p.matcher(text);
                java.util.Set<String> seen = new java.util.HashSet<>();
                while (m.find()) {
                    String k = m.group(1) + "." + m.group(2);
                    if (seen.add(k)) {
                        refs.computeIfAbsent(k, x -> new ArrayList<>()).add(name.replace(".java", ""));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return refs;
    }

    private static void collect(File dir, List<File> out) {
        File[] kids = dir.listFiles();
        if (kids == null) {
            return;
        }
        for (File k : kids) {
            if (k.isDirectory()) {
                collect(k, out);
            } else if (k.getName().endsWith(".java")) {
                out.add(k);
            }
        }
    }
}
