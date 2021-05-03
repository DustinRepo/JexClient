package me.dustin.jex.gui.waypoints;

import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.feature.impl.world.Waypoints;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class WaypointScreen extends Screen {
    private ArrayList<Button> serverButtons = new ArrayList<>();
    private ArrayList<Button> waypointButtons = new ArrayList<>();
    protected WaypointScreen() {
        super(new LiteralText("Waypoints"));
    }

    @Override
    protected void init() {
        for (String server : Waypoints.getServers()) {

        }
        super.init();
    }
}
