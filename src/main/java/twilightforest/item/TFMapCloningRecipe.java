package twilightforest.item;

import net.minecraft.CraftingResult;
import net.minecraft.IRecipe;
import net.minecraft.InventoryCrafting;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.Skill;
import net.minecraft.World;

public class TFMapCloningRecipe implements IRecipe {
    public Item fullMapID;
    public Item blankMapID;

    private float difficulty = 100.0F;
    // vanilla uses {Skill.FINE_ARTS.id}; Skill.id is package-private, but id == 1 << index in the public Skill.list
    private int[] skillsets = new int[] {1 << java.util.Arrays.asList(Skill.list).indexOf(Skill.FINE_ARTS)};
    private Material materialToCheckToolBenchHardnessAgainst;

    public TFMapCloningRecipe(Item magicMap, Item emptyMagicMap) {
        this.fullMapID = magicMap;
        this.blankMapID = emptyMagicMap;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(InventoryCrafting par1InventoryCrafting, World par2World) {
        int var3 = 0;
        ItemStack var4 = null;

        for (int var5 = 0; var5 < par1InventoryCrafting.getSizeInventory(); ++var5) {
            ItemStack var6 = par1InventoryCrafting.getStackInSlot(var5);

            if (var6 != null) {
                if (var6.getItem() == this.fullMapID) {
                    if (var4 != null) {
                        return false;
                    }

                    var4 = var6;
                } else {
                    if (var6.getItem() != this.blankMapID) {
                        return false;
                    }

                    ++var3;
                }
            }
        }

        return var4 != null && var3 > 0;
    }

    /** Returns the result of this recipe */
    @Override
    public CraftingResult getCraftingResult(InventoryCrafting par1InventoryCrafting) {
        int var2 = 0;
        ItemStack var3 = null;

        for (int var4 = 0; var4 < par1InventoryCrafting.getSizeInventory(); ++var4) {
            ItemStack var5 = par1InventoryCrafting.getStackInSlot(var4);

            if (var5 != null) {
                if (var5.getItem() == this.fullMapID) {
                    if (var3 != null) {
                        return null;
                    }

                    var3 = var5;
                } else {
                    if (var5.getItem() != this.blankMapID) {
                        return null;
                    }

                    ++var2;
                }
            }
        }

        if (var3 != null && var2 >= 1) {
            // map id lives in the subtype in MITE
            ItemStack var6 = new ItemStack(this.fullMapID, var2 + 1, var3.getItemSubtype());

            if (var3.hasDisplayName()) {
                var6.setItemName(var3.getDisplayName());
            }

            return new CraftingResult(var6, this.difficulty, this.skillsets, this);
        } else {
            return null;
        }
    }

    /** Returns the size of the recipe area */
    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    @Override
    public ItemStack[] getComponents() {
        return null;
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
