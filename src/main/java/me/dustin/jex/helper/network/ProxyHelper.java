package me.dustin.jex.helper.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.dustin.events.core.annotate.EventListener;
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

    public class ClientProxy {
        private String host;
        private int port;
        private SocksType socksType;

        public ClientProxy(String host, int port, SocksType socksType) {
            this.host = host;
            this.port = port;
            this.socksType = socksType;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public SocksType getSocksType() {
            return socksType;
        }
    }

    public enum SocksType {
        FOUR, FIVE;
    }

    @EventListener(events = {EventDrawScreen.class})
    private void runMethod(EventDrawScreen eventDrawScreen) {
        if (eventDrawScreen.getScreen() instanceof MultiplayerScreen && eventDrawScreen.getMode() == EventDrawScreen.Mode.POST) {
            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();
                String string = "Current Proxy: " + proxy.getHost() + ":" + proxy.getPort();
                FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), string, Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(string) - 2, 22, ColorHelper.INSTANCE.getClientColor());
            }
        }
    }

    public ClientConnection clientConnection;
    public ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<>() {
        protected void initChannel(Channel channel) {
            ProxyHelper.ClientProxy proxy = ProxyHelper.INSTANCE.getProxy();

            if (ProxyHelper.INSTANCE.isConnectedToProxy()) {
                if (proxy.getSocksType() == ProxyHelper.SocksType.FIVE) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.getHost(), proxy.getPort()), null, null));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.getHost(), proxy.getPort())));
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
