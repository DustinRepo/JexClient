package me.dustin.jex.load.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.packet.EventPacketSent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {


    @Shadow
    private PacketListener packetListener;
    @Shadow
    @Final
    private NetworkSide side;
    @Shadow
    private int packetsReceivedCounter;

    @Shadow
    protected static <T extends PacketListener> void handlePacket(Packet<T> packet_1, PacketListener packetListener_1) {
        packet_1.apply((T) packetListener_1);
    }

    @Shadow
    public abstract void send(Packet<?> packet_1, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1);

    @Shadow
    public abstract boolean isOpen();

    @Inject(method = "send(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void send(Packet packet, CallbackInfo ci) {
        EventPacketSent eventPacketSent = new EventPacketSent(packet).run();
        packet = eventPacketSent.getPacket();
        if (eventPacketSent.isCancelled()) {
            ci.cancel();
            return;
        }
        send(eventPacketSent.getPacket(), (GenericFutureListener) null);
        ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo ci) {
        if (this.side == NetworkSide.CLIENTBOUND && this.isOpen()) {
            EventPacketReceive eventPacketReceive = new EventPacketReceive(packet_1).run();
            if (eventPacketReceive.isCancelled())
                ci.cancel();
        }
    }
}
