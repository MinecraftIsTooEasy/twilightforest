package twilightforest.client.renderer.entity;

import net.minecraft.Tessellator;
import net.minecraft.Render;
import net.minecraft.TextureMap;
import net.minecraft.Entity;
import net.minecraft.Icon;
import net.minecraft.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import twilightforest.entity.EntityTFCharmEffect;
import twilightforest.item.TFItems;

public class RenderTFCharm extends Render
{
    /**
     * Have the icon index (in items.png) that will be used to render the image. Currently, eggs and snowballs uses this
     * classes.
     */
    private Icon itemIcon;

    public RenderTFCharm(Icon par1)
    {
        this.itemIcon = par1;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature @Override
    public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
    	if (par1Entity instanceof EntityTFCharmEffect)
    	{
    		EntityTFCharmEffect charm = (EntityTFCharmEffect)par1Entity;
    		if (charm.getItemID() > 0)
    		{
    			//this.itemIcon = Item.itemsList[charm.getItemID()].getIconFromSubtype(0);
    			this.itemIcon = TFItems.charmOfKeeping1.getIconFromSubtype(0);
    		}
    	}
    	if (this.itemIcon == null)
    	{
    		this.itemIcon = TFItems.charmOfLife1.getIconFromSubtype(0);
    	}
    	
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        this.bindEntityTexture(par1Entity);
        Tessellator var10 = Tessellator.instance;

        
        this.func_77026_a(var10, this.itemIcon);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    private void func_77026_a(Tessellator par1Tessellator, Icon par2Icon)
    {
        float f = par2Icon.getMinU();
        float f1 = par2Icon.getMaxU();
        float f2 = par2Icon.getMinV();
        float f3 = par2Icon.getMaxV();
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        par1Tessellator.startDrawingQuads();
        par1Tessellator.setNormal(0.0F, 1.0F, 0.0F);
        par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F - f6), 0.0D, (double)f, (double)f3);
        par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
        par1Tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6), 0.0D, (double)f1, (double)f2);
        par1Tessellator.addVertexWithUV((double)(0.0F - f5), (double)(f4 - f6), 0.0D, (double)f, (double)f2);
        par1Tessellator.draw();
    }
    
    @Override
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return TextureMap.locationItemsTexture;
    }
}
