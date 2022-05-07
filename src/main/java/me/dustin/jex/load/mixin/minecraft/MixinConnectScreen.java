package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.packet.EventConnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
    public void onConnect(Minecraft client, ServerAddress address, CallbackInfo ci) {
        EventConnect eventConnect = new EventConnect(address).run();
        if (eventConnect.isCancelled())
            ci.cancel();
    }

}
