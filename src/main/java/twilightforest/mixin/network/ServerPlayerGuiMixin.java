package twilightforest.mixin.network;

import net.minecraft.Container;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import twilightforest.TwilightForestMod;
import twilightforest.network.ITFServerPlayer;
import twilightforest.network.TFNetwork;

/** Opens TF containers without IGuiHandler: server container + a TFPacket telling the client which screen to show. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerGuiMixin extends EntityPlayer implements ITFServerPlayer {
    @Shadow
    private int currentWindowId;

    @Shadow
    protected abstract void incrementWindowID();

    protected ServerPlayerGuiMixin(World world, String name) {
        super(world, name);
    }

    @Override
    public void tf$openGui(int guiId, int x, int y, int z) {
        Container container = (Container) TwilightForestMod.proxy.getServerGuiElement(guiId, this, this.worldObj, x, y, z);
        if (container == null) {
            return;
        }
        if (this.openContainer != this.inventoryContainer) {
            this.closeScreen();
        }
        this.incrementWindowID();
        this.sendPacket(TFNetwork.makeOpenGuiPacket(this.currentWindowId, guiId, x, y, z));
        this.openContainer = container;
        // The id must be set before addCraftingToCrafters sends the slot contents.
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.addCraftingToCrafters((ServerPlayer) (Object) this);
    }
}
