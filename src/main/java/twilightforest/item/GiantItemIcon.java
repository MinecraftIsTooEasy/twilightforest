package twilightforest.item;

import net.minecraft.Icon;

/** A quarter-area slice of a normal tool icon, used to draw the giant pick / giant sword at 2x size. */
public class GiantItemIcon implements Icon {

	private Icon baseIcon;
	private float myX;
	private float myY;

	public GiantItemIcon(Icon blockIcon, float x, float y) {
		this.baseIcon = blockIcon;
		this.myX = x;
		this.myY = y;
	}

	@Override
	public int getIconWidth() {
		return this.baseIcon.getIconWidth() / 2;
	}

	@Override
	public int getIconHeight() {
		return this.baseIcon.getIconHeight() / 2;
	}

	@Override
	public float getMinU() {
		float f = this.baseIcon.getMaxU() - this.baseIcon.getMinU();
		return this.baseIcon.getMinU() + (f * myX);
	}

	@Override
	public float getMaxU() {
		float f = this.baseIcon.getMaxU() - this.baseIcon.getMinU();
		return this.baseIcon.getMinU() + (f * (myX + 0.5F));
	}

	@Override
	public float getInterpolatedU(double par1) {
		float f = this.getMaxU() - this.getMinU();
		return this.getMinU() + f * (float)par1 / 16.0F;
	}

	@Override
	public float getMinV() {
		float f = this.baseIcon.getMaxV() - this.baseIcon.getMinV();
		return this.baseIcon.getMinV() + (f * myY);
	}

	@Override
	public float getMaxV() {
		float f = this.baseIcon.getMaxV() - this.baseIcon.getMinV();
		return this.baseIcon.getMinV() + (f * (myY + 0.5F));
	}

	@Override
	public float getInterpolatedV(double par1) {
		float f = this.getMaxV() - this.getMinV();
		return this.getMinV() + f * ((float)par1 / 16.0F);
	}

	@Override
	public String getIconName() {
		return this.baseIcon.getIconName();
	}

	@Override
	public boolean isGreenGrassSide() {
		return false;
	}
}
