package me.dustin.jex.module.impl.render;

import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@ModClass(name = "Gui", category = ModCategory.VISUAL, description = "Opens the ClickGui.")
public class Gui extends Module {

    public static ClickGui clickgui = new ClickGui(new LiteralText("Click Gui"));

    @Op(name = "Client Color", isColor = true)
    public int clientColor = 0xff00a1ff;
    @OpChild(name = "Rainbow", parent = "Client Color")
    public boolean rainbowClientColor;

    @Op(name = "Particles")
    public boolean particles;

    public Gui() {
        this.setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        Wrapper.INSTANCE.getMinecraft().openScreen(clickgui);
        this.toggleState();
    }

    @Override
    public void onDisable() {
    }
}
