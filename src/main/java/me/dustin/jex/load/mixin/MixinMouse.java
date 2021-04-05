package me.dustin.jex.load.mixin;

import me.dustin.events.core.Event;
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
            if (bl)
                if (((Event)(new EventMouseButton(button, Wrapper.INSTANCE.getLocalPlayer() == null ? EventMouseButton.ClickType.IN_MENU : EventMouseButton.ClickType.IN_GAME).run())).isCancelled())
                    ci.cancel();
        }
    }

}
