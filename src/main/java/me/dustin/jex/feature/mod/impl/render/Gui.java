package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.gui.click.jex.JexGui;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Opens the ClickGui.", key = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class Gui extends Feature {
    public static Gui INSTANCE;
    public static ClickGui clickgui = new ClickGui(new LiteralText("Click Gui"));
    public static JexGui jexGui = new JexGui(new LiteralText("Jex Gui"));

    public Gui() {
        INSTANCE = this;
    }

    @Op(name = "Mode", all = {"Window", "Jex"})
    public String mode = "Window";

    @OpChild(name = "Max Window Height", min = 200, max = 800, inc = 5, parent = "Mode", dependency = "Window")
    public int maxWindowHeight = 295;
    @OpChild(name = "Colors", all = {"Customize", "Client"}, parent = "Mode", dependency = "Window")
    public String colorScheme = "Customize";

    @OpChild(name = "Combat", parent = "Colors", dependency = "Customize", isColor = true)
    public int combatColor = new Color(255, 61, 56).getRGB();
    @OpChild(name = "Player", parent = "Colors", dependency = "Customize", isColor = true)
    public int playerColor = new Color(64, 255, 83).getRGB();
    @OpChild(name = "Movement", parent = "Colors", dependency = "Customize", isColor = true)
    public int movementColor = new Color(141, 95, 255).getRGB();
    @OpChild(name = "Visual", parent = "Colors", dependency = "Customize", isColor = true)
    public int visualColor = new Color(255, 92, 252).getRGB();
    @OpChild(name = "World", parent = "Colors", dependency = "Customize", isColor = true)
    public int worldColor = new Color(74, 84, 255).getRGB();
    @OpChild(name = "Misc", parent = "Colors", dependency = "Customize", isColor = true)
    public int miscColor = new Color(247, 255, 65).getRGB();

    @Override
    public void onEnable() {
        switch (mode.toLowerCase()) {
            case "jex" -> Wrapper.INSTANCE.getMinecraft().openScreen(jexGui);
            case "window" -> Wrapper.INSTANCE.getMinecraft().openScreen(clickgui);
        }
        this.toggleState();
    }

    @Override
    public void onDisable() {
    }
}
