package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.ItemSword;

public class ItemTFIronwoodSword extends ItemSword {

    public ItemTFIronwoodSword(int id, Material material) {
        super(id, material);
        this.setTextureName("twilightforest:ironwoodSword");
    }

    // The creative-tab stack carried a free Knockback enchantment in 1.7.10; removed per MITE_BALANCE (ironwood: no free enchants).

    /** Repair with ironwood nuggets on the anvil (MITE_BALANCE 6.7). */
    @Override
    public Item getRepairItem() {
        return TFItems.ironwoodNugget;
    }

    /** Ironwood uses the iron crafting-table / anvil gate (MITE_BALANCE 6.1, P0). */
    @Override
    public Material getHardestMetalMaterial() {
        return Material.iron;
    }

    @Override
    public Material getMaterialForRepairs() {
        return Material.iron;
    }

    @Override
    public boolean hasWoodenHandle() {
        return false;
    }
}
