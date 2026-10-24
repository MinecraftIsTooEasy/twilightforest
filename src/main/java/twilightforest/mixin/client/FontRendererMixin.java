package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.FontRenderer;

/**
 * FishModLoader swaps {@code /font.txt} for a list that also holds the GBK characters, so
 * {@code ChatAllowedCharacters.allowedCharacters.indexOf(c)} can return values far beyond the 224 entries of the default
 * bitmap font ({@code charWidth} has 256 slots, indexed {@code index + 32}). With a non-unicode locale (en_US, de_DE, ...) any
 * chat line holding such a character (the Chinese load message, zh text from a resource pack) then crashes the client with an
 * ArrayIndexOutOfBoundsException. Treat out of range hits as "not in the bitmap font" so the unicode glyph path is used.
 * <p>
 * BetterGameSetting {@code @Overwrite}s {@code getCharWidth} (priority 1200) with a FontFixer delegate that has no
 * {@code indexOf} call; a lower priority injector into that merged method aborts the game, so this mixin runs above it and the
 * width wrap is optional.
 */
@Mixin(value = FontRenderer.class, priority = 1300)
public abstract class FontRendererMixin {
    private static final int TF_BITMAP_CHARS = 256 - 32;

    @WrapOperation(method = "getCharWidth(C)I",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I"), require = 0)
    private int tf$clampIndexWidth(String instance, int ch, Operation<Integer> original) {
        int index = original.call(instance, ch);
        return index >= TF_BITMAP_CHARS ? -1 : index;
    }

    @WrapOperation(method = "renderStringAtPos(Ljava/lang/String;Z)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;indexOf(I)I"))
    private int tf$clampIndexRender(String instance, int ch, Operation<Integer> original) {
        int index = original.call(instance, ch);
        return index >= TF_BITMAP_CHARS ? -1 : index;
    }
}
