package twilightforest.compat;

/**
 * Implemented by WorldProviderTwilightForest. Return types are Object so this common interface never names client
 * classes; the client mixin casts the results to twilightforest.client.TFRenderHandler. Null means vanilla rendering.
 */
public interface ITFProviderRenderers {
    Object tfSkyRenderer();

    Object tfWeatherRenderer();
}
