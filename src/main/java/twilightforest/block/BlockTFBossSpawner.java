package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.BlockContainer;
import net.minecraft.EntityLivingBase;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.TFIds;
import twilightforest.tileentity.TileEntityTFHydraSpawner;
import twilightforest.tileentity.TileEntityTFKnightPhantomsSpawner;
import twilightforest.tileentity.TileEntityTFLichSpawner;
import twilightforest.tileentity.TileEntityTFMinoshroomSpawner;
import twilightforest.tileentity.TileEntityTFNagaSpawner;
import twilightforest.tileentity.TileEntityTFSnowQueenSpawner;
import twilightforest.tileentity.TileEntityTFTowerBossSpawner;

/**
 * Boss spawner block. The metadata picks the boss (0 naga, 1 lich, 2 hydra, 3 tower boss / ur-ghast, 4 knight phantoms,
 * 5 snow queen, 6 minoshroom). The block destroys itself when it spawns its boss.
 * <p>
 * MITE's {@code ITileEntityProvider.createNewTileEntity(World)} has no metadata, so a placeholder tile entity is created
 * and swapped for the right class on its first tick through {@link #verifyTileEntity(TileEntity)}.
 */
public class BlockTFBossSpawner extends BlockContainer {

	public static final int META_NAGA = 0;
	public static final int META_LICH = 1;
	public static final int META_HYDRA = 2;
	public static final int META_TOWER_BOSS = 3;
	public static final int META_KNIGHT_PHANTOMS = 4;
	public static final int META_SNOW_QUEEN = 5;
	public static final int META_MINOSHROOM = 6;
	/** Highest valid metadata */
	public static final int META_MAX = META_MINOSHROOM;

	public BlockTFBossSpawner(int id)
	{
		super(id, Material.stone, new BlockConstants().setNeverHidesAdjacentFaces());
		// balance B8: boss spawners are unbreakable (they vanish when they spawn their boss)
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
		// no creative tab: the block is never carried
	}

	@Override
	public boolean isValidMetadata(int meta)
	{
		return meta >= META_NAGA && meta <= META_MAX;
	}

	/**
	 * Tile entity for the given metadata (TF createTileEntity), null for unknown metadata.
	 */
	public static TileEntity createTileEntityForMeta(int metadata)
	{
		switch (metadata)
		{
		case META_NAGA:
			return new TileEntityTFNagaSpawner();
		case META_LICH:
			return new TileEntityTFLichSpawner();
		case META_HYDRA:
			return new TileEntityTFHydraSpawner();
		case META_TOWER_BOSS:
			return new TileEntityTFTowerBossSpawner();
		case META_KNIGHT_PHANTOMS:
			return new TileEntityTFKnightPhantomsSpawner();
		case META_SNOW_QUEEN:
			return new TileEntityTFSnowQueenSpawner();
		case META_MINOSHROOM:
			return new TileEntityTFMinoshroomSpawner();
		default:
			return null;
		}
	}

	/**
	 * Placeholder tile entity (no metadata available here); corrected by {@link #verifyTileEntity(TileEntity)}.
	 */
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTFNagaSpawner();
	}

	/**
	 * Called by the spawner tile entities every tick. Returns true when the tile entity is the right class for the
	 * block's metadata; otherwise swaps it (or removes it) and returns false.
	 */
	public static boolean verifyTileEntity(TileEntity te)
	{
		World world = te.getWorldObj();
		if (world == null)
		{
			return false;
		}
		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;
		if (world.getBlockId(x, y, z) != TFIds.BOSS_SPAWNER)
		{
			return false;
		}

		TileEntity wanted = createTileEntityForMeta(world.getBlockMetadata(x, y, z));
		if (wanted != null && wanted.getClass() == te.getClass())
		{
			return true;
		}

		world.removeBlockTileEntity(x, y, z);
		if (wanted != null)
		{
			world.setBlockTileEntity(x, y, z, wanted);
		}
		return false;
	}

	/** Never drops anything (1.7.10 getItemDropped null / quantityDropped 0). */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info)
	{
		return 0;
	}

	@Override
	public boolean canBeCarried()
	{
		return false;
	}

	@Override
	public boolean isPortable(World world, EntityLivingBase entity_living_base, int x, int y, int z)
	{
		return false;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int metadata)
	{
		return Block.mobSpawner.getIcon(side, metadata);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		; // don't load anything
	}
}
