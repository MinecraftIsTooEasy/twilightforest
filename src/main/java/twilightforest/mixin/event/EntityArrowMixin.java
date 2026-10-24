package twilightforest.mixin.event;

import net.minecraft.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import twilightforest.compat.ITFNoIFrames;

/** Lets any arrow be flagged as ignoring invulnerability frames; see EntityLivingBaseMixin. */
@Mixin(EntityArrow.class)
public abstract class EntityArrowMixin implements ITFNoIFrames {
    @Unique
    private boolean tf$noIFrames;

    @Override
    public boolean tf$isNoIFrames() {
        return this.tf$noIFrames;
    }

    @Override
    public void tf$setNoIFrames(boolean value) {
        this.tf$noIFrames = value;
    }
}
