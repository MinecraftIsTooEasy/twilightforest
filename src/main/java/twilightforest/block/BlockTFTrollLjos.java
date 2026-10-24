package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.Material;

/**
 * Troll lamp stone. Unused in the original mod (dead code), ported for completeness.
 */
public class BlockTFTrollLjos extends Block {

	protected BlockTFTrollLjos(int id) {
		super(id, Material.stone, new BlockConstants());

		// MITE_BALANCE B3: soft structure stone (hardness 2.4, harvest level 2 from Material.stone)
		this.setHardness(2.4F);
		this.setStepSound(soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);

		this.setLightValue(1.0F);
	}

	/**
	 * Uses the lit redstone lamp texture.
	 */
	@Override
	public Icon getIcon(int side, int metadata) {
		return Block.redstoneLampActive.getIcon(0, 0);
	}
}
