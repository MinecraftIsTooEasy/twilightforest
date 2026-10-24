package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockBreakable;
import net.minecraft.BlockConstants;
import net.minecraft.Entity;
import net.minecraft.EntityLightningBolt;
import net.minecraft.EntityPlayer;
import net.minecraft.IBlockAccess;
import net.minecraft.IconRegister;
import net.minecraft.Icon;
import net.minecraft.Material;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFParticles;
import twilightforest.dimension.TFDimension;

public class BlockTFPortal extends BlockBreakable
{

    public BlockTFPortal(int id)
    {
        super(id, "twilightforest:TFPortal", Material.portal, false, new BlockConstants().setUsesAlphaBlending());
        this.setHardness(-1F);
        this.setStepSound(Block.soundGlassFootstep);
        this.setLightValue(0.75F);
		//this.setCreativeTab(TFItems.creativeTab);
    }

    /** No collision box: entities walk straight into the portal. */
    @Override
	public Object getCollisionBounds(World world, int x, int y, int z, Entity entity)
    {
        return null;
    }

    @Override
	public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess iblockaccess, int i, int j, int k)
    {
        this.setBlockBoundsForCurrentThread(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);
    }

    @Override
	public boolean isSolid(boolean[] is_solid, int metadata)
    {
        return false;
    }

    @Override
	public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata)
    {
        return false;
    }

    /** Portal blocks are never carried or dropped (1.7.10 quantityDropped == 0). */
    @Override
	public boolean canBeCarried()
    {
        return false;
    }

    @Override
	public int dropBlockAsEntityItem(BlockBreakInfo info)
    {
        return 0;
    }

    /** Plain textured quads (type 0) in the translucent pass; no vanilla portal / lava style special rendering. */
    @Override
    public int getRenderType()
    {
        return 0;
    }

    /** The purple sprite is drawn untinted: never a biome / foliage / nether colour. */
    @Override
    public int getRenderColor(int meta)
    {
        return 0xFFFFFF;
    }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z)
    {
        return 0xFFFFFF;
    }

    /** Own animated purple portal texture; MITE's Block.portal icons for metadata 2 are the red nether portal. */
    @Override
    public Icon getIcon(int side, int meta)
    {
        return this.blockIcon;
    }

    /** World rendering asks for the per-position icon first; always the purple TF portal sprite. */
    @Override
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        return this.blockIcon;
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("twilightforest:TFPortal");
    }

    /**
     * The function name says it all.  Tries to create a portal at the specified location.
     * In this case, the location is the location of a pool with very specific parameters.
     */
    public boolean tryToCreatePortal(World world, int dx, int dy, int dz)
    {
    	if (isGoodPortalPool(world, dx, dy, dz))
    	{
    		world.addWeatherEffect(new EntityLightningBolt(world, dx, dy, dz));

    		transmuteWaterToPortal(world, dx, dy, dz);

    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    /**
     * Changes the pool it's been given to all portal.  No checks done, only does 4 squares.
     */
    public void transmuteWaterToPortal(World world, int dx, int dy, int dz)
    {
    	int px = dx;
    	int pz = dz;

    	// adjust so that the other 3 water squares are in the +x, +z directions.
    	if (world.getBlockMaterial(px - 1, dy, pz) == Material.water)
    	{
    		px--;
    	}
    	if (world.getBlockMaterial(px, dy, pz - 1) == Material.water)
    	{
    		pz--;
    	}

    	world.setBlock(px + 0, dy, pz + 0, TFBlocks.portal.blockID, 0, 2);
    	world.setBlock(px + 1, dy, pz + 0, TFBlocks.portal.blockID, 0, 2);
    	world.setBlock(px + 1, dy, pz + 1, TFBlocks.portal.blockID, 0, 2);
    	world.setBlock(px + 0, dy, pz + 1, TFBlocks.portal.blockID, 0, 2);
    }

    /**
     * If this spot, or a spot in any one of the 8 directions around me is good, we're good.
     */
    public boolean isGoodPortalPool(World world, int dx, int dy, int dz)
    {
    	boolean flag = false;

    	flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz + 0);

    	flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz - 1);
    	flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz - 1);
    	flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz - 1);

    	flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz + 0);
    	flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz + 0);

    	flag |= isGoodPortalPoolStrict(world, dx - 1, dy, dz + 1);
    	flag |= isGoodPortalPoolStrict(world, dx + 0, dy, dz + 1);
    	flag |= isGoodPortalPoolStrict(world, dx + 1, dy, dz + 1);

    	return flag;
    }

    /**
     * Returns true only if there is water here, and at dx + 1, dy + 1, grass surrounding it, and solid beneath.
     *
     *
     *   GGGG
     *   G+wG
     *   GwwG
     *   GGGG
     *
     *
     */
    public boolean isGoodPortalPoolStrict(World world, int dx, int dy, int dz)
    {
    	boolean flag = true;

    	// 4 squares of water
    	flag &= world.getBlockMaterial(dx + 0, dy, dz + 0) == Material.water;
    	flag &= world.getBlockMaterial(dx + 1, dy, dz + 0) == Material.water;
    	flag &= world.getBlockMaterial(dx + 1, dy, dz + 1) == Material.water;
    	flag &= world.getBlockMaterial(dx + 0, dy, dz + 1) == Material.water;

    	// grass in the 12 squares surrounding
    	flag &= isGrassOrDirt(world, dx - 1, dy, dz - 1);
    	flag &= isGrassOrDirt(world, dx - 1, dy, dz + 0);
    	flag &= isGrassOrDirt(world, dx - 1, dy, dz + 1);
    	flag &= isGrassOrDirt(world, dx - 1, dy, dz + 2);

    	flag &= isGrassOrDirt(world, dx + 0, dy, dz - 1);
    	flag &= isGrassOrDirt(world, dx + 1, dy, dz - 1);

    	flag &= isGrassOrDirt(world, dx + 0, dy, dz + 2);
    	flag &= isGrassOrDirt(world, dx + 1, dy, dz + 2);

    	flag &= isGrassOrDirt(world, dx + 2, dy, dz - 1);
    	flag &= isGrassOrDirt(world, dx + 2, dy, dz + 0);
    	flag &= isGrassOrDirt(world, dx + 2, dy, dz + 1);
    	flag &= isGrassOrDirt(world, dx + 2, dy, dz + 2);

    	// solid underneath
    	flag &= world.getBlockMaterial(dx + 0, dy - 1, dz + 0).isSolid();
    	flag &= world.getBlockMaterial(dx + 1, dy - 1, dz + 0).isSolid();
    	flag &= world.getBlockMaterial(dx + 1, dy - 1, dz + 1).isSolid();
    	flag &= world.getBlockMaterial(dx + 0, dy - 1, dz + 1).isSolid();

    	// 12 nature blocks above the grass?
    	flag &= isNatureBlock(world, dx - 1, dy + 1, dz - 1);
    	flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 0);
    	flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 1);
    	flag &= isNatureBlock(world, dx - 1, dy + 1, dz + 2);

    	flag &= isNatureBlock(world, dx + 0, dy + 1, dz - 1);
    	flag &= isNatureBlock(world, dx + 1, dy + 1, dz - 1);

    	flag &= isNatureBlock(world, dx + 0, dy + 1, dz + 2);
    	flag &= isNatureBlock(world, dx + 1, dy + 1, dz + 2);

    	flag &= isNatureBlock(world, dx + 2, dy + 1, dz - 1);
    	flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 0);
    	flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 1);
    	flag &= isNatureBlock(world, dx + 2, dy + 1, dz + 2);

    	return flag;
    }

    /**
     * Does the block at this location count as a "nature" block for portal purposes?
     */
    public boolean isNatureBlock(World world, int dx, int dy, int dz)
    {
    	Material mat = world.getBlockMaterial(dx, dy, dz);

    	// plants = tallgrass
    	// vine = flower
    	return mat == Material.plants || mat == Material.vine || mat == Material.tree_leaves;
    }


    /**
     * Each twilight portal pool block should have grass or dirt on one side and a portal on the other.  If this is not true, delete this block, presumably causing a chain reaction.
     */
    @Override
	public boolean onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId)
    {
    	boolean good = true;

    	if (world.getBlockId(x - 1, y, z) == this.blockID) {
    		good &= isGrassOrDirt(world, x + 1, y, z);
    	}
    	else if (world.getBlockId(x + 1, y, z) == this.blockID) {
    		good &= isGrassOrDirt(world, x - 1, y, z);
    	}
    	else
    	{
    		good = false;
    	}

    	if (world.getBlockId(x, y, z - 1) == this.blockID) {
    		good &= isGrassOrDirt(world, x, y, z + 1);
    	}
    	else if (world.getBlockId(x, y, z + 1) == this.blockID) {
    		good &= isGrassOrDirt(world, x, y, z - 1);
    	}
    	else
    	{
    		good = false;
    	}

    	// if we're not good, remove this block
    	if (!good)
    	{
    		return world.setBlock(x, y, z, Block.waterMoving.blockID, 0, 3);
    	}
    	return false;
    }

    protected boolean isGrassOrDirt(World world, int dx, int dy, int dz)
    {
    	Material mat = world.getBlockMaterial(dx, dy, dz);
    	// grass = grass
    	// dirt = 1.7.10 Material.ground
    	return mat == Material.grass || mat == Material.dirt;
    }

    @Override
	public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
    {
    	if (entity.ridingEntity != null || entity.riddenByEntity != null)
    	{
    		return;
    	}

    	if (entity instanceof ServerPlayer && entity.timeUntilPortal <= 0 && entity.dimension != TFDimension.id)
    	{
    		ServerPlayer playerMP = (ServerPlayer) entity;
    		playerMP.triggerAchievement(TFAchievementPage.twilightPortal);
    		playerMP.triggerAchievement(TFAchievementPage.twilightArrival);
    		TwilightForestMod.LOGGER.info("Player touched the portal block.  Sending the player to dimension {}", TFDimension.id);
    	}

    	// MITE: travel runs through Entity.setInPortal / Entity.onEntityUpdate (portal delay, cooldown and the TeleporterMixin
    	// that routes to TFTeleporter), replacing the 1.7.10 direct transferPlayerToDimension / transferEntityToWorld calls.
    	// TODO(mite-port): 1.7.10 also set the player's TF-dimension respawn point here; MITE's setSpawnChunk has no dimension
    	// parameter and would turn TF coordinates into an overworld bed location (MITE_DIMENSION 1.8).
    	TFDimension.travel(entity);
    }

    /**
     * Sends a non-player entity through the portal. The 1.7.10 copy of Entity.travelToDimension is replaced by MITE's own,
     * which already uses the TF teleporter through the dimension mixins.
     */
	public void sendEntityToDimension(Entity entity, int par1) {
		entity.travelToDimension(par1);
	}

    @Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random)
    {
        if(random.nextInt(100) == 0)
        {
            world.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, "portal.portal", 1.0F, random.nextFloat() * 0.4F + 0.8F);
        }
        for(int l = 0; l < 4; l++)
        {
            double d = i + random.nextFloat();
            double d1 = j + random.nextFloat();
            double d2 = k + random.nextFloat();
            double d3 = 0.0D;
            double d4 = 0.0D;
            double d5 = 0.0D;
            int i1 = random.nextInt(2) * 2 - 1;
            d3 = (random.nextFloat() - 0.5D) * 0.5D;
            d4 = (random.nextFloat() - 0.5D) * 0.5D;
            d5 = (random.nextFloat() - 0.5D) * 0.5D;
            if(world.getBlockId(i - 1, j, k) == this.blockID || world.getBlockId(i + 1, j, k) == this.blockID)
            {
                d2 = k + 0.5D + 0.25D * i1;
                d5 = random.nextFloat() * 2.0F * i1;
            } else
            {
                d = i + 0.5D + 0.25D * i1;
                d3 = random.nextFloat() * 2.0F * i1;
            }
            TFParticles.spawn(world, "portal", d, d1, d2, d3, d4, d5);
        }

    }
}
