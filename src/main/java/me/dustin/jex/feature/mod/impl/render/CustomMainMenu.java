package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.gui.screen.TitleScreen;

@Feature.Manifest(name = "CustomMainMenu", category = Feature.Category.VISUAL, description = "The custom main menu for Jex", enabled = true, visible = false)
public class CustomMainMenu extends Feature {

    @Op(name = "Background")
    public boolean customBackground = true;
    @OpChild(name = "Scroll", parent = "Background")
    public boolean scroll;
    @OpChild(name = "Delay (Seconds)", min = 1, max = 60, parent = "Scroll")
    public int scrollDelay = 5;

    @EventListener(events = {EventTick.class, EventDisplayScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof TitleScreen)
                Wrapper.INSTANCE.getMinecraft().openScreen(new JexTitleScreen());
        }
        if (event instanceof EventDisplayScreen eventDisplayScreen) {
            if (eventDisplayScreen.getScreen() instanceof TitleScreen) {
                Wrapper.INSTANCE.getMinecraft().openScreen(new JexTitleScreen());
            }
        }
    }

}
