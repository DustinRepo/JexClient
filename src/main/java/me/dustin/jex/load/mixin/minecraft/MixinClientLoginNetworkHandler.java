package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.packet.EventHello;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.Signer;
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
import java.security.PublicKey;

@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginNetworkHandler {

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onHello", at = @At(value = "INVOKE", target = "net/minecraft/network/packet/c2s/login/LoginKeyC2SPacket.<init>(Ljavax/crypto/SecretKey;Ljava/security/PublicKey;[B)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onHelloPacket(LoginHelloS2CPacket packet, CallbackInfo ci, Cipher cipher, Cipher cipher2, String string, SecretKey secretKey, PublicKey publicKey, byte[] bs, Signer lv) {
        EventHello eventHello = new EventHello(this.connection, string).run();
        if (eventHello.isCancelled())
            ci.cancel();
    }

}
