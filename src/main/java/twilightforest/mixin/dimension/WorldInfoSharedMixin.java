package twilightforest.mixin.dimension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Arrays;
import net.minecraft.GameRules;
import net.minecraft.WorldInfoShared;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.dimension.TFDimension;

@Mixin(WorldInfoShared.class)
public abstract class WorldInfoSharedMixin {
    @Shadow private long[] totalTime;

    /** field initializer `totalTime = new long[4]` is inlined in all three ctors; grow it right after theGameRules is stored
     *  (before the NBT ctor reads Time0..Time<length-1>). level.dat then persists "Time4". */
    @WrapOperation(
        method = {"<init>()V", "<init>(Lnet/minecraft/NBTTagCompound;)V", "<init>(Lnet/minecraft/WorldSettings;Ljava/lang/String;)V"},
        at = @At(value = "FIELD", target = "Lnet/minecraft/WorldInfoShared;theGameRules:Lnet/minecraft/GameRules;", opcode = Opcodes.PUTFIELD))
    private void tf$growTotalTime(WorldInfoShared self, GameRules rules, Operation<Void> original) {
        original.call(self, rules);
        if (this.totalTime.length < TFDimension.WORLD_COUNT) {
            this.totalTime = Arrays.copyOf(this.totalTime, TFDimension.WORLD_COUNT);
        }
    }
}
