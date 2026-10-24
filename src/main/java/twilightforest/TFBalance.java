package twilightforest;

import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.WorldServer;
import net.minecraft.server.MinecraftServer;
import twilightforest.dimension.TFDimension;

/** Small helpers shared by the balance mixins and handlers (MITE_BALANCE sections 3, 4, 5). */
public final class TFBalance {
    /** Set while /tf stuck moves a player, so the teleporter drops them at the global spawn without building a portal. */
    public static boolean stuckTransfer;

    private TFBalance() {
    }

    public static WorldServer overworld() {
        MinecraftServer server = MinecraftServer.getServer();
        return server == null || server.worldServers == null ? null : server.worldServers[0];
    }

    /** Time source of cooldowns and day gates: always the overworld clock (H14). */
    public static long overworldTime() {
        WorldServer world = overworld();
        return world == null ? 0L : world.getTotalWorldTime();
    }

    public static int overworldDay() {
        WorldServer world = overworld();
        return world == null ? 0 : world.getDayOfWorld();
    }

    /** Spawn cap of one spawner; wraith cages hold fewer. */
    public static int maxSpawnsFor(String mobName) {
        if (TFTestMode.isOn()) {
            return Integer.MAX_VALUE;
        }
        int max = TFConfig.spawnerMaxSpawns;
        if (mobName != null && mobName.toLowerCase().contains("wraith")) {
            return Math.min(8, max);
        }
        return max;
    }

    /** Experience of a dying mob: spawner mobs of the forest give spawner.xpMult (at least 1), TF mobs mob.xpMult. */
    public static int scaleMobXp(EntityLivingBase mob, int xp, boolean fromSpawner) {
        if (xp <= 0 || !TFDimension.is(mob.worldObj)) {
            return xp;
        }
        double scaled = xp;
        if (mob.getClass().getName().startsWith("twilightforest.entity.")) {
            scaled *= TFConfig.mobXpMult;
        }
        if (fromSpawner) {
            scaled *= TFConfig.spawnerXpMult;
        }
        return Math.max(scaled > 0.0D ? 1 : 0, (int) scaled);
    }

    /** Any entity of the port (mobs, bosses, projectiles) by package. */
    public static boolean isTFEntity(Entity entity) {
        return entity != null && entity.getClass().getName().startsWith("twilightforest.entity.");
    }

    /** The eight bosses (shadow clones and minions are ordinary mobs). */
    public static boolean isBoss(Entity entity) {
        return entity instanceof twilightforest.entity.boss.EntityTFNaga || entity instanceof twilightforest.entity.boss.EntityTFLich l && !l.isShadowClone()
                || entity instanceof twilightforest.entity.boss.EntityTFMinoshroom || entity instanceof twilightforest.entity.boss.EntityTFHydra
                || entity instanceof twilightforest.entity.boss.EntityTFKnightPhantom || entity instanceof twilightforest.entity.boss.EntityTFUrGhast
                || entity instanceof twilightforest.entity.boss.EntityTFYetiAlpha || entity instanceof twilightforest.entity.boss.EntityTFSnowQueen;
    }

    /** Bosses and their summoned minions ignore the mirrored blood moon frenzy unless mirrorBloodMoonBosses is on (E11, H7). */
    public static boolean frenzyExempt(Entity entity) {
        if (TFConfig.mirrorBloodMoonBosses) {
            return false;
        }
        return isBoss(entity) || entity instanceof twilightforest.entity.boss.EntityTFLichMinion
                || entity instanceof twilightforest.entity.boss.EntityTFLich;
    }

    /** mob.dayScaling.max: 1 + max * min(day, 366) / 366 for the common TF mobs, 1 when off. */
    public static double dayScale() {
        double max = TFConfig.mobDayScalingMax;
        return max <= 0.0D ? 1.0D : 1.0D + max * Math.min(overworldDay(), 366) / 366.0D;
    }

    /**
     * Global stat tuning without touching entity attributes: damage dealt by TF mobs is multiplied by mob.damageMult (and the
     * day scale for common mobs), damage taken by a TF mob is divided by mob.hpMult / boss.hpMult (and the day scale), and a
     * single hit on a boss is capped at max(boss.singleHitCapPct of its max health, 40). Called with the final damage.
     */
    public static float tuneDamage(EntityLivingBase target, Entity attacker, float amount) {
        if (amount <= 0.0F) {
            return amount;
        }
        double scaled = amount;
        if (isTFEntity(attacker)) {
            scaled *= TFConfig.mobDamageMult;
            if (!isBoss(attacker) && attacker instanceof EntityLivingBase && TFDimension.is(attacker.worldObj)) {
                scaled *= dayScale();
            }
        }
        if (isTFEntity(target)) {
            if (isBoss(target)) {
                scaled /= TFConfig.bossHpMult;
                if (TFConfig.bossSingleHitCapPct > 0.0D) {
                    scaled = Math.min(scaled, Math.max(target.getMaxHealth() * TFConfig.bossSingleHitCapPct / 100.0D, 40.0D));
                }
            } else {
                // mobs a difficulty mod already buffs through a vanilla parent only take the configured factor
                scaled /= twilightforest.compat.TFModCompat.profileHpExempt(target) ? TFConfig.mobHpMultConfig : TFConfig.mobHpMult;
                if (TFDimension.is(target.worldObj)) {
                    scaled /= dayScale();
                }
            }
        }
        return (float) scaled;
    }
}
