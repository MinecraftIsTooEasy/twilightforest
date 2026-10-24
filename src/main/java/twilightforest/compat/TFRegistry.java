package twilightforest.compat;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Block;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.Item;
import net.minecraft.ItemBlock;
import net.minecraft.ItemMonsterPlacer;
import net.minecraft.ResourceLocation;
import net.minecraft.TileEntity;
import net.xiaoyu233.fml.api.block.IBlock;
import net.xiaoyu233.fml.api.item.IItem;
import net.xiaoyu233.fml.reload.event.EntityRegisterEvent;
import net.xiaoyu233.fml.reload.event.TileEntityRegisterEvent;
import twilightforest.TwilightForestMod;

/**
 * Replacement for GameRegistry / EntityRegistry / Item.itemRegistry. The register* methods must be called from inside
 * the FML registry event handlers; entity and tile entity registration need the event, set it with
 * {@link #beginEntities} / {@link #beginTileEntities} first (or use the overloads taking the event).
 */
public final class TFRegistry {
    public static final String NAMESPACE = "twilightforest";

    /** Tracker parameters recorded for creatures and projectiles (vehicleType is -1 for living entities). */
    public record TrackInfo(Class<? extends Entity> entityClass, String name, int id, int vehicleType, int trackRange, int updateFreq,
                            boolean sendsVelocity) {
    }

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<Class<? extends Entity>, TrackInfo> TRACKED = new LinkedHashMap<>();
    private static final Map<Integer, TrackInfo> BY_VEHICLE_TYPE = new HashMap<>();
    private static EntityRegisterEvent entityEvent;
    private static TileEntityRegisterEvent tileEntityEvent;
    private static Boolean ricPrefixesEntityNames;

    private TFRegistry() {
    }

    public static void beginEntities(EntityRegisterEvent e) {
        entityEvent = e;
    }

    public static void beginTileEntities(TileEntityRegisterEvent e) {
        tileEntityEvent = e;
    }

    /**
     * GameRegistry.registerBlock: names the block, sets its namespace and texture, then builds its ItemBlock by reflection
     * with {@code (Block, extraCtorArgs...)} (plain {@link ItemBlock} when itemClass is null). Does not touch crafting difficulty.
     */
    public static <T extends ItemBlock> T registerBlock(Block block, Class<T> itemClass, String name, Object... extraCtorArgs) {
        block.setUnlocalizedName(name);
        ((IBlock) block).setNamespace(NAMESPACE);
        ((IBlock) block).setBlockTextureName(name);
        BLOCKS.put(name.toLowerCase(Locale.ROOT), block);
        ItemBlock item;
        try {
            item = itemClass == null || itemClass == ItemBlock.class ? new ItemBlock(block) : construct(itemClass, block, extraCtorArgs);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot construct " + itemClass + " for block " + name, e);
        }
        item.setUnlocalizedName(name);
        ((IItem) item).setNamespace(NAMESPACE);
        item.setMaxStackSize(block.getItemStackLimit());
        ITEMS.put(name.toLowerCase(Locale.ROOT), item);
        @SuppressWarnings("unchecked") T result = (T) item;
        return result;
    }

    public static void registerBlock(Block block, String name) {
        registerBlock(block, null, name);
    }

    private static <T extends ItemBlock> T construct(Class<T> cls, Block block, Object[] extra) throws ReflectiveOperationException {
        Object[] args = new Object[extra.length + 1];
        args[0] = block;
        System.arraycopy(extra, 0, args, 1, extra.length);
        for (Constructor<?> c : cls.getConstructors()) {
            Class<?>[] types = c.getParameterTypes();
            if (types.length != args.length) {
                continue;
            }
            boolean ok = true;
            for (int i = 0; i < types.length && ok; i++) {
                ok = args[i] == null ? !types[i].isPrimitive() : wrap(types[i]).isInstance(args[i]);
            }
            if (ok) {
                return cls.cast(c.newInstance(args));
            }
        }
        throw new NoSuchMethodException(cls.getName() + " has no constructor for " + args.length + " args");
    }

    private static Class<?> wrap(Class<?> t) {
        if (!t.isPrimitive()) {
            return t;
        }
        if (t == int.class) return Integer.class;
        if (t == boolean.class) return Boolean.class;
        if (t == float.class) return Float.class;
        if (t == double.class) return Double.class;
        if (t == long.class) return Long.class;
        if (t == byte.class) return Byte.class;
        if (t == short.class) return Short.class;
        return Character.class;
    }

    /** GameRegistry.registerItem (no creative tab). */
    public static void registerItem(Item item, String name) {
        registerItem(item, name, null);
    }

    /** GameRegistry.registerItem with a vanilla creative tab (TF has no tab of its own). */
    public static void registerItem(Item item, String name, CreativeTabs tab) {
        // keep a texture name the item class chose itself ("twilightforest:xyz"); MITE defaults (tools/x, food/x, nuggets/x) have no domain
        String own = iconStringOf(item);
        if (item instanceof ItemMonsterPlacer) {
            // vanilla spawn_egg + spawn_egg_overlay
        } else if (own == null || own.indexOf(':') < 0) {
            ((IItem) item).setItemTextureName(NAMESPACE + ":" + name);
        }
        item.setUnlocalizedName(name);
        ((IItem) item).setNamespace(NAMESPACE);
        if (tab != null) {
            item.setCreativeTab(tab);
        }
        ITEMS.put(name.toLowerCase(Locale.ROOT), item);
    }

    private static final java.lang.reflect.Field ICON_STRING_FIELD = findIconStringField();

    private static java.lang.reflect.Field findIconStringField() {
        try {
            java.lang.reflect.Field f = Item.class.getDeclaredField("iconString");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    /** Item.iconString is private and has no getter. */
    private static String iconStringOf(Item item) {
        try {
            return ICON_STRING_FIELD == null ? null : (String) ICON_STRING_FIELD.get(item);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    /** GameRegistry.registerTileEntity; the id string is the NBT "id". */
    public static void registerTileEntity(Class<? extends TileEntity> c, String id) {
        if (tileEntityEvent == null) {
            throw new IllegalStateException("TFRegistry.beginTileEntities was not called");
        }
        registerTileEntity(tileEntityEvent, c, id);
    }

    public static void registerTileEntity(TileEntityRegisterEvent e, Class<? extends TileEntity> c, String id) {
        e.register(c, id);
    }

    /** EntityRegistry.registerModEntity for living/spawnable entities; eggA &lt; 0 registers without a spawn egg. */
    public static void registerCreature(Class<? extends Entity> c, String name, int id, int eggA, int eggB, int trackRange, int updateFreq,
                                        boolean velocity) {
        if (entityEvent == null) {
            throw new IllegalStateException("TFRegistry.beginEntities was not called");
        }
        registerCreature(entityEvent, c, name, id, eggA, eggB, trackRange, updateFreq, velocity);
    }

    public static void registerCreature(EntityRegisterEvent e, Class<? extends Entity> c, String name, int id, int eggA, int eggB,
                                        int trackRange, int updateFreq, boolean velocity) {
        String entityName = ricPrefixesEntityNames() ? name : NAMESPACE + ":" + name;
        if (eggA < 0) {
            e.register(c, NAMESPACE, entityName, id);
        } else {
            e.register(c, NAMESPACE, entityName, id, eggA, eggB);
        }
        track(new TrackInfo(c, name, id, -1, trackRange, updateFreq, velocity));
    }

    /**
     * FML's own EntityRegisterEvent maps the name as given, but RustedIronCore 1.5.9+ posts the event itself and registers
     * {@code new ResourceLocation(namespace, name)}, which would turn "twilightforest:wild_deer" into
     * "twilightforest:twilightforest:wild_deer". Its RicRegistry.registerEntity only exists in those versions.
     */
    private static boolean ricPrefixesEntityNames() {
        if (ricPrefixesEntityNames == null) {
            boolean prefixes;
            try {
                Class.forName("moddedmite.rustedironcore.api.RicRegistry").getMethod("registerEntity", Class.class,
                        ResourceLocation.class, int.class);
                prefixes = true;
            } catch (ReflectiveOperationException | LinkageError ex) {
                prefixes = false;
            }
            ricPrefixesEntityNames = prefixes;
        }
        return ricPrefixesEntityNames;
    }

    /**
     * Records tracker data for a non-living entity (vehicle spawn type 100..127). The entity itself is registered by the
     * entity unit (TFEntitySpawnRegistry); this only fills the lookup tables.
     */
    public static void registerProjectile(Class<? extends Entity> c, String name, int vehicleType, int trackRange, int updateFreq, boolean velocity) {
        track(new TrackInfo(c, name, -1, vehicleType, trackRange, updateFreq, velocity));
    }

    private static void track(TrackInfo info) {
        TRACKED.put(info.entityClass(), info);
        if (info.vehicleType() >= 0) {
            BY_VEHICLE_TYPE.put(info.vehicleType(), info);
        }
    }

    /** Tracker data of a registered TF entity class, or null. */
    public static TrackInfo getTrackInfo(Class<? extends Entity> c) {
        return TRACKED.get(c);
    }

    /** Tracker data by Packet23VehicleSpawn type (100..127), or null. */
    public static TrackInfo getTrackInfoByVehicleType(int type) {
        return BY_VEHICLE_TYPE.get(type);
    }

    public static Map<Class<? extends Entity>, TrackInfo> allTracked() {
        return Collections.unmodifiableMap(TRACKED);
    }

    /**
     * Item by name: TF/registered name first, then the unlocalized name of any item or block item without its
     * "item."/"tile." prefix (e.g. "diamond"), accepting "minecraft:"/"twilightforest:" prefixes and numeric ids. Null if unknown.
     */
    public static Item findItem(String name) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return null;
        }
        Item registered = ITEMS.get(key);
        if (registered != null) {
            return registered;
        }
        if (key.chars().allMatch(Character::isDigit)) {
            return Item.getItem(Integer.parseInt(key));
        }
        for (Item item : Item.itemsList) {
            if (item != null && key.equals(stripPrefix(item.getUnlocalizedName()))) {
                return item;
            }
        }
        Block block = findBlock(name);
        return block == null ? null : Item.getItem(block);
    }

    /** Block by registered name or unlocalized name without "tile." prefix; null if unknown. */
    public static Block findBlock(String name) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return null;
        }
        Block registered = BLOCKS.get(key);
        if (registered != null) {
            return registered;
        }
        if (key.chars().allMatch(Character::isDigit)) {
            return Block.getBlock(Integer.parseInt(key));
        }
        for (Block block : Block.blocksList) {
            if (block != null && key.equals(stripPrefix(block.getUnlocalizedName()))) {
                return block;
            }
        }
        return null;
    }

    private static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String s = name.trim().toLowerCase(Locale.ROOT);
        int colon = s.indexOf(':');
        return colon >= 0 ? s.substring(colon + 1) : s;
    }

    private static String stripPrefix(String unlocalized) {
        if (unlocalized == null) {
            return "";
        }
        String s = unlocalized.toLowerCase(Locale.ROOT);
        if (s.startsWith("item.")) {
            return s.substring(5);
        }
        if (s.startsWith("tile.")) {
            return s.substring(5);
        }
        return s;
    }

    /** Logs a registration summary (debug aid). */
    public static void logSummary() {
        TwilightForestMod.LOGGER.info("TFRegistry: {} blocks, {} items, {} tracked entities", BLOCKS.size(), ITEMS.size(), TRACKED.size());
    }
}
