package twilightforest.client;

import net.minecraft.FontRenderer;
import net.minecraft.Gui;
import net.minecraft.GuiContainer;
import net.minecraft.InventoryPlayer;
import net.minecraft.ItemStack;
import net.minecraft.RenderHelper;
import net.minecraft.ResourceLocation;
import net.minecraft.Slot;
import net.minecraft.StatCollector;
import net.minecraft.World;
import org.lwjgl.opengl.GL11;
import twilightforest.TwilightForestMod;
import twilightforest.inventory.ContainerTFUncrafting;

public class GuiTFGoblinCrafting extends GuiContainer {
    private static final ResourceLocation textureLoc = new ResourceLocation(TwilightForestMod.GUI_DIR + "guigoblintinkering.png");

    public GuiTFGoblinCrafting(InventoryPlayer inventory, World world, int x, int y, int z) {
        super(new ContainerTFUncrafting(inventory, world, x, y, z));
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("Uncrafting Table", 8, 6, 4210752);
        this.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(textureLoc);
        int frameX = (this.width - this.xSize) / 2;
        int frameY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(frameX, frameY, 0, 0, this.xSize, this.ySize);

        ContainerTFUncrafting tfContainer = (ContainerTFUncrafting) this.inventorySlots;

        // show uncrafting ingredients as background
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.guiLeft, (float) this.guiTop, 0.0F);

        for (int i = 0; i < 9; i++) {
            Slot uncrafting = tfContainer.getSlot(2 + i);
            Slot assembly = tfContainer.getSlot(11 + i);

            if (uncrafting.getStack() != null) {
                drawSlotAsBackground(uncrafting, assembly);
            }
        }
        GL11.glPopMatrix();

        // show the costs if there are any
        FontRenderer font = this.mc.fontRenderer;
        RenderHelper.disableStandardItemLighting();

        int costVal = tfContainer.getUncraftingCost();
        if (costVal > 0) {
            drawCost(font, costVal, frameX + 48, frameY + 38);
        }

        costVal = tfContainer.getRecraftingCost();
        if (costVal > 0) {
            drawCost(font, costVal, frameX + 130, frameY + 38);
        }
    }

    private void drawCost(FontRenderer font, int costVal, int rightX, int y) {
        boolean affordable = this.mc.thePlayer.getExperienceLevel() >= costVal || this.mc.thePlayer.inCreativeMode();
        int color = affordable ? 0x80FF20 : 0xA00000;
        String cost = "" + costVal;
        font.drawStringWithShadow(cost, rightX - font.getStringWidth(cost), y, color);
    }

    /**
     * Draws an inventory slot
     */
    private void drawSlotAsBackground(Slot backgroundSlot, Slot appearSlot) {
        int screenX = appearSlot.xDisplayPosition;
        int screenY = appearSlot.yDisplayPosition;
        ItemStack itemStackToRender = backgroundSlot.getStack();
        this.zLevel = 50.0F;
        itemRenderer.zLevel = 50.0F;

        itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemStackToRender, screenX, screenY);
        itemRenderer.renderItemOverlayIntoGUI(this.fontRenderer, this.mc.getTextureManager(), itemStackToRender, screenX, screenY);

        boolean itemBroken = backgroundSlot.getHasStack() && backgroundSlot.getStack().stackSize == 0;

        // draw 50% gray rectangle over the item
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        Gui.drawRect(screenX, screenY, screenX + 16, screenY + 16, itemBroken ? 0x80FF8b8b : 0x9f8b8b8b);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        itemRenderer.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }
}
