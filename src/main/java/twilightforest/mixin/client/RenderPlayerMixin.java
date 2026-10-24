package twilightforest.mixin.client;

import net.minecraft.AbstractClientPlayer;
import net.minecraft.ItemArmor;
import net.minecraft.ItemStack;
import net.minecraft.ModelBase;
import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.RenderPlayer;
import net.minecraft.RendererLivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFArmorRender;

/** Armor model hook for the TF armors on players; the texture goes through RenderBipedMixin. */
@Mixin(value = RenderPlayer.class, priority = 1100)
public abstract class RenderPlayerMixin extends RendererLivingEntity {
    @Shadow private ModelBiped modelBipedMain;

    protected RenderPlayerMixin(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    @Inject(method = "setArmorModel", at = @At("HEAD"), cancellable = true)
    private void tf$armorPass(AbstractClientPlayer player, int pass, float partialTick, CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = player.inventory.armorItemInSlot(3 - pass);
        if (stack == null || !(stack.getItem() instanceof ItemArmor)) {
            return;
        }
        ItemArmor armor = (ItemArmor) stack.getItem();
        ModelBiped model = TFArmorRender.model(armor, pass);
        if (model == null) {
            return;
        }
        this.bindTexture(RenderBiped.func_110857_a(armor, pass));
        TFArmorRender.prepare(model, pass, this.mainModel, this.modelBipedMain);
        this.setRenderPassModel(model);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        cir.setReturnValue(stack.isItemEnchanted() ? 15 : 1);
    }
}
