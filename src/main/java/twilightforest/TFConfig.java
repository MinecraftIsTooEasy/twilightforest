package twilightforest;

import net.xiaoyu233.fml.config.Config;
import net.xiaoyu233.fml.config.ConfigCategory;
import net.xiaoyu233.fml.config.ConfigEntry;
import net.xiaoyu233.fml.config.ConfigRegistry;
import net.xiaoyu233.fml.config.ConfigRoot;
import net.xiaoyu233.fml.util.FieldReference;

import java.io.File;

/**
 * FML config backing the 1.7.10 option fields on {@link TwilightForestMod} plus the balance options of MITE_BALANCE section 13.
 * Biome and mob ids are fixed in {@link TFIds} and no longer configurable. Written to twilightforest.json. Raw values are
 * clamped in {@link #apply()} (ConfigEntry.ranged is not usable); read the clamped static fields, never the references.
 */
public final class TFConfig {
    public static final FieldReference<Integer> DIMENSION_ID = new FieldReference<>(TFIds.DIMENSION);

    public static final FieldReference<Boolean> SILENT_CICADAS = new FieldReference<>(false);
    public static final FieldReference<String> TWILIGHT_FOREST_SEED = new FieldReference<>("");
    public static final FieldReference<Boolean> DISABLE_UNCRAFTING = new FieldReference<>(false);
    public static final FieldReference<Boolean> OLD_MAP_GEN = new FieldReference<>(false);

    /** Developer commands (/twilightforest tp|give|dump): usable by every player when true (otherwise dev mode / admins only). */
    public static final FieldReference<Boolean> DEV_COMMANDS_ALWAYS = new FieldReference<>(false);

    public static final FieldReference<Double> CANOPY_COVERAGE = new FieldReference<>(1.7D);
    public static final FieldReference<Integer> TWILIGHT_OAK_CHANCE = new FieldReference<>(48);

    // raw balance values
    public static final FieldReference<Boolean> SELF_CHECK = new FieldReference<>(true);
    public static final FieldReference<String> PORTAL_ITEMS = new FieldReference<>("auto");
    public static final FieldReference<Integer> PORTAL_COST = new FieldReference<>(1);
    public static final FieldReference<Integer> PORTAL_MIN_LEVEL = new FieldReference<>(10);
    public static final FieldReference<Integer> PORTAL_MIN_DAY = new FieldReference<>(20);
    public static final FieldReference<Boolean> PORTAL_REQUIRE_OBSIDIAN_TOOL = new FieldReference<>(true);
    public static final FieldReference<Integer> PORTAL_COOLDOWN_TICKS = new FieldReference<>(6000);
    public static final FieldReference<Integer> PORTAL_SCAN_RADIUS = new FieldReference<>(32);
    public static final FieldReference<Boolean> PORTAL_COSMETIC_LIGHTNING = new FieldReference<>(true);
    public static final FieldReference<Boolean> PORTAL_ALLOW_IN_TF = new FieldReference<>(true);
    public static final FieldReference<Boolean> PORTAL_ALLOW_OTHER_DIMENSIONS = new FieldReference<>(false);
    public static final FieldReference<Boolean> PORTAL_ADMIN_ONLY = new FieldReference<>(false);
    public static final FieldReference<Boolean> PORTAL_DISABLED = new FieldReference<>(false);
    public static final FieldReference<Integer> PORTAL_SANCTUARY_RADIUS = new FieldReference<>(24);
    public static final FieldReference<Integer> TELEPORTER_SEARCH_RADIUS = new FieldReference<>(64);
    public static final FieldReference<Boolean> STUCK_COMMAND = new FieldReference<>(true);
    public static final FieldReference<Double> SPAWN_HOSTILE_FACTOR = new FieldReference<>(0.5);
    public static final FieldReference<Integer> MITE_MONSTER_WEIGHT_PCT = new FieldReference<>(20);
    public static final FieldReference<Boolean> MIRROR_BLOOD_MOON = new FieldReference<>(true);
    public static final FieldReference<Boolean> MIRROR_BLOOD_MOON_BOSSES = new FieldReference<>(false);
    public static final FieldReference<Boolean> MOB_DIGGING = new FieldReference<>(false);
    public static final FieldReference<Double> MOB_DAY_SCALING_MAX = new FieldReference<>(0.0);
    public static final FieldReference<Integer> SPAWNER_MAX_SPAWNS = new FieldReference<>(24);
    public static final FieldReference<Double> SPAWNER_XP_MULT = new FieldReference<>(0.25);
    public static final FieldReference<Boolean> EXPLOSION_BREAK_BLOCKS = new FieldReference<>(false);
    public static final FieldReference<Boolean> ORE_TERRAIN = new FieldReference<>(true);
    public static final FieldReference<Double> ORE_SCALE_COAL = new FieldReference<>(0.5);
    public static final FieldReference<Double> ORE_SCALE_IRON = new FieldReference<>(0.5);
    public static final FieldReference<Double> ORE_SCALE_COPPER = new FieldReference<>(0.5);
    public static final FieldReference<Double> ORE_SCALE_REDSTONE = new FieldReference<>(0.5);
    public static final FieldReference<Double> ORE_SCALE_LAPIS = new FieldReference<>(0.5);
    public static final FieldReference<Double> ORE_HEIGHT_SCALE = new FieldReference<>(0.6);
    public static final FieldReference<Integer> HILL_ORE_CAP_HILL1 = new FieldReference<>(24);
    public static final FieldReference<Integer> HILL_ORE_CAP_HILL2 = new FieldReference<>(40);
    public static final FieldReference<Integer> HILL_ORE_CAP_HILL3 = new FieldReference<>(96);
    public static final FieldReference<String> PROGRESS_MODE = new FieldReference<>("enforced");
    public static final FieldReference<Boolean> PROGRESS_BY_KILL = new FieldReference<>(true);
    public static final FieldReference<Integer> PROGRESS_CREDIT_RADIUS = new FieldReference<>(48);
    public static final FieldReference<Double> PROGRESS_PENALTY_HP_FLOOR = new FieldReference<>(0.5);
    public static final FieldReference<Boolean> PENALTIES_LETHAL = new FieldReference<>(false);
    public static final FieldReference<Boolean> BOSS_MIN_LEVEL_GATE = new FieldReference<>(true);
    public static final FieldReference<Integer> BOSS_MIN_LEVEL_OFFSET = new FieldReference<>(0);
    public static final FieldReference<Double> BOSS_HP_MULT = new FieldReference<>(1.0);
    public static final FieldReference<Double> MOB_HP_MULT = new FieldReference<>(1.0);
    public static final FieldReference<Double> MOB_DAMAGE_MULT = new FieldReference<>(1.0);
    public static final FieldReference<Double> MOB_XP_MULT = new FieldReference<>(1.0);
    public static final FieldReference<Double> BOSS_REPEAT_XP_MULT = new FieldReference<>(0.25);
    public static final FieldReference<Double> BOSS_REPEAT_MATERIAL_MULT = new FieldReference<>(0.5);
    public static final FieldReference<Double> BOSS_SINGLE_HIT_CAP_PCT = new FieldReference<>(0.0);
    public static final FieldReference<Integer> BOSS_SPAWN_RANGE = new FieldReference<>(50);
    public static final FieldReference<Boolean> ITEMS_FREE_ENCHANTS = new FieldReference<>(false);
    public static final FieldReference<Integer> ITEMS_TOOL_LEVEL_CAP = new FieldReference<>(3);
    public static final FieldReference<Double> ITEMS_GLASS_SWORD_DAMAGE = new FieldReference<>(50.0);
    public static final FieldReference<Boolean> IRONWOOD_GATE_PROXY = new FieldReference<>(true);
    public static final FieldReference<Integer> IRONWOOD_INGOTS_PER_IRON = new FieldReference<>(1);
    public static final FieldReference<String> FIERY_PICK_SMELT = new FieldReference<>("heat2");
    public static final FieldReference<Integer> GIANT_CHAIN = new FieldReference<>(2);
    public static final FieldReference<Boolean> GIANT_RECIPES = new FieldReference<>(true);
    public static final FieldReference<Boolean> CHARM_OF_LIFE_CRAFTABLE = new FieldReference<>(false);
    public static final FieldReference<Boolean> MINOTAUR_AXE_SPRINT_BONUS = new FieldReference<>(false);
    public static final FieldReference<String> MAGNET_WHITELIST = new FieldReference<>("coal,iron,copper,redstone,lapis");
    public static final FieldReference<Integer> MAGNET_VEIN_MAX = new FieldReference<>(12);
    public static final FieldReference<Integer> MAGNET_DURABILITY = new FieldReference<>(6);
    public static final FieldReference<Integer> MAGNET_RANGE = new FieldReference<>(16);
    public static final FieldReference<String> UNCRAFTING = new FieldReference<>("off");
    public static final FieldReference<Integer> UNCRAFTING_COST_DIVISOR = new FieldReference<>(5);
    public static final FieldReference<Boolean> TREES_MINING = new FieldReference<>(false);
    public static final FieldReference<Boolean> TREES_TIME = new FieldReference<>(false);
    public static final FieldReference<Integer> TREES_TRANSFORM_INTERVAL = new FieldReference<>(100);
    public static final FieldReference<Integer> TREES_SORT_INTERVAL = new FieldReference<>(200);
    public static final FieldReference<Integer> TREES_SORT_RADIUS = new FieldReference<>(8);
    public static final FieldReference<String> QUEST_RAM_REWARD = new FieldReference<>("iron3,gold2,lapis6,shardEmerald1,horn");
    public static final FieldReference<Double> BEANS_TROLL_DROP_CHANCE = new FieldReference<>(0.01);
    public static final FieldReference<Integer> TORCHBERRY_TORCHES_PER_BERRY = new FieldReference<>(3);
    public static final FieldReference<String> LOOT_PROFILE = new FieldReference<>("mite");
    public static final FieldReference<Boolean> LOOT_DAY_GATES = new FieldReference<>(true);
    public static final FieldReference<Integer> LOOT_ENCHANT_CAP_T1 = new FieldReference<>(4);
    public static final FieldReference<Integer> LOOT_ENCHANT_CAP_T2 = new FieldReference<>(8);
    public static final FieldReference<Integer> LOOT_ENCHANT_CAP_T3 = new FieldReference<>(12);
    public static final FieldReference<Integer> LOOT_ENCHANT_CAP_T4 = new FieldReference<>(16);
    public static final FieldReference<Integer> LOOT_ENCHANT_CAP_T5 = new FieldReference<>(16);
    public static final FieldReference<Integer> LOOT_BUDGET_XP_HARD = new FieldReference<>(300);
    public static final FieldReference<Integer> LOOT_BUDGET_IIE_HARD = new FieldReference<>(25);
    public static final FieldReference<Boolean> LOOT_ALLOW_BLAZE_ROD = new FieldReference<>(false);
    public static final FieldReference<String> FOOD_HYDRA_CHOP = new FieldReference<>("16/16");
    public static final FieldReference<Boolean> HUNGER_ADVISORY = new FieldReference<>(true);
    public static final FieldReference<Boolean> COMPAT_INFERNAL_MOBS_WARN = new FieldReference<>(true);
    public static final FieldReference<String> COMPAT_PROFILE = new FieldReference<>("auto");

    // clamped balance values, valid once the config has been read
    public static boolean selfCheck = true;
    public static String portalItems = "auto";
    public static int portalCost = 1;
    public static int portalMinLevel = 10;
    public static int portalMinDay = 20;
    public static boolean portalRequireObsidianTool = true;
    public static int portalCooldownTicks = 6000;
    public static int portalScanRadius = 32;
    public static boolean portalCosmeticLightning = true;
    public static boolean portalAllowInTF = true;
    public static boolean portalAllowOtherDimensions = false;
    public static boolean portalAdminOnly = false;
    public static boolean portalDisabled = false;
    public static int portalSanctuaryRadius = 24;
    public static int teleporterSearchRadius = 64;
    public static boolean stuckCommand = true;
    public static double spawnHostileFactor = 0.5;
    public static int miteMonsterWeightPct = 20;
    public static boolean mirrorBloodMoon = true;
    public static boolean mirrorBloodMoonBosses = false;
    public static boolean mobDigging = false;
    public static double mobDayScalingMax = 0.0;
    public static int spawnerMaxSpawns = 24;
    public static double spawnerXpMult = 0.25;
    public static boolean explosionBreakBlocks = false;
    public static boolean oreTerrain = true;
    public static double oreScaleCoal = 0.5;
    public static double oreScaleIron = 0.5;
    public static double oreScaleCopper = 0.5;
    public static double oreScaleRedstone = 0.5;
    public static double oreScaleLapis = 0.5;
    public static double oreHeightScale = 0.6;
    public static int hillOreCapHill1 = 24;
    public static int hillOreCapHill2 = 40;
    public static int hillOreCapHill3 = 96;
    public static String progressMode = "enforced";
    public static boolean progressByKill = true;
    public static int progressCreditRadius = 48;
    public static double progressPenaltyHpFloor = 0.5;
    public static boolean penaltiesLethal = false;
    public static boolean bossMinLevelGate = true;
    public static int bossMinLevelOffset = 0;
    public static double bossHpMult = 1.0;
    public static double mobHpMult = 1.0;
    public static double mobDamageMult = 1.0;
    public static double mobXpMult = 1.0;
    public static double bossRepeatXpMult = 0.25;
    public static double bossRepeatMaterialMult = 0.5;
    public static double bossSingleHitCapPct = 0.0;
    public static int bossSpawnRange = 50;
    public static boolean itemsFreeEnchants = false;
    public static int itemsToolLevelCap = 3;
    public static double itemsGlassSwordDamage = 50.0;
    public static boolean ironwoodGateProxy = true;
    public static int ironwoodIngotsPerIron = 1;
    public static String fieryPickSmelt = "heat2";
    public static int giantChain = 2;
    public static boolean giantRecipes = true;
    public static boolean charmOfLifeCraftable = false;
    public static boolean minotaurAxeSprintBonus = false;
    public static String magnetWhitelist = "coal,iron,copper,redstone,lapis";
    public static int magnetVeinMax = 12;
    public static int magnetDurability = 6;
    public static int magnetRange = 16;
    public static String uncrafting = "off";
    public static int uncraftingCostDivisor = 5;
    public static boolean treesMining = false;
    public static boolean treesTime = false;
    public static int treesTransformInterval = 100;
    public static int treesSortInterval = 200;
    public static int treesSortRadius = 8;
    public static String questRamReward = "iron3,gold2,lapis6,shardEmerald1,horn";
    public static double beansTrollDropChance = 0.01;
    public static int torchberryTorchesPerBerry = 3;
    public static String lootProfile = "mite";
    public static boolean lootDayGates = true;
    public static int lootEnchantCapT1 = 4;
    public static int lootEnchantCapT2 = 8;
    public static int lootEnchantCapT3 = 12;
    public static int lootEnchantCapT4 = 16;
    public static int lootEnchantCapT5 = 16;
    public static int lootBudgetXpHard = 300;
    public static int lootBudgetIieHard = 25;
    public static boolean lootAllowBlazeRod = false;
    public static String foodHydraChop = "16/16";
    public static boolean hungerAdvisory = true;
    public static boolean compatInfernalMobsWarn = true;
    public static String compatProfile = "auto";
    /** mob.hpMult as configured, without the difficulty profile (for mobs a difficulty mod already buffs). */
    public static double mobHpMultConfig = 1.0;

    // Bump when the structure changes: an older file is reset to defaults.
    public static final ConfigRoot ROOT = new Utf8ConfigRoot(3)
            .withComment("Twilight Forest")
            .addEntry(ConfigCategory.of("dimension")
                    .withComment("Dimension settings / 维度设置")
                    .addEntry(ConfigEntry.of("dimensionID", DIMENSION_ID).withComment(
                            "ID of the Twilight Forest dimension, 2 to 127. Must be identical on client and server. / 暮色森林维度的ID,范围2到127。客户端与服务端必须一致。")))
            .addEntry(ConfigCategory.of("general")
                    .withComment("General options / 常规选项")
                    .addEntry(ConfigEntry.of("SilentCicadas", SILENT_CICADAS).withComment(
                            "Make cicadas silent for those finding them annoying / 让蝉不再发出声音"))
                    .addEntry(ConfigEntry.of("TwilightForestSeed", TWILIGHT_FOREST_SEED).withComment(
                            "If set, overrides the world seed when generating the Twilight Forest dimension. / 若填写,则生成暮色森林维度时使用该种子,而不是世界种子。"))
                    .addEntry(ConfigEntry.of("DisableUncrafting", DISABLE_UNCRAFTING).withComment(
                            "Disable the uncrafting function of the uncrafting table. / 禁用拆解台的拆解功能。"))
                    .addEntry(ConfigEntry.of("OldMapGen", OLD_MAP_GEN).withComment(
                            "Use the old (pre 1.7) map gen. May not be fully supported. / 使用旧版(1.7之前)的地图生成。可能不完全支持。")))
            .addEntry(ConfigCategory.of("safety")
                    .withComment("Safety / 安全")
                    .addEntry(ConfigEntry.of("selfCheck", SELF_CHECK).withComment("Refuse to create portals when the startup self check fails (hard rule H1). / 启动自检失败时,拒绝创建传送门(硬性规则H1)。")))
            .addEntry(ConfigCategory.of("portal")
                    .withComment("Portal and return trip / 传送门与返程")
                    .addEntry(ConfigEntry.of("portal.items", PORTAL_ITEMS).withComment("Items that open the portal when thrown into the pool (consumed), comma separated, e.g. ingotCopper, diamond, emerald. 'auto': copper ingot, or diamond when MITE-ITE, Extreme or Beyond Extreme is installed. / 投入水池即可开启传送门的物品(会被消耗),用逗号分隔,例如 ingotCopper(铜锭)、diamond(钻石)、emerald(绿宝石)。填auto:默认铜锭,安装了MITE-ITE、极难或极难拓展时为钻石。"))
                    .addEntry(ConfigEntry.of("portal.cost", PORTAL_COST).withComment("Number of items consumed. / 消耗的物品数量。"))
                    .addEntry(ConfigEntry.of("portal.minLevel", PORTAL_MIN_LEVEL).withComment("Minimum experience level of the nearest player. 0 disables. / 最近玩家所需的最低经验等级。0为不限制。"))
                    .addEntry(ConfigEntry.of("portal.minDay", PORTAL_MIN_DAY).withComment("Minimum overworld day. 0 disables. / 主世界所需的最低天数。0为不限制。"))
                    .addEntry(ConfigEntry.of("portal.requireObsidianTool", PORTAL_REQUIRE_OBSIDIAN_TOOL).withComment("Require that somebody in the world already made an obsidian-tier pickaxe (village_conditions & 16). / 要求世界中已有人制作过黑曜石级镐(village_conditions & 16)。"))
                    .addEntry(ConfigEntry.of("portal.cooldownTicks", PORTAL_COOLDOWN_TICKS).withComment("Ticks before the same player may create another portal with an item. / 同一玩家再次用物品创建传送门前的冷却时间(刻,20刻为1秒)。"))
                    .addEntry(ConfigEntry.of("portal.scanRadius", PORTAL_SCAN_RADIUS).withComment("Radius around a player in which thrown items are checked. / 检测玩家周围投掷物品的半径。"))
                    .addEntry(ConfigEntry.of("portal.cosmeticLightning", PORTAL_COSMETIC_LIGHTNING).withComment("Visual only lightning on portal creation (no fire, no damage). / 创建传送门时仅有视觉效果的闪电(不起火,不伤人)。"))
                    .addEntry(ConfigEntry.of("portal.allowInTF", PORTAL_ALLOW_IN_TF).withComment("Allow creating a return portal inside the forest. / 允许在暮色森林内创建返回传送门。"))
                    .addEntry(ConfigEntry.of("portal.allowOtherDimensions", PORTAL_ALLOW_OTHER_DIMENSIONS).withComment("Allow portals in the Nether, Underworld and End. / 允许在下界、地下世界和末地创建传送门。"))
                    .addEntry(ConfigEntry.of("portal.adminOnly", PORTAL_ADMIN_ONLY).withComment("Only admins may create portals. / 只有管理员可以创建传送门。"))
                    .addEntry(ConfigEntry.of("portal.disabled", PORTAL_DISABLED).withComment("Disable portal creation entirely. / 完全禁用传送门创建。"))
                    .addEntry(ConfigEntry.of("portal.sanctuaryRadius", PORTAL_SANCTUARY_RADIUS).withComment("No hostile spawns this close to a Twilight Forest portal. / 暮色森林传送门附近此半径内不会生成敌对生物。"))
                    .addEntry(ConfigEntry.of("teleporter.searchRadius", TELEPORTER_SEARCH_RADIUS).withComment("Radius for finding an existing portal. / 寻找已有传送门的搜索半径。"))
                    .addEntry(ConfigEntry.of("stuckCommand", STUCK_COMMAND).withComment("Enable /tf stuck (24 hour cooldown). / 启用 /tf stuck 指令(冷却24小时)。")))
            .addEntry(ConfigCategory.of("rules")
                    .withComment("Dimension rules / 维度规则")
                    .addEntry(ConfigEntry.of("spawn.hostileFactor", SPAWN_HOSTILE_FACTOR).withComment("Hostile spawn rate in the forest (MITE uses 0.25 for other dimensions). / 森林中敌对生物的生成比例(MITE其他维度为0.25)。"))
                    .addEntry(ConfigEntry.of("spawn.miteMonsterWeightPct", MITE_MONSTER_WEIGHT_PCT).withComment("MITE's own monsters (ghouls, wights, shadows, jellies, ...) spawn inside hollow hills and in caves below sea level with this percent of their MITE weight and vanilla stats; 0 turns it off. / MITE原版怪物(食尸鬼、尸妖、暗影、果冻怪等)在空心山丘内部和海平面以下的洞穴中按其MITE权重的此百分比生成,保持原版数值;0为关闭。"))
                    .addEntry(ConfigEntry.of("mirrorBloodMoon", MIRROR_BLOOD_MOON).withComment("The forest has a blood moon whenever the overworld has one. / 主世界出现血月时,森林也出现血月。"))
                    .addEntry(ConfigEntry.of("mirrorBloodMoonBosses", MIRROR_BLOOD_MOON_BOSSES).withComment("Bosses are frenzied during the mirrored blood moon. / 镜像血月期间,首领会进入狂暴状态。"))
                    .addEntry(ConfigEntry.of("mob.digging", MOB_DIGGING).withComment("Allow monsters to dig blocks inside the forest. / 允许怪物在森林中挖掘方块。"))
                    .addEntry(ConfigEntry.of("mob.dayScaling.max", MOB_DAY_SCALING_MAX).withComment("Extra mob strength after a year of days. 0 disables. / 经过一年天数后怪物额外增强的幅度。0为禁用。"))
                    .addEntry(ConfigEntry.of("spawner.maxSpawns", SPAWNER_MAX_SPAWNS).withComment("Mobs a single spawner produces before it is exhausted. / 单个刷怪笼耗尽前可生成的怪物数量。"))
                    .addEntry(ConfigEntry.of("spawner.xpMult", SPAWNER_XP_MULT).withComment("XP multiplier for mobs that came from a spawner. / 刷怪笼生成的怪物的经验倍率。"))
                    .addEntry(ConfigEntry.of("explosion.breakBlocks", EXPLOSION_BREAK_BLOCKS).withComment("Allow TF explosions to break blocks. / 允许暮色森林的爆炸破坏方块。"))
                    .addEntry(ConfigEntry.of("ore.terrain", ORE_TERRAIN).withComment("Generate ores in Twilight Forest terrain. / 在暮色森林地形中生成矿石。"))
                    .addEntry(ConfigEntry.of("ore.scale.coal", ORE_SCALE_COAL).withComment("Terrain ore frequency scale for coal. / 地形煤矿的生成频率倍率。"))
                    .addEntry(ConfigEntry.of("ore.scale.iron", ORE_SCALE_IRON).withComment("Terrain ore frequency scale for iron. / 地形铁矿的生成频率倍率。"))
                    .addEntry(ConfigEntry.of("ore.scale.copper", ORE_SCALE_COPPER).withComment("Terrain ore frequency scale for copper. / 地形铜矿的生成频率倍率。"))
                    .addEntry(ConfigEntry.of("ore.scale.redstone", ORE_SCALE_REDSTONE).withComment("Terrain ore frequency scale for redstone. / 地形红石矿的生成频率倍率。"))
                    .addEntry(ConfigEntry.of("ore.scale.lapis", ORE_SCALE_LAPIS).withComment("Terrain ore frequency scale for lapis. / 地形青金石矿的生成频率倍率。"))
                    .addEntry(ConfigEntry.of("ore.heightScale", ORE_HEIGHT_SCALE).withComment("Height range scale of terrain ores. / 地形矿石的高度范围倍率。"))
                    .addEntry(ConfigEntry.of("hill.oreCap.hill1", HILL_ORE_CAP_HILL1).withComment("Ore block cap of a level 1 hollow hill. / 一级空心山丘的矿石方块数量上限。"))
                    .addEntry(ConfigEntry.of("hill.oreCap.hill2", HILL_ORE_CAP_HILL2).withComment("Ore block cap of a level 2 hollow hill. / 二级空心山丘的矿石方块数量上限。"))
                    .addEntry(ConfigEntry.of("hill.oreCap.hill3", HILL_ORE_CAP_HILL3).withComment("Ore block cap of a level 3 hollow hill. / 三级空心山丘的矿石方块数量上限。")))
            .addEntry(ConfigCategory.of("progress")
                    .withComment("Progression and bosses / 进度与首领")
                    .addEntry(ConfigEntry.of("progress.mode", PROGRESS_MODE).withComment("enforced, lenient (boss structures only) or off. / enforced(强制)、lenient(宽松,仅保护首领建筑)或 off(关闭)。"))
                    .addEntry(ConfigEntry.of("progress.byKill", PROGRESS_BY_KILL).withComment("Progress is granted by kills, never by picking items up. / 进度只通过击杀获得,拾取物品不会推进进度。"))
                    .addEntry(ConfigEntry.of("progress.creditRadius", PROGRESS_CREDIT_RADIUS).withComment("Players this close to a dying boss get credit. / 濒死首领周围此半径内的玩家可获得进度。"))
                    .addEntry(ConfigEntry.of("progress.penaltyHpFloor", PROGRESS_PENALTY_HP_FLOOR).withComment("Damaging biome penalties stop below this fraction of max health. / 生命值低于最大生命值的此比例时,群系伤害惩罚不再生效。"))
                    .addEntry(ConfigEntry.of("penalties.lethal", PENALTIES_LETHAL).withComment("Allow biome penalties to kill. / 允许群系惩罚致死。"))
                    .addEntry(ConfigEntry.of("boss.minLevelGate", BOSS_MIN_LEVEL_GATE).withComment("Boss spawners need a nearby player of a minimum level. / 首领刷怪笼要求附近有达到最低等级的玩家。"))
                    .addEntry(ConfigEntry.of("boss.minLevelOffset", BOSS_MIN_LEVEL_OFFSET).withComment("Added to every boss level gate. / 叠加到每个首领等级门槛上的数值。"))
                    .addEntry(ConfigEntry.of("boss.hpMult", BOSS_HP_MULT).withComment("Boss health multiplier. / 首领生命值倍率。"))
                    .addEntry(ConfigEntry.of("mob.hpMult", MOB_HP_MULT).withComment("Mob health multiplier. / 怪物生命值倍率。"))
                    .addEntry(ConfigEntry.of("mob.damageMult", MOB_DAMAGE_MULT).withComment("Mob damage multiplier. / 怪物伤害倍率。"))
                    .addEntry(ConfigEntry.of("mob.xpMult", MOB_XP_MULT).withComment("Mob XP multiplier. / 怪物经验倍率。"))
                    .addEntry(ConfigEntry.of("boss.repeatXpMult", BOSS_REPEAT_XP_MULT).withComment("XP multiplier when every credited player already has the boss flag. / 所有获得功劳的玩家都已击败过该首领时的经验倍率。"))
                    .addEntry(ConfigEntry.of("boss.repeatMaterialMult", BOSS_REPEAT_MATERIAL_MULT).withComment("Material drop multiplier on repeat kills. / 重复击杀首领时材料掉落的倍率。"))
                    .addEntry(ConfigEntry.of("boss.singleHitCapPct", BOSS_SINGLE_HIT_CAP_PCT).withComment("Cap on a single hit in percent of player max health. 0 disables. / 单次受击伤害上限,按玩家最大生命值的百分比计。0为禁用。"))
                    .addEntry(ConfigEntry.of("boss.spawnRange", BOSS_SPAWN_RANGE).withComment("Range in which the boss spawner looks for players. / 首领刷怪笼搜索玩家的范围。")))
            .addEntry(ConfigCategory.of("items")
                    .withComment("Items and economy / 物品与经济")
                    .addEntry(ConfigEntry.of("items.freeEnchants", ITEMS_FREE_ENCHANTS).withComment("Allow recipes to output enchanted items. / 允许配方产出带附魔的物品。"))
                    .addEntry(ConfigEntry.of("items.toolLevelCap", ITEMS_TOOL_LEVEL_CAP).withComment("Maximum harvest level of TF tools. / 暮色森林工具的最高采集等级。"))
                    .addEntry(ConfigEntry.of("items.glassSwordDamage", ITEMS_GLASS_SWORD_DAMAGE).withComment("Damage of the one-hit glass sword (not changed by compat.profile). Needs a restart. / 一击即碎的玻璃剑的伤害(不受 compat.profile 影响)。需重启生效。"))
                    .addEntry(ConfigEntry.of("ironwood.gateProxy", IRONWOOD_GATE_PROXY).withComment("Ironwood stays at the iron tier of crafting gates. / 铁木在合成门槛中仍视为铁级。"))
                    .addEntry(ConfigEntry.of("ironwood.ingotsPerIron", IRONWOOD_INGOTS_PER_IRON).withComment("Ironwood ingots per iron ingot. / 每个铁锭对应的铁木锭数量。"))
                    .addEntry(ConfigEntry.of("fieryPick.smelt", FIERY_PICK_SMELT).withComment("Fiery pickaxe auto-smelt: off, heatN (furnace heat level N, heat2 = iron/gold/copper/silver) or vanilla (no heat gate). / 炽热镐自动熔炼:off(关闭)、heatN(按熔炉热量等级N,heat2可熔铁/金/铜/银)或 vanilla(无热量限制)。"))
                    .addEntry(ConfigEntry.of("giant.chain", GIANT_CHAIN).withComment("Edge length of the giant pickaxe cell. / 巨型镐作用范围的边长。"))
                    .addEntry(ConfigEntry.of("giant.recipes", GIANT_RECIPES).withComment("Giant blocks unpack to 16 items. / 巨型方块可拆分为16个物品。"))
                    .addEntry(ConfigEntry.of("charmOfLifeCraftable", CHARM_OF_LIFE_CRAFTABLE).withComment("Charm of life II can be crafted. / 允许合成生命护符II。"))
                    .addEntry(ConfigEntry.of("minotaurAxeSprintBonus", MINOTAUR_AXE_SPRINT_BONUS).withComment("Minotaur axe sprint bonus. / 牛头人斧的疾跑加成。"))
                    .addEntry(ConfigEntry.of("magnet.whitelist", MAGNET_WHITELIST).withComment("Ores the ore magnet and ore map find. / 矿石磁铁和矿石地图可寻找的矿石。"))
                    .addEntry(ConfigEntry.of("magnet.veinMax", MAGNET_VEIN_MAX).withComment("Maximum vein size of the ore magnet. / 矿石磁铁的最大矿脉大小。"))
                    .addEntry(ConfigEntry.of("magnet.durability", MAGNET_DURABILITY).withComment("Ore magnet durability. / 矿石磁铁的耐久。"))
                    .addEntry(ConfigEntry.of("magnet.range", MAGNET_RANGE).withComment("Ore magnet range. / 矿石磁铁的范围。"))
                    .addEntry(ConfigEntry.of("uncrafting", UNCRAFTING).withComment("Uncrafting table: off (default, the table does not open), whitelist or vanilla. / 拆解台:off(默认,拆解台无法打开)、whitelist(白名单)或 vanilla(原版)。"))
                    .addEntry(ConfigEntry.of("uncrafting.costDivisor", UNCRAFTING_COST_DIVISOR).withComment("Uncrafting costs recipe difficulty / divisor experience points. / 拆解消耗的经验 = 配方难度 / 此除数。"))
                    .addEntry(ConfigEntry.of("trees.mining", TREES_MINING).withComment("Enable the mining tree. / 启用采矿树。"))
                    .addEntry(ConfigEntry.of("trees.time", TREES_TIME).withComment("Enable the time tree. / 启用时间树。"))
                    .addEntry(ConfigEntry.of("trees.transformInterval", TREES_TRANSFORM_INTERVAL).withComment("Transformation tree interval in ticks. / 转化树的作用间隔(刻)。"))
                    .addEntry(ConfigEntry.of("trees.sortInterval", TREES_SORT_INTERVAL).withComment("Sorting tree interval in ticks. / 整理树的作用间隔(刻)。"))
                    .addEntry(ConfigEntry.of("trees.sortRadius", TREES_SORT_RADIUS).withComment("Sorting tree radius. / 整理树的作用半径。"))
                    .addEntry(ConfigEntry.of("questRam.reward", QUEST_RAM_REWARD).withComment("Quest ram reward items. / 任务公羊的奖励物品。"))
                    .addEntry(ConfigEntry.of("beans.trollDropChance", BEANS_TROLL_DROP_CHANCE).withComment("Chance that a troll drops a magic bean. / 巨魔掉落魔豆的几率。"))
                    .addEntry(ConfigEntry.of("torchberry.torchesPerBerry", TORCHBERRY_TORCHES_PER_BERRY).withComment("Torches crafted from one torchberry. / 一个火把莓可合成的火把数量。")))
            .addEntry(ConfigCategory.of("loot")
                    .withComment("Loot and food / 战利品与食物")
                    .addEntry(ConfigEntry.of("loot.profile", LOOT_PROFILE).withComment("mite or vanilla (vanilla breaks the economy). / mite 或 vanilla(原版会破坏经济平衡)。"))
                    .addEntry(ConfigEntry.of("loot.dayGates", LOOT_DAY_GATES).withComment("Loot tables are gated by overworld day. / 战利品表受主世界天数限制。"))
                    .addEntry(ConfigEntry.of("loot.enchantCap.T1", LOOT_ENCHANT_CAP_T1).withComment("Enchantment level cap of tier 1 loot. / 一级战利品的附魔等级上限。"))
                    .addEntry(ConfigEntry.of("loot.enchantCap.T2", LOOT_ENCHANT_CAP_T2).withComment("Enchantment level cap of tier 2 loot. / 二级战利品的附魔等级上限。"))
                    .addEntry(ConfigEntry.of("loot.enchantCap.T3", LOOT_ENCHANT_CAP_T3).withComment("Enchantment level cap of tier 3 loot. / 三级战利品的附魔等级上限。"))
                    .addEntry(ConfigEntry.of("loot.enchantCap.T4", LOOT_ENCHANT_CAP_T4).withComment("Enchantment level cap of tier 4 loot. / 四级战利品的附魔等级上限。"))
                    .addEntry(ConfigEntry.of("loot.enchantCap.T5", LOOT_ENCHANT_CAP_T5).withComment("Enchantment level cap of tier 5 loot. / 五级战利品的附魔等级上限。"))
                    .addEntry(ConfigEntry.of("loot.budget.xpHard", LOOT_BUDGET_XP_HARD).withComment("Hard cap of experience value per chest. / 每个箱子的经验价值硬上限。"))
                    .addEntry(ConfigEntry.of("loot.budget.iieHard", LOOT_BUDGET_IIE_HARD).withComment("Hard cap of item importance per chest. / 每个箱子的物品重要度硬上限。"))
                    .addEntry(ConfigEntry.of("loot.allowBlazeRod", LOOT_ALLOW_BLAZE_ROD).withComment("Allow blaze rods in loot. / 允许战利品中出现烈焰棒。"))
                    .addEntry(ConfigEntry.of("food.hydraChop", FOOD_HYDRA_CHOP).withComment("Hydra chop hunger/saturation. / 九头蛇肉排的饥饿值/饱和度。"))
                    .addEntry(ConfigEntry.of("hunger.advisory", HUNGER_ADVISORY).withComment("Tell players to bring food on first entry. / 首次进入时提醒玩家携带食物。"))
                    .addEntry(ConfigEntry.of("compat.infernalMobsWarn", COMPAT_INFERNAL_MOBS_WARN).withComment("Warn when Infernal Mobs is installed. / 安装了Infernal Mobs时给出警告。")))
            .addEntry(ConfigCategory.of("compat")
                    .withComment("Difficulty mods / 难度模组兼容")
                    .addEntry(ConfigEntry.of("compat.profile", COMPAT_PROFILE).withComment("Difficulty profile, multiplied onto mob.hpMult, boss.hpMult and mob.damageMult, added to mob.dayScaling.max, and scaling TF weapon damage. auto: ite with MITE-ITE (mob hp x1.6, boss hp x1.5, mob damage x1.25, weapons x1.25, day scaling +1.4), extreme with Extreme or Beyond Extreme (mob damage x1.4, otherwise as ite), none otherwise (ITF-Reborn and BetterGameSetting change no mob or weapon numbers). Or force none, ite or extreme. The profile day scaling is added even when mob.dayScaling.max is 0; use none to turn it off. Needs a restart. / 难度档,会乘到 mob.hpMult、boss.hpMult、mob.damageMult 上、加到 mob.dayScaling.max 上,并缩放暮色武器伤害。auto:装了MITE-ITE为ite(怪物血量x1.6、Boss血量x1.5、怪物伤害x1.25、武器x1.25、天数成长+1.4),装了极难或极难拓展为extreme(怪物伤害x1.4,其余同ite),否则为none(黄昏重生与更好的游戏设置不改怪物和武器数值)。也可强制填 none、ite 或 extreme。即使 mob.dayScaling.max 为0也会加上难度档的天数成长,填none可关闭。需重启生效。")))
            .addEntry(ConfigCategory.of("developer")
                    .withComment("Developer / cheat command options / 开发者/作弊指令选项")
                    .addEntry(ConfigEntry.of("DevCommandsAlways", DEV_COMMANDS_ALWAYS).withComment(
                            "Let every player use /twilightforest tp, give and dump. Default false: MITE dev mode, admins and cheat-enabled worlds only. / 让所有玩家都能使用 /twilightforest tp、give 和 dump。默认关闭:仅MITE开发模式、管理员和允许作弊的世界可用。")))
            .addEntry(ConfigCategory.of("performance")
                    .withComment("Performance options / 性能选项")
                    .addEntry(ConfigEntry.of("CanopyCoverage", CANOPY_COVERAGE).withComment(
                            "Amount of canopy coverage, 0.0 and up. Lower is faster to generate but thinner. / 树冠覆盖量,0.0及以上。越低生成越快,但树冠越稀疏。"))
                    .addEntry(ConfigEntry.of("TwilightOakChance", TWILIGHT_OAK_CHANCE).withComment(
                            "Chance that a chunk in the Twilight Forest contains a twilight oak. Higher means fewer trees. / 暮色森林区块中出现暮色橡树的几率。数值越大,树越少。")));

    public static final ConfigRegistry REGISTRY = new ConfigRegistry(ROOT, new File("twilightforest.json"))
            .setReloadRun(TFConfig::apply);


    /**
     * ConfigRoot whose file access is always UTF-8. FishModLoader 3.4.2 reads and writes the first file as UTF-8 but rewrites a
     * changed one (added keys, a version bump) through FileWriter, i.e. in the platform charset, which turns the Chinese comments
     * into garbage on a GBK/ISO-8859-1 JVM. Same steps as ConfigRoot.readFromFile otherwise.
     */
    private static final class Utf8ConfigRoot extends ConfigRoot {
        Utf8ConfigRoot(int version) {
            super(version);
        }

        @Override
        public void readFromFile(File relative) {
            File file = new File(net.xiaoyu233.fml.FishModLoader.CONFIG_DIR, relative.toString());
            try {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    write(file, this.writeDefault());
                }
                Config.ReadResult result;
                try (java.io.Reader in = java.nio.file.Files.newBufferedReader(file.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                    result = this.read(new com.google.gson.JsonParser().parse(in));
                }
                if (result.isDirty()) {
                    write(file, result.getChanged());
                }
            } catch (Throwable t) {
                TwilightForestMod.LOGGER.error("Twilight Forest config {} could not be read or written", file, t);
            }
        }

        private static void write(File file, com.google.gson.JsonElement json) throws java.io.IOException {
            try (java.io.Writer out = java.nio.file.Files.newBufferedWriter(file.toPath(), java.nio.charset.StandardCharsets.UTF_8)) {
                Config.GSON.toJson(json, out);
            }
        }
    }

    private TFConfig() {}

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Double.isNaN(v) ? lo : Math.max(lo, Math.min(hi, v));
    }

    /** True when the progression gate applies at all (progress.mode is not off). */
    public static boolean progressEnforced() {
        return !"off".equalsIgnoreCase(progressMode);
    }

    /** lenient: only boss structures are protected, biome penalties are skipped. */
    public static boolean progressLenient() {
        return "lenient".equalsIgnoreCase(progressMode);
    }

    /** Clamps the raw values and copies them into the 1.7.10 style static fields. */
    static void apply() {
        // read first: TFModCompat.profile() resolves it once, before the multipliers below use it
        compatProfile = COMPAT_PROFILE.get() == null || COMPAT_PROFILE.get().trim().isEmpty() ? "auto" : COMPAT_PROFILE.get().trim();
        int dim = DIMENSION_ID.get();
        if (dim < 2 || dim > 127) {
            TwilightForestMod.LOGGER.warn("Invalid dimensionID {}, using {}", dim, TFIds.DIMENSION);
            dim = TFIds.DIMENSION;
            DIMENSION_ID.set(dim);
        }
        TwilightForestMod.dimensionID = dim;
        twilightforest.dimension.TFDimension.setId(dim);

        selfCheck = SELF_CHECK.get();
        portalItems = PORTAL_ITEMS.get() == null ? "auto" : PORTAL_ITEMS.get().trim();
        if (portalItems.replace(" ", "").equalsIgnoreCase("emerald,diamond")) {
            // the pre-auto default of version 2 files: follow the installed mods like a fresh config
            portalItems = "auto";
        }
        portalCost = clamp(PORTAL_COST.get(), 1, 64);
        portalMinLevel = clamp(PORTAL_MIN_LEVEL.get(), 0, 100);
        portalMinDay = clamp(PORTAL_MIN_DAY.get(), 0, 100000);
        portalRequireObsidianTool = PORTAL_REQUIRE_OBSIDIAN_TOOL.get();
        portalCooldownTicks = clamp(PORTAL_COOLDOWN_TICKS.get(), 0, 1728000);
        portalScanRadius = clamp(PORTAL_SCAN_RADIUS.get(), 4, 64);
        portalCosmeticLightning = PORTAL_COSMETIC_LIGHTNING.get();
        portalAllowInTF = PORTAL_ALLOW_IN_TF.get();
        portalAllowOtherDimensions = PORTAL_ALLOW_OTHER_DIMENSIONS.get();
        portalAdminOnly = PORTAL_ADMIN_ONLY.get();
        portalDisabled = PORTAL_DISABLED.get();
        portalSanctuaryRadius = clamp(PORTAL_SANCTUARY_RADIUS.get(), 0, 64);
        teleporterSearchRadius = clamp(TELEPORTER_SEARCH_RADIUS.get(), 16, 256);
        stuckCommand = STUCK_COMMAND.get();
        spawnHostileFactor = clamp(SPAWN_HOSTILE_FACTOR.get(), 0.0, 1.0);
        miteMonsterWeightPct = clamp(MITE_MONSTER_WEIGHT_PCT.get(), 0, 100);
        mirrorBloodMoon = MIRROR_BLOOD_MOON.get();
        mirrorBloodMoonBosses = MIRROR_BLOOD_MOON_BOSSES.get();
        mobDigging = MOB_DIGGING.get();
        mobDayScalingMax = clamp(MOB_DAY_SCALING_MAX.get() + twilightforest.compat.TFModCompat.profile().dayScalingMax, 0.0, 3.0);
        spawnerMaxSpawns = clamp(SPAWNER_MAX_SPAWNS.get(), 1, 100000);
        spawnerXpMult = clamp(SPAWNER_XP_MULT.get(), 0.0, 1.0);
        explosionBreakBlocks = EXPLOSION_BREAK_BLOCKS.get();
        oreTerrain = ORE_TERRAIN.get();
        oreScaleCoal = clamp(ORE_SCALE_COAL.get(), 0.0, 1.0);
        oreScaleIron = clamp(ORE_SCALE_IRON.get(), 0.0, 1.0);
        oreScaleCopper = clamp(ORE_SCALE_COPPER.get(), 0.0, 1.0);
        oreScaleRedstone = clamp(ORE_SCALE_REDSTONE.get(), 0.0, 1.0);
        oreScaleLapis = clamp(ORE_SCALE_LAPIS.get(), 0.0, 1.0);
        oreHeightScale = clamp(ORE_HEIGHT_SCALE.get(), 0.1, 1.0);
        hillOreCapHill1 = clamp(HILL_ORE_CAP_HILL1.get(), 0, 256);
        hillOreCapHill2 = clamp(HILL_ORE_CAP_HILL2.get(), 0, 256);
        hillOreCapHill3 = clamp(HILL_ORE_CAP_HILL3.get(), 0, 256);
        progressMode = PROGRESS_MODE.get() == null ? "enforced" : PROGRESS_MODE.get().trim();
        progressByKill = PROGRESS_BY_KILL.get();
        progressCreditRadius = clamp(PROGRESS_CREDIT_RADIUS.get(), 8, 128);
        progressPenaltyHpFloor = clamp(PROGRESS_PENALTY_HP_FLOOR.get(), 0.0, 1.0);
        penaltiesLethal = PENALTIES_LETHAL.get();
        bossMinLevelGate = BOSS_MIN_LEVEL_GATE.get();
        bossMinLevelOffset = clamp(BOSS_MIN_LEVEL_OFFSET.get(), -30, 30);
        // difficulty profile of the installed difficulty mods (compat.profile) on top of the configured multipliers
        twilightforest.compat.TFModCompat.Profile profile = twilightforest.compat.TFModCompat.profile();
        mobHpMultConfig = clamp(MOB_HP_MULT.get(), 0.1, 10.0);
        bossHpMult = clamp(BOSS_HP_MULT.get() * profile.bossHpMult, 0.1, 10.0);
        mobHpMult = clamp(mobHpMultConfig * profile.mobHpMult, 0.1, 10.0);
        mobDamageMult = clamp(MOB_DAMAGE_MULT.get() * profile.mobDamageMult, 0.1, 10.0);
        mobXpMult = clamp(MOB_XP_MULT.get(), 0.0, 10.0);
        bossRepeatXpMult = clamp(BOSS_REPEAT_XP_MULT.get(), 0.0, 1.0);
        bossRepeatMaterialMult = clamp(BOSS_REPEAT_MATERIAL_MULT.get(), 0.0, 1.0);
        bossSingleHitCapPct = clamp(BOSS_SINGLE_HIT_CAP_PCT.get(), 0.0, 100.0);
        bossSpawnRange = clamp(BOSS_SPAWN_RANGE.get(), 16, 128);
        itemsFreeEnchants = ITEMS_FREE_ENCHANTS.get();
        itemsToolLevelCap = clamp(ITEMS_TOOL_LEVEL_CAP.get(), 1, 3);
        itemsGlassSwordDamage = clamp(ITEMS_GLASS_SWORD_DAMAGE.get(), 1.0, 1000.0);
        ironwoodGateProxy = IRONWOOD_GATE_PROXY.get();
        ironwoodIngotsPerIron = clamp(IRONWOOD_INGOTS_PER_IRON.get(), 1, 2);
        fieryPickSmelt = FIERY_PICK_SMELT.get() == null ? "heat2" : FIERY_PICK_SMELT.get().trim();
        giantChain = clamp(GIANT_CHAIN.get(), 1, 4);
        giantRecipes = GIANT_RECIPES.get();
        charmOfLifeCraftable = CHARM_OF_LIFE_CRAFTABLE.get();
        minotaurAxeSprintBonus = MINOTAUR_AXE_SPRINT_BONUS.get();
        magnetWhitelist = MAGNET_WHITELIST.get() == null ? "coal,iron,copper,redstone,lapis" : MAGNET_WHITELIST.get().trim();
        magnetVeinMax = clamp(MAGNET_VEIN_MAX.get(), 1, 64);
        magnetDurability = clamp(MAGNET_DURABILITY.get(), 1, 100);
        magnetRange = clamp(MAGNET_RANGE.get(), 4, 64);
        uncrafting = UNCRAFTING.get() == null ? "off" : UNCRAFTING.get().trim();
        uncraftingCostDivisor = clamp(UNCRAFTING_COST_DIVISOR.get(), 1, 100);
        treesMining = TREES_MINING.get();
        treesTime = TREES_TIME.get();
        treesTransformInterval = clamp(TREES_TRANSFORM_INTERVAL.get(), 20, 2400);
        treesSortInterval = clamp(TREES_SORT_INTERVAL.get(), 20, 2400);
        treesSortRadius = clamp(TREES_SORT_RADIUS.get(), 1, 32);
        questRamReward = QUEST_RAM_REWARD.get() == null ? "iron3,gold2,lapis6,shardEmerald1,horn" : QUEST_RAM_REWARD.get().trim();
        beansTrollDropChance = clamp(BEANS_TROLL_DROP_CHANCE.get(), 0.0, 1.0);
        torchberryTorchesPerBerry = clamp(TORCHBERRY_TORCHES_PER_BERRY.get(), 1, 5);
        lootProfile = LOOT_PROFILE.get() == null ? "mite" : LOOT_PROFILE.get().trim();
        lootDayGates = LOOT_DAY_GATES.get();
        lootEnchantCapT1 = clamp(LOOT_ENCHANT_CAP_T1.get(), 0, 50);
        lootEnchantCapT2 = clamp(LOOT_ENCHANT_CAP_T2.get(), 0, 50);
        lootEnchantCapT3 = clamp(LOOT_ENCHANT_CAP_T3.get(), 0, 50);
        lootEnchantCapT4 = clamp(LOOT_ENCHANT_CAP_T4.get(), 0, 50);
        lootEnchantCapT5 = clamp(LOOT_ENCHANT_CAP_T5.get(), 0, 50);
        lootBudgetXpHard = clamp(LOOT_BUDGET_XP_HARD.get(), 0, 100000);
        lootBudgetIieHard = clamp(LOOT_BUDGET_IIE_HARD.get(), 0, 1000);
        lootAllowBlazeRod = LOOT_ALLOW_BLAZE_ROD.get();
        foodHydraChop = FOOD_HYDRA_CHOP.get() == null ? "16/16" : FOOD_HYDRA_CHOP.get().trim();
        hungerAdvisory = HUNGER_ADVISORY.get();
        compatInfernalMobsWarn = COMPAT_INFERNAL_MOBS_WARN.get();

        TwilightForestMod.silentCicadas = SILENT_CICADAS.get();
        TwilightForestMod.twilightForestSeed = TWILIGHT_FOREST_SEED.get().trim();
        TwilightForestMod.disableUncrafting = DISABLE_UNCRAFTING.get();
        TwilightForestMod.oldMapGen = OLD_MAP_GEN.get();
        TwilightForestMod.portalCreationItemString = portalItems;

        TwilightForestMod.canopyCoverage = (float) Math.max(0.0D, CANOPY_COVERAGE.get());
        TwilightForestMod.twilightOakChance = Math.max(1, TWILIGHT_OAK_CHANCE.get());

        twilightforest.dimension.TFDimension.hostileSpawnRateModifier = (float) spawnHostileFactor;
        String mode = progressMode.toLowerCase();
        if (!mode.equals("enforced") && !mode.equals("lenient") && !mode.equals("off")) {
            TwilightForestMod.LOGGER.warn("Invalid progress.mode '{}', using enforced", progressMode);
            progressMode = "enforced";
        } else {
            progressMode = mode;
        }
    }
}
