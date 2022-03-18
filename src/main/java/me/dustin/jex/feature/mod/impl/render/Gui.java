package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.navigator.Navigator;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Opens the ClickGui.", key = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class Gui extends Feature {
    public static Gui INSTANCE;

    public Gui() {
        INSTANCE = this;
    }

    @Op(name = "Mode", all = {"Window", "Navigator"})
    public String mode = "Window";

    @Override
    public void onEnable() {
        switch (mode.toLowerCase()) {
            case "window" -> Wrapper.INSTANCE.getMinecraft().setScreen(new DropDownGui());
            case "navigator" -> Wrapper.INSTANCE.getMinecraft().setScreen(new Navigator());
            default -> Wrapper.INSTANCE.getMinecraft().setScreen(new DropDownGui());
        }
        this.toggleState();
    }

    @Override
    public void onDisable() {
    }
}
