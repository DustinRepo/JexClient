package me.dustin.jex.load.mixin.minecraft;

import me.dustin.events.core.Event;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == Wrapper.INSTANCE.getWindow().getHandle()) {
            boolean bl = action == 1;
            if (bl) {
                if (button >= 2) {
                    EventKeyPressed eventKeyPressed = new EventKeyPressed(10000 + button, 0, Wrapper.INSTANCE.getMinecraft().currentScreen != null ? EventKeyPressed.PressType.IN_MENU : EventKeyPressed.PressType.IN_GAME).run();
                    if (eventKeyPressed.isCancelled())
                        ci.cancel();
                }
                if (((Event) (new EventMouseButton(button, Wrapper.INSTANCE.getMinecraft().currentScreen != null ? EventMouseButton.ClickType.IN_MENU : EventMouseButton.ClickType.IN_GAME).run())).isCancelled())
                    ci.cancel();
            }
        }
    }

}
