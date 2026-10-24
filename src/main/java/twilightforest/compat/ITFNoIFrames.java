package twilightforest.compat;

/** Implemented (by mixin) on projectiles whose hits must not trigger invulnerability frames (Triple Bow side arrows). */
public interface ITFNoIFrames {
    boolean tf$isNoIFrames();

    void tf$setNoIFrames(boolean value);
}
