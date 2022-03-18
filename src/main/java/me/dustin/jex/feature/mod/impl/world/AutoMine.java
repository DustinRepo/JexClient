package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically mine any block you hover over.")
public class AutoMine extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getOptions().attackKey.setPressed(true);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        Wrapper.INSTANCE.getOptions().attackKey.setPressed(false);
        super.onDisable();
    }
}
