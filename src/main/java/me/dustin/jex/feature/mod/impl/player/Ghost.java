package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.SetScreenFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventSetPlayerHealth;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.DeathScreen;

public class Ghost extends Feature {

    public Ghost() {
        super(Category.PLAYER);
    }

    @EventPointer
    private final EventListener<EventSetPlayerHealth> eventSetPlayerHealthEventListener = new EventListener<>(event -> {
        if (event.getHealth() <= 0) {
            event.cancel();
            event.setHealth(1);
        }
    });

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        event.cancel();
        event.setScreen(null);
    }, new SetScreenFilter(DeathScreen.class));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getHealth() <= 0) {
            Wrapper.INSTANCE.getLocalPlayer().setHealth(1);
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DeathScreen)
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
        if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= 0)
            Wrapper.INSTANCE.getLocalPlayer().setHealth(1);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getHealth() == 1)
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
        super.onDisable();
    }
}
