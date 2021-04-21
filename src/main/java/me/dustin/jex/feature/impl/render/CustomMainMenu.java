package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.gui.screen.TitleScreen;

@Feat(name = "CustomMainMenu", category = FeatureCategory.VISUAL, description = "The custom main menu for Jex")
public class CustomMainMenu extends Feature {

    @Op(name = "Background")
    public boolean customBackground = true;
    @OpChild(name = "Scroll", parent = "Background")
    public boolean scroll;
    @OpChild(name = "Delay (Seconds)", min = 1, max = 60, parent = "Scroll")
    public int scrollDelay = 5;
    public CustomMainMenu() {
        this.setState(true);
        this.setVisible(false);
    }

    @EventListener(events = {EventTick.class, EventDisplayScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof TitleScreen)
                Wrapper.INSTANCE.getMinecraft().openScreen(new JexTitleScreen());
        }
        if (event instanceof EventDisplayScreen) {
            if (((EventDisplayScreen) event).getScreen() instanceof TitleScreen) {
                Wrapper.INSTANCE.getMinecraft().openScreen(new JexTitleScreen());
            }
        }
    }

}
