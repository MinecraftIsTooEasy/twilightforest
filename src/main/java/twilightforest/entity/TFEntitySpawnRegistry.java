package twilightforest.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;
import net.minecraft.Entity;
import net.minecraft.EntityArrow;
import net.minecraft.EntityThrowable;
import net.minecraft.Item;
import net.minecraft.ItemArrow;
import net.minecraft.Packet23VehicleSpawn;
import net.minecraft.World;
import twilightforest.TFIds;
import twilightforest.entity.boss.EntityTFFallingIce;
import twilightforest.entity.boss.EntityTFHydraHead;
import twilightforest.entity.boss.EntityTFHydraMortar;
import twilightforest.entity.boss.EntityTFIceBomb;
import twilightforest.entity.boss.EntityTFLichBolt;
import twilightforest.entity.boss.EntityTFLichBomb;
import twilightforest.entity.boss.EntityTFThrownAxe;
import twilightforest.entity.boss.EntityTFThrownPick;
import twilightforest.network.ITFSpawnData;

/**
 * Replacement for EntityRegistry.registerModEntity of the non-living TF entities (TwilightForestMod.java:447-466).
 * MITE's EntityTracker / EntityTrackerEntry / NetClientHandler have hard-coded instanceof chains, so the mixins in
 * twilightforest.mixin.entity consult this table: tracking range and frequency, the Packet23VehicleSpawn type
 * (100..127, fixed in {@link TFIds}) and the client-side factory.
 *
 * Hydra decision: EntityTFHydraHead extends EntityLiving but is not IAnimals, so MITE neither tracks it nor can build
 * a Packet24MobSpawn for it. It is therefore tracked here like a projectile (type VEHICLE_HYDRA_HEAD); the client
 * builds a bare head whose part name and state arrive through the DataWatcher (Packet40 is sent when tracking
 * starts), and HydraHeadContainer.findNearbyHead re-links head.hydraObj by name. The necks are never added to the
 * world and the DragonPart bodies travel with EntityTFHydra (an IMob) through the normal mob spawn path.
 */
public final class TFEntitySpawnRegistry {
    private TFEntitySpawnRegistry() {}

    /** Client-side constructor: {@code data} is the raw throwerEntityId of the spawn packet. */
    @FunctionalInterface
    public interface ClientFactory {
        Entity create(World world, double x, double y, double z, int data);
    }

    /**
     * @param forcePositionSync arrows never get location packets in MITE's tracker, so entities flagged here get a
     *                          teleport + velocity packet every {@code freq} ticks while they move
     * @param data              value for Packet23VehicleSpawn.throwerEntityId; a result <= 0 falls back to the entity id
     *                          (it must stay positive so MITE sends the motion with the spawn packet)
     */
    public record Spec(int type, int range, int freq, boolean sendVelocity, boolean forcePositionSync,
                       ToIntFunction<Entity> data, ClientFactory factory) {}

    private static final Spec NONE = new Spec(-1, 0, 1, false, false, null, null);
    private static final Spec[] BY_TYPE = new Spec[128];
    private static final Map<Class<?>, Spec> BY_CLASS = new HashMap<>();
    private static final Map<Class<?>, Spec> RESOLVED = new ConcurrentHashMap<>();

    private static final ToIntFunction<Entity> THROWER_OR_SELF = e ->
        e instanceof EntityThrowable throwable && throwable.getThrower() != null ? throwable.getThrower().entityId : e.entityId;
    private static final ToIntFunction<Entity> SELF = e -> e.entityId;

    private static Entity at(Entity entity, double x, double y, double z) {
        entity.setPosition(x, y, z);
        return entity;
    }

    private static void register(int type, Class<? extends Entity> cls, int range, int freq, boolean velocity,
                                 ToIntFunction<Entity> data, ClientFactory factory) {
        register(type, cls, range, freq, velocity, false, data, factory);
    }

    private static void register(int type, Class<? extends Entity> cls, int range, int freq, boolean velocity,
                                 boolean forcePositionSync, ToIntFunction<Entity> data, ClientFactory factory) {
        Spec spec = new Spec(type, range, freq, velocity, forcePositionSync, data, factory);
        BY_TYPE[type] = spec;
        BY_CLASS.put(cls, spec);
    }

    static {
        // type, class, trackingRange, updateFrequency, sendsVelocityUpdates: the TF 1.7.10 registerModEntity values
        register(TFIds.VEHICLE_HYDRA_HEAD, EntityTFHydraHead.class, 150, 3, false, SELF,
            (w, x, y, z, d) -> at(new EntityTFHydraHead(w), x, y, z));
        register(TFIds.VEHICLE_NATURE_BOLT, EntityTFNatureBolt.class, 150, 5, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFNatureBolt(w), x, y, z));
        register(TFIds.VEHICLE_LICH_BOLT, EntityTFLichBolt.class, 150, 2, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFLichBolt(w), x, y, z));
        register(TFIds.VEHICLE_TWILIGHT_WAND_BOLT, EntityTFTwilightWandBolt.class, 150, 5, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFTwilightWandBolt(w), x, y, z));
        register(TFIds.VEHICLE_TOME_BOLT, EntityTFTomeBolt.class, 150, 5, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFTomeBolt(w), x, y, z));
        register(TFIds.VEHICLE_HYDRA_MORTAR, EntityTFHydraMortar.class, 150, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFHydraMortar(w), x, y, z));
        register(TFIds.VEHICLE_LICH_BOMB, EntityTFLichBomb.class, 150, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFLichBomb(w), x, y, z));
        register(TFIds.VEHICLE_MOONWORM_SHOT, EntityTFMoonwormShot.class, 150, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFMoonwormShot(w), x, y, z));
        register(TFIds.VEHICLE_SLIME_BLOB, EntityTFSlimeProjectile.class, 150, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFSlimeProjectile(w), x, y, z));
        register(TFIds.VEHICLE_CHARM_EFFECT, EntityTFCharmEffect.class, 80, 3, true, SELF,
            (w, x, y, z, d) -> at(new EntityTFCharmEffect(w), x, y, z));
        register(TFIds.VEHICLE_THROWN_AXE, EntityTFThrownAxe.class, 80, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFThrownAxe(w), x, y, z));
        register(TFIds.VEHICLE_THROWN_PICK, EntityTFThrownPick.class, 80, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFThrownPick(w), x, y, z));
        register(TFIds.VEHICLE_FALLING_ICE, EntityTFFallingIce.class, 80, 3, true, SELF,
            (w, x, y, z, d) -> at(new EntityTFFallingIce(w), x, y, z));
        register(TFIds.VEHICLE_THROWN_ICE, EntityTFIceBomb.class, 80, 2, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFIceBomb(w), x, y, z));
        // The arrow item must reach the client (RenderArrow reads item_arrow), packed as itemID | enchanted << 16.
        register(TFIds.VEHICLE_SEEKER_ARROW, EntitySeekerArrow.class, 150, 1, true, true,
            e -> e instanceof EntityArrow arrow ? packArrow(arrow) : e.entityId,
            (w, x, y, z, d) -> {
                EntitySeekerArrow arrow = new EntitySeekerArrow(w);
                arrow.item_arrow = unpackArrowItem(d);
                arrow.launcher_was_enchanted = (d >> 16 & 1) != 0;
                return at(arrow, x, y, z);
            });
        register(TFIds.VEHICLE_ICE_SNOWBALL, EntityTFIceSnowball.class, 150, 3, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFIceSnowball(w), x, y, z));
        register(TFIds.VEHICLE_CHAIN_BLOCK, EntityTFChainBlock.class, 80, 1, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFChainBlock(w), x, y, z));
        register(TFIds.VEHICLE_CUBE_OF_ANNIHILATION, EntityTFCubeOfAnnihilation.class, 80, 1, true, THROWER_OR_SELF,
            (w, x, y, z, d) -> at(new EntityTFCubeOfAnnihilation(w), x, y, z));
        register(TFIds.VEHICLE_SLIDE_BLOCK, EntityTFSlideBlock.class, 80, 1, true,
            e -> e instanceof ITFSpawnData spawnData ? spawnData.tf$writeSpawnData() : 0,
            (w, x, y, z, d) -> {
                EntityTFSlideBlock block = new EntityTFSlideBlock(w);
                block.tf$readSpawnData(d);
                return at(block, x, y, z);
            });
    }

    private static int packArrow(EntityArrow arrow) {
        Item item = arrow.item_arrow != null ? arrow.item_arrow : Item.arrowFlint;
        return item.itemID | (arrow.launcher_was_enchanted ? 1 << 16 : 0);
    }

    private static ItemArrow unpackArrowItem(int data) {
        int id = data & 0xFFFF;
        return id > 0 && id < Item.itemsList.length && Item.itemsList[id] instanceof ItemArrow arrow ? arrow : Item.arrowFlint;
    }

    /** The spec of the entity's class or of the nearest registered superclass; null for everything that is not ours. */
    public static Spec forEntity(Entity entity) {
        Class<?> cls = entity.getClass();
        Spec spec = RESOLVED.get(cls);
        if (spec == null) {
            spec = NONE;
            for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
                Spec found = BY_CLASS.get(c);
                if (found != null) {
                    spec = found;
                    break;
                }
            }
            RESOLVED.put(cls, spec);
        }
        return spec == NONE ? null : spec;
    }

    public static Spec forType(int type) {
        return type >= 0 && type < BY_TYPE.length ? BY_TYPE[type] : null;
    }

    /** Builds the spawn packet for a tracked entity of this registry. */
    public static Packet23VehicleSpawn createSpawnPacket(Entity entity, Spec spec) {
        int data = spec.data().applyAsInt(entity);
        return new Packet23VehicleSpawn(entity, spec.type(), data > 0 ? data : entity.entityId);
    }
}
