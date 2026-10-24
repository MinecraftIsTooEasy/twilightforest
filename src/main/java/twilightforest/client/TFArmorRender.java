package twilightforest.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.ItemArmor;
import net.minecraft.ModelBase;
import net.minecraft.ModelBiped;
import net.minecraft.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.item.ItemTFArcticArmor;
import twilightforest.item.ItemTFFieryArmor;
import twilightforest.item.ItemTFKnightlyArmor;
import twilightforest.item.ItemTFNagaArmor;
import twilightforest.item.ItemTFPhantomArmor;
import twilightforest.item.ItemTFSteeleafArmor;
import twilightforest.item.ItemTFYetiArmor;

/**
 * Replacement for Forge's ItemArmor.getArmorTexture/getArmorModel on the TF armors. MITE only has one
 * choke point for the texture (RenderBiped.func_110858_a) and none for the model, see RenderBipedMixin
 * and RenderPlayerMixin.
 */
public final class TFArmorRender {
    private static final Map<String, ResourceLocation> TEXTURES = new HashMap<>();

    private TFArmorRender() {
    }

    /** slot is the armor type: 0 helmet, 1 chestplate, 2 leggings, 3 boots. Null means "not a TF armor". */
    public static ResourceLocation texture(ItemArmor armor, int slot, String suffix) {
        if (suffix != null) {
            return null;
        }
        String file;
        if (armor instanceof ItemTFKnightlyArmor) {
            file = slot == 2 ? "knightly_2.png" : "knightly_1.png";
        } else if (armor instanceof ItemTFPhantomArmor) {
            file = "phantom_1.png";
        } else if (ItemTFYetiArmor.isYetiArmor(armor)) {
            file = slot == 1 || slot == 2 ? "yetiarmor_2.png" : "yetiarmor_1.png";
        } else if (ItemTFArcticArmor.isArcticArmor(armor)) {
            file = slot == 2 ? "arcticarmor_2.png" : "arcticarmor_1.png";
        } else if (ItemTFFieryArmor.isFieryArmor(armor)) {
            file = slot == 2 ? "fiery_2.png" : "fiery_1.png";
        } else if (isSteeleaf(armor)) {
            file = slot == 2 ? "steeleaf_2.png" : "steeleaf_1.png";
        } else if (armor instanceof ItemTFNagaArmor.Plate || armor instanceof ItemTFNagaArmor.Legs) {
            file = slot == 2 ? "naga_scale_2.png" : "naga_scale_1.png";
        } else {
            return null;
        }
        return TEXTURES.computeIfAbsent(file, f -> new ResourceLocation(TwilightForestMod.ARMOR_DIR + f));
    }

    private static boolean isSteeleaf(ItemArmor armor) {
        return armor instanceof ItemTFSteeleafArmor.Helm || armor instanceof ItemTFSteeleafArmor.Plate
                || armor instanceof ItemTFSteeleafArmor.Legs || armor instanceof ItemTFSteeleafArmor.Boots;
    }

    /** Null means "not a TF armor", the vanilla model is used. */
    public static ModelBiped model(ItemArmor armor, int slot) {
        if (armor instanceof ItemTFKnightlyArmor) {
            return (ModelBiped) TwilightForestMod.proxy.getKnightlyArmorModel(slot);
        } else if (armor instanceof ItemTFPhantomArmor) {
            return slot < 2 ? (ModelBiped) TwilightForestMod.proxy.getPhantomArmorModel(slot) : null;
        } else if (ItemTFYetiArmor.isYetiArmor(armor)) {
            return (ModelBiped) TwilightForestMod.proxy.getYetiArmorModel(slot);
        } else if (ItemTFArcticArmor.isArcticArmor(armor)) {
            return (ModelBiped) TwilightForestMod.proxy.getArcticArmorModel(slot);
        } else if (ItemTFFieryArmor.isFieryArmor(armor)) {
            return (ModelBiped) TwilightForestMod.proxy.getFieryArmorModel(slot);
        }
        return null;
    }

    /** What vanilla does for its own armor models: show only the parts of this slot and copy the pose flags. */
    public static void prepare(ModelBiped model, int pass, ModelBase main, ModelBiped mainBiped) {
        model.bipedHead.showModel = pass == 0;
        model.bipedHeadwear.showModel = pass == 0;
        model.bipedBody.showModel = pass == 1 || pass == 2;
        model.bipedRightArm.showModel = pass == 1;
        model.bipedLeftArm.showModel = pass == 1;
        model.bipedRightLeg.showModel = pass == 2 || pass == 3;
        model.bipedLeftLeg.showModel = pass == 2 || pass == 3;
        model.onGround = main.onGround;
        model.isRiding = main.isRiding;
        model.isChild = main.isChild;
        model.heldItemRight = mainBiped.heldItemRight;
        model.isSneak = mainBiped.isSneak;
        model.aimedBow = mainBiped.aimedBow;
    }
}
