package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.gui.jexgui.JexGuiScreen;
import me.dustin.jex.gui.navigator.Navigator;
import me.dustin.jex.helper.misc.Wrapper;
import org.lwjgl.glfw.GLFW;

public class Gui extends Feature {
    public static Gui INSTANCE;

    public final Property<GuiMode> guiModeProperty = new Property.PropertyBuilder<GuiMode>(this.getClass())
            .name("Mode")
            .value(GuiMode.JEX)
            .build();
    public final Property<Boolean> noCategoriesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("No Categories")
            .description("Display all mods on the first page rather than grouping by category.")
            .value(false)
            .parent(guiModeProperty)
            .depends(parent -> parent.value() == GuiMode.JEX)
            .build();

    public Gui() {
        super(Category.VISUAL, "Opens the ClickGui.", GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        switch (guiModeProperty.value()) {
            case JEX -> Wrapper.INSTANCE.getMinecraft().setScreen(new JexGuiScreen(Wrapper.INSTANCE.getMinecraft().currentScreen, noCategoriesProperty.value()));
            case NAVIGATOR -> Wrapper.INSTANCE.getMinecraft().setScreen(new Navigator());
        }
        this.toggleState();
    }

    public enum GuiMode {
        JEX, NAVIGATOR
    }
}
