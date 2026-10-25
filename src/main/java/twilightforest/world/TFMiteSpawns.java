package twilightforest.world;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.BiomeGenBase;
import net.minecraft.EnumCreatureType;
import net.minecraft.SpawnListEntry;
import net.minecraft.WeightedRandomItem;
import net.minecraft.World;
import twilightforest.TFConfig;

/**
 * MITE's own underground monsters (ghouls, wights, shadows, revenants, jellies, bone lords, ...) for the hollow hills and the caves
 * below sea level. They come from MITE's base biome list and keep their vanilla stats: TFBalance only tunes twilightforest.entity
 * mobs. Their weights are spawn.miteMonsterWeightPct percent of MITE's, so the TF list stays the bulk of the spawns; monsters the
 * TF list already has keep their TF weight. Each mob's own getCanSpawnHere (light, depth) still decides.
 * <p>
 * Entries from other mods in MITE's base list (e.g. ITF's retinue zombies and bone bodyguards, weight 10 or 35) are mixed in at their
 * full weight and group size, resolved at runtime so there is no compile dependency. TF's weights are tiny next to MITE's (~40 vs
 * ~900), so the same absolute weight gives elites a larger underground share here than in the overworld caves. RIC's spawn conditions
 * (WorldServer.getSuitableCreature) do run in the forest: isOverworld() is false there, so ITF's depth checks pass everywhere and its
 * zombie/skeleton substitution is unconditional rather than 25%.
 * <p>
 * MITE-ITE adds ghasts, giant zombies and ancient bone lords to the base list and only lets them spawn in the overworld on a blood
 * moon; its conditions let them through everywhere else. Here ghasts are left out (no room in caves) and the other two keep ITE's
 * overworld rule: blood moon only.
 */
public final class TFMiteSpawns {
    private static final Map<List<SpawnListEntry>, List<SpawnListEntry>> MIXED = new IdentityHashMap<>();
    private static final Map<List<SpawnListEntry>, List<SpawnListEntry>> MIXED_BLOOD_MOON = new IdentityHashMap<>();
    private static int mixedPct = -1;

    private TFMiteSpawns() {
    }

    public static synchronized List<SpawnListEntry> withMiteMonsters(List<SpawnListEntry> tfList, World world) {
        int pct = TFConfig.miteMonsterWeightPct;
        if (tfList == null || pct <= 0) {
            return tfList;
        }
        if (pct != mixedPct) {
            MIXED.clear();
            MIXED_BLOOD_MOON.clear();
            mixedPct = pct;
        }
        boolean bloodMoon = world != null && world.isBloodMoon(false);
        return (bloodMoon ? MIXED_BLOOD_MOON : MIXED).computeIfAbsent(tfList, list -> mix(list, pct, bloodMoon));
    }

    @SuppressWarnings("unchecked")
    private static List<SpawnListEntry> mix(List<SpawnListEntry> tfList, int pct, boolean bloodMoon) {
        List<SpawnListEntry> mixed = new ArrayList<>(tfList);
        for (SpawnListEntry mite : (List<SpawnListEntry>) BiomeGenBase.plains.getSpawnableList(EnumCreatureType.monster)) {
            String name = mite.entityClass.getName();
            if (name.startsWith("twilightforest.") || contains(tfList, mite.entityClass) || name.equals("net.minecraft.EntityGhast")) {
                continue;
            }
            if (!bloodMoon && (name.equals("net.minecraft.EntityGiantZombie") || name.equals("net.minecraft.EntityAncientBoneLord"))) {
                continue;
            }
            // other mods' monsters keep their own weight; MITE's are scaled down
            int weight = name.startsWith("net.minecraft.") ? Math.max(1, Math.round(weightOf(mite) * pct / 100.0F)) : weightOf(mite);
            mixed.add(new SpawnListEntry(mite.entityClass, weight, mite.minGroupCount, mite.maxGroupCount));
        }
        return mixed;
    }

    /** WeightedRandomItem.itemWeight is protected; read once per mix, so reflection is fine. */
    private static int weightOf(SpawnListEntry entry) {
        try {
            Field field = WeightedRandomItem.class.getDeclaredField("itemWeight");
            field.setAccessible(true);
            return field.getInt(entry);
        } catch (ReflectiveOperationException e) {
            return 10;
        }
    }

    private static boolean contains(List<SpawnListEntry> list, Class<?> entityClass) {
        for (SpawnListEntry entry : list) {
            if (entry.entityClass == entityClass) {
                return true;
            }
        }
        return false;
    }
}
