package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.Entity;
import net.minecraft.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.TFEvents;

/** Forge RenderGameOverlayEvent.Pre(HEALTHMOUNT): no mount health bar on the TF mounts that hurt their rider. */
@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin {
    // func_110327_a draws health, armor, air and the mount health; ridingEntity is read once, for the mount bar
    @ModifyExpressionValue(method = "func_110327_a",
            at = @At(value = "FIELD", target = "Lnet/minecraft/EntityClientPlayerMP;ridingEntity:Lnet/minecraft/Entity;"))
    private Entity tf$hideMountHealth(Entity riding) {
        return riding != null && TFEvents.shouldHideMountHealth(riding) ? null : riding;
    }
}
