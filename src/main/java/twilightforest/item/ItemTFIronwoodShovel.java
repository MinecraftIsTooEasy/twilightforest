package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.ItemShovel;

public class ItemTFIronwoodShovel extends ItemShovel {

    public ItemTFIronwoodShovel(int id, Material material) {
        super(id, material);
        this.setTextureName("twilightforest:ironwoodShovel");
    }

    // The creative-tab stack carried a free Unbreaking enchantment in 1.7.10; removed per MITE_BALANCE (ironwood: no free enchants).

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
