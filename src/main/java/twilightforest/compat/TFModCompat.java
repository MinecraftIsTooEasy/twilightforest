package twilightforest.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatMessageComponent;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumChatFormatting;
import net.xiaoyu233.fml.FishModLoader;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;

/**
 * Detects the MITE difficulty mods next to the port. They decide the portal item of portal.items = auto (diamond instead of
 * the copper ingot) and the difficulty profile of compat.profile = auto. Detection only reads the loader's mod list, so the
 * answer is fixed from the moment FishModLoader has resolved the mods and identical on client and server.
 */
public final class TFModCompat {
    /** Mods the port knows. hard: a difficulty mod (diamond portal item). */
    public enum KnownMod {
        MITE_ITE("mite_ite", "MITE-ITE", true),
        EXTREME("extreme", "Extreme", true),
        BEYOND_EXTREME("bex", "Beyond Extreme", true),
        // no generic mob/weapon scaling by default (FinalChallenge is opt-in); the denser hostile cap also reaches the forest
        ITF_REBORN("mite-itf-reborn", "MITE-ITF-Reborn", false),
        BETTER_GAME_SETTING("better_game_setting", "BetterGameSetting", false);

        public final String modId;
        public final String displayName;
        public final boolean hard;

        KnownMod(String modId, String displayName, boolean hard) {
            this.modId = modId;
            this.displayName = displayName;
            this.hard = hard;
        }
    }

    /**
     * Difficulty profile matching the installed mods (multipliers on top of the mob/boss/items config values). Numbers from
     * the decompiled mods: ITE gives vanilla hostiles x1.7 HP at day 1 growing to x3.3 at day 256 and x1.4..x2.7 damage, but
     * whitelists vanilla classes, so TF mobs are not scaled by it; TF weapons get ITE tool levels but no forging grade
     * (+21..55 % on forged vanilla swords). Extreme and Beyond Extreme add no generic scaling on top of ITE, but Extreme gems
     * give the player up to +6 armor and +24 health. ITF-Reborn and BetterGameSetting leave mob and weapon numbers alone.
     */
    public enum Profile {
        //    id         mobHp boss  mobDmg weapon dayScalingMax
        NONE("none",     1.0D, 1.0D, 1.0D,  1.0D,  0.0D),
        ITE("ite",       1.6D, 1.5D, 1.25D, 1.25D, 1.4D),
        EXTREME("extreme", 1.6D, 1.5D, 1.4D, 1.25D, 1.4D);

        public final String id;
        public final double mobHpMult;
        public final double bossHpMult;
        public final double mobDamageMult;
        public final double weaponDamageMult;
        /** Added to mob.dayScaling.max (TFBalance.dayScale). */
        public final double dayScalingMax;

        Profile(String id, double mobHpMult, double bossHpMult, double mobDamageMult, double weaponDamageMult, double dayScalingMax) {
            this.id = id;
            this.mobHpMult = mobHpMult;
            this.bossHpMult = bossHpMult;
            this.mobDamageMult = mobDamageMult;
            this.weaponDamageMult = weaponDamageMult;
            this.dayScalingMax = dayScalingMax;
        }

        public static Profile byId(String id) {
            for (Profile profile : values()) {
                if (profile.id.equalsIgnoreCase(id)) {
                    return profile;
                }
            }
            return null;
        }
    }

    private static List<KnownMod> detected;
    private static Profile profile;

    private TFModCompat() {
    }

    /** The known mods that are loaded, in declaration order. */
    public static List<KnownMod> detected() {
        if (detected == null) {
            List<KnownMod> found = new ArrayList<>();
            for (KnownMod mod : KnownMod.values()) {
                if (FishModLoader.hasMod(mod.modId)) {
                    found.add(mod);
                }
            }
            detected = Collections.unmodifiableList(found);
            if (!found.isEmpty()) {
                TwilightForestMod.LOGGER.info("Twilight Forest compat: detected {}", found);
            }
        }
        return detected;
    }

    public static boolean isLoaded(KnownMod mod) {
        return detected().contains(mod);
    }

    /** A difficulty mod is installed: portal.items = auto opens the portal with a diamond. */
    public static boolean hardModPresent() {
        for (KnownMod mod : detected()) {
            if (mod.hard) {
                return true;
            }
        }
        return false;
    }

    /** The profile compat.profile = auto picks for the installed mods. */
    public static Profile detectedProfile() {
        if (isLoaded(KnownMod.EXTREME) || isLoaded(KnownMod.BEYOND_EXTREME)) {
            return Profile.EXTREME;
        }
        return isLoaded(KnownMod.MITE_ITE) ? Profile.ITE : Profile.NONE;
    }

    /** Active profile: compat.profile, auto resolving to {@link #detectedProfile()}. Fixed once read (weapon damage is baked into the items). */
    public static Profile profile() {
        if (profile == null) {
            Profile configured = Profile.byId(TFConfig.compatProfile);
            if (configured == null && !"auto".equalsIgnoreCase(TFConfig.compatProfile)) {
                TwilightForestMod.LOGGER.warn("Invalid compat.profile '{}', using auto", TFConfig.compatProfile);
            }
            profile = configured != null ? configured : detectedProfile();
            TwilightForestMod.LOGGER.info("Twilight Forest difficulty profile: {} (mob hp x{}, boss hp x{}, mob damage x{}, weapons x{}, day scaling +{})",
                profile.id, profile.mobHpMult, profile.bossHpMult, profile.mobDamageMult, profile.weaponDamageMult, profile.dayScalingMax);
        }
        return profile;
    }

    /**
     * Mobs that ITE already buffs through a vanilla parent (its EntityZombie formula, or the EntityMob x2 default for a mob
     * that never sets its health), so the profile health factor must not stack on them.
     */
    public static boolean profileHpExempt(Entity entity) {
        return isLoaded(KnownMod.MITE_ITE)
            && (entity instanceof twilightforest.entity.boss.EntityTFLichMinion || entity instanceof twilightforest.entity.EntityTFIceExploder
                || entity instanceof twilightforest.entity.EntityTFIceShooter);
    }

    /** Login lines naming the detected difficulty mods and the active profile; nothing when no profile applies. */
    public static void sendProfileHint(EntityPlayer player) {
        StringBuilder names = new StringBuilder();
        for (KnownMod mod : detected()) {
            if (mod.hard) {
                names.append(names.length() == 0 ? "" : ", ").append(mod.displayName);
            }
        }
        Profile active = profile();
        if (names.length() == 0 && active == Profile.NONE) {
            return;
        }
        if (active == Profile.NONE) {
            player.sendChatToPlayer(TFChat.translation("tf.compat.none", names.toString()).setColor(EnumChatFormatting.GOLD));
            return;
        }
        // set by hand in compat.profile: name the config instead of mods that were not what chose it
        boolean forced = !"auto".equalsIgnoreCase(TFConfig.compatProfile);
        ChatMessageComponent source = forced ? TFChat.translation("tf.compat.forced") : TFChat.translation("tf.compat.detectedBy", names.toString());
        player.sendChatToPlayer(TFChat.translation("tf.compat.profile", TFChat.translation("tf.compat.profile." + active.id), source,
            fmt(TFConfig.mobHpMult), fmt(TFConfig.bossHpMult), fmt(TFConfig.mobDamageMult), fmt(active.weaponDamageMult),
            fmt(1.0D + TFConfig.mobDayScalingMax)).setColor(EnumChatFormatting.GOLD));
    }

    private static String fmt(double value) {
        return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
