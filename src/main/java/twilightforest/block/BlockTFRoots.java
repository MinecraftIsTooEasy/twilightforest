package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.Material;
import twilightforest.TwilightForestMod;
import twilightforest.item.TFItems;

public class BlockTFRoots extends Block {

	public static Icon spRootSide;
	public static Icon spOreRootSide;

	public static final int ROOT_META = 0;
	public static final int OREROOT_META = 1;

	public BlockTFRoots(int id) {
		super(id, Material.wood, new BlockConstants());
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setHardness(2.0F);
		this.setStepSound(soundWoodFootstep);
	}

	@Override
	public String getMetadataNotes() {
		return "0=Roots, 1=Liveroot ore roots";
	}

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata == ROOT_META || metadata == OREROOT_META;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		if (meta == OREROOT_META) {
			return spOreRootSide;
		} else {
			return spRootSide;
		}
	}

	@Override
	public void registerIcons(IconRegister register) {
		BlockTFRoots.spRootSide = register.registerIcon(TwilightForestMod.ID + ":rootblock");
		BlockTFRoots.spOreRootSide = register.registerIcon(TwilightForestMod.ID + ":oreroots");
	}

	/**
	 * Roots drop 3-4 sticks, ore roots drop a liveroot (1.7.10 getItemDropped/damageDropped/quantityDropped).
	 * Silk harvests are routed to dropBlockAsItself by MITE and give the block with its own metadata.
	 */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		switch (info.getMetadata()) {
		case ROOT_META:
			return this.dropBlockAsEntityItem(info, Item.stick.itemID, 0, 3 + info.world.rand.nextInt(2), 1.0F);
		case OREROOT_META:
			return this.dropBlockAsEntityItem(info, TFItems.liveRoot);
		default:
			return super.dropBlockAsEntityItem(info);
		}
	}
}
