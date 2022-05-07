package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.SetScreenFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screens.TitleScreen;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "The custom main menu for Jex", enabled = true, visible = false)
public class CustomMainMenu extends Feature {

    @Op(name = "Background")
    public boolean customBackground = true;
    @OpChild(name = "Scroll", parent = "Background")
    public boolean scroll;
    @OpChild(name = "Delay (Seconds)", min = 1, max = 60, parent = "Scroll")
    public int scrollDelay = 5;

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof TitleScreen)
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexTitleScreen());
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getMinecraft().setScreen(new JexTitleScreen());
        event.cancel();
    }, new SetScreenFilter(TitleScreen.class));
}
