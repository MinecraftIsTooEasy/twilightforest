package twilightforest.item;

import twilightforest.TwilightForestMod;

public class ItemTFEnderBow extends ItemTFBowBase {

    public ItemTFEnderBow(int id) {
        super(id);
        this.setTextureName(TwilightForestMod.ID + ":enderbow");
        this.setMaxDamage(48); // MITE_BALANCE 7.4
    }

}
