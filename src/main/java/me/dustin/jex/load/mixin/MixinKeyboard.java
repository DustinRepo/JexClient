package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard {

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void onChar(long window, int codePoint, int modifiers);

    @Shadow public abstract void onKey(long window, int key, int scancode, int action, int modifiers);

    //Really janky fix for optifabric
    @Inject(method = "setup", at = @At("RETURN"))
    public void addKeyListener(long window, CallbackInfo ci) {
        InputUtil.setKeyboardCallbacks(window, (windowx, key, scancode, action, modifiers) -> {
            this.client.execute(() -> {
                this.onKey(windowx, key, scancode, action, modifiers);
                if (action == 1)
                    new EventKeyPressed(key, scancode, Wrapper.INSTANCE.getMinecraft().currentScreen == null ? EventKeyPressed.PressType.IN_GAME : EventKeyPressed.PressType.IN_MENU).run();
            });
        }, (windowx, codePoint, modifiers) -> {
            this.client.execute(() -> {
                this.onChar(windowx, codePoint, modifiers);
            });
        });
    }

}
