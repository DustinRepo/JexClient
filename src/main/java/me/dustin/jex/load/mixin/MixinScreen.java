package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventDrawScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "render", at = @At("HEAD"))
    public void renderPre(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.PRE).run();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void renderPost(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.POST).run();
    }

}
