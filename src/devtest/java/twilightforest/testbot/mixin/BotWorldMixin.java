package twilightforest.testbot.mixin;

import net.minecraft.Entity;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.testbot.TestBot;

/**
 * Dev only: lets the bot see every entity that is spawned on the server while a recording window is open. Projectiles (scepter bolts, thrown
 * bombs, arrows) hit their target and die within one or two ticks, so polling the entity list can miss them; this hook cannot.
 */
@Mixin(World.class)
public abstract class BotWorldMixin {
    @Inject(method = "spawnEntityInWorld", at = @At("HEAD"))
    private void tfbot$recordSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (TestBot.ENABLED && !((World) (Object) this).isRemote) {
            TestBot.onServerEntitySpawn(entity);
        }
    }
}
