package twilightforest.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Achievement;
import net.minecraft.AchievementList;
import net.minecraft.GuiAchievements;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.GuiSmallButton;
import net.minecraft.StatCollector;
import net.minecraft.StatFileWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.TFAchievementPage;

/**
 * Forge achievement pages: a page button between the view mode and done buttons switches between the Minecraft tree
 * (every achievement not on the Twilight page, other mods' included) and the Twilight tree. The page is remembered
 * while the game runs.
 */
@Mixin(GuiAchievements.class)
public abstract class GuiAchievementsMixin extends GuiScreen {
    @Unique private static final int TF_PAGE_BUTTON = 70;
    @Unique private static boolean tf$twilightPage;
    @Unique private static List<Achievement> tf$minecraftPage;
    @Unique private static int tf$minecraftPageSource = -1;

    @Shadow protected double field_74117_m;
    @Shadow protected double field_74115_n;
    @Shadow protected double guiMapX;
    @Shadow protected double guiMapY;
    @Shadow protected double field_74124_q;
    @Shadow protected double field_74123_r;
    @Shadow GuiButton button_cycle_view_mode;

    @Unique private GuiButton tf$pageButton;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tf$centerOnPage(StatFileWriter writer, CallbackInfo ci) {
        tf$centerView();
    }

    /** Three 76 wide buttons across the pane bottom: view mode, page, done. */
    @Inject(method = "initGui", at = @At("TAIL"))
    private void tf$addPageButton(CallbackInfo ci) {
        int x = this.width / 2 - 118;
        int y = this.height / 2 + 74;
        this.button_cycle_view_mode.xPosition = x;
        this.button_cycle_view_mode.width = 76;
        for (Object o : this.buttonList) {
            GuiButton button = (GuiButton) o;
            if (button.id == 1) {
                button.xPosition = x + 160;
                button.width = 76;
            }
        }
        this.tf$pageButton = new GuiSmallButton(TF_PAGE_BUTTON, x + 80, y, 76, 20, "");
        this.buttonList.add(this.tf$pageButton);
        tf$updatePageButton();
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void tf$switchPage(GuiButton button, CallbackInfo ci) {
        if (button.id == TF_PAGE_BUTTON) {
            tf$twilightPage = !tf$twilightPage;
            tf$updatePageButton();
            tf$centerView();
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "genAchievementBackground",
            at = @At(value = "FIELD", target = "Lnet/minecraft/AchievementList;achievementList:Ljava/util/List;"))
    private List<?> tf$pageAchievements(List<?> all) {
        return tf$page();
    }

    // the scroll limits come from the extent of the shown page instead of every registered achievement
    @ModifyExpressionValue(method = {"drawScreen", "genAchievementBackground"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/GuiAchievements;guiMapTop:I"))
    private int tf$mapTop(int original) {
        return tf$extent(true, false) * 24 - 112;
    }

    @ModifyExpressionValue(method = {"drawScreen", "genAchievementBackground"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/GuiAchievements;guiMapLeft:I"))
    private int tf$mapLeft(int original) {
        return tf$extent(false, false) * 24 - 112;
    }

    @ModifyExpressionValue(method = {"drawScreen", "genAchievementBackground"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/GuiAchievements;guiMapBottom:I"))
    private int tf$mapBottom(int original) {
        return tf$extent(true, true) * 24 - 77;
    }

    @ModifyExpressionValue(method = {"drawScreen", "genAchievementBackground"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/GuiAchievements;guiMapRight:I"))
    private int tf$mapRight(int original) {
        return tf$extent(false, true) * 24 - 77;
    }

    @Unique
    private void tf$updatePageButton() {
        if (this.tf$pageButton != null) {
            this.tf$pageButton.displayString = StatCollector.translateToLocal(
                    tf$twilightPage ? "tf.gui.achievementPage.twilight" : "tf.gui.achievementPage.minecraft");
        }
    }

    /** Same centering as the constructor: open inventory on the Minecraft page, arrival on the Twilight page. */
    @Unique
    private void tf$centerView() {
        Achievement center = tf$twilightPage && TFAchievementPage.twilightArrival != null ? TFAchievementPage.twilightArrival : AchievementList.openInventory;
        this.field_74117_m = this.guiMapX = this.field_74124_q = center.displayColumn * 24 - 141 / 2 - 12;
        this.field_74115_n = this.guiMapY = this.field_74123_r = center.displayRow * 24 - 141 / 2;
    }

    @Unique
    private static List<Achievement> tf$page() {
        if (tf$twilightPage && !TFAchievementPage.PAGE.isEmpty()) {
            return TFAchievementPage.PAGE;
        }
        @SuppressWarnings("unchecked")
        List<Achievement> all = AchievementList.achievementList;
        if (tf$minecraftPage == null || tf$minecraftPageSource != all.size()) {
            List<Achievement> page = new ArrayList<Achievement>(all);
            page.removeAll(TFAchievementPage.PAGE);
            tf$minecraftPage = page;
            tf$minecraftPageSource = all.size();
        }
        return tf$minecraftPage;
    }

    @Unique
    private static int tf$extent(boolean column, boolean max) {
        int out = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Achievement a : tf$page()) {
            int v = column ? a.displayColumn : a.displayRow;
            out = max ? Math.max(out, v) : Math.min(out, v);
        }
        return out == Integer.MIN_VALUE || out == Integer.MAX_VALUE ? 0 : out;
    }
}
