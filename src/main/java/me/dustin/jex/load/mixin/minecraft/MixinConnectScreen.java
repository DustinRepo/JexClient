package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.packet.EventConnect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V", at = @At("HEAD"), cancellable = true)
    public void onConnect(MinecraftClient client, ServerAddress address, CallbackInfo ci) {
        EventConnect eventConnect = new EventConnect(address).run();
        if (eventConnect.isCancelled())
            ci.cancel();
    }

}
