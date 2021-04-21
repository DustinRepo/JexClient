package me.dustin.jex.feature.impl.render;

import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@Feat(name = "Gui", category = FeatureCategory.VISUAL, description = "Opens the ClickGui.")
public class Gui extends Feature {

    public static ClickGui clickgui = new ClickGui(new LiteralText("Click Gui"));

    @Op(name = "Colors", all = {"Customize", "Client"})
    public String colorScheme = "Customize";

    @OpChild(name = "Combat", parent = "Colors", dependency = "Customize", isColor = true)
    public int combatColor = Hud.getCategoryColor(FeatureCategory.COMBAT);
    @OpChild(name = "Player", parent = "Colors", dependency = "Customize", isColor = true)
    public int playerColor = Hud.getCategoryColor(FeatureCategory.PLAYER);
    @OpChild(name = "Movement", parent = "Colors", dependency = "Customize", isColor = true)
    public int movementColor = Hud.getCategoryColor(FeatureCategory.MOVEMENT);
    @OpChild(name = "Visual", parent = "Colors", dependency = "Customize", isColor = true)
    public int visualColor = Hud.getCategoryColor(FeatureCategory.VISUAL);
    @OpChild(name = "World", parent = "Colors", dependency = "Customize", isColor = true)
    public int worldColor = Hud.getCategoryColor(FeatureCategory.WORLD);
    @OpChild(name = "Misc", parent = "Colors", dependency = "Customize", isColor = true)
    public int miscColor = Hud.getCategoryColor(FeatureCategory.MISC);

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
