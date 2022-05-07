package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventGetToolTipFromItem;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderBackground;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", at = @At("HEAD"), cancellable = true)
    public void renderBG(PoseStack matrices, int vOffset, CallbackInfo ci) {
        EventRenderBackground eventRenderBackground = new EventRenderBackground(matrices).run();
        if (eventRenderBackground.isCancelled())
            ci.cancel();
    }

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    public void getToolTipText(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        EventGetToolTipFromItem eventGetToolTipFromItem = new EventGetToolTipFromItem(stack, cir.getReturnValue()).run();
        if (eventGetToolTipFromItem.getTextList() != cir.getReturnValue())
            cir.setReturnValue(eventGetToolTipFromItem.getTextList());
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void renderPre(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.PRE).run();
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void renderPost(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new EventDrawScreen((Screen) (Object) this, matrices, EventDrawScreen.Mode.POST).run();
    }

}
