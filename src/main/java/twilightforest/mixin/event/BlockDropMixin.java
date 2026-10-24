package twilightforest.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.EntityItem;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.TFEvents;

/** HarvestDropsEvent equivalent: every dropped unit goes through the private Block.dropBlockAsItem_do. */
@Mixin(Block.class)
public abstract class BlockDropMixin {
    @WrapOperation(method = "dropBlockAsEntityItem(Lnet/minecraft/BlockBreakInfo;IIIF)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/Block;dropBlockAsItem_do(Lnet/minecraft/BlockBreakInfo;Lnet/minecraft/ItemStack;)Lnet/minecraft/EntityItem;"))
    private EntityItem tf$modifyDrop(Block block, BlockBreakInfo info, ItemStack stack, Operation<EntityItem> original) {
        return original.call(block, info, TFEvents.onBlockDrop(info, stack));
    }
}
