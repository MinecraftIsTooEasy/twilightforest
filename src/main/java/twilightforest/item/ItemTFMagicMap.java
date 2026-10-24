package twilightforest.item;

import net.minecraft.BiomeGenBase;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.ItemMap;
import net.minecraft.ItemStack;
import net.minecraft.MapData;
import net.minecraft.MapInfo;
import net.minecraft.MathHelper;
import net.minecraft.Packet;
import net.minecraft.Packet131MapData;
import net.minecraft.StatCollector;
import net.minecraft.World;
import twilightforest.TFFeature;
import twilightforest.TFMagicMapData;
import twilightforest.biomes.TFBiomeBase;
import twilightforest.world.TFWorldChunkManager;

public class ItemTFMagicMap extends ItemMap {
    public static final String STR_ID = "magicmap";

    protected ItemTFMagicMap(int id) {
        super(id, "twilightforest:magicMap");
    }

    /** Client side: fetch (or create) the map data for an incoming map packet. Hides {@link ItemMap#getMPMapData(short, World)}. */
    public static TFMagicMapData getMPMapData(short par0, World par1World) {
        return getMPMapData((int) par0, par1World);
    }

    public static TFMagicMapData getMPMapData(int par0, World par1World) {
        String mapName = STR_ID + "_" + par0;
        TFMagicMapData mapData = (TFMagicMapData) par1World.loadItemData(TFMagicMapData.class, mapName);

        if (mapData == null) {
            mapData = new TFMagicMapData(mapName);
            par1World.setItemData(mapName, mapData);
        }

        return mapData;
    }

    @Override
    public TFMagicMapData getMapData(ItemStack par1ItemStack, World par2World) {
        String mapName = STR_ID + "_" + par1ItemStack.getItemSubtype();
        TFMagicMapData mapData = (TFMagicMapData) par2World.loadItemData(TFMagicMapData.class, mapName);

        if (mapData == null && !par2World.isRemote) {
            par1ItemStack.setItemSubtype(par2World.getUniqueDataId(STR_ID));
            mapName = STR_ID + "_" + par1ItemStack.getItemSubtype();
            mapData = new TFMagicMapData(mapName);
            mapData.xCenter = par2World.getWorldInfo().getSpawnX();
            mapData.zCenter = par2World.getWorldInfo().getSpawnZ();
            mapData.scale = 4;
            mapData.dimension = (byte) par2World.provider.dimensionId;
            mapData.markDirty();
            par2World.setItemData(mapName, mapData);
        }

        return mapData;
    }

    public void updateMapData(World par1World, Entity par2Entity, TFMagicMapData par3MapData) {
        if (par1World.provider.dimensionId == par3MapData.dimension && par2Entity instanceof EntityPlayer) {
            short xSize = 128;
            short zSize = 128;
            int scaleFactor = 1 << par3MapData.scale;
            int xCenter = par3MapData.xCenter;
            int zCenter = par3MapData.zCenter;
            int xDraw = MathHelper.floor_double(par2Entity.posX - (double) xCenter) / scaleFactor + xSize / 2;
            int zDraw = MathHelper.floor_double(par2Entity.posZ - (double) zCenter) / scaleFactor + zSize / 2;
            int drawSize = 512 / scaleFactor;

            MapInfo mapInfo = par3MapData.func_82568_a((EntityPlayer) par2Entity);
            ++mapInfo.field_82569_d;

            for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; ++xStep) {
                if ((xStep & 15) == (mapInfo.field_82569_d & 15)) {
                    int highNumber = 255;
                    int lowNumber = 0;

                    for (int zStep = zDraw - drawSize - 1; zStep < zDraw + drawSize; ++zStep) {
                        if (xStep >= 0 && zStep >= -1 && xStep < xSize && zStep < zSize) {
                            int xOffset = xStep - xDraw;
                            int zOffset = zStep - zDraw;
                            boolean var20 = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
                            int xDraw2 = (xCenter / scaleFactor + xStep - xSize / 2) * scaleFactor;
                            int zDraw2 = (zCenter / scaleFactor + zStep - zSize / 2) * scaleFactor;
                            int[] biomeFrequencies = new int[256];

                            for (int xStep2 = 0; xStep2 < scaleFactor; ++xStep2) {
                                for (int zStep2 = 0; zStep2 < scaleFactor; ++zStep2) {
                                    int biomeID = par1World.getBiomeGenForCoords(xDraw2 + xStep2, zDraw2 + zStep2).biomeID;

                                    biomeFrequencies[biomeID]++;

                                    // make rivers and streams 3x more prominent
                                    if (biomeID == BiomeGenBase.river.biomeID || biomeID == TFBiomeBase.stream.biomeID) {
                                        biomeFrequencies[biomeID] += 2;
                                    }
                                    // add in TF features
                                    if (par1World.getWorldChunkManager() instanceof TFWorldChunkManager) {
                                        TFWorldChunkManager tfManager = (TFWorldChunkManager) par1World.getWorldChunkManager();

                                        if (tfManager.isInFeatureChunk(par1World, xDraw2 + xStep2, zDraw2 + zStep2) && zStep >= 0 && xOffset * xOffset + zOffset * zOffset < drawSize * drawSize) {
                                            par3MapData.addFeatureToMap(TFFeature.getNearestFeature((xDraw2 + xStep2) >> 4, (zDraw2 + zStep2) >> 4, par1World), xDraw2, zDraw2);
                                        }
                                    }
                                }
                            }

                            // figure out which color is the most prominent and make that one appear on the map
                            byte biomeIDToShow = 0;
                            int highestFrequency = 0;

                            for (int i = 0; i < 256; i++) {
                                if (biomeFrequencies[i] > highestFrequency) {
                                    biomeIDToShow = (byte) i;
                                    highestFrequency = biomeFrequencies[i];
                                }
                            }

                            // increase biomeID by one so we can properly show oceans
                            biomeIDToShow++;

                            if (zStep >= 0 && xOffset * xOffset + zOffset * zOffset < drawSize * drawSize && (!var20 || (xStep + zStep & 1) != 0)) {
                                byte existingColor = par3MapData.colors[xStep + zStep * xSize];

                                if (existingColor != biomeIDToShow) {
                                    if (highNumber > zStep) {
                                        highNumber = zStep;
                                    }

                                    if (lowNumber < zStep) {
                                        lowNumber = zStep;
                                    }

                                    par3MapData.colors[xStep + zStep * xSize] = biomeIDToShow;
                                }
                            }
                        }
                    }

                    if (highNumber <= lowNumber) {
                        par3MapData.setColumnDirty(xStep, highNumber, lowNumber);
                    }
                }
            }
        }
    }

    /** Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and update it's contents. */
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
        if (!par2World.isRemote) {
            TFMagicMapData mapData = this.getMapData(par1ItemStack, par2World);

            if (par3Entity instanceof EntityPlayer) {
                EntityPlayer var7 = (EntityPlayer) par3Entity;
                mapData.updateVisiblePlayers(var7, par1ItemStack);
            }

            if (par5) {
                this.updateMapData(par2World, par3Entity, mapData);
            }
        }
    }

    /** Called when item is crafted/smelted. Used only by maps so far. */
    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        // we don't need to do anything here, I think
    }

    /** Return an item rarity from EnumRarity */
    @Override
    public EnumRarity getRarity(ItemStack par1ItemStack) {
        return EnumRarity.uncommon;
    }

    /** Do the enchanted shimmer thing */
    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
        return false;
    }

    /**
     * returns null if no update is to be sent. Replaces 1.7.10 func_150911_c.
     * The packet is a vanilla Packet131MapData carrying this item's id; the client side is handled by
     * TFPacketHandler.handleMapData. Every 4th player update packet is replaced by a feature icon update.
     */
    @Override
    public Packet createMapDataPacket(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        TFMagicMapData mapData = this.getMapData(par1ItemStack, par2World);
        byte[] mapBytes = mapData.getUpdatePacketData(par1ItemStack, par2World, par3EntityPlayer);

        if (mapBytes == null) {
            return null;
        }
        // hijack random player data packets for our feature data packets?
        if (mapBytes[0] == 1 && par2World.rand.nextInt(4) == 0) {
            mapData.checkExistingFeatures(par2World);
            mapBytes = mapData.makeFeatureStorageArray();
        }

        return new Packet131MapData((short) this.itemID, (short) par1ItemStack.getItemSubtype(), mapBytes);
    }

    /** Add the map number to the tooltip */
    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack) {
        return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(par1ItemStack) + ".name") + " #" + par1ItemStack.getItemSubtype()).trim();
    }

    /** Add the map number to the tooltip (MITE's name path goes through getItemDisplayName). */
    @Override
    public String getItemDisplayName(ItemStack par1ItemStack) {
        if (par1ItemStack == null) {
            return super.getItemDisplayName(par1ItemStack);
        }
        return this.getItemStackDisplayName(par1ItemStack);
    }

    // registerIcons: default Item.registerIcons uses the "twilightforest:magicMap" texture string given to the constructor.
}
