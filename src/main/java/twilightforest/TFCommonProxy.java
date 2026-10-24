package twilightforest;

import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.StructureBoundingBox;
import net.minecraft.World;
import twilightforest.inventory.ContainerTFCinderFurnace;
import twilightforest.inventory.ContainerTFUncrafting;

/**
 * Server side of the proxy. TFClientProxy overrides these; the armor model getters return Object here so that no
 * client class appears in a common signature (the client overrides narrow it to ModelBiped).
 */
public class TFCommonProxy {

    public void doPreLoadRegistration() {
    }

    public void doOnLoadRegistration() {
    }

    public int getCritterBlockRenderID() {
        return 0;
    }

    public int getPlantBlockRenderID() {
        return 0;
    }

    public int getComplexBlockRenderID() {
        return 0;
    }

    public int getNagastoneBlockRenderID() {
        return 0;
    }

    public int getMagicLeavesBlockRenderID() {
        return 0;
    }

    public int getPedestalBlockRenderID() {
        return 0;
    }

    public int getThornsBlockRenderID() {
        return 0;
    }

    public int getKnightmetalBlockRenderID() {
        return 0;
    }

    public int getHugeLilyPadBlockRenderID() {
        return 0;
    }

    public int getCastleMagicBlockRenderID() {
        return 0;
    }

    public int registerArmorRenderID(String prefix) {
        return 0;
    }

    public World getClientWorld() {
        return null;
    }

    /** Spawns one of TF's custom string-named particles. Null op except on the client. */
    public void spawnParticle(World world, String particleType, double x, double y, double z, double velX, double velY, double velZ) {
    }

    /** Container for the custom GUI packet flow; null for unknown ids. */
    public Container getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == TwilightForestMod.GUI_ID_UNCRAFTING) {
            return new ContainerTFUncrafting(player.inventory, world, x, y, z);
        } else if (id == TwilightForestMod.GUI_ID_FURNACE) {
            return new ContainerTFCinderFurnace(player.inventory, world, x, y, z);
        }
        return null;
    }

    /** The GuiScreen for the id, only available on the client. */
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public Object getKnightlyArmorModel(int armorSlot) {
        return null;
    }

    public Object getPhantomArmorModel(int armorSlot) {
        return null;
    }

    public Object getYetiArmorModel(int armorSlot) {
        return null;
    }

    public Object getArcticArmorModel(int armorSlot) {
        return null;
    }

    public Object getFieryArmorModel(int armorSlot) {
        return null;
    }

    /** Client: opens the screen for a custom GUI packet and applies the server window id. */
    public void openClientGui(int windowId, int guiId, int x, int y, int z) {
    }

    /** Client: forwards the protected-structure box to the TF weather renderer; null clears it. */
    public void setProtectedBox(World world, StructureBoundingBox sbb) {
    }

    public void doBlockAnnihilateEffect(World worldObj, int blockX, int blockY, int blockZ) {
    }
}
