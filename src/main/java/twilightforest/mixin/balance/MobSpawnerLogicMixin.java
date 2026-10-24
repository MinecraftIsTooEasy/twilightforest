package twilightforest.mixin.balance;

import net.minecraft.Entity;
import net.minecraft.MobSpawnerBaseLogic;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.TFBalance;
import twilightforest.dimension.TFDimension;

/** MITE_BALANCE X10: a spawner inside the forest is exhausted after spawner.maxSpawns mobs (8 for wraith cages). */
@Mixin(MobSpawnerBaseLogic.class)
public abstract class MobSpawnerLogicMixin {
    @Unique private int tf$spawned;

    @Shadow public abstract World getSpawnerWorld();
    @Shadow public abstract String getEntityNameToSpawn();

    @Unique
    private boolean tf$inForest() {
        World world = this.getSpawnerWorld();
        return world != null && !world.isRemote && TFDimension.is(world);
    }

    @Inject(method = "updateSpawner()V", at = @At("HEAD"), cancellable = true)
    private void tf$exhausted(CallbackInfo ci) {
        if (this.tf$inForest() && this.tf$spawned >= TFBalance.maxSpawnsFor(this.getEntityNameToSpawn())) {
            ci.cancel();
        }
    }

    @Inject(method = "func_98265_a(Lnet/minecraft/Entity;)Lnet/minecraft/Entity;", at = @At("HEAD"))
    private void tf$countSpawn(Entity entity, CallbackInfoReturnable<Entity> cir) {
        if (this.tf$inForest()) {
            this.tf$spawned++;
        }
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/NBTTagCompound;)V", at = @At("RETURN"))
    private void tf$readSpawned(NBTTagCompound tag, CallbackInfo ci) {
        this.tf$spawned = tag.getInteger("tf_spawned");
    }

    @Inject(method = "writeToNBT(Lnet/minecraft/NBTTagCompound;)V", at = @At("RETURN"))
    private void tf$writeSpawned(NBTTagCompound tag, CallbackInfo ci) {
        if (this.tf$spawned > 0) {
            tag.setInteger("tf_spawned", this.tf$spawned);
        }
    }
}
