package me.dustin.jex.gui.waypoints.impl;

import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.gui.waypoints.WaypointScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ServerButton extends Button {

	private boolean selected;

	public ServerButton(String name, float x, float y, float width, float height, ButtonListener listener) {
		super(null, name, x, y, width, height, listener);
	}

	@Override
	public void draw(MatrixStack matrixStack) {
		Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x50000000);
		FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getName(), this.getX() + 2, this.getY() + (this.getHeight() / 2) - 4, ColorHelper.INSTANCE.getClientColor());
		if (isSelected())
			Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).brighter().getRGB() & 0x20ffffff);
		if (isHovered())
			Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
	}

	@Override
	public void click(double double_1, double double_2, int int_1) {
		if (isHovered() && int_1 == 0) {
			WaypointScreen.getServerButtons().forEach(serverButton -> serverButton.setSelected(false));
			this.setSelected(true);
		}
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
