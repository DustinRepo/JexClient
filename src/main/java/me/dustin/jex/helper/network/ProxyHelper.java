package me.dustin.jex.helper.network;

import me.dustin.events.core.*;
import me.dustin.jex.helper.*;
import me.dustin.jex.event.*;
import io.netty.*;
import java.net.InetSocketAddress;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.network.*;

public enum ProxyHelper {
    INSTANCE;
    private ClientProxy proxy;
    
    public ProxyType type = ProxyType.SOCKS5;

    public void connectToProxy(boolean isSocks4, ProxyType type, String hostname, int port, String username, String password) {
        this.proxy = new ClientProxy(hostname, port, type, username, password);
        this.type = isSocks4 ? ProxyType.SOCKS4 : ProxyType.SOCKS5;
    }

    public boolean isConnectedToProxy() {
        return proxy != null;
    }

    public ClientProxy getProxy() {
        return proxy;
    }

    public void disconnectFromProxy() {
        proxy = null;
    }

    public record ClientProxy(String host, int port, ProxyType proxyType, String authName, String authPass){}

    public enum ProxyType {
     SOCKS4, SOCKS5
    }

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = getProxy();
            String string = "Current Proxy: " + proxy.host() + ":" + proxy.port();
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), string, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(string) - 2, 22, ColorHelper.INSTANCE.getClientColor());
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, MultiplayerScreen.class));

    public class ProxyHelper {
        private void connect(Channel channel, CallbackInfo cir) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                if (proxy.type == ProxyHelper.ProxyType.SOCKS5) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port()), proxy.authName.isEmpty() ? null : proxy.authName, proxy.authPass.isEmpty() ? null : proxy.authPass));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port())));
                }
            }
        };
}
}
