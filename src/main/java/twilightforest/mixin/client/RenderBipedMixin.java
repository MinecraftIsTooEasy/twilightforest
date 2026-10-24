package twilightforest.mixin.client;

import net.minecraft.EntityLiving;
import net.minecraft.ItemArmor;
import net.minecraft.ItemStack;
import net.minecraft.ModelBase;
import net.minecraft.ModelBiped;
import net.minecraft.RenderBiped;
import net.minecraft.RenderLiving;
import net.minecraft.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.client.TFArmorRender;

/**
 * Armor texture and model hook for the TF armors on biped mobs. Not an @Overwrite, so it coexists with
 * mods that overwrite these methods; anything that is not a TF armor falls through to the original.
 */
@Mixin(value = RenderBiped.class, priority = 1100)
public abstract class RenderBipedMixin extends RenderLiving {
    @Shadow protected ModelBiped modelBipedMain;

    protected RenderBipedMixin(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    @Inject(method = "func_110858_a", at = @At("HEAD"), cancellable = true)
    private static void tf$armorTexture(ItemArmor armor, int slot, String suffix, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation texture = TFArmorRender.texture(armor, slot, suffix);
        if (texture != null) {
            cir.setReturnValue(texture);
        }
    }

    @Inject(method = "func_130006_a", at = @At("HEAD"), cancellable = true)
    private void tf$armorPass(EntityLiving entity, int pass, float partialTick, CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = entity.func_130225_q(3 - pass);
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
