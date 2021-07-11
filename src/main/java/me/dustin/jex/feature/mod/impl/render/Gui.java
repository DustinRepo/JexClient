package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.jex.JexGui;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(name = "Gui", category = Feature.Category.VISUAL, description = "Opens the ClickGui.", key = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class Gui extends Feature {

    public static ClickGui clickgui = new ClickGui(new LiteralText("Click Gui"));
    public static JexGui jexGui = new JexGui(new LiteralText("Jex Gui"));

    @Op(name = "Mode", all = {"Jex", "Window"})
    public String mode = "Jex";

    @OpChild(name = "Colors", all = {"Customize", "Client"}, parent = "Mode", dependency = "Window")
    public String colorScheme = "Customize";

    @OpChild(name = "Combat", parent = "Colors", dependency = "Customize", isColor = true)
    public int combatColor = Hud.getCategoryColor(Feature.Category.COMBAT);
    @OpChild(name = "Player", parent = "Colors", dependency = "Customize", isColor = true)
    public int playerColor = Hud.getCategoryColor(Feature.Category.PLAYER);
    @OpChild(name = "Movement", parent = "Colors", dependency = "Customize", isColor = true)
    public int movementColor = Hud.getCategoryColor(Feature.Category.MOVEMENT);
    @OpChild(name = "Visual", parent = "Colors", dependency = "Customize", isColor = true)
    public int visualColor = Hud.getCategoryColor(Feature.Category.VISUAL);
    @OpChild(name = "World", parent = "Colors", dependency = "Customize", isColor = true)
    public int worldColor = Hud.getCategoryColor(Feature.Category.WORLD);
    @OpChild(name = "Misc", parent = "Colors", dependency = "Customize", isColor = true)
    public int miscColor = Hud.getCategoryColor(Feature.Category.MISC);

    public boolean particles = false;

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
