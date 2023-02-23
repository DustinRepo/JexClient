package me.dustin.jex.helper.network;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.packet.EventConnect;
import net.minecraft.client.network.ServerAddress;

public enum ConnectedServerHelper {
    INSTANCE;
    private ServerAddress serverAddress;
    private ServerPort serverPort;

    @EventPointer
    private final EventListener<EventConnect> eventConnectEventListener = new EventListener<>(event -> {
        this.serverAddress = event.getServerAddress();
        this.serverPort = event.getServerAddress();
        
    });

    public ServerAddress getServerAddress() {
        return serverAddress;
    }
    public ServerAddress getServerPort() {
        return serverPort;
    }
}
