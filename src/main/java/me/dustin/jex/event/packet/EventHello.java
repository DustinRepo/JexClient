package me.dustin.jex.event.packet;

import me.dustin.events.core.Event;
import net.minecraft.network.ClientConnection;

public class EventHello extends Event {

    private ClientConnection clientConnection;
    private String serverhash;

    public EventHello(ClientConnection clientConnection, String serverhash) {
        this.clientConnection = clientConnection;
        this.serverhash = serverhash;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public String getServerhash() {
        return serverhash;
    }
}
