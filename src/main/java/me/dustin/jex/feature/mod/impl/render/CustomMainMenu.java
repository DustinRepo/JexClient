package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.SetScreenFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.TitleScreen;
import me.dustin.jex.feature.mod.core.Feature;

public class CustomMainMenu extends Feature {

    public Property<Boolean> customBackgroundProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Background")
            .value(true)
            .build();
    public Property<Boolean> scrollProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Scroll")
            .value(false)
            .parent(customBackgroundProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public Property<Integer> scrollDelayProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Delay (Seconds)")
            .value(5)
            .min(1)
            .max(60)
            .parent(scrollProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    public CustomMainMenu() {
        super("CustomMainMenu", Category.VISUAL, "", true, false, 0);
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof TitleScreen)
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexTitleScreen());
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getMinecraft().setScreen(new JexTitleScreen());
        event.cancel();
    }, new SetScreenFilter(TitleScreen.class));
}
