package twilightforest.compat;

import java.util.List;
import java.util.Set;
import moddedmite.rustedironcore.internal.config.RICConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Plugin of twilightforest.dimension.mixins.json (it must live outside the mixin package).
 *
 * The dimension plumbing of this mod (world slot 4, getWorldIndexForDimensionId, the {@code new WorldServer[4]} constants of
 * MinecraftServer/IntegratedServer, the WorldInfoShared time array, the DIM7 save folder) duplicates what RustedIronCore's
 * dimension API (mixin package "moddedmite.rustedironcore.mixin.dimension") does when "UseCustomDimension" is enabled in
 * RustedIronCore.json. RIC leaves that API switched off by default, and when it is on it also registers a demo dimension
 * ("test", slot 4). Both sets of mixins patch the same constants, which would give the world array the wrong size and put the
 * Twilight Forest into somebody else's slot, so refuse to start instead of corrupting a save.
 */
public class TFDimensionMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        boolean ricDimensions;
        try {
            ricDimensions = RICConfig.UseCustomDimension.get();
        } catch (Throwable t) {
            // RIC's config could not be read; its own mixin plugin has the same problem and treats it as "off"
            ricDimensions = false;
        }
        if (ricDimensions) {
            throw new IllegalStateException("Twilight Forest registers its own dimension and is not compatible with RustedIronCore's "
                + "custom dimension API. Set \"UseCustomDimension\" to false in RustedIronCore.json (it is false by default).");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
