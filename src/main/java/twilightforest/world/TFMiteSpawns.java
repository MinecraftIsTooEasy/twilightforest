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
import twilightforest.TFConfig;

/**
 * MITE's own underground monsters (ghouls, wights, shadows, revenants, jellies, bone lords, ...) for the hollow hills and the caves
 * below sea level. They come from MITE's base biome list and keep their vanilla stats: TFBalance only tunes twilightforest.entity
 * mobs. Their weights are spawn.miteMonsterWeightPct percent of MITE's, so the TF list stays the bulk of the spawns; monsters the
 * TF list already has keep their TF weight. Each mob's own getCanSpawnHere (light, depth) still decides.
 */
public final class TFMiteSpawns {
    private static final Map<List<SpawnListEntry>, List<SpawnListEntry>> MIXED = new IdentityHashMap<>();
    private static int mixedPct = -1;

    private TFMiteSpawns() {
    }

    public static synchronized List<SpawnListEntry> withMiteMonsters(List<SpawnListEntry> tfList) {
        int pct = TFConfig.miteMonsterWeightPct;
        if (tfList == null || pct <= 0) {
            return tfList;
        }
        if (pct != mixedPct) {
            MIXED.clear();
            mixedPct = pct;
        }
        return MIXED.computeIfAbsent(tfList, list -> mix(list, pct));
    }

    @SuppressWarnings("unchecked")
    private static List<SpawnListEntry> mix(List<SpawnListEntry> tfList, int pct) {
        List<SpawnListEntry> mixed = new ArrayList<>(tfList);
        for (SpawnListEntry mite : (List<SpawnListEntry>) BiomeGenBase.plains.getSpawnableList(EnumCreatureType.monster)) {
            if (!mite.entityClass.getName().startsWith("net.minecraft.") || contains(tfList, mite.entityClass)) {
                continue;
            }
            int weight = Math.max(1, Math.round(weightOf(mite) * pct / 100.0F));
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
