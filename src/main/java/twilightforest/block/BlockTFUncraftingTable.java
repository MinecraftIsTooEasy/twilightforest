package twilightforest.block;

import net.minecraft.Block;
import net.minecraft.BlockConstants;
import net.minecraft.CreativeTabs;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.Material;
import net.minecraft.PlayerStatsHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TwilightForestMod;
import twilightforest.compat.TFChat;
import twilightforest.inventory.ContainerTFUncrafting;
import twilightforest.network.TFNetwork;

public class BlockTFUncraftingTable extends Block {

	public static Icon tinkerTop;
	public static Icon tinkerSide;

	public BlockTFUncraftingTable(int id) {
		super(id, Material.wood, new BlockConstants());
		// MITE_BALANCE B8: wood, hardness 2.5, resistance 5, no tool requirement, not pocketable
		this.setHardness(2.5F);
		this.setResistance(5.0F);
		this.setStepSound(soundWoodFootstep);
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return side == 1 ? tinkerTop : tinkerSide;
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		tinkerTop = par1IconRegister.registerIcon(TwilightForestMod.ID + ":uncrafting_top");
		tinkerSide = par1IconRegister.registerIcon(TwilightForestMod.ID + ":uncrafting_side");
	}

	/** Like the workbench, the table opens a GUI, so it must not be picked up into the pocket. */
	@Override
	public boolean isPortable(World world, EntityLivingBase entity, int x, int y, int z) {
		return false;
	}

	/**
	 * Called when the block is clicked by a player.
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float offsetX, float offsetY, float offsetZ) {
		if (world.isRemote) {
			return true;
		}

		if (ContainerTFUncrafting.isDisabled()) {
			TFChat.sendTranslated(player, "tf.uncrafting.disabled");
			return true;
		}

		// MITE_BALANCE X10: the table only opens for players who have beaten the Minoshroom (labyrinth progress)
		if (!canPlayerUse(world, player)) {
			TFChat.sendTranslated(player, "tf.uncrafting.locked");
			return true;
		}

		TFNetwork.openGui(player, TwilightForestMod.GUI_ID_UNCRAFTING, x, y, z);
		return true;
	}

	private static boolean canPlayerUse(World world, EntityPlayer player) {
		if (player.inCreativeMode() || ContainerTFUncrafting.isVanillaMode() || !TwilightForestMod.isProgressionEnforced(world) || twilightforest.TFTestMode.isOn(world)) {
			return true;
		}
		if (player instanceof ServerPlayer && player.stats != null && TFAchievementPage.twilightProgressLabyrinth != null) {
			return PlayerStatsHelper.getValue(TFAchievementPage.twilightProgressLabyrinth.statId, player.stats) == 1L;
		}
		return false;
	}

}
