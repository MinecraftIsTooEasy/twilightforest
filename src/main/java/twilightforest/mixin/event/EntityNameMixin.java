package twilightforest.mixin.event;

import net.minecraft.Entity;
import net.minecraft.EntityList;
import net.minecraft.StatCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.TFCreatures;

/**
 * TF projectiles and parts are tracked through TFEntitySpawnRegistry and are not in EntityList, so Entity.getEntityName would show
 * "entity.generic.name" for them. Give them their own "entity.twilightforest:&lt;name&gt;.name" key (same scheme as the registered creatures).
 */
@Mixin(Entity.class)
public abstract class EntityNameMixin {
    @Inject(method = "getEntityName()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void tf$namedNonLiving(CallbackInfoReturnable<String> cir) {
        Entity self = (Entity) (Object) this;
        if (EntityList.getEntityString(self) != null) {
            return;
        }
        String name = TFCreatures.languageNameOf(self.getClass());
        if (name != null) {
            cir.setReturnValue(StatCollector.translateToLocal("entity." + name + ".name"));
        }
    }
}
