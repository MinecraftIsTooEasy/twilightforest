package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemMap;
import net.minecraft.ItemRenderer;
import net.minecraft.ItemStack;
import net.minecraft.MapData;
import net.minecraft.MapItemRenderer;
import net.minecraft.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.client.TFItemRenderType;
import twilightforest.client.TFItemRenderer;
import twilightforest.client.TFItemRenderers;

/**
 * Forge IItemRenderer FIRST_PERSON_MAP. MITE's first-person view hard-codes Item.map twice (the itemID test
 * and getMapData) and MapItemRenderer.renderMap; for every ItemMap subclass the held item replaces Item.map,
 * and a FIRST_PERSON_MAP renderer (if any) replaces renderMap.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMapMixin {
    @Shadow private ItemStack itemToRender;

    @ModifyExpressionValue(method = "renderItemInFirstPerson",
            at = @At(value = "FIELD", target = "Lnet/minecraft/Item;map:Lnet/minecraft/ItemMap;"))
    private ItemMap tf$heldMapItem(ItemMap vanillaMap) {
        ItemStack held = this.itemToRender;
        return TFItemRenderers.handlesFirstPersonMap(held) ? (ItemMap) held.getItem() : vanillaMap;
    }

    @WrapOperation(method = "renderItemInFirstPerson",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/MapItemRenderer;renderMap(Lnet/minecraft/EntityPlayer;Lnet/minecraft/TextureManager;Lnet/minecraft/MapData;)V"))
    private void tf$renderHeldMap(MapItemRenderer renderer, EntityPlayer player, TextureManager textures, MapData data, Operation<Void> original) {
        TFItemRenderer custom = TFItemRenderers.get(this.itemToRender, TFItemRenderType.FIRST_PERSON_MAP);
        if (custom != null) {
            custom.render(TFItemRenderType.FIRST_PERSON_MAP, this.itemToRender, data, player, textures);
        } else {
            original.call(renderer, player, textures, data);
        }
    }
}
