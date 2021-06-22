package me.dustin.jex.load.mixin;

import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventGameFinishedLoading;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class MixinSplashScreen {

    @Inject(method = "init", at = @At("HEAD"))
    private static void initClient(MinecraftClient client, CallbackInfo ci) {
        JexClient.INSTANCE.initializeClient();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"))
    public void resourcesFinishedLoading(CallbackInfo ci) {
        new EventGameFinishedLoading().run();
    }


}
