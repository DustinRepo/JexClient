package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventGameFinishedLoading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class MixinLoadingOverlay {

    @Inject(method = "registerTextures", at = @At("RETURN"))
    private static void initClient(Minecraft client, CallbackInfo ci) {
        JexClient.INSTANCE.initializeClient();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screens/Screen.init (Lnet/minecraft/client/Minecraft;II)V"))
    public void resourcesFinishedLoading(CallbackInfo ci) {
        new EventGameFinishedLoading().run();
    }


}
