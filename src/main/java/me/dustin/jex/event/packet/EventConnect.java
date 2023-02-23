package me.dustin.jex.event.packet;

import me.dustin.events.core.Event;
import net.minecraft.client.network.ServerAddress;

public class EventConnect extends Event {

    private final ServerAddress serverAddress;
    

    public EventConnect(ServerAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }
}
