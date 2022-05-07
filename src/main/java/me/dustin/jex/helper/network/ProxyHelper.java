package me.dustin.jex.helper.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;

import java.net.InetSocketAddress;

public enum ProxyHelper {
    INSTANCE;
    private ClientProxy proxy;

    public void connectToProxy(SocksType type, String hostname, int port, String username, String password) {
        this.proxy = new ClientProxy(hostname, port, type, username, password);
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

    public record ClientProxy(String host, int port, SocksType socksType, String authName, String authPass){}

    public enum SocksType {
        FOUR, FIVE;
    }

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = getProxy();
            String string = "Current Proxy: " + proxy.host() + ":" + proxy.port();
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), string, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(string) - 2, 22, ColorHelper.INSTANCE.getClientColor());
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, JoinMultiplayerScreen.class));

    public Connection clientConnection;
    public final ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<>() {
        protected void initChannel(Channel channel) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                if (proxy.socksType() == ProxyHelper.SocksType.FIVE) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port()), proxy.authName(), proxy.authPass()));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port())));
                }
            }
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);

            channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            channel.pipeline().addLast("splitter", new Varint21FrameDecoder());
            channel.pipeline().addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND));
            channel.pipeline().addLast("prepender", new Varint21LengthFieldPrepender());
            channel.pipeline().addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND));
            channel.pipeline().addLast("packet_handler", clientConnection);
        }
    };
}
