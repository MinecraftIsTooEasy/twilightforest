package twilightforest.compat;

import net.minecraft.Block;
import net.minecraft.IBlockAccess;

/** Implemented only by TF plants; vanilla MITE plants do not implement it. */
public interface IPlantable {
    EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z);

    Block getPlant(IBlockAccess world, int x, int y, int z);

    int getPlantMetadata(IBlockAccess world, int x, int y, int z);
}
