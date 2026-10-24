package twilightforest.block;

import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.tileentity.TileEntityTFCicada;

public class BlockTFCicada extends BlockTFCritter {

	public static int sprCicada = 5;

	protected BlockTFCicada(int id) {
		super(id);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFCicada();
	}
}
