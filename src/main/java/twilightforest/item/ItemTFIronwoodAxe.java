package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.ItemAxe;

public class ItemTFIronwoodAxe extends ItemAxe {

    public ItemTFIronwoodAxe(int id, Material material) {
        super(id, material);
        this.setTextureName("twilightforest:ironwoodAxe");
    }

    // The creative-tab stack carried a free Fortune enchantment in 1.7.10; removed per MITE_BALANCE (ironwood: no free enchants).

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
