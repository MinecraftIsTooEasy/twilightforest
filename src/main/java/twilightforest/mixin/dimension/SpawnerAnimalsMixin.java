package twilightforest.mixin.dimension;

import net.minecraft.EnumCreatureType;
import net.minecraft.SpawnerAnimals;
import net.minecraft.World;
import net.minecraft.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.dimension.TFDimension;
import twilightforest.dimension.TFPortalSanctuary;

@Mixin(SpawnerAnimals.class)
public abstract class SpawnerAnimalsMixin {
    /** MITE hard-codes 0.25 for every non-overworld dimension; make the forest's rate tunable. */
    @Inject(method = "calcEffectiveHostileMobSpawningRateModifier", at = @At("HEAD"), cancellable = true)
    private void tf$hostileSpawnRate(WorldServer world, CallbackInfoReturnable<Float> cir) {
        if (TFDimension.is(world.provider.dimensionId)) {
            cir.setReturnValue(TFDimension.hostileSpawnRateModifier);
        }
    }

    /** Portal sanctuary (portal.sanctuaryRadius): no hostile spawn candidate is accepted next to a TF portal. */
    @Inject(method = "canCreatureTypeSpawnAtLocation", at = @At("HEAD"), cancellable = true)
    private static void tf$portalSanctuary(EnumCreatureType type, World world, int x, int y, int z, boolean initialSpawn,
                                           double[] resultingYPos, CallbackInfoReturnable<Boolean> cir) {
        if (type == EnumCreatureType.monster && TFPortalSanctuary.protects(world, x, y, z)) {
            cir.setReturnValue(false);
        }
    }
}
