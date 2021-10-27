package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.packet.EventHello;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginNetworkHandler {

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onHello", at = @At(value = "INVOKE", target = "net/minecraft/text/TranslatableText.<init>(Ljava/lang/String;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onHelloPacket(LoginHelloS2CPacket packet, CallbackInfo ci) {
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateKey();
            PublicKey publicKey = packet.getPublicKey();
            String string2 = (new BigInteger(NetworkEncryptionUtils.generateServerId(packet.getServerId(), publicKey, secretKey))).toString(16);
            EventHello eventHello = new EventHello(this.connection, string2).run();
            if (eventHello.isCancelled())
                ci.cancel();
        } catch (NetworkEncryptionException var8) {
            throw new IllegalStateException("Protocol error", var8);
        }
    }

}
