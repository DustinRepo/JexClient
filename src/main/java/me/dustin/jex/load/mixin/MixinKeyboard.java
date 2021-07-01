package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IKeyboard;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class MixinKeyboard implements IKeyboard {

    @Shadow protected abstract void onChar(long window, int codePoint, int modifiers);

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKeyListener(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == 1)
            new EventKeyPressed(key, scancode, Wrapper.INSTANCE.getMinecraft().currentScreen == null ? EventKeyPressed.PressType.IN_GAME : EventKeyPressed.PressType.IN_MENU).run();
    }

    @Override
    public void callOnChar(long window, int codePoint, int modifiers) {
        this.onChar(window, codePoint, modifiers);
    }
}
