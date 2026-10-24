package twilightforest.structures.trollcave;

import java.util.Random;

import net.minecraft.NBTTagCompound;
import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.StructureBoundingBox;
import twilightforest.structures.StructureTFComponent;

public class ComponentTFTrollCloud extends StructureTFComponent {

	private int size;
	private int height;

	public ComponentTFTrollCloud() { }
	
	public ComponentTFTrollCloud(int index, int x, int y, int z) {
		super(index);
		this.setCoordBaseMode(0);

		this.size = 40;
		this.height = 20;
		
		int radius = this.size / 2;
		this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, -radius, -this.height, -radius, this.size, this.height, this.size, 0);
	}
	
	/**
	 * Save to NBT
	 */
	@Override
	protected void func_143012_a(NBTTagCompound par1NBTTagCompound) {
		super.func_143012_a(par1NBTTagCompound);
		
        par1NBTTagCompound.setInteger("size", this.size);
        par1NBTTagCompound.setInteger("height", this.height);
	}

	/**
	 * Load from NBT
	 */
	@Override
	protected void func_143011_b(NBTTagCompound par1NBTTagCompound) {
		super.func_143011_b(par1NBTTagCompound);
        this.size = par1NBTTagCompound.getInteger("size");
        this.height = par1NBTTagCompound.getInteger("height");
	}

	@Override
	public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb) {
		placeCloud(world, sbb, 0, 0, 0, this.size - 1, 6, this.size - 1);

		return true;
	}

    protected void placeCloud(World world, StructureBoundingBox sbb, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.fillWithMetadataBlocks(world, sbb, minX, minY, minZ, maxX, maxY, maxZ, Block.glass /* TODO(mite-port): MITE 1.6.4 has no stained glass */, 0, Block.glass /* TODO(mite-port): MITE 1.6.4 has no stained glass */, 0, false); 
		this.fillWithMetadataBlocks(world, sbb, minX + 2, minY + 2, minZ + 2, maxX - 2, maxY - 1, maxZ - 2, Block.blockNetherQuartz, 0, Block.blockNetherQuartz, 0, false); 
		
	}

}
