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
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.network.*;
import java.net.InetSocketAddress;
import com.google.gson.annotations.SerializedName;

public enum ProxyHelper {
    INSTANCE;
    public ClientProxy proxy;

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

    public record ClientProxy(String hostname, int port, SocksType socksType, String username, String password){}

    public enum SocksType {
        FOUR, FIVE;
    }

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (isConnectedToProxy()) {
            ProxyHelper.ClientProxy proxy = getProxy();
            String string = "Current Proxy: " + proxy.hostname() + " " + proxy.port();
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), string, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(string) - 2, 22, ColorHelper.INSTANCE.getClientColor());
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, MultiplayerScreen.class));

    public ClientConnection clientConnection;
    public final ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<>() {
        protected void initChannel(Channel channel) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                if (proxy.socksType() == ProxyHelper.SocksType.FIVE) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.hostname(), proxy.port()), proxy.username(), proxy.password()));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.hostname(), proxy.port())));
                }
            }
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);

            channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            channel.pipeline().addLast("splitter", new SplitterHandler());
            channel.pipeline().addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND));
            channel.pipeline().addLast("prepender", new SizePrepender());
            channel.pipeline().addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND));
            channel.pipeline().addLast("packet_handler", clientConnection);
        }
    };
}
