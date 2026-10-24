package twilightforest.block;

import java.util.List;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.MathHelper;
import net.minecraft.World;
import twilightforest.TwilightForestMod;

public class BlockTFNagastone extends Block {

	private static Icon FACE_LEFT;
	private static Icon FACE_RIGHT;
	private static Icon CROSS_SECTION;
	private static Icon FACE_FRONT;
	private static Icon TOP_TIP;
	private static Icon TOP_LONG;
	private static Icon BOTTOM_TIP;
	private static Icon BOTTOM_LONG;
	private static Icon LEFT_DOWN;
	private static Icon RIGHT_DOWN;
	private static Icon LEFT_UP;
	private static Icon RIGHT_UP;
	private static Icon TIP_LEFT;
	private static Icon LONG_SIDE;
	private static Icon TIP_RIGHT;
	private static Icon TURN_TOP;

	public BlockTFNagastone(int id) {
		super(id, Material.stone, new BlockConstants());
		// balance B3: soft structure stone, hardness = MITE stone
		this.setHardness(2.4F);
		this.setResistance(10.0F);
		this.setStepSound(soundStoneFootstep);
		this.setCreativeTab(CreativeTabs.tabBlock);
	}

	@Override
	public int getRenderType() {
		return TwilightForestMod.proxy.getNagastoneBlockRenderID();
	}

	// every metadata 0..15 is a valid orientation/shape; the item subtype equals the metadata (1.7.10 damageDropped)
	@Override
	public boolean isValidMetadata(int metadata) {
		return metadata >= 0 && metadata < 16;
	}

	@Override
	public int getBlockSubtypeUnchecked(int metadata) {
		return metadata;
	}

	/** Only head (1) and body (13) exist as items; the other orientations drop as one of them. */
	@Override
	public int getItemSubtype(int metadata) {
		return (metadata & 12) == 12 ? 13 : 1;
	}

	/**
	 * Replaces 1.7.10 onBlockPlaced. The item subtype (1 = head, 13 = body) gives the type, the clicked face the orientation.
	 *
	 * 0 - 3  head directions
	 * 4 - 7  bend down
	 * 8 - 11 bend up
	 * 12     straight n/s
	 * 13     straight e/w
	 * 14     straight u/d
	 * 15     weird middle piece
	 */
	@Override
	public int getMetadataForPlacement(World world, int x, int y, int z, ItemStack item_stack, Entity entity, EnumFace face,
			float offset_x, float offset_y, float offset_z) {
		int metadata = item_stack.getHasSubtypes() ? item_stack.getItemSubtype() : 0;
		int side = face.ordinal();
		int type = metadata & 12;
		int orient = 0;
		if (type == 0) {
			// head!
			switch (side) {
			case 0:
			case 1:
			default:
				// don't know
				orient = 1;
				break;
			case 2:
				orient = 0;
				break;
			case 3:
				orient = 1;
				break;
			case 4:
				orient = 2;
				break;
			case 5:
				orient = 3;
				break;
			}
		}
		if (type == 12) {
			// straight
			switch (side) {
			case 0:
			case 1:
				orient = 2; // u/d
				break;
			case 2:
			case 3:
				orient = 1; // e/w
				break;
			case 4:
			case 5:
				orient = 0; // n/s
				break;
			}
		}

		return type | orient;
	}

	/**
	 * Lets the block know when one of its neighbor changes (server only in MITE).
	 */
	@Override
	public boolean onNeighborBlockChange(World world, int x, int y, int z, int neighbor_block_id) {
		int type = world.getBlockMetadata(x, y, z) & 12;

		if (type == 0) {
			placePerfectFitHead(world, x, y, z);
		} else {
			placePerfectFitBody(world, x, y, z);
		}
		// nagastone has no support requirement, it is never removed here
		return false;
	}

	/**
	 * Check if there is a perfect fit naga body block at the specified position, and if so, place it.
	 *
	 * A perfect fit is when there are two nagastone blocks in exactly two directions.
	 */
	public boolean placePerfectFitBody(World world, int x, int y, int z) {
		int firstConnection = getPosibleConnection(world, x, y, z, -1, -1);
		int secondConnection = getPosibleConnection(world, x, y, z, firstConnection, -1);

		// if we aren't connecting twice, fail
		if (firstConnection == -1 || secondConnection == -1) {
			return false;
		}

		// third connection == fail
		if (getPosibleConnection(world, x, y, z, firstConnection, secondConnection) != -1) {
			return false;
		}

		// place the perfect block
		if ((firstConnection == 2 && secondConnection == 3) || (firstConnection == 3 && secondConnection == 2)) {
			// e/w straight piece
			world.setBlockMetadataWithNotify(x, y, z, 12, 3);
			return true;
		} else if (firstConnection + secondConnection == 1) {
			// n/s straight piece
			world.setBlockMetadataWithNotify(x, y, z, 13, 3);
			return true;
		} else if (firstConnection + secondConnection == 9) {
			// up/down straight piece
			world.setBlockMetadataWithNotify(x, y, z, 14, 3);
			return true;
		} else if (firstConnection == 4) {
			// curve up towards second connection
			world.setBlockMetadataWithNotify(x, y, z, 4 | secondConnection, 3);
			return true;
		} else if (firstConnection == 5) {
			// curve down towards second connection
			world.setBlockMetadataWithNotify(x, y, z, 8 | secondConnection, 3);
			return true;
		} else if (secondConnection == 4) {
			// curve up towards first connection
			world.setBlockMetadataWithNotify(x, y, z, 4 | firstConnection, 3);
			return true;
		} else if (secondConnection == 5) {
			// curve down towards first connection
			world.setBlockMetadataWithNotify(x, y, z, 8 | firstConnection, 3);
			return true;
		} else {
			// block 15!
			world.setBlockMetadataWithNotify(x, y, z, 15, 3);
			return true;
		}
	}

	/**
	 * If we can place the perfect head bit, do it, otherwise do nothing.
	 */
	public boolean placePerfectFitHead(World world, int x, int y, int z) {
		int connect = getOnlyNSEWConnection(world, x, y, z);

		if (connect != -1) {
			// if the first connection is n/s/e/w, and the second connection is not, we win!
			switch (connect) {
			case 0:
				world.setBlock(x, y, z, this.blockID, 1, 3);
				return true;
			case 1:
				world.setBlock(x, y, z, this.blockID, 0, 3);
				return true;
			case 2:
				world.setBlock(x, y, z, this.blockID, 3, 3);
				return true;
			case 3:
				world.setBlock(x, y, z, this.blockID, 2, 3);
				return true;
			}
			return false; // this is impossible
		} else {
			return false;
		}
	}

	/**
	 * Return a direction only if it is the only possible connection, otherwise return -1.  This only returns n/s/e/w
	 */
	public int getOnlyNSEWConnection(World world, int x, int y, int z) {
		int firstConnection = getPosibleConnection(world, x, y, z, -1, -1);
		int secondConnection = getPosibleConnection(world, x, y, z, firstConnection, -1);

		if (firstConnection > 0 && firstConnection < 4 && (secondConnection < 0 || secondConnection > 3)) {
			return firstConnection;
		} else {
			return -1;
		}
	}

	/**
	 * Return a direction only if it is the only possible connection, otherwise return -1.
	 */
	public int getOnlyConnection(World world, int x, int y, int z) {
		int firstConnection = getPosibleConnection(world, x, y, z, -1, -1);
		int secondConnection = getPosibleConnection(world, x, y, z, firstConnection, -1);

		if (firstConnection > 0 && firstConnection < 6 && secondConnection == -1) {
			return firstConnection;
		} else {
			return -1;
		}
	}

	/**
	 * Return a direction we have found nagastone in, excluding the two exclude directions.
	 */
	public int getPosibleConnection(World world, int x, int y, int z, int exclude1, int exclude2) {
		for (int i = 0; i < 6; i++) {
			if (i != exclude1 && i != exclude2 && isNagaStoneInDirection(world, x, y, z, i)) {
				return i;
			}
		}
		// none found
		return -1;
	}

	/**
	 * Is there nagastone in the following direction?   0 = n, 1 = s, 2 = w, 3 = e, 4 = u, 5 = d;
	 */
	public boolean isNagaStoneInDirection(World par1World, int x, int y, int z, int direction) {
		switch (direction) {
		case 0:
			return par1World.getBlockId(x, y, z - 1) == this.blockID;
		case 1:
			return par1World.getBlockId(x, y, z + 1) == this.blockID;
		case 2:
			return par1World.getBlockId(x - 1, y, z) == this.blockID;
		case 3:
			return par1World.getBlockId(x + 1, y, z) == this.blockID;
		case 4:
			return par1World.getBlockId(x, y - 1, z) == this.blockID;
		case 5:
			return par1World.getBlockId(x, y + 1, z) == this.blockID;
		}
		// welp,
		return false;
	}

	/**
	 * Returns only 0-4.  0 = n, 1 = s, 2 = w, 3 = e.
	 */
	public static int determineOrientation(World par0World, int par1, int par2, int par3, EntityPlayer par4EntityPlayer) {
		int rot = MathHelper.floor_double(par4EntityPlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		return rot == 0 ? 0 : (rot == 1 ? 3 : (rot == 2 ? 1 : (rot == 3 ? 2 : 0)));
	}

    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     * 
     * 0 - 3  head directions
     * 4 - 7  bend down
     * 8 - 11 bend up
     * 12     straight n/s
     * 13     straight e/w
     * 14     straight u/d
     * 15     weird middle piece
     * 
     * TODO: Surely there is a way to do this that's not just specifying all 6 * 16 values.
     */
    @Override
	public Icon getIcon(int side, int meta)
    {
        int type = meta & 12;
        int orient = meta & 3;
        
        if (type == 0)
        {
        	if (orient == 0)
        	{
        		// north
        		switch (side)
        		{
        		case 0:
        			return BOTTOM_TIP;
        		case 1:
        			return TOP_TIP;
        		case 2:
        			return FACE_FRONT;
        		case 3:
        			return CROSS_SECTION;
        		case 4:
        			return FACE_LEFT;
        		case 5:
        			return FACE_RIGHT;
        		}
        	}        
        	else if (orient == 1)
        	{
        		switch (side)
        		{
        		case 0:
        			return BOTTOM_TIP;
        		case 1:
        			return TOP_TIP;
        		case 2:
        			return CROSS_SECTION;
        		case 3:
        			return FACE_FRONT;
        		case 4:
        			return FACE_RIGHT;
        		case 5:
        			return FACE_LEFT;
        		}
        	}
        	else if (orient == 2)
        	{
        		switch (side)
        		{
        		case 0:
        			return BOTTOM_TIP;
        		case 1:
        			return TOP_TIP;
        		case 2:
        			return FACE_RIGHT;
        		case 3:
        			return FACE_LEFT;
        		case 4:
        			return FACE_FRONT;
        		case 5:
        			return CROSS_SECTION;
        		}
        	}
        	else if (orient == 3)
        	{
        		switch (side)
        		{
        		case 0:
        			return BOTTOM_TIP;
        		case 1:
        			return TOP_TIP;
        		case 2:
        			return FACE_LEFT;
        		case 3:
        			return FACE_RIGHT;
        		case 4:
        			return CROSS_SECTION;
        		case 5:
        			return FACE_FRONT;
        		}
        	}
        }
        
        // TURN DOWN
        if (type == 4)
        {
          	if (orient == 0)
        	{
        		// north
            	switch (side)
            	{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return TOP_TIP;
            	case 2:
            		return CROSS_SECTION;
            	case 3:
            		return TIP_RIGHT;
            	case 4:
            		return LEFT_DOWN;
            	case 5:
            		return RIGHT_DOWN;
            	}
        	}        
        	else if (orient == 1)
        	{
        		switch (side)
        		{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return TOP_TIP;
            	case 2:
            		return TIP_LEFT;
            	case 3:
            		return CROSS_SECTION;
            	case 4:
            		return RIGHT_DOWN;
            	case 5:
            		return LEFT_DOWN;
        		}
        	}
        	else if (orient == 2)
        	{
        		switch (side)
        		{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return TOP_TIP;
            	case 2:
            		return RIGHT_DOWN;
            	case 3:
            		return LEFT_DOWN;
            	case 4:
            		return CROSS_SECTION;
            	case 5:
            		return TIP_LEFT;
        		}
        	}
        	else if (orient == 3)
        	{
        		switch (side)
        		{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return TOP_TIP;
            	case 2:
            		return LEFT_DOWN;
            	case 3:
            		return RIGHT_DOWN;
            	case 4:
            		return TIP_RIGHT;
            	case 5:
            		return CROSS_SECTION;
        		}
        	}

        }
        
        if (type == 8)
        {
        	// TURN UP
        	if (orient == 0)
        	{
        		// north
            	switch (side)
            	{
            	case 0:
            		return BOTTOM_TIP;
            	case 1:
            		return CROSS_SECTION;
            	case 2:
            		return CROSS_SECTION;
            	case 3:
            		return TIP_LEFT;
            	case 4:
            		return LEFT_UP;
            	case 5:
            		return RIGHT_UP;
            	}
        	}        
        	else if (orient == 1)
        	{
        		switch (side)
        		{
            	case 0:
            		return BOTTOM_TIP;
            	case 1:
            		return CROSS_SECTION;
            	case 2:
            		return TIP_RIGHT;
            	case 3:
            		return CROSS_SECTION;
            	case 4:
            		return RIGHT_UP;
            	case 5:
            		return LEFT_UP;
        		}
        	}
        	else if (orient == 2)
        	{
        		switch (side)
        		{
            	case 0:
            		return BOTTOM_TIP;
            	case 1:
            		return CROSS_SECTION;
            	case 2:
            		return RIGHT_UP;
            	case 3:
            		return LEFT_UP;
            	case 4:
            		return CROSS_SECTION;
            	case 5:
            		return TIP_RIGHT;
        		}
        	}
        	else if (orient == 3)
        	{
        		switch (side)
        		{
            	case 0:
            		return BOTTOM_TIP;
            	case 1:
            		return CROSS_SECTION;
            	case 2:
            		return LEFT_UP;
            	case 3:
            		return RIGHT_UP;
            	case 4:
            		return TIP_LEFT;
            	case 5:
            		return CROSS_SECTION;
        		}
        	}

        }
        
        if (type == 12)
        {
        	if (orient == 0)
        	{
        		// north
            	switch (side)
            	{
            	case 0:
            		return BOTTOM_LONG;
            	case 1:
            		return TOP_LONG;
            	case 2:
            		return LONG_SIDE;
            	case 3:
            		return LONG_SIDE;
            	case 4:
            		return CROSS_SECTION;
            	case 5:
            		return CROSS_SECTION;
            	}
        	}        
        	else if (orient == 1)
        	{
            	switch (side)
            	{
            	case 0:
            		return BOTTOM_LONG;
            	case 1:
            		return TOP_LONG;
            	case 2:
            		return CROSS_SECTION;
            	case 3:
            		return CROSS_SECTION;
            	case 4:
            		return LONG_SIDE;
            	case 5:
            		return LONG_SIDE;
            	}
        	}
           	else if (orient == 2)
        	{
        		switch (side)
        		{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return CROSS_SECTION;
            	case 2:
            		return LONG_SIDE;
            	case 3:
            		return LONG_SIDE;
            	case 4:
            		return LONG_SIDE;
            	case 5:
            		return LONG_SIDE;
        		}
        	}
           	else if (orient == 3)
        	{
        		switch (side)
        		{
            	case 0:
            		return CROSS_SECTION;
            	case 1:
            		return TURN_TOP;
            	case 2:
            		return LONG_SIDE;
            	case 3:
            		return LONG_SIDE;
            	case 4:
            		return LONG_SIDE;
            	case 5:
            		return LONG_SIDE;
        		}
        	}

        }
        
        return null;
    }
    
	/**
	 * Creative list: only the head (1) and the straight e/w body (13), as in 1.7.10 getSubBlocks.
	 */
	@Override
	public void getItemStacks(int id, CreativeTabs creative_tabs, List list) {
		list.add(new ItemStack(id, 1, 1));
		list.add(new ItemStack(id, 1, 13));
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		BlockTFNagastone.FACE_LEFT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_face_left");
		BlockTFNagastone.FACE_RIGHT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_face_right");
		BlockTFNagastone.CROSS_SECTION = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_cross_section");
		BlockTFNagastone.FACE_FRONT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_face_front");
		BlockTFNagastone.TOP_TIP = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_top_tip");
		BlockTFNagastone.TOP_LONG = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_tip_long");
		BlockTFNagastone.BOTTOM_TIP = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_bottom_tip");
		BlockTFNagastone.BOTTOM_LONG = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_bottom_long");
		BlockTFNagastone.LEFT_DOWN = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_left_down");
		BlockTFNagastone.RIGHT_DOWN = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_right_down");
		BlockTFNagastone.LEFT_UP = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_left_up");
		BlockTFNagastone.RIGHT_UP = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_right_up");
		BlockTFNagastone.TIP_LEFT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_tip_left");
		BlockTFNagastone.LONG_SIDE = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_long_side");
		BlockTFNagastone.TIP_RIGHT = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_tip_right");
		BlockTFNagastone.TURN_TOP = par1IconRegister.registerIcon(TwilightForestMod.ID + ":nagastone_turn_top");
	}

}
