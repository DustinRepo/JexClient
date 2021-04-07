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

    @Op(name = "Colors", all = {"Customize", "Client"})
    public String colorScheme = "Customize";

    @OpChild(name = "Combat", parent = "Colors", dependency = "Customize", isColor = true)
    public int combatColor = Hud.getCategoryColor(ModCategory.COMBAT);
    @OpChild(name = "Player", parent = "Colors", dependency = "Customize", isColor = true)
    public int playerColor = Hud.getCategoryColor(ModCategory.PLAYER);
    @OpChild(name = "Movement", parent = "Colors", dependency = "Customize", isColor = true)
    public int movementColor = Hud.getCategoryColor(ModCategory.MOVEMENT);
    @OpChild(name = "Visual", parent = "Colors", dependency = "Customize", isColor = true)
    public int visualColor = Hud.getCategoryColor(ModCategory.VISUAL);
    @OpChild(name = "World", parent = "Colors", dependency = "Customize", isColor = true)
    public int worldColor = Hud.getCategoryColor(ModCategory.WORLD);
    @OpChild(name = "Misc", parent = "Colors", dependency = "Customize", isColor = true)
    public int miscColor = Hud.getCategoryColor(ModCategory.MISC);

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
