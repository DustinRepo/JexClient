package me.dustin.jex.event.packet;

import me.dustin.events.core.Event;
import net.minecraft.network.Connection;

public class EventHello extends Event {

    private final Connection clientConnection;
    private final String serverhash;

    public EventHello(Connection clientConnection, String serverhash) {
        this.clientConnection = clientConnection;
        this.serverhash = serverhash;
    }

    public Connection getClientConnection() {
        return clientConnection;
    }

    public String getServerhash() {
        return serverhash;
    }
}
