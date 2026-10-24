package twilightforest.client;

import java.util.Random;
import net.minecraft.Block;
import net.minecraft.EntityLivingBase;
import net.minecraft.Potion;
import net.minecraft.RenderBlocks;
import net.minecraft.RenderManager;
import net.minecraft.RendererLivingEntity;
import net.minecraft.TextureMap;
import org.lwjgl.opengl.GL11;

/**
 * Client-side event handlers, called from mixins in twilightforest.mixin.client. The Forge FOVUpdateEvent
 * handler is gone: MITE's ClientPlayer.getFOVMultiplier already zooms for every ItemBow subclass.
 */
public final class TFClientEvents {
    private static final Random random = new Random();
    private static RenderBlocks iceRenderBlocks;

    private TFClientEvents() {
    }

    /** Do ice effect on slowed monsters (RendererLivingEntity.doRenderLiving TAIL, same moment as Forge's RenderLivingEvent.Post). */
    public static void renderLivingPost(EntityLivingBase entity, RendererLivingEntity renderer, double x, double y, double z) {
        if (entity.getDataWatcher().getWatchableObjectInt(7) == Potion.moveSlowdown.getLiquidColor()
                && entity.getDataWatcher().getWatchableObjectByte(8) > 0) {
            renderIcedEntity(entity, renderer, x, y, z);
        }
    }

    /**
     * Render an entity with the ice effect.
     * This just displays a bunch of ice cubes around on their model
     */
    private static void renderIcedEntity(EntityLivingBase entity, RendererLivingEntity renderer, double x, double y, double z) {
        if (iceRenderBlocks == null) {
            iceRenderBlocks = new RenderBlocks();
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderManager.instance.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        random.setSeed(entity.entityId * entity.entityId * 3121 + entity.entityId * 45238971);

        // number of cubes
        int numCubes = (int) (entity.height / 0.4F);

        for (int i = 0; i < numCubes; i++) {
            GL11.glPushMatrix();
            float dx = (float) (x + random.nextGaussian() * 0.2F * entity.width);
            float dy = (float) (y + random.nextGaussian() * 0.2F * entity.height) + entity.height / 2F;
            float dz = (float) (z + random.nextGaussian() * 0.2F * entity.width);
            GL11.glTranslatef(dx, dy, dz);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            GL11.glRotatef(random.nextFloat() * 360F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(random.nextFloat() * 360F, 0.0F, 0.0F, 1.0F);

            iceRenderBlocks.renderBlockAsItem(Block.ice, 0, 1.0F);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
    }
}
