package twilightforest.mixin.network;

import net.minecraft.Minecraft;
import net.minecraft.NetClientHandler;
import net.minecraft.Packet131MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.network.TFPacketHandler;

/** Vanilla handleMapData only accepts Item.map; TF magic, maze and ore maps arrive in the same Packet131MapData. */
@Mixin(NetClientHandler.class)
public abstract class NetClientHandlerMapMixin {
    @Shadow
    private Minecraft mc;

    @Inject(method = "handleMapData", at = @At("HEAD"), cancellable = true)
    private void tf$handleTFMapData(Packet131MapData packet, CallbackInfo ci) {
        if (TFPacketHandler.handleMapData(packet, this.mc.theWorld)) {
            ci.cancel();
        }
    }
}
