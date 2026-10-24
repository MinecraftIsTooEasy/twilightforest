package twilightforest.compat;

import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.Entity;
import net.minecraft.EntityDamageResult;
import net.minecraft.EntityDamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;

/**
 * Creating MITE Damage from TF code. DamageSource constructors and flag setters are protected in MITE, so TF-specific
 * sources are built with {@link #source(String)} / {@link #entitySource(String, Entity, Entity)}.
 */
public final class TFDamage {
    private TFDamage() {
    }

    /** DamageSource with public flag setters. */
    public static class Source extends DamageSource {
        public Source(String type) {
            super(type);
        }

        public Source withBypassMundaneArmor() {
            this.setDamageBypassesMundaneArmor();
            return this;
        }

        public Source unblockable() {
            this.setUnblockable();
            return this;
        }

        public Source absolute() {
            this.setAbsolute();
            return this;
        }

        public Source fire() {
            this.setFireDamage();
            return this;
        }

        public Source magic() {
            this.setMagicAspect();
            return this;
        }

        public Source allowedInCreative() {
            this.setDamageAllowedInCreativeMode();
            return this;
        }
    }

    /** EntityDamageSource with public flag setters. */
    public static class EntitySource extends EntityDamageSource {
        public EntitySource(String type, Entity immediate, Entity responsible) {
            super(type, immediate, responsible);
        }

        public EntitySource withBypassMundaneArmor() {
            this.setDamageBypassesMundaneArmor();
            return this;
        }

        public EntitySource unblockable() {
            this.setUnblockable();
            return this;
        }

        public EntitySource absolute() {
            this.setAbsolute();
            return this;
        }

        public EntitySource fire() {
            this.setFireDamage();
            return this;
        }

        public EntitySource magic() {
            this.setMagicAspect();
            return this;
        }
    }

    /** New named damage source (damageType is the death message key suffix). */
    public static Source source(String type) {
        return new Source(type);
    }

    public static EntitySource entitySource(String type, Entity immediate, Entity responsible) {
        return new EntitySource(type, immediate, responsible);
    }

    public static Damage damage(DamageSource source, float amount) {
        return new Damage(source, amount);
    }

    /** Equivalent of 1.7.10 {@code target.attackEntityFrom(source, amount)}: true if the target was hurt, knocked back or lost armor/held item durability. */
    public static boolean hurt(Entity target, DamageSource source, float amount) {
        EntityDamageResult result = target.attackEntityFrom(new Damage(source, amount));
        return result != null && result.entityWasNegativelyAffected();
    }

    /** {@code target.attackEntityFrom(DamageSource.causeMobDamage(attacker), amount)}. */
    public static boolean hurtByMob(Entity target, EntityLivingBase attacker, float amount) {
        return hurt(target, DamageSource.causeMobDamage(attacker), amount);
    }

    /** {@code target.attackEntityFrom(DamageSource.causePlayerDamage(attacker), amount)}. */
    public static boolean hurtByPlayer(Entity target, EntityPlayer attacker, float amount) {
        return hurt(target, DamageSource.causePlayerDamage(attacker), amount);
    }

    /** {@code target.attackEntityFrom(DamageSource.causeThrownDamage(projectile, shooter), amount)}. */
    public static boolean hurtByThrown(Entity target, Entity projectile, Entity shooter, float amount) {
        return hurt(target, DamageSource.causeThrownDamage(projectile, shooter), amount);
    }

    /** {@code target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(source, shooter), amount)}. */
    public static boolean hurtByMagic(Entity target, Entity immediate, Entity shooter, float amount) {
        return hurt(target, DamageSource.causeIndirectMagicDamage(immediate, shooter), amount);
    }

    /** Damage with {@link DamageSource#generic}. */
    public static boolean hurtGeneric(Entity target, float amount) {
        return hurt(target, DamageSource.generic, amount);
    }
}
