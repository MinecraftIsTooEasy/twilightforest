package twilightforest.client;

import net.minecraft.EntityFX;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySmokeFX;
import net.minecraft.GuiScreen;
import net.minecraft.Item;
import net.minecraft.Minecraft;
import net.minecraft.ModelBiped;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import twilightforest.TFCommonProxy;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.ModelTFArcticArmor;
import twilightforest.client.model.ModelTFFieryArmor;
import twilightforest.client.model.ModelTFKnightlyArmor;
import twilightforest.client.model.ModelTFPhantomArmor;
import twilightforest.client.model.ModelTFYetiArmor;
import twilightforest.client.particle.EntityTFAnnihilateFX;
import twilightforest.client.particle.EntityTFBossTearFX;
import twilightforest.client.particle.EntityTFGhastTrapFX;
import twilightforest.client.particle.EntityTFIceBeamFX;
import twilightforest.client.particle.EntityTFLargeFlameFX;
import twilightforest.client.particle.EntityTFLeafRuneFX;
import twilightforest.client.particle.EntityTFProtectionFX;
import twilightforest.client.particle.EntityTFSnowFX;
import twilightforest.client.particle.EntityTFSnowGuardianFX;
import twilightforest.client.particle.EntityTFSnowWarningFX;
import twilightforest.compat.ITFProviderRenderers;

public class TFClientProxy extends TFCommonProxy {
    private ModelBiped[] knightlyArmorModel;
    private ModelBiped[] phantomArmorModel;
    private ModelBiped[] yetiArmorModel;
    private ModelBiped[] arcticArmorModel;
    private ModelBiped[] fieryArmorModel;

    private boolean isDangerOverlayShown;

    /**
     * Builds the armor models. Called by TFClientRegistry from the entity renderer registry event
     * (Minecraft's texture and font managers exist by then); the getters call it lazily as well.
     */
    @Override
    public void doOnLoadRegistration() {
        if (knightlyArmorModel != null) {
            return;
        }
        knightlyArmorModel = new ModelBiped[4];
        knightlyArmorModel[0] = new ModelTFKnightlyArmor(0, 0.5F);
        knightlyArmorModel[1] = new ModelTFKnightlyArmor(1, 1.0F);
        knightlyArmorModel[2] = new ModelTFKnightlyArmor(2, 0.5F);
        knightlyArmorModel[3] = new ModelTFKnightlyArmor(3, 0.5F);

        phantomArmorModel = new ModelBiped[2];
        phantomArmorModel[0] = new ModelTFPhantomArmor(0, 0.5F);
        phantomArmorModel[1] = new ModelTFPhantomArmor(1, 1.0F);

        yetiArmorModel = new ModelBiped[4];
        yetiArmorModel[0] = new ModelTFYetiArmor(0, 0.6F);
        yetiArmorModel[1] = new ModelTFYetiArmor(1, 1.0F);
        yetiArmorModel[2] = new ModelTFYetiArmor(2, 0.4F);
        yetiArmorModel[3] = new ModelTFYetiArmor(3, 0.55F);

        arcticArmorModel = new ModelBiped[4];
        arcticArmorModel[0] = new ModelTFArcticArmor(0, 0.6F);
        arcticArmorModel[1] = new ModelTFArcticArmor(1, 1.0F);
        arcticArmorModel[2] = new ModelTFArcticArmor(2, 0.4F);
        arcticArmorModel[3] = new ModelTFArcticArmor(3, 0.55F);

        fieryArmorModel = new ModelBiped[4];
        fieryArmorModel[0] = new ModelTFFieryArmor(0, 0.5F);
        fieryArmorModel[1] = new ModelTFFieryArmor(1, 1.0F);
        fieryArmorModel[2] = new ModelTFFieryArmor(2, 0.5F);
        fieryArmorModel[3] = new ModelTFFieryArmor(3, 0.5F);
    }

    @Override
    public int getCritterBlockRenderID() {
        return TFRenderTypes.CRITTER;
    }

    @Override
    public int getPlantBlockRenderID() {
        return TFRenderTypes.PLANT;
    }

    @Override
    public int getComplexBlockRenderID() {
        return TFRenderTypes.COMPLEX;
    }

    @Override
    public int getNagastoneBlockRenderID() {
        return TFRenderTypes.NAGASTONE;
    }

    @Override
    public int getMagicLeavesBlockRenderID() {
        return TFRenderTypes.MAGIC_LEAVES;
    }

    @Override
    public int getPedestalBlockRenderID() {
        return TFRenderTypes.PEDESTAL;
    }

    @Override
    public int getThornsBlockRenderID() {
        return TFRenderTypes.THORNS;
    }

    @Override
    public int getKnightmetalBlockRenderID() {
        return TFRenderTypes.KNIGHTMETAL;
    }

    @Override
    public int getHugeLilyPadBlockRenderID() {
        return TFRenderTypes.HUGE_LILY_PAD;
    }

    @Override
    public int getCastleMagicBlockRenderID() {
        return TFRenderTypes.CASTLE_MAGIC;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    /**
     * Opens a TF container screen after the server sent the open-window packet (TFPacket OPEN_GUI).
     * The server container was already created with this window id; GuiContainer.initGui attaches the
     * client container to the player, so the id is applied afterwards.
     */
    @Override
    public void openClientGui(int windowId, int guiId, int x, int y, int z) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        GuiScreen gui = createGui(guiId, mc.thePlayer, mc.theWorld, x, y, z);
        if (gui != null) {
            mc.displayGuiScreen(gui);
            mc.thePlayer.openContainer.windowId = windowId;
        }
    }

    public static GuiScreen createGui(int guiId, EntityPlayer player, World world, int x, int y, int z) {
        if (guiId == TwilightForestMod.GUI_ID_UNCRAFTING) {
            return new GuiTFGoblinCrafting(player.inventory, world, x, y, z);
        } else if (guiId == TwilightForestMod.GUI_ID_FURNACE) {
            return new GuiTFCinderFurnace(player.inventory, world, x, y, z);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return createGui(id, player, world, x, y, z);
    }

    /**
     * Spawns a particle. This is a copy of RenderGlobal.spawnParticle where TF's custom particles are implemented.
     */
    @Override
    public void spawnParticle(World world, String particleType, double x, double y, double z, double velX, double velY, double velZ) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.renderViewEntity != null && mc.effectRenderer != null && mc.theWorld == world) {
            double distX = mc.renderViewEntity.posX - x;
            double distY = mc.renderViewEntity.posY - y;
            double distZ = mc.renderViewEntity.posZ - z;

            EntityFX particle = null;

            double maxDist = 64.0D; // normally 16.0D

            if (distX * distX + distY * distY + distZ * distZ < maxDist * maxDist) {
                if (particleType.equals("largeflame")) {
                    particle = new EntityTFLargeFlameFX(world, x, y, z, velX, velY, velZ);
                } else if (particleType.equals("hugesmoke")) {
                    particle = new EntitySmokeFX(world, x, y, z, velX, velY, velZ, 8.0f);
                } else if (particleType.equals("leafrune")) {
                    particle = new EntityTFLeafRuneFX(world, x, y, z, velX, velY, velZ);
                } else if (particleType.equals("bosstear")) {
                    particle = new EntityTFBossTearFX(world, x, y, z, velX, velY, velZ, Item.ghastTear);
                } else if (particleType.equals("ghasttrap")) {
                    particle = new EntityTFGhastTrapFX(world, x, y, z, velX, velY, velZ);
                } else if (particleType.equals("protection")) {
                    particle = new EntityTFProtectionFX(world, x, y, z, velX, velY, velZ);
                } else if (particleType.equals("snowstuff")) {
                    particle = new EntityTFSnowFX(world, x, y, z, velX, velY, velZ);
                } else if (particleType.equals("snowwarning")) {
                    particle = new EntityTFSnowWarningFX(world, x, y, z, velX, velY, velZ, 1F);
                } else if (particleType.equals("snowguardian")) {
                    particle = new EntityTFSnowGuardianFX(world, x, y, z, velX, velY, velZ, 0.75F);
                } else if (particleType.equals("icebeam")) {
                    particle = new EntityTFIceBeamFX(world, x, y, z, velX, velY, velZ, 0.75F);
                } else if (particleType.equals("annihilate")) {
                    particle = new EntityTFAnnihilateFX(world, x, y, z, velX, velY, velZ, 0.75F);
                }

                if (particle != null) {
                    particle.prevPosX = particle.posX;
                    particle.prevPosY = particle.posY;
                    particle.prevPosZ = particle.posZ;
                    mc.effectRenderer.addEffect(particle);
                }
            }
        }
    }

    @Override
    public ModelBiped getKnightlyArmorModel(int armorSlot) {
        doOnLoadRegistration();
        return knightlyArmorModel[armorSlot];
    }

    @Override
    public ModelBiped getPhantomArmorModel(int armorSlot) {
        doOnLoadRegistration();
        return phantomArmorModel[armorSlot];
    }

    @Override
    public ModelBiped getYetiArmorModel(int armorSlot) {
        doOnLoadRegistration();
        return yetiArmorModel[armorSlot];
    }

    @Override
    public ModelBiped getArcticArmorModel(int armorSlot) {
        doOnLoadRegistration();
        return arcticArmorModel[armorSlot];
    }

    @Override
    public ModelBiped getFieryArmorModel(int armorSlot) {
        doOnLoadRegistration();
        return fieryArmorModel[armorSlot];
    }

    /** Hands the protected structure box to the weather renderer of the Twilight Forest provider. */
    @Override
    public void setProtectedBox(World world, StructureBoundingBox box) {
        if (world != null && world.provider instanceof ITFProviderRenderers providerRenderers
                && providerRenderers.tfWeatherRenderer() instanceof TFRenderHandler weather) {
            weather.setProtectedBox(box);
        }
    }

    public boolean isDangerOverlayShown() {
        return isDangerOverlayShown;
    }

    public void setDangerOverlayShown(boolean isDangerOverlayShown) {
        this.isDangerOverlayShown = isDangerOverlayShown;
    }

    @Override
    public void doBlockAnnihilateEffect(World worldObj, int blockX, int blockY, int blockZ) {
        byte four = 4;

        for (int dx = 0; dx < four; ++dx) {
            for (int dy = 0; dy < four; ++dy) {
                for (int dz = 0; dz < four; ++dz) {
                    double d0 = (double) blockX + ((double) dx + 0.5D) / (double) four;
                    double d1 = (double) blockY + ((double) dy + 0.5D) / (double) four;
                    double d2 = (double) blockZ + ((double) dz + 0.5D) / (double) four;

                    double gx = worldObj.rand.nextGaussian() * 0.2D;
                    double gy = worldObj.rand.nextGaussian() * 0.2D;
                    double gz = worldObj.rand.nextGaussian() * 0.2D;

                    TwilightForestMod.proxy.spawnParticle(worldObj, "annihilate", d0, d1, d2, gx, gy, gz);
                }
            }
        }
    }
}
