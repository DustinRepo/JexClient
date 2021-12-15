package me.dustin.jex.helper.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public enum Lagometer {
    INSTANCE;

    private Timer lagTimer = new Timer();

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (!(event.getPacket() instanceof GameMessageS2CPacket))
            lagTimer.reset();
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE));

    public boolean isServerLagging() {
        return lagTimer.getPassed() > 1000;
    }

    public long getLagTime() {
        return lagTimer.getPassed();
    }

}
