package me.dustin.jex.gui.waypoints;

import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.WaypointFile;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.gui.waypoints.impl.ServerButton;
import me.dustin.jex.gui.waypoints.impl.WaypointButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class WaypointScreen extends Screen {

    public WaypointScreen() {
        super(new LiteralText("Waypoints"));
    }

    private String goToServer;

    public WaypointScreen(String server) {
        super(new LiteralText("Waypoints"));
    }


    private static ArrayList<ServerButton> serverButtons = new ArrayList<>();
    private static ArrayList<WaypointButton> waypointButtons = new ArrayList<>();
    private static Button editButton;

    private static Button editWaypointButton;
    private static Button deleteButton;

    private int serverCount = 0;
    private int waypointCount = 0;
    private static int page = 0;
    private int buttonWidth = 100;

    private Scrollbar scrollbar;

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        serverButtons.clear();
        waypointButtons.clear();
        serverCount = 0;
        waypointCount = 0;
        page = 0;

        ArrayList<String> servers = new ArrayList<>();
        Waypoints.waypoints.forEach(waypoint -> {
            if(!servers.contains(waypoint.getServer()))
            {
                servers.add(waypoint.getServer());
            }
        });
        servers.forEach(waypoint -> {
            serverButtons.add(new ServerButton(waypoint, getMidX() - 100, getMidY() - 90 + (20 * serverCount), 201, 20, null));
            serverCount++;
        });

        float contentHeight = serverCount * 20;
        float viewportHeight = 190;
        float scrollBarHeight = viewportHeight * (contentHeight / viewportHeight);
        scrollbar = new Scrollbar(getMidX() + buttonWidth + 2, getMidY() - 90, 2, scrollBarHeight, viewportHeight, contentHeight, -1);
        editButton = new Button(null, "Edit Waypoints", getMidX() - 100, getMidY() + 105, 200, 20, new ButtonListener() {
            @Override
            public void invoke() {
                WaypointScreen.page = 1;

                int waypointSize = Waypoints.getWaypoints(getSelectedServer().getName()).size();
                if(waypointSize < 4)
                    waypointSize = 4;

                Waypoints.getWaypoints(getSelectedServer().getName()).forEach(waypoint -> {
                    waypointButtons.add(new WaypointButton(waypoint, getMidX() - 100, (getMidY() - 90) + (20 * waypointCount), 201, 20, null));
                    waypointCount++;
                });

                scrollbar.setY(getMidY() - 90);
                scrollbar.setContentHeight(waypointCount * 20);
                editWaypointButton = new Button(null, "Edit", getMidX() - 99, getMidY() + 105, 98, 20, editListener);
                deleteButton = new Button(null, "Delete", getMidX() + 1, getMidY() + 105, 98, 20, deleteListener);

            }
        });
        if (goToServer != null && !goToServer.isEmpty()){
            serverButtons.forEach(serverButton -> {if (serverButton.getName().equalsIgnoreCase(goToServer)) { serverButton.setSelected(true); editButton.getListener().invoke();}});
        }
        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
        renderBackground(matrixStack);
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getMidX() - buttonWidth - 6, getMidY() - 105, getMidX() + buttonWidth + 6, getMidY() + 130, ColorHelper.INSTANCE.getClientColor(), 0x60000000, 1);

        if(page == 0)
        {
            Render2DHelper.INSTANCE.fill(matrixStack, getMidX() - buttonWidth - 1, getMidY() - 100, getMidX() + buttonWidth + 1, getMidY() + 100, 0x60000000);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, "Servers", getMidX(), getMidY() - 99, -1);
            Scissor.INSTANCE.cut((int)getMidX() - buttonWidth - 1, (int)getMidY() - 90, 200, 190);
            serverButtons.forEach(button -> {
                button.draw(matrixStack);
            });
            Scissor.INSTANCE.seal();
            editButton.setEnabled(getSelectedServer() != null);
            editButton.draw(matrixStack);
        }
        if(page == 1)
        {
            Render2DHelper.INSTANCE.fill(matrixStack, getMidX() - buttonWidth - 1, getMidY() - 100, getMidX() + buttonWidth + 1, getMidY() + 100, 0x60000000);
            FontHelper.INSTANCE.drawCenteredString(matrixStack, "Waypoints", getMidX(), getMidY() - 99, -1);
            Scissor.INSTANCE.cut((int)getMidX() - buttonWidth - 1, (int)getMidY() - 90, 200, 190);
            waypointButtons.forEach(button -> button.draw(matrixStack));
            Scissor.INSTANCE.seal();
            deleteButton.setEnabled(getSelectedWaypoint() != null);
            deleteButton.draw(matrixStack);
            editWaypointButton.setEnabled(getSelectedWaypoint() != null);
            editWaypointButton.draw(matrixStack);
        }
        scrollbar.render(matrixStack);
        super.render(matrixStack, int_1, int_2, float_1);
    }

    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {

            if (page == 0) {
                if (Render2DHelper.INSTANCE.isHovered((int)getMidX() - buttonWidth - 1, (int)getMidY() - 90, 200, 190)) {
                    serverButtons.forEach(serverButton -> serverButton.click(double_1, double_2, int_1));
                }
                editButton.click(double_1, double_2, int_1);
                return super.mouseClicked(double_1, double_2, int_1);
            }
            if (page == 1) {
                if (Render2DHelper.INSTANCE.isHovered((int)getMidX() - buttonWidth - 1, (int)getMidY() - 90, 200, 190)) {
                    waypointButtons.forEach(waypointButton -> waypointButton.click(double_1, double_2, int_1));
                }
                editWaypointButton.click(double_1, double_2, int_1);
                deleteButton.click(double_1, double_2, int_1);
            }
        return super.mouseClicked(double_1, double_2, int_1);
    }

    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if(int_1 == GLFW.GLFW_KEY_ESCAPE && page > 0)
        {
            page--;
            goToServer = "";
            scrollbar.setY(getMidY() - 90);
            this.init();
            return false;
        }
        else
            return super.keyPressed(int_1, int_2, int_3);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0) {
            if (!serverButtons.isEmpty()) {
                Button topButton = serverButtons.get(0);
                if (topButton != null && MouseHelper.INSTANCE.getMouseX() > topButton.getX() && MouseHelper.INSTANCE.getMouseX() < topButton.getX() + topButton.getWidth()) {
                    float topY = getMidY() - 90;
                    if (topButton.getY() < topY) {
                        for (int i = 0; i < 15; i++) {
                            if (topButton.getY() < topY) {
                                for (Button button : serverButtons) {
                                    button.move(0, 1);
                                }
                                scrollbar.moveUp();
                            }
                        }
                    }
                }
            }
            if (!waypointButtons.isEmpty()) {
                Button topJexButton = waypointButtons.get(0);
                if (topJexButton != null && MouseHelper.INSTANCE.getMouseX() > topJexButton.getX() && MouseHelper.INSTANCE.getMouseX() < topJexButton.getX() + topJexButton.getWidth()) {
                    float topY = getMidY() - 90;
                    if (topJexButton.getY() < topY) {
                        for (int i = 0; i < 15; i++) {
                            if (topJexButton.getY() < topY) {
                                for (Button button : waypointButtons) {
                                    button.move(0, 1);
                                }
                                scrollbar.moveUp();
                            }
                        }
                    }
                }
            }
        } else if (amount < 0) {
            if (!serverButtons.isEmpty()) {
                Button bottomButton = serverButtons.get(serverButtons.size() - 1);
                if (bottomButton != null && MouseHelper.INSTANCE.getMouseX() > bottomButton.getX() && MouseHelper.INSTANCE.getMouseX() < bottomButton.getX() + bottomButton.getWidth()) {
                    if (bottomButton.getY() + bottomButton.getHeight() > getMidY() + 100) {
                        for (int i = 0; i < 15; i++) {
                            if (bottomButton.getY() + bottomButton.getHeight() > getMidY() + 100) {
                                for (Button button : serverButtons) {
                                    button.move(0, -1);
                                }
                                scrollbar.moveDown();
                            }
                        }
                    }
                }
            }
            if (!waypointButtons.isEmpty()) {
                Button bottomJexButton = waypointButtons.get(waypointButtons.size() - 1);
                if (bottomJexButton != null && MouseHelper.INSTANCE.getMouseX() > bottomJexButton.getX() && MouseHelper.INSTANCE.getMouseX() < bottomJexButton.getX() + bottomJexButton.getWidth()) {
                    if (bottomJexButton.getY() + bottomJexButton.getHeight() > getMidY() + 100) {
                        for (int i = 0; i < 15; i++) {
                            if (bottomJexButton.getY() + bottomJexButton.getHeight() > getMidY() + 100) {
                                for (Button button : waypointButtons) {
                                    button.move(0, -1);
                                }
                                scrollbar.moveDown();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private float getMidX()
    {
        return Wrapper.INSTANCE.getWindow().getScaledWidth() / 2.f;
    }

    private float getMidY()
    {
        return Wrapper.INSTANCE.getWindow().getScaledHeight() / 2.f;
    }

    public static ArrayList<WaypointButton> getWaypointButtons() {
        return waypointButtons;
    }

    public static ArrayList<ServerButton> getServerButtons() {
        return serverButtons;
    }

    public ServerButton getSelectedServer()
    {
        for(ServerButton serverButton : getServerButtons())
        {
            if(serverButton.isSelected())
                return serverButton;
        }
        return null;
    }

    public WaypointButton getSelectedWaypoint()
    {
        for(WaypointButton waypointButton : getWaypointButtons())
        {
            if(waypointButton.isSelected())
                return waypointButton;
        }
        return null;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page1) {
       page = page1;
    }

    private ButtonListener deleteListener = new ButtonListener() {
        @Override
        public void invoke() {
            Waypoints.Waypoint waypoint = getSelectedWaypoint().getWaypoint();
            Waypoints.waypoints.remove(waypoint);
            waypointButtons.remove(getSelectedWaypoint());
            goToServer = getSelectedServer().getName();
            waypointCount--;
            ConfigManager.INSTANCE.get(WaypointFile.class).write();
            init();
        }
    };

    private ButtonListener editListener = new ButtonListener() {
        @Override
        public void invoke() {
            Waypoints.Waypoint waypoint = getSelectedWaypoint().getWaypoint();
            Wrapper.INSTANCE.getMinecraft().openScreen(new WaypointEditScreen(getSelectedServer().getName(), waypoint));
        }
    };
}
