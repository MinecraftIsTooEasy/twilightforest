package twilightforest.mixin.client;

import net.minecraft.Icon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import twilightforest.item.ItemTFMoonwormQueen;

@Mixin(ItemTFMoonwormQueen.class)
public interface ItemTFMoonwormQueenAccessor {
    @Accessor("icons")
    Icon[] tf$getIcons();
}
