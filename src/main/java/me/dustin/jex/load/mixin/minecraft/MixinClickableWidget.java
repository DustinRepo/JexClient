package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickableWidget.class)
public class MixinClickableWidget {

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void renderButton1(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventRenderWidget eventRenderWidget = new EventRenderWidget((ClickableWidget)(Object)this, matrices).run();
        if (eventRenderWidget.isCancelled())
            ci.cancel();
    }

}
