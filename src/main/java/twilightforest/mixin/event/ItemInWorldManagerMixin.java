package twilightforest.mixin.event;

import net.minecraft.Block;
import net.minecraft.ItemInWorldManager;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFEvents;
import twilightforest.compat.ITFBlockHooks;

/** BlockEvent.BreakEvent equivalent (progression protection, giant pickaxe) and Block.removedByPlayer. */
@Mixin(ItemInWorldManager.class)
public abstract class ItemInWorldManagerMixin {
    @Shadow public World theWorld;
    @Shadow public ServerPlayer thisPlayerMP;

    @Inject(method = "tryHarvestBlock(III)Z", at = @At("HEAD"), cancellable = true)
    private void tf$onHarvest(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (TFEvents.shouldCancelBreak(this.thisPlayerMP, this.theWorld, x, y, z)) {
            cir.setReturnValue(false);
            return;
        }
        if (TFEvents.onGiantPickBreak(this.thisPlayerMP, this.theWorld, x, y, z)) {
            cir.setReturnValue(true);
            return;
        }
        Block block = this.theWorld.getBlock(x, y, z);
        if (block instanceof ITFBlockHooks hooks && !this.thisPlayerMP.capabilities.isCreativeMode
            && hooks.tf$removedByPlayer(this.theWorld, this.thisPlayerMP, x, y, z)) {
            // the block replaced itself; false makes NetServerHandler resend the block to the client
            cir.setReturnValue(false);
        }
    }
}
