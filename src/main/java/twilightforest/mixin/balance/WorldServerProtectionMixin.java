package twilightforest.mixin.balance;

import net.minecraft.EntityPlayer;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;

/** MITE_BALANCE X13: structure protection also covers every canMineBlock query (placing and breaking), never attacks. */
@Mixin(WorldServer.class)
public abstract class WorldServerProtectionMixin {
    @Inject(method = "canMineBlock(Lnet/minecraft/EntityPlayer;III)Z", at = @At("HEAD"), cancellable = true)
    private void tf$protectStructures(EntityPlayer player, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && TFEvents.isBlockLocked((WorldServer) (Object) this, player, x, y, z)) {
            cir.setReturnValue(false);
        }
    }
}
