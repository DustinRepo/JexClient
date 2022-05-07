package me.dustin.jex.load.mixin.minecraft;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.network.ProxyHelper;
import me.dustin.jex.helper.player.bot.BotClientConnection;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.util.LazyLoadedValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;

@Mixin(Connection.class)
public abstract class MixinConnection {

    @Shadow
    public abstract void send(Packet<?> packet_1, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1);

    @Shadow @Final public static LazyLoadedValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP;

    @Shadow @Final public static LazyLoadedValue<NioEventLoopGroup> NETWORK_WORKER_GROUP;

    @Shadow @Final private PacketFlow receiving;

    @Shadow public abstract boolean isConnected();

    @Inject(method = "connectToServer", at = @At("HEAD"), cancellable = true)
    private static void connect1(InetSocketAddress address, boolean useEpoll, CallbackInfoReturnable<Connection> cir) {
        if (!ProxyHelper.INSTANCE.isConnectedToProxy())
            return;
        final Connection clientConnection = new Connection(PacketFlow.CLIENTBOUND);
        Class channelClass;
        LazyLoadedValue group;
        if (Epoll.isAvailable() && useEpoll) {
            channelClass = EpollSocketChannel.class;
            group = NETWORK_EPOLL_WORKER_GROUP;
        } else {
            channelClass = NioSocketChannel.class;
            group = NETWORK_WORKER_GROUP;
        }
        ProxyHelper.INSTANCE.clientConnection = clientConnection;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap = bootstrap.group((EventLoopGroup)group.get());
        bootstrap = bootstrap.handler(ProxyHelper.INSTANCE.channelInitializer);
        bootstrap = bootstrap.channel(channelClass);
        bootstrap.connect(address.getAddress(), address.getPort()).syncUninterruptibly();

        cir.setReturnValue(clientConnection);
        cir.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send1(Packet<?> packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        EventPacketSent.EventPacketSentDirect eventPacketSent = new EventPacketSent.EventPacketSentDirect(packet, EventPacketSent.Mode.PRE).run();
        if (eventPacketSent.isCancelled()) {
            ci.cancel();
            return;
        }
        send(eventPacketSent.getPacket(), null);
        new EventPacketSent.EventPacketSentDirect(packet, EventPacketSent.Mode.POST).run();
        ci.cancel();
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        if (this.receiving == PacketFlow.CLIENTBOUND && this.isConnected() && !isBotHandler()) {
            EventPacketReceive eventPacketReceive = new EventPacketReceive(packet_1, EventPacketReceive.Mode.PRE).run();
            if (eventPacketReceive.isCancelled())
                ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("TAIL"))
    public void channelRead01(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        if (this.receiving == PacketFlow.CLIENTBOUND && this.isConnected() && !isBotHandler()) {
            new EventPacketReceive(packet_1, EventPacketReceive.Mode.POST).run();
        }
    }

    private boolean isBotHandler() {
        Connection me = (Connection)(Object)this;
        return me instanceof BotClientConnection;
    }
}
