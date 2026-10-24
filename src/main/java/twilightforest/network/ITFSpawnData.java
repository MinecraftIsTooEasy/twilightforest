package twilightforest.network;

/**
 * Replacement for Forge's IEntityAdditionalSpawnData for non-living TF entities. The value rides in the
 * {@code throwerEntityId} int of Packet23VehicleSpawn, so it must be positive and fit 31 bits
 * (EntityTFSlideBlock: {@code blockId | meta << 16}).
 */
public interface ITFSpawnData {
    int tf$writeSpawnData();

    void tf$readSpawnData(int data);
}
