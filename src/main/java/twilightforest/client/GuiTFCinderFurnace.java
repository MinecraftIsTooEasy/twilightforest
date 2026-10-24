package twilightforest.client;

import net.minecraft.GuiContainer;
import net.minecraft.I18n;
import net.minecraft.InventoryPlayer;
import net.minecraft.ResourceLocation;
import net.minecraft.World;
import org.lwjgl.opengl.GL11;
import twilightforest.inventory.ContainerTFCinderFurnace;
import twilightforest.tileentity.TileEntityTFCinderFurnace;

public class GuiTFCinderFurnace extends GuiContainer {
    private static final ResourceLocation furnaceGuiTextures = new ResourceLocation("textures/gui/container/furnace.png");
    private TileEntityTFCinderFurnace tileFurnace;

    public GuiTFCinderFurnace(InventoryPlayer inventory, TileEntityTFCinderFurnace furnace) {
        super(new ContainerTFCinderFurnace(inventory, furnace));
        this.tileFurnace = furnace;
    }

    public GuiTFCinderFurnace(InventoryPlayer inventory, World world, int x, int y, int z) {
        this(inventory, (TileEntityTFCinderFurnace) world.getBlockTileEntity(x, y, z));
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileFurnace.hasCustomName() ? this.tileFurnace.getCustomNameOrUnlocalized() : I18n.getString(this.tileFurnace.getCustomNameOrUnlocalized());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.getString("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(furnaceGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        if (this.tileFurnace.isBurning()) {
            int i1 = this.tileFurnace.getBurnTimeRemainingScaled(13);
            this.drawTexturedModalRect(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 1);
            i1 = this.tileFurnace.getCookProgressScaled(24);
            this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
        }
    }
}
