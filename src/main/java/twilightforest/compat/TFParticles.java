package twilightforest.compat;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.EnumParticle;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

/**
 * 1.7.10 string particles on MITE. TF-specific particles are client-only and go through
 * {@code TwilightForestMod.proxy.spawnParticle}; everything vanilla maps to {@link EnumParticle}.
 * Safe to call on the server (the server world has no listener that renders particles).
 */
public final class TFParticles {
    /** The 11 TF-specific particle names handled by TFClientProxy. */
    private static final Set<String> TF_PARTICLES = Set.of("largeflame", "hugesmoke", "leafrune", "bosstear", "ghasttrap",
        "protection", "snowstuff", "snowwarning", "snowguardian", "icebeam", "annihilate");

    private static final Map<String, EnumParticle> VANILLA = Map.ofEntries(
        Map.entry("mobspell", EnumParticle.mobSpell),
        Map.entry("mobspellambient", EnumParticle.mobSpellAmbient),
        Map.entry("flame", EnumParticle.flame),
        Map.entry("smoke", EnumParticle.smoke),
        Map.entry("reddust", EnumParticle.reddust),
        Map.entry("largesmoke", EnumParticle.largesmoke),
        Map.entry("splash", EnumParticle.splash),
        Map.entry("slime", EnumParticle.slime),
        Map.entry("crit", EnumParticle.crit),
        Map.entry("magiccrit", EnumParticle.magicCrit),
        Map.entry("spell", EnumParticle.spell),
        Map.entry("instantspell", EnumParticle.instantSpell),
        Map.entry("witchmagic", EnumParticle.witchMagic),
        Map.entry("snowballpoof", EnumParticle.snowballpoof),
        Map.entry("lava", EnumParticle.lava),
        Map.entry("explode", EnumParticle.explode),
        Map.entry("largeexplode", EnumParticle.largeexplode),
        Map.entry("hugeexplosion", EnumParticle.hugeexplosion),
        Map.entry("townaura", EnumParticle.townaura),
        Map.entry("note", EnumParticle.note),
        Map.entry("happyvillager", EnumParticle.happyVillager),
        Map.entry("angryvillager", EnumParticle.angryVillager),
        Map.entry("enchantmenttable", EnumParticle.enchantmenttable),
        Map.entry("cloud", EnumParticle.cloud),
        Map.entry("bubble", EnumParticle.bubble),
        Map.entry("suspended", EnumParticle.suspended),
        Map.entry("depthsuspend", EnumParticle.depthsuspend),
        Map.entry("fireworksspark", EnumParticle.fireworkSpark),
        Map.entry("footstep", EnumParticle.footstep),
        Map.entry("dripwater", EnumParticle.dripWater),
        Map.entry("driplava", EnumParticle.dripLava),
        Map.entry("snowshovel", EnumParticle.snowshovel),
        Map.entry("heart", EnumParticle.heart));

    private TFParticles() {
    }

    /** True for the TF-specific particle names (client proxy handles them). */
    public static boolean isTFParticle(String name) {
        return TF_PARTICLES.contains(name);
    }

    /** Equivalent of 1.7.10 {@code world.spawnParticle(String, ...)} / {@code proxy.spawnParticle} for every particle TF uses. */
    public static void spawn(World world, String name, double x, double y, double z, double vx, double vy, double vz) {
        if (isTFParticle(name)) {
            if (TwilightForestMod.proxy != null) {
                TwilightForestMod.proxy.spawnParticle(world, name, x, y, z, vx, vy, vz);
            }
            return;
        }
        spawnVanilla(world, name, x, y, z, vx, vy, vz);
    }

    /** Vanilla part only (what the client proxy falls back to); returns false if the name is unknown. */
    public static boolean spawnVanilla(World world, String name, double x, double y, double z, double vx, double vy, double vz) {
        if (name.startsWith("iconcrack_") || name.startsWith("blockcrack_")) {
            String[] parts = name.split("_");
            try {
                int id = Integer.parseInt(parts[1]);
                int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                world.spawnParticleEx(parts[0].equals("iconcrack") ? EnumParticle.iconcrack : EnumParticle.tilecrack, id, meta, x, y, z, vx, vy, vz);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (name.equals("portal")) {
            // always the purple swirl (EntityPortalFX type 0): portal_nether is the red/orange nether portal look, which TF portals in the overworld
            // or in a hell-flagged dimension must never show
            world.spawnParticle(EnumParticle.portal_underworld, x, y, z, vx, vy, vz);
            return true;
        }
        EnumParticle p = VANILLA.get(name.toLowerCase(Locale.ROOT));
        if (p == null) {
            TwilightForestMod.LOGGER.debug("Unknown particle {}", name);
            return false;
        }
        world.spawnParticle(p, x, y, z, vx, vy, vz);
        return true;
    }

    /** Item break particle (1.7.10 "iconcrack_id_meta"). */
    public static void spawnItemCrack(World world, int itemId, int meta, double x, double y, double z, double vx, double vy, double vz) {
        world.spawnParticleEx(EnumParticle.iconcrack, itemId, meta, x, y, z, vx, vy, vz);
    }

    /** Block break particle (1.7.10 "blockcrack_id_meta"). */
    public static void spawnBlockCrack(World world, int blockId, int meta, double x, double y, double z, double vx, double vy, double vz) {
        world.spawnParticleEx(EnumParticle.tilecrack, blockId, meta, x, y, z, vx, vy, vz);
    }
}
