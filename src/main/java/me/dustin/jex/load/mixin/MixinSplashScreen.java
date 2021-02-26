package me.dustin.jex.load.mixin;

import me.dustin.jex.JexClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public class MixinSplashScreen {

    @Inject(method = "init", at = @At("HEAD"))
    private static void initClient(MinecraftClient client, CallbackInfo ci) {
        JexClient.INSTANCE.initializeClient();
    }

}
