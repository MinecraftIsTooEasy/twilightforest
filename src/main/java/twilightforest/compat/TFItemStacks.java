package twilightforest.compat;

import net.minecraft.ItemStack;

/** ItemStack comparison helpers (Forge OreDictionary.itemMatches / ItemStack.isItemEqual replacements). */
public final class TFItemStacks {
    /** OreDictionary.WILDCARD_VALUE: matches any subtype in MITE recipes and in {@link #matches}. */
    public static final int WILDCARD_VALUE = 32767;

    private TFItemStacks() {
    }

    /** Same item id (null-safe, both null is false). */
    public static boolean sameItem(ItemStack a, ItemStack b) {
        return a != null && b != null && a.itemID == b.itemID;
    }

    /** Same item and subtype (the 1.7.10 metadata), ignoring stack size and NBT. */
    public static boolean sameItemAndSubtype(ItemStack a, ItemStack b) {
        return sameItem(a, b) && a.getItemSubtype() == b.getItemSubtype();
    }

    /** Same item, subtype, NBT and damage; stack size ignored. */
    public static boolean equalsIgnoreSize(ItemStack a, ItemStack b) {
        return ItemStack.areItemStacksEqual(a, b, true);
    }

    /** Wildcard-aware match: {@code pattern} subtype {@link #WILDCARD_VALUE} matches any subtype of the same item. */
    public static boolean matches(ItemStack pattern, ItemStack candidate) {
        if (!sameItem(pattern, candidate)) {
            return false;
        }
        return pattern.getItemSubtype() == WILDCARD_VALUE || pattern.getItemSubtype() == candidate.getItemSubtype();
    }

    /** Null-safe copy. */
    public static ItemStack copy(ItemStack s) {
        return s == null ? null : s.copy();
    }
}
