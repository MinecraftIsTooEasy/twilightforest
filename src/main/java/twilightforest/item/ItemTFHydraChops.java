package twilightforest.item;

import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Potion;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFConfig;

public class ItemTFHydraChops extends ItemTFFood {

    public ItemTFHydraChops(int id, int satiation, int nutrition, boolean protein, boolean fats, boolean phytonutrients) {
        super(id, configured(0, satiation), configured(1, nutrition), protein, fats, phytonutrients);
        // balance: Regeneration I for 10 s (duration is in seconds, ItemFood.onEaten multiplies by 20), always applied
        this.setPotionEffect(Potion.regeneration.id, 10, 0, 1.0F);
    }

    /** food.hydraChop = "satiation/nutrition" (MITE_BALANCE 12 #26, default 16/16); malformed values fall back to the given default. */
    private static int configured(int index, int fallback) {
        try {
            String[] parts = TFConfig.foodHydraChop.split("/");
            return Math.max(0, Math.min(40, Integer.parseInt(parts[index].trim())));
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    @Override
    public void onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        // if the player is at zero food, achievement (checked before the food value is added)
        if (player.onServer() && player.getSatiation() <= 0) {
            player.triggerAchievement(TFAchievementPage.twilightHydraChop);
        }
        // then normal effects
        super.onItemUseFinish(stack, world, player);
    }
}
