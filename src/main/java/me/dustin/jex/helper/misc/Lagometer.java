package me.dustin.jex.helper.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public enum Lagometer {
    INSTANCE;

    private Timer lagTimer = new Timer();

    @EventListener(events = {EventPacketReceive.class})
    public void run(EventPacketReceive eventPacketReceive) {
        if (!(eventPacketReceive.getPacket() instanceof ChatMessageC2SPacket))
            lagTimer.reset();
    }

    public boolean isServerLagging() {
        return lagTimer.getPassed() > 1000;
    }

    public long getLagTime() {
        return lagTimer.getPassed();
    }

}
