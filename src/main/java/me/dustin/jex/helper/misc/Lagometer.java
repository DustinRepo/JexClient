package me.dustin.jex.helper.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

public enum Lagometer {
    INSTANCE;

    private StopWatch lagStopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (!(event.getPacket() instanceof ClientboundPlayerChatPacket))
            lagStopWatch.reset();
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE));

    public boolean isServerLagging() {
        return lagStopWatch.getPassed() > 1000 && !(Wrapper.INSTANCE.getMinecraft().isLocalServer() && Wrapper.INSTANCE.getMinecraft().isPaused());
    }

    public long getLagTime() {
        return lagStopWatch.getPassed();
    }

}
