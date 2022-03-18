package me.dustin.jex.helper.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
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
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.network.*;

import java.net.InetSocketAddress;

public enum ProxyHelper {
    INSTANCE;
    private ClientProxy proxy;

    public void connectToProxy(SocksType type, String hostname, int port) {
        this.proxy = new ClientProxy(hostname, port, type);
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

    public static record ClientProxy(String host, int port, SocksType socksType){}

    public enum SocksType {
        FOUR, FIVE;
    }

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = getProxy();
            String string = "Current Proxy: " + proxy.host() + ":" + proxy.port();
            FontHelper.INSTANCE.drawWithShadow(event.getMatrixStack(), string, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(string) - 2, 22, ColorHelper.INSTANCE.getClientColor());
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, MultiplayerScreen.class));

    public ClientConnection clientConnection;
    public final ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<>() {
        protected void initChannel(Channel channel) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();

            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                if (proxy.socksType() == ProxyHelper.SocksType.FIVE) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port()), null, null));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.host(), proxy.port())));
                }
            }
            try {
                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException var3) {
            }

            channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new SplitterHandler()).addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND)).addLast("prepender", new SizePrepender()).addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND)).addLast("packet_handler", clientConnection);
        }
    };
}
