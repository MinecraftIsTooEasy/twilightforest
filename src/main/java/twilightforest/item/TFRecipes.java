package twilightforest.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Block;
import net.minecraft.CraftingManager;
import net.minecraft.CraftingResult;
import net.minecraft.Enchantment;
import net.minecraft.FurnaceRecipes;
import net.minecraft.IRecipe;
import net.minecraft.InventoryCrafting;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.World;
import net.xiaoyu233.fml.reload.event.RecipeRegistryEvent;
import twilightforest.TFConfig;
import twilightforest.block.BlockTFFireJet;
import twilightforest.block.BlockTFPlant;
import twilightforest.block.BlockTFTowerDevice;
import twilightforest.block.TFBlocks;

/**
 * Crafting and smelting recipes. Shaped/shapeless recipes go through the FML RecipeRegistryEvent; recipes that need a
 * custom IRecipe (map cloning, recharging used-up scepters) can only be added after CraftingManager's constructor, see
 * {@link #registerLateRecipes(List)}. No recipe carries enchantments (MITE_BALANCE 7.8) unless items.freeEnchants is on and there is no
 * OreDictionary (the logWood/treeSapling/... registrations of 1.7.10 live in twilightforest.compat.TFTags).
 */
public class TFRecipes {

    /** ItemStack subtype that matches every subtype in a recipe (was OreDictionary.WILDCARD_VALUE). */
    private static final int WILDCARD = 32767;

    private static boolean lateRecipesRegistered;

    public static void registerRecipes(RecipeRegistryEvent e) {
        // smelting for logs (the output item decides the XP: 0 for charcoal)
        FurnaceRecipes smelting = FurnaceRecipes.smelting();
        smelting.addSmelting(TFBlocks.log.blockID, new ItemStack(Item.coal, 1, 1));
        smelting.addSmelting(TFBlocks.magicLog.blockID, new ItemStack(Item.coal, 1, 1));

        // recipes
        shaped(e, new ItemStack(Block.planks, 4, 0), "w", 'w', new ItemStack(TFBlocks.log, 1, 0));
        shaped(e, new ItemStack(Block.planks, 4, 1), "w", 'w', new ItemStack(TFBlocks.log, 1, 1));
        shaped(e, new ItemStack(Block.planks, 4, 2), "w", 'w', new ItemStack(TFBlocks.log, 1, 2));
        shaped(e, new ItemStack(Block.planks, 4, 1), "w", 'w', new ItemStack(TFBlocks.log, 1, 3));

        shaped(e, enchanted(TFItems.plateNaga, Enchantment.fireProtection, 3), "# #", "###", "###", '#', TFItems.nagaScale);
        shaped(e, enchanted(TFItems.legsNaga, Enchantment.protection, 3), "###", "# #", "# #", '#', TFItems.nagaScale);

        shapeless(e, new ItemStack(TFBlocks.fireflyJar, 1, 0), TFBlocks.firefly, Item.glassBottle);

        // the used-up scepter / moonworm queen recharge recipes are custom recipes, see registerLateRecipes

        shapeless(e, new ItemStack(TFItems.magicMapFocus), TFItems.feather, TFItems.torchberries, Item.glowstone);
        shaped(e, new ItemStack(TFItems.emptyMagicMap), "###", "#X#", "###", '#', Item.paper, 'X', TFItems.magicMapFocus);
        shaped(e, new ItemStack(TFItems.emptyMazeMap), "###", "#X#", "###", '#', Item.paper, 'X', TFItems.mazeMapFocus);
        // balance 7.7: an (empty) maze map + gold ingot + iron ingot + lapis lazuli (was gold + diamond + iron block)
        shapeless(e, new ItemStack(TFItems.emptyOreMap), new ItemStack(TFItems.mazeMap, 1, WILDCARD), Item.ingotGold, Item.ingotIron,
            new ItemStack(Item.dyePowder, 1, 4));
        shapeless(e, new ItemStack(TFItems.emptyOreMap), new ItemStack(TFItems.emptyMazeMap, 1, WILDCARD), Item.ingotGold, Item.ingotIron,
            new ItemStack(Item.dyePowder, 1, 4));

        // TODO(mite-port): MITE has no generic arrow; the raven feather arrow recipe makes flint arrows (1.7.10: plain arrows)
        shaped(e, new ItemStack(Item.arrowFlint, 4), "X", "#", "Y", 'Y', TFItems.feather, 'X', Item.flint, '#', Item.stick);

        shapeless(e, new ItemStack(Item.stick), new ItemStack(TFBlocks.plant, 1, BlockTFPlant.META_ROOT_STRAND));
        // balance 7.7: 1 berry + 1 stick = 3 torches (was 5), config torchberry.torchesPerBerry
        shaped(e, new ItemStack(Block.torchWood, TFConfig.torchberryTorchesPerBerry), "B", "S", 'B', TFItems.torchberries, 'S', Item.stick);

        // balance 6.4/6.5: raw ironwood = liveroot + iron ingot + gold nugget, smelts to 1 ingot (was 2)
        shapeless(e, new ItemStack(TFItems.ironwoodRaw), TFItems.liveRoot, Item.ingotIron, Item.goldNugget);
        smelting.addSmelting(TFItems.ironwoodRaw.itemID, new ItemStack(TFItems.ironwoodIngot, TFConfig.ironwoodIngotsPerIron));

        // ironwood: no enchantments on the products (balance 7.8)
        shaped(e, enchanted(TFItems.ironwoodHelm, Enchantment.aquaAffinity, 1), "###", "# #", '#', TFItems.ironwoodIngot);
        shaped(e, enchanted(TFItems.ironwoodPlate, Enchantment.protection, 1), "# #", "###", "###", '#', TFItems.ironwoodIngot);
        shaped(e, enchanted(TFItems.ironwoodLegs, Enchantment.protection, 1), "###", "# #", "# #", '#', TFItems.ironwoodIngot);
        shaped(e, enchanted(TFItems.ironwoodBoots, Enchantment.featherFalling, 1), "# #", "# #", '#', TFItems.ironwoodIngot);
        shaped(e, enchanted(TFItems.ironwoodSword, Enchantment.knockback, 1), "#", "#", "X", '#', TFItems.ironwoodIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.ironwoodShovel, Enchantment.unbreaking, 1), "#", "X", "X", '#', TFItems.ironwoodIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.ironwoodPick, Enchantment.efficiency, 1), "###", " X ", " X ", '#', TFItems.ironwoodIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.ironwoodAxe, Enchantment.fortune, 1), "##", "#X", " X", '#', TFItems.ironwoodIngot, 'X', Item.stick);
        shaped(e, new ItemStack(TFItems.ironwoodHoe), "##", " X", " X", '#', TFItems.ironwoodIngot, 'X', Item.stick);

        shaped(e, new ItemStack(TFBlocks.uncraftingTable), "###", "#X#", "###", '#', Block.workbench, 'X', TFItems.mazeMapFocus);

        // venison / meef cook through ItemFood.setCookingResult (TFItems); explicit entries are harmless duplicates
        smelting.addSmelting(TFItems.venisonRaw.itemID, new ItemStack(TFItems.venisonCooked));
        smelting.addSmelting(TFItems.meefRaw.itemID, new ItemStack(TFItems.meefSteak));

        // fiery: blood or tears + iron ingot (blood/tears count 800 each, so the recipe adds up to a D16 ingot)
        shapeless(e, new ItemStack(TFItems.fieryIngot), TFItems.fieryBlood, Item.ingotIron);
        shapeless(e, new ItemStack(TFItems.fieryIngot), TFItems.fieryTears, Item.ingotIron);
        shaped(e, new ItemStack(TFItems.fieryHelm), "###", "# #", '#', TFItems.fieryIngot);
        shaped(e, new ItemStack(TFItems.fieryPlate), "# #", "###", "###", '#', TFItems.fieryIngot);
        shaped(e, new ItemStack(TFItems.fieryLegs), "###", "# #", "# #", '#', TFItems.fieryIngot);
        shaped(e, new ItemStack(TFItems.fieryBoots), "# #", "# #", '#', TFItems.fieryIngot);
        // balance 7.1: a stick handle instead of the blaze rod (no Fire Aspect on the recipe result)
        shaped(e, enchanted(TFItems.fierySword, Enchantment.fireAspect, 2), "#", "#", "X", '#', TFItems.fieryIngot, 'X', fieryHandle());
        shaped(e, new ItemStack(TFItems.fieryPick), "###", " X ", " X ", '#', TFItems.fieryIngot, 'X', fieryHandle());

        shaped(e, enchanted(TFItems.steeleafHelm, Enchantment.projectileProtection, 2), "###", "# #", '#', TFItems.steeleafIngot);
        shaped(e, enchanted(TFItems.steeleafPlate, Enchantment.blastProtection, 2), "# #", "###", "###", '#', TFItems.steeleafIngot);
        shaped(e, enchanted(TFItems.steeleafLegs, Enchantment.fireProtection, 2), "###", "# #", "# #", '#', TFItems.steeleafIngot);
        shaped(e, enchanted(TFItems.steeleafBoots, Enchantment.featherFalling, 2), "# #", "# #", '#', TFItems.steeleafIngot);
        shaped(e, enchanted(TFItems.steeleafSword, Enchantment.looting, 2), "#", "#", "X", '#', TFItems.steeleafIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.steeleafShovel, Enchantment.efficiency, 2), "#", "X", "X", '#', TFItems.steeleafIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.steeleafPick, Enchantment.fortune, 2), "###", " X ", " X ", '#', TFItems.steeleafIngot, 'X', Item.stick);
        shaped(e, enchanted(TFItems.steeleafAxe, Enchantment.efficiency, 2), "##", "#X", " X", '#', TFItems.steeleafIngot, 'X', Item.stick);
        shaped(e, new ItemStack(TFItems.steeleafHoe), "##", " X", " X", '#', TFItems.steeleafIngot, 'X', Item.stick);

        // the moonworm queen recharge is a custom recipe, see registerLateRecipes

        // charms: Keeping III and Life II are not craftable any more (balance 7.6); charmOfLifeCraftable restores Life II
        if (TFConfig.charmOfLifeCraftable) {
            shapeless(e, new ItemStack(TFItems.charmOfLife2), TFItems.charmOfLife1, TFItems.charmOfLife1, TFItems.charmOfLife1,
                TFItems.charmOfLife1);
        }
        shapeless(e, new ItemStack(TFItems.charmOfKeeping2), TFItems.charmOfKeeping1, TFItems.charmOfKeeping1, TFItems.charmOfKeeping1,
            TFItems.charmOfKeeping1);

        // dark tower recipes
        shaped(e, new ItemStack(TFBlocks.towerWood, 4, 0), "##", "##", '#', new ItemStack(TFBlocks.log, 1, 3));
        shaped(e, new ItemStack(TFBlocks.towerWood, 3, 1), "#", "#", "#", '#', new ItemStack(TFBlocks.towerWood, 1, 0));
        shaped(e, new ItemStack(TFItems.carminite), "brb", "rgr", "brb", 'b', new ItemStack(TFItems.borerEssence), 'r',
            new ItemStack(Item.redstone), 'g', new ItemStack(Item.ghastTear));
        shaped(e, new ItemStack(TFItems.carminite), "rbr", "bgb", "rbr", 'b', new ItemStack(TFItems.borerEssence), 'r',
            new ItemStack(Item.redstone), 'g', new ItemStack(Item.ghastTear));
        shaped(e, new ItemStack(TFBlocks.towerDevice, 8, BlockTFTowerDevice.META_VANISH_INACTIVE), "ewe", "wcw", "ewe", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'w', new ItemStack(TFBlocks.towerWood, 1, 0), 'c', new ItemStack(TFItems.carminite));
        shaped(e, new ItemStack(TFBlocks.towerDevice, 2, BlockTFTowerDevice.META_REAPPEARING_INACTIVE), "ere", "rcr", "ere", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'r', new ItemStack(Item.redstone), 'c', new ItemStack(TFItems.carminite));
        shaped(e, new ItemStack(TFBlocks.towerDevice, 1, BlockTFTowerDevice.META_BUILDER_INACTIVE), "ece", "cdc", "ece", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'd', new ItemStack(Block.dispenser), 'c', new ItemStack(TFItems.carminite));
        shaped(e, new ItemStack(TFBlocks.towerDevice, 1, BlockTFTowerDevice.META_REACTOR_INACTIVE), "ece", "coc", "ece", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'o', new ItemStack(Block.oreRedstone), 'c', new ItemStack(TFItems.carminite));
        shaped(e, new ItemStack(TFBlocks.fireJet, 1, BlockTFFireJet.META_ENCASED_SMOKER_OFF), "ere", "rsr", "ere", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'r', new ItemStack(Item.redstone), 's',
            new ItemStack(TFBlocks.fireJet, 1, BlockTFFireJet.META_SMOKER));
        shaped(e, new ItemStack(TFBlocks.fireJet, 1, BlockTFFireJet.META_ENCASED_JET_IDLE), "ere", "rjr", "lll", 'e',
            new ItemStack(TFBlocks.towerWood, 1, 1), 'r', new ItemStack(Item.redstone), 'l', new ItemStack(Item.bucketIronLava), 'j',
            new ItemStack(TFBlocks.fireJet, 1, BlockTFFireJet.META_JET_IDLE));

        shaped(e, new ItemStack(TFItems.shardCluster), "###", "###", "###", '#', TFItems.armorShard);

        // shard cluster smelts to a knightmetal ingot (heat level 3, see the furnace heat table in the balance spec)
        smelting.addSmelting(TFItems.shardCluster.itemID, new ItemStack(TFItems.knightMetal));

        shaped(e, new ItemStack(TFItems.knightlyHelm), "###", "# #", '#', TFItems.knightMetal);
        shaped(e, new ItemStack(TFItems.knightlyPlate), "# #", "###", "###", '#', TFItems.knightMetal);
        shaped(e, new ItemStack(TFItems.knightlyLegs), "###", "# #", "# #", '#', TFItems.knightMetal);
        shaped(e, new ItemStack(TFItems.knightlyBoots), "# #", "# #", '#', TFItems.knightMetal);

        shaped(e, new ItemStack(TFItems.knightlySword), "#", "#", "X", '#', TFItems.knightMetal, 'X', Item.stick);
        shaped(e, new ItemStack(TFItems.knightlyPick), "###", " X ", " X ", '#', TFItems.knightMetal, 'X', Item.stick);
        shaped(e, new ItemStack(TFItems.knightlyAxe), "##", "#X", " X", '#', TFItems.knightMetal, 'X', Item.stick);
        shaped(e, new ItemStack(TFItems.knightlyAxe), "##", "X#", "X ", '#', TFItems.knightMetal, 'X', Item.stick);

        // yeti / arctic: no enchantments on the products (balance 7.8)
        shaped(e, enchanted(TFItems.yetiHelm, Enchantment.protection, 2), "###", "# #", '#', TFItems.alphaFur);
        shaped(e, enchanted(TFItems.yetiPlate, Enchantment.protection, 2), "# #", "###", "###", '#', TFItems.alphaFur);
        shaped(e, enchanted(TFItems.yetiLegs, Enchantment.protection, 2), "###", "# #", "# #", '#', TFItems.alphaFur);
        shaped(e, enchanted(TFItems.yetiBoots, Enchantment.protection, 2, Enchantment.featherFalling, 4), "# #", "# #", '#', TFItems.alphaFur);

        shaped(e, new ItemStack(TFItems.arcticHelm), "###", "# #", '#', TFItems.arcticFur);
        shaped(e, new ItemStack(TFItems.arcticPlate), "# #", "###", "###", '#', TFItems.arcticFur);
        shaped(e, new ItemStack(TFItems.arcticLegs), "###", "# #", "# #", '#', TFItems.arcticFur);
        shaped(e, new ItemStack(TFItems.arcticBoots), "# #", "# #", '#', TFItems.arcticFur);

        shaped(e, new ItemStack(TFBlocks.auroraSlab, 6, 0), "###", '#', TFBlocks.auroraBlock);
        shaped(e, new ItemStack(TFBlocks.auroraPillar, 2, 0), "#", "#", '#', TFBlocks.auroraBlock);

        shaped(e, new ItemStack(TFItems.giantPick), "###", " X ", " X ", '#', TFBlocks.giantCobble, 'X', TFBlocks.giantLog);
        shaped(e, new ItemStack(TFItems.giantSword), "#", "#", "X", '#', TFBlocks.giantCobble, 'X', TFBlocks.giantLog);

        // balance 7.7: unpacking gives 16 (1.7.10: 64 cobblestone / 64 planks / 64 leaves); config giant.recipes
        if (TFConfig.giantRecipes) {
            shapeless(e, new ItemStack(Block.cobblestone, 16), new ItemStack(TFBlocks.giantCobble));
            shapeless(e, new ItemStack(Block.wood, 16), new ItemStack(TFBlocks.giantLog));
            shapeless(e, new ItemStack(Block.leaves, 16), new ItemStack(TFBlocks.giantLeaves));
        }

        shaped(e, new ItemStack(TFItems.knightmetalRing), " # ", "# #", " # ", '#', TFItems.knightMetal);
        shapeless(e, new ItemStack(TFItems.chainBlock), TFItems.knightmetalRing, TFItems.knightMetal, TFItems.knightMetal,
            TFItems.knightMetal, TFBlocks.knightmetalStorage);

        shapeless(e, new ItemStack(TFItems.knightMetal, 9), new ItemStack(TFBlocks.knightmetalStorage));
        shaped(e, new ItemStack(TFBlocks.knightmetalStorage), "###", "###", "###", '#', TFItems.knightMetal);

        // repair nuggets of the four metals: 1 ingot <-> 9 nuggets
        addNuggetRecipes(e, TFItems.ironwoodIngot, TFItems.ironwoodNugget);
        addNuggetRecipes(e, TFItems.steeleafIngot, TFItems.steeleafNugget);
        addNuggetRecipes(e, TFItems.knightMetal, TFItems.knightMetalNugget);
        addNuggetRecipes(e, TFItems.fieryIngot, TFItems.fieryNugget);
    }

    /**
     * Recipes that cannot go through the RecipeRegistryEvent because they are custom IRecipe objects: map cloning and
     * recharging used-up scepters / the moonworm queen (balance 7.5, 7.7). Has to be called once at the end of
     * CraftingManager.<init> with its (private) recipe list.
     * TODO(mite-port): needs a mixin on CraftingManager.<init> (RETURN) with @Shadow private List recipes that calls this method.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static synchronized void registerLateRecipes(List recipes) {
        if (lateRecipesRegistered) {
            return;
        }
        lateRecipesRegistered = true;
        recipes.add(new TFMapCloningRecipe(TFItems.magicMap, TFItems.emptyMagicMap));
        recipes.add(new TFMapCloningRecipe(TFItems.mazeMap, TFItems.emptyMazeMap));
        recipes.add(new TFMapCloningRecipe(TFItems.oreMap, TFItems.emptyOreMap));

        // used-up scepters + (balance 7.5): ender pearl + 3 gold nuggets / fermented spider eye + 3 gold nuggets /
        // 2 rotten flesh + 3 gold nuggets
        recipes.add(new TFRechargeRecipe(TFItems.scepterTwilight, true, 300.0F, Item.enderPearl, Item.goldNugget, Item.goldNugget, Item.goldNugget));
        recipes.add(new TFRechargeRecipe(TFItems.scepterLifeDrain, true, 300.0F, Item.fermentedSpiderEye, Item.goldNugget, Item.goldNugget,
            Item.goldNugget));
        recipes.add(new TFRechargeRecipe(TFItems.scepterZombie, true, 300.0F, Item.rottenFlesh, Item.rottenFlesh, Item.goldNugget,
            Item.goldNugget, Item.goldNugget));
        // moonworm queen: any damaged queen + 3 torchberries
        recipes.add(new TFRechargeRecipe(TFItems.moonwormQueen, false, 150.0F, TFItems.torchberries, TFItems.torchberries, TFItems.torchberries));
    }

    /** Furnace heat level override for TF smelting inputs (balance 6.4); -1 = no override. */
    public static int getHeatLevelOverride(int itemId) {
        if (TFItems.ironwoodRaw != null && itemId == TFItems.ironwoodRaw.itemID) {
            return 2;
        }
        if (TFItems.shardCluster != null && itemId == TFItems.shardCluster.itemID) {
            return 3;
        }
        return -1;
    }

    /**
     * Mixin-free variant: appends the late recipes to the live CraftingManager list (idempotent). Only usable once
     * CraftingManager has finished constructing, e.g. from the server-starting hook.
     */
    public static void registerLateRecipes() {
        registerLateRecipes(CraftingManager.getInstance().getRecipeList());
    }

    private static void addNuggetRecipes(RecipeRegistryEvent e, Item ingot, Item nugget) {
        shapeless(e, new ItemStack(nugget, 9), ingot);
        shaped(e, new ItemStack(ingot), "###", "###", "###", '#', nugget);
    }

    /** Result stack; carries the 1.7.10 enchantments only when items.freeEnchants is on (balance 7.8). */
    private static ItemStack enchanted(Item item, Object... enchantmentsAndLevels) {
        ItemStack stack = new ItemStack(item);
        if (TFConfig.itemsFreeEnchants) {
            for (int i = 0; i + 1 < enchantmentsAndLevels.length; i += 2) {
                stack.addEnchantment((Enchantment) enchantmentsAndLevels[i], (Integer) enchantmentsAndLevels[i + 1]);
            }
        }
        return stack;
    }

    /** Balance 7.1: stick handle; items.freeEnchants restores the 1.7.10 blaze rod. */
    private static Item fieryHandle() {
        return TFConfig.itemsFreeEnchants ? Item.blazeRod : Item.stick;
    }

    private static void shaped(RecipeRegistryEvent e, ItemStack result, Object... pattern) {
        e.registerShapedRecipe(result, true, pattern);
    }

    private static void shapeless(RecipeRegistryEvent e, ItemStack result, Object... ingredients) {
        e.registerShapelessRecipe(result, true, ingredients);
    }

    /**
     * Recharges a used-up tool: the damaged tool plus a fixed set of extra items (compared by item) gives the tool back
     * with full durability. MITE refuses damaged items in a grid unless the result is flagged as a repair.
     */
    public static class TFRechargeRecipe implements IRecipe {
        private final Item tool;
        /** true: only a completely used-up tool (damage at max - 1); false: any damaged tool. */
        private final boolean requireExhausted;
        private final Item[] extras;
        private float difficulty;
        private int[] skillsets;
        private Material materialToCheckToolBenchHardnessAgainst;

        public TFRechargeRecipe(Item tool, boolean requireExhausted, float difficulty, Item... extras) {
            this.tool = tool;
            this.requireExhausted = requireExhausted;
            this.difficulty = difficulty;
            this.extras = extras;
        }

        @Override
        public boolean matches(InventoryCrafting inventory, World world) {
            boolean foundTool = false;
            List<Item> remaining = new ArrayList<Item>();
            for (Item extra : this.extras) {
                remaining.add(extra);
            }
            for (int i = 0; i < inventory.getSizeInventory(); ++i) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack == null) {
                    continue;
                }
                if (stack.getItem() == this.tool) {
                    if (foundTool || !this.isRechargeable(stack)) {
                        return false;
                    }
                    foundTool = true;
                } else if (!remaining.remove(stack.getItem())) {
                    return false;
                }
            }
            return foundTool && remaining.isEmpty();
        }

        private boolean isRechargeable(ItemStack stack) {
            if (this.requireExhausted) {
                // 1.7.10: only a completely empty scepter (damage == max damage, see ItemTFTwilightWand) recharges
                return stack.getItemDamage() >= stack.getMaxDamage();
            }
            return stack.getItemDamage() > 0;
        }

        @Override
        public CraftingResult getCraftingResult(InventoryCrafting inventory) {
            return new CraftingResult(new ItemStack(this.tool), this.difficulty, this.skillsets, this).setExperienceCostExempt().setRepair();
        }

        @Override
        public int getRecipeSize() {
            return this.extras.length + 1;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return new ItemStack(this.tool);
        }

        @Override
        public ItemStack[] getComponents() {
            ItemStack[] components = new ItemStack[this.extras.length + 1];
            components[0] = new ItemStack(this.tool);
            for (int i = 0; i < this.extras.length; i++) {
                components[i + 1] = new ItemStack(this.extras[i]);
            }
            return components;
        }

        @Override
        public IRecipe setDifficulty(float difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        @Override
        public IRecipe scaleDifficulty(float factor) {
            this.difficulty *= factor;
            return this;
        }

        @Override
        public float getUnmodifiedDifficulty() {
            return this.difficulty;
        }

        @Override
        public void setIncludeInLowestCraftingDifficultyDetermination() {
        }

        @Override
        public boolean getIncludeInLowestCraftingDifficultyDetermination() {
            return false;
        }

        @Override
        public void setSkillsets(int[] skillsets) {
            this.skillsets = skillsets;
        }

        @Override
        public void setSkillset(int skillset) {
            this.skillsets = skillset == 0 ? null : new int[] {skillset};
        }

        @Override
        public int[] getSkillsets() {
            return this.skillsets;
        }

        @Override
        public void setMaterialToCheckToolBenchHardnessAgainst(Material material) {
            this.materialToCheckToolBenchHardnessAgainst = material;
        }

        @Override
        public Material getMaterialToCheckToolBenchHardnessAgainst() {
            return this.materialToCheckToolBenchHardnessAgainst;
        }
    }
}
