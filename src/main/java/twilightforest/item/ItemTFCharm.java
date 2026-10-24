package twilightforest.item;

import net.minecraft.CreativeTabs;
import net.minecraft.Material;

public class ItemTFCharm extends ItemTF {

    protected ItemTFCharm(int id) {
        super(id, Material.gold);
        this.setCreativeTab(CreativeTabs.tabMisc);
    }
}
