package twilightforest.block;

import java.util.Random;

import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockConstants;
import net.minecraft.BlockContainer;
import net.minecraft.EntityItem;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumFace;
import net.minecraft.EnumParticle;
import net.minecraft.Icon;
import net.minecraft.IconRegister;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.NBTTagCompound;
import net.minecraft.TileEntity;
import net.minecraft.World;
import twilightforest.TwilightForestMod;
import twilightforest.network.TFNetwork;
import twilightforest.tileentity.TileEntityTFCinderFurnace;

public class BlockTFCinderFurnace extends BlockContainer {

	private static boolean isUpdating;
	private final boolean isLit;
	private Icon topIcon;
	private final Random furnaceRandom = new Random();

	public BlockTFCinderFurnace(int id, boolean isLit) {
		super(id, Material.stone, new BlockConstants());

		this.isLit = isLit;

		// MITE_BALANCE 8.2: stone, hardness 7.0, resistance 20, copper pickaxe (level 2), creative only
		this.setHardness(7.0F);
		this.setResistance(20.0F);
		this.setMinHarvestLevel(2);

		this.setLightValue(isLit ? 1F : 0);

		if (!isLit) {
			this.setCreativeTab(net.minecraft.CreativeTabs.tabDecorations);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityTFCinderFurnace();
	}

	/** Tile entity blocks are portable by default in MITE; the balance spec gives this block a real tool requirement. */
	@Override
	public boolean isPortable(World world, EntityLivingBase entity, int x, int y, int z) {
		return false;
	}

	@Override
	public boolean canBeCarried() {
		// like MITE's BlockFurnace: only the unlit block has an item form; if the lit one reported false for the dropped
		// unlit block too, Block.dropBlockAsEntityItem would abort and drop nothing
		return !this.isLit;
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	public Icon getIcon(int side, int meta) {
		return side == 1 ? this.topIcon : (side == 0 ? this.topIcon : this.blockIcon);
	}

	@Override
	public void registerIcons(IconRegister register) {
		this.blockIcon = register.registerIcon(this.isLit ? "furnace_front_on" : "furnace_front_off");
		this.topIcon = register.registerIcon("furnace_top");
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float offsetX, float offsetY, float offsetZ) {
		if (world.isRemote) {
			return true;
		}

		TileEntityTFCinderFurnace furnace = (TileEntityTFCinderFurnace) world.getBlockTileEntity(x, y, z);

		if (furnace != null) {
			TFNetwork.openGui(player, TwilightForestMod.GUI_ID_FURNACE, x, y, z);
		}

		return true;
	}

	/**
	 * Update which block the furnace is using depending on whether or not it is burning
	 */
	public static void updateFurnaceBlockState(boolean isBurning, World world, int x, int y, int z) {
		TileEntity tileentity = world.getBlockTileEntity(x, y, z);
		isUpdating = true;

		if (isBurning) {
			world.setBlock(x, y, z, TFBlocks.cinderFurnaceLit.blockID);
		} else {
			world.setBlock(x, y, z, TFBlocks.cinderFurnace.blockID);
		}

		isUpdating = false;

		if (tileentity != null) {
			tileentity.validate();
			world.setBlockTileEntity(x, y, z, tileentity);
		}
	}

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		if (this.isLit) {
			float f = x + 0.5F;
			float f1 = y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float f2 = z + 0.5F;
			float f3 = 0.52F;
			float f4 = random.nextFloat() * 0.6F - 0.3F;

			int l = random.nextInt(4) + 2;

			if (l == 4) {
				world.spawnParticle(EnumParticle.smoke, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			} else if (l == 5) {
				world.spawnParticle(EnumParticle.smoke, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
			} else if (l == 2) {
				world.spawnParticle(EnumParticle.smoke, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
			} else if (l == 3) {
				world.spawnParticle(EnumParticle.smoke, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticle.flame, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	/**
	 * Dump things all over if broken
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		if (!isUpdating) {
			TileEntityTFCinderFurnace tileEntity = (TileEntityTFCinderFurnace) world.getBlockTileEntity(x, y, z);

			if (tileEntity != null) {
				for (int i = 0; i < tileEntity.getSizeInventory(); ++i) {
					ItemStack itemstack = tileEntity.getStackInSlot(i);

					if (itemstack != null) {
						float dx = this.furnaceRandom.nextFloat() * 0.8F + 0.1F;
						float dy = this.furnaceRandom.nextFloat() * 0.8F + 0.1F;
						float dz = this.furnaceRandom.nextFloat() * 0.8F + 0.1F;

						while (itemstack.stackSize > 0) {
							int j1 = this.furnaceRandom.nextInt(21) + 10;

							if (j1 > itemstack.stackSize) {
								j1 = itemstack.stackSize;
							}

							itemstack.stackSize -= j1;
							EntityItem entityitem = new EntityItem(world, x + dx, y + dy, z + dz, new ItemStack(itemstack.itemID, j1, itemstack.getItemSubtype()));

							if (itemstack.isItemDamaged()) {
								entityitem.getEntityItem().setItemDamage(itemstack.getItemDamage());
							}

							if (itemstack.getItem().hasQuality()) {
								entityitem.getEntityItem().setQuality(itemstack.getQuality());
							}

							if (itemstack.hasTagCompound()) {
								entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
							}

							float pointFive = 0.05F;
							entityitem.motionX = (float) this.furnaceRandom.nextGaussian() * pointFive;
							entityitem.motionY = (float) this.furnaceRandom.nextGaussian() * pointFive + 0.2F;
							entityitem.motionZ = (float) this.furnaceRandom.nextGaussian() * pointFive;
							world.spawnEntityInWorld(entityitem);
						}
					}
				}

				world.func_96440_m(x, y, z, blockId);
			}
		}

		super.breakBlock(world, x, y, z, blockId, meta);
	}

	/** Both the lit and the unlit block drop the unlit one. */
	@Override
	public int dropBlockAsEntityItem(BlockBreakInfo info) {
		return this.dropBlockAsEntityItem(info, TFBlocks.cinderFurnace.blockID, 0, 1, 1.0F);
	}

	@Override
	public ItemStack createStackedBlock(int metadata) {
		return new ItemStack(TFBlocks.cinderFurnace.blockID);
	}

	/**
	 * Gets the block id for pick block. Args: world, x, y, z
	 */
	@Override
	public int idPicked(World world, int x, int y, int z) {
		return TFBlocks.cinderFurnace.blockID;
	}
}
