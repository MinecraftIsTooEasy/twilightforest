package twilightforest.entity;

/**
 * Very similar to EntityEggInfo (MITE's own EntityEggInfo is filled in by the FML entity registration as well).
 */
public class TFEntityEggInfo {
    /**
     * The entityID of the spawned mob
     */
    public final int spawnedID;
    /**
     * Base color of the egg
     */
    public final int primaryColor;
    /**
     * Color of the egg spots
     */
    public final int secondaryColor;
    // TODO(mite-port): the 1.7.10 killedStat/killedByStat per-entity statistics are not ported (MITE StatBase needs an int
    // stat id and the stats tables are built before mod entities exist)

    public TFEntityEggInfo(int par1, int par2, int par3) {
        this.spawnedID = par1;
        this.primaryColor = par2;
        this.secondaryColor = par3;
    }
}
