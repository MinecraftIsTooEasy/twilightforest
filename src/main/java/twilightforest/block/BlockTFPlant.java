package twilightforest.block;

import java.util.List;
import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockPlant;
import net.minecraft.ColorizerFoliage;
import net.minecraft.ColorizerGrass;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.IBlockAccess;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.compat.EnumPlantType;
import twilightforest.compat.IPlantable;
import twilightforest.compat.TFBlockUtil;
import twilightforest.compat.TFParticles;
import twilightforest.item.TFItems;

/**
 * Twilight Forest multi-plant block (1.7.10 extended BlockBush; MITE's BlockBush is a final blueberry bush, so this extends BlockPlant).
 * Shearing needs no code in MITE: ItemShears drops any canSilkHarvest (= carriable) plant block as itself.
 */
public class BlockTFPlant extends BlockPlant implements IPlantable {

	private static final boolean[] isGrassColor = {false, false, false, false, true, true, false, false, true, false, true, false, false, false, false, false};
	private static final String[] iconNames = new String[] {null, null, null, "mosspatch", "mayapple", "cloverpatch", null, null, "fiddlehead", "mushgloom", null, null, null, "torchberry", "rootstrand", null};
	/** Metadata values the block can have (those with a texture or a borrowed vanilla one). */
	private static final boolean[] validMeta = {false, false, false, true, true, true, false, false, true, true, true, true, false, true, true, false};

	private Icon[] icons;
	public static Icon mayappleSide;

	public static final int META_MOSSPATCH = 3;
	public static final int META_MAYAPPLE = 4;
	public static final int META_CLOVERPATCH = 5;
	public static final int META_FIDDLEHEAD = 8;
	public static final int META_MUSHGLOOM = 9;
	public static final int META_FORESTGRASS = 10;
	public static final int META_DEADBUSH = 11;
	public static final int META_TORCHBERRY = 13;
	public static final int META_ROOT_STRAND = 14;

	protected BlockTFPlant(int id) {
		super(id, Material.plants);
		float var3 = 0.4F;
		this.setBlockBoundsForAllThreads(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 0.8F, 0.5F + var3);
		this.setHardness(0.0F);
		this.setStepSound(soundGrassFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		// TODO(mite-port): 1.7.10 emitted light per metadata (mushgloom 3, torchberry 8) through getLightValue(IBlockAccess,..);
		// MITE light is static per block id. Needs either split block ids or a light mixin (MITE_BLOCK_ITEM 7.3).
	}

	/*
	 * Metadata / items
	 */

	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16 && validMeta[metadata];
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}

	/**
	 * Creative list of 1.7.10 getSubBlocks (MITE validate() needs it to match isValidMetadata, so the clover patch is listed too).
	 */
	@Override
	public void getItemStacks(int id, CreativeTabs tabs, List list) {
		list.add(new ItemStack(id, 1, META_MOSSPATCH));
		list.add(new ItemStack(id, 1, META_MAYAPPLE));
		list.add(new ItemStack(id, 1, META_CLOVERPATCH));
		list.add(new ItemStack(id, 1, META_FIDDLEHEAD));
		list.add(new ItemStack(id, 1, META_MUSHGLOOM));
		list.add(new ItemStack(id, 1, META_FORESTGRASS));
		list.add(new ItemStack(id, 1, META_DEADBUSH));
		list.add(new ItemStack(id, 1, META_TORCHBERRY));
		list.add(new ItemStack(id, 1, META_ROOT_STRAND));
	}

	/*
	 * Icons and colours
	 */

	@Override
	public Icon getIcon(int side, int metadata) {
		return this.icons[metadata & 15];
	}

	@Override
	public void registerIcons(IconRegister register) {
		this.icons = new Icon[iconNames.length];

		for (int i = 0; i < this.icons.length; ++i) {
			if (iconNames[i] != null) {
				this.icons[i] = register.registerIcon(TwilightForestMod.ID + ":" + iconNames[i]);
			}
		}

		this.icons[META_FORESTGRASS] = Block.tallGrass.getIcon(2, 1);
		this.icons[META_DEADBUSH] = Block.deadBush.getIcon(2, 0);

		BlockTFPlant.mayappleSide = register.registerIcon(TwilightForestMod.ID + ":mayapple_side");
	}

	@Override
	public int getBlockColor() {
		double var1 = 0.5D;
		double var3 = 1.0D;
		return ColorizerGrass.getGrassColor(var1, var3);
	}

	/**
	 * Returns the color this block should be rendered (inventory).
	 */
	@Override
	public int getRenderColor(int metadata) {
		return isGrassColor[metadata & 15] ? ColorizerFoliage.getFoliageColorBasic() : 0xFFFFFF;
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return isGrassColor[meta & 15] ? world.getBiomeGenForCoords(x, z).getBiomeGrassColor() : 0xFFFFFF;
	}

	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getPlantBlockRenderID();
	}

	/*
	 * Shape
	 */

	@Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);

		if (meta == META_MOSSPATCH) {
			long seed = x * 3129871 ^ y * 116129781L ^ z;
			seed = seed * seed * 42317861L + seed * 11L;

			int xOff0 = (int) (seed >> 12 & 3L);
			int xOff1 = (int) (seed >> 15 & 3L);
			int zOff0 = (int) (seed >> 18 & 3L);
			int zOff1 = (int) (seed >> 21 & 3L);

			boolean xConnect0 = isSame(world, x + 1, y, z, META_MOSSPATCH);
			boolean xConnect1 = isSame(world, x - 1, y, z, META_MOSSPATCH);
			boolean zConnect0 = isSame(world, x, y, z + 1, META_MOSSPATCH);
			boolean zConnect1 = isSame(world, x, y, z - 1, META_MOSSPATCH);

			this.setBlockBoundsForCurrentThread(xConnect1 ? 0F : (1F + xOff1) / 16F, 0.0F, zConnect1 ? 0F : (1F + zOff1) / 16F,
					xConnect0 ? 1F : (15F - xOff0) / 16F, 1F / 16F, zConnect0 ? 1F : (15F - zOff0) / 16F);
		} else if (meta == META_CLOVERPATCH) {
			long seed = x * 3129871 ^ y * 116129781L ^ z;
			seed = seed * seed * 42317861L + seed * 11L;

			int xOff0 = (int) (seed >> 12 & 3L);
			int xOff1 = (int) (seed >> 15 & 3L);
			int zOff0 = (int) (seed >> 18 & 3L);
			int zOff1 = (int) (seed >> 21 & 3L);

			int yOff0 = (int) (seed >> 24 & 1L);
			int yOff1 = (int) (seed >> 27 & 1L);

			boolean xConnect0 = isSame(world, x + 1, y, z, META_CLOVERPATCH);
			boolean xConnect1 = isSame(world, x - 1, y, z, META_CLOVERPATCH);
			boolean zConnect0 = isSame(world, x, y, z + 1, META_CLOVERPATCH);
			boolean zConnect1 = isSame(world, x, y, z - 1, META_CLOVERPATCH);

			this.setBlockBoundsForCurrentThread(xConnect1 ? 0F : (1F + xOff1) / 16F, 0.0F, zConnect1 ? 0F : (1F + zOff1) / 16F,
					xConnect0 ? 1F : (15F - xOff0) / 16F, (1F + yOff0 + yOff1) / 16F, zConnect0 ? 1F : (15F - zOff0) / 16F);
		} else if (meta == META_MAYAPPLE) {
			this.setBlockBoundsForCurrentThread(4F / 16F, 0, 4F / 16F, 13F / 16F, 6F / 16F, 13F / 16F);
		} else {
			this.setBlockBoundsForCurrentThread(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private boolean isSame(IBlockAccess world, int x, int y, int z, int meta) {
		return world.getBlock(x, y, z) == this && world.getBlockMetadata(x, y, z) == meta;
	}

	@Override
	public void setBlockBoundsForItemRender(int itemDamage) {
		this.setBlockBoundsForCurrentThread(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	/*
	 * Legality (1.7.10 canBlockStay / canPlaceBlockOn / canReplace)
	 */

	/** TF plants do not need light to survive (BlockPlant default is min light 8, which kills them under the canopy). */
	@Override
	public int getMinAllowedLightValue() {
		return 0;
	}

	@Override
	public boolean isLegalAt(World world, int x, int y, int z, int metadata) {
		Block soil = world.getBlock(x, y - 1, z);
		int soilMeta = world.getBlockMetadata(x, y - 1, z);

		switch (metadata) {
		case META_TORCHBERRY:
		case META_ROOT_STRAND:
			return BlockTFPlant.canPlaceRootBelow(world, x, y + 1, z);
		case 0: // let's make this happen
		case META_FORESTGRASS:
		case META_DEADBUSH:
		case META_MUSHGLOOM:
		case META_MOSSPATCH:
			return this.isLegalOn(metadata, soil, soilMeta);
		default:
			return (world.getFullBlockLightValue(x, y, z) >= 3 || world.canBlockSeeTheSky(x, y, z))
					&& this.isLegalOn(metadata, soil, soilMeta);
		}
	}

	@Override
	public boolean isLegalOn(int metadata, Block below, int belowMeta) {
		if (below == null) {
			return false;
		}
		switch (metadata) {
		case META_TORCHBERRY:
		case META_ROOT_STRAND:
			// hanging plants: the support is checked against the block above in isLegalAt
			return true;
		case META_MUSHGLOOM:
		case META_MOSSPATCH:
			return below.isFaceFlatAndSolid(belowMeta, net.minecraft.EnumFace.TOP);
		default:
			return TFBlockUtil.canSustainPlant(below, belowMeta, plantTypeOf(metadata));
		}
	}

	/**
	 * MITE's default demands a full-height solid block below (only farmland is exempt), which would reject the 15/16 high uberous
	 * soil that 1.7.10 allowed. Only the soil check (isLegalOn) applies, like Forge canPlaceBlockOnSide.
	 */
	@Override
	public boolean canBePlacedOnBlock(int metadata, Block blockBelow, int blockBelowMeta, double blockBelowBoundsMaxY) {
		return blockBelow != null && this.isLegalOn(metadata, blockBelow, blockBelowMeta);
	}

	/**
	 * Root-specific method.
	 * @return true if the root can be placed in the block immediately below this one
	 */
	public static boolean canPlaceRootBelow(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block != null && (block.blockMaterial == Material.dirt || block.blockMaterial == Material.grass)) {
			// can always hang below dirt blocks
			return true;
		} else {
			int blockMeta = world.getBlockMetadata(x, y, z);
			return (block == TFBlocks.plant && blockMeta == META_ROOT_STRAND) || (block == TFBlocks.root && blockMeta == BlockTFRoots.ROOT_META);
		}
	}

	/**
	 * 1.7.10 only had random ticks here to re-light the block, which did nothing. A tick must not run the MITE legality check
	 * (Block.updateTick) because the 1.7.10 block did not die from darkness on random ticks.
	 */
	@Override
	public boolean updateTick(World world, int x, int y, int z, Random rand) {
		return false;
	}

	/*
	 * Drops
	 */

	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		switch (info.getMetadata()) {
		case META_TORCHBERRY:
			return this.dropBlockAsEntityItem(info, new ItemStack(TFItems.torchberries));
		case META_MOSSPATCH:
		case META_MAYAPPLE:
		case META_CLOVERPATCH:
		case META_ROOT_STRAND:
		case META_FORESTGRASS:
		case META_DEADBUSH:
			// Just don't drop anything
			return 0;
		default:
			return this.dropBlockAsEntityItem(info, new ItemStack(this, 1, info.getMetadata()));
		}
	}

	@Override
	public boolean dropsAsSelfWhenTrampled(Entity entity) {
		return false;
	}

	/*
	 * IPlantable
	 */

	private static EnumPlantType plantTypeOf(int meta) {
		switch (meta) {
		case META_MOSSPATCH:
		case META_MUSHGLOOM:
			return EnumPlantType.Cave;
		default:
			return EnumPlantType.Plains;
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
		return plantTypeOf(world.getBlockMetadata(x, y, z));
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z) {
		return this;
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z);
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		super.randomDisplayTick(world, x, y, z, random);

		int meta = world.getBlockMetadata(x, y, z);

		if (meta == META_MOSSPATCH && random.nextInt(10) == 0) {
			TFParticles.spawn(world, "townaura", x + random.nextFloat(), y + 0.1F, z + random.nextFloat(), 0.0D, 0.0D, 0.0D);
		}
	}
}
