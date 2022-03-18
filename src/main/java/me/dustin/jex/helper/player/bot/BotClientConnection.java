package me.dustin.jex.helper.player.bot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.text.TranslatableText;

public class BotClientConnection extends ClientConnection {
    private Channel channel;

    public BotClientConnection(NetworkSide side) {
        super(side);
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
    }

    @Override
    public void handleDisconnection() {
    }

    public void close() {
        if (this.channel != null && channel.isOpen()) {
            channel.close();
        }
    }
}
