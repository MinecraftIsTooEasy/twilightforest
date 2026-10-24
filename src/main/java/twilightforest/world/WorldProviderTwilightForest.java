package twilightforest.world;

import net.minecraft.ChunkCoordinates;
import net.minecraft.EntityLivingBase;
import net.minecraft.IChunkProvider;
import net.minecraft.MathHelper;
import net.minecraft.Vec3;
import net.minecraft.WorldProvider;
import twilightforest.client.renderer.TFSkyRenderer;
import twilightforest.client.renderer.TFWeatherRenderer;
import twilightforest.compat.ITFProviderRenderers;
import twilightforest.dimension.TFDimension;

/**
 * MITE counterpart of the 1.7.10 provider. Sky colour, star brightness, horizon and isDaytime live on World in MITE and are
 * applied by twilightforest.mixin.dimension.WorldMixin using the constants below; the sky/weather renderers are hooked by the
 * client mixins (RenderGlobal.renderSky/renderClouds, EntityRenderer.renderRainSnow).
 */
public class WorldProviderTwilightForest extends WorldProvider implements ITFProviderRenderers {
    public static final double SKY_COLOR_RED = 43 / 256.0;
    public static final double SKY_COLOR_GREEN = 46 / 256.0;
    public static final double SKY_COLOR_BLUE = 99 / 256.0;
    public static final float STAR_BRIGHTNESS = 1.0F;
    public static final double HORIZON = 32.0D;
    public static final float CLOUD_HEIGHT = 161.0F;
    /** Constant celestial angle: the forest is always in the same twilight. */
    public static final float CELESTIAL_ANGLE = 0.225F;

    /** Server side only; set by createChunkGenerator(). */
    public ChunkProviderTwilightForest chunkProvider;

    // Client only; created on first use so the common provider never loads the client classes on a dedicated server.
    private Object skyRenderer;
    private Object weatherRenderer;

    public WorldProviderTwilightForest() {
        super(TFDimension.id, TFDimension.NAME);
    }

    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public Vec3 getFogColor(float celestialAngle, float partialTicks, EntityLivingBase viewer) {
        float bright = MathHelper.clamp_float(MathHelper.cos(0.25F * (float) Math.PI * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);
        float red = 0.7529412F * (bright * 0.94F + 0.06F);
        float green = 1.0F * (bright * 0.94F + 0.06F);
        float blue = 0.8470588F * (bright * 0.91F + 0.09F);
        return this.worldObj.getWorldVec3Pool().getVecFromPool(red, green, blue);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return CELESTIAL_ANGLE;
    }

    @Override
    public void registerWorldChunkManager() {
        this.worldChunkMgr = new TFWorldChunkManager(this.worldObj);
    }

    /** MITE calls this once per World (WorldServer.createChunkProvider), so the instance can be stored directly. */
    @Override
    public IChunkProvider createChunkGenerator() {
        this.chunkProvider = new ChunkProviderTwilightForest(this.worldObj, TFDimension.getSeed(this.worldObj), this.worldObj.getWorldInfo().isMapFeaturesEnabled());
        return this.chunkProvider;
    }

    public ChunkProviderTwilightForest getChunkProvider() {
        return this.chunkProvider;
    }

    @Override
    public boolean isSkyColored() {
        return false;
    }

    @Override
    public int getAverageGroundLevel() {
        return 30;
    }

    /** Lies until the world is initialised, otherwise the server generates a whole spawn area for a dimension nobody spawns in. */
    @Override
    public boolean canRespawnHere() {
        return this.worldObj.getWorldInfo().isInitialized();
    }

    /** y levels below the surface fade to black like in 1.7.10 (needs World.hasSkylight, fixed by WorldMixin). */
    @Override
    public boolean getWorldHasVoidFog() {
        return true;
    }

    @Override
    public boolean drawGuiVignette() {
        return false;
    }

    /** Never null: ServerConfigurationManager.transferEntityToWorld falls back to it for unknown destinations. */
    @Override
    public ChunkCoordinates getEntrancePortalLocation() {
        ChunkCoordinates spawn = this.worldObj.getSpawnPoint();
        return new ChunkCoordinates(spawn.posX, this.getAverageGroundLevel() + 64, spawn.posZ);
    }

    @Override
    public float getCloudHeight() {
        return CLOUD_HEIGHT;
    }

    @Override
    public Object tfSkyRenderer() {
        if (this.skyRenderer == null) {
            this.skyRenderer = new TFSkyRenderer();
        }
        return this.skyRenderer;
    }

    @Override
    public Object tfWeatherRenderer() {
        if (this.weatherRenderer == null) {
            this.weatherRenderer = new TFWeatherRenderer();
        }
        return this.weatherRenderer;
    }
}
