package twilightforest.structures.mushroomtower;

import net.minecraft.MapGenStructureIO;

public class TFMushroomTowerPieces {

	// TODO(mite-port): MapGenStructureIO.func_143031_a is package-private, needs AW (same as TFStrongholdPieces)
	public static void registerPieces() {
        MapGenStructureIO.func_143031_a(ComponentTFMushroomTowerMain.class, "TFMTMai");
        MapGenStructureIO.func_143031_a(ComponentTFMushroomTowerWing.class, "TFMTWin");
        MapGenStructureIO.func_143031_a(ComponentTFMushroomTowerBridge.class, "TFMTBri");
        MapGenStructureIO.func_143031_a(ComponentTFMushroomTowerMainBridge.class, "TFMTMB");
        MapGenStructureIO.func_143031_a(ComponentTFTowerRoofMushroom.class, "TFMTRoofMush");
	}

}
