package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.option.KeyBinding;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically hold W")
public class AutoWalk extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().keyForward.getDefaultKey(), true);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        try {
            KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().keyForward.getDefaultKey(), false);
        } catch (NullPointerException ignored) {
        }
        super.onDisable();
    }
}
