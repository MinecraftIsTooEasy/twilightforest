package twilightforest.compat;

/** Implemented by TF blocks that sustain plants (Forge canSustainPlant); queried by {@link TFBlockUtil#canSustainPlant}. */
public interface ITFPlantSoil {
    boolean canSustainPlant(EnumPlantType type);
}
