package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventGetToolTipFromItem;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderBackground;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", at = @At("HEAD"), cancellable = true)
    public void renderBG(MatrixStack matrices, int vOffset, CallbackInfo ci) {
        EventRenderBackground eventRenderBackground = new EventRenderBackground(matrices).run();
        if (eventRenderBackground.isCancelled())
            ci.cancel();
    }

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    public void getToolTipText(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        EventGetToolTipFromItem eventGetToolTipFromItem = new EventGetToolTipFromItem(stack, cir.getReturnValue()).run();
        if (eventGetToolTipFromItem.getTextList() != cir.getReturnValue())
            cir.setReturnValue(eventGetToolTipFromItem.getTextList());
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void renderPre(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.PRE).run();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void renderPost(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.POST).run();
    }

}
