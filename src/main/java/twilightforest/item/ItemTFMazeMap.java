package twilightforest.item;

import net.minecraft.Block;
import net.minecraft.Chunk;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumRarity;
import net.minecraft.ItemMap;
import net.minecraft.ItemStack;
import net.minecraft.MapColor;
import net.minecraft.MapCoord;
import net.minecraft.MapInfo;
import net.minecraft.MathHelper;
import net.minecraft.Packet;
import net.minecraft.Packet131MapData;
import net.minecraft.StatCollector;
import net.minecraft.World;
import twilightforest.TFMazeMapData;

public class ItemTFMazeMap extends ItemMap {
    public static final String STR_ID = "mazemap";
    private static final int YSEARCH = 3;
    protected boolean mapOres;

    protected ItemTFMazeMap(int id, boolean par2MapOres) {
        super(id, par2MapOres ? "twilightforest:oreMap" : "twilightforest:mazeMap");
        this.mapOres = par2MapOres;
    }

    /** Client side: fetch (or create) the map data for an incoming map packet. Hides {@link ItemMap#getMPMapData(short, World)}. */
    public static TFMazeMapData getMPMapData(short par0, World par1World) {
        return getMPMapData((int) par0, par1World);
    }

    public static TFMazeMapData getMPMapData(int par0, World par1World) {
        String mapName = STR_ID + "_" + par0;

        TFMazeMapData mapData = (TFMazeMapData) par1World.loadItemData(TFMazeMapData.class, mapName);

        if (mapData == null) {
            mapData = new TFMazeMapData(mapName);
            par1World.setItemData(mapName, mapData);
        }

        return mapData;
    }

    @Override
    public TFMazeMapData getMapData(ItemStack par1ItemStack, World par2World) {
        TFMazeMapData mapData = (TFMazeMapData) par2World.loadItemData(TFMazeMapData.class, STR_ID + "_" + par1ItemStack.getItemSubtype());

        if (mapData == null && !par2World.isRemote) {
            par1ItemStack.setItemSubtype(par2World.getUniqueDataId(STR_ID));
            String mapName = STR_ID + "_" + par1ItemStack.getItemSubtype();
            mapData = new TFMazeMapData(mapName);
            mapData.xCenter = par2World.getWorldInfo().getSpawnX();
            mapData.zCenter = par2World.getWorldInfo().getSpawnZ();
            mapData.scale = 0;
            mapData.dimension = (byte) par2World.provider.dimensionId;
            mapData.markDirty();
            par2World.setItemData(mapName, mapData);
        }

        return mapData;
    }

    private static Block blockAt(Chunk chunk, int x15, int y, int z15) {
        if (y < 0 || y > 255) {
            return null;
        }
        return Block.getBlock(chunk.getBlockID(x15, y, z15));
    }

    /** Balance 7.7: the ore map only shows the magnet whitelist ores (config magnet.whitelist); other ores look like stone. */
    private static boolean isHiddenOre(Block block) {
        return (block instanceof net.minecraft.BlockOre || block instanceof net.minecraft.BlockRedstoneOre) && !ItemTFOreMagnet.isOre(block, 0);
    }

    private static boolean isOreBlock(Block block) {
        if (block == null) {
            return false;
        }
        String name = block.getUnlocalizedName();
        return name != null && name.toLowerCase().contains("ore");
    }

    /** Maze map update data.  Look at scale 0 for walls, and maybe ores. */
    public void updateMapData(World par1World, Entity par2Entity, TFMazeMapData par3MapData) {
        int yDraw = MathHelper.floor_double(par2Entity.posY - (double) par3MapData.yCenter);

        if (par1World.provider.dimensionId == par3MapData.dimension && yDraw > -YSEARCH && yDraw < YSEARCH) {
            short xSize = 128;
            short zSize = 128;
            int xCenter = par3MapData.xCenter;
            int zCenter = par3MapData.zCenter;
            int xDraw = MathHelper.floor_double(par2Entity.posX - (double) xCenter) + xSize / 2;
            int zDraw = MathHelper.floor_double(par2Entity.posZ - (double) zCenter) + zSize / 2;
            int drawSize = 16;

            MapInfo mapInfo = par3MapData.func_82568_a((EntityPlayer) par2Entity);
            ++mapInfo.field_82569_d;

            for (int xStep = xDraw - drawSize + 1; xStep < xDraw + drawSize; ++xStep) {
                // MITE port: every column in range is refreshed each tick (TF: one column in 16 per tick)
                int highNumber = 255;
                int lowNumber = 0;

                for (int zStep = zDraw - drawSize - 1; zStep < zDraw + drawSize; ++zStep) {
                    if (xStep >= 0 && zStep >= -1 && xStep < xSize && zStep < zSize) {
                        int xOffset = xStep - xDraw;
                        int zOffset = zStep - zDraw;
                        boolean var20 = xOffset * xOffset + zOffset * zOffset > (drawSize - 2) * (drawSize - 2);
                        int xDraw2 = (xCenter + xStep - xSize / 2);
                        int zDraw2 = (zCenter + zStep - zSize / 2);
                        Chunk chunk = par1World.getChunkFromBlockCoords(xDraw2, zDraw2);
                        int x15 = xDraw2 & 15;
                        int z15 = zDraw2 & 15;
                        int heightValue = par3MapData.yCenter;
                        Block block = blockAt(chunk, x15, heightValue, z15);

                        byte tint = 1;
                        int colorIndex = 0;

                        // for stone, search up and down for ores
                        if (block == Block.stone && mapOres) {
                            for (int i = -YSEARCH; i <= YSEARCH; i++) {
                                Block searchBlock = blockAt(chunk, x15, heightValue + i, z15);
                                if (searchBlock != Block.stone && !isHiddenOre(searchBlock)) {
                                    block = searchBlock;
                                    if (i > 0) {
                                        tint = 2;
                                    }
                                    if (i < 0) {
                                        tint = 0;
                                    }
                                    // stop searching
                                    break;
                                }
                            }
                        }

                        if (mapOres && isHiddenOre(block)) {
                            block = Block.stone;
                        }

                        if (block != null) {
                            MapColor mapColor = block.blockMaterial.map_color;
                            colorIndex = mapColor.colorIndex;
                        }

                        if (mapOres && block != null) {
                            // recolor ores (MITE has no lapis/red/pink MapColor, closest ones are used)
                            if (block == Block.oreCoal) {
                                colorIndex = MapColor.obsidianColor.colorIndex;
                            } else if (block == Block.oreGold) {
                                colorIndex = MapColor.goldColor.colorIndex;
                            } else if (block == Block.oreIron) {
                                colorIndex = MapColor.ironColor.colorIndex;
                            } else if (block == Block.oreCopper) {
                                colorIndex = MapColor.copperColor.colorIndex;
                            } else if (block == Block.oreSilver) {
                                colorIndex = MapColor.silverColor.colorIndex;
                            } else if (block == Block.oreMithril) {
                                colorIndex = MapColor.mithrilColor.colorIndex;
                            } else if (block == Block.oreAdamantium) {
                                colorIndex = MapColor.adamantiumColor.colorIndex;
                            } else if (block == Block.oreLapis) {
                                colorIndex = MapColor.waterColor.colorIndex;
                            } else if (block == Block.oreRedstone || block == Block.oreRedstoneGlowing) {
                                colorIndex = MapColor.redstoneColor.colorIndex;
                            } else if (block == Block.oreDiamond) {
                                colorIndex = MapColor.diamondColor.colorIndex;
                            } else if (block == Block.oreEmerald) {
                                colorIndex = MapColor.emeraldColor.colorIndex;
                            } else if (isOreBlock(block)) {
                                // any other ore, catchall
                                colorIndex = MapColor.tntColor.colorIndex;
                            }
                        }

                        if (zStep >= 0 && xOffset * xOffset + zOffset * zOffset < drawSize * drawSize && (!var20 || (xStep + zStep & 1) != 0)) {
                            byte existingColor = par3MapData.colors[xStep + zStep * xSize];
                            byte tintedColor = (byte) (colorIndex * 4 + tint);

                            if (existingColor != tintedColor) {
                                if (highNumber > zStep) {
                                    highNumber = zStep;
                                }

                                if (lowNumber < zStep) {
                                    lowNumber = zStep;
                                }

                                par3MapData.colors[xStep + zStep * xSize] = tintedColor;
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

    /** Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and update it's contents. */
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean isActiveItem) {
        if (!par2World.isRemote) {
            TFMazeMapData mapData = this.getMapData(par1ItemStack, par2World);

            if (par3Entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) par3Entity;
                mapData.updateVisiblePlayers(player, par1ItemStack);

                int yProximity = MathHelper.floor_double(player.posY - mapData.yCenter);
                if (yProximity < -YSEARCH || yProximity > YSEARCH) {
                    // fix player icon so that it's a dot
                    MapCoord mapCoord = (MapCoord) mapData.playersVisibleOnMap.get(player.getCommandSenderName());
                    if (mapCoord != null) {
                        mapCoord.iconSize = 6;
                    }
                }
            }

            if (isActiveItem) {
                this.updateMapData(par2World, par3Entity, mapData);
            }
        }
    }

    /** Called when item is crafted/smelted. Used only by maps so far. */
    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.setItemSubtype(par2World.getUniqueDataId(STR_ID));
        String mapName = STR_ID + "_" + par1ItemStack.getItemSubtype();
        TFMazeMapData mapData = new TFMazeMapData(mapName);
        par2World.setItemData(mapName, mapData);
        mapData.xCenter = MathHelper.floor_double(par3EntityPlayer.posX);
        mapData.yCenter = MathHelper.floor_double(par3EntityPlayer.posY);
        mapData.zCenter = MathHelper.floor_double(par3EntityPlayer.posZ);
        mapData.scale = 0;
        mapData.dimension = (byte) par2World.provider.dimensionId;
        mapData.markDirty();
    }

    /** Return an item rarity from EnumRarity */
    @Override
    public EnumRarity getRarity(ItemStack par1ItemStack) {
        return mapOres ? EnumRarity.epic : EnumRarity.uncommon;
    }

    /** Do the enchanted shimmer thing */
    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
        return false;
    }

    /**
     * returns null if no update is to be sent. Replaces 1.7.10 func_150911_c; the packet is a vanilla Packet131MapData
     * carrying this item's id, handled client side by TFPacketHandler.handleMapData.
     */
    @Override
    public Packet createMapDataPacket(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        TFMazeMapData mapData = this.getMapData(par1ItemStack, par2World);
        byte[] mapBytes = mapData.getUpdatePacketData(par1ItemStack, par2World, par3EntityPlayer);

        if (mapBytes == null) {
            mapBytes = nextDirtyColumn(mapData, mapData.func_82568_a(par3EntityPlayer));
        }
        if (mapBytes == null) {
            return null;
        }
        return new Packet131MapData((short) this.itemID, (short) par1ItemStack.getItemSubtype(), mapBytes);
    }

    /**
     * Vanilla MapInfo only looks at one pseudo-random column of 128 per tick, so the few dirty columns around the
     * player took seconds to reach the client. Sends the first dirty column instead, in the same format.
     */
    private static byte[] nextDirtyColumn(TFMazeMapData mapData, MapInfo mapInfo) {
        for (int x = 0; x < 128; ++x) {
            int zLow = mapInfo.field_76209_b[x];
            if (zLow >= 0) {
                int zHigh = mapInfo.field_76210_c[x];
                byte[] bytes = new byte[zHigh - zLow + 1 + 3];
                bytes[0] = 0;
                bytes[1] = (byte) x;
                bytes[2] = (byte) zLow;
                for (int i = 0; i < bytes.length - 3; ++i) {
                    bytes[i + 3] = mapData.colors[(i + zLow) * 128 + x];
                }
                mapInfo.field_76210_c[x] = -1;
                mapInfo.field_76209_b[x] = -1;
                return bytes;
            }
        }
        return null;
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
}
