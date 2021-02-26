package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At(value = "HEAD"))
    public void onKey(long long_1, int int_1, int int_2, int int_3, int int_4, CallbackInfo ci) {
        if (int_3 == 1)
            new EventKeyPressed(int_1, int_2, Wrapper.INSTANCE.getMinecraft().currentScreen == null ? EventKeyPressed.PressType.IN_GAME : EventKeyPressed.PressType.IN_MENU).run();
    }

}
