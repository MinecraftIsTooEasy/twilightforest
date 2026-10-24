package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockFluid;
import net.minecraft.BlockPlant;
import net.minecraft.Entity;
import net.minecraft.Material;
import net.minecraft.World;

/** Large water lily plant growing on a water surface (decoration, no collision). */
public class BlockTFHugeWaterLily extends BlockPlant {

	public BlockTFHugeWaterLily(int id) {
		super(id, Material.plants);

		this.setStepSound(soundGrassFootstep);

		float radius = 0.4F;
		this.setBlockBoundsForAllThreads(0.5F - radius, 0.5F - radius, 0.5F - radius, 0.5F + radius, 0.5F + radius, 0.5F + radius);
	}

	/**
	 * No light requirement (BlockPlant would demand light 8..15).
	 */
	@Override
	public int getMinAllowedLightValue() {
		return 0;
	}

	@Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity) {
		return null;
	}

	/**
	 * Only legal on top of a full water block (1.7.10 canBlockStay / canPlaceBlockOn).
	 */
	@Override
	public boolean isLegalOn(int metadata, Block blockBelow, int blockBelowMetadata) {
		return BlockFluid.isFullWaterBlock(blockBelow, blockBelowMetadata, true);
	}

	@Override
	public boolean canBePlacedOnBlock(int metadata, Block blockBelow, int blockBelowMetadata, double blockBelowBoundsMaxY) {
		return this.isLegalOn(metadata, blockBelow, blockBelowMetadata);
	}
}
