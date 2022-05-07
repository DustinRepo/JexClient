package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.packet.EventHello;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.security.Signature;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinClientHandshakePacketListenerImpl {

    @Shadow @Final private Connection connection;

    @Inject(method = "handleHello", at = @At(value = "INVOKE", target = "net/minecraft/network/protocol/login/ServerboundKeyPacket.<init> (Ljavax/crypto/SecretKey;Ljava/security/PublicKey;[B)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onHelloPacket(ClientboundHelloPacket clientboundHelloPacket, CallbackInfo ci, Cipher cipher, Cipher cipher2, String string, SecretKey secretKey, PublicKey publicKey, byte[] bs, Signature signature) {
        EventHello eventHello = new EventHello(this.connection, string).run();
        if (eventHello.isCancelled())
            ci.cancel();
    }

}
