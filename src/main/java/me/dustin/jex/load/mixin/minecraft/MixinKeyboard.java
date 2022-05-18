package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKeyListener(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1) {
            EventKeyPressed eventKeyPressed = new EventKeyPressed(key, scancode, Wrapper.INSTANCE.getMinecraft().currentScreen == null ? EventKeyPressed.PressType.IN_GAME : EventKeyPressed.PressType.IN_MENU).run();
            if (eventKeyPressed.isCancelled())
                ci.cancel();
        }
    }
}
