package twilightforest.mixin.dimension;

import java.io.File;
import net.minecraft.SaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** SaveHandler.getWorldDirectory is protected; an invoker avoids an access widener line. */
@Mixin(SaveHandler.class)
public interface SaveHandlerAccessor {
    @Invoker("getWorldDirectory")
    File tf$getWorldDirectory();
}
