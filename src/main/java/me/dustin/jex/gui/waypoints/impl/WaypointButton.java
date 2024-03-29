package me.dustin.jex.gui.waypoints.impl;

import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.gui.waypoints.WaypointScreen;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import me.dustin.jex.helper.render.Render2DHelper;

public class WaypointButton extends Button {
    private Waypoints.Waypoint waypoint;
    private boolean selected;
    public WaypointButton(Waypoints.Waypoint waypoint, float x, float y, float width, float height, ButtonListener listener) {
        super(waypoint.getName(), x, y, width, height, listener);
        this.waypoint = waypoint;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x50000000);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getName(), this.getX() + 2, this.getY() + (this.getHeight() / 2) - 4, waypoint.getColor());
        if(isSelected())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), waypoint.getColor() & 0x30ffffff);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if(isHovered() && int_1 == 0)
        {
            WaypointScreen.getWaypointButtons().forEach(serverButton -> serverButton.setSelected(false));
            this.setSelected(true);
        }
    }

    public Waypoints.Waypoint getWaypoint() {
        return waypoint;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
