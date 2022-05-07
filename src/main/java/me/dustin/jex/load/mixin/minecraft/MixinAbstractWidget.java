package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRenderWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWidget.class)
public class MixinAbstractWidget {

    @Inject(method = "renderButton", at = @At("HEAD"), cancellable = true)
    public void renderButton1(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventRenderWidget eventRenderWidget = new EventRenderWidget((AbstractWidget)(Object)this, matrices).run();
        if (eventRenderWidget.isCancelled())
            ci.cancel();
    }

}
