package twilightforest.mixin.network;

import net.minecraft.Packet51MapChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * getMapChunkData packs a chunk into the static 196864 byte temp buffer: 16 sections of ids, metadata, block light, sky light and
 * the high id nibbles, plus the biomes. MITE also appends skylight_bottom (256 bytes) when the world has sky, so a sky world chunk
 * whose 16 sections all hold ids above 255 overflows it. Every TF block is above 255, so tall TF chunks crash the server tick;
 * size the buffer for the real maximum (the client side read already grows its own copy on demand).
 */
@Mixin(Packet51MapChunk.class)
public abstract class Packet51MapChunkBufferMixin {
    private static final int TF_MAX_CHUNK_DATA = 16 * (4096 + 4 * 2048) + 256 + 256;

    @Shadow
    private static byte[] temp;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tf$growTemp(CallbackInfo ci) {
        if (temp.length < TF_MAX_CHUNK_DATA) {
            temp = new byte[TF_MAX_CHUNK_DATA];
        }
    }
}
