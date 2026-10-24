package twilightforest.mixin.client;

import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Minecraft;
import net.minecraft.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.compat.ITFMeleeItem;

/** Client side of ITFMeleeItem.onLeftClickEntity (glass and ice sword particles); the result is ignored here. */
@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin {
    @Shadow private Minecraft mc;

    @Inject(method = "leftClickEntity(Lnet/minecraft/Entity;)V", at = @At("HEAD"))
    private void tf$onLeftClickEntity(Entity target, CallbackInfo ci) {
        EntityPlayer player = this.mc.thePlayer;
        ItemStack held = player == null ? null : player.getHeldItemStack();
        if (held != null && held.getItem() instanceof ITFMeleeItem melee) {
            melee.onLeftClickEntity(held, player, target);
        }
    }
}
