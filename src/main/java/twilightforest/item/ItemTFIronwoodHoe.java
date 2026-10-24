package twilightforest.item;

import net.minecraft.Item;
import net.minecraft.Material;
import net.minecraft.ItemHoe;

public class ItemTFIronwoodHoe extends ItemHoe {

    public ItemTFIronwoodHoe(int id, Material material) {
        super(id, material);
        this.setTextureName("twilightforest:ironwoodHoe");
    }

    // The creative-tab stack carried no enchantment.

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
