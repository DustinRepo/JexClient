package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.navigator.Navigator;
import me.dustin.jex.helper.misc.Wrapper;
import org.lwjgl.glfw.GLFW;

public class Gui extends Feature {
    public static Gui INSTANCE;

    public Gui() {
        super(Category.VISUAL, "Opens the ClickGui.", GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        Wrapper.INSTANCE.getMinecraft().setScreen(new Navigator());
        this.toggleState();
    }

    @Override
    public void onDisable() {
    }
}
