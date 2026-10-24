package twilightforest.mixin.balance;

import net.minecraft.DamageSource;
import net.minecraft.EntityBlaze;
import net.minecraft.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.dimension.TFDimension;

/**
 * Blazes in the Twilight Forest (dark tower spawners, final castle) drop nether wart instead of blaze rods, so the TF does not
 * become a Nether-free blaze rod farm; the count roll (recently hit, 0..1 + looting) is the vanilla one. Nether blazes are untouched.
 */
@Mixin(EntityBlaze.class)
public abstract class BlazeDropMixin {
    @Inject(method = "dropFewItems", at = @At("HEAD"), cancellable = true)
    private void tf$netherWartInTF(boolean recentlyHit, DamageSource source, CallbackInfo ci) {
        EntityBlaze self = (EntityBlaze) (Object) this;
        if (!TFDimension.is(self.worldObj)) {
            return;
        }
        ci.cancel();
        if (recentlyHit) {
            int count = self.getRNG().nextInt(2 + source.getLootingModifier());
            for (int i = 0; i < count; i++) {
                self.dropItem(Item.netherStalkSeeds.itemID, 1);
            }
        }
    }
}
