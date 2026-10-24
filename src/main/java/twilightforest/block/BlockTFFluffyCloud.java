package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;

public class BlockTFFluffyCloud extends Block {

	public BlockTFFluffyCloud(int id) {
		// 1.7.10 Material.packedIce -> Material.ice
		super(id, Material.ice, new BlockConstants());
		this.setStepSound(soundClothFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(0.8F);

		this.setTextureName(TwilightForestMod.ID + ":fluffy_cloud");
	}
}
